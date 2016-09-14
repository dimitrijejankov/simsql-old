

% /*****************************************************************************
% *                                                                           *
% *  Copyright 2014 Rice University                                           *
% *                                                                           *
% *  Licensed under the Apache License, Version 2.0 (the "License");          *
% *  you may not use this file except in compliance with the License.         *
% *  You may obtain a copy of the License at                                  *
% *                                                                           *
% *      http://www.apache.org/licenses/LICENSE-2.0                           *
% *                                                                           *
% *  Unless required by applicable law or agreed to in writing, software      *
% *  distributed under the License is distributed on an "AS IS" BASIS,        *
% *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
% *  See the License for the specific language governing permissions and      *
% *  limitations under the License.                                           *
% *                                                                           *
% *****************************************************************************/


%*****************************************************************************************************************************
% RULE FOR RUNNING THE PRE_OPTIMIZER
% It receives an input plan and augments it with the necessary split operators, deletes unecessary projections
% and seed attributes and identifies the output attributes for each operator in the plan.
% NextAction should be either 'run' or 'optimize'
preoptimize(NextAction, OutFile) :-
%preoptimize(NextAction) :-
    % **** plan graph structure
    (current_predicate(parent/2) -> setof(parent(Parent, Child), parent(Parent, Child), IntGraph1); IntGraph1 = []),

    %**** plan operators
    (current_predicate(selection/2) -> setof(selection(NodeName, SelectionPreds), selection(NodeName, SelectionPreds), Selections); Selections = []),
    (current_predicate(tablescan/3) -> setof(tablescan(NodeName, RelationName, OutAttr), tablescan(NodeName, RelationName, OutAttr), TableScans); TableScans = []),
    (current_predicate(seed/2) -> setof(seed(NodeName, SeedAttr), seed(NodeName, SeedAttr), Seeds); Seeds = []),
    (current_predicate(split/2) -> setof(split(NodeName, SplitAttr), split(NodeName, SplitAttr), Splits); Splits = []),
    (current_predicate(join/3) -> setof(join(NodeName, JoinPred, _), join(NodeName, JoinPred, _), Joins); Joins = []),
    (current_predicate(antijoin/3) -> setof(antijoin(NodeName, DataSourceChildName, AntiJoinPred), antijoin(NodeName, DataSourceChildName, AntiJoinPred), AntiJoins); AntiJoins = []),
    (current_predicate(semijoin/3) -> setof(semijoin(NodeName, DataSourceChildName, SemiJoinPred), semijoin(NodeName, DataSourceChildName, SemiJoinPred), SemiJoins); SemiJoins = []),
    (current_predicate(dedup/2) -> setof(dedup(NodeName, DedupAttr), dedup(NodeName, DedupAttr), Dedups); Dedups = []),
    (current_predicate(projection/2) -> setof(projection(NodeName, ProjectionAttrs), projection(NodeName, ProjectionAttrs), Projections); Projections = []),
    (current_predicate(genagg/6) -> setof(genagg(NodeName, GenAggName, GroupByAttrs, AggExprs, AggExprsInAttrs, AggExprsOutAttrs),
                    genagg(NodeName, GenAggName, GroupByAttrs, AggExprs, AggExprsInAttrs, AggExprsOutAttrs), Aggregations); Aggregations = []),
    (current_predicate(vgwrapper/9) -> setof(vgwrapper(NodeName, VGName, VGFunc, SeedAttrIn, RestOfInnerAttrs, JoinPreds, VGParams, VGOutAttrs, SeedAttrOut),
                    vgwrapper(NodeName, VGName, VGFunc, SeedAttrIn, RestOfInnerAttrs, JoinPreds, VGParams, VGOutAttrs, SeedAttrOut), VGWrappers); VGWrappers = []),
    (current_predicate(scalarfunc/4) -> setof(scalarfunc(NodeName, FuncExprs, FuncExprsInAttrs, FuncExprsOutAttrs),
                    scalarfunc(NodeName, FuncExprs, FuncExprsInAttrs, FuncExprsOutAttrs), ScalarFuncs); ScalarFuncs = []),

    flatten([Selections, TableScans, Seeds, Joins, AntiJoins, SemiJoins, Splits, Dedups, Projections, Aggregations, VGWrappers, ScalarFuncs], IntNodes1),

    % **** outer relation information for the VGWrappers in the plan
    (current_predicate(outerRelation/2) -> setof(outerRelation(VGWrapperNodeName, OuterRelationName),
                    outerRelation(VGWrapperNodeName, OuterRelationName), IntOuterRelations); IntOuterRelations = []),

    % **** identifies the output and random attributes for each query node,
    % **** as byproduct it (1) preserves 'seed' and 'isPres' attributes through projection nodes,
    % **** (2) infers attributes for dedup nodes (3) adds 'isPres' attributes for selection, join
    % **** semi-join, anti-join and split nodes if needed and (4) adds split nodes if needed
    identifyOutputAttributes(IntGraph1, IntGraph2, IntNodes1, IntNodes2, IntOutputAttrs1, IntRandomAttrs1, IntOuterRelations),

    % **** select all seed attributes in the plan and eliminate the ones not used in higher levels in the plan
    findall(OuterSeedAttr, member(seed(_, OuterSeedAttr), IntNodes2), OuterSeedAttrs),
    findall(VGWrapperSeedAttr, member(vgwrapper(_, _, _, _, _, _, _, _, VGWrapperSeedAttr), IntNodes2), VGWrapperSeedAttrs),
    append(OuterSeedAttrs, VGWrapperSeedAttrs, PlanSeedAttrs),
    eliminateSeedAttributesFromPlan(PlanSeedAttrs, IntGraph2, IntGraph3, IntNodes2, IntNodes3, Joins, Selections,
                          IntOutputAttrs1, IntOutputAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntSeedAttrs, IntOuterRelations),

    % **** infer output, random and seed attributes for each node in the plan, by checking the attribute requirements
    % **** of the node's parents; as a by-product removes any unecessary projection nodes
    updateOutputAttributes(IntGraph3, GraphOut, IntNodes3, NodesOut, IntOutputAttrs2,
                          OutputAttrsOut, IntRandomAttrs2, RandomAttrsOut, IntSeedAttrs, IntOuterRelations, OuterRelationsOut),

    % **** add inference operator if needed
    (NextAction = run ->
        select(parent(planRoot, RootNode), GraphOut, IntGraphOut1),
        member(randomAttributes(RootNode, RootNodeRandomAttrs), RandomAttrsOut),
        (RootNodeRandomAttrs = [] ->
            ExtendedGraphOut = GraphOut,
            ExtendedNodesOut = NodesOut,
            ExtendedOutputAttrsOut = OutputAttrsOut,
            ExtendedRandomAttrsOut = RandomAttrsOut
            ;
            gensym(inferenceNode, InferenceNodeID),
            append([inference(InferenceNodeID)], NodesOut, ExtendedNodesOut),
            append([parent(planRoot, InferenceNodeID)], IntGraphOut1, IntGraphOut2),
            append([parent(InferenceNodeID, RootNode)], IntGraphOut2, ExtendedGraphOut),
            (member(isPres, RootNodeRandomAttrs) ->
                select(isPres, RootNodeRandomAttrs, InferenceNodeRandomAttrs)
                ;
                InferenceNodeRandomAttrs = RootNodeRandomAttrs
            ),
            append([randomAttributes(InferenceNodeID, InferenceNodeRandomAttrs)], RandomAttrsOut, ExtendedRandomAttrsOut),
            member(outputAttributes(RootNode, RootNodeOutputAttrs), OutputAttrsOut),
            (member(isPres, RootNodeOutputAttrs) ->
                select(isPres, RootNodeOutputAttrs, InferenceNodeOutputAttrs)
                ;
                InferenceNodeOutputAttrs = RootNodeOutputAttrs
            ),
            append([outputAttributes(InferenceNodeID, InferenceNodeOutputAttrs)], OutputAttrsOut, ExtendedOutputAttrsOut)
        )
        ;
        ExtendedGraphOut = GraphOut,
        ExtendedNodesOut = NodesOut,
        ExtendedOutputAttrsOut = OutputAttrsOut,
        ExtendedRandomAttrsOut = RandomAttrsOut
    ),
    
    % **** output augmented plan
    %outputPlan(GraphOut, NodesOut, OutputAttrsOut, RandomAttrsOut, OuterRelationsOut, OutFile).
    %outputPlan(NextAction, ExtendedGraphOut, ExtendedNodesOut, ExtendedOutputAttrsOut, ExtendedRandomAttrsOut, OuterRelationsOut).
    outputPlan(NextAction, ExtendedGraphOut, ExtendedNodesOut, ExtendedOutputAttrsOut, ExtendedRandomAttrsOut, OuterRelationsOut, OutFile).
    % ****

%*****************************************************************************************************************************
% RULE FOR RECURSIVELY IDENTIFYING ALL ATTRIBUTES THAT COME OUT OF EACH NODE IN THE ENTIRE PLAN
identifyOutputAttributes(GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsOut, RandomAttrsOut, OuterRelations) :-
    member(parent(planRoot, ChildNode), GraphIn),
    identifyOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut, [], OutputAttrsOut, [], _, [], RandomAttrsOut, OuterRelations).

%*****************************************************************************************************************************
% RULE FOR RECURSIVELY IDENTIFYING ALL ATTRIBUTES THAT COME OUT OF EACH NODE IN THE PLAN ANCOHORED BY A SPECIFIC NODE
%**** version that deals with the case where the node is a table scan
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, _) :-
    member(tablescan(WhichNode, _, TableOutputAttrs), NodesIn),
    NodesOut = NodesIn,
    GraphOut = GraphIn,

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn,
        SeedAttrsOut = SeedAttrsIn,
        RandomAttrsOut = RandomAttrsIn;

        append([outputAttributes(WhichNode, TableOutputAttrs)], OutputAttrsIn, OutputAttrsOut),
        append([seedAttributes(WhichNode, [])], SeedAttrsIn, SeedAttrsOut),
        append([randomAttributes(WhichNode, [])], RandomAttrsIn, RandomAttrsOut)).

%**** version that deals with the case where the node is a scalar function
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(scalarfunc(WhichNode, _, FuncExprsInAttrs, FuncExprsOutAttrs), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, GraphOut = GraphIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, IntOutputAttrs, SeedAttrsIn, IntSeedAttrs, RandomAttrsIn, IntRandomAttrs, OuterRelations),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        append([seedAttributes(WhichNode, ChildSeedAttrs)], IntSeedAttrs, SeedAttrsOut),

        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs),
        flatten(FuncExprsOutAttrs, FlattenedFuncExprsOutAttrs),
        append(ChildOutputAttrs, FlattenedFuncExprsOutAttrs, NodeOutputAttrs),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut),

        member(randomAttributes(ChildNode, ChildRandomAttrs), IntRandomAttrs),
        identifyExpressionOutputRandomAttrs(FuncExprsInAttrs, FuncExprsOutAttrs, ChildRandomAttrs, [], FuncOutRandomAttrs),
        append(ChildRandomAttrs, FuncOutRandomAttrs, NodeRandomAttrs),
        append([randomAttributes(WhichNode, NodeRandomAttrs)], IntRandomAttrs, RandomAttrsOut)).

%**** version that deals with the case where the node is a generalized aggregate
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(genagg(WhichNode, _, GroupByAttrs, _, AggExprsInAttrs, AggExprsOutAttrs), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, GraphOut = GraphIn, SeedAttrsIn = SeedAttrsOut, RandomAttrsOut = RandomAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyOutputAttributes(ChildNode, GraphIn, IntGraph1, NodesIn, IntNodesOut, OutputAttrsIn, IntOutputAttrs1, SeedAttrsIn, IntSeedAttrs1, RandomAttrsIn, IntRandomAttrs1, OuterRelations),

        %**** random attributes from child node  : check to see if split child is needed
        member(randomAttributes(ChildNode, ChildRandomAttrs), IntRandomAttrs1),
        intersectSets(GroupByAttrs, ChildRandomAttrs, GroupByRandomAttrs),
        
        (GroupByRandomAttrs = [] ->
            NodesOut = IntNodesOut,
            GraphOut = IntGraph1,
            IntSeedAttrs2 = IntSeedAttrs1,
            IntOutputAttrs2 = IntOutputAttrs1,
            IntRandomAttrs2 = IntRandomAttrs1,
            NewChildNode = ChildNode
            ;
            gensym(splitNode, NewSplitNodeID),
            append([split(NewSplitNodeID, GroupByRandomAttrs)], IntNodesOut, NodesOut),
            select(parent(WhichNode, ChildNode), IntGraph1, IntGraph2),
            append([parent(WhichNode, NewSplitNodeID)], IntGraph2, IntGraph3),
            append([parent(NewSplitNodeID, ChildNode)], IntGraph3, GraphOut),
            identifySplitOutputAttributes(NewSplitNodeID, GroupByRandomAttrs, ChildNode, IntOutputAttrs1, IntOutputAttrs2, IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2),
            NewChildNode = NewSplitNodeID
        ),
        
        member(seedAttributes(NewChildNode, NewChildSeedAttrs), IntSeedAttrs2),
        append([seedAttributes(WhichNode, NewChildSeedAttrs)], IntSeedAttrs2, SeedAttrsOut),

        %**** output attributes from group by and aggregate expressions
        IntNodeOutputAttrs1 = GroupByAttrs,
        flatten(AggExprsOutAttrs, FlattenedAggExprsOutAttrs),
        append(FlattenedAggExprsOutAttrs, IntNodeOutputAttrs1, IntNodeOutputAttrs2),

        member(randomAttributes(NewChildNode, NewChildRandomAttrs), IntRandomAttrs2),
        identifyExpressionOutputRandomAttrs(AggExprsInAttrs, AggExprsOutAttrs, NewChildRandomAttrs, [], IntNodeRandomAttrs),

        (member(isPres, NewChildRandomAttrs) ->
            mergeSets([isPres], IntNodeOutputAttrs2, NodeOutputAttrs),
            mergeSets([isPres], IntNodeRandomAttrs, NodeRandomAttrs)
            ;
            NodeOutputAttrs = IntNodeOutputAttrs2,
            NodeRandomAttrs = IntNodeRandomAttrs
        ),

        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs2, OutputAttrsOut),
        append([randomAttributes(WhichNode, NodeRandomAttrs)], IntRandomAttrs2, RandomAttrsOut)).

