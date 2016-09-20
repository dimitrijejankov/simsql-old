

% /*****************************************************************************
%  *                                                                           *
%  *  Copyright 2014 Rice University                                           *
%  *                                                                           *
%  *  Licensed under the Apache License, Version 2.0 (the "License");          *
%  *  you may not use this file except in compliance with the License.         *
%  *  You may obtain a copy of the License at                                  *
%  *                                                                           *
%  *      http://www.apache.org/licenses/LICENSE-2.0                           *
%  *                                                                           *
%  *  Unless required by applicable law or agreed to in writing, software      *
%  *  distributed under the License is distributed on an "AS IS" BASIS,        *
%  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
%  *  See the License for the specific language governing permissions and      *
%  *  limitations under the License.                                           *
%  *                                                                           *
%  *****************************************************************************/


%*****************************************************************************************************************************
% RULE FOR RUNNING THE OPTIMIZER
% The optimizer works by taking an input plan (stored in the predicate database) and repeatedly applying the transformations
% allowed by the transform\15 predicate. At each step, the optimizer maintains a current set of good plans. It performs all
% possible transformations on every plan in that set, and retains the best few plans that are optained. The number of
% transformations used to compute the query plan must be at most MaxIterationsToAllow, and at every step of the optimization,
% the best MaxPlanNoPerIteration alternative plans are retained. Thus, if MaxPlanNoPerIteration = 1, then the optimzer
% uses a purely greedy search.

optimize(Action, MaxIterationsToAllow, MaxPlanNoPerIteration, NumberOfGeneratedPlans, TotalTime, AppliedTransformations, NumberOfPlansPerIteration, OutFile)  :-
    % **** the first few lines simple make lists out of all of the parts of the original query plan; the reason for the
    % **** "current_predicate(parent/2)" and the like is that before we make a list out of all of the original plan entries,
    % **** we need to make sure that there is actually a relation (otherwise, setof will abort)
        
    % **** plan graph structure
    (current_predicate(parent/2) -> setof(parent(Parent, Child), parent(Parent, Child), IntGraph01); IntGraph01 = []),
     
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
    (current_predicate(frameoutput/3) -> setof(frameoutput(NodeName, FrameOutputChildren, FrameOutputFiles),
                    frameoutput(NodeName, FrameOutputChildren, FrameOutputFiles), FrameOutputs); FrameOutputs = []),
    
    flatten([Selections, TableScans, Seeds, Joins, AntiJoins, SemiJoins, Splits, Dedups, Projections, Aggregations, VGWrappers, ScalarFuncs, FrameOutputs], IntNodes01),
     
    % **** outer relation information for the VGWrappers in the plan
    (current_predicate(outerRelation/2) -> setof(outerRelation(VGWrapperNodeName, OuterRelationName),
                    outerRelation(VGWrapperNodeName, OuterRelationName), IntOuterRelations); IntOuterRelations = []),

    %% added by Luis: break up all selections.
    findall(S, (member(selection(S, [_|X]), IntNodes01), X = [_|_], findall(K, member(parent(S, K), IntGraph01), [_])), BreakSels),
    breakUpSelectionNodes(BreakSels, IntGraph01, IntGraph1, IntNodes01, IntNodes1),
                    
    % **** identifies the output and random attributes for each query node; as a byproduct
    % **** it (1) preserves 'seed' and 'isPres' attributes along projection nodes (2) infers
    % **** attributes for dedup nodes (3) adds 'isPres' attributes for selection, join,
    % **** semi-join and anti-join and (4) adds split nodes if needed
    identifyOutputAttributes(IntGraph1, IntGraph2, IntNodes1, IntNodes2, IntOutputAttrs1, IntRandomAttrs1, IntOuterRelations),
    
    % **** select all seed attributes in the plan
    findall(OuterSeedAttr, member(seed(_, OuterSeedAttr), IntNodes2), OuterSeedAttrs),
    findall(VGWrapperSeedAttr, member(vgwrapper(_, _, _, _, _, _, _, _, VGWrapperSeedAttr), IntNodes2), VGWrapperSeedAttrs),
    append(OuterSeedAttrs, VGWrapperSeedAttrs, PlanSeedAttrs),
    eliminateSeedAttributesFromPlan(PlanSeedAttrs, IntGraph2, IntGraph3, IntNodes2, IntNodes3, Joins, Selections,
                          IntOutputAttrs1, IntOutputAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntSeedAttrs, IntOuterRelations),
    
    % **** infer output, random and seed attributes for each node in the plan, by checking the attribute requirements
    % **** of the node's parents; as a by-product removes any unecessary projection nodes
    updateOutputAttributes(IntGraph3, GraphIn, IntNodes3, IntNodes4, IntOutputAttrs2, OutputAttrsIn, IntRandomAttrs2,
                                                 RandomAttrsIn, IntSeedAttrs, SeedAttrsIn, IntOuterRelations, OuterRelationsIn),
    
    % **** identify candidate keys for each query node; as a by-product it also labels the existing joins to 1x1, 1xN, Nx1, NxM
    (current_predicate(candidateKey/2) -> setof(candidateKey(NodeName, Keys), candidateKey(NodeName, Keys), IntKeys); IntKeys = []),
    identifyPlanCandidateKeys(GraphIn, IntNodes4, NodesIn, IntKeys, KeysIn, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),

    % **** plan statistics for tablescan operators
    costPlan(GraphIn, NodesIn, OuterRelationsIn, RandomAttrsIn, OutputAttrsIn, PlanCost, _),
    
    % **** generate hash keys of plans to avoid storing duplicate plans
    sort(GraphIn, SGraphIn), hash_term(SGraphIn, GraphHashKey),
    sort(NodesIn, SNodesIn), hash_term(SNodesIn, NodesHashKey),
    assert(hashedPlan(GraphHashKey, NodesHashKey)),
    assert(appliedTransformation(default(1))),
    doSeriesOfTransforms([PlanCost-plan(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn)],
                                                  [], AllPlans, MaxIterationsToAllow, MaxPlanNoPerIteration, [], NumberOfPlansPerIteration),
    % **** output best plan
    keysort(AllPlans, SortedAllPlans),
    SortedAllPlans = [_-plan(IntGraphOut, IntNodesOut, IntOutputAttrsOut, _, IntOuterRelationsOut, IntRandomAttrsOut, _)|_],

    GraphOut = IntGraphOut,
    NodesOut = IntNodesOut,
    OutputAttrsOut = IntOutputAttrsOut,
    OuterRelationsOut = IntOuterRelationsOut,
    RandomAttrsOut = IntRandomAttrsOut,
    
    (Action = optimize ->
        estimateplancosts(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, EstimatedCosts),
        outputPlan(GraphOut, NodesOut, OutputAttrsOut, RandomAttrsOut, OuterRelationsOut, EstimatedCosts, OutFile),
        (member(frameoutput(_, FrameOutputNodeChildren, FrameOutputNodeFiles), NodesOut) ->
            outputPlanInfo(FrameOutputNodeFiles, FrameOutputNodeChildren, OutputAttrsOut, RandomAttrsOut)
            ;
            true
        )
        ;
        costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, DPlanCost, PlanStats),

        (Action = computeCost ->
            outputCostInfo(DPlanCost, OutFile)
        ;
            outputCutInfo(GraphOut, NodesOut, RandomAttrsOut, PlanStats))
    ),
    % ****
    
    statistics(real_time, TotalTime),
    length(AllPlans, NumberOfGeneratedPlans),

    setof(AppliedTransformation, appliedTransformation(AppliedTransformation), AppliedTransformations).
    
%*****************************************************************************************************************************
% RULE FOR RUNNING THE OPTIMIZER
% True if, starting with the set StartPlans, you do up to a series of SeriesLen steps where at each step you apply all possible
% transformations to every plan in StartPlans
doSeriesOfTransforms(StartPlans, VisitedPlans, AllPlans, SeriesLen, PlansToKeepPerIteration, NumberOfPlansPerIterationIn, NumberOfPlansPerIterationOut) :-
        SeriesLen > 0,
        SeriesLenMinusOne is SeriesLen - 1,
        
        transformEveryQueryPlanWeHave(StartPlans, NewPlansYouGetInOneStep),
        length(NewPlansYouGetInOneStep, NumberOfGeneratedPlans),
        append(NumberOfPlansPerIterationIn, [NumberOfGeneratedPlans], IntNumberOfPlansPerIteration),
        length(NewPlansYouGetInOneStep, NewPlansNo),
        (NewPlansNo > 0 ->
            mergeSets(StartPlans, VisitedPlans, IntVisitedPlans),
            mergeSets(IntVisitedPlans, NewPlansYouGetInOneStep, TotalCurrentPlans),
            keysort(TotalCurrentPlans, SortedTotalCurrentPlans),
            getCheapestPlans(SortedTotalCurrentPlans, [], PreservedPlans, PlansToKeepPerIteration),
            intersectSets(IntVisitedPlans, PreservedPlans, NewVisitedPlans),
            intersectSets(NewPlansYouGetInOneStep, PreservedPlans, NewPlans),
            doSeriesOfTransforms(NewPlans, NewVisitedPlans, AllPlans, SeriesLenMinusOne, PlansToKeepPerIteration, IntNumberOfPlansPerIteration, NumberOfPlansPerIterationOut)
            ;
            append(StartPlans, VisitedPlans, IntVisitedPlans),
            doSeriesOfTransforms(NewPlansYouGetInOneStep, IntVisitedPlans, AllPlans, 0, _, IntNumberOfPlansPerIteration, NumberOfPlansPerIterationOut)
        ).
                
doSeriesOfTransforms(StartPlans, VisitedPlans, AllPlans, 0, _, NumberOfPlansPerIterationIn, NumberOfPlansPerIterationOut) :-
    append(StartPlans, VisitedPlans, AllPlans),
    NumberOfPlansPerIterationOut = NumberOfPlansPerIterationIn.

%*********************************************************************************************************************************
% RULE FOR COMPUTING ALL THE PLANS THAT CAN BE OBTAINED FROM RUNNING ALL POSSIBLE TRANSFORMS ON THE CURRENT SET OF PLANS
% returns true if you start with the set StartPlans, run all possible transforms and return the set the new plans that
% are generated along with the set of StartPlans

transformEveryQueryPlanWeHave(StartPlans, NextPlans) :-
        findall(OneSublist, getAllTransformsForOneQueryPlan(StartPlans, OneSublist), PartitionedList),
        %dePartition(PartitionedList, NextPlans).
        flatten(PartitionedList, NextPlans).

%*********************************************************************************************************************************
% RULE FOR RUNNING ALL POSSIBLE TRANSFORMS ON A SET OF QUERY PLANS
% true if, when you run all possible transformations on every query plan in the set BestPlans, you get the set of NextPlans
% and the plan where the formers where generated from