%**** version that deals with the case where the node is a vgwrapper
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(vgwrapper(WhichNode, _, _, _, _, _, _, VGOutAttrs, SeedAttrOut), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, GraphOut = GraphIn, SeedAttrsIn = SeedAttrsOut, RandomAttrsOut = RandomAttrsIn;

        % **** identify VGWrapper's children
        member(outerRelation(WhichNode, OuterRelationNode), OuterRelations),
        setof(InnerRelationNode, member(parent(WhichNode, InnerRelationNode), GraphIn), VGWrapperChildNodes),
        select(OuterRelationNode, VGWrapperChildNodes, InnerRelationNodes),
        identifyOutputAttributes(OuterRelationNode, GraphIn, IntGraph, NodesIn, IntNodes1, OutputAttrsIn, IntOutputAttrs1, SeedAttrsIn, IntSeedAttrs1, RandomAttrsIn, IntRandomAttrs1, OuterRelations),
        iterateOverVGWrapperInnerRelationsToCollectOutputAttributes(InnerRelationNodes, IntGraph, GraphOut, IntNodes1, NodesOut, IntOutputAttrs1, IntOutputAttrs2, IntRandomAttrs1, IntRandomAttrs2, OuterRelations, IntSeedAttrs1, IntSeedAttrs),

        append([seedAttributes(WhichNode, [SeedAttrOut])], IntSeedAttrs, SeedAttrsOut),

        append([SeedAttrOut], VGOutAttrs, NodeOutputAttrs),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs2, OutputAttrsOut),

        append([randomAttributes(WhichNode, VGOutAttrs)], IntRandomAttrs2, RandomAttrsOut)).

%**** version that deals with the case where the node is a selection
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(selection(WhichNode, SelectionPreds), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, GraphOut = GraphIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, IntOutputAttrs, SeedAttrsIn, IntSeedAttrs, RandomAttrsIn, IntRandomAttrs, OuterRelations),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        append([seedAttributes(WhichNode, ChildSeedAttrs)], IntSeedAttrs, SeedAttrsOut),

        member(randomAttributes(ChildNode, ChildRandomAttrs), IntRandomAttrs),
        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs),

        % **** check if needed to add isPres attribute
        (member(isPres, ChildOutputAttrs) ->
            NodeOutputAttrs = ChildOutputAttrs,
            NodeRandomAttrs = ChildRandomAttrs
            ;
            extractAttrsFromPredicates(SelectionPreds, [], PredReferencedAttrs),
            intersectSets(ChildRandomAttrs, PredReferencedAttrs, PredRandomAttrs),
            (PredRandomAttrs = [] ->
                NodeOutputAttrs = ChildOutputAttrs,
                NodeRandomAttrs = ChildRandomAttrs
                ;
                append([isPres], ChildOutputAttrs, NodeOutputAttrs),
                append([isPres], ChildRandomAttrs, NodeRandomAttrs))
        ),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut),
        append([randomAttributes(WhichNode, NodeRandomAttrs)], IntRandomAttrs, RandomAttrsOut)).

%**** version that deals with the case where the node is a projection
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(projection(WhichNode, ProjectionAttrs), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, GraphOut = GraphIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, IntNodes1, OutputAttrsIn, IntOutputAttrs, SeedAttrsIn, IntSeedAttrs, RandomAttrsIn, IntRandomAttrs, OuterRelations),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        append([seedAttributes(WhichNode, ChildSeedAttrs)], IntSeedAttrs, SeedAttrsOut),

        mergeSets(ProjectionAttrs, ChildSeedAttrs, IntNodeOutputAttrs),

        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs),
        (member(isPres, ChildOutputAttrs) -> % preserve isPres in the projected attributes
            mergeSets([isPres], IntNodeOutputAttrs, NodeOutputAttrs)
            ;
            NodeOutputAttrs = IntNodeOutputAttrs
        ),

        select(projection(WhichNode, _), IntNodes1, IntNodes2),
        append([projection(WhichNode, NodeOutputAttrs)], IntNodes2, NodesOut),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut),

        member(randomAttributes(ChildNode, ChildRandomAttrs), IntRandomAttrs),
        intersectSets(ChildRandomAttrs, NodeOutputAttrs, NodeRandomAttrs),
        append([randomAttributes(WhichNode, NodeRandomAttrs)], IntRandomAttrs, RandomAttrsOut)).

%**** version that deals with the case where the node is a seed
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(seed(WhichNode, SeedAttr), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, GraphOut = GraphIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, IntOutputAttrs, SeedAttrsIn, IntSeedAttrs, RandomAttrsIn, IntRandomAttrs, OuterRelations),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        append([SeedAttr], ChildSeedAttrs, SeedNodeSeedAttrs),
        append([seedAttributes(WhichNode, SeedNodeSeedAttrs)], IntSeedAttrs, SeedAttrsOut),

        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs),
        append([SeedAttr], ChildOutputAttrs, NodeOutputAttrs),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut),

         member(randomAttributes(ChildNode, ChildRandomAttrs), IntRandomAttrs),
         append([randomAttributes(WhichNode, ChildRandomAttrs)], IntRandomAttrs, RandomAttrsOut)).

%**** version that deals with the case where the node is a duplicate elimination
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(dedup(WhichNode, DedupAttrs), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, GraphOut = GraphIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyOutputAttributes(ChildNode, GraphIn, IntGraph1, NodesIn, IntNodes1, OutputAttrsIn, IntOutputAttrs1, SeedAttrsIn, IntSeedAttrs1, RandomAttrsIn, IntRandomAttrs1, OuterRelations),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs1),
        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs1),
        (DedupAttrs = [] ->      % **** complete dedup attributes
            select(dedup(WhichNode, _), IntNodes1, IntNodes2),
            (member(isPres, ChildOutputAttrs) -> select(isPres, ChildOutputAttrs, IntDedupAttr); IntDedupAttr = ChildOutputAttrs),
            subtractSet(IntDedupAttr, ChildSeedAttrs, NewDedupAttrs),
            append([dedup(WhichNode, NewDedupAttrs)], IntNodes2, IntNodes3)
            ;
            IntNodes3 = IntNodes1,
            NewDedupAttrs = DedupAttrs
        ),
        
        % **** check if a split is needed
        member(randomAttributes(ChildNode, ChildRandomAttrs), IntRandomAttrs1),
        intersectSets(NewDedupAttrs, ChildRandomAttrs, DedupRandomAttrs),
        (DedupRandomAttrs = [] ->
            NodesOut = IntNodes3,
            GraphOut = IntGraph1,
            IntSeedAttrs2 = IntSeedAttrs1,
            IntOutputAttrs2 = IntOutputAttrs1,
            IntRandomAttrs2 = IntRandomAttrs1,
            NewChildNode = ChildNode
            ;
            gensym(splitNode, NewSplitNodeID),
            append([split(NewSplitNodeID, DedupRandomAttrs)], IntNodes3, NodesOut),
            select(parent(WhichNode, ChildNode), IntGraph1, IntGraph2),
            append([parent(WhichNode, NewSplitNodeID)], IntGraph2, IntGraph3),
            append([parent(NewSplitNodeID, ChildNode)], IntGraph3, GraphOut),
            identifySplitOutputAttributes(NewSplitNodeID, DedupRandomAttrs, ChildNode, IntOutputAttrs1, IntOutputAttrs2, IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2),
            NewChildNode = NewSplitNodeID
        ),
        
        member(seedAttributes(NewChildNode, NewChildSeedAttrs), IntSeedAttrs2),
        append([seedAttributes(WhichNode, NewChildSeedAttrs)], IntSeedAttrs2, SeedAttrsOut),
        
        member(outputAttributes(NewChildNode, NewChildOutputAttrs), IntOutputAttrs2),
        append([outputAttributes(WhichNode, NewChildOutputAttrs)], IntOutputAttrs2, OutputAttrsOut),

        member(randomAttributes(NewChildNode, NewChildRandomAttrs), IntRandomAttrs2),
        append([randomAttributes(WhichNode, NewChildRandomAttrs)], IntRandomAttrs2, RandomAttrsOut)).

%**** version that deals with the case where the node is a join
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(join(WhichNode, JoinPreds, _), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, GraphOut = GraphIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

        member(parent(WhichNode, JoinLHSChildID), GraphIn),
        member(parent(WhichNode, JoinRHSChildID), GraphIn),
        JoinLHSChildID @> JoinRHSChildID,

        identifyOutputAttributes(JoinLHSChildID, GraphIn, IntGraph1, NodesIn, IntNodes1, OutputAttrsIn, IntOutputAttrs1, SeedAttrsIn, IntSeedAttrs1, RandomAttrsIn, IntRandomAttrs1, OuterRelations),
        identifyOutputAttributes(JoinRHSChildID, IntGraph1, IntGraph2, IntNodes1, IntNodes2, IntOutputAttrs1, IntOutputAttrs2, IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, OuterRelations),

        member(randomAttributes(JoinLHSChildID, IntJoinLHSChildRandomAttrs), IntRandomAttrs2),
        member(randomAttributes(JoinRHSChildID, IntJoinRHSChildRandomAttrs), IntRandomAttrs2),

        % *** check and add split nodes if necessary
        extractAttrsFromPredicates(JoinPreds, [], PredReferencedAttrs),
        intersectSets(PredReferencedAttrs, IntJoinLHSChildRandomAttrs, JoinLHSChildSplitAttrs),
        (JoinLHSChildSplitAttrs = [] ->
            IntNodes3 = IntNodes2,
            IntGraph5 = IntGraph2,
            IntOutputAttrs3 = IntOutputAttrs2,
            IntSeedAttrs3 = IntSeedAttrs2,
            IntRandomAttrs3 = IntRandomAttrs2,
            NewJoinLHSChildID = JoinLHSChildID
            ;
            gensym(splitNode, LHSSplitNodeID),
            append([split(LHSSplitNodeID, JoinLHSChildSplitAttrs)], IntNodes2, IntNodes3),
            select(parent(WhichNode, JoinLHSChildID), IntGraph2, IntGraph3),
            append([parent(WhichNode, LHSSplitNodeID)], IntGraph3, IntGraph4),
            append([parent(LHSSplitNodeID, JoinLHSChildID)], IntGraph4, IntGraph5),
            identifySplitOutputAttributes(LHSSplitNodeID, JoinLHSChildSplitAttrs, JoinLHSChildID, IntOutputAttrs2, IntOutputAttrs3, IntSeedAttrs2, IntSeedAttrs3, IntRandomAttrs2, IntRandomAttrs3),
            NewJoinLHSChildID = LHSSplitNodeID
        ),

        intersectSets(PredReferencedAttrs, IntJoinRHSChildRandomAttrs, JoinRHSChildSplitAttrs),
        (JoinRHSChildSplitAttrs = [] ->
            NodesOut = IntNodes3,
            GraphOut = IntGraph5,
            IntOutputAttrs4 = IntOutputAttrs3,
            IntSeedAttrs4 = IntSeedAttrs3,
            IntRandomAttrs4 = IntRandomAttrs3,
            NewJoinRHSChildID = JoinRHSChildID
            ;
            gensym(splitNode, RHSSplitNodeID),
            append([split(RHSSplitNodeID, JoinRHSChildSplitAttrs)], IntNodes3, NodesOut),
            select(parent(WhichNode, JoinRHSChildID), IntGraph5, IntGraph6),
            append([parent(WhichNode, RHSSplitNodeID)], IntGraph6, IntGraph7),
            append([parent(RHSSplitNodeID, JoinRHSChildID)], IntGraph7, GraphOut),
            identifySplitOutputAttributes(RHSSplitNodeID, JoinRHSChildSplitAttrs, JoinRHSChildID, IntOutputAttrs3, IntOutputAttrs4, IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4),
            NewJoinRHSChildID = RHSSplitNodeID
        ),
        
        member(seedAttributes(NewJoinLHSChildID, JoinLHSChildSeedAttrs), IntSeedAttrs4),
        member(seedAttributes(NewJoinRHSChildID, JoinRHSChildSeedAttrs), IntSeedAttrs4),
        mergeSets(JoinLHSChildSeedAttrs, JoinRHSChildSeedAttrs, JoinSeedAttrs),
        append([seedAttributes(WhichNode, JoinSeedAttrs)], IntSeedAttrs4, SeedAttrsOut),
        
        member(randomAttributes(NewJoinLHSChildID, JoinLHSChildRandomAttrs), IntRandomAttrs4),
        member(randomAttributes(NewJoinRHSChildID, JoinRHSChildRandomAttrs), IntRandomAttrs4),
        mergeSets(JoinLHSChildRandomAttrs, JoinRHSChildRandomAttrs, IntJoinRandomAttrs),
        member(outputAttributes(NewJoinLHSChildID, JoinLHSChildOutputAttrs), IntOutputAttrs4),
        member(outputAttributes(NewJoinRHSChildID, JoinRHSChildOutputAttrs), IntOutputAttrs4),
        mergeSets(JoinLHSChildOutputAttrs, JoinRHSChildOutputAttrs, IntNodeOutputAttrs),
        (member(isPres, IntNodeOutputAttrs) ->
            NodeOutputAttrs = IntNodeOutputAttrs,
            JoinRandomAttrs = IntJoinRandomAttrs;

            intersectSets(PredReferencedAttrs, IntJoinRandomAttrs, JoinPredRandomAttrs),
            (JoinPredRandomAttrs = [] ->
                NodeOutputAttrs = IntNodeOutputAttrs,
                JoinRandomAttrs = IntJoinRandomAttrs
                ;
                append([isPres], IntNodeOutputAttrs, NodeOutputAttrs),
                append([isPres], IntJoinRandomAttrs, JoinRandomAttrs)
            )
        ),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs4, OutputAttrsOut),
        append([randomAttributes(WhichNode, JoinRandomAttrs)], IntRandomAttrs4, RandomAttrsOut)).

%**** version that deals with the case where the node is an antijoin
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(antijoin(WhichNode, DataSourceChildID, AntiJoinPreds), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, GraphOut = GraphIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

        select(parent(WhichNode, DataSourceChildID), GraphIn, IntGraph),
        member(parent(WhichNode, OtherChildID), IntGraph),

        identifyOutputAttributes(DataSourceChildID, GraphIn, IntGraph1, NodesIn, IntNodes1, OutputAttrsIn, IntOutputAttrs1, SeedAttrsIn, IntSeedAttrs1, RandomAttrsIn, IntRandomAttrs1, OuterRelations),
        identifyOutputAttributes(OtherChildID, IntGraph1, IntGraph2, IntNodes1, IntNodes2, IntOutputAttrs1, IntOutputAttrs2, IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, OuterRelations),

        member(randomAttributes(DataSourceChildID, IntDataSourceChildRandomAttrs), IntRandomAttrs1),
        member(randomAttributes(OtherChildID, IntOtherChildRandomAttrs), IntRandomAttrs2),

        % *** check and add split nodes if necessary
        extractAttrsFromPredicates(AntiJoinPreds, [], PredReferencedAttrs),
        intersectSets(PredReferencedAttrs, IntDataSourceChildRandomAttrs, DataSourceChildSplitAttrs),
        (DataSourceChildSplitAttrs = [] ->
            IntNodes5 = IntNodes2,
            IntGraph5 = IntGraph2,
            IntOutputAttrs3 = IntOutputAttrs2,
            IntSeedAttrs3 = IntSeedAttrs2,
            IntRandomAttrs3 = IntRandomAttrs2,
            NewDataSourceChildID = DataSourceChildID
            ;
            gensym(splitNode, DataSourceSplitNodeID),
            append([split(DataSourceSplitNodeID, DataSourceChildSplitAttrs)], IntNodes2, IntNodes3),
            select(antijoin(WhichNode, DataSourceChildID, AntiJoinPreds), IntNodes3, IntNodes4),
            append([antijoin(WhichNode, DataSourceSplitNodeID, AntiJoinPreds)], IntNodes4, IntNodes5),
            select(parent(WhichNode, DataSourceChildID), IntGraph2, IntGraph3),
            append([parent(WhichNode, DataSourceSplitNodeID)], IntGraph3, IntGraph4),
            append([parent(DataSourceSplitNodeID, DataSourceChildID)], IntGraph4, IntGraph5),
            identifySplitOutputAttributes(DataSourceSplitNodeID, DataSourceChildSplitAttrs, DataSourceChildID, IntOutputAttrs2, IntOutputAttrs3, IntSeedAttrs2, IntSeedAttrs3, IntRandomAttrs2, IntRandomAttrs3),
            NewDataSourceChildID = DataSourceSplitNodeID
        ),

        intersectSets(PredReferencedAttrs, IntOtherChildRandomAttrs, OtherChildSplitAttrs),
        (OtherChildSplitAttrs = [] ->
            NodesOut = IntNodes5,
            GraphOut = IntGraph5,
            IntOutputAttrs4 = IntOutputAttrs3,
            IntSeedAttrs4 = IntSeedAttrs3,
            IntRandomAttrs4 = IntRandomAttrs3,
            NewOtherChildID = OtherChildID
            ;
            gensym(splitNode, OtherChildSplitNodeID),
            append([split(OtherChildSplitNodeID, OtherChildSplitAttrs)], IntNodes5, NodesOut),
            select(parent(WhichNode, OtherChildID), IntGraph5, IntGraph6),
            append([parent(WhichNode, OtherChildSplitNodeID)], IntGraph6, IntGraph7),
            append([parent(OtherChildSplitNodeID, OtherChildID)], IntGraph7, GraphOut),
            identifySplitOutputAttributes(OtherChildSplitNodeID, OtherChildSplitAttrs, OtherChildID, IntOutputAttrs3, IntOutputAttrs4, IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4),
            NewOtherChildID = OtherChildSplitNodeID
        ),

        member(seedAttributes(NewDataSourceChildID, DataSourceChildSeedAttrs), IntSeedAttrs4),
        append([seedAttributes(WhichNode, DataSourceChildSeedAttrs)], IntSeedAttrs4, SeedAttrsOut),

        member(randomAttributes(NewDataSourceChildID, DataSourceChildRandomAttrs), IntRandomAttrs4),
        member(randomAttributes(NewOtherChildID, OtherChildRandomAttrs), IntRandomAttrs4),
        mergeSets(DataSourceChildRandomAttrs, OtherChildRandomAttrs, IntJoinRandomAttrs),

        member(outputAttributes(NewDataSourceChildID, DataSourceChildOutputAttrs), IntOutputAttrs4),
        (member(isPres, DataSourceChildOutputAttrs) ->
            NodeOutputAttrs = DataSourceChildOutputAttrs,
            JoinRandomAttrs = DataSourceChildRandomAttrs;

            extractAttrsFromPredicates(AntiJoinPreds, [], PredReferencedAttrs),
            intersectSets(PredReferencedAttrs, IntJoinRandomAttrs, JoinPredRandomAttrs),
            (JoinPredRandomAttrs = [] ->
                NodeOutputAttrs = DataSourceChildOutputAttrs,
                JoinRandomAttrs = DataSourceChildRandomAttrs
                ;
                append([isPres], DataSourceChildOutputAttrs, NodeOutputAttrs),
                append([isPres], DataSourceChildRandomAttrs, JoinRandomAttrs)
            )
        ),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs4, OutputAttrsOut),
        append([randomAttributes(WhichNode, JoinRandomAttrs)], IntRandomAttrs4, RandomAttrsOut)).

%**** version that deals with the case where the node is a semijoin
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(semijoin(WhichNode, DataSourceChildID, SemiJoinPreds), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, GraphOut = GraphIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

        select(parent(WhichNode, DataSourceChildID), GraphIn, IntGraph),
        member(parent(WhichNode, OtherChildID), IntGraph),

        identifyOutputAttributes(DataSourceChildID, GraphIn, IntGraph1, NodesIn, IntNodes1, OutputAttrsIn, IntOutputAttrs1, SeedAttrsIn, IntSeedAttrs1, RandomAttrsIn, IntRandomAttrs1, OuterRelations),
        identifyOutputAttributes(OtherChildID, IntGraph1, IntGraph2, IntNodes1, IntNodes2, IntOutputAttrs1, IntOutputAttrs2, IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, OuterRelations),

        member(randomAttributes(DataSourceChildID, IntDataSourceChildRandomAttrs), IntRandomAttrs1),
        member(randomAttributes(OtherChildID, IntOtherChildRandomAttrs), IntRandomAttrs2),

        % *** check and add split nodes if necessary
        extractAttrsFromPredicates(SemiJoinPreds, [], PredReferencedAttrs),
        intersectSets(PredReferencedAttrs, IntDataSourceChildRandomAttrs, DataSourceChildSplitAttrs),
        (DataSourceChildSplitAttrs = [] ->
            IntNodes5 = IntNodes2,
            IntGraph5 = IntGraph2,
            IntOutputAttrs3 = IntOutputAttrs2,
            IntSeedAttrs3 = IntSeedAttrs2,
            IntRandomAttrs3 = IntRandomAttrs2,
            NewDataSourceChildID = DataSourceChildID
            ;
            gensym(splitNode, DataSourceSplitNodeID),
            append([split(DataSourceSplitNodeID, DataSourceChildSplitAttrs)], IntNodes2, IntNodes3),
            select(semijoin(WhichNode, DataSourceChildID, SemiJoinPreds), IntNodes3, IntNodes4),
            append([semijoin(WhichNode, DataSourceSplitNodeID, SemiJoinPreds)], IntNodes4, IntNodes5),
            select(parent(WhichNode, DataSourceChildID), IntGraph2, IntGraph3),
            append([parent(WhichNode, DataSourceSplitNodeID)], IntGraph3, IntGraph4),
            append([parent(DataSourceSplitNodeID, DataSourceChildID)], IntGraph4, IntGraph5),
            identifySplitOutputAttributes(DataSourceSplitNodeID, DataSourceChildSplitAttrs, DataSourceChildID, IntOutputAttrs2, IntOutputAttrs3, IntSeedAttrs2, IntSeedAttrs3, IntRandomAttrs2, IntRandomAttrs3),
            NewDataSourceChildID = DataSourceSplitNodeID
        ),

        intersectSets(PredReferencedAttrs, IntOtherChildRandomAttrs, OtherChildSplitAttrs),
        (OtherChildSplitAttrs = [] ->
            NodesOut = IntNodes5,
            GraphOut = IntGraph5,
            IntOutputAttrs4 = IntOutputAttrs3,
            IntSeedAttrs4 = IntSeedAttrs3,
            IntRandomAttrs4 = IntRandomAttrs3,
            NewOtherChildID = OtherChildID
            ;
            gensym(splitNode, OtherChildSplitNodeID),
            append([split(OtherChildSplitNodeID, OtherChildSplitAttrs)], IntNodes5, NodesOut),
            select(parent(WhichNode, OtherChildID), IntGraph5, IntGraph6),
            append([parent(WhichNode, OtherChildSplitNodeID)], IntGraph6, IntGraph7),
            append([parent(OtherChildSplitNodeID, OtherChildID)], IntGraph7, GraphOut),
            identifySplitOutputAttributes(OtherChildSplitNodeID, OtherChildSplitAttrs, OtherChildID, IntOutputAttrs3, IntOutputAttrs4, IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4),
            NewOtherChildID = OtherChildSplitNodeID
        ),

        member(seedAttributes(NewDataSourceChildID, DataSourceChildSeedAttrs), IntSeedAttrs4),
        append([seedAttributes(WhichNode, DataSourceChildSeedAttrs)], IntSeedAttrs4, SeedAttrsOut),

        member(randomAttributes(NewDataSourceChildID, DataSourceChildRandomAttrs), IntRandomAttrs4),
        member(randomAttributes(NewOtherChildID, OtherChildRandomAttrs), IntRandomAttrs4),
        mergeSets(DataSourceChildRandomAttrs, OtherChildRandomAttrs, IntJoinRandomAttrs),

        member(outputAttributes(NewDataSourceChildID, DataSourceChildOutputAttrs), IntOutputAttrs4),
        (member(isPres, DataSourceChildOutputAttrs) ->
            NodeOutputAttrs = DataSourceChildOutputAttrs,
            JoinRandomAttrs = DataSourceChildRandomAttrs;

            extractAttrsFromPredicates(SemiJoinPreds, [], PredReferencedAttrs),
            intersectSets(PredReferencedAttrs, IntJoinRandomAttrs, JoinPredRandomAttrs),
            (JoinPredRandomAttrs = [] ->
                NodeOutputAttrs = DataSourceChildOutputAttrs,
                JoinRandomAttrs = DataSourceChildRandomAttrs
                ;
                append([isPres], DataSourceChildOutputAttrs, NodeOutputAttrs),
                append([isPres], DataSourceChildRandomAttrs, JoinRandomAttrs)
            )
        ),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs4, OutputAttrsOut),
        append([randomAttributes(WhichNode, JoinRandomAttrs)], IntRandomAttrs4, RandomAttrsOut)).

%*****************************************************************************************************************************
% RULE FOR IDENTIFING THE OUTPUT, RANDOM AND SEED ATTRIBUTES FOR EACH NODE BASED ON THE ATTRIBUTE REQUIREMENTS OF THE NODE'S PARENT
updateOutputAttributes(GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, RandomAttrsIn, RandomAttrsOut, SeedAttrsIn, OuterRelationsIn, OuterRelationsOut) :-
    member(parent(planRoot, ChildNode), GraphIn),
    updateOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut,
                 OutputAttrsIn, _, [], OutputAttrsOut, RandomAttrsIn, _, [], RandomAttrsOut,
                 SeedAttrsIn, _, [], _, OuterRelationsIn, OuterRelationsOut).

%*****************************************************************************************************************************
% RULE FOR IDENTIFING THE OUTPUT, RANDOM AND SEED ATTRIBUTES FOR EACH NODE BASED ON THE ATTRIBUTE REQUIREMENTS OF THE NODE'S
% PARENT FOR THE PLAN ANCORED FROM A SPECIFIC NODE
% **** version that deals with a projection node; need to eliminate it
updateOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, PrevOutputAttrsIn, PrevOutputAttrsOut, OutputAttrsIn, OutputAttrsOut, PrevRandomAttrsIn,
                   PrevRandomAttrsOut, RandomAttrsIn, RandomAttrsOut, PrevSeedAttrsIn, PrevSeedAttrsOut, SeedAttrsIn, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut) :-
    member(projection(WhichNode, _), NodesIn),

    select(projection(WhichNode, _), NodesIn, IntNodes1),

    % **** find child of projection node
    select(parent(WhichNode, ChildNode), GraphIn, IntGraph1),
    % **** find parents of projection node
    setof(parent(ProjectionParentID, WhichNode),
               member(parent(ProjectionParentID, WhichNode), IntGraph1), ProjectionNodeParents),

    select(outputAttributes(WhichNode, NodeOutputAttrs), PrevOutputAttrsIn, IntPrevOutputAttrs),
    select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, IntPrevRandomAttrs),
    select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, IntPrevSeedAttrs),
    
    member(parent(ParentNode, WhichNode), GraphIn),

    ((ProjectionNodeParents = [parent(planRoot, WhichNode)]; member(outerRelation(ParentNode, WhichNode), OuterRelationsIn)) ->  % **** preserve projection
        append([outputAttributes(WhichNode, NodeOutputAttrs)], OutputAttrsIn, IntOutputAttrs),
        append([randomAttributes(WhichNode, NodeRandomAttrs)], RandomAttrsIn, IntRandomAttrs),
        append([seedAttributes(WhichNode, NodeSeedAttrs)], SeedAttrsIn, IntSeedAttrs),

        updateOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut, IntPrevOutputAttrs, PrevOutputAttrsOut, IntOutputAttrs, OutputAttrsOut, IntPrevRandomAttrs,
                      PrevRandomAttrsOut, IntRandomAttrs, RandomAttrsOut, IntPrevSeedAttrs, PrevSeedAttrsOut, IntSeedAttrs, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut)
        ;

        % **** and make them the parents of the join node
        swapNodeParents(ProjectionNodeParents, WhichNode, ChildNode, IntGraph1, IntGraph2, IntNodes1, IntNodes2),

        % **** update outer relation info if necessary
        findall(outerRelation(VGWrapperNodeID, WhichNode), member(outerRelation(VGWrapperNodeID, WhichNode), OuterRelationsIn), ProjectionOuterRelations),
        findall(outerRelation(VGWrapperNodeID, ChildNode), member(outerRelation(VGWrapperNodeID, WhichNode), OuterRelationsIn), ChildOuterRelations),
        subtractSet(OuterRelationsIn, ProjectionOuterRelations, IntOuterRelations1),
        mergeSets(IntOuterRelations1, ChildOuterRelations, IntOuterRelations2),

        updateOutputAttributes(ChildNode, IntGraph2, GraphOut, IntNodes2, NodesOut, IntPrevOutputAttrs, PrevOutputAttrsOut, OutputAttrsIn, OutputAttrsOut, IntPrevRandomAttrs,
                      PrevRandomAttrsOut, RandomAttrsIn, RandomAttrsOut, IntPrevSeedAttrs, PrevSeedAttrsOut, SeedAttrsIn, SeedAttrsOut, IntOuterRelations2, OuterRelationsOut)
    ).