getAllTransformsForOneQueryPlan(StartPlans, NextPlans) :-
        % choose a plan
        member(_-plan(Graph, Nodes, OutputAttrs, Keys, OuterRelations, RandomAttrs, SeedAttrs), StartPlans),
        
        % and collect all possible plans that can be generated by transforming the chosen plan
        findall(PlanCostOut-plan(GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut), ( 
                   transform(Graph, Nodes, OutputAttrs, Keys, OuterRelations, RandomAttrs, SeedAttrs, GraphOut, NodesOut,
                                                     OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCostOut)), NextPlans).
        
%*****************************************************************************************************************************
% RULE FOR RECURSIVELY IDENTIFYING ALL ATTRIBUTES THAT COME OUT OF EACH NODE IN THE ENTIRE PLAN
identifyOutputAttributes(GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsOut, RandomAttrsOut, OuterRelations) :-
    member(parent(planRoot, ChildNode), GraphIn),
    identifyOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut, [], OutputAttrsOut, [], _, [], RandomAttrsOut, OuterRelations).

%*****************************************************************************************************************************
% RULE FOR RECURSIVELY IDENTIFYING ALL ATTRIBUTES THAT COME OUT OF EACH NODE IN THE PLAN ANCOHORED BY A SPECIFIC NODE
%**** version that deals with the case where the node is a top frame operator
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(frameoutput(WhichNode, FrameOutputChildren, _), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, GraphOut = GraphIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

        iterateOverFrameOutputChildrenToCollectOutputAttributes(FrameOutputChildren, GraphIn, GraphOut, NodesIn, NodesOut,
                                  OutputAttrsIn, IntOutputAttrs, [], FrameOutputOutputAttrs, RandomAttrsIn, IntRandomAttrs,
                                  [], FrameOutputRandomAttrs, OuterRelations, SeedAttrsIn, IntSeedAttrs, [], FrameOutputSeedAttrs),

        append([outputAttributes(WhichNode, FrameOutputOutputAttrs)], IntOutputAttrs, OutputAttrsOut),
        append([randomAttributes(WhichNode, FrameOutputRandomAttrs)], IntRandomAttrs, RandomAttrsOut),
        append([seedAttributes(WhichNode, FrameOutputSeedAttrs)], IntSeedAttrs, SeedAttrsOut)
    ), !.
    
%**** version that deals with the case where the node is a table scan
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, _) :-
    member(tablescan(WhichNode, _, TableOutAttrs), NodesIn),
    NodesOut = NodesIn,
    GraphOut = GraphIn,
    
    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn,
        SeedAttrsOut = SeedAttrsIn,
        RandomAttrsOut = RandomAttrsIn;

        (current_predicate(randomAttrsRelation/2) ->
%%% changed by Luis to avoid those bad splits.
%%            (randomAttrsRelation(WhichNode, RelationRandomAttrs) ->
            (randomAttrsRelation(WhichNode, _) ->

%%% changed by Luis to avoid those bad splits.
%%%                append([randomAttributes(WhichNode, RelationRandomAttrs)], RandomAttrsIn, RandomAttrsOut)
                append([randomAttributes(WhichNode, [])], RandomAttrsIn, RandomAttrsOut)
                ;
                append([randomAttributes(WhichNode, [])], RandomAttrsIn, RandomAttrsOut)
            )
            ;
            append([randomAttributes(WhichNode, [])], RandomAttrsIn, RandomAttrsOut)
        ),
        append([outputAttributes(WhichNode, TableOutAttrs)], OutputAttrsIn, OutputAttrsOut),
        append([seedAttributes(WhichNode, [])], SeedAttrsIn, SeedAttrsOut)), !.
                
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
        append([randomAttributes(WhichNode, JoinRandomAttrs)], IntRandomAttrs4, RandomAttrsOut)), !.
        
%**** version that deals with the case where the node is a split
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(split(WhichNode, SplitAttrs), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, GraphOut = GraphIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, IntOutputAttrs, SeedAttrsIn, IntSeedAttrs, RandomAttrsIn, IntRandomAttrs, OuterRelations),

        member(randomAttributes(ChildNode, ChildRandomAttrs), IntRandomAttrs),
        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs),
        (member(isPres, ChildOutputAttrs) ->
            NodeOutputAttrs = ChildOutputAttrs,
            IntNodeRandomAttrs = ChildRandomAttrs
            ;
            append([isPres], ChildOutputAttrs, NodeOutputAttrs),
            append([isPres], ChildRandomAttrs, IntNodeRandomAttrs)
        ),

        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        append([seedAttributes(WhichNode, ChildSeedAttrs)], IntSeedAttrs, SeedAttrsOut),

        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut),

        subtractSet(IntNodeRandomAttrs, SplitAttrs, NodeRandomAttrs),
        append([randomAttributes(WhichNode, NodeRandomAttrs)], IntRandomAttrs, RandomAttrsOut)),!.
        
%**** version that deals with the case where the node is a scalar function
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(scalarfunc(WhichNode, _, FuncExprsInAttrs, FuncExprsOutAttrs), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, GraphOut = GraphIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

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
        append([randomAttributes(WhichNode, NodeRandomAttrs)], IntRandomAttrs, RandomAttrsOut)),!.
        
%**** version that deals with the case where the node is a generalized aggregate
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(genagg(WhichNode, _, GroupByAttrs, _, AggExprsInAttrs, AggExprsOutAttrs), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, GraphOut = GraphIn, NodesOut = NodesIn, SeedAttrsIn = SeedAttrsOut, RandomAttrsOut = RandomAttrsIn;

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
        append([randomAttributes(WhichNode, NodeRandomAttrs)], IntRandomAttrs2, RandomAttrsOut)), !.

%**** version that deals with the case where the node is a vgwrapper
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(vgwrapper(WhichNode, _, _, _, _, _, _, VGOutAttrs, SeedAttrOut), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, GraphOut = GraphIn, NodesOut = NodesIn, SeedAttrsIn = SeedAttrsOut, RandomAttrsOut = RandomAttrsIn;

        % **** identify VGWrapper's children
        member(outerRelation(WhichNode, OuterRelationNode), OuterRelations),
        setof(InnerRelationNode, member(parent(WhichNode, InnerRelationNode), GraphIn), VGWrapperChildNodes),
        select(OuterRelationNode, VGWrapperChildNodes, InnerRelationNodes),
        identifyOutputAttributes(OuterRelationNode, GraphIn, IntGraph, NodesIn, IntNodes1, OutputAttrsIn, IntOutputAttrs1, SeedAttrsIn, IntSeedAttrs1, RandomAttrsIn, IntRandomAttrs1, OuterRelations),
        iterateOverVGWrapperInnerRelationsToCollectOutputAttributes(InnerRelationNodes, IntGraph, GraphOut, IntNodes1, NodesOut, IntOutputAttrs1, IntOutputAttrs2, IntRandomAttrs1, IntRandomAttrs2, OuterRelations, IntSeedAttrs1, IntSeedAttrs),

        append([seedAttributes(WhichNode, [SeedAttrOut])], IntSeedAttrs, SeedAttrsOut),
        
        append([SeedAttrOut], VGOutAttrs, NodeOutputAttrs),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs2, OutputAttrsOut),

        (current_predicate(randomAttrsRelation/2) ->
%%% changed by Luis
%%%            (randomAttrsRelation(WhichNode, RelationRandomAttrs) ->
            (randomAttrsRelation(WhichNode, _) ->
%%%                append([randomAttributes(WhichNode, RelationRandomAttrs)], IntRandomAttrs2, RandomAttrsOut)
                append([randomAttributes(WhichNode, [])], IntRandomAttrs2, RandomAttrsOut)
                ;
                append([randomAttributes(WhichNode, VGOutAttrs)], IntRandomAttrs2, RandomAttrsOut)
            )
            ;
            append([randomAttributes(WhichNode, VGOutAttrs)], IntRandomAttrs2, RandomAttrsOut)
        )),!.
         
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
        append([randomAttributes(WhichNode, NodeRandomAttrs)], IntRandomAttrs, RandomAttrsOut)), !.

%**** version that deals with the case where the node is a projection
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(projection(WhichNode, ProjectionAttrs), NodesIn),
    
    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, GraphOut = GraphIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

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
        append([randomAttributes(WhichNode, NodeRandomAttrs)], IntRandomAttrs, RandomAttrsOut)), !.
        
%**** version that deals with the case where the node is a seed
identifyOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelations) :-
    member(seed(WhichNode, SeedAttr), NodesIn),
    
    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, GraphOut = GraphIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, RandomAttrsOut = RandomAttrsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, IntOutputAttrs, SeedAttrsIn, IntSeedAttrs, RandomAttrsIn, IntRandomAttrs, OuterRelations),
        
        member(seedAttributes(ChildNode, ChildSeedAttrs), IntSeedAttrs),
        append([SeedAttr], ChildSeedAttrs, SeedNodeSeedAttrs),
        append([seedAttributes(WhichNode, SeedNodeSeedAttrs)], IntSeedAttrs, SeedAttrsOut),
        
        member(outputAttributes(ChildNode, ChildOutputAttrs), IntOutputAttrs),
        append([SeedAttr], ChildOutputAttrs, NodeOutputAttrs),
        append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut),

         member(randomAttributes(ChildNode, ChildRandomAttrs), IntRandomAttrs),
         append([randomAttributes(WhichNode, ChildRandomAttrs)], IntRandomAttrs, RandomAttrsOut)), !.
        
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
        append([randomAttributes(WhichNode, NewChildRandomAttrs)], IntRandomAttrs2, RandomAttrsOut)), !.

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
        append([randomAttributes(WhichNode, JoinRandomAttrs)], IntRandomAttrs4, RandomAttrsOut)), !.

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
        append([randomAttributes(WhichNode, JoinRandomAttrs)], IntRandomAttrs4, RandomAttrsOut)), !.

%*****************************************************************************************************************************
% RULE FOR IDENTIFING THE OUTPUT, RANDOM AND SEED ATTRIBUTES FOR EACH NODE BASED ON THE ATTRIBUTE REQUIREMENTS OF THE NODE'S PARENT
updateOutputAttributes(GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, RandomAttrsIn, RandomAttrsOut, SeedAttrsIn, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut) :-
    member(parent(planRoot, ChildNode), GraphIn),
    updateOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut,
                 OutputAttrsIn, _, [], OutputAttrsOut, RandomAttrsIn, _, [], RandomAttrsOut,
                 SeedAttrsIn, _, [], SeedAttrsOut, OuterRelationsIn, OuterRelationsOut).
        