% **** version that deals with a tablescan node
updateOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, PrevOutputAttrsIn, PrevOutputAttrsOut, OutputAttrsIn, OutputAttrsOut, PrevRandomAttrsIn,
                   PrevRandomAttrsOut, RandomAttrsIn, RandomAttrsOut, PrevSeedAttrsIn, PrevSeedAttrsOut, SeedAttrsIn, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut) :-
    member(tablescan(WhichNode, _, _), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        GraphOut = GraphIn, OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, PrevSeedAttrsOut = PrevSeedAttrsIn,
        RandomAttrsOut = RandomAttrsIn, OuterRelationsOut = OuterRelationsIn, PrevOutputAttrsOut = PrevOutputAttrsIn, PrevRandomAttrsOut = PrevRandomAttrsIn;

        % **** find parent nodes
        setof(TableScanParentID, member(parent(TableScanParentID, WhichNode), GraphIn), TableScanParents),
        (TableScanParents = [planRoot] ->
            NodesOut = NodesIn,
            GraphOut = GraphIn,
            OuterRelationsOut = OuterRelationsIn,

            select(outputAttributes(WhichNode, NodeOutputAttrs), PrevOutputAttrsIn, PrevOutputAttrsOut),
            append([outputAttributes(WhichNode, NodeOutputAttrs)], OutputAttrsIn, OutputAttrsOut),

            select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, PrevRandomAttrsOut),
            append([randomAttributes(WhichNode, NodeRandomAttrs)], RandomAttrsIn, RandomAttrsOut),

            select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, PrevSeedAttrsOut),
            append([seedAttributes(WhichNode, NodeSeedAttrs)], SeedAttrsIn, SeedAttrsOut)
            ;
            findall(UnvisitedParent, (member(UnvisitedParent, TableScanParents), not(member(outputAttributes(UnvisitedParent, _), OutputAttrsIn))), UnvisitedParents),

            (UnvisitedParents = [] -> % **** all parents have already been visited
                NodesOut = NodesIn,
                GraphOut = GraphIn,
                OuterRelationsOut = OuterRelationsIn,

                identifyRequiredAttributes(TableScanParents, [], RequiredAttrs, GraphOut, NodesOut, OutputAttrsIn, PrevOutputAttrsIn),

                select(outputAttributes(WhichNode, NodeOutputAttrs), PrevOutputAttrsIn, PrevOutputAttrsOut),
                intersectSets(RequiredAttrs, NodeOutputAttrs, NewNodeOutputAttrs),
                append([outputAttributes(WhichNode, NewNodeOutputAttrs)], OutputAttrsIn, OutputAttrsOut),

                select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, PrevRandomAttrsOut),
                intersectSets(RequiredAttrs, NodeRandomAttrs, NewNodeRandomAttrs),
                append([randomAttributes(WhichNode, NewNodeRandomAttrs)], RandomAttrsIn, RandomAttrsOut),

                select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, PrevSeedAttrsOut),
                intersectSets(RequiredAttrs, NodeSeedAttrs, NewNodeSeedAttrs),
                append([seedAttributes(WhichNode, NewNodeSeedAttrs)], SeedAttrsIn, SeedAttrsOut)
                ;
                GraphOut = GraphIn, OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, PrevSeedAttrsOut = PrevSeedAttrsIn,
                RandomAttrsOut = RandomAttrsIn, OuterRelationsOut = OuterRelationsIn, PrevOutputAttrsOut = PrevOutputAttrsIn, PrevRandomAttrsOut = PrevRandomAttrsIn
            )
        )
    ).

% **** version that deals with a selection/scalarfunc/genagg/seed/dedup/split node
updateOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, PrevOutputAttrsIn, PrevOutputAttrsOut, OutputAttrsIn, OutputAttrsOut, PrevRandomAttrsIn,
                   PrevRandomAttrsOut, RandomAttrsIn, RandomAttrsOut, PrevSeedAttrsIn, PrevSeedAttrsOut, SeedAttrsIn, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut) :-
    (member(selection(WhichNode, _), NodesIn);
     member(scalarfunc(WhichNode, _, _, _), NodesIn);
     member(genagg(WhichNode, _, _, _, _, _), NodesIn);
     member(seed(WhichNode, _), NodesIn);
     member(dedup(WhichNode, _), NodesIn);
     member(split(WhichNode, _), NodesIn)
    ),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        GraphOut = GraphIn, OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, PrevSeedAttrsOut = PrevSeedAttrsIn,
        RandomAttrsOut = RandomAttrsIn, OuterRelationsOut = OuterRelationsIn, PrevOutputAttrsOut = PrevOutputAttrsIn, PrevRandomAttrsOut = PrevRandomAttrsIn;

        % **** find parent nodes
        setof(NodeParentID, member(parent(NodeParentID, WhichNode), GraphIn), NodeParents),
        % **** and child node
        member(parent(WhichNode, ChildNode), GraphIn),

        (NodeParents = [planRoot] ->
            select(outputAttributes(WhichNode, NodeOutputAttrs), PrevOutputAttrsIn, IntPrevOutputAttrs),
            append([outputAttributes(WhichNode, NodeOutputAttrs)], OutputAttrsIn, IntOutputAttrs),

            select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, IntPrevRandomAttrs),
            append([randomAttributes(WhichNode, NodeRandomAttrs)], RandomAttrsIn, IntRandomAttrs),

            select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, IntPrevSeedAttrs),
            append([seedAttributes(WhichNode, NodeSeedAttrs)], SeedAttrsIn, IntSeedAttrs),

            updateOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut, IntPrevOutputAttrs, PrevOutputAttrsOut, IntOutputAttrs, OutputAttrsOut, IntPrevRandomAttrs,
                      PrevRandomAttrsOut, IntRandomAttrs, RandomAttrsOut, IntPrevSeedAttrs, PrevSeedAttrsOut, IntSeedAttrs, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut)
            ;
            findall(UnvisitedParent, (member(UnvisitedParent, NodeParents), not(member(outputAttributes(UnvisitedParent, _), OutputAttrsIn))), UnvisitedParents),

            (UnvisitedParents = [] -> % **** all parents have already been visited
                identifyRequiredAttributes(NodeParents, [], RequiredAttrs, GraphIn, NodesIn, OutputAttrsIn, PrevOutputAttrsIn),

                select(outputAttributes(WhichNode, NodeOutputAttrs), PrevOutputAttrsIn, IntPrevOutputAttrs),
                intersectSets(RequiredAttrs, NodeOutputAttrs, NewNodeOutputAttrs),
                append([outputAttributes(WhichNode, NewNodeOutputAttrs)], OutputAttrsIn, IntOutputAttrs),

                select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, IntPrevRandomAttrs),
                intersectSets(RequiredAttrs, NodeRandomAttrs, NewNodeRandomAttrs),
                append([randomAttributes(WhichNode, NewNodeRandomAttrs)], RandomAttrsIn, IntRandomAttrs),

                select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, IntPrevSeedAttrs),
                intersectSets(RequiredAttrs, NodeSeedAttrs, NewNodeSeedAttrs),
                append([seedAttributes(WhichNode, NewNodeSeedAttrs)], SeedAttrsIn, IntSeedAttrs),

                updateOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut, IntPrevOutputAttrs, PrevOutputAttrsOut, IntOutputAttrs, OutputAttrsOut, IntPrevRandomAttrs,
                      PrevRandomAttrsOut, IntRandomAttrs, RandomAttrsOut, IntPrevSeedAttrs, PrevSeedAttrsOut, IntSeedAttrs, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut)
                ;
                GraphOut = GraphIn, OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, PrevSeedAttrsOut = PrevSeedAttrsIn,
                RandomAttrsOut = RandomAttrsIn, OuterRelationsOut = OuterRelationsIn, PrevOutputAttrsOut = PrevOutputAttrsIn, PrevRandomAttrsOut = PrevRandomAttrsIn
            )
        )
    ).

% **** version that deals with a join, semi-join or anti-join node
updateOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, PrevOutputAttrsIn, PrevOutputAttrsOut, OutputAttrsIn, OutputAttrsOut, PrevRandomAttrsIn,
                   PrevRandomAttrsOut, RandomAttrsIn, RandomAttrsOut, PrevSeedAttrsIn, PrevSeedAttrsOut, SeedAttrsIn, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut) :-
    (member(join(WhichNode, _, _), NodesIn);
     member(semijoin(WhichNode, _, _), NodesIn);
     member(antijoin(WhichNode, _, _), NodesIn)
    ),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        GraphOut = GraphIn, OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, PrevSeedAttrsOut = PrevSeedAttrsIn,
        RandomAttrsOut = RandomAttrsIn, OuterRelationsOut = OuterRelationsIn, PrevOutputAttrsOut = PrevOutputAttrsIn, PrevRandomAttrsOut = PrevRandomAttrsIn;

        % **** find parent nodes
        setof(NodeParentID, member(parent(NodeParentID, WhichNode), GraphIn), NodeParents),
        % **** and children nodes
        member(parent(WhichNode, JoinLHSChildID), GraphIn),
        member(parent(WhichNode, JoinRHSChildID), GraphIn),
        JoinLHSChildID @> JoinRHSChildID,

        (NodeParents = [planRoot] ->
            select(outputAttributes(WhichNode, NodeOutputAttrs), PrevOutputAttrsIn, IntPrevOutputAttrs1),
            append([outputAttributes(WhichNode, NodeOutputAttrs)], OutputAttrsIn, IntOutputAttrs1),

            select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, IntPrevRandomAttrs1),
            append([randomAttributes(WhichNode, NodeRandomAttrs)], RandomAttrsIn, IntRandomAttrs1),

            select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, IntPrevSeedAttrs1),
            append([seedAttributes(WhichNode, NodeSeedAttrs)], SeedAttrsIn, IntSeedAttrs1),

            updateOutputAttributes(JoinLHSChildID, GraphIn, IntGraphOut, NodesIn, IntNodesOut, IntPrevOutputAttrs1, IntPrevOutputAttrs2, IntOutputAttrs1, IntOutputAttrs2, IntPrevRandomAttrs1,
                          IntPrevRandomAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntPrevSeedAttrs1, IntPrevSeedAttrs2, IntSeedAttrs1, IntSeedAttrs2, OuterRelationsIn, IntOuterRelations),
            updateOutputAttributes(JoinRHSChildID, IntGraphOut, GraphOut, IntNodesOut, NodesOut, IntPrevOutputAttrs2, PrevOutputAttrsOut, IntOutputAttrs2, OutputAttrsOut, IntPrevRandomAttrs2,
                         PrevRandomAttrsOut, IntRandomAttrs2, RandomAttrsOut, IntPrevSeedAttrs2, PrevSeedAttrsOut, IntSeedAttrs2, SeedAttrsOut, IntOuterRelations, OuterRelationsOut)
            ;
            findall(UnvisitedParent, (member(UnvisitedParent, NodeParents), not(member(outputAttributes(UnvisitedParent, _), OutputAttrsIn))), UnvisitedParents),

            (UnvisitedParents = [] -> % **** all parents have already been visited
                identifyRequiredAttributes(NodeParents, [], RequiredAttrs, GraphIn, NodesIn, OutputAttrsIn, PrevOutputAttrsIn),

                select(outputAttributes(WhichNode, NodeOutputAttrs), PrevOutputAttrsIn, IntPrevOutputAttrs1),
                intersectSets(RequiredAttrs, NodeOutputAttrs, NewNodeOutputAttrs),
                append([outputAttributes(WhichNode, NewNodeOutputAttrs)], OutputAttrsIn, IntOutputAttrs1),

                select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, IntPrevRandomAttrs1),
                intersectSets(RequiredAttrs, NodeRandomAttrs, NewNodeRandomAttrs),
                append([randomAttributes(WhichNode, NewNodeRandomAttrs)], RandomAttrsIn, IntRandomAttrs1),

                select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, IntPrevSeedAttrs1),
                intersectSets(RequiredAttrs, NodeSeedAttrs, NewNodeSeedAttrs),
                append([seedAttributes(WhichNode, NewNodeSeedAttrs)], SeedAttrsIn, IntSeedAttrs1),

                updateOutputAttributes(JoinLHSChildID, GraphIn, IntGraphOut, NodesIn, IntNodesOut, IntPrevOutputAttrs1, IntPrevOutputAttrs2, IntOutputAttrs1, IntOutputAttrs2, IntPrevRandomAttrs1,
                          IntPrevRandomAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntPrevSeedAttrs1, IntPrevSeedAttrs2, IntSeedAttrs1, IntSeedAttrs2, OuterRelationsIn, IntOuterRelations),
                updateOutputAttributes(JoinRHSChildID, IntGraphOut, GraphOut, IntNodesOut, NodesOut, IntPrevOutputAttrs2, PrevOutputAttrsOut, IntOutputAttrs2, OutputAttrsOut, IntPrevRandomAttrs2,
                         PrevRandomAttrsOut, IntRandomAttrs2, RandomAttrsOut, IntPrevSeedAttrs2, PrevSeedAttrsOut, IntSeedAttrs2, SeedAttrsOut, IntOuterRelations, OuterRelationsOut)
                ;
                GraphOut = GraphIn, OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, PrevSeedAttrsOut = PrevSeedAttrsIn,
                RandomAttrsOut = RandomAttrsIn, OuterRelationsOut = OuterRelationsIn, PrevOutputAttrsOut = PrevOutputAttrsIn, PrevRandomAttrsOut = PrevRandomAttrsIn
            )
        )
    ).

% **** version that deals with a vgwrapper node
updateOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, PrevOutputAttrsIn, PrevOutputAttrsOut, OutputAttrsIn, OutputAttrsOut, PrevRandomAttrsIn,
                   PrevRandomAttrsOut, RandomAttrsIn, RandomAttrsOut, PrevSeedAttrsIn, PrevSeedAttrsOut, SeedAttrsIn, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut) :-
    member(vgwrapper(WhichNode, _, _, _, _, _, _, _, _), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        GraphOut = GraphIn, OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, PrevSeedAttrsOut = PrevSeedAttrsIn,
        RandomAttrsOut = RandomAttrsIn, OuterRelationsOut = OuterRelationsIn, PrevOutputAttrsOut = PrevOutputAttrsIn, PrevRandomAttrsOut = PrevRandomAttrsIn;

        % **** find parent nodes
        setof(NodeParentID, member(parent(NodeParentID, WhichNode), GraphIn), NodeParents),

        (NodeParents = [planRoot] ->
            select(outputAttributes(WhichNode, NodeOutputAttrs), PrevOutputAttrsIn, IntPrevOutputAttrs1),
            append([outputAttributes(WhichNode, NodeOutputAttrs)], OutputAttrsIn, IntOutputAttrs1),

            select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, IntPrevRandomAttrs1),
            append([randomAttributes(WhichNode, NodeRandomAttrs)], RandomAttrsIn, IntRandomAttrs1),

            select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, IntPrevSeedAttrs1),
            append([seedAttributes(WhichNode, NodeSeedAttrs)], SeedAttrsIn, IntSeedAttrs1),

            member(outerRelation(WhichNode, OuterRelationNode), OuterRelationsIn),
            setof(VGWrapperChildNode, member(parent(WhichNode, VGWrapperChildNode), GraphIn), VGWrapperChildNodes),
            select(OuterRelationNode, VGWrapperChildNodes, InnerRelationNodes),
            updateOutputAttributes(OuterRelationNode, GraphIn, IntGraphOut, NodesIn, IntNodesOut, IntPrevOutputAttrs1, IntPrevOutputAttrs2, IntOutputAttrs1, IntOutputAttrs2, IntPrevRandomAttrs1,
                   IntPrevRandomAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntPrevSeedAttrs1, IntPrevSeedAttrs2, IntSeedAttrs1, IntSeedAttrs2, OuterRelationsIn, IntOuterRelations),
            iterateOverVGWrapperInnerRelationsToUpdateOutputAttributes(InnerRelationNodes, IntGraphOut, GraphOut, IntNodesOut, NodesOut, IntPrevOutputAttrs2, PrevOutputAttrsOut, IntOutputAttrs2, OutputAttrsOut,
                   IntPrevRandomAttrs2, PrevRandomAttrsOut, IntRandomAttrs2, RandomAttrsOut, IntPrevSeedAttrs2, PrevSeedAttrsOut, IntSeedAttrs2, SeedAttrsOut, IntOuterRelations, OuterRelationsOut)
            ;
            findall(UnvisitedParent, (member(UnvisitedParent, NodeParents), not(member(outputAttributes(UnvisitedParent, _), OutputAttrsIn))), UnvisitedParents),

            (UnvisitedParents = [] -> % **** all parents have already been visited
                identifyRequiredAttributes(NodeParents, [], RequiredAttrs, GraphIn, NodesIn, OutputAttrsIn, PrevOutputAttrsIn),

                select(outputAttributes(WhichNode, NodeOutputAttrs), PrevOutputAttrsIn, IntPrevOutputAttrs1),
                intersectSets(RequiredAttrs, NodeOutputAttrs, NewNodeOutputAttrs),
                append([outputAttributes(WhichNode, NewNodeOutputAttrs)], OutputAttrsIn, IntOutputAttrs1),

                select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, IntPrevRandomAttrs1),
                intersectSets(RequiredAttrs, NodeRandomAttrs, NewNodeRandomAttrs),
                append([randomAttributes(WhichNode, NewNodeRandomAttrs)], RandomAttrsIn, IntRandomAttrs1),

                select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, IntPrevSeedAttrs1),
                intersectSets(RequiredAttrs, NodeSeedAttrs, NewNodeSeedAttrs),
                append([seedAttributes(WhichNode, NewNodeSeedAttrs)], SeedAttrsIn, IntSeedAttrs1),

                member(outerRelation(WhichNode, OuterRelationNode), OuterRelationsIn),
                setof(VGWrapperChildNode, member(parent(WhichNode, VGWrapperChildNode), GraphIn), VGWrapperChildNodes),
                select(OuterRelationNode, VGWrapperChildNodes, InnerRelationNodes),
                updateOutputAttributes(OuterRelationNode, GraphIn, IntGraphOut, NodesIn, IntNodesOut, IntPrevOutputAttrs1, IntPrevOutputAttrs2, IntOutputAttrs1, IntOutputAttrs2, IntPrevRandomAttrs1,
                   IntPrevRandomAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntPrevSeedAttrs1, IntPrevSeedAttrs2, IntSeedAttrs1, IntSeedAttrs2, OuterRelationsIn, IntOuterRelations),
                iterateOverVGWrapperInnerRelationsToUpdateOutputAttributes(InnerRelationNodes, IntGraphOut, GraphOut, IntNodesOut, NodesOut, IntPrevOutputAttrs2, PrevOutputAttrsOut, IntOutputAttrs2, OutputAttrsOut,
                   IntPrevRandomAttrs2, PrevRandomAttrsOut, IntRandomAttrs2, RandomAttrsOut, IntPrevSeedAttrs2, PrevSeedAttrsOut, IntSeedAttrs2, SeedAttrsOut, IntOuterRelations, OuterRelationsOut)
               ;
               GraphOut = GraphIn, OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, PrevSeedAttrsOut = PrevSeedAttrsIn,
               RandomAttrsOut = RandomAttrsIn, OuterRelationsOut = OuterRelationsIn, PrevOutputAttrsOut = PrevOutputAttrsIn, PrevRandomAttrsOut = PrevRandomAttrsIn
            )
        )
    ).

%*****************************************************************************************************************************
% RULE THAT IDENTIFIES THE NODES THAT ARE NEEDED FOR A GIVEN SET OF NODES
identifyRequiredAttributes([Node|OtherNodes], RequiredAttrsIn, RequiredAttrsOut, Graph, Nodes, OutputAttrs, PrevOutputAttrs) :-
    identifySingleNodeRequiredAttributes(Node, RequiredAttrs, Graph, Nodes, OutputAttrs, PrevOutputAttrs),
    mergeSets(RequiredAttrs, RequiredAttrsIn, IntRequiredAttrs),
    identifyRequiredAttributes(OtherNodes, IntRequiredAttrs, RequiredAttrsOut, Graph, Nodes, OutputAttrs, PrevOutputAttrs).

identifyRequiredAttributes([], RequiredAttrsIn, RequiredAttrsOut, _, _, _, _) :-
    RequiredAttrsOut = RequiredAttrsIn.

%*****************************************************************************************************************************
% RULE THAT IDENTIFIES THE NODES THAT ARE NEEDED FOR A GIVEN NODES
% **** Version that deals with a seed
%identifySingleNodeRequiredAttributes(WhichNode, RequiredAttrs, Graph, Nodes, OutputAttrs, PrevOutputAttrs) :-
identifySingleNodeRequiredAttributes(WhichNode, RequiredAttrs, _, Nodes, OutputAttrs, _) :-
    member(seed(WhichNode, _), Nodes),
    %member(parent(WhichNode, ChildNode), Graph),
    member(outputAttributes(WhichNode, RequiredAttrs), OutputAttrs).
    %member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
    %member(outputAttributes(ChildNode, ChildOutputAttrs), PrevOutputAttrs),
    %mergeSets(NodeOutputAttrs, ChildOutputAttrs, RequiredAttrs).
  
% **** Version that deals with a projection
identifySingleNodeRequiredAttributes(WhichNode, RequiredAttrs, _, Nodes, OutputAttrs, _) :-
    member(projection(WhichNode, _), Nodes),
    member(outputAttributes(WhichNode, RequiredAttrs), OutputAttrs).

% **** Version that deals with a scalarfun
identifySingleNodeRequiredAttributes(WhichNode, RequiredAttrs, _, Nodes, OutputAttrs, _) :-
    member(scalarfunc(WhichNode, _, Attrs, _), Nodes),
    member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
    flatten(Attrs, FlattenedAttrs),
    mergeSets(NodeOutputAttrs, FlattenedAttrs, RequiredAttrs).

% **** Version that deals with a genagg
identifySingleNodeRequiredAttributes(WhichNode, RequiredAttrs, _, Nodes, OutputAttrs, _) :-
    member(genagg(WhichNode, _, GroupByAttrs, _, Attrs, _), Nodes),
    member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
    flatten(Attrs, FlattenedAttrs),
    mergeSets(NodeOutputAttrs, FlattenedAttrs, IntRequiredAttrs),
    mergeSets(IntRequiredAttrs, GroupByAttrs, RequiredAttrs).
    
% **** Version that deals with a dedup/split
identifySingleNodeRequiredAttributes(WhichNode, RequiredAttrs, _, Nodes, OutputAttrs, _) :-
    (member(dedup(WhichNode, Attrs), Nodes);
     member(split(WhichNode, Attrs), Nodes)),
    member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
    mergeSets(NodeOutputAttrs, Attrs, RequiredAttrs).

% **** Version that deals with a selection/join/semijoin/antijoin
identifySingleNodeRequiredAttributes(WhichNode, RequiredAttrs, _, Nodes, OutputAttrs, _) :-
    (member(selection(WhichNode, Preds), Nodes);
     member(join(WhichNode, Preds, _), Nodes);
     member(semijoin(WhichNode, _, Preds), Nodes);
     member(antijoin(WhichNode, _, Preds), Nodes)),

    extractAttrsFromPredicates(Preds, [], PredReferencedAttrs),
    member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
    mergeSets(NodeOutputAttrs, PredReferencedAttrs, RequiredAttrs).

% **** Version that deals with a selection/join/semijoin/antijoin
identifySingleNodeRequiredAttributes(WhichNode, RequiredAttrs, _, Nodes, _, _) :-
    member(vgwrapper(WhichNode, _, _, SeedAttrIn, RestOfInnerAttrs, _, _, _, _), Nodes),
    mergeSets([SeedAttrIn], RestOfInnerAttrs, RequiredAttrs).

%*****************************************************************************************************************************
% RULEs THAT ITERATE OVER A VGWRAPPER's INNER RELATIONS TO COLLECT THE OUTPUT ATTRIBUTES FOR THE PLANS ANCORED AT THEM
iterateOverVGWrapperInnerRelationsToCollectOutputAttributes([WhichNode|OtherNodes], GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelationsIn, SeedAttrsIn, SeedAttrsOut) :-
    identifyOutputAttributes(WhichNode, GraphIn, IntGraph, NodesIn, IntNodes, OutputAttrsIn, IntOutputAttrs, SeedAttrsIn, IntSeedAttrs, RandomAttrsIn, IntRandomAttrs, OuterRelationsIn),
    iterateOverVGWrapperInnerRelationsToCollectOutputAttributes(OtherNodes, IntGraph, GraphOut, IntNodes, NodesOut, IntOutputAttrs, OutputAttrsOut, IntRandomAttrs, RandomAttrsOut, OuterRelationsIn, IntSeedAttrs, SeedAttrsOut).

iterateOverVGWrapperInnerRelationsToCollectOutputAttributes([], GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, RandomAttrsIn, RandomAttrsOut, _, SeedAttrsIn, SeedAttrsOut) :-
    NodesOut = NodesIn,
    OutputAttrsOut = OutputAttrsIn,
    RandomAttrsOut = RandomAttrsIn,
    SeedAttrsOut = SeedAttrsIn,
    GraphOut = GraphIn.

%*****************************************************************************************************************************
% RULEs THAT ITERATE OVER A VGWRAPPER's INNER RELATIONS TO UPDATE THE OUTPUT ATTRIBUTES FOR THE PLANS ANCORED AT THEM
iterateOverVGWrapperInnerRelationsToUpdateOutputAttributes([WhichNode|OtherNodes], GraphIn, GraphOut, NodesIn, NodesOut, PrevOutputAttrsIn, PrevOutputAttrsOut, OutputAttrsIn, OutputAttrsOut,
                   PrevRandomAttrsIn, PrevRandomAttrsOut, RandomAttrsIn, RandomAttrsOut, PrevSeedAttrsIn, PrevSeedAttrsOut, SeedAttrsIn, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut) :-
    updateOutputAttributes(WhichNode, GraphIn, IntGraphOut, NodesIn, IntNodesOut, PrevOutputAttrsIn, IntPrevOutputAttrs, OutputAttrsIn, IntOutputAttrs,
                   PrevRandomAttrsIn, IntPrevRandomAttrs, RandomAttrsIn, IntRandomAttrs, PrevSeedAttrsIn, IntPrevSeedAttrs, SeedAttrsIn, IntSeedAttrs, OuterRelationsIn, IntOuterRelations),
    iterateOverVGWrapperInnerRelationsToUpdateOutputAttributes(OtherNodes, IntGraphOut, GraphOut, IntNodesOut, NodesOut, IntPrevOutputAttrs, PrevOutputAttrsOut, IntOutputAttrs, OutputAttrsOut,
                   IntPrevRandomAttrs, PrevRandomAttrsOut, IntRandomAttrs, RandomAttrsOut, IntPrevSeedAttrs, PrevSeedAttrsOut, IntSeedAttrs, SeedAttrsOut, IntOuterRelations, OuterRelationsOut).