%*****************************************************************************************************************************
% RULE FOR IDENTIFING THE OUTPUT, RANDOM AND SEED ATTRIBUTES FOR EACH NODE BASED ON THE ATTRIBUTE REQUIREMENTS OF THE NODE'S
% PARENT FOR THE PLAN ANCORED FROM A SPECIFIC NODE
% **** version that deals with a frameoutput node; need to eliminate it
updateOutputAttributes(WhichNode, GraphIn, GraphOut, NodesIn, NodesOut, PrevOutputAttrsIn, PrevOutputAttrsOut, OutputAttrsIn, OutputAttrsOut, PrevRandomAttrsIn,
                   PrevRandomAttrsOut, RandomAttrsIn, RandomAttrsOut, PrevSeedAttrsIn, PrevSeedAttrsOut, SeedAttrsIn, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut) :-
    member(frameoutput(WhichNode, FrameOutputChildren, _), NodesIn),
    
    select(outputAttributes(WhichNode, NodeOutputAttrs), PrevOutputAttrsIn, IntPrevOutputAttrs),
    append([outputAttributes(WhichNode, NodeOutputAttrs)], OutputAttrsIn, IntOutputAttrs),
    select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, IntPrevRandomAttrs),
    append([randomAttributes(WhichNode, NodeRandomAttrs)], RandomAttrsIn, IntRandomAttrs),
    select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, IntPrevSeedAttrs),
    append([seedAttributes(WhichNode, NodeSeedAttrs)], SeedAttrsIn, IntSeedAttrs),

    iterateOverFrameOutputChildrenToUpdateOutputAttributes(FrameOutputChildren, GraphIn, GraphOut, NodesIn, NodesOut, IntPrevOutputAttrs, PrevOutputAttrsOut, IntOutputAttrs, OutputAttrsOut, IntPrevRandomAttrs, PrevRandomAttrsOut, IntRandomAttrs, RandomAttrsOut, IntPrevSeedAttrs, PrevSeedAttrsOut, IntSeedAttrs, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut), !.
                      
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
    
    (((ProjectionNodeParents = [parent(planRoot, WhichNode)]); (parent(FrameOutputNode, WhichNode), member(frameoutput(FrameOutputNode, _, _), NodesIn));
                    (parent(VGWrapperNode, WhichNode), member(vgwrapper(VGWrapperNode, _, _, _, _, _, _, _, _), NodesIn))) ->  % **** preserve projection
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
    ), !.
    
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
                intersectSets(RequiredAttrs, NodeOutputAttrs, TmpNewNodeOutputAttrs),
                (TmpNewNodeOutputAttrs = [] ->
                    NewNodeOutputAttrs =  NodeOutputAttrs
                    ;
                    NewNodeOutputAttrs = TmpNewNodeOutputAttrs
                ),
                append([outputAttributes(WhichNode, NewNodeOutputAttrs)], OutputAttrsIn, OutputAttrsOut),

                select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, PrevRandomAttrsOut),
                intersectSets(RequiredAttrs, NodeRandomAttrs, TmpNewNodeRandomAttrs),
                (TmpNewNodeRandomAttrs = [] ->
                    NewNodeRandomAttrs =  NodeRandomAttrs
                    ;
                    NewNodeRandomAttrs = TmpNewNodeRandomAttrs
                ),
                append([randomAttributes(WhichNode, NewNodeRandomAttrs)], RandomAttrsIn, RandomAttrsOut),

                select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, PrevSeedAttrsOut),
                intersectSets(RequiredAttrs, NodeSeedAttrs, TmpNewNodeSeedAttrs),
                (TmpNewNodeSeedAttrs = [] ->
                    NewNodeSeedAttrs =  NodeSeedAttrs
                    ;
                    NewNodeSeedAttrs = TmpNewNodeSeedAttrs
                ),
                append([seedAttributes(WhichNode, NewNodeSeedAttrs)], SeedAttrsIn, SeedAttrsOut)
                ;
                GraphOut = GraphIn, OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, PrevSeedAttrsOut = PrevSeedAttrsIn,
                RandomAttrsOut = RandomAttrsIn, OuterRelationsOut = OuterRelationsIn, PrevOutputAttrsOut = PrevOutputAttrsIn, PrevRandomAttrsOut = PrevRandomAttrsIn
            )
        )
    ),!.

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
                intersectSets(RequiredAttrs, NodeOutputAttrs, TmpNewNodeOutputAttrs),
                (TmpNewNodeOutputAttrs = [] ->
                    NewNodeOutputAttrs =  NodeOutputAttrs
                    ;
                    NewNodeOutputAttrs = TmpNewNodeOutputAttrs
                ),
                append([outputAttributes(WhichNode, NewNodeOutputAttrs)], OutputAttrsIn, IntOutputAttrs),

                select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, IntPrevRandomAttrs),
                intersectSets(RequiredAttrs, NodeRandomAttrs, TmpNewNodeRandomAttrs),
                (TmpNewNodeRandomAttrs = [] ->
                    NewNodeRandomAttrs =  NodeRandomAttrs
                    ;
                    NewNodeRandomAttrs = TmpNewNodeRandomAttrs
                ),
                append([randomAttributes(WhichNode, NewNodeRandomAttrs)], RandomAttrsIn, IntRandomAttrs),

                select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, IntPrevSeedAttrs),
                intersectSets(RequiredAttrs, NodeSeedAttrs, TmpNewNodeSeedAttrs),
                (TmpNewNodeSeedAttrs = [] ->
                    NewNodeSeedAttrs =  NodeSeedAttrs
                    ;
                    NewNodeSeedAttrs = TmpNewNodeSeedAttrs
                ),
                append([seedAttributes(WhichNode, NewNodeSeedAttrs)], SeedAttrsIn, IntSeedAttrs),

                updateOutputAttributes(ChildNode, GraphIn, GraphOut, NodesIn, NodesOut, IntPrevOutputAttrs, PrevOutputAttrsOut, IntOutputAttrs, OutputAttrsOut, IntPrevRandomAttrs,
                      PrevRandomAttrsOut, IntRandomAttrs, RandomAttrsOut, IntPrevSeedAttrs, PrevSeedAttrsOut, IntSeedAttrs, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut)
                ;
                GraphOut = GraphIn, OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn, PrevSeedAttrsOut = PrevSeedAttrsIn,
                RandomAttrsOut = RandomAttrsIn, OuterRelationsOut = OuterRelationsIn, PrevOutputAttrsOut = PrevOutputAttrsIn, PrevRandomAttrsOut = PrevRandomAttrsIn
            )
        )
    ),!.
    
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
                intersectSets(RequiredAttrs, NodeOutputAttrs, TmpNewNodeOutputAttrs),
                (TmpNewNodeOutputAttrs = [] ->
                    NewNodeOutputAttrs =  NodeOutputAttrs
                    ;
                    NewNodeOutputAttrs = TmpNewNodeOutputAttrs
                ),
                append([outputAttributes(WhichNode, NewNodeOutputAttrs)], OutputAttrsIn, IntOutputAttrs1),

                select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, IntPrevRandomAttrs1),
                intersectSets(RequiredAttrs, NodeRandomAttrs, TmpNewNodeRandomAttrs),
                (TmpNewNodeRandomAttrs = [] ->
                    NewNodeRandomAttrs =  NodeRandomAttrs
                    ;
                    NewNodeRandomAttrs = TmpNewNodeRandomAttrs
                ),
                append([randomAttributes(WhichNode, NewNodeRandomAttrs)], RandomAttrsIn, IntRandomAttrs1),

                select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, IntPrevSeedAttrs1),
                intersectSets(RequiredAttrs, NodeSeedAttrs, TmpNewNodeSeedAttrs),
                (TmpNewNodeSeedAttrs = [] ->
                    NewNodeSeedAttrs =  NodeSeedAttrs
                    ;
                    NewNodeSeedAttrs = TmpNewNodeSeedAttrs
                ),
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
    ),!.
    
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
                intersectSets(RequiredAttrs, NodeOutputAttrs, TmpNewNodeOutputAttrs),
                (TmpNewNodeOutputAttrs = [] ->
                    NewNodeOutputAttrs =  NodeOutputAttrs
                    ;
                    NewNodeOutputAttrs = TmpNewNodeOutputAttrs
                ),
                append([outputAttributes(WhichNode, NewNodeOutputAttrs)], OutputAttrsIn, IntOutputAttrs1),

                select(randomAttributes(WhichNode, NodeRandomAttrs), PrevRandomAttrsIn, IntPrevRandomAttrs1),
                intersectSets(RequiredAttrs, NodeRandomAttrs, TmpNewNodeRandomAttrs),
                (TmpNewNodeRandomAttrs = [] ->
                    NewNodeRandomAttrs =  NodeRandomAttrs
                    ;
                    NewNodeRandomAttrs = TmpNewNodeRandomAttrs
                ),
                append([randomAttributes(WhichNode, NewNodeRandomAttrs)], RandomAttrsIn, IntRandomAttrs1),

                select(seedAttributes(WhichNode, NodeSeedAttrs), PrevSeedAttrsIn, IntPrevSeedAttrs1),
                intersectSets(RequiredAttrs, NodeSeedAttrs, TmpNewNodeSeedAttrs),
                (TmpNewNodeSeedAttrs = [] ->
                    NewNodeSeedAttrs =  NodeSeedAttrs
                    ;
                    NewNodeSeedAttrs = TmpNewNodeSeedAttrs
                ),
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
    ),!.

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
% **** Version that deals with a frame output
%identifySingleNodeRequiredAttributes(WhichNode, RequiredAttrs, Graph, Nodes, OutputAttrs, PrevOutputAttrs) :-
identifySingleNodeRequiredAttributes(WhichNode, RequiredAttrs, _, Nodes, OutputAttrs, _) :-
    member(frameoutput(WhichNode, _, _), Nodes),
    member(outputAttributes(WhichNode, RequiredAttrs), OutputAttrs).
    
% **** Version that deals with a seed
identifySingleNodeRequiredAttributes(WhichNode, RequiredAttrs, Graph, Nodes, OutputAttrs, PrevOutputAttrs) :-
    member(seed(WhichNode, SeedAttr), Nodes),
    member(outputAttributes(WhichNode, IntRequiredAttrs), OutputAttrs),
    (IntRequiredAttrs = [SeedAttr] -> % need to preserve child output attributes
        member(parent(WhichNode, ChildNode), Graph),
        member(outputAttributes(ChildNode, ChildOutputAttrs), PrevOutputAttrs),
        mergeSets(IntRequiredAttrs, ChildOutputAttrs, RequiredAttrs)
        ;
        RequiredAttrs = IntRequiredAttrs
    ).
    
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
% RULE FOR RECURSIVELY IDENTIFYING ALL CANDIDATE KEYS FOR EACH NODE IN THE ENTIRE PLAN
% As a by product it returns a graph having the join nodes characterized as 1x1, 1xN, Nx1, NxM
identifyPlanCandidateKeys(GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, OuterRelations, OutputAttrs, SeedAttrs) :-
    member(parent(planRoot, ChildNode), GraphIn),
    identifyPlanCandidateKeys(ChildNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, _, OuterRelations, OutputAttrs, SeedAttrs).