iterateOverVGWrapperInnerRelationsToUpdateOutputAttributes([], GraphIn, GraphOut, NodesIn, NodesOut, PrevOutputAttrsIn, PrevOutputAttrsOut, OutputAttrsIn, OutputAttrsOut,
                   PrevRandomAttrsIn, PrevRandomAttrsOut, RandomAttrsIn, RandomAttrsOut, PrevSeedAttrsIn, PrevSeedAttrsOut, SeedAttrsIn, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut) :-
    GraphOut = GraphIn,
    NodesOut = NodesIn,
    PrevOutputAttrsOut = PrevOutputAttrsIn,
    OutputAttrsOut = OutputAttrsIn,
    PrevRandomAttrsOut = PrevRandomAttrsIn,
    RandomAttrsOut = RandomAttrsIn,
    PrevSeedAttrsOut = PrevSeedAttrsIn,
    SeedAttrsOut = SeedAttrsIn,
    OuterRelationsOut = OuterRelationsIn.

%*****************************************************************************************************************************
% RULEs THAT EXTRACT THE REFERENCED ATTRIBUTES FROM A SET OF PREDICATES
extractAttrsFromPredicates([Pred|OtherPreds], ReferencedAttrsIn, ReferencedAttrsOut) :-
    extractAttrsFromSinglePredicate(Pred, ReferencedAttrsIn, IntReferencedAttrs),
    extractAttrsFromPredicates(OtherPreds, IntReferencedAttrs, ReferencedAttrsOut).

extractAttrsFromPredicates([], ReferencedAttrsIn, ReferencedAttrsOut) :-
     ReferencedAttrsOut = ReferencedAttrsIn.

%*****************************************************************************************************************************
% RULEs THAT EXTRACT THE ATTRIBUTES THAT ARE BY A PREDICATE
% **** version that deals with equality predicates
extractAttrsFromSinglePredicate(Pred, ReferencedAttrsIn, ReferencedAttrsOut) :-
    current_predicate(compExp/5),
    %equals(Pred, LHSAttr, RHSAttr),
    compExp(Pred, equals, LHSAttr, RHSAttr, RHSAttrType),
    mergeSets([LHSAttr], ReferencedAttrsIn, IntReferencedAttrs),
    %(RHSAttr = literal -> ReferencedAttrsOut = IntReferencedAttrs; mergeSets([RHSAttr], IntReferencedAttrs, ReferencedAttrsOut)).
    (RHSAttrType = literal -> ReferencedAttrsOut = IntReferencedAttrs; mergeSets([RHSAttr], IntReferencedAttrs, ReferencedAttrsOut)).
    
% **** version that deals with non-equality predicates
extractAttrsFromSinglePredicate(Pred, ReferencedAttrsIn, ReferencedAttrsOut) :-
    %current_predicate(otherComparisonOp/4),
    %otherComparisonOp(Pred, _, LHSAttr, RHSAttr),
    current_predicate(compExp/5),
    compExp(Pred, CompExpType, LHSAttr, RHSAttr, RHSAttrType),
    CompExpType \= equals,
    mergeSets([LHSAttr], ReferencedAttrsIn, IntReferencedAttrs),
    %(RHSAttr = literal -> ReferencedAttrsOut = IntReferencedAttrs; mergeSets([RHSAttr], IntReferencedAttrs, ReferencedAttrsOut)).
    (RHSAttrType = literal -> ReferencedAttrsOut = IntReferencedAttrs; mergeSets([RHSAttr], IntReferencedAttrs, ReferencedAttrsOut)).
    
% **** version that deals with OR predicates
extractAttrsFromSinglePredicate(Pred, ReferencedAttrsIn, ReferencedAttrsOut) :-
    current_predicate(boolOr/2),
    boolOr(Pred, OrPreds),
    extractAttrsFromPredicates(OrPreds, ReferencedAttrsIn, ReferencedAttrsOut).

%*****************************************************************************************************************************
% RULE THAT INFER WHICH OF THE OUTPUT ATTRIBUTES OF EXPRESSIONS ARE RANDOM, IN A GENERALIZED AGGREGATE OPERATOR
identifyExpressionOutputRandomAttrs([InAttrs|OtherInAttrs], [OutAttrs|OtherOutAttrs], ChildRandomAttrs, NodeRandomAttrsIn, NodeRandomAttrsOut) :-
    intersectSets(InAttrs, ChildRandomAttrs, RandomInAttrs),
    (RandomInAttrs = [] -> IntNodeRandomAttrs = NodeRandomAttrsIn ; append(OutAttrs, NodeRandomAttrsIn, IntNodeRandomAttrs)),
    identifyExpressionOutputRandomAttrs(OtherInAttrs, OtherOutAttrs, ChildRandomAttrs, IntNodeRandomAttrs, NodeRandomAttrsOut).

identifyExpressionOutputRandomAttrs([], [], _, NodeRandomAttrsIn, NodeRandomAttrsOut) :-
    NodeRandomAttrsOut = NodeRandomAttrsIn.

%*****************************************************************************************************************************
% RULES FOR ELIMINATES SEED ATTRIBUTES FROM THE PLAN WHEN THEY ARE NO LONGER NEEDED
eliminateSeedAttributesFromPlan([SeedAttr|OtherAttrs], GraphIn, GraphOut, NodesIn, NodesOut, PlanJoins, PlanSelections,
                                       OutputAttrsIn, OutputAttrsOut, RandomAttrsIn, RandomAttrsOut, SeedAttrsOut, OuterRelations) :-
    % **** identify all joins and selections that reference SeedAttr
    findall(SeedJoin,
           (member(join(SeedJoin, JoinPreds, _), PlanJoins), extractAttrsFromPredicates(JoinPreds, [], JoinAttrs), member(SeedAttr, JoinAttrs)),
           SeedJoins),

    findall(SeedSelection,
           (member(selection(SeedSelection, SelectionPreds), PlanSelections), extractAttrsFromPredicates(SelectionPreds, [], SelectionAttrs),
                                 member(SeedAttr, SelectionAttrs)),
            SeedSelections),
    append(SeedJoins, SeedSelections, DuplSeedOperators),
    removeDuplicates(DuplSeedOperators, [], SeedOperators),

    %findall(VGW, member(vgwrapper(VGW, _, _, _, _, _, _, _, SeedAttr), NodesIn), VGWS),
    %(VGWS = [] ->
    %    identifyTopLevelOperators(SeedOperators, SeedOperators, [], TopSeedOperators, GraphIn)
    %    ;
    %    TopSeedOperators = SeedOperators
    %),
    length(SeedOperators, SeedOperatorsLen),
    (SeedOperatorsLen = 1->
        TopSeedOperators = SeedOperators
        ;
        (SeedOperatorsLen = 0 ->
            TopSeedOperators = SeedOperators
            ;
            identifyTopLevelOperators(SeedOperators, SeedOperators, [], TopSeedOperators, GraphIn)
        )
    ),
    addParentSeedProjectionOperators(TopSeedOperators, SeedAttr, GraphIn, IntGraph, NodesIn, IntNodes, OutputAttrsIn, RandomAttrsIn, IntRandomAttrs),

    eliminateSeedAttributesFromPlan(OtherAttrs, IntGraph, GraphOut, IntNodes, NodesOut, PlanJoins, PlanSelections,
                                       OutputAttrsIn, OutputAttrsOut, IntRandomAttrs, RandomAttrsOut, SeedAttrsOut, OuterRelations).

eliminateSeedAttributesFromPlan([], GraphIn, GraphOut, NodesIn, NodesOut, _, _, _, OutputAttrsOut, RandomAttrsIn, RandomAttrsOut, SeedAttrsOut, OuterRelations) :-
    % **** blindly identifies output attributes for each operator assume that all inferences have been performed;
    % **** additionally it changes the projected attributes, generalized attributes, and dedups so that the plan is correct
    updatePlanOutputAttributes(GraphIn, NodesIn, NodesOut, OutputAttrsOut, OuterRelations, RandomAttrsIn, SeedAttrsOut),
    RandomAttrsOut = RandomAttrsIn,
    GraphOut = GraphIn.

%*****************************************************************************************************************************
% RULES THAT IDENTIFY THE HIGHER LEVEL OPERATORS FROM A GIVEN SET OF OPERATORS
identifyTopLevelOperators([SeedOperator|OtherOperators], SeedOperators, TopSeedOperatorsIn, TopSeedOperatorsOut, Graph) :-
   select(SeedOperator, SeedOperators, IntSeedOperators),
   (hasAncestor(SeedOperator, IntSeedOperators, Graph) ->
       IntTopSeedOperators = TopSeedOperatorsIn;
       append([SeedOperator], TopSeedOperatorsIn, IntTopSeedOperators)
   ),
   identifyTopLevelOperators(OtherOperators, SeedOperators, IntTopSeedOperators, TopSeedOperatorsOut, Graph).

identifyTopLevelOperators([], _, TopSeedOperatorsIn, TopSeedOperatorsOut, _) :-
    TopSeedOperatorsOut = TopSeedOperatorsIn.

%*****************************************************************************************************************************
% RULES THAT ADD A PROJECTION OPERATOR TO ELIMINATE REDUNDANT SEED FROM THE PLAN
addParentSeedProjectionOperators([Operator|OtherOperators], SeedAttr, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrs, RandomAttrsIn, RandomAttrsOut) :-
    setof(OperatorParentID, member(parent(OperatorParentID, Operator), GraphIn), OperatorParents),
    member(outputAttributes(Operator, OperatorOutputAttrs), OutputAttrs),

    injectProjectionBetweenOperators(Operator, OperatorParents, OperatorOutputAttrs, SeedAttr, GraphIn, IntGraph, NodesIn, IntNodes, RandomAttrsIn, IntRandomAttrs),

    addParentSeedProjectionOperators(OtherOperators, SeedAttr, IntGraph, GraphOut, IntNodes, NodesOut, OutputAttrs, IntRandomAttrs, RandomAttrsOut).

addParentSeedProjectionOperators([], _, GraphIn, GraphOut, NodesIn, NodesOut, _, RandomAttrsIn, RandomAttrsOut) :-
    GraphOut = GraphIn,
    NodesOut = NodesIn,
    RandomAttrsOut = RandomAttrsIn.

%*****************************************************************************************************************************
% RULES THAT INJECT A PROJECTION OPERATOR BETWEEN TWO OPERATORS
injectProjectionBetweenOperators(Operator, [OperatorParent|OtherParents], OperatorOutputAttrs, SeedAttr, GraphIn, GraphOut, NodesIn, NodesOut, RandomAttrsIn, RandomAttrsOut) :-
    injectProjectionBetweenTwoOperators(Operator, OperatorParent, OperatorOutputAttrs, SeedAttr, GraphIn, IntGraph, NodesIn, IntNodes, RandomAttrsIn, IntRandomAttrs),
    injectProjectionBetweenOperators(Operator, OtherParents, OperatorOutputAttrs, SeedAttr, IntGraph, GraphOut, IntNodes, NodesOut, IntRandomAttrs, RandomAttrsOut).

injectProjectionBetweenOperators(_, [], _, _, GraphIn, GraphOut, NodesIn, NodesOut, RandomAttrsIn, RandomAttrsOut) :-
    GraphOut = GraphIn,
    NodesOut = NodesIn,
    RandomAttrsOut = RandomAttrsIn.

% **** version that deals with a projection parent
injectProjectionBetweenTwoOperators(_, OperatorParent, _, SeedAttr, GraphIn, GraphOut, NodesIn, NodesOut, RandomAttrsIn, RandomAttrsOut) :-
    select(projection(OperatorParent, ProjectionAttrs), NodesIn, IntNodes),
    GraphOut = GraphIn,
    RandomAttrsOut = RandomAttrsIn,
    (select(SeedAttr, ProjectionAttrs, NewProjectionAttrs) ->
        append([projection(OperatorParent, NewProjectionAttrs)], IntNodes, NodesOut)
        ;
        NodesOut = NodesIn
    ).

% **** version that deals with a non-projection parent
injectProjectionBetweenTwoOperators(Operator, OperatorParent, OperatorOutputAttrs, SeedAttr, GraphIn, GraphOut, NodesIn, NodesOut, RandomAttrsIn, RandomAttrsOut) :-
    not(member(projection(OperatorParent, ProjectionAttrs), NodesIn)),

    (select(SeedAttr, OperatorOutputAttrs, ProjectionAttrs) ->
        gensym(projectionNode, NewProjectionNodeID),
        append([projection(NewProjectionNodeID, ProjectionAttrs)], NodesIn, IntNodes1),

        (select(antijoin(OperatorParent, Operator, Preds), IntNodes1, IntNodes2) ->
            append([antijoin(OperatorParent, NewProjectionNodeID, Preds)], IntNodes2, IntNodes3)
            ;
            IntNodes3 = IntNodes1
        ),

        (select(semijoin(OperatorParent, Operator, Preds), IntNodes3, IntNodes4) ->
            append([semijoin(OperatorParent, NewProjectionNodeID, Preds)], IntNodes4, NodesOut)
            ;
            NodesOut = IntNodes3
        ),

        select(parent(OperatorParent, Operator), GraphIn, IntGraph1),
        append([parent(OperatorParent, NewProjectionNodeID)], IntGraph1, IntGraph2),
        append([parent(NewProjectionNodeID, Operator)], IntGraph2, GraphOut),

        member(randomAttributes(Operator, OperatorRandomAttrs), RandomAttrsIn),
        append([randomAttributes(NewProjectionNodeID, OperatorRandomAttrs)], RandomAttrsIn, RandomAttrsOut)

        ;
        NodesOut = NodesIn,
        GraphOut = GraphIn,
        RandomAttrsOut = RandomAttrsIn
    ).

%*****************************************************************************************************************************
% RULE FOR RECURSIVELY IDENTIFYING ALL OUTPUT OPERATORS FOR EACH NODE IN THE ENTIRE PLAN
updatePlanOutputAttributes(GraphIn, NodesIn, NodesOut, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn) :-
    member(parent(planRoot, ChildNode), GraphIn),
    updatePlanOutputAttributes(ChildNode, GraphIn, NodesIn, NodesOut, [], OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, [], SeedAttrsIn).

%*****************************************************************************************************************************
% RULE FOR RECURSIVELY IDENTIFYING ALL ATTRIBUTES THAT COME OUT OF EACH NODE IN THE PLAN ANCOHORED BY A SPECIFIC NODE
%**** version that deals with the case where the node is a table scan
updatePlanOutputAttributes(WhichNode, _, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, _, _, SeedAttrsIn, SeedAttrsOut) :-
    member(tablescan(WhichNode, _, TableOutAttrs), NodesIn),
    NodesOut = NodesIn,

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, SeedAttrsOut = SeedAttrsIn;

        append([outputAttributes(WhichNode, TableOutAttrs)], OutputAttrsIn, OutputAttrsOut),
        append([seedAttributes(WhichNode, [])], SeedAttrsIn, SeedAttrsOut)).

%**** version that deals with the case where the node is a scalar function
updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
    member(scalarfunc(WhichNode, _, _, FuncExprsOutAttrs), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        updatePlanOutputAttributes(ChildNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, IntOutputAttrs, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        append([seedAttributes(WhichNode, ChildSeedAttrs)], IntSeedAttrs, SeedAttrsOut),

        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs),
        flatten(FuncExprsOutAttrs, FlattenedFuncExprsOutAttrs),
        append(ChildOutputAttrs, FlattenedFuncExprsOutAttrs, NodeOutputAttrs),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut)).

%**** version that deals with the case where the node is a generalized aggregate
updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
    member(genagg(WhichNode, _, GroupByAttrs, _, _, AggExprsOutAttrs), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        updatePlanOutputAttributes(ChildNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, IntOutputAttrs, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        append([seedAttributes(WhichNode, ChildSeedAttrs)], IntSeedAttrs, SeedAttrsOut),

        %(AggExprs = nothing ->
        %    IntNodeOutputAttrs1 = GroupByAttrs
         %   ;
        flatten(AggExprsOutAttrs, FlattenedAggExprsOutAttrs),
        append(FlattenedAggExprsOutAttrs, GroupByAttrs, IntNodeOutputAttrs1),  %),

        member(randomAttributes(WhichNode, NodeRandomAttributes), RandomAttrsIn),
        % **** retain isPres and seed attributes
        (member(isPres, NodeRandomAttributes) -> append([isPres], IntNodeOutputAttrs1, IntNodeOutputAttrs2); IntNodeOutputAttrs2 = IntNodeOutputAttrs1),
        append(IntNodeOutputAttrs2, ChildSeedAttrs, NodeOutputAttrs),

        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut)).

%**** version that deals with the case where the node is a vgwrapper
updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
    member(vgwrapper(WhichNode, _, _, _, _, _, _, VGOutAttrs, SeedAttrOut), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn;

        % **** identify VGWrapper's children
        member(outerRelation(WhichNode, OuterRelationNode), OuterRelationsIn),
        setof(InnerRelationNode, member(parent(WhichNode, InnerRelationNode), GraphIn), VGWrapperChildNodes),
        select(OuterRelationNode, VGWrapperChildNodes, InnerRelationNodes),
        updatePlanOutputAttributes(OuterRelationNode, GraphIn, NodesIn, IntNodes1, OutputAttrsIn, IntOutputAttrs1, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs1),
        iterateOverVGWrapperInnerRelationsToUpdateOutputAttributes(InnerRelationNodes, GraphIn, IntNodes1, IntNodes2, IntOutputAttrs1, IntOutputAttrs2, OuterRelationsIn, RandomAttrsIn, IntSeedAttrs1, IntSeedAttrs),

        % **** delete isPres from outer projection if needed
        (member(projection(OuterRelationNode, PrjAttrs), IntNodes2) ->
            (member(isPres, PrjAttrs) ->
                select(isPres, PrjAttrs, NewPrjAttrs),
                select(projection(OuterRelationNode, PrjAttrs), IntNodes2, IntNodes3),
                append([projection(OuterRelationNode, NewPrjAttrs)], IntNodes3, NodesOut)
                ;
                NodesOut = IntNodes2
            )
            ;
            NodesOut = IntNodes2
        ),

        append([seedAttributes(WhichNode, [SeedAttrOut])], IntSeedAttrs, SeedAttrsOut),

        append([SeedAttrOut], VGOutAttrs, NodeOutputAttrs),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs2, OutputAttrsOut)).

%**** version that deals with the case where the node is a selection
updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
    member(selection(WhichNode, _), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        updatePlanOutputAttributes(ChildNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, IntOutputAttrs, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        append([seedAttributes(WhichNode, ChildSeedAttrs)], IntSeedAttrs, SeedAttrsOut),

        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs),
        member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrsIn),
        (member(isPres, NodeRandomAttrs) ->
            mergeSets([isPres], ChildOutputAttrs, NodeOutputAttrs);
            NodeOutputAttrs = ChildOutputAttrs
        ),

        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut)).

%**** version that deals with the case where the node is a split
updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
     member(split(WhichNode, _), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        updatePlanOutputAttributes(ChildNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, IntOutputAttrs, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        append([seedAttributes(WhichNode, ChildSeedAttrs)], IntSeedAttrs, SeedAttrsOut),

        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs),
        (member(isPres, ChildOutputAttrs) ->
            NodeOutputAttrs = ChildOutputAttrs
            ;
            append([isPres], ChildOutputAttrs, NodeOutputAttrs)
        ),

        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut)).

%**** version that deals with the case where the node is a duplicate elimination
updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
    member(dedup(WhichNode, _), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        updatePlanOutputAttributes(ChildNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, IntOutputAttrs, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        append([seedAttributes(WhichNode, ChildSeedAttrs)], IntSeedAttrs, SeedAttrsOut),

        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs),
        append([outputAttributes(WhichNode, ChildOutputAttrs)], IntOutputAttrs, OutputAttrsOut)).

%**** version that deals with the case where the node is a seed
updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
    member(seed(WhichNode, SeedAttr), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        updatePlanOutputAttributes(ChildNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, IntOutputAttrs, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        append([SeedAttr], ChildSeedAttrs, NodeSeedAttrs),
        append([seedAttributes(WhichNode, NodeSeedAttrs)], IntSeedAttrs, SeedAttrsOut),

        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs),
        append([SeedAttr], ChildOutputAttrs, SeedNodeOutputAttrs),
        append([outputAttributes(WhichNode, SeedNodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut)).

%**** version that deals with the case where the node is a join
updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
    member(join(WhichNode, _, _), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn;

        member(parent(WhichNode, JoinLHSChildID), GraphIn),
        member(parent(WhichNode, JoinRHSChildID), GraphIn),
        JoinLHSChildID @> JoinRHSChildID,

        updatePlanOutputAttributes(JoinLHSChildID, GraphIn, NodesIn, IntNodes, OutputAttrsIn, IntOutputAttrs1, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs1),
        updatePlanOutputAttributes(JoinRHSChildID, GraphIn, IntNodes, NodesOut, IntOutputAttrs1, IntOutputAttrs2, OuterRelationsIn, RandomAttrsIn, IntSeedAttrs1, IntSeedAttrs2),

        member(seedAttributes(JoinLHSChildID, JoinLHSChildSeedAttrs), IntSeedAttrs1),
        member(seedAttributes(JoinRHSChildID, JoinRHSChildSeedAttrs), IntSeedAttrs2),
        append(JoinLHSChildSeedAttrs, JoinRHSChildSeedAttrs, JoinSeedAttrs),
        append([seedAttributes(WhichNode, JoinSeedAttrs)], IntSeedAttrs2, SeedAttrsOut),

        member(outputAttributes(JoinLHSChildID, OutputAttrs1), IntOutputAttrs2),
        member(outputAttributes(JoinRHSChildID, OutputAttrs2), IntOutputAttrs2),
        append(OutputAttrs1, OutputAttrs2, IntNodeOutputAttrs),

        member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrsIn),
        (member(isPres, NodeRandomAttrs) ->
            mergeSets([isPres], IntNodeOutputAttrs, NodeOutputAttrs);
            NodeOutputAttrs = IntNodeOutputAttrs
        ),

        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs2, OutputAttrsOut)).

%**** version that deals with the case where the node is an antijoin
updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
    member(antijoin(WhichNode, DataSourceChildID, _), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn;

        select(parent(WhichNode, DataSourceChildID), GraphIn, IntGraph),
        member(parent(WhichNode, OtherChildID), IntGraph),

        updatePlanOutputAttributes(DataSourceChildID, GraphIn, NodesIn, IntNodes, OutputAttrsIn, IntOutputAttrs1, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs1),
        updatePlanOutputAttributes(OtherChildID, GraphIn, IntNodes, NodesOut, IntOutputAttrs1, IntOutputAttrs2, OuterRelationsIn, RandomAttrsIn, IntSeedAttrs1, IntSeedAttrs2),

        member(seedAttributes(DataSourceChildID, DataSourceChildSeedAttrs), IntSeedAttrs1),
        append([seedAttributes(WhichNode, DataSourceChildSeedAttrs)], IntSeedAttrs2, SeedAttrsOut),

        member(outputAttributes(DataSourceChildID, DataSourceChildOutputAttrs), IntOutputAttrs2),
        member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrsIn),
        (member(isPres, NodeRandomAttrs) ->
            mergeSets([isPres], DataSourceChildOutputAttrs, NodeOutputAttrs);
            NodeOutputAttrs = DataSourceChildOutputAttrs
        ),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs2, OutputAttrsOut)).

%**** version that deals with the case where the node is an antijoin
updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
    member(semijoin(WhichNode, DataSourceChildID, _), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn;

        select(parent(WhichNode, DataSourceChildID), GraphIn, IntGraph),
        member(parent(WhichNode, OtherChildID), IntGraph),

        updatePlanOutputAttributes(DataSourceChildID, GraphIn, NodesIn, IntNodes, OutputAttrsIn, IntOutputAttrs1, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs1),
        updatePlanOutputAttributes(OtherChildID, GraphIn, IntNodes, NodesOut, IntOutputAttrs1, IntOutputAttrs2, OuterRelationsIn, RandomAttrsIn, IntSeedAttrs1, IntSeedAttrs2),

        member(seedAttributes(DataSourceChildID, DataSourceChildSeedAttrs), IntSeedAttrs1),
        append([seedAttributes(WhichNode, DataSourceChildSeedAttrs)], IntSeedAttrs2, SeedAttrsOut),

        member(outputAttributes(DataSourceChildID, DataSourceChildOutputAttrs), IntOutputAttrs2),
        member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrsIn),
        (member(isPres, NodeRandomAttrs) ->
            mergeSets([isPres], DataSourceChildOutputAttrs, NodeOutputAttrs);
            NodeOutputAttrs = DataSourceChildOutputAttrs
        ),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs2, OutputAttrsOut)).

%**** version that deals with the case where the node is a projection
updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
    member(projection(WhichNode, ProjectionAttrs), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        updatePlanOutputAttributes(ChildNode, GraphIn, NodesIn, IntNodes1, OutputAttrsIn, IntOutputAttrs, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs),

        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs),
        % **** some of the seed attributes may have been projected out
        intersectSets(ProjectionAttrs, ChildOutputAttrs, NodeOutputAttrs),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        intersectSets(NodeOutputAttrs, ChildSeedAttrs, ProjectedSeedAttrs),
        append([seedAttributes(WhichNode, ProjectedSeedAttrs)], IntSeedAttrs, SeedAttrsOut),

        select(projection(WhichNode, _), IntNodes1, IntNodes2),
        append([projection(WhichNode, NodeOutputAttrs)], IntNodes2, NodesOut)).

%*****************************************************************************************************************************
% RULEs THAT ITERATE OVER A VGWRAPPER's INNER RELATIONS TO UPDATE THE OUTPUT ATTRIBUTES FOR THE PLANS ANCORED AT THEM
iterateOverVGWrapperInnerRelationsToUpdateOutputAttributes([WhichNode|OtherNodes], GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
    updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, IntNodes, OutputAttrsIn, IntOutputAttrs, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs),
    iterateOverVGWrapperInnerRelationsToUpdateOutputAttributes(OtherNodes, GraphIn, IntNodes, NodesOut, IntOutputAttrs, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, IntSeedAttrs, SeedAttrsOut).

iterateOverVGWrapperInnerRelationsToUpdateOutputAttributes([], _, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, _, _, SeedAttrsIn, SeedAttrsOut) :-
    OutputAttrsOut = OutputAttrsIn,
    NodesOut = NodesIn,
    SeedAttrsOut = SeedAttrsIn.

%*********************************************************************************************************************************
% RULE THAT MERGES TWO SETS OF ATTRUBUTES ELIMINATING DUPLICATES
mergeSets(Set1, [Member|OtherSetMembers], NewSet) :-
    mergeSets(Set1, OtherSetMembers, IntSet),
    (member(Member, IntSet) -> NewSet = IntSet; append([Member], IntSet, NewSet)).

mergeSets(Set1, [], NewSet) :- NewSet = Set1.

%*********************************************************************************************************************************
% RULE THAT SUBTRACTS FROM THE FIRST SET THE MEMBERS OF THE SECOND SET
subtractSet([Member|OtherSetMembers], Set2, NewSet) :-
    subtractSet(OtherSetMembers, Set2, IntSet),
    (member(Member, Set2) ->
        NewSet = IntSet
        ;
        append([Member], IntSet, NewSet)
    ).

subtractSet([], _, []).

%*********************************************************************************************************************************
% RULE THAT FINDS THE INTERSECTION OF TWO SETS
intersectSets(Set1, [Member|OtherSetMembers], NewSet) :-
    intersectSets(Set1, OtherSetMembers, IntSet),
    (member(Member, Set1) -> append([Member], IntSet, NewSet); NewSet = IntSet).