%*****************************************************************************************************************************
% RULE FOR RECURSIVELY IDENTIFYING ALL CANDIDATE KEYS FOR EACH NODE IN THE PLAN ANCOHORED BY A SPECIFIC NODE
%**** Version that deals with the case where the node is a tablescan node
identifyPlanCandidateKeys(WhichNode, _, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, _, OutputAttrs, _) :-
    member(tablescan(WhichNode, _, _), NodesIn),
    NodesOut = NodesIn,
        
    (setof(IntCandidateKey, member(candidateKey(WhichNode, IntCandidateKey), KeysIn), IntCandidateKeys1) ->
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
        removeProjectedCandidateKeys(IntCandidateKeys1, NodeOutputAttrs, IntCandidateKeys2),
        (IntCandidateKeys2 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys2), CandidateKeys)),
        setof(candidateKey(WhichNode, IntCandidateKey), member(candidateKey(WhichNode, IntCandidateKey), KeysIn), IntCandidateKeys3),
        subtractSet(KeysIn, IntCandidateKeys3, IntKeys),
        append(CandidateKeys, IntKeys, KeysOut);

        CandidateKeys = [candidateKey(WhichNode, [])],
        append(CandidateKeys, KeysIn, KeysOut)).

%**** Version that deals with the case where the node is a sole group by (1)
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn) :-
    member(genagg(WhichNode, _, GroupByAttrs, nothing, _, _), NodesIn),

    (setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(WhichNode, CandidateKey), KeysIn), ExistingCandidateKeys) ->
        CandidateKeys = ExistingCandidateKeys,
        KeysIn = KeysOut,
        NodesOut = NodesIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyPlanCandidateKeys(ChildNode, GraphIn, NodesIn, NodesOut, KeysIn, IntKeys, IntCandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),

        setof(CndKey, member(candidateKey(ChildNode, CndKey), IntCandidateKeys), IntCandidateKeys1),
        (IntCandidateKeys1 = [[]] ->
            IntCandidateKeys3 = [candidateKey(WhichNode, GroupByAttrs)]
            ;
            identifyKeysWithinAttributeSet(GroupByAttrs, IntCandidateKeys1, IntCandidateKeys2),
            setof(candidateKey(WhichNode, CandidateKey), member(CandidateKey, IntCandidateKeys2), IntCandidateKeys3)
        ),

        setof(CndKey, member(candidateKey(WhichNode, CndKey), IntCandidateKeys3), IntCandidateKeys4),
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn),
        removeProjectedCandidateKeys(IntCandidateKeys4, NodeOutputAttrs, IntCandidateKeys5),
        (IntCandidateKeys5 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys5), CandidateKeys)),

        append(CandidateKeys, IntKeys, KeysOut)), !.
        
%**** Version that deals with the case where the node is a generalized attribute (2)
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn) :-
    member(genagg(WhichNode, _, GroupByAttrs, AggExprs, _, AggExprsOutAttrs), NodesIn),
    AggExprs \= nothing,

    (setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(WhichNode, CandidateKey), KeysIn), ExistingCandidateKeys) ->
        CandidateKeys = ExistingCandidateKeys,
        KeysOut = KeysIn,
        NodesOut = NodesIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyPlanCandidateKeys(ChildNode, GraphIn, NodesIn, NodesOut, KeysIn, IntKeys, IntCandidateKeys1, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),

        setof(CndKey, member(candidateKey(ChildNode, CndKey), IntCandidateKeys1), IntCandidateKeys2),
        (GroupByAttrs = [] ->
            flatten(AggExprsOutAttrs, FlattenedAggExprsOutAttrs),
            IntCandidateKeys4 = [candidateKey(WhichNode, FlattenedAggExprsOutAttrs)]
            ;
            (IntCandidateKeys2 = [[]] ->
                IntCandidateKeys4 = [candidateKey(WhichNode, GroupByAttrs)]
                ;
                member(seedAttributes(WhichNode, NodeSeedAttrs), SeedAttrsIn),
                append(NodeSeedAttrs, GroupByAttrs, ExtendedGroupByAttrs),
                identifyKeysWithinAttributeSet(ExtendedGroupByAttrs, IntCandidateKeys2, IntCandidateKeys3),
                setof(candidateKey(WhichNode, CandidateKey), member(CandidateKey, IntCandidateKeys3), IntCandidateKeys4)
            )
        ),

        setof(CndKey, member(candidateKey(WhichNode, CndKey), IntCandidateKeys4), IntCandidateKeys5),
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn),
        removeProjectedCandidateKeys(IntCandidateKeys5, NodeOutputAttrs, IntCandidateKeys6),
        (IntCandidateKeys6 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys6), CandidateKeys)),

        append(CandidateKeys, IntKeys, KeysOut)), !.
        
%**** Version that deals with the case where the node is a dedup node
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn) :-
    member(dedup(WhichNode, DedupAttrs), NodesIn),

    (setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(WhichNode, CandidateKey), KeysIn), ExistingCandidateKeys) ->
        CandidateKeys = ExistingCandidateKeys,
        KeysOut = KeysIn,
        NodesOut = NodesIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyPlanCandidateKeys(ChildNode, GraphIn, NodesIn, NodesOut, KeysIn, IntKeys, IntCandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),

        setof(CndKey, member(candidateKey(ChildNode, CndKey), IntCandidateKeys), IntCandidateKeys1),
        (IntCandidateKeys1 = [[]] ->
            IntCandidateKeys3 = [candidateKey(WhichNode, DedupAttrs)]
            ;
            identifyKeysWithinAttributeSet(DedupAttrs, IntCandidateKeys1, IntCandidateKeys2),
            setof(candidateKey(WhichNode, CandidateKey), member(CandidateKey, IntCandidateKeys2), IntCandidateKeys3)
        ),

        setof(CKey, member(candidateKey(WhichNode, CKey), IntCandidateKeys3), IntCandidateKeys4),
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn),
        removeProjectedCandidateKeys(IntCandidateKeys4, NodeOutputAttrs, IntCandidateKeys5),
        (IntCandidateKeys5 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys5), CandidateKeys)),

        append(CandidateKeys, IntKeys, KeysOut)), !.
        
%**** Version that deals with the case where the node is a semijoin
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn) :-
    member(semijoin(WhichNode, DataSourceChildID, _), NodesIn),

    (setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(WhichNode, CandidateKey), KeysIn), ExistingCandidateKeys) ->
        CandidateKeys = ExistingCandidateKeys,
        KeysOut = KeysIn,
        NodesOut = NodesIn;

        select(parent(WhichNode, DataSourceChildID), GraphIn, IntGraph),
        member(parent(WhichNode, OtherChildID), IntGraph),

        identifyPlanCandidateKeys(DataSourceChildID, GraphIn, NodesIn, IntNodes, KeysIn, IntKeys1, IntCandidateKeys1, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),
        identifyPlanCandidateKeys(OtherChildID, GraphIn, IntNodes, NodesOut, IntKeys1, IntKeys2, _, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),

        setof(DataSourceChildCandidateKey, member(candidateKey(DataSourceChildID, DataSourceChildCandidateKey), IntCandidateKeys1), DataSourceChildCandidateKeys),
        (DataSourceChildCandidateKeys = [[]] ->
            IntCandidateKeys2 = [candidateKey(WhichNode, [])]
            ;
            setof(candidateKey(WhichNode, CK), member(candidateKey(DataSourceChildID, CK), IntCandidateKeys1), IntCandidateKeys2)
        ),

        setof(CndKey, member(candidateKey(WhichNode, CndKey), IntCandidateKeys2), IntCandidateKeys3),
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn),
        removeProjectedCandidateKeys(IntCandidateKeys3, NodeOutputAttrs, IntCandidateKeys4),
        (IntCandidateKeys4 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys4), CandidateKeys)),

        append(CandidateKeys, IntKeys2, KeysOut)), !.
        
%**** Version that deals with the case where the node is a selection node
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn) :-
    member(selection(WhichNode, _), NodesIn),

    (setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(WhichNode, CandidateKey), KeysIn), ExistingCandidateKeys) ->
        CandidateKeys = ExistingCandidateKeys,
        KeysOut = KeysIn,
        NodesOut = NodesIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyPlanCandidateKeys(ChildNode, GraphIn, NodesIn, NodesOut, KeysIn, IntKeys, IntCandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),
        setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(ChildNode, CandidateKey), IntCandidateKeys), IntCandidateKeys1),

        setof(CndKey, member(candidateKey(WhichNode, CndKey), IntCandidateKeys1), IntCandidateKeys2),
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn),
        removeProjectedCandidateKeys(IntCandidateKeys2, NodeOutputAttrs, IntCandidateKeys3),
        (IntCandidateKeys3 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys3), CandidateKeys)),

        append(CandidateKeys, IntKeys, KeysOut)), !.
        
%**** Version that deals with the case where the node is a projection node
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelations, OutputAttrs, SeedAttrs) :-
    member(projection(WhichNode, ProjectionAttrs), NodesIn),

    (setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(WhichNode, CandidateKey), KeysIn), CandidateKeys) ->
        KeysOut = KeysIn,
        NodesOut = NodesIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyPlanCandidateKeys(ChildNode, GraphIn, NodesIn, NodesOut, KeysIn, IntKeys1, IntCandidateKeys1, OuterRelations, OutputAttrs, SeedAttrs),
        setof(CandidateKey, member(candidateKey(ChildNode, CandidateKey), IntCandidateKeys1), IntCandidateKeys2),
        removeProjectedCandidateKeys(IntCandidateKeys2, ProjectionAttrs, IntCandidateKeys3),
        (IntCandidateKeys3 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys3), CandidateKeys)),
        append(CandidateKeys, IntKeys1, KeysOut)), !.
        
%**** Version that deals with the case where the node is a split node
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn) :-
    member(split(WhichNode, _), NodesIn),

    (setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(WhichNode, CandidateKey), KeysIn), ExistingCandidateKeys) ->
        CandidateKeys = ExistingCandidateKeys,
        KeysOut = KeysIn,
        NodesOut = NodesIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyPlanCandidateKeys(ChildNode, GraphIn, NodesIn, NodesOut, KeysIn, IntKeys, IntCandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),
        setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(ChildNode, CandidateKey), IntCandidateKeys), IntCandidateKeys1),
        
        setof(CndKey, member(candidateKey(WhichNode, CndKey), IntCandidateKeys1), IntCandidateKeys2),
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn),
        removeProjectedCandidateKeys(IntCandidateKeys2, NodeOutputAttrs, IntCandidateKeys3),
        (IntCandidateKeys3 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys3), CandidateKeys)),
        
        append(CandidateKeys, IntKeys, KeysOut)), !.
        