intersectSets(_, [], []).

%*****************************************************************************************************************************
% RULES THAT CHECK WHETHER AN OPERATOR HAS AN ANCESTOR OPERATOR IN A GIVEN LIST OF OPERATORS
hasAncestor(SeedOperator, [Operator|OtherOperators], Graph) :-
    (isDescendant(SeedOperator, Operator, Graph) ->
        true;
        hasAncestor(SeedOperator, OtherOperators, Graph)
    ).

%*****************************************************************************************************************************
% RULEs THAT CHECK WHETHER THE FIRST NODE IS DESCENDANT OF THE SECOND IN THE QUERY GRAPH
isDescendant(FirstNode, SecondNode, Graph) :-
    member(parent(ParentNode, FirstNode), Graph),
    (SecondNode = ParentNode -> true;
        isDescendant(ParentNode, SecondNode, Graph)
    ).

isDescendant(Node, Node, _).

%*****************************************************************************************************************************
% RULE THAT CHANGES THE PARENTS OF A NODE TO BECOME PARENTS OF ANOTHER NODE IN THE QUERY GRAPH
swapNodeParents([parent(ParentNodeID, FromWhichNode)|OtherParents], FromWhichNode, ToWhichNode, GraphIn, GraphOut, NodesIn, NodesOut) :-
    select(parent(ParentNodeID, FromWhichNode), GraphIn, IntGraph1),
    append([parent(ParentNodeID, ToWhichNode)], IntGraph1, IntGraph2),
    (select(antijoin(ParentNodeID, FromWhichNode, AntiJoinPred), NodesIn, IntNodes1) ->
        append([antijoin(ParentNodeID, ToWhichNode, AntiJoinPred)], IntNodes1, IntNodes2)
        ;
        IntNodes2 = NodesIn
    ),
    (select(semijoin(ParentNodeID, FromWhichNode, SemiJoinPred), IntNodes2, IntNodes3) ->
        append([semijoin(ParentNodeID, ToWhichNode, SemiJoinPred)], IntNodes3, IntNodes4)
        ;
        IntNodes4 = IntNodes2
    ),
    swapNodeParents(OtherParents, FromWhichNode, ToWhichNode, IntGraph2, GraphOut, IntNodes4, NodesOut).

swapNodeParents([], _, _, GraphIn, GraphOut, NodesIn, NodesOut) :-
    GraphOut = GraphIn,
    NodesOut = NodesIn.

%**** version that deals with the case where the node is a split
identifySplitOutputAttributes(SplitNode, SplitAttrs, ChildNode, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut) :-
    member(randomAttributes(ChildNode, ChildRandomAttrs), RandomAttrsIn),
    member(outputAttributes(ChildNode, ChildOutputAttrs), OutputAttrsIn),
    (member(isPres, ChildOutputAttrs) ->
        NodeOutputAttrs = ChildOutputAttrs,
        IntNodeRandomAttrs = ChildRandomAttrs
        ;
        append([isPres], ChildOutputAttrs, NodeOutputAttrs),
        append([isPres], ChildRandomAttrs, IntNodeRandomAttrs)
    ),

    member(seedAttributes(ChildNode, ChildSeedAttrs), SeedAttrsIn),
    append([seedAttributes(SplitNode, ChildSeedAttrs)], SeedAttrsIn, SeedAttrsOut),

    append([outputAttributes(SplitNode, NodeOutputAttrs)], OutputAttrsIn, OutputAttrsOut),

    subtractSet(IntNodeRandomAttrs, SplitAttrs, NodeRandomAttrs),
    append([randomAttributes(SplitNode, NodeRandomAttrs)], RandomAttrsIn, RandomAttrsOut).
        
%*********************************************************************************************************************************
% RULE THAT REMOVES THE DUPLICATE ELEMENTS FROM THE FIRST MULTISET
removeDuplicates([Member|OtherSetMembers], SetIn, SetOut) :-
    (member(Member, SetIn) ->
        IntSet = SetIn
        ;
        append([Member], SetIn, IntSet)
    ),
    removeDuplicates(OtherSetMembers, IntSet, SetOut).

removeDuplicates([], SetIn, SetOut) :- SetOut = SetIn.

%*********************************************************************************************************************************
% RULEs THAT OUTPUT THE AUGMENTED PLAN IN A GIVEN FILE
outputPlan(run, Graph, Nodes, OutputAttrs, RandomAttrs, OuterRelations, OutFile) :-
%outputPlan(run, Graph, Nodes, OutputAttrs, RandomAttrs, OuterRelations) :-
    telling(Old),
    tell(OutFile),
    %tell('Q_preopt.pl'),

    (current_predicate(vgfunction/4) ->
        setof(vgfunction(VGFuncName, VGFuncPath, VGParams, VGOutput), vgfunction(VGFuncName, VGFuncPath, VGParams, VGOutput), VGFunctions),
        ouputListCanonical(VGFunctions)
        ;
        true
    ), nl,

    (current_predicate(relation/3) ->
        setof(relation(RelName, RelLocation, RelAttrs), relation(RelName, RelLocation, RelAttrs), Relations),
        ouputListCanonical(Relations)
        ;
        true
    ), nl,
    
    (current_predicate(attributeType/2) ->
        setof(attributeType(AttrName, AttrType), attributeType(AttrName, AttrType), AttrTypes),
        ouputListCanonical(AttrTypes)
        ;
        true
    ), nl,
    
    % ***** remove planRoot node
    findall(parent(ParentNode, Node), (member(parent(ParentNode, Node), Graph), ParentNode \= planRoot), NewGraph),
    ouputListCanonical(NewGraph), nl,

    ouputListCanonical(Nodes), nl,
    
    ouputListCanonical(OutputAttrs), nl,
    
    ouputListCanonical(RandomAttrs), nl,
    
    ouputListCanonical(OuterRelations), nl,
    
    (current_predicate(compExp/5) ->
        findall(compExp(PredName, CompExpType, LHSAttr, RHSAttr, RHSAttrType), compExp(PredName, CompExpType, LHSAttr, RHSAttr, RHSAttrType), AtomicPredicates)
        ;
        AtomicPredicates = []
    ),
    (current_predicate(boolOr/2) -> setof(boolOr(PredName, OrPreds), boolOr(PredName, OrPreds), OrPredicates); OrPredicates = []),
    flatten([AtomicPredicates, OrPredicates], Preds),
    ouputListCanonical(Preds),
    write('\n'),
     
    (current_predicate(aggExp/4) ->
        setof(aggExp(ExpName, ExpFunc, ExpAttr, ExpType), aggExp(ExpName, ExpFunc, ExpAttr, ExpType), AggregateExpressions)
        ;
        AggregateExpressions = []
    ),
    ouputListCanonical(AggregateExpressions),
    write('\n'),
     
    (current_predicate(arithExp/6) ->
        setof(arithExp(ExpName, ExpType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType), arithExp(ExpName, ExpType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType), ArithmeticExpressions)
        ;
        ArithmeticExpressions = []
    ),
    ouputListCanonical(ArithmeticExpressions),
    write('\n'),
     
    (current_predicate(function/3) ->
        setof(function(FuncName, FuncType, FuncAttrs), function(FuncName, FuncType, FuncAttrs), Functions)
        ;
        Functions = []
    ),
    ouputListCanonical(Functions),
    write('\n'),
    
    (current_predicate(verbatim/3) ->
        setof(verbatim(VerbatimName, VerbatimExp, ExpType), verbatim(VerbatimName, VerbatimExp, ExpType), Verbatims)
        ;
        Verbatims = []
    ),
    ouputListCanonical(Verbatims),
    write('\n'),

    (current_predicate(temporaryTable/3) ->
        setof(temporaryTable(TableName, TableAttrs, TableRows), temporaryTable(TableName, TableAttrs, TableRows), TemporaryTables)
        ;
        TemporaryTables = []
    ),
    ouputListCanonical(TemporaryTables),
    write('\n'),
    
    monteCarloIterations(MCI),
    write(monteCarloIterations(MCI)),
    write('.\n\n'),
    
    vgThreads(VGTHREADSNO),
    write(vgThreads(VGTHREADSNO)),
    write('.\n'),
    
    vgTuples(VGTUPLESNO),
    write(vgTuples(VGTUPLESNO)),
    write('.\n'),
    
    vgPipeSize(VGPIPESIZE),
    write(vgPipeSize(VGPIPESIZE)),
    write('.\n'),
    
    told,
    tell(Old).
    
outputPlan(optimize, Graph, Nodes, OutputAttrs, RandomAttrs, OuterRelations, OutFile) :-
%outputPlan(optimize, Graph, Nodes, OutputAttrs, RandomAttrs, OuterRelations) :-
    telling(Old),
    tell(OutFile),
    %tell('Q_preopt.pl'),

    write(':- dynamic compExp/5.'), nl,
    
    (current_predicate(vgfunction/4) ->
        setof(vgfunction(VGFuncName, VGFuncPath, VGParams, VGOutput), vgfunction(VGFuncName, VGFuncPath, VGParams, VGOutput), VGFunctions),
        ouputListCanonical(VGFunctions)
        ;
        true
    ), nl,

    (current_predicate(relation/3) ->
        setof(relation(RelName, RelLocation, RelAttrs), relation(RelName, RelLocation, RelAttrs), Relations),
        ouputListCanonical(Relations)
        ;
        true
    ), nl,

    (current_predicate(attributeType/2) ->
        setof(attributeType(AttrName, AttrType), attributeType(AttrName, AttrType), AttrTypes),
        ouputListCanonical(AttrTypes)
        ;
        true
    ), nl,

    ouputListCanonical(Graph), nl,

    ouputListCanonical(Nodes), nl,

    ouputListCanonical(OutputAttrs), nl,

    ouputListCanonical(RandomAttrs), nl,

    ouputListCanonical(OuterRelations), nl,

    (current_predicate(compExp/5) ->
        findall(compExp(PredName, CompExpType, LHSAttr, RHSAttr, RHSAttrType), compExp(PredName, CompExpType, LHSAttr, RHSAttr, RHSAttrType), AtomicPredicates)
        ;
        AtomicPredicates = []
    ),
    (current_predicate(boolOr/2) -> setof(boolOr(PredName, OrPreds), boolOr(PredName, OrPreds), OrPredicates); OrPredicates = []),
    flatten([AtomicPredicates, OrPredicates], Preds),
    ouputListCanonical(Preds),
    write('\n'),

    (current_predicate(aggExp/4) ->
        setof(aggExp(ExpName, ExpFunc, ExpAttr, ExpType), aggExp(ExpName, ExpFunc, ExpAttr, ExpType), AggregateExpressions)
        ;
        AggregateExpressions = []
    ),
    ouputListCanonical(AggregateExpressions),
    write('\n'),

    (current_predicate(arithExp/6) ->
        setof(arithExp(ExpName, ExpType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType), arithExp(ExpName, ExpType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType), ArithmeticExpressions)
        ;
        ArithmeticExpressions = []
    ),
    ouputListCanonical(ArithmeticExpressions),
    write('\n'),

    (current_predicate(function/3) ->
        setof(function(FuncName, FuncType, FuncAttrs), function(FuncName, FuncType, FuncAttrs), Functions)
        ;
        Functions = []
    ),
    ouputListCanonical(Functions),
    write('\n'),
    
    (current_predicate(verbatim/3) ->
        setof(verbatim(VerbatimName, VerbatimExp, ExpType), verbatim(VerbatimName, VerbatimExp, ExpType), Verbatims)
        ;
        Verbatims = []
    ),
    ouputListCanonical(Verbatims),
    write('\n'),
    
    setof(stats(WhichNode, UniqueAttrValues, UniqueAttrValuesPerTupleBundle, AttrSizes, NumTuples, RecordSize), stats(WhichNode, UniqueAttrValues, UniqueAttrValuesPerTupleBundle, AttrSizes, NumTuples, RecordSize), OperatorStatistics),
    ouputListCanonical(OperatorStatistics),
    write('\n'),
    
    (current_predicate(temporaryTable/3) ->
        setof(temporaryTable(TableName, TableAttrs, TableRows), temporaryTable(TableName, TableAttrs, TableRows), TemporaryTables)
        ;
        TemporaryTables = []
    ),
    ouputListCanonical(TemporaryTables),
    write('\n'),
    
    (current_predicate(vgwrapperproperties/4) ->
        setof(vgwrapperproperties(VGName, BPT, VGWrapperAttrProperties, VGWrapperAttrSizes),
                      vgwrapperproperties(VGName, BPT, VGWrapperAttrProperties, VGWrapperAttrSizes), VGWrprProps)
        ;
        VGWrprProps = []
    ),
    ouputListCanonical(VGWrprProps),
    write('\n'),
    
    setof(operatorCostWeight(OperatorName, OperatorWeight), operatorCostWeight(OperatorName, OperatorWeight), OperatorWeights),
    ouputListCanonical(OperatorWeights),
    write('\n'),

    defaultSelectivity(DS),
    write(defaultSelectivity(DS)),
    write('.\n\n'),

    attributeSize(isPres, ISPRESSIZE),
    write(attributeSize(isPres, ISPRESSIZE)),
    write('.\n\n'),

    attributeSize(seed, SEEDSIZE),
    write(attributeSize(seed, SEEDSIZE)),
    write('.\n\n'),

    monteCarloIterations(MCI),
    write(monteCarloIterations(MCI)),
    write('.\n\n'),

    vgThreads(VGTHREADSNO),
    write(vgThreads(VGTHREADSNO)),
    write('.\n'),

    vgTuples(VGTUPLESNO),
    write(vgTuples(VGTUPLESNO)),
    write('.\n'),

    vgPipeSize(VGPIPESIZE),
    write(vgPipeSize(VGPIPESIZE)),
    write('.\n'),

    told,
    tell(Old).

ouputList([Member|OtherMembers]) :-
    write(Member),
    write('.\n'),
    ouputList(OtherMembers).

ouputList([]).

% preserve string quotes if needed
ouputListCanonical([Node|OtherNodes]) :-
    writeq(Node),
    write('.\n'),
    ouputListCanonical(OtherNodes).

ouputListCanonical([]).