%**** Version that deals with the case where the node is a join
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn) :-
    member(join(WhichNode, Preds, _), NodesIn),

    (setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(WhichNode, CandidateKey), KeysIn), ExistingCandidateKeys) ->
        CandidateKeys = ExistingCandidateKeys,
        KeysOut = KeysIn,
        NodesOut = NodesIn;

        member(parent(WhichNode, JoinRHSChildID), GraphIn),
        member(parent(WhichNode, JoinLHSChildID), GraphIn),
        JoinLHSChildID @> JoinRHSChildID,

        identifyPlanCandidateKeys(JoinRHSChildID, GraphIn, NodesIn, IntNodes1, KeysIn, IntKeys1, IntCandidateKeys1, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),
        identifyPlanCandidateKeys(JoinLHSChildID, GraphIn, IntNodes1, IntNodes2, IntKeys1, IntKeys2, IntCandidateKeys2, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),

        setof(RHSCandidateKey, member(candidateKey(JoinRHSChildID, RHSCandidateKey), IntCandidateKeys1), RHSCandidateKeys),
        setof(LHSCandidateKey, member(candidateKey(JoinLHSChildID, LHSCandidateKey), IntCandidateKeys2), LHSCandidateKeys),

        (RHSCandidateKeys = [[]] -> EmptyKeys = yes; (LHSCandidateKeys = [[]] -> EmptyKeys = yes; EmptyKeys = no)),

        select(join(WhichNode, Preds, _), IntNodes2, IntNodes3),
        (EmptyKeys = yes ->
            append([join(WhichNode, Preds, manyToMany)], IntNodes3, NodesOut),
            IntCandidateKeys = [candidateKey(WhichNode, [])]
            ;
            % find out whether the join is 1x1, 1xN, Nx1 or NxM
            labelJoin(JoinType, Preds, IntCandidateKeys2, IntCandidateKeys1),

            %**** label join accordingly
            append([join(WhichNode, Preds, JoinType)], IntNodes3, NodesOut),

            (JoinType = oneToOne ->
                %**** and identify its keys
                setof(candidateKey(WhichNode, CK1), member(candidateKey(JoinRHSChildID, CK1), IntCandidateKeys1), CandidateKeys1),
                setof(candidateKey(WhichNode, CK2), member(candidateKey(JoinLHSChildID, CK2), IntCandidateKeys2), CandidateKeys2),
                append(CandidateKeys1, CandidateKeys2, IntCandidateKeys);
                true),

            (JoinType = oneToMany ->
                setof(candidateKey(WhichNode, CK), member(candidateKey(JoinRHSChildID, CK), IntCandidateKeys1), IntCandidateKeys);
                true),

            (JoinType = manyToOne ->
                setof(candidateKey(WhichNode, CK), member(candidateKey(JoinLHSChildID, CK), IntCandidateKeys2), IntCandidateKeys);
                true),

            (JoinType = manyToMany ->
                findall(candidateKey(WhichNode, CK), (member(candidateKey(JoinRHSChildID, CK1), IntCandidateKeys1),
                           member(candidateKey(JoinLHSChildID, CK2), IntCandidateKeys2), append(CK1, CK2, CK)), IntCandidateKeys);
                true)
        ),

        setof(CndKey, member(candidateKey(WhichNode, CndKey), IntCandidateKeys), IntCandidateKeys3),
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn),
        removeProjectedCandidateKeys(IntCandidateKeys3, NodeOutputAttrs, IntCandidateKeys4),
        (IntCandidateKeys4 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys4), CandidateKeys)),
        
        append(CandidateKeys, IntKeys2, KeysOut)), !.
        
%**** Version that deals with the case where the node is a vgwrapper node
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn) :-
    member(vgwrapper(WhichNode, VGName, _, _, _, _, _, _, SeedAttrOut), NodesIn),

    (setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(WhichNode, CandidateKey), KeysIn), ExistingCandidateKeys) ->
        CandidateKeys = ExistingCandidateKeys,
        KeysOut = KeysIn,
        NodesOut = NodesIn;

       % **** identify VGWrapper's children
        member(outerRelation(WhichNode, OuterRelationNode), OuterRelationsIn),
        setof(InnerRelationNode, member(parent(WhichNode, InnerRelationNode), GraphIn), IntInnerRelationNodes),
        select(OuterRelationNode, IntInnerRelationNodes, InnerRelationNodes),
        identifyPlanCandidateKeys(OuterRelationNode, GraphIn, NodesIn, IntNodes, KeysIn, IntKeys1, _, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),
        iterateOverVGWrapperInnerRelationsToIdentifyCandidateKeys(InnerRelationNodes, GraphIn, IntNodes, NodesOut, IntKeys1, IntKeys2, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),

        vgwrapperproperties(VGName, bundlesPerTuple(BPT), _, _),
        (BPT is 1 ->
            IntCandidateKeys1 = [candidateKey(WhichNode, [SeedAttrOut])]
            ;
            IntCandidateKeys1 = [candidateKey(WhichNode, [])]),

        setof(CndKey, member(candidateKey(WhichNode, CndKey), IntCandidateKeys1), IntCandidateKeys2),
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn),
        removeProjectedCandidateKeys(IntCandidateKeys2, NodeOutputAttrs, IntCandidateKeys3),
        (IntCandidateKeys3 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys3), CandidateKeys)),
        
        append(CandidateKeys, IntKeys2, KeysOut)), !.
        
%**** Version that deals with the case where the node is a frame output node
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelations, OutputAttrs, SeedAttrs) :-
    member(frameoutput(WhichNode, FrameOutputChildren, _), NodesIn),
    CandidateKeys = [candidateKey(WhichNode, [])],
    iterateOverFrameOutputChildrenToIdentifyCandidateKeys(FrameOutputChildren, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, OuterRelations, OutputAttrs, SeedAttrs), !.
        
%**** Version that deals with the case where the node is a seed node
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn) :-
    member(seed(WhichNode, SeedAddr), NodesIn),

    (setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(WhichNode, CandidateKey), KeysIn), ExistingCandidateKeys) ->
        CandidateKeys = ExistingCandidateKeys,
        KeysOut = KeysIn,
        NodesOut = NodesIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyPlanCandidateKeys(ChildNode, GraphIn, NodesIn, NodesOut, KeysIn, IntKeys1, IntCandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),

        setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(ChildNode, CandidateKey), IntCandidateKeys), CdtKeys),

        (CdtKeys = [candidateKey(WhichNode, [])] ->
            IntCandidateKeys1 = [candidateKey(WhichNode, [SeedAddr])]
            ;
            append([candidateKey(WhichNode, [SeedAddr])], CdtKeys, IntCandidateKeys1)
        ),

        setof(CndKey, member(candidateKey(WhichNode, CndKey), IntCandidateKeys1), IntCandidateKeys2),
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn),
        removeProjectedCandidateKeys(IntCandidateKeys2, NodeOutputAttrs, IntCandidateKeys3),
        (IntCandidateKeys3 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys3), CandidateKeys)),
        
        append(CandidateKeys, IntKeys1, KeysOut)), !.

%**** Version that deals with the case where the node is a scalar dunction
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn) :-
    member(scalarfunc(WhichNode, _, _, _), NodesIn),

    (setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(WhichNode, CandidateKey), KeysIn), ExistingCandidateKeys) ->
        CandidateKeys = ExistingCandidateKeys,
        KeysOut = KeysIn,
        NodesOut = NodesIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        identifyPlanCandidateKeys(ChildNode, GraphIn, NodesIn, NodesOut, KeysIn, IntKeys, IntCandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),
        setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(ChildNode, CandidateKey), IntCandidateKeys), IntCandidateKeys1),
        
        setof(CndKey, member(candidateKey(WhichNode, CndKey), IntCandidateKeys1), IntCandidateKeys2),
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn),
        removeProjectedCandidateKeys(IntCandidateKeys2, NodeOutputAttrs, IntCandidateKeys3),
        (IntCandidateKeys3 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys3), CandidateKeys)),
        
        append(CandidateKeys, IntKeys, KeysOut)), !.

%**** Version that deals with the case where the node is an antijoin
identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, CandidateKeys, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn) :-
    member(antijoin(WhichNode, DataSourceChildID, _), NodesIn),

    (setof(candidateKey(WhichNode, CandidateKey), member(candidateKey(WhichNode, CandidateKey), KeysIn), ExistingCandidateKeys) ->
        CandidateKeys = ExistingCandidateKeys,
        KeysOut = KeysIn,
        NodesOut = NodesIn;

        select(parent(WhichNode, DataSourceChildID), GraphIn, IntGraph),
        member(parent(WhichNode, OtherChildID), IntGraph),

        identifyPlanCandidateKeys(DataSourceChildID, GraphIn, NodesIn, IntNodes, KeysIn, IntKeys1, IntCandidateKeys1, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),
        identifyPlanCandidateKeys(OtherChildID, GraphIn, IntNodes, NodesOut, IntKeys1, IntKeys2, _, OuterRelationsIn, OutputAttrsIn, SeedAttrsIn),

        setof(DataSourceChildCandidateKey, member(candidateKey(DataSourceChildID, DataSourceChildCandidateKey), IntCandidateKeys1), DataSourceChildCandidateKeys),
        (DataSourceChildCandidateKeys = [[]] ->
            IntCandidateKeys2 = [candidateKey(WhichNode, [])]
            ;
            setof(candidateKey(WhichNode, CK), member(candidateKey(DataSourceChildID, CK), IntCandidateKeys1), IntCandidateKeys2)
        ),
        
        setof(CndKey, member(candidateKey(WhichNode, CndKey), IntCandidateKeys2), IntCandidateKeys3),
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn),
        removeProjectedCandidateKeys(IntCandidateKeys3, NodeOutputAttrs, IntCandidateKeys4),
        (IntCandidateKeys4 = [] -> CandidateKeys = [candidateKey(WhichNode, [])]; setof(candidateKey(WhichNode, CndKey), member(CndKey, IntCandidateKeys4), CandidateKeys)),
        
        append(CandidateKeys, IntKeys2, KeysOut)), !.
        
%*****************************************************************************************************************************
% RULE FOR IDENTIYING THE TYPE OF THE JOIN BASED ON ITS PREDICATEs
labelJoin(JoinType, PredIDs, LHSChildCandidateKeys, RHSChildCandidateKeys) :-
    %**** collect all relevant equality predicates
    current_predicate(compExp/5),
    %current_predicate(equals/3),
    %findall(equals(Pred, Attr1, Attr2), (member(Pred, PredIDs), equals(Pred, Attr1, Attr2)), EqualPredsList),
    findall(compExp(Pred, equals, LHSAttr, RHSAttr, RHSAttrType), (member(Pred, PredIDs), compExp(Pred, equals, LHSAttr, RHSAttr, RHSAttrType)), EqualPredsList),

    (predicatesContainCandidateKey(EqualPredsList, LHSChildCandidateKeys, WhichCandidateKeyList) ->
        (predicatesContainCandidateKey(EqualPredsList, RHSChildCandidateKeys, _) -> JoinType = oneToOne; JoinType = oneToMany)
        ;
        (predicatesContainCandidateKey(EqualPredsList, RHSChildCandidateKeys, WhichCandidateKeyList) -> JoinType = manyToOne; JoinType = manyToMany)
    ).
    
labelJoin(JoinType, [], _, _) :- JoinType = manyToMany.

%*****************************************************************************************************************************
% RULE THAT CHECKs WHETHER A SET OF PREDICATES CONTAINS A CANDIDATE KEY FOR A RELATION
% handles case where a set of attributes is defined as a candidate key for the table
predicatesContainCandidateKey(Preds, [candidateKey(_, CandidateKeyList)|RestOfCandidateKeys], WhichCandidateKeyList) :-
    (isKeyPartOfPredicate(Preds, CandidateKeyList) ->
        WhichCandidateKeyList = CandidateKeyList;
        predicatesContainCandidateKey(Preds, RestOfCandidateKeys, WhichCandidateKeyList)).

isKeyPartOfPredicate(Preds, [CandidateKeyPart|RestOfCandidateKey]) :-
    %(member(equals(_, _, CandidateKeyPart), Preds) ->
    (member(compExp(_, equals, _, CandidateKeyPart, _), Preds) ->
        isKeyPartOfPredicate(Preds, RestOfCandidateKey);
        
        %member(equals(_, CandidateKeyPart, RHSAttName), Preds),
        member(compExp(_, equals, CandidateKeyPart, _, RHSAttrType), Preds),
        RHSAttrType \= literal,
        isKeyPartOfPredicate(Preds, RestOfCandidateKey)
    ).
    
isKeyPartOfPredicate(_, []).

%*****************************************************************************************************************************
% RULEs THAT ITERATE OVER A VGWRAPPER's INNER RELATIONS TO COLLECT THE OUTPUT ATTRIBUTES FOR THE PLANS ANCORED AT THEM
iterateOverVGWrapperInnerRelationsToCollectOutputAttributes([WhichNode|OtherNodes], GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, RandomAttrsIn, RandomAttrsOut, OuterRelationsIn, SeedAttrsIn, SeedAttrsOut) :-
    identifyOutputAttributes(WhichNode, GraphIn, IntGraph, NodesIn, IntNodes, OutputAttrsIn, IntOutputAttrs, SeedAttrsIn, IntSeedAttrs, RandomAttrsIn, IntRandomAttrs, OuterRelationsIn),
    iterateOverVGWrapperInnerRelationsToCollectOutputAttributes(OtherNodes, IntGraph, GraphOut, IntNodes, NodesOut, IntOutputAttrs, OutputAttrsOut, IntRandomAttrs, RandomAttrsOut, OuterRelationsIn, IntSeedAttrs, SeedAttrsOut).
    
iterateOverVGWrapperInnerRelationsToCollectOutputAttributes([], GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, RandomAttrsIn, RandomAttrsOut, _, SeedAttrsIn, SeedAttrsOut) :-
    NodesOut = NodesIn,
    GraphOut = GraphIn,
    OutputAttrsOut = OutputAttrsIn,
    RandomAttrsOut = RandomAttrsIn,
    SeedAttrsOut = SeedAttrsIn.
    
%*****************************************************************************************************************************
% RULEs THAT ITERATE OVER A OUTPUT FRAME's CHILDREN TO COLLECT THE OUTPUT ATTRIBUTES FOR THE PLANS ANCORED AT THEM
iterateOverFrameOutputChildrenToCollectOutputAttributes([WhichNode|OtherNodes], GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, ParentOutputAttrsIn, ParentOutputAttrsOut,
                RandomAttrsIn, RandomAttrsOut, ParentRandomAttrsIn, ParentRandomAttrsOut, OuterRelations, SeedAttrsIn, SeedAttrsOut, ParentSeedAttrsIn, ParentSeedAttrsOut) :-
    identifyOutputAttributes(WhichNode, GraphIn, IntGraph, NodesIn, IntNodes, OutputAttrsIn, IntOutputAttrs, SeedAttrsIn, IntSeedAttrs, RandomAttrsIn, IntRandomAttrs, OuterRelations),

    member(outputAttributes(WhichNode, WhichNodeOutputAttrs), IntOutputAttrs),   % update parent's output, random, seed attributes
    append(WhichNodeOutputAttrs, ParentOutputAttrsIn, IntParentOutputAttrs),
    member(randomAttributes(WhichNode, WhichNodeRandomAttrs), IntRandomAttrs),
    append(WhichNodeRandomAttrs, ParentRandomAttrsIn, IntParentRandomAttrs),
    member(seedAttributes(WhichNode, WhichNodeSeedAttrs), IntSeedAttrs),
    append(WhichNodeSeedAttrs, ParentSeedAttrsIn, IntParentSeedAttrs),

    iterateOverFrameOutputChildrenToCollectOutputAttributes(OtherNodes, IntGraph, GraphOut, IntNodes, NodesOut, IntOutputAttrs, OutputAttrsOut, IntParentOutputAttrs, ParentOutputAttrsOut,
                IntRandomAttrs, RandomAttrsOut, IntParentRandomAttrs, ParentRandomAttrsOut, OuterRelations, IntSeedAttrs, SeedAttrsOut, IntParentSeedAttrs, ParentSeedAttrsOut).

iterateOverFrameOutputChildrenToCollectOutputAttributes([], GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, ParentOutputAttrsIn, ParentOutputAttrsOut,
                RandomAttrsIn, RandomAttrsOut, ParentRandomAttrsIn, ParentRandomAttrsOut, _, SeedAttrsIn, SeedAttrsOut, ParentSeedAttrsIn, ParentSeedAttrsOut) :-
    NodesOut = NodesIn,
    GraphOut = GraphIn,
    OutputAttrsOut = OutputAttrsIn,
    ParentOutputAttrsOut = ParentOutputAttrsIn,
    RandomAttrsOut = RandomAttrsIn,
    ParentRandomAttrsOut = ParentRandomAttrsIn,
    SeedAttrsOut = SeedAttrsIn,
    ParentSeedAttrsOut = ParentSeedAttrsIn.
    
%*****************************************************************************************************************************
% RULEs THAT ITERATE OVER A VGWRAPPER's INNER RELATIONS TO UPDATE THE OUTPUT ATTRIBUTES FOR THE PLANS ANCORED AT THEM
iterateOverVGWrapperInnerRelationsToUpdateOutputAttributes([WhichNode|OtherNodes], GraphIn, GraphOut, NodesIn, NodesOut, PrevOutputAttrsIn, PrevOutputAttrsOut, OutputAttrsIn, OutputAttrsOut,
                   PrevRandomAttrsIn, PrevRandomAttrsOut, RandomAttrsIn, RandomAttrsOut, PrevSeedAttrsIn, PrevSeedAttrsOut, SeedAttrsIn, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut) :-
    updateOutputAttributes(WhichNode, GraphIn, IntGraph, NodesIn, IntNodes, PrevOutputAttrsIn, IntPrevOutputAttrs, OutputAttrsIn, IntOutputAttrs,
                   PrevRandomAttrsIn, IntPrevRandomAttrs, RandomAttrsIn, IntRandomAttrs, PrevSeedAttrsIn, IntPrevSeedAttrs, SeedAttrsIn, IntSeedAttrs, OuterRelationsIn, IntOuterRelations),
    iterateOverVGWrapperInnerRelationsToUpdateOutputAttributes(OtherNodes, IntGraph, GraphOut, IntNodes, NodesOut, IntPrevOutputAttrs, PrevOutputAttrsOut, IntOutputAttrs, OutputAttrsOut,
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
% RULEs THAT ITERATE OVER A OUTPUT FRAME's CHILDREN TO UPDATE THE OUTPUT ATTRIBUTES FOR THE PLANS ANCORED AT THEM
iterateOverFrameOutputChildrenToUpdateOutputAttributes([WhichNode|OtherNodes], GraphIn, GraphOut, NodesIn, NodesOut, PrevOutputAttrsIn, PrevOutputAttrsOut, OutputAttrsIn,
                      OutputAttrsOut, PrevRandomAttrsIn, PrevRandomAttrsOut, RandomAttrsIn, RandomAttrsOut, PrevSeedAttrsIn, PrevSeedAttrsOut, SeedAttrsIn, SeedAttrsOut,
                      OuterRelationsIn, OuterRelationsOut) :-
    updateOutputAttributes(WhichNode, GraphIn, IntGraph, NodesIn, IntNodes, PrevOutputAttrsIn, IntPrevOutputAttrs, OutputAttrsIn, IntOutputAttrs, PrevRandomAttrsIn,
                       IntPrevRandomAttrs, RandomAttrsIn, IntRandomAttrs, PrevSeedAttrsIn, IntPrevSeedAttrs, SeedAttrsIn, IntSeedAttrs, OuterRelationsIn, IntOuterRelations),
    iterateOverFrameOutputChildrenToUpdateOutputAttributes(OtherNodes, IntGraph, GraphOut, IntNodes, NodesOut, IntPrevOutputAttrs, PrevOutputAttrsOut, IntOutputAttrs,
                      OutputAttrsOut, IntPrevRandomAttrs, PrevRandomAttrsOut, IntRandomAttrs, RandomAttrsOut, IntPrevSeedAttrs, PrevSeedAttrsOut, IntSeedAttrs, SeedAttrsOut,
                      IntOuterRelations, OuterRelationsOut).
                      
iterateOverFrameOutputChildrenToUpdateOutputAttributes([], GraphIn, GraphOut, NodesIn, NodesOut, PrevOutputAttrsIn, PrevOutputAttrsOut, OutputAttrsIn, OutputAttrsOut,
               PrevRandomAttrsIn, PrevRandomAttrsOut, RandomAttrsIn, RandomAttrsOut, PrevSeedAttrsIn, PrevSeedAttrsOut, SeedAttrsIn, SeedAttrsOut, OuterRelationsIn, OuterRelationsOut):-
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
% RULEs THAT ITERATE OVER A VGWRAPPER's INNER RELATIONS TO IDENTIFY THE CANDIDATE KEYS FOR THE PLANS ANCORED AT THEM
iterateOverVGWrapperInnerRelationsToIdentifyCandidateKeys([WhichNode|OtherNodes], GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, OuterRelations, OutputAttrs, SeedAttrs) :-
    identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, IntNodes, KeysIn, IntKeys, _, OuterRelations, OutputAttrs, SeedAttrs),
    iterateOverVGWrapperInnerRelationsToIdentifyCandidateKeys(OtherNodes, GraphIn, IntNodes, NodesOut, IntKeys, KeysOut, OuterRelations, OutputAttrs, SeedAttrs).

iterateOverVGWrapperInnerRelationsToIdentifyCandidateKeys([], _, NodesIn, NodesOut, KeysIn, KeysOut, _, _, _) :-
    NodesOut = NodesIn,
    KeysOut = KeysIn.

%*****************************************************************************************************************************
% RULEs THAT ITERATE OVER A FRAMEOUTPUT'S CHILDREN TO IDENTIFY THE CANDIDATE KEYS FOR THE PLANS ANCORED AT THEM
iterateOverFrameOutputChildrenToIdentifyCandidateKeys([WhichNode|OtherNodes], GraphIn, NodesIn, NodesOut, KeysIn, KeysOut, OuterRelations, OutputAttrs, SeedAttrs) :-
    identifyPlanCandidateKeys(WhichNode, GraphIn, NodesIn, IntNodes, KeysIn, IntKeys, _, OuterRelations, OutputAttrs, SeedAttrs),
    iterateOverFrameOutputChildrenToIdentifyCandidateKeys(OtherNodes, GraphIn, IntNodes, NodesOut, IntKeys, KeysOut, OuterRelations, OutputAttrs, SeedAttrs).

iterateOverFrameOutputChildrenToIdentifyCandidateKeys([], _, NodesIn, NodesOut, KeysIn, KeysOut, _, _, _) :-
    NodesOut = NodesIn,
    KeysOut = KeysIn.
    
%*****************************************************************************************************************************
% RULE THAT CHECKs WHETHER A SET OF PREDICATES CONTAINS A FOREIGN KEY FOR A RELATION
% receives as input the set of referenced candidate keys that exist in the predicate
%predicatesContainForeignKey(CandidateKeyList, Preds) :-
%    (current_predicate(foreignKey/2) ->
%        (foreignKey(ForeignKeyList, CandidateKeyList) -> isForeignKeyPartOfPredicate(Preds, ForeignKeyList); false)
%        ;
%        false
%    ).
    
%isForeignKeyPartOfPredicate(Preds, [ForeignKeyPart|RestOfForeignKey]) :-
%    (member(equals(_, _, ForeignKeyPart), Preds) ->
%        isForeignKeyPartOfPredicate(Preds, RestOfForeignKey);
%
%        member(equals(_, ForeignKeyPart, RHSAttName), Preds),
%        RHSAttName \== literal,
%        isForeignKeyPartOfPredicate(Preds, RestOfForeignKey)
%    ).

%isForeignKeyPartOfPredicate(_, []).

%*****************************************************************************************************************************
% RULE THAT REMOVES FROM A LIST OF CANDIDATE KEYS THOSE THAT ARE NOT SUPERSET OF THE PROJECTION SET
removeProjectedCandidateKeys([CandidateKey|OtherKeys], ProjectionSet, CandidateKeysOut) :-
    removeProjectedCandidateKeys(OtherKeys, ProjectionSet, IntCandidateKeys),
    (subset(CandidateKey, ProjectionSet) -> append([CandidateKey], IntCandidateKeys, CandidateKeysOut);
        CandidateKeysOut = IntCandidateKeys).
    
removeProjectedCandidateKeys([], _, []).

%*****************************************************************************************************************************
% RULE THAT TAKES A LIST OF CANDIDATE KEYS AND EXTRACTS THOSE THAT ARE NOT SUBSET OF THE GROUP_BY SET
identifyKeysWithinAttributeSet(AttrSet, CandidateKeysIn, CandidateKeysOut) :-
    findall(RedundantCandidateKey, (member(RedundantCandidateKey, CandidateKeysIn), subtractSet(RedundantCandidateKey, AttrSet, NewSet), NewSet \= []), RedundantCandidateKeys),
    subtractSet(CandidateKeysIn, RedundantCandidateKeys, IntCandidateKeys),
    (IntCandidateKeys = [] ->
        CandidateKeysOut = [AttrSet]
        ;
        CandidateKeysOut = IntCandidateKeys
    ).

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
    %current_predicate(equals/3),
    current_predicate(compExp/5),
    %equals(Pred, LHSAttr, RHSAttr),
    compExp(Pred, equals, LHSAttr, RHSAttr, RHSAttrType),
    mergeSets([LHSAttr], ReferencedAttrsIn, IntReferencedAttrs),
    (RHSAttrType = literal -> ReferencedAttrsOut = IntReferencedAttrs; mergeSets([RHSAttr], IntReferencedAttrs, ReferencedAttrsOut)).

% **** version that deals with non-equality predicates
extractAttrsFromSinglePredicate(Pred, ReferencedAttrsIn, ReferencedAttrsOut) :-
    %current_predicate(otherComparisonOp/4),
    %otherComparisonOp(Pred, _, LHSAttr, RHSAttr),
    current_predicate(compExp/5),
    compExp(Pred, CompExpType, LHSAttr, RHSAttr, RHSAttrType),
    CompExpType \= equals,
    mergeSets([LHSAttr], ReferencedAttrsIn, IntReferencedAttrs),
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
    length(SeedOperators, SeedOperatorsLen),
    (SeedOperatorsLen = 1 ->
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
            append([semijoin(OperatorParent, NewProjectionNodeID, Preds)], IntNodes4, IntNodes5)
            ;
            IntNodes4 = IntNodes3
        ),

        (select(frameoutput(OperatorParent, FrameOutputChildren, FrameOutputFiles), IntNodes4, IntNodes5) ->
            replaceInList(Operator, NewProjectionNodeID, FrameOutputChildren, [], NewFrameOutputChildren),
            append([frameoutput(OperatorParent, NewFrameOutputChildren, FrameOutputFiles)], IntNodes5, NodesOut)
            ;
            NodesOut = IntNodes4
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
%**** version that deals with the case where the node is a top frame operator
updatePlanOutputAttributes(WhichNode, Graph, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelations, RandomAttrs, SeedAttrsIn, SeedAttrsOut) :-
    member(frameoutput(WhichNode, FrameOutputChildren, _), NodesIn),

    (member(outputAttributes(WhichNode, _), OutputAttrsIn) ->
        OutputAttrsOut = OutputAttrsIn, NodesOut = NodesIn, SeedAttrsOut = SeedAttrsIn;

        iterateOverFrameOutputChildrenToUpdatePlanOutputAttributes(FrameOutputChildren, Graph, NodesIn, NodesOut, OutputAttrsIn, IntOutputAttrs,
                      [], FrameOutputOutputAttrs, RandomAttrs, OuterRelations, SeedAttrsIn, IntSeedAttrs, [], FrameOutputSeedAttrs),

        append([outputAttributes(WhichNode, FrameOutputOutputAttrs)], IntOutputAttrs, OutputAttrsOut),
        append([seedAttributes(WhichNode, FrameOutputSeedAttrs)], IntSeedAttrs, SeedAttrsOut)
    ).
    
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
        iterateOverVGWrapperInnerRelationsToUpdatePlanOutputAttributes(InnerRelationNodes, GraphIn, IntNodes1, IntNodes2, IntOutputAttrs1, IntOutputAttrs2, OuterRelationsIn, RandomAttrsIn, IntSeedAttrs1, IntSeedAttrs),

        % **** delete is present from outer projection if needed
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

        ((member(parent(ParentNode, WhichNode), GraphIn), member(parent(GrandparentNode, ParentNode), GraphIn), \+ member(join(GrandparentNode, _, _), NodesIn)) ->
            NodeOutputAttrs = VGOutAttrs;
            append([SeedAttrOut], VGOutAttrs, NodeOutputAttrs)),
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
iterateOverVGWrapperInnerRelationsToUpdatePlanOutputAttributes([WhichNode|OtherNodes], GraphIn, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, SeedAttrsOut) :-
    updatePlanOutputAttributes(WhichNode, GraphIn, NodesIn, IntNodes, OutputAttrsIn, IntOutputAttrs, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn, IntSeedAttrs),
    iterateOverVGWrapperInnerRelationsToUpdatePlanOutputAttributes(OtherNodes, GraphIn, IntNodes, NodesOut, IntOutputAttrs, OutputAttrsOut, OuterRelationsIn, RandomAttrsIn, IntSeedAttrs, SeedAttrsOut).

iterateOverVGWrapperInnerRelationsToUpdatePlanOutputAttributes([], _, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, _, _, SeedAttrsIn, SeedAttrsOut) :-
    OutputAttrsOut = OutputAttrsIn,
    NodesOut = NodesIn,
    SeedAttrsOut = SeedAttrsIn.
    
%*****************************************************************************************************************************
% RULEs THAT ITERATE OVER A FRAME OUTPUT'S CHILDREN TO UPDATE THE OUTPUT ATTRIBUTES FOR THE PLANS ANCORED AT THEM
iterateOverFrameOutputChildrenToUpdatePlanOutputAttributes([WhichNode|OtherNodes], Graph, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, FrameOutputOutputAttrsIn,
                   FrameOutputOutputAttrsOut, RandomAttrs, OuterRelations, SeedAttrsIn, SeedAttrsOut, FrameOutputSeedAttrsIn, FrameOutputSeedAttrsOut) :-
    updatePlanOutputAttributes(WhichNode, Graph, NodesIn, IntNodes, OutputAttrsIn, IntOutputAttrs, OuterRelations, RandomAttrs, SeedAttrsIn, IntSeedAttrs),
    member(outputAttributes(WhichNode, WhichNodeOutputAttrs), IntOutputAttrs),
    append(WhichNodeOutputAttrs, FrameOutputOutputAttrsIn, IntFrameOutputOutputAttrs),
    member(seedAttributes(WhichNode, WhichNodeSeedAttrs), IntSeedAttrs),
    append(WhichNodeSeedAttrs, FrameOutputSeedAttrsIn, IntFrameOutputSeedAttrs),
    iterateOverFrameOutputChildrenToUpdatePlanOutputAttributes(OtherNodes, Graph, IntNodes, NodesOut, IntOutputAttrs, OutputAttrsOut, IntFrameOutputOutputAttrs,
                   FrameOutputOutputAttrsOut, RandomAttrs, OuterRelations, IntSeedAttrs, SeedAttrsOut, IntFrameOutputSeedAttrs, FrameOutputSeedAttrsOut).

iterateOverFrameOutputChildrenToUpdatePlanOutputAttributes([], _, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut, FrameOutputOutputAttrsIn,
                   FrameOutputOutputAttrsOut, _, _, SeedAttrsIn, SeedAttrsOut, FrameOutputSeedAttrsIn, FrameOutputSeedAttrsOut) :-
    OutputAttrsOut = OutputAttrsIn,
    FrameOutputOutputAttrsOut = FrameOutputOutputAttrsIn,
    NodesOut = NodesIn,
    SeedAttrsOut = SeedAttrsIn,
    FrameOutputSeedAttrsOut = FrameOutputSeedAttrsIn.
    
%*********************************************************************************************************************************
% RULE FOR OBTAINING ONLY THE FIRST FEW PLANS FROM A SORTED LIST OF PLANS
getCheapestPlans([Plan|OtherPlans], PreservedPlansIn, PreservedPlansOut, PreservedPlanNo) :-
    not(PreservedPlanNo is 0),
    append([Plan], PreservedPlansIn, IntPreservedPlans),
    IntPreservedPlanNo is PreservedPlanNo - 1,
    getCheapestPlans(OtherPlans, IntPreservedPlans, PreservedPlansOut, IntPreservedPlanNo).
    
getCheapestPlans(_, PreservedPlansIn, PreservedPlansOut, PreservedPlanNo) :-
    PreservedPlanNo is 0,
    PreservedPlansOut = PreservedPlansIn.
    
getCheapestPlans([], PreservedPlansIn, PreservedPlansOut, _) :-
    PreservedPlansOut = PreservedPlansIn.

projectVGWrapperOuterSeeds(GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut,
                                       [outerRelation(VGWrapperID, OuterRelationID)|OtherOuterRelations], OuterRelationsIn, OuterRelationsOut, RandomAttrsIn, RandomAttrsOut) :-
    member(vgwrapper(VGWrapperID, _, _, SeedAttrIn, _, _, _, _, _), NodesIn),
    gensym(projectionNode, NewProjectionNodeID),

    select(parent(VGWrapperID, OuterRelationID), GraphIn, IntGraph1),     % ***update query plan graph
    append([parent(VGWrapperID, NewProjectionNodeID)], IntGraph1, IntGraph2),
    append([parent(NewProjectionNodeID, OuterRelationID)], IntGraph2, IntGraph),

    select(outerRelation(VGWrapperID, OuterRelationID), OuterRelationsIn, IntOuterRelations1),
    append([outerRelation(VGWrapperID, NewProjectionNodeID)], IntOuterRelations1, IntOuterRelations),

    append([projection(NewProjectionNodeID, [SeedAttrIn])], NodesIn, IntNodes),

    append([outputAttributes(NewProjectionNodeID, [SeedAttrIn])], OutputAttrsIn, IntOutputAttrs),
    append([randomAttributes(NewProjectionNodeID, [])], RandomAttrsIn, IntRandomAttrs),

    projectVGWrapperOuterSeeds(IntGraph, GraphOut, IntNodes, NodesOut, IntOutputAttrs, OutputAttrsOut,
                                       OtherOuterRelations, IntOuterRelations, OuterRelationsOut, IntRandomAttrs, RandomAttrsOut).

projectVGWrapperOuterSeeds(GraphIn, GraphOut, NodesIn, NodesOut, OutputAttrsIn, OutputAttrsOut,
                                       [], OuterRelationsIn, OuterRelationsOut, RandomAttrsIn, RandomAttrsOut) :-
    GraphOut = GraphIn,
    NodesOut = NodesIn,
    OutputAttrsOut = OutputAttrsIn,
    OuterRelationsOut = OuterRelationsIn,
    RandomAttrsOut = RandomAttrsIn.
                                       
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
% RULEs THAT OUTPUT THE AUGMENTED PLAN IN A GIVEN FILE
%outputPlan(Graph, Nodes, OutputAttrs, RandomAttrs, OuterRelations, OutFile) :-
outputPlan(Graph, Nodes, OutputAttrs, RandomAttrs, OuterRelations, EstimatedCosts, OutFile) :-
    telling(Old),
    tell(OutFile),
    %tell('Q_opt.pl'),
    %tell('tmp/qopt.pl'),

    (current_predicate(vgfunction/4) ->
        findall(vgfunction(VGFuncName, VGFuncPath, VGParams, VGOutput), vgfunction(VGFuncName, VGFuncPath, VGParams, VGOutput), VGFunctions),
        ouputListCanonical(VGFunctions)
        ;
        true
    ), nl,
    
    (current_predicate(relation/3) ->
        findall(relation(RelName, RelLocation, RelAttrs), relation(RelName, RelLocation, RelAttrs), Relations),
        ouputListCanonical(Relations)
        ;
        true
    ), nl,

    (current_predicate(attributeType/2) ->
        findall(attributeType(AttrName, AttrType), attributeType(AttrName, AttrType), AttrTypes),
        ouputListCanonical(AttrTypes)
        ;
        true
    ), nl,

    % ***** remove planRoot node
    findall(parent(ParentNode, Node), (member(parent(ParentNode, Node), Graph), ParentNode \= planRoot), NewGraph),
    ouputList(NewGraph), nl,

    ouputListCanonical(Nodes), nl,

    ouputList(OutputAttrs), nl,

    ouputList(RandomAttrs), nl,

    ouputList(OuterRelations), nl,

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
        findall(aggExp(ExpName, ExpFunc, ExpAttr, ExpType), aggExp(ExpName, ExpFunc, ExpAttr, ExpType), AggregateExpressions)
        ;
        AggregateExpressions = []
    ),
    ouputList(AggregateExpressions),
    write('\n'),

    (current_predicate(arithExp/6) ->
        findall(arithExp(ExpName, ExpType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType), arithExp(ExpName, ExpType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType), ArithmeticExpressions)
        ;
        ArithmeticExpressions = []
    ),
    ouputList(ArithmeticExpressions),
    write('\n'),

    (current_predicate(function/3) ->
        findall(function(FuncName, FuncType, FuncAttrs), function(FuncName, FuncType, FuncAttrs), Functions)
        ;
        Functions = []
    ),
    ouputList(Functions),
    write('\n'),
    
    (current_predicate(verbatim/3) ->
        findall(verbatim(VerbatimName, VerbatimExp, ExpType), verbatim(VerbatimName, VerbatimExp, ExpType), Verbatims)
        ;
        Verbatims = []
    ),
    ouputListCanonical(Verbatims),
    write('\n'),
    
    (current_predicate(temporaryTable/3) ->
        findall(temporaryTable(TableName, TableAttrs, TableRows), temporaryTable(TableName, TableAttrs, TableRows), TemporaryTables)
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
    write('.\n\n'),
    
    % **** output regressors
    ouputListCanonical(EstimatedCosts),
    
    told,
    tell(Old).
    
outputPlanInfo(FileNames, FrameOutputNodeChildren, OutputAttrs, RandomAttrs) :-
    telling(Old2),
    tell('Q_info.pl'),
    %tell('/var/tmp/jaql/Q_info.pl'),
    outputFilePlanInfo(FileNames, FrameOutputNodeChildren, OutputAttrs, RandomAttrs),
    told,
    tell(Old2).

outputFilePlanInfo([FileName|OtherFileNames], FrameOutputNodeChildren, OutputAttrs, RandomAttrs) :-
    RelationName = FileName,
    nth0(0, FrameOutputNodeChildren, WhichNode),
    member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
    member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrs),
    identifyAttributeTypes(NodeOutputAttrs, [], NodeOutputAttrTypes),
    writeq(outputRelation(RelationName, NodeOutputAttrs, NodeOutputAttrTypes, NodeRandomAttrs)),
    write('.\n'),
    append([WhichNode], IntFrameOutputNodeChildren, FrameOutputNodeChildren),
    outputFilePlanInfo(OtherFileNames, IntFrameOutputNodeChildren, OutputAttrs, RandomAttrs).
    
outputFilePlanInfo([], [], _, _) :- true.

outputCutInfo(Graph, Nodes, RandomAttrs, Stats) :-
    telling(Old3),
    tell('Q_cut_info.pl'),
    %tell('/var/tmp/jaql/Q_cut_info.pl'),

    %**** output number of bytes coming out of top level operator
    write('END: '),
    member(parent(planRoot, RootNode), Graph),
    member(stats(RootNode, _, _, _, _, RootNodeSize), Stats),
    write(RootNodeSize),
    write('\n'),
    
    %**** for each random table output size of parent operator's, if predicate
    findall(tablescan(TableScanNode, RelationName, Attrs), member(tablescan(TableScanNode, RelationName, Attrs), Nodes), TableScanNodes),
    outputTableScanParentPredicateSizes(TableScanNodes, Graph, Nodes, RandomAttrs, Stats),
    
    told,
    tell(Old3).

outputTableScanParentPredicateSizes([tablescan(TableScanNode, RelationName, _)|OtherNodes], Graph, Nodes, RandomAttrs, Stats) :-
    member(randomAttributes(TableScanNode, TableScanNodeRandomAttrs), RandomAttrs),
    (TableScanNodeRandomAttrs = [] ->
        true
        ;
        writeq(RelationName),
        write(': '),
        findall(ParentNode, member(parent(ParentNode, TableScanNode), Graph), TableScanParentNodes),
        outputTableScanParentPredicateSize(TableScanParentNodes, Nodes, Stats),
        write('\n')
    ),
    outputTableScanParentPredicateSizes(OtherNodes, Graph, Nodes, RandomAttrs, Stats).
    
outputTableScanParentPredicateSizes([], _, _, _, _) :- true.

outputTableScanParentPredicateSize([TableScanParentNode|OtherParentNodes], Nodes, Stats) :-
    ((member(selection(TableScanParentNode, _), Nodes);member(projection(TableScanParentNode, _), Nodes)) ->
        member(stats(TableScanParentNode, _, _, _, _, TableScanParentNodeSize), Stats),
        write(TableScanParentNodeSize),
        write(' ')
        ;
        write('-1')
    ),
    outputTableScanParentPredicateSize(OtherParentNodes, Nodes, Stats).

outputTableScanParentPredicateSize([], _, _) :- true.

identifyAttributeTypes([AttrName|OtherAttrs], NodeOutputAttrTypesIn, NodeOutputAttrTypesOut) :-
    attributeType(AttrName, AttrType),
    append(NodeOutputAttrTypesIn, [AttrType], IntNodeOutputAttrTypes),
    identifyAttributeTypes(OtherAttrs, IntNodeOutputAttrTypes, NodeOutputAttrTypesOut).
    
identifyAttributeTypes([], NodeOutputAttrTypesIn, NodeOutputAttrTypesOut) :-
    NodeOutputAttrTypesOut = NodeOutputAttrTypesIn.
    
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


%%%% STUFF FOR BREAKING UP SELECTION NODES
%%%% ADDED BY LUIS.
breakUpSelectionNodes([], Graph, Graph, Nodes, Nodes) :-
    !.

breakUpSelectionNodes([H|T], GraphIn, GraphOut, NodesIn, NodesOut) :-
    memberchk(selection(H, [FirstPred|RemainingPreds]), NodesIn),
    memberchk(parent(H, KidNode), GraphIn),
    breakUpOneSelectionNode(H, FirstPred, RemainingPreds, KidNode, GraphIn, NodesIn, GraphMid, NodesMid),
    breakUpSelectionNodes(T, GraphMid, GraphOut, NodesMid, NodesOut),
    !.

breakUpOneSelectionNode(SelNode, FirstPred, RemainingPreds, KidNode, GraphIn, NodesIn, IntGraph2, IntNodes2) :-

    %%% break them up, obtaining a whole sub-plan.
    breakUpSelection(SelNode, RemainingPreds, KidNode, NewNodes0, NewGraph0),
    last(NewNodes0, selection(NewKid, _)),
    NewNodes1 = [selection(SelNode, [FirstPred])|NewNodes0],
    NewGraph1 = [parent(SelNode, NewKid)|NewGraph0],

    %%% Remove
    selectchk(selection(SelNode, [FirstPred|RemainingPreds]), NodesIn, NodesIn1),
    selectchk(parent(SelNode, KidNode), GraphIn, GraphIn1),
    
    %% Merge
    append(NodesIn1, NewNodes1, IntNodes2),
    append(GraphIn1, NewGraph1, IntGraph2),
    !.

breakUpSelection(_, [], _, [], []) :-
    !.

breakUpSelection(SelNode, [H|T], KidNode, [selection(NewNode, [H])|TN], [parent(NewNode, KidNode)|TG]) :-
    atom_concat(SelNode, H, NewNode),
    breakUpSelection(SelNode, T, NewNode, TN, TG),
    !.

%%% Added by Luis
outputCostInfo(PlanCost, OutFile) :-
  telling(Old),
  tell(OutFile),
% write('totalPlanCost('),
  format('~2f', PlanCost),
% write(').'), 
  nl,
  told,
  tell(Old).
