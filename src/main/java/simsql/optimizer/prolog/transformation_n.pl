

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
% RULE THAT IMPLEMENTS AN EMPTY TRANSFORMATION THAT DOES NOTHING
transform(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _) :- false.
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN SELECTIONS PAST A JOIN
% Returns true if (GraphOut, NodesOut) can legally result from pushing a selection operation past a join
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSelectionDownJoin),

    %**** find some selection predicate that is above some join
    select(selection(SelectionNodeID, SelectionPreds), NodesIn, IntNodes1),
    member(parent(SelectionNodeID, JoinNodeID), GraphIn),
    select(join(JoinNodeID, JoinPreds, JoinLabel), IntNodes1, IntNodes2),

    %**** rule applies only to nodes with one parent
    nodeHasSingleParent(JoinNodeID, GraphIn),

    %**** find the two children of the join
    member(parent(JoinNodeID, JoinLHSChildID), GraphIn),
    member(parent(JoinNodeID, JoinRHSChildID), GraphIn),
    %**** it is the same transformation if we switch the LHS and RHS
    JoinLHSChildID @> JoinRHSChildID,

    %**** find all attrs that are reachable from either the LHS or the RHS of the join
    member(outputAttributes(JoinLHSChildID, LHSChildOutputAttrs), OutputAttrsIn),
    member(outputAttributes(JoinRHSChildID, RHSChildOutputAttrs), OutputAttrsIn),

    %**** use those attributes to partition all of the predicates in the selection into three subsets: those that can be
    %**** pushed down to the left, those that can be pushed to the right, and those that need to be inside of the join because
    %**** they deal with attributes from both the LHS and the RHS of the join
    partitionPredsOverJoin(SelectionPreds, [], TopSelPredsOut, [], JoinPredsOut, [], LHSSelPredsOut, [], RHSSelPredsOut, LHSChildOutputAttrs, RHSChildOutputAttrs),

    %**** assign to the join its new predicates
    append(JoinPreds, JoinPredsOut, NewJoinPreds),
    append([join(JoinNodeID, NewJoinPreds, JoinLabel)], IntNodes2, IntNodes3),

    % **** create split operators if needed for the new predicates
    extractAttrsFromPredicates(JoinPredsOut, [], PredReferencedAttrs),
    member(randomAttributes(JoinLHSChildID, LHSJoinRandomAttrs), RandomAttrsIn),
    member(randomAttributes(JoinRHSChildID, RHSJoinRandomAttrs), RandomAttrsIn),
    append(LHSJoinRandomAttrs, RHSJoinRandomAttrs, JoinRandomAttrs),
    intersectSets(JoinRandomAttrs, PredReferencedAttrs, PredRandomAttrs),
    intersectSets(PredRandomAttrs, LHSChildOutputAttrs, PredLHSChildAttrs),
    intersectSets(PredRandomAttrs, RHSChildOutputAttrs, PredRHSChildAttrs),
    (PredLHSChildAttrs = [] ->
        IntNodes4 = IntNodes3,
        IntGraph3 = GraphIn,
        LHSNodeID = JoinNodeID
        ;
        gensym(splitNode, LHSSplitNodeID),
        append([split(LHSSplitNodeID, PredLHSChildAttrs)], IntNodes3, IntNodes4),
        select(parent(JoinNodeID, JoinLHSChildID), GraphIn, IntGraph1),
        append([parent(JoinNodeID, LHSSplitNodeID)], IntGraph1, IntGraph2),
        append([parent(LHSSplitNodeID, JoinLHSChildID)], IntGraph2, IntGraph3),
        LHSNodeID = LHSSplitNodeID
    ),

    (PredRHSChildAttrs = [] ->
        IntNodes5 = IntNodes4,
        IntGraph6 = IntGraph3,
        RHSNodeID = JoinNodeID
        ;
        gensym(splitNode, RHSSplitNodeID),
        append([split(RHSSplitNodeID, PredRHSChildAttrs)], IntNodes4, IntNodes5),
        select(parent(JoinNodeID, JoinRHSChildID), IntGraph3, IntGraph4),
        append([parent(JoinNodeID, RHSSplitNodeID)], IntGraph4, IntGraph5),
        append([parent(RHSSplitNodeID, JoinRHSChildID)], IntGraph5, IntGraph6),
        RHSNodeID = RHSSplitNodeID
    ),

    (TopSelPredsOut = [] ->
        % **** remove top selection node from plan
        select(parent(SelectionNodeID, JoinNodeID), IntGraph6, IntGraph7),
        % **** find parents of selection node
        setof(parent(SelectionParentID, SelectionNodeID),
               member(parent(SelectionParentID, SelectionNodeID), IntGraph7), SelectionNodeParents),
        % **** and make them the parents of the join node
        swapNodeParents(SelectionNodeParents, SelectionNodeID, JoinNodeID, IntGraph7, IntGraph8, IntNodes5, IntNodes6),

        % **** delete selection node info from the plan
        select(outputAttributes(SelectionNodeID, _), OutputAttrsIn, IntOutputAttrs1),
        select(randomAttributes(SelectionNodeID, _), RandomAttrsIn, IntRandomAttrs1),
        select(seedAttributes(SelectionNodeID, _), SeedAttrsIn, IntSeedAttrs1),
        deleteCandidateKeyInformation(SelectionNodeID, KeysIn, IntKeys),

        % **** update outer relation info if necessary
        findall(outerRelation(VGWrapperNodeID, SelectionNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SelectionOuterRelations),
        findall(outerRelation(VGWrapperNodeID, JoinNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), JoinOuterRelations),
        subtractSet(OuterRelationsIn, SelectionOuterRelations, IntOuterRelations),
        mergeSets(IntOuterRelations, JoinOuterRelations, IntOuterRelationsOut)
        ;
        % **** retain top selection node
        append([selection(SelectionNodeID, TopSelPredsOut)], IntNodes5, IntNodes6),
        IntGraph8 = IntGraph6, IntOutputAttrs1 = OutputAttrsIn, IntRandomAttrs1 = RandomAttrsIn,
        IntOuterRelationsOut = OuterRelationsIn, IntKeys = KeysIn, IntSeedAttrs1 = SeedAttrsIn
    ),

    %**** add new selection operations on the LHS and the RHS of the join, if needed
    (LHSSelPredsOut = [] ->
        IntNodes7 = IntNodes6,
        IntGraph11 = IntGraph8,
        IntOutputAttrs2 = IntOutputAttrs1,
        IntRandomAttrs2 = IntRandomAttrs1,
        IntSeedAttrs2 = IntSeedAttrs1
        ;
        gensym(selectionNode, LHSSelectionNodeID),
        append([selection(LHSSelectionNodeID, LHSSelPredsOut)], IntNodes6, IntNodes7),
        select(parent(LHSNodeID, JoinLHSChildID), IntGraph8, IntGraph9),
        append([parent(LHSNodeID, LHSSelectionNodeID)], IntGraph9, IntGraph10),
        append([parent(LHSSelectionNodeID, JoinLHSChildID)], IntGraph10, IntGraph11),

        % **** insert output and random attrs
        member(outputAttributes(JoinLHSChildID, LHSNodeChildOutputAttrs), IntOutputAttrs1),
        member(randomAttributes(JoinLHSChildID, LHSNodeChildRandomAttrs), IntRandomAttrs1),
        (member(isPres, LHSNodeChildOutputAttrs) ->
            LHSSelectionOutputAttrs = LHSNodeChildOutputAttrs,
            LHSSelectionRandomAttrs = LHSNodeChildRandomAttrs
            ;
            extractAttrsFromPredicates(LHSSelPredsOut, [], LHSPredReferencedAttrs),
            intersectSets(LHSNodeChildRandomAttrs, LHSPredReferencedAttrs, LHSPredRandomAttrs),
            (LHSPredRandomAttrs = [] ->
                LHSSelectionOutputAttrs = LHSNodeChildOutputAttrs,
                LHSSelectionRandomAttrs = LHSNodeChildRandomAttrs
                ;
                append([isPres], LHSNodeChildOutputAttrs, LHSSelectionOutputAttrs),
                append([isPres], LHSNodeChildRandomAttrs, LHSSelectionRandomAttrs))
        ),
        append([outputAttributes(LHSSelectionNodeID, LHSSelectionOutputAttrs)], IntOutputAttrs1, IntOutputAttrs2),
        append([randomAttributes(LHSSelectionNodeID, LHSSelectionRandomAttrs)], IntRandomAttrs1, IntRandomAttrs2),

        % **** insert seed attrs
        member(seedAttributes(JoinLHSChildID, LHSNodeChildSeedAttrs), IntSeedAttrs1),
        append([seedAttributes(LHSSelectionNodeID, LHSNodeChildSeedAttrs)], IntSeedAttrs1, IntSeedAttrs2)
    ),

    (RHSSelPredsOut = [] ->
        IntNodesOut = IntNodes7,
        IntGraphOut = IntGraph11,
        IntOutputAttrs3 = IntOutputAttrs2,
        IntRandomAttrs3 = IntRandomAttrs2,
        IntSeedAttrs3 = IntSeedAttrs2
        ;
        gensym(selectionNode, RHSSelectionNodeID),
        append([selection(RHSSelectionNodeID, RHSSelPredsOut)], IntNodes7, IntNodesOut),
        select(parent(RHSNodeID, JoinRHSChildID), IntGraph11, IntGraph12),
        append([parent(RHSNodeID, RHSSelectionNodeID)], IntGraph12, IntGraph13),
        append([parent(RHSSelectionNodeID, JoinRHSChildID)], IntGraph13, IntGraphOut),

        % **** insert output and random attrs
        member(outputAttributes(JoinRHSChildID, RHSNodeChildOutputAttrs), IntOutputAttrs2),
        member(randomAttributes(JoinRHSChildID, RHSNodeChildRandomAttrs), IntRandomAttrs2),
        (member(isPres, RHSNodeChildOutputAttrs) ->
            RHSSelectionOutputAttrs = RHSNodeChildOutputAttrs,
            RHSSelectionRandomAttrs = RHSNodeChildRandomAttrs
            ;
            extractAttrsFromPredicates(RHSSelPredsOut, [], RHSPredReferencedAttrs),
            intersectSets(RHSNodeChildRandomAttrs, RHSPredReferencedAttrs, RHSPredRandomAttrs),
            (RHSPredRandomAttrs = [] ->
                RHSSelectionOutputAttrs = RHSNodeChildOutputAttrs,
                RHSSelectionRandomAttrs = RHSNodeChildRandomAttrs
                ;
                append([isPres], RHSNodeChildOutputAttrs, RHSSelectionOutputAttrs),
                append([isPres], RHSNodeChildRandomAttrs, RHSSelectionRandomAttrs))
        ),
        append([outputAttributes(RHSSelectionNodeID, RHSSelectionOutputAttrs)], IntOutputAttrs2, IntOutputAttrs3),
        append([randomAttributes(RHSSelectionNodeID, RHSSelectionRandomAttrs)], IntRandomAttrs2, IntRandomAttrs3),

        % **** insert seed attrs
        member(seedAttributes(JoinRHSChildID, RHSNodeChildSeedAttrs), IntSeedAttrs2),
        append([seedAttributes(RHSSelectionNodeID, RHSNodeChildSeedAttrs)], IntSeedAttrs2, IntSeedAttrs3)
    ),

    % **** insert output random and seed attributes for split nodes
    (PredLHSChildAttrs = [] ->
        IntOutputAttrs4 = IntOutputAttrs3,
        IntRandomAttrs4 = IntRandomAttrs3,
        IntSeedAttrs4 = IntSeedAttrs3
        ;
        member(parent(LHSSplitNodeID, LHSSplitChildID), IntGraphOut),
        member(outputAttributes(LHSSplitChildID, LHSSplitChildOutputAttrs), IntOutputAttrs3),
        member(randomAttributes(LHSSplitChildID, LHSSplitChildRandomAttrs), IntRandomAttrs3),
        (member(isPres, LHSSplitChildOutputAttrs) ->
            LSHSplitOutputAttrs = LHSSplitChildOutputAttrs,
            IntLSHSplitRandomAttrs = LHSSplitChildRandomAttrs
            ;
            append([isPres], LHSSplitChildOutputAttrs, LSHSplitOutputAttrs),
            append([isPres], LHSSplitChildRandomAttrs, IntLSHSplitRandomAttrs)
        ),
        append([outputAttributes(LHSSplitNodeID, LSHSplitOutputAttrs)], IntOutputAttrs3, IntOutputAttrs4),
        subtractSet(IntLSHSplitRandomAttrs, PredLHSChildAttrs, LSHSplitRandomAttrs),
        append([randomAttributes(LHSSplitNodeID, LSHSplitRandomAttrs)], IntRandomAttrs3, IntRandomAttrs4),

        % **** insert seed attrs
        member(seedAttributes(LHSSplitChildID, LHSSplitChildSeedAttrs), IntSeedAttrs3),
        append([seedAttributes(LHSSplitNodeID, LHSSplitChildSeedAttrs)], IntSeedAttrs3, IntSeedAttrs4)
    ),

    (PredRHSChildAttrs = [] ->
        IntOutputAttrs5 = IntOutputAttrs4,
        IntRandomAttrs5 = IntRandomAttrs4,
        IntSeedAttrsOut = IntSeedAttrs4
        ;
        member(parent(RHSSplitNodeID, RHSSplitChildID), IntGraphOut),
        member(outputAttributes(RHSSplitChildID, RHSSplitChildOutputAttrs), IntOutputAttrs4),
        member(randomAttributes(RHSSplitChildID, RHSSplitChildRandomAttrs), IntRandomAttrs4),
        (member(isPres, RHSSplitChildOutputAttrs) ->
            RSHSplitOutputAttrs = RHSSplitChildOutputAttrs,
            IntRSHSplitRandomAttrs = RHSSplitChildRandomAttrs
            ;
            append([isPres], RHSSplitChildOutputAttrs, RSHSplitOutputAttrs),
            append([isPres], RHSSplitChildRandomAttrs, IntRSHSplitRandomAttrs)
        ),
        append([outputAttributes(RHSSplitNodeID, RSHSplitOutputAttrs)], IntOutputAttrs4, IntOutputAttrs5),
        subtractSet(IntRSHSplitRandomAttrs, PredRHSChildAttrs, RSHSplitRandomAttrs),
        append([randomAttributes(RHSSplitNodeID, RSHSplitRandomAttrs)], IntRandomAttrs4, IntRandomAttrs5),

        % **** insert seed attrs
        member(seedAttributes(RHSSplitChildID, RHSSplitChildSeedAttrs), IntSeedAttrs4),
        append([seedAttributes(RHSSplitNodeID, RHSSplitChildSeedAttrs)], IntSeedAttrs4, IntSeedAttrsOut)
    ),

    % **** update output and random attributes for join
    member(parent(JoinNodeID, NewJoinLHSChildID), IntGraphOut),
    member(parent(JoinNodeID, NewJoinRHSChildID), IntGraphOut),
    %**** it is the same transformation if we switch the LHS and RHS
    NewJoinLHSChildID @> NewJoinRHSChildID,

    select(outputAttributes(JoinNodeID, _), IntOutputAttrs5, IntOutputAttrs6),
    select(randomAttributes(JoinNodeID, _), IntRandomAttrs5, IntRandomAttrs6),

    member(randomAttributes(NewJoinLHSChildID, NewJoinLHSChildRandomAttrs), IntRandomAttrs6),
    member(randomAttributes(NewJoinRHSChildID, NewJoinRHSChildRandomAttrs), IntRandomAttrs6),
    mergeSets(NewJoinLHSChildRandomAttrs, NewJoinRHSChildRandomAttrs, IntJoinRandomAttrs),

    member(outputAttributes(NewJoinLHSChildID, NewJoinLHSChildOutputAttrs), IntOutputAttrs6),
    member(outputAttributes(NewJoinRHSChildID, NewJoinRHSChildOutputAttrs), IntOutputAttrs6),
    append(NewJoinLHSChildOutputAttrs, NewJoinRHSChildOutputAttrs, IntJoinOutputAttrs),

    (member(isPres, IntJoinOutputAttrs) ->
        NewJoinOutputAttrs = IntJoinOutputAttrs,
        NewJoinRandomAttrs = IntJoinRandomAttrs;

        extractAttrsFromPredicates(NewJoinPreds, [], JoinPredReferencedAttrs),
        intersectSets(JoinPredReferencedAttrs, IntJoinRandomAttrs, JoinPredRandomAttrs),
        (JoinPredRandomAttrs = [] ->
            NewJoinOutputAttrs = IntJoinOutputAttrs,
            NewJoinRandomAttrs = IntJoinRandomAttrs
            ;
            append([isPres], IntJoinOutputAttrs, NewJoinOutputAttrs),
            append([isPres], IntJoinRandomAttrs, NewJoinRandomAttrs)
        )
    ),
    append([outputAttributes(JoinNodeID, NewJoinOutputAttrs)], IntOutputAttrs6, IntOutputAttrs7),
    append([randomAttributes(JoinNodeID, NewJoinRandomAttrs)], IntRandomAttrs6, IntRandomAttrs7),

    % **** update output and random attribute for top selection node
    (TopSelPredsOut = [] ->
        IntOutputAttrsOut = IntOutputAttrs7,
        IntRandomAttrsOut = IntRandomAttrs7
        ;
        select(outputAttributes(SelectionNodeID, _), IntOutputAttrs7, IntOutputAttrs8),
        select(randomAttributes(SelectionNodeID, _), IntRandomAttrs7, IntRandomAttrs8),
        (member(isPres, NewJoinOutputAttrs) ->
            TopSelectionOutputAttrs = NewJoinOutputAttrs,
            TopSelectionRandomAttrs = NewJoinRandomAttrs
            ;
            extractAttrsFromPredicates(TopSelPredsOut, [], TopSelReferencedAttrs),
            intersectSets(NewJoinRandomAttrs, TopSelReferencedAttrs, TopSelPredRandomAttrs),
            (TopSelPredRandomAttrs = [] ->
                TopSelectionOutputAttrs = NewJoinOutputAttrs,
                TopSelectionRandomAttrs = NewJoinRandomAttrs
                ;
                append([isPres], NewJoinOutputAttrs, TopSelectionOutputAttrs),
                append([isPres], NewJoinRandomAttrs, TopSelectionRandomAttrs))
        ),
        append([outputAttributes(SelectionNodeID, TopSelectionOutputAttrs)], IntOutputAttrs8, IntOutputAttrsOut),
        append([randomAttributes(SelectionNodeID, TopSelectionRandomAttrs)], IntRandomAttrs8, IntRandomAttrsOut)
    ),

    % **** update output/seed/random attributes from the plan
    identifyOutputAttributes(IntGraphOut, IntIntGraphOut, IntNodesOut, TmpNodes1, TmpOutputAttrs1, TmpRandomAttrs1, IntOuterRelationsOut),
    findall(OuterSeedAttr, member(seed(_, OuterSeedAttr), TmpNodes1), OuterSeedAttrs),
    findall(VGWrapperSeedAttr, member(vgwrapper(_, _, _, _, _, _, _, _, VGWrapperSeedAttr), TmpNodes1), VGWrapperSeedAttrs),
    append(OuterSeedAttrs, VGWrapperSeedAttrs, PlanSeedAttrs),
    findall(join(TJoinID, TJoinPreds, TJoinType), member(join(TJoinID, TJoinPreds, TJoinType), TmpNodes1), Joins),
    findall(selection(TSelectionID, TSelectionPreds), member(selection(TSelectionID, TSelectionPreds), TmpNodes1), Selections),
    eliminateSeedAttributesFromPlan(PlanSeedAttrs, IntIntGraphOut, TmpGraph, TmpNodes1, TmpNodes2, Joins, Selections,
                          TmpOutputAttrs1, TmpOutputAttrs2, TmpRandomAttrs1, TmpRandomAttrs2, TmpSeedAttrs, IntOuterRelationsOut),
    updateOutputAttributes(TmpGraph, GraphOut, TmpNodes2, TmpNodesOut, TmpOutputAttrs2, OutputAttrsOut,
                          TmpRandomAttrs2, RandomAttrsOut, TmpSeedAttrs, SeedAttrsOut, IntOuterRelationsOut, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), TmpNodesOut), member(candidateKey(NodeName, CandidateKey), IntKeys)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushSelectionDownJoin(N)) ->
        retract(appliedTransformation(pushSelectionDownJoin(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSelectionDownJoin(NN)))
        ;
        assert(appliedTransformation(pushSelectionDownJoin(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN PROJECTIONS'S SCALAR FUNCTION CHILDREN PAST A JOIN
% Returns true if (GraphOut, NodesOut) can legally result from pushing projection's scalar function past a join
% in the query plan represented by (GraphIn, NodesIn)

% Just a projection with one scalar function as a test
% Written by Shangyu
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushProjectionFuncDownJoin),

    %**** find some projection and its scalar function child that are above some join
    % select(projection(ProjectionNodeID, ProjectionAttrs), NodesIn, IntNodes1),
    member(projection(ProjectionNodeID, ProjectionAttrs), NodesIn),
    member(parent(ProjectionNodeID, ScalarFunctionNodeID), GraphIn),
    member(parent(ScalarFunctionNodeID, JoinNodeID), GraphIn),
    % select(join(JoinNodeID, JoinPreds, JoinLabel), IntNodes1, IntNodes2),
    member(join(JoinNodeID, JoinPreds, JoinLabel), NodesIn),
    select(scalarfunc(ScalarFunctionNodeID, FuncExprs, FuncExprsInAttrs, FuncExprsOutAttrs), NodesIn, IntNodes3),

    %**** rule applies only to nodes with one parent
    nodeHasSingleParent(JoinNodeID, GraphIn),

    %**** find the two children of the join
    member(parent(JoinNodeID, JoinLHSChildID), GraphIn),
    member(parent(JoinNodeID, JoinRHSChildID), GraphIn),
    %**** it is the same transformation if we switch the LHS and RHS
    JoinLHSChildID @> JoinRHSChildID,

    %**** find all attrs that are reachable from either the LHS or the RHS of the join
    member(outputAttributes(JoinLHSChildID, LHSChildOutputAttrs), OutputAttrsIn),
    member(outputAttributes(JoinRHSChildID, RHSChildOutputAttrs), OutputAttrsIn),

    %**** partition all scalar functions into three subsets: those that can be
    %**** pushed down to the left, those that can be pushed to the right, and those that need to be inside of the join because
    %**** they deal with attributes from both the LHS and the RHS of the join
    %**** currently just deal with one scalar function
    partitionScalarFuncOverJoin(ScalarFunctionNodeID, LHSChildOutputAttrs, RHSChildOutputAttrs, POS),

    %**** if the scalar function containes the attributes from both join sides, cannot apply this transformation
    %**** else, remove the original scalar function
    (POS == 3 -> false;
        % **** remove scalar function node from plan
        member(parent(ProjectionNodeID, ScalarFunctionNodeID), GraphIn),
        select(parent(ScalarFunctionNodeID, JoinNodeID), GraphIn, IntGraph1),
        % **** and make the projection node to be the parents of the join node
	swapNodeParents([parent(ProjectionNodeID, ScalarFunctionNodeID)], ScalarFunctionNodeID, JoinNodeID, IntGraph1, IntGraph2, IntNodes3, IntNodes4),

        % **** delete selection node info from the plan
        % select(outputAttributes(ScalarFunctionNodeID, _), OutputAttrsIn, IntOutputAttrs1),
        % select(randomAttributes(ScalarFunctionNodeID, _), RandomAttrsIn, IntRandomAttrs1),
        % select(seedAttributes(ScalarFunctionNodeID, _), SeedAttrsIn, IntSeedAttrs1),
        deleteCandidateKeyInformation(ScalarFunctionNodeID, KeysIn, IntKeys),

        % **** update outer relation info if necessary
        findall(outerRelation(VGWrapperNodeID, ScalarFunctionNodeID), member(outerRelation(VGWrapperNodeID, ScalarFunctionNodeID), OuterRelationsIn), ScalarOuterRelations),
        findall(outerRelation(VGWrapperNodeID, JoinNodeID), member(outerRelation(VGWrapperNodeID, ScalarFunctionNodeID), OuterRelationsIn), JoinOuterRelations),
        subtractSet(OuterRelationsIn, ScalarOuterRelations, IntOuterRelations),
        mergeSets(IntOuterRelations, JoinOuterRelations, IntOuterRelationsOut)
    ),

    %**** add new scalar function operations on the LHS and the RHS of the join, if needed
    (POS == 1 ->
        % **** for left
        gensym(scalarFuncNode, LHSScalarFuncNodeID),
        append([scalarfunc(LHSScalarFuncNodeID, FuncExprs, FuncExprsInAttrs, FuncExprsOutAttrs)], IntNodes4, IntNodesOut),
        select(parent(LHSNodeID, JoinLHSChildID), IntGraph2, IntGraph3),
        append([parent(LHSNodeID, LHSScalarFuncNodeID)], IntGraph3, IntGraph4),
        append([parent(LHSScalarFuncNodeID, JoinLHSChildID)], IntGraph4, IntGraphOut);

	/* The identifyOutputAttributes rule will deal with this part
        % **** insert output and random attrs

        member(outputAttributes(JoinLHSChildID, LHSNodeChildOutputAttrs), IntOutputAttrs1),
        member(randomAttributes(JoinLHSChildID, LHSNodeChildRandomAttrs), IntRandomAttrs1),
        (member(isPres, LHSNodeChildOutputAttrs) ->
            LHSScalarFuncOutputAttrs = LHSNodeChildOutputAttrs,
            LHSScalarFuncRandomAttrs = LHSNodeChildRandomAttrs
            ;
            intersectSets(LHSNodeChildRandomAttrs, FuncExprsInAttrs, LHSScalarFuncRandomAttrs),
            (LHSScalarFuncRandomAttrs = [] ->
                LHSScalarFuncOutputAttrs = LHSNodeChildOutputAttrs,
                LHSScalarFuncRandomAttrs = LHSNodeChildRandomAttrs
                ;
                append([isPres], LHSNodeChildOutputAttrs, LHSScalarFuncOutputAttrs),
                append([isPres], LHSNodeChildRandomAttrs, LHSScalarFuncRandomAttrs))
        ),
        mergeSets(FuncExprsOutAttrs, LHSScalarFuncOutputAttrs, LHSScalarFuncFinalOutputAttrs),
        append([outputAttributes(LHSScalarFuncNodeID, LHSScalarFuncFinalOutputAttrs)], IntOutputAttrs1, IntOutputAttrs2),
        append([randomAttributes(LHSScalarFuncNodeID, LHSScalarFuncRandomAttrs)], IntRandomAttrs1, IntRandomAttrs2),

        % **** insert seed attrs
        member(seedAttributes(JoinLHSChildID, LHSNodeChildSeedAttrs), IntSeedAttrs1),
        append([seedAttributes(LHSScalarFuncNodeID, LHSNodeChildSeedAttrs)], IntSeedAttrs1, IntSeedAttrsOut);
        */
        
        % **** for right
	gensym(scalarFuncNode, RHSScalarFuncNodeID),
        append([scalarfunc(RHSScalarFuncNodeID, FuncExprs, FuncExprsInAttrs, FuncExprsOutAttrs)], IntNodes4, IntNodesOut),
        select(parent(RHSNodeID, JoinRHSChildID), IntGraph2, IntGraph3),
        append([parent(RHSNodeID, RHSScalarFuncNodeID)], IntGraph3, IntGraph4),
        append([parent(RHSScalarFuncNodeID, JoinRHSChildID)], IntGraph4, IntGraphOut)

	/* The identifyOutputAttributes rule will deal with this part
	
        % **** insert output and random attrs
        member(outputAttributes(JoinRHSChildID, RHSNodeChildOutputAttrs), IntOutputAttrs1),
        member(randomAttributes(JoinRHSChildID, RHSNodeChildRandomAttrs), IntRandomAttrs1),
        (member(isPres, RHSNodeChildOutputAttrs) ->
            RHSScalarFuncOutputAttrs = RHSNodeChildOutputAttrs,
            RHSScalarFuncRandomAttrs = RHSNodeChildRandomAttrs
            ;
            intersectSets(RHSNodeChildRandomAttrs, FuncExprsInAttrs, RHSScalarFuncRandomAttrs),
            (RHSScalarFuncRandomAttrs = [] ->
                RHSScalarFuncOutputAttrs = RHSNodeChildOutputAttrs,
                RHSScalarFuncRandomAttrs = RHSNodeChildRandomAttrs
                ;
                append([isPres], RHSNodeChildOutputAttrs, RHSScalarFuncOutputAttrs),
                append([isPres], RHSNodeChildRandomAttrs, RHSScalarFuncRandomAttrs))
        ),
        mergeSets(FuncExprsOutAttrs, RHSScalarFuncOutputAttrs, RHSScalarFuncFinalOutputAttrs),
        append([outputAttributes(RHSScalarFuncNodeID, RHSScalarFuncFinalOutputAttrs)], IntOutputAttrs1, IntOutputAttrs2),
        append([randomAttributes(RHSScalarFuncNodeID, RHSScalarFuncRandomAttrs)], IntRandomAttrs1, IntRandomAttrs2),

        % **** insert seed attrs
        member(seedAttributes(JoinRHSChildID, RHSNodeChildSeedAttrs), IntSeedAttrs1),
        append([seedAttributes(RHSScalarFuncNodeID, RHSNodeChildSeedAttrs)], IntSeedAttrs1, IntSeedAttrsOut)
        */
    ),

    /* The identifyOutputAttributes rule will deal with this part

    % **** update output and random attributes for join
    member(parent(JoinNodeID, NewJoinLHSChildID), IntGraphOut),
    member(parent(JoinNodeID, NewJoinRHSChildID), IntGraphOut),
    %**** it is the same transformation if we switch the LHS and RHS
    NewJoinLHSChildID @> NewJoinRHSChildID,

    select(outputAttributes(JoinNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(randomAttributes(JoinNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    
    member(randomAttributes(NewJoinLHSChildID, NewJoinLHSChildRandomAttrs), IntRandomAttrs2),
    member(randomAttributes(NewJoinRHSChildID, NewJoinRHSChildRandomAttrs), IntRandomAttrs2),
    mergeSets(NewJoinLHSChildRandomAttrs, NewJoinRHSChildRandomAttrs, IntJoinRandomAttrs),

    member(outputAttributes(NewJoinLHSChildID, NewJoinLHSChildOutputAttrs), IntOutputAttrs2),
    member(outputAttributes(NewJoinRHSChildID, NewJoinRHSChildOutputAttrs), IntOutputAttrs2),
    append(NewJoinLHSChildOutputAttrs, NewJoinRHSChildOutputAttrs, IntJoinOutputAttrs),

    (member(isPres, IntJoinOutputAttrs) ->
        NewJoinOutputAttrs = IntJoinOutputAttrs,
        NewJoinRandomAttrs = IntJoinRandomAttrs;

        extractAttrsFromPredicates(JoinPreds, [], JoinPredReferencedAttrs),
        intersectSets(JoinPredReferencedAttrs, IntJoinRandomAttrs, JoinPredRandomAttrs),
        (JoinPredRandomAttrs = [] ->
            NewJoinOutputAttrs = IntJoinOutputAttrs,
            NewJoinRandomAttrs = IntJoinRandomAttrs
            ;
            append([isPres], IntJoinOutputAttrs, NewJoinOutputAttrs),
            append([isPres], IntJoinRandomAttrs, NewJoinRandomAttrs)
        )
    ),
    append([outputAttributes(JoinNodeID, NewJoinOutputAttrs)], IntOutputAttrs2, IntOutputAttrsOut),
    append([randomAttributes(JoinNodeID, NewJoinRandomAttrs)], IntRandomAttrs2, IntRandomAttrsOut),
    */

    % **** update output/seed/random attributes from the plan
    % this function will identify and modify output attributes for all nodes, not efficient!
    identifyOutputAttributes(IntGraphOut, IntIntGraphOut, IntNodesOut, TmpNodes1, TmpOutputAttrs1, TmpRandomAttrs1, IntOuterRelationsOut),
    findall(OuterSeedAttr, member(seed(_, OuterSeedAttr), TmpNodes1), OuterSeedAttrs),
    findall(VGWrapperSeedAttr, member(vgwrapper(_, _, _, _, _, _, _, _, VGWrapperSeedAttr), TmpNodes1), VGWrapperSeedAttrs),
    append(OuterSeedAttrs, VGWrapperSeedAttrs, PlanSeedAttrs),
    findall(join(TJoinID, TJoinPreds, TJoinType), member(join(TJoinID, TJoinPreds, TJoinType), TmpNodes1), Joins),
    findall(selection(TSelectionID, TSelectionPreds), member(selection(TSelectionID, TSelectionPreds), TmpNodes1), Selections),
    eliminateSeedAttributesFromPlan(PlanSeedAttrs, IntIntGraphOut, TmpGraph, TmpNodes1, TmpNodes2, Joins, Selections,
                          TmpOutputAttrs1, TmpOutputAttrs2, TmpRandomAttrs1, TmpRandomAttrs2, TmpSeedAttrs, IntOuterRelationsOut),
    updateOutputAttributes(TmpGraph, GraphOut, TmpNodes2, TmpNodesOut, TmpOutputAttrs2, OutputAttrsOut,
                          TmpRandomAttrs2, RandomAttrsOut, TmpSeedAttrs, SeedAttrsOut, IntOuterRelationsOut, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), TmpNodesOut), member(candidateKey(NodeName, CandidateKey), IntKeys)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushProjectionFuncDownJoin(N)) ->
        retract(appliedTransformation(pushProjectionFuncDownJoin(N))),
        NN is N + 1,
        assert(appliedTransformation(pushProjectionFuncDownJoin(NN)))
        ;
        assert(appliedTransformation(pushProjectionFuncDownJoin(1)))).        
        
%*****************************************************************************************************************************
% RULE THAT CHOOSES A MATERIALIZED VIEW AND CHECKS WHETHER IT CAN BE APPLIED IN THE PLAN
% Returns true if (GraphOut, NodesOut) can legally result by applying a materialized view in the query plan represented
% by (GraphIn, NodesIn)
% **** version that deals with the case where a single VGWrapper exists in the branch under consideration
transform(GraphIn, NodesIn, _, KeysIn, _, _, _,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(composeMaterializedView),
    
    % **** rule applies if materialized views exist
    current_predicate(materializedView/1),
    
    % **** pick one view
    materializedView(ViewName),
    mvGraph(ViewName, MVGraph),
    mvNodes(ViewName, MVNodes),
    (current_predicate(mvPredicates/2) -> mvPredicates(ViewName, MVPreds); MVPreds = []),
    (current_predicate(mvArithmeticExpressions/2) -> mvArithmeticExpressions(ViewName, MVArithmExprs); MVArithmExprs = []),
    (current_predicate(mvAggregateExpressions/2) -> mvAggregateExpressions(ViewName, MVAggExprs); MVAggExprs = []),
    
    % **** and check if it can be applied in the plan
    member(parent(ViewName, ViewRootNode), MVGraph),
    findEquivalentSubPlan(ViewRootNode, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, MaterializedRootNode, GraphIn, NodesIn, [], MaterializedNodes),
    % **** check that NO nodes outside - except root parents - of the selected branch have edges pointing to a node in the branch
    findall(NodeID, (member(parent(NodeID, _), GraphIn), not(member(NodeID, MaterializedNodes))), IntNonMaterializedParents1),
    select(planRoot, IntNonMaterializedParents1, IntNonMaterializedParents2),
    removeDuplicates(IntNonMaterializedParents2, [], NonMaterializedParents),
    findall(parent(NodeID1, NodeID2), (member(parent(NodeID1, NodeID2), GraphIn), member(NodeID1, NonMaterializedParents), member(NodeID2, MaterializedNodes)), ExternalEdges),
    setof(parent(MaterializedRootNodeParent, MaterializedRootNode),
         member(parent(MaterializedRootNodeParent, MaterializedRootNode), GraphIn), MaterializedRootNodeParents),
    equalSets(ExternalEdges, MaterializedRootNodeParents),
    
    % **** update structure and metadata of plan to reflect reading from a materialized view
    mvStats(ViewName, MVStats),
    addToGlogalMemory(MVStats),
    mvAttributeTypes(ViewName, MVAttrTypes),
    addToGlogalMemory(MVAttrTypes),
    mvRelations(ViewName, MVRelations),
    addToGlogalMemory(MVRelations),
    mvViewScan(ViewName, tablescan(NodeName, RelationName, TableScanOutAttrs)),

    deleteMaterializedNodesFromPlan(MaterializedNodes, GraphIn, IntGraph1, NodesIn, IntNodes1),
    append([tablescan(NodeName, RelationName, TableScanOutAttrs)], IntNodes1, IntNodes2),
    addMaterializedNodeParents(NodeName, MaterializedRootNodeParents, IntGraph1, IntGraph2),
    
    % **** update output/seed/random attributes from the plan
    identifyOutputAttributes(IntGraph2, IntGraph3, IntNodes2, IntNodes3, IntOutputAttrs1, IntRandomAttrs1, IntOuterRelations),
    findall(OuterSeedAttr, member(seed(_, OuterSeedAttr), IntNodes3), OuterSeedAttrs),
    findall(VGWrapperSeedAttr, member(vgwrapper(_, _, _, _, _, _, _, _, VGWrapperSeedAttr), IntNodes3), VGWrapperSeedAttrs),
    append(OuterSeedAttrs, VGWrapperSeedAttrs, PlanSeedAttrs),
    findall(join(JoinID, JoinPreds, JoinType), member(join(JoinID, JoinPreds, JoinType), IntNodes3), Joins),
    findall(selection(SelectionID, SelectionPreds), member(selection(SelectionID, SelectionPreds), IntNodes3), Selections),
    eliminateSeedAttributesFromPlan(PlanSeedAttrs, IntGraph3, IntGraph4, IntNodes3, IntNodes4, Joins, Selections,
                          IntOutputAttrs1, IntOutputAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntSeedAttrs, IntOuterRelations),
    updateOutputAttributes(IntGraph4, GraphOut, IntNodes4, IntNodesOut, IntOutputAttrs2, OutputAttrsOut,
                          IntRandomAttrs2, RandomAttrsOut, IntSeedAttrs, SeedAttrsOut, IntOuterRelations, OuterRelationsOut),

    % **** re-compute keys for plan
    findall(candidateKey(TableNodeName, TableCandKey), (member(tablescan(TableNodeName, _, _), IntNodesOut),
                                           member(candidateKey(TableNodeName, TableCandKey), KeysIn)), IntPrelimKeys1),
    mvCandidateKeys(ViewName, IntPrelimKeys2),
    mergeSets(IntPrelimKeys1, IntPrelimKeys2, PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),
    
    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),
    
    (appliedTransformation(composeMaterializedView(N)) ->
        retract(appliedTransformation(composeMaterializedView(N))),
        NN is N + 1,
        assert(appliedTransformation(composeMaterializedView(NN)))
        ;
        assert(appliedTransformation(composeMaterializedView(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING A VGWRAPPER OPERATOR IN UPPER LEVELS IN THE PLAN SO AS TO AVOID MOVING REDUNDANT ATTRIBUTES
% Returns true if (GraphOut, NodesOut) can legally result from pushing up a vgwrapper in the query plan represented
% by (GraphIn, NodesIn)
% **** version that deals with the case where a single VGWrapper exists in the branch under consideration
transform(GraphIn, NodesIn, _, KeysIn, OuterRelationsIn, _, _,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushGroupedVGWrapperUpInPlan),

    % **** find a vgwrapper operator
    member(vgwrapper(VGWrapperNodeID, _, _, SeedAttrIn, _, _, _, VGOutAttrs, SeedAttrOut), NodesIn),

    % **** as well as its immediate join ancestor
    select(parent(VGWrapperParent, VGWrapperNodeID), GraphIn, IntGraph1),
    member(join(VGWrapperParent, _, _), NodesIn),

    % **** find the top seed join pertaining to that VGWrapper
    findTopLevelSeedJoin(VGWrapperNodeID, TopLevelSeedJoin, GraphIn, NodesIn, SeedAttrIn),

    % **** we are considering a VGWrapper within a group of VGWrappers
    TopLevelSeedJoin \= VGWrapperParent,

    % **** find a path to an ancestor node that needs the output attributes of this VGWrapper
    % **** and collect the attributes from that VGWrapper that are needed along the path
    findVGWrapperDependantAncestorPath(TopLevelSeedJoin, VGOutAttrs, GraphIn, NodesIn, [], NecessaryVGOutputAttrs, [], AncestorNodePath),
    length(AncestorNodePath, ALen),
    ALen > 1,

    % **** no node in the path should need the VGWrapper attributes
    NecessaryVGOutputAttrs = [],

    % **** delete VGWrapper branch
    select(outerRelation(VGWrapperNodeID, VGOuterRelationNode), OuterRelationsIn, IntOuterRelations1),
    not(nodeHasSingleParent(VGOuterRelationNode, IntGraph1)),
    select(parent(VGWrapperNodeID, VGOuterRelationNode), IntGraph1, IntGraph2),
    select(parent(VGWrapperParent, OtherChild), IntGraph2, IntGraph3),
    select(parent(VGWrapperParentParent, VGWrapperParent), IntGraph3, IntGraph4),
    %select(join(VGWrapperParentParent, _, _), NodesIn, IntNodes1),
    append([parent(VGWrapperParentParent, OtherChild)], IntGraph4, IntGraph5),

    % **** and move it in its new position in the plan
    NodeIndex1 is ALen - 2,
    nth0(NodeIndex1, AncestorNodePath, LowerNode),
    NodeIndex2 is ALen - 1,
    nth0(NodeIndex2, AncestorNodePath, UpperNode),
    select(parent(UpperNode, LowerNode), IntGraph5, IntGraph6),

    % **** first create new predicate for join
    member(join(TopLevelSeedJoin, [TopLevelJoinPred], _), NodesIn),
    compExp(TopLevelJoinPred, equals, TopLevelJoinLHSSeedAttr, TopLevelJoinRHSSeedAttr, identifier),
    (member(seed(_, TopLevelJoinLHSSeedAttr), NodesIn) -> OuterSeedAttr = TopLevelJoinLHSSeedAttr; OuterSeedAttr = TopLevelJoinRHSSeedAttr),
    gensym(equalsPred, NewJoinPred),
    assert(compExp(NewJoinPred, equals, OuterSeedAttr, SeedAttrOut, identifier)),

    % **** and then a new seed join node
    append([join(VGWrapperParent, [NewJoinPred], _)], NodesIn, IntNodes1),
    append([parent(UpperNode, VGWrapperParent)], IntGraph6, IntGraph7),
    gensym(dedupNode, NewDedupNodeID),
    append([dedup(NewDedupNodeID, [SeedAttrIn])], IntNodes1, IntNodes2),
    %append([parent(VGWrapperNodeID, LowerNode)], IntGraph7, IntGraph8),
    append([parent(VGWrapperNodeID, NewDedupNodeID)], IntGraph7, IntGraph8),
    append([parent(NewDedupNodeID, LowerNode)], IntGraph8, IntGraph9),
    append([parent(VGWrapperParent, LowerNode)], IntGraph9, IntGraph10),
    append([parent(VGWrapperParent, VGWrapperNodeID)], IntGraph10, IntGraph11),
    %append([outerRelation(VGWrapperNodeID, LowerNode)], IntOuterRelations1, IntOuterRelations2),
    append([outerRelation(VGWrapperNodeID, NewDedupNodeID)], IntOuterRelations1, IntOuterRelations2),
     
    % **** update output/seed/random attributes from the plan
    identifyOutputAttributes(IntGraph11, IntGraph12, IntNodes2, IntNodes3, IntOutputAttrs1, IntRandomAttrs1, IntOuterRelations2),
    findall(OuterSeedAttr, member(seed(_, OuterSeedAttr), IntNodes3), OuterSeedAttrs),
    findall(VGWrapperSeedAttr, member(vgwrapper(_, _, _, _, _, _, _, _, VGWrapperSeedAttr), IntNodes3), VGWrapperSeedAttrs),
    append(OuterSeedAttrs, VGWrapperSeedAttrs, PlanSeedAttrs),
    findall(join(JoinID, JoinPreds, JoinType), member(join(JoinID, JoinPreds, JoinType), IntNodes3), Joins),
    findall(selection(SelectionID, SelectionPreds), member(selection(SelectionID, SelectionPreds), IntNodes3), Selections),
    eliminateSeedAttributesFromPlan(PlanSeedAttrs, IntGraph12, IntGraph13, IntNodes3, IntNodes4, Joins, Selections,
                          IntOutputAttrs1, IntOutputAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntSeedAttrs, IntOuterRelations2),
    updateOutputAttributes(IntGraph13, GraphOut, IntNodes4, IntNodesOut, IntOutputAttrs2, OutputAttrsOut,
                          IntRandomAttrs2, RandomAttrsOut, IntSeedAttrs, SeedAttrsOut, IntOuterRelations2, OuterRelationsOut),

    % **** re-compute keys for plan
    findall(candidateKey(TableNodeName, TableCandKey), (member(tablescan(TableNodeName, _, _), IntNodesOut),
                                           member(candidateKey(TableNodeName, TableCandKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushSingleVGWrapperUpInPlan(N)) ->
        retract(appliedTransformation(pushGroupedVGWrapperUpInPlan(N))),
        NN is N + 1,
        assert(appliedTransformation(pushGroupedVGWrapperUpInPlan(NN)))
        ;
        assert(appliedTransformation(pushGroupedVGWrapperUpInPlan(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING A VGWRAPPER OPERATOR IN UPPER LEVELS IN THE PLAN SO AS TO AVOID MOVING REDUNDANT ATTRIBUTES
% Returns true if (GraphOut, NodesOut) can legally result from pushing up a vgwrapper in the query plan represented
% by (GraphIn, NodesIn)
% **** version that deals with the case where a single VGWrapper exists in the branch under consideration
transform(GraphIn, NodesIn, _, KeysIn, OuterRelationsIn, _, _,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSingleVGWrapperUpInPlan),

    % **** find a vgwrapper operator
    member(vgwrapper(VGWrapperNodeID, _, _, SeedAttrIn, _, _, _, VGOutAttrs, _), NodesIn),

    % **** as well as its immediate join ancestor
    select(parent(VGWrapperParent, VGWrapperNodeID), GraphIn, Trash),
    member(join(VGWrapperParent, _, _), NodesIn),

    % **** find the top seed join pertaining to that VGWrapper
    findTopLevelSeedJoin(VGWrapperNodeID, TopLevelSeedJoin, GraphIn, NodesIn, SeedAttrIn),

    % **** we are considering a single VGWrapper
    TopLevelSeedJoin = VGWrapperParent,

    % **** find a path to an ancestor node that needs the output attributes of this VGWrapper and collect the attributes
    % **** from that VGWrapper that are needed along the path
    findVGWrapperDependantAncestorPath(TopLevelSeedJoin, VGOutAttrs, GraphIn, NodesIn, [], NecessaryVGOutputAttrs, [], AncestorNodePath),
    length(AncestorNodePath, ALen),
    ALen > 1,

    % **** no node in the path should need the VGWrapper attributes
    NecessaryVGOutputAttrs = [],

    % ***** find lower operator after the VGWrapper-related plan
    nth0(0, AncestorNodePath, WhichNodeToStartFrom),

    % **** delete VGWrapper branch
    select(outerRelation(VGWrapperNodeID, VGOuterRelationNode), OuterRelationsIn, IntOuterRelations1),
    not(nodeHasSingleParent(VGOuterRelationNode, GraphIn)),
    select(parent(VGWrapperNodeID, VGOuterRelationNode), GraphIn, IntGraph1),
    member(parent(VGWrapperParent, OtherChild), Trash),
    OtherChild = VGOuterRelationNode,
    select(parent(VGWrapperParent, OtherChild), IntGraph1, IntGraph2),

    select(parent(WhichNodeToStartFrom, TopLevelSeedJoin), IntGraph2, IntGraph3),
    append([parent(WhichNodeToStartFrom, OtherChild)], IntGraph3, IntGraph4),

    % **** and move it in its new position in the plan
    NodeIndex1 is ALen - 2,
    nth0(NodeIndex1, AncestorNodePath, LowerNode),
    NodeIndex2 is ALen - 1,
    nth0(NodeIndex2, AncestorNodePath, UpperNode),
    select(parent(UpperNode, LowerNode), IntGraph4, IntGraph5),

    append([parent(UpperNode, VGWrapperParent)], IntGraph5, IntGraph6),
    gensym(dedupNode, NewDedupNodeID),
    append([dedup(NewDedupNodeID, [SeedAttrIn])], NodesIn, IntNodes1),
    append([parent(VGWrapperNodeID, NewDedupNodeID)], IntGraph6, IntGraph7),
    append([parent(NewDedupNodeID, LowerNode)], IntGraph7, IntGraph8),
    append([parent(VGWrapperParent, LowerNode)], IntGraph8, IntGraph9),
    append([outerRelation(VGWrapperNodeID, NewDedupNodeID)], IntOuterRelations1, IntOuterRelations2),

    % **** update output/seed/random attributes from the plan
    identifyOutputAttributes(IntGraph9, IntGraph10, IntNodes1, IntNodes2, IntOutputAttrs1, IntRandomAttrs1, IntOuterRelations2),
    findall(OuterSeedAttr, member(seed(_, OuterSeedAttr), IntNodes2), OuterSeedAttrs),
    findall(VGWrapperSeedAttr, member(vgwrapper(_, _, _, _, _, _, _, _, VGWrapperSeedAttr), IntNodes2), VGWrapperSeedAttrs),
    append(OuterSeedAttrs, VGWrapperSeedAttrs, PlanSeedAttrs),
    findall(join(JoinID, JoinPreds, JoinType), member(join(JoinID, JoinPreds, JoinType), IntNodes2), Joins),
    findall(selection(SelectionID, SelectionPreds), member(selection(SelectionID, SelectionPreds), IntNodes2), Selections),
    eliminateSeedAttributesFromPlan(PlanSeedAttrs, IntGraph10, IntGraph11, IntNodes2, IntNodes3, Joins, Selections,
                          IntOutputAttrs1, IntOutputAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntSeedAttrs, IntOuterRelations2),
    updateOutputAttributes(IntGraph11, GraphOut, IntNodes3, IntNodesOut, IntOutputAttrs2, OutputAttrsOut,
                          IntRandomAttrs2, RandomAttrsOut, IntSeedAttrs, SeedAttrsOut, IntOuterRelations2, OuterRelationsOut),

    % **** re-compute keys for plan
    findall(candidateKey(TableNodeName, TableCandKey), (member(tablescan(TableNodeName, _, _), IntNodesOut),
                                           member(candidateKey(TableNodeName, TableCandKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushSingleVGWrapperUpInPlan(N)) ->
        retract(appliedTransformation(pushSingleVGWrapperUpInPlan(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSingleVGWrapperUpInPlan(NN)))
        ;
        assert(appliedTransformation(pushSingleVGWrapperUpInPlan(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING A SEMIJOIN PAST THE COMMON OUTER RELATION OF A SET OF VGWRAPPERS
% Returns true if (GraphOut, NodesOut) can legally result from pushing a semijoin past a set of vgwrappers in the query plan
% represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(copySemiJoinThroughVGWrapperBranch),

    % **** pick a VGWrapper
    member(vgwrapper(VGWrapperNodeID, _, _, VGSeedAttrIn, _, _, _, _, _), NodesIn),

    % **** and find its main relation
    member(outerRelation(VGWrapperNodeID, VGOuterRelationNode), OuterRelationsIn),
    findVGWrapperBaseOuterRelation(VGOuterRelationNode, BaseOuterRelation, GraphIn),

    % **** as well as its top seed join
    findTopLevelSeedJoin(VGWrapperNodeID, TopLevelSeedJoin, GraphIn, NodesIn, VGSeedAttrIn),

    % **** check if there exists a semijoin operator along the path [BaseOuterRelation...TopLevelSeedJoin],
    % **** which is parent of the BaseOuterRelation
    member(parent(SemiJoinNode, BaseOuterRelation), GraphIn),
    member(semijoin(SemiJoinNode, BaseOuterRelation, _), NodesIn),
    isDescendantNoVGWrapper(SemiJoinNode, TopLevelSeedJoin, GraphIn, NodesIn),
    nodeHasSingleParent(SemiJoinNode, GraphIn),

    % **** find all the related VGWrappers in that branch
    findall(VGWNodeID, (member(vgwrapper(VGWNodeID, _, _, VGSeedAttrIn, _, _, _, _, _), NodesIn),
                                        member(outerRelation(VGWNodeID, VGWOuterRelNode), OuterRelationsIn),
                                        isDescendant(VGWNodeID, TopLevelSeedJoin, GraphIn),
                                        findVGWrapperBaseOuterRelation(VGWOuterRelNode, BaseOuterRelation, GraphIn)), IntVGWNodes),
    removeDuplicates(IntVGWNodes, [], VGWNodes),

    % **** ensure that  this rule is fired once per VGWrapper branch
    select(VGWrapperNodeID, VGWNodes, RestVGWNodes),
    rankNodes(RestVGWNodes, VGWrapperNodeID),

    %findall(VGWOuterNodeID, (member(parent(VGWOuterNodeID, BaseOuterRelation), GraphIn), member(VGWNodeID, VGWNodes),
    %                                   (VGWOuterNodeID = VGWNodeID ; isDescendant(VGWOuterNodeID, VGWNodeID, GraphIn))), IntVGWOuterNodes),
    %removeDuplicates(IntVGWOuterNodes, [], VGWOuterNodes),

    % **** find BaseOuterRelation's parents
    select(parent(SemiJoinNode, BaseOuterRelation), GraphIn, IntGraph1),
    setof(parent(BaseOuterRelationParent, BaseOuterRelation),
         member(parent(BaseOuterRelationParent, BaseOuterRelation), IntGraph1), BaseOuterRelationParents),
    swapNodeParents(BaseOuterRelationParents, BaseOuterRelation, SemiJoinNode, IntGraph1, IntGraph2, NodesIn, IntNodes1),
    append([parent(SemiJoinNode, BaseOuterRelation)], IntGraph2, IntGraph3),

    % **** update outer relation info if necessary
    findall(outerRelation(AnyVGWrapperNodeID, BaseOuterRelation), member(outerRelation(AnyVGWrapperNodeID, BaseOuterRelation), OuterRelationsIn), BaseOuterRelationOuterRelations),
    findall(outerRelation(AnyVGWrapperNodeID, SemiJoinNode), member(outerRelation(AnyVGWrapperNodeID, BaseOuterRelation), OuterRelationsIn), SemiJoinOuterRelations),
    subtractSet(OuterRelationsIn, BaseOuterRelationOuterRelations, IntOuterRelations1),
    mergeSets(IntOuterRelations1, SemiJoinOuterRelations, IntOuterRelations2),

    % **** identify new candidate output attributes for semijoin node
    select(outputAttributes(SemiJoinNode, _), OutputAttrsIn, IntOutputAttrs1),
    select(randomAttributes(SemiJoinNode, _), RandomAttrsIn, IntRandomAttrs1),
    select(seedAttributes(SemiJoinNode, _), SeedAttrsIn, IntSeedAttrs1),
    identifyNodeSeedOutputRandomAttributes(SemiJoinNode, IntGraph3, IntNodes1, IntNodes2,
                                                  IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1,
                                                  IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    % **** add appropriate dedup operators
    %createDedupNodesAtVGWrappers(VGWOuterNodes, VGSeedAttrIn, SemiJoinNode, IntNodes2, IntNodes3, IntGraph3, IntGraph4,
    %                                            IntOuterRelations2, IntOuterRelations3, IntOutputAttrs2,
    %                                            IntOutputAttrs3, IntSeedAttrs2, IntSeedAttrs3, IntRandomAttrs2, IntRandomAttrs3),

    % **** project out any unecessary attributes
    updateOutputAttributes(IntGraph3, GraphOut, IntNodes2, IntNodesOut, IntOutputAttrs2, OutputAttrsOut,
                     IntRandomAttrs2, RandomAttrsOut, IntSeedAttrs2, SeedAttrsOut, IntOuterRelations2, OuterRelationsOut),

    %**** update candidate key information for entire plan
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut),
                                           member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut,
                                           OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(copySemiJoinThroughVGWrapperBranch(N)) ->
        retract(appliedTransformation(copySemiJoinThroughVGWrapperBranch(N))),
        NN is N + 1,
        assert(appliedTransformation(copySemiJoinThroughVGWrapperBranch(NN)))
        ;
        assert(appliedTransformation(copySemiJoinThroughVGWrapperBranch(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING A SELECTION PAST THE COMMON OUTER RELATION OF A SET OF VGWRAPPERS
% Returns true if (GraphOut, NodesOut) can legally result from pushing a selection past a set of vgwrappers in the query plan
% represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(copySelectionThroughVGWrapperBranch),

    % **** pick a VGWrapper
    member(vgwrapper(VGWrapperNodeID, _, _, VGSeedAttrIn, _, _, _, _, _), NodesIn),

    % **** and find its main relation
    member(outerRelation(VGWrapperNodeID, VGOuterRelationNode), OuterRelationsIn),
    findVGWrapperBaseOuterRelation(VGOuterRelationNode, BaseOuterRelation, GraphIn),

    % **** as well as its top seed join
    findTopLevelSeedJoin(VGWrapperNodeID, TopLevelSeedJoin, GraphIn, NodesIn, VGSeedAttrIn),

    % **** check if there exists a selection operator along the path [BaseOuterRelation...TopLevelSeedJoin],
    % **** which is parent of the BaseOuterRelation
    member(parent(SelectionNode, BaseOuterRelation), GraphIn),
    member(selection(SelectionNode, _), NodesIn),
    isDescendantNoVGWrapper(SelectionNode, TopLevelSeedJoin, GraphIn, NodesIn),
    nodeHasSingleParent(SelectionNode, GraphIn),

    % **** find all the related VGWrappers in that branch
    findall(VGWNodeID, (member(vgwrapper(VGWNodeID, _, _, VGSeedAttrIn, _, _, _, _, _), NodesIn),
                                        member(outerRelation(VGWNodeID, VGWOuterRelNode), OuterRelationsIn),
                                        isDescendant(VGWNodeID, TopLevelSeedJoin, GraphIn),
                                        findVGWrapperBaseOuterRelation(VGWOuterRelNode, BaseOuterRelation, GraphIn)), IntVGWNodes),
    removeDuplicates(IntVGWNodes, [], VGWNodes),

    % **** ensure that  this rule is fired once per VGWrapper branch
    select(VGWrapperNodeID, VGWNodes, RestVGWNodes),
    rankNodes(RestVGWNodes, VGWrapperNodeID),

    %findall(VGWOuterNodeID, (member(parent(VGWOuterNodeID, BaseOuterRelation), GraphIn), member(VGWNodeID, VGWNodes),
    %                                   (VGWOuterNodeID = VGWNodeID ; isDescendant(VGWOuterNodeID, VGWNodeID, GraphIn))), IntVGWOuterNodes),
    %removeDuplicates(IntVGWOuterNodes, [], VGWOuterNodes),

    % **** find BaseOuterRelation's parents
    select(parent(SelectionNode, BaseOuterRelation), GraphIn, IntGraph1),
    setof(parent(BaseOuterRelationParent, BaseOuterRelation),
         member(parent(BaseOuterRelationParent, BaseOuterRelation), IntGraph1), BaseOuterRelationParents),
    swapNodeParents(BaseOuterRelationParents, BaseOuterRelation, SelectionNode, IntGraph1, IntGraph2, NodesIn, IntNodes1),
    append([parent(SelectionNode, BaseOuterRelation)], IntGraph2, IntGraph3),

    % **** update outer relation info if necessary
    findall(outerRelation(AnyVGWrapperNodeID, BaseOuterRelation), member(outerRelation(AnyVGWrapperNodeID, BaseOuterRelation), OuterRelationsIn), BaseOuterRelationOuterRelations),
    findall(outerRelation(AnyVGWrapperNodeID, SelectionNode), member(outerRelation(AnyVGWrapperNodeID, BaseOuterRelation), OuterRelationsIn), SelectionOuterRelations),
    subtractSet(OuterRelationsIn, BaseOuterRelationOuterRelations, IntOuterRelations1),
    mergeSets(IntOuterRelations1, SelectionOuterRelations, IntOuterRelations2),

    % **** identify new candidate output attributes for selection node
    select(outputAttributes(SelectionNode, _), OutputAttrsIn, IntOutputAttrs1),
    select(randomAttributes(SelectionNode, _), RandomAttrsIn, IntRandomAttrs1),
    select(seedAttributes(SelectionNode, _), SeedAttrsIn, IntSeedAttrs1),
    identifyNodeSeedOutputRandomAttributes(SelectionNode, IntGraph3, IntNodes1, IntNodes2,
                                                  IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1,
                                                  IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    % **** add appropriate dedup operators
    %createDedupNodesAtVGWrappers(VGWOuterNodes, VGSeedAttrIn, SelectionNode, IntNodes2, IntNodes3, IntGraph3, IntGraph4,
    %                                            IntOuterRelations2, IntOuterRelations3, IntOutputAttrs2,
    %                                            IntOutputAttrs3, IntSeedAttrs2, IntSeedAttrs3, IntRandomAttrs2, IntRandomAttrs3),

    % **** project out any unecessary attributes
    updateOutputAttributes(IntGraph3, GraphOut, IntNodes2, IntNodesOut, IntOutputAttrs2, OutputAttrsOut,
                             IntRandomAttrs2, RandomAttrsOut, IntSeedAttrs2, SeedAttrsOut, IntOuterRelations2, OuterRelationsOut),

    %**** update candidate key information for entire plan
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut),
                                           member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(copySelectionThroughVGWrapperBranch(N)) ->
        retract(appliedTransformation(copySelectionThroughVGWrapperBranch(N))),
        NN is N + 1,
        assert(appliedTransformation(copySelectionThroughVGWrapperBranch(NN)))
        ;
        assert(appliedTransformation(copySelectionThroughVGWrapperBranch(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS ELIMINATION OF A REDUNDANT DEDUP OPERATOR
% Returns true if (GraphOut, NodesOut) can legally result eliminating a redundant dedup operator in the query plan represented
% by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(eliminateDedup),

    %**** find a dedup operator
    member(dedup(DedupNodeID, DedupAttrs), NodesIn),

    % **** find its child node
    select(parent(DedupNodeID, ChildNodeID), GraphIn, IntGraph1),

    %**** and check whether it can be eliminated
    setof(ChildCandidateKey, (member(candidateKey(ChildNodeID, ChildCandidateKey), KeysIn)), ChildCandidateKeys),
    dedupCanBeEliminated(ChildCandidateKeys, DedupAttrs),

    %**** re-arrange the structure of the graph
    setof(parent(DedupParentID, DedupNodeID),
         member(parent(DedupParentID, DedupNodeID), IntGraph1), DedupNodeParents),
    swapNodeParents(DedupNodeParents, DedupNodeID, ChildNodeID, IntGraph1, GraphOut, NodesIn, IntNodes),

    % delete dedup node from plan
    select(dedup(DedupNodeID, DedupAttrs), IntNodes, IntNodesOut),

    %**** delete dedup node from set of output, seed and random attributes
    select(outputAttributes(DedupNodeID, _), OutputAttrsIn, IntOutputAttrs),
    select(seedAttributes(DedupNodeID, _), SeedAttrsIn, IntSeedAttrs),
    select(randomAttributes(DedupNodeID, _), RandomAttrsIn, IntRandomAttrs),

    % **** update output/seed/random attributes of child node
    updateNodeSeedOutputRandomAttributes(ChildNodeID, GraphOut, IntNodesOut, IntOutputAttrs, OutputAttrsOut, IntRandomAttrs, RandomAttrsOut, IntSeedAttrs, SeedAttrsOut),

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, DedupNodeID), member(outerRelation(VGWrapperNodeID, DedupNodeID), OuterRelationsIn), DedupOuterRelations),
    findall(outerRelation(VGWrapperNodeID, ChildNodeID), member(outerRelation(VGWrapperNodeID, DedupNodeID), OuterRelationsIn), ChildOuterRelations),
    subtractSet(OuterRelationsIn, DedupOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, ChildOuterRelations, OuterRelationsOut),

    %**** update candidate key information for entire plan
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut),
                                           member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut,
                                           OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(eliminateDedup(N)) ->
        retract(appliedTransformation(eliminateDedup(N))),
        NN is N + 1,
        assert(appliedTransformation(eliminateDedup(NN)))
        ;
        assert(appliedTransformation(eliminateDedup(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS THE RE_ORDERING OF TWO JOIN OPERATORS
% Returns true if (GraphOut, NodesOut) can legally result from re-ordering three join operators in the query plan represented by
% (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(changeJoinOrders),

    %%% DEBUG
    %TopJoinNodeID = node5,
    %

    %**** find a join operator that is above another join operator
    select(join(TopJoinNodeID, TopJoinPreds, _), NodesIn, IntNodes1),
    select(parent(TopJoinNodeID, BtmJoinNodeID), GraphIn, IntGraph1),
    select(join(BtmJoinNodeID, BtmJoinPreds, _), IntNodes1, IntNodes2),

    % **** DEBUG
    %BtmJoinNodeID = node14,
    % ****

    %**** rule applies only if lower join node has one parent
    nodeHasSingleParent(BtmJoinNodeID, GraphIn),
    %**** and when only of in of them is NOT a seed join
    (not(isSeedJoin(BtmJoinNodeID, NodesIn));not(isSeedJoin(TopJoinNodeID, NodesIn))),

    %**** find children nodes of the two join nodes
    select(parent(TopJoinNodeID, TopLHSNodeID), IntGraph1, IntGraph2),
    select(parent(BtmJoinNodeID, BtmLHSNodeID), IntGraph2, IntGraph3),
    select(parent(BtmJoinNodeID, BtmRHSNodeID), IntGraph3, IntGraph4),

    % **** ensure that BtmLHSNodeID is not a VGWrapper branch
    %(isSeedJoin(BtmJoinNodeID, NodesIn) -> pathContainsVGWrapperNode(BtmLHSNodeID, NodesIn, GraphIn); true),

    %**** re-arrange the structure of the graph: the TopJoinNodeID will now be a parent of BtmLHSNodeID and BtmJoinNodeID;
    %**** BotJoinNodeID will now be a parent of TopLHSNodeID and BotRHSNodeID
    append([parent(TopJoinNodeID, BtmLHSNodeID), parent(BtmJoinNodeID, TopLHSNodeID),
            parent(BtmJoinNodeID, BtmRHSNodeID), parent(TopJoinNodeID, BtmJoinNodeID)], IntGraph4, GraphOut),

    % **** redistribute predicates among the two joins
    member(outputAttributes(TopLHSNodeID, TopLHSNodeOutputAttrs), OutputAttrsIn),
    member(outputAttributes(BtmRHSNodeID, BtmRHSNodeOutputAttrs), OutputAttrsIn),
    append(TopLHSNodeOutputAttrs, BtmRHSNodeOutputAttrs, IntBtmJoinOutputAttrs),
    append(TopJoinPreds, BtmJoinPreds, AllJoinPreds),
    partitionPredsAmongJoins(AllJoinPreds, [], NewTopJoinPreds, [], NewBtmJoinPreds, IntBtmJoinOutputAttrs),
    % ****

    % **** update join nodes in the plan
    append([join(TopJoinNodeID, NewTopJoinPreds, _)], IntNodes2, IntNodes3),
    append([join(BtmJoinNodeID, NewBtmJoinPreds, _)], IntNodes3, IntNodes4),

    % **** update output/random/seed attributes of joins
    select(outputAttributes(BtmJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(randomAttributes(BtmJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    select(seedAttributes(BtmJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    identifyNodeSeedOutputRandomAttributes(BtmJoinNodeID, GraphOut, IntNodes4, IntNodes5, IntSeedAttrs1, IntSeedAttrs2,
                                             IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    select(outputAttributes(TopJoinNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(randomAttributes(TopJoinNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    select(seedAttributes(TopJoinNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    identifyNodeSeedOutputRandomAttributes(TopJoinNodeID, GraphOut, IntNodes5, IntNodesOut, IntSeedAttrs3, IntSeedAttrs4,
                                             IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(TopJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(BtmJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    % ****

    OuterRelationsOut = OuterRelationsIn,

    %**** update candidate key information for entire plan
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut),
                                           member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut,
                                           OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(changeJoinOrders(N)) ->
        retract(appliedTransformation(changeJoinOrders(N))),
        NN is N + 1,
        assert(appliedTransformation(changeJoinOrders(NN)))
        ;
        assert(appliedTransformation(changeJoinOrders(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN A SELECTION PAST A RENAME OPERATOR
% Returns true if (GraphOut, NodesOut) can legally result from pushing a selection operation
% past a rename operator in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSelectionDownRename),

    %**** find a selection operator that is above a scalar function
    select(selection(SelectionNodeID, SelectionPreds), NodesIn, IntNodes1),
    select(parent(SelectionNodeID, ScalarFuncNodeID), GraphIn, IntGraph1),
    member(scalarfunc(ScalarFuncNodeID, FuncExprs, FuncExprsInAttrs, FuncExprsOutAttrs), IntNodes1),

    %**** rule applies only if scalar func node has one parent
    nodeHasSingleParent(ScalarFuncNodeID, GraphIn),

    identifyRenameAttrPairs(FuncExprs, FuncExprsInAttrs, FuncExprsOutAttrs, [], RenameAttrPairs),
    RenameAttrPairs \= [],
    flatten(FuncExprsOutAttrs, FlattenedFuncExprsOutAttrs),
    partitionPredsBasedOnRenamedAttrs(SelectionPreds, RenameAttrPairs, FlattenedFuncExprsOutAttrs, [], TopSelectionPreds, [], BtmSelectionPreds),

    BtmSelectionPreds \= [],

    (TopSelectionPreds = [] ->
        % **** move selection operator past scalar function
        append([selection(SelectionNodeID, BtmSelectionPreds)], IntNodes1, IntNodes2),
        select(parent(ScalarFuncNodeID, ScalarFuncChildNode), IntGraph1, IntGraph2),
        setof(parent(SelectionParentID, SelectionNodeID),
            member(parent(SelectionParentID, SelectionNodeID), IntGraph2), SelectionNodeParents),
        swapNodeParents(SelectionNodeParents, SelectionNodeID, ScalarFuncNodeID, IntGraph2, IntGraph3, IntNodes2, IntNodes3),
        append([parent(ScalarFuncNodeID, SelectionNodeID)], IntGraph3, IntGraph4),
        append([parent(SelectionNodeID, ScalarFuncChildNode)], IntGraph4, GraphOut),

        % **** update output, random and seed attributes for selection node
        select(outputAttributes(SelectionNodeID, _), OutputAttrsIn, IntOutputAttrs1),
        select(randomAttributes(SelectionNodeID, _), RandomAttrsIn, IntRandomAttrs1),
        select(seedAttributes(SelectionNodeID, _), SeedAttrsIn, IntSeedAttrs1),
        identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes3, IntNodes4, IntSeedAttrs1, IntSeedAttrs2,
                                             IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

        select(outputAttributes(ScalarFuncNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
        select(randomAttributes(ScalarFuncNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
        select(seedAttributes(ScalarFuncNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
        identifyNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodes4, IntNodesOut, IntSeedAttrs3, IntSeedAttrs4,
                                             IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

        updateNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
        updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
        % ****

        % **** update outer relation info if necessary
        findall(outerRelation(VGWrapperNodeID, SelectionNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SelectionOuterRelations),
        findall(outerRelation(VGWrapperNodeID, ScalarFuncNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), ScalarFuncOuterRelations),
        subtractSet(OuterRelationsIn, SelectionOuterRelations, IntOuterRelations),
        mergeSets(IntOuterRelations, ScalarFuncOuterRelations, OuterRelationsOut)
        ;
        % **** create a new selection predicate past scalar function
        append([selection(SelectionNodeID, TopSelectionPreds)], IntNodes1, IntNodes2),
        gensym(selectionNode, NewSelectionNodeID),
        append([selection(NewSelectionNodeID, BtmSelectionPreds)], IntNodes2, IntNodes3),

        select(parent(ScalarFuncNodeID, ScalarFuncChildNode), GraphIn, IntGraph2),
        append([parent(ScalarFuncNodeID, NewSelectionNodeID)], IntGraph2, IntGraph3),
        append([parent(NewSelectionNodeID, ScalarFuncChildNode)], IntGraph3, GraphOut),

        % **** add output and random and attributes for new selection node
        identifyNodeSeedOutputRandomAttributes(NewSelectionNodeID, GraphOut, IntNodes3, IntNodes4, SeedAttrsIn, IntSeedAttrs1,
                                             RandomAttrsIn, IntRandomAttrs1, OutputAttrsIn, IntOutputAttrs1),


        % **** identify new output and random attributes for scalar function node
        select(outputAttributes(ScalarFuncNodeID, _), IntOutputAttrs1, IntOutputAttrs2),
        select(randomAttributes(ScalarFuncNodeID, _), IntRandomAttrs1, IntRandomAttrs2),
        select(seedAttributes(ScalarFuncNodeID, _), IntSeedAttrs1, IntSeedAttrs2),
        identifyNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodes4, IntNodes5, IntSeedAttrs2, IntSeedAttrs3,
                                             IntRandomAttrs2, IntRandomAttrs3, IntOutputAttrs2, IntOutputAttrs3),

        % **** identify new output and random attributes for selection node
        select(outputAttributes(SelectionNodeID, _), IntOutputAttrs3, IntOutputAttrs4),
        select(randomAttributes(SelectionNodeID, _), IntRandomAttrs3, IntRandomAttrs4),
        select(seedAttributes(SelectionNodeID, _), IntSeedAttrs3, IntSeedAttrs4),
        identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes5, IntNodesOut, IntSeedAttrs4, IntSeedAttrs5,
                                             IntRandomAttrs4, IntRandomAttrs5, IntOutputAttrs4, IntOutputAttrs5),

        updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, IntOutputAttrs6, IntRandomAttrs5, IntRandomAttrs6, IntSeedAttrs5, IntSeedAttrs6),
        updateNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodesOut, IntOutputAttrs6, IntOutputAttrs7, IntRandomAttrs6, IntRandomAttrs7, IntSeedAttrs6, IntSeedAttrs7),
        updateNodeSeedOutputRandomAttributes(NewSelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs7, OutputAttrsOut, IntRandomAttrs7, RandomAttrsOut, IntSeedAttrs7, SeedAttrsOut),
        % *****

        OuterRelationsOut = OuterRelationsIn
    ),

    %**** update candidate key information for entire plan
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut),
                                           member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut,
                                           OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushSelectionDownRename(N)) ->
        retract(appliedTransformation(pushSelectionDownRename(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSelectionDownRename(NN)))
        ;
        assert(appliedTransformation(pushSelectionDownRename(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN A JOIN PAST A RENAME OPERATOR
% Returns true if (GraphOut, NodesOut) can legally result from pushing a join operation
% past a rename operator in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushJoinDownRename),

    %**** find a join operator that is above a scalar function
    select(join(JoinNodeID, JoinPreds, _), NodesIn, IntNodes1),
    select(parent(JoinNodeID, ScalarFuncNodeID), GraphIn, IntGraph1),
    member(scalarfunc(ScalarFuncNodeID, FuncExprs, FuncExprsInAttrs, FuncExprsOutAttrs), IntNodes1),

    %**** rule applies only if scalar func node has one parent
    nodeHasSingleParent(ScalarFuncNodeID, GraphIn),

    %*** ADDED BY LUIS: make sure this isn't some kind of fork/self-join
    member(parent(JoinNodeID, OtherNode), IntGraph1),
    member(outputAttributes(OtherNode, OtherAttrs), OutputAttrsIn),
    findall(Att, (member(Att, OtherAttrs), member(Att, FuncExprsInAttrs)), []),

    identifyRenameAttrPairs(FuncExprs, FuncExprsInAttrs, FuncExprsOutAttrs, [], RenameAttrPairs),
    RenameAttrPairs \= [],
    flatten(FuncExprsOutAttrs, FlattenedFuncExprsOutAttrs),
    partitionPredsBasedOnRenamedAttrs(JoinPreds, RenameAttrPairs, FlattenedFuncExprsOutAttrs, [], TopJoinPreds, [], BtmJoinPreds),

    BtmJoinPreds \= [],
    
    % **** transformation can only legally be applied if all predicates can be pushed down
    TopJoinPreds = [],
    
    % **** move join operator past scalar function
    append([join(JoinNodeID, BtmJoinPreds, _)], IntNodes1, IntNodes2),
    select(parent(ScalarFuncNodeID, ScalarFuncChildNode), IntGraph1, IntGraph2),
    setof(parent(JoinParentID, JoinNodeID), member(parent(JoinParentID, JoinNodeID), IntGraph2), JoinNodeParents),
    swapNodeParents(JoinNodeParents, JoinNodeID, ScalarFuncNodeID, IntGraph2, IntGraph3, IntNodes2, IntNodes3),
    append([parent(ScalarFuncNodeID, JoinNodeID)], IntGraph3, IntGraph4),
    append([parent(JoinNodeID, ScalarFuncChildNode)], IntGraph4, GraphOut),

    % **** update output, random and seed attributes for join node
    select(outputAttributes(JoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(randomAttributes(JoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    select(seedAttributes(JoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    identifyNodeSeedOutputRandomAttributes(JoinNodeID, GraphOut, IntNodes3, IntNodes4, IntSeedAttrs1, IntSeedAttrs2,
                                             IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    select(outputAttributes(ScalarFuncNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(randomAttributes(ScalarFuncNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    select(seedAttributes(ScalarFuncNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    identifyNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodes4, IntNodesOut, IntSeedAttrs3, IntSeedAttrs4,
                                             IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(JoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    % ****

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, JoinNodeID), member(outerRelation(VGWrapperNodeID, JoinNodeID), OuterRelationsIn), JoinOuterRelations),
    findall(outerRelation(VGWrapperNodeID, ScalarFuncNodeID), member(outerRelation(VGWrapperNodeID, JoinNodeID), OuterRelationsIn), ScalarFuncOuterRelations),
    subtractSet(OuterRelationsIn, JoinOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, ScalarFuncOuterRelations, OuterRelationsOut),

    %**** update candidate key information for entire plan
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut),
                                           member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut,
                                           OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushJoinDownRename(N)) ->
        retract(appliedTransformation(pushJoinDownRename(N))),
        NN is N + 1,
        assert(appliedTransformation(pushJoinDownRename(NN)))
        ;
        assert(appliedTransformation(pushJoinDownRename(1)))).
        
%*****************************************************************************************************************************
% RULE THAT ELIMINATES REDUNDANT COMPUTATION FROM A SCALAR FUNCTION OPERATOR BASED ON ITS OUTPUT ATTRIBUTES
% Returns true if (GraphOut, NodesOut) can legally result from eliminating from a scalar function generation
% of attributes that do not exist in the output attributes of the operator in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, _, _,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(simplifyScalarFunc),

    %**** find a a scalar function
    select(scalarfunc(ScalarFuncNodeID, FuncExprs, FuncExprsInAttrs, FuncExprsOutAttrs), NodesIn, IntNodes1),

    member(outputAttributes(ScalarFuncNodeID, ScalarFuncOutputAttrs), OutputAttrsIn),
    eliminateRedundantAttributeGeneration(FuncExprs, [], NewFuncExprs, FuncExprsInAttrs, [],
                                  NewFuncExprsInAttrs, FuncExprsOutAttrs, [], NewFuncExprsOutAttrs, ScalarFuncOutputAttrs),

    not(equalSets(FuncExprs, NewFuncExprs)),
    
    (NewFuncExprs = [] ->
        % **** scalar function can be eliminated
        select(parent(ScalarFuncNodeID, ScalarFuncChildID), GraphIn, IntGraph1),
        nodeHasSingleParent(ScalarFuncChildID, GraphIn),
        
        setof(parent(ScalarFuncParentID, ScalarFuncNodeID),
            member(parent(ScalarFuncParentID, ScalarFuncNodeID), GraphIn), ScalarFuncParents),
        swapNodeParents(ScalarFuncParents, ScalarFuncNodeID, ScalarFuncChildID, IntGraph1, IntGraph2, IntNodes1, IntNodes2),
        
        % **** update outer relation info if necessary
        findall(outerRelation(VGWrapperNodeID, ScalarFuncNodeID), member(outerRelation(VGWrapperNodeID, ScalarFuncNodeID), OuterRelationsIn), ScalarFuncOuterRelations),
        findall(outerRelation(VGWrapperNodeID, ScalarFuncChildID), member(outerRelation(VGWrapperNodeID, ScalarFuncNodeID), OuterRelationsIn), ScalarFuncChildOuterRelations),
        subtractSet(OuterRelationsIn, ScalarFuncOuterRelations, IntOuterRelations1),
        mergeSets(IntOuterRelations1, ScalarFuncChildOuterRelations, IntOuterRelations2)
        ;
        % **** update scalar function definition
        append([scalarfunc(ScalarFuncNodeID, NewFuncExprs, NewFuncExprsInAttrs, NewFuncExprsOutAttrs)], IntNodes1, IntNodes2),
        IntGraph2 = GraphIn,
        IntOuterRelations2 = OuterRelationsIn
    ),
    
    % **** update output/seed/random attributes from the plan
    identifyOutputAttributes(IntGraph2, IntGraph3, IntNodes2, IntNodes3, IntOutputAttrs1, IntRandomAttrs1, IntOuterRelations2),
    findall(OuterSeedAttr, member(seed(_, OuterSeedAttr), IntNodes3), OuterSeedAttrs),
    findall(VGWrapperSeedAttr, member(vgwrapper(_, _, _, _, _, _, _, _, VGWrapperSeedAttr), IntNodes3), VGWrapperSeedAttrs),
    append(OuterSeedAttrs, VGWrapperSeedAttrs, PlanSeedAttrs),
    findall(join(JoinID, JoinPreds, JoinType), member(join(JoinID, JoinPreds, JoinType), IntNodes3), Joins),
    findall(selection(SelectionID, SelectionPreds), member(selection(SelectionID, SelectionPreds), IntNodes3), Selections),
    eliminateSeedAttributesFromPlan(PlanSeedAttrs, IntGraph3, IntGraph4, IntNodes3, IntNodes4, Joins, Selections,
                          IntOutputAttrs1, IntOutputAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntSeedAttrs, IntOuterRelations2),
    updateOutputAttributes(IntGraph4, GraphOut, IntNodes4, IntNodesOut, IntOutputAttrs2, OutputAttrsOut,
                          IntRandomAttrs2, RandomAttrsOut, IntSeedAttrs, SeedAttrsOut, IntOuterRelations2, OuterRelationsOut),

    % **** re-compute keys for plan
    findall(candidateKey(TableNodeName, TableCandKey), (member(tablescan(TableNodeName, _, _), IntNodesOut),
                                           member(candidateKey(TableNodeName, TableCandKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(simplifyScalarFunc(N)) ->
        retract(appliedTransformation(simplifyScalarFunc(N))),
        NN is N + 1,
        assert(appliedTransformation(simplifyScalarFunc(NN)))
        ;
        assert(appliedTransformation(simplifyScalarFunc(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN SELECTIONS PAST A SCALAR FUNCTION
% Returns true if (GraphOut, NodesOut) can legally result from pushing a selection operation past a scalar function
% (no rename) operator in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSelectionDownScalarFunc),

    %**** find a selection operator that is above a scalar function
    select(selection(SelectionNodeID, SelectionPreds), NodesIn, IntNodes1),
    select(parent(SelectionNodeID, ScalarFuncNodeID), GraphIn, IntGraph1),
    member(scalarfunc(ScalarFuncNodeID, _, _, FuncExprsOutAttrs), IntNodes1),

    %**** rule applies only if scalar func node has one parent
    nodeHasSingleParent(ScalarFuncNodeID, GraphIn),

    flatten(FuncExprsOutAttrs, FlattenedFuncExprsOutAttrs),
    partitionPredsBasedOnFuncOutputAttrs(FlattenedFuncExprsOutAttrs, SelectionPreds, [], TopSelectionPreds, [], BtmSelectionPreds),

    BtmSelectionPreds \= [],

    (TopSelectionPreds = [] ->
        % **** move selection predicate past scalar function
        select(parent(ScalarFuncNodeID, ScalarFuncChildNode), IntGraph1, IntGraph2),
        setof(parent(SelectionParentID, SelectionNodeID),
            member(parent(SelectionParentID, SelectionNodeID), IntGraph2), SelectionNodeParents),
        swapNodeParents(SelectionNodeParents, SelectionNodeID, ScalarFuncNodeID, IntGraph2, IntGraph3, NodesIn, IntNodes2),
        append([parent(ScalarFuncNodeID, SelectionNodeID)], IntGraph3, IntGraph4),
        append([parent(SelectionNodeID, ScalarFuncChildNode)], IntGraph4, GraphOut),

        % **** update output, random and seed attributes for selection node
        select(outputAttributes(SelectionNodeID, _), OutputAttrsIn, IntOutputAttrs1),
        select(randomAttributes(SelectionNodeID, _), RandomAttrsIn, IntRandomAttrs1),
        select(seedAttributes(SelectionNodeID, _), SeedAttrsIn, IntSeedAttrs1),
        identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes2, IntNodes3, IntSeedAttrs1, IntSeedAttrs2,
                                             IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

        select(outputAttributes(ScalarFuncNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
        select(randomAttributes(ScalarFuncNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
        select(seedAttributes(ScalarFuncNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
        identifyNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodes3, IntNodesOut, IntSeedAttrs3, IntSeedAttrs4,
                                             IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

        updateNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
        updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
        % ****

        % **** update outer relation info if necessary
        findall(outerRelation(VGWrapperNodeID, SelectionNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SelectionOuterRelations),
        findall(outerRelation(VGWrapperNodeID, ScalarFuncNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), ScalarFuncOuterRelations),
        subtractSet(OuterRelationsIn, SelectionOuterRelations, IntOuterRelations),
        mergeSets(IntOuterRelations, ScalarFuncOuterRelations, OuterRelationsOut)
        ;
        % **** create a new selection predicate past generalized aggregate
        append([selection(SelectionNodeID, TopSelectionPreds)], IntNodes1, IntNodes2),
        gensym(selectionNode, NewSelectionNodeID),
        append([selection(NewSelectionNodeID, BtmSelectionPreds)], IntNodes2, IntNodes3),

        select(parent(ScalarFuncNodeID, ScalarFuncChildNode), GraphIn, IntGraph2),
        append([parent(ScalarFuncNodeID, NewSelectionNodeID)], IntGraph2, IntGraph3),
        append([parent(NewSelectionNodeID, ScalarFuncChildNode)], IntGraph3, GraphOut),

        % **** add output and random and attributes for new selection node
        identifyNodeSeedOutputRandomAttributes(NewSelectionNodeID, GraphOut, IntNodes3, IntNodes4, SeedAttrsIn, IntSeedAttrs1,
                                             RandomAttrsIn, IntRandomAttrs1, OutputAttrsIn, IntOutputAttrs1),


        % **** identify new output and random attributes for scalar function node
        select(outputAttributes(ScalarFuncNodeID, _), IntOutputAttrs1, IntOutputAttrs2),
        select(randomAttributes(ScalarFuncNodeID, _), IntRandomAttrs1, IntRandomAttrs2),
        select(seedAttributes(ScalarFuncNodeID, _), IntSeedAttrs1, IntSeedAttrs2),
        identifyNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodes4, IntNodes5, IntSeedAttrs2, IntSeedAttrs3,
                                             IntRandomAttrs2, IntRandomAttrs3, IntOutputAttrs2, IntOutputAttrs3),

        % **** identify new output and random attributes for selection node
        select(outputAttributes(SelectionNodeID, _), IntOutputAttrs3, IntOutputAttrs4),
        select(randomAttributes(SelectionNodeID, _), IntRandomAttrs3, IntRandomAttrs4),
        select(seedAttributes(SelectionNodeID, _), IntSeedAttrs3, IntSeedAttrs4),
        identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes5, IntNodesOut, IntSeedAttrs4, IntSeedAttrs5,
                                             IntRandomAttrs4, IntRandomAttrs5, IntOutputAttrs4, IntOutputAttrs5),

        updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, IntOutputAttrs6, IntRandomAttrs5, IntRandomAttrs6, IntSeedAttrs5, IntSeedAttrs6),
        updateNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodesOut, IntOutputAttrs6, IntOutputAttrs7, IntRandomAttrs6, IntRandomAttrs7, IntSeedAttrs6, IntSeedAttrs7),
        updateNodeSeedOutputRandomAttributes(NewSelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs7, OutputAttrsOut, IntRandomAttrs7, RandomAttrsOut, IntSeedAttrs7, SeedAttrsOut),
        % *****

        OuterRelationsOut = OuterRelationsIn
    ),

    %**** update candidate key information for entire plan
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut),
                                           member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut,
                                           OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushSelectionDownScalarFunc(N)) ->
        retract(appliedTransformation(pushSelectionDownScalarFunc(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSelectionDownScalarFunc(NN)))
        ;
        assert(appliedTransformation(pushSelectionDownScalarFunc(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING A VGWRAPPER OPERATOR IN UPPER LEVELS IN THE PLAN SO AS TO AVOID MOVING REDUNDANT ATTRIBUTES
% Returns true if (GraphOut, NodesOut) can legally result from pushing up a vgwrapper in the query plan represented
% by (GraphIn, NodesIn)
% **** version that deals with the case where a single VGWrapper exists in the branch under consideration
transform(GraphIn, NodesIn, _, KeysIn, OuterRelationsIn, _, _,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushGroupedVGWrapperUpInPlan),

    % **** find a vgwrapper operator
    member(vgwrapper(VGWrapperNodeID, _, _, SeedAttrIn, _, _, _, VGOutAttrs, SeedAttrOut), NodesIn),
    
    % **** as well as its immediate join ancestor
    select(parent(VGWrapperParent, VGWrapperNodeID), GraphIn, IntGraph1),
    member(join(VGWrapperParent, _, _), NodesIn),

    % **** find the top seed join pertaining to that VGWrapper
    findTopLevelSeedJoin(VGWrapperNodeID, TopLevelSeedJoin, GraphIn, NodesIn, SeedAttrIn),

    % **** we are considering a VGWrapper within a group of VGWrappers
    TopLevelSeedJoin \= VGWrapperParent,

    % **** find a path to an ancestor node that needs the output attributes of this VGWrapper
    % **** and collect the attributes from that VGWrapper that are needed along the path
    findVGWrapperDependantAncestorPath(TopLevelSeedJoin, VGOutAttrs, GraphIn, NodesIn, [], NecessaryVGOutputAttrs, [], AncestorNodePath),
    length(AncestorNodePath, ALen),
    ALen > 1,

    % **** no node in the path should need the VGWrapper attributes
    NecessaryVGOutputAttrs = [],

    % **** delete VGWrapper branch
    select(outerRelation(VGWrapperNodeID, VGOuterRelationNode), OuterRelationsIn, IntOuterRelations1),
    not(nodeHasSingleParent(VGOuterRelationNode, IntGraph1)),
    select(parent(VGWrapperNodeID, VGOuterRelationNode), IntGraph1, IntGraph2),
    select(parent(VGWrapperParent, OtherChild), IntGraph2, IntGraph3),
    select(parent(VGWrapperParentParent, VGWrapperParent), IntGraph3, IntGraph4),
    %select(join(VGWrapperParentParent, _, _), NodesIn, IntNodes1),
    append([parent(VGWrapperParentParent, OtherChild)], IntGraph4, IntGraph5),
    
    % **** and move it in its new position in the plan
    NodeIndex1 is ALen - 2,
    nth0(NodeIndex1, AncestorNodePath, LowerNode),
    NodeIndex2 is ALen - 1,
    nth0(NodeIndex2, AncestorNodePath, UpperNode),
    select(parent(UpperNode, LowerNode), IntGraph5, IntGraph6),

    % **** first create new predicate for join
    member(join(TopLevelSeedJoin, [TopLevelJoinPred], _), NodesIn),
    compExp(TopLevelJoinPred, equals, TopLevelJoinLHSSeedAttr, TopLevelJoinRHSSeedAttr, identifier),
    (member(seed(_, TopLevelJoinLHSSeedAttr), NodesIn) -> OuterSeedAttr = TopLevelJoinLHSSeedAttr; OuterSeedAttr = TopLevelJoinRHSSeedAttr),
    gensym(equalsPred, NewJoinPred),
    assert(compExp(NewJoinPred, equals, OuterSeedAttr, SeedAttrOut, identifier)),
    
    % **** and then a new seed join node
    append([join(VGWrapperParent, [NewJoinPred], _)], NodesIn, IntNodes1),
    append([parent(UpperNode, VGWrapperParent)], IntGraph6, IntGraph7),
    append([parent(VGWrapperNodeID, LowerNode)], IntGraph7, IntGraph8),
    append([parent(VGWrapperParent, LowerNode)], IntGraph8, IntGraph9),
    append([parent(VGWrapperParent, VGWrapperNodeID)], IntGraph9, IntGraph10),
    append([outerRelation(VGWrapperNodeID, LowerNode)], IntOuterRelations1, IntOuterRelations2),

    % **** update output/seed/random attributes from the plan
    identifyOutputAttributes(IntGraph10, IntGraph11, IntNodes1, IntNodes2, IntOutputAttrs1, IntRandomAttrs1, IntOuterRelations2),
    findall(OuterSeedAttr, member(seed(_, OuterSeedAttr), IntNodes2), OuterSeedAttrs),
    findall(VGWrapperSeedAttr, member(vgwrapper(_, _, _, _, _, _, _, _, VGWrapperSeedAttr), IntNodes2), VGWrapperSeedAttrs),
    append(OuterSeedAttrs, VGWrapperSeedAttrs, PlanSeedAttrs),
    findall(join(JoinID, JoinPreds, JoinType), member(join(JoinID, JoinPreds, JoinType), IntNodes2), Joins),
    findall(selection(SelectionID, SelectionPreds), member(selection(SelectionID, SelectionPreds), IntNodes2), Selections),
    eliminateSeedAttributesFromPlan(PlanSeedAttrs, IntGraph11, IntGraph12, IntNodes2, IntNodes3, Joins, Selections,
                          IntOutputAttrs1, IntOutputAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntSeedAttrs, IntOuterRelations2),
    updateOutputAttributes(IntGraph12, GraphOut, IntNodes3, IntNodesOut, IntOutputAttrs2, OutputAttrsOut,
                          IntRandomAttrs2, RandomAttrsOut, IntSeedAttrs, SeedAttrsOut, IntOuterRelations2, OuterRelationsOut),

    % **** re-compute keys for plan
    findall(candidateKey(TableNodeName, TableCandKey), (member(tablescan(TableNodeName, _, _), IntNodesOut),
                                           member(candidateKey(TableNodeName, TableCandKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushSingleVGWrapperUpInPlan(N)) ->
        retract(appliedTransformation(pushGroupedVGWrapperUpInPlan(N))),
        NN is N + 1,
        assert(appliedTransformation(pushGroupedVGWrapperUpInPlan(NN)))
        ;
        assert(appliedTransformation(pushGroupedVGWrapperUpInPlan(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSH OF A SELECTION DOWN A SPLIT
% Returns true if (GraphOut, NodesOut) can legally result from pushing a selection operator past a split operator in the query
% plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSelectionDownSplit),

    %**** find a selection operator that is above a split
    member(selection(SelectionNodeID, _), NodesIn),
    select(parent(SelectionNodeID, SplitNodeID), GraphIn, IntGraph1),
    member(split(SplitNodeID, _), NodesIn),

    %**** rule applies only if lower split node has one parent
    nodeHasSingleParent(SplitNodeID, GraphIn),

    %**** find the child of the split node
    select(parent(SplitNodeID, SplitNodeChild), IntGraph1, IntGraph2),
    %**** and the parents of the selection node
    setof(parent(SelectionParentID, SelectionNodeID),
         member(parent(SelectionParentID, SelectionNodeID), GraphIn), SelectionNodeParents),
    %**** re-arrange the structure of the graph
    swapNodeParents(SelectionNodeParents, SelectionNodeID, SplitNodeID, IntGraph2, IntGraph3, NodesIn, IntNodes1tmp),
    append([parent(SelectionNodeID, SplitNodeChild)], IntGraph3, IntGraph4),
    append([parent(SplitNodeID, SelectionNodeID)], IntGraph4, GraphOut),

    % **** update output, random and attributes for swapped nodes
    select(outputAttributes(SelectionNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(randomAttributes(SelectionNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    select(seedAttributes(SelectionNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes1tmp, IntNodes1, IntSeedAttrs1, IntSeedAttrs2,
                                             IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    select(outputAttributes(SplitNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(randomAttributes(SplitNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    select(seedAttributes(SplitNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    identifyNodeSeedOutputRandomAttributes(SplitNodeID, GraphOut, IntNodes1, IntNodesOut, IntSeedAttrs3, IntSeedAttrs4,
                                             IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(SplitNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    % ****

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, SelectionNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SelectionOuterRelations),
    findall(outerRelation(VGWrapperNodeID, SplitNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SplitOuterRelations),
    subtractSet(OuterRelationsIn, SelectionOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, SplitOuterRelations, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushSelectionDownSplit(N)) ->
        retract(appliedTransformation(pushSelectionDownSplit(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSelectionDownSplit(NN)))
        ;
        assert(appliedTransformation(pushSelectionDownSplit(1)))).

%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING A SELECTION PAST THE COMMON OUTER RELATION OF A SET OF VGWRAPPERS
% Returns true if (GraphOut, NodesOut) can legally result from pushing a join past a set of vgwrappers in the query plan
% represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(copyJoinThroughVGWrapperBranch),

    % **** pick a VGWrapper
    member(vgwrapper(VGWrapperNodeID, _, _, VGSeedAttrIn, _, _, _, _, _), NodesIn),

    % **** and find its main relation
    member(outerRelation(VGWrapperNodeID, VGOuterRelationNode), OuterRelationsIn),
    findVGWrapperBaseOuterRelation(VGOuterRelationNode, BaseOuterRelation, GraphIn),

    % **** as well as its top seed join
    findTopLevelSeedJoin(VGWrapperNodeID, TopLevelSeedJoin, GraphIn, NodesIn, VGSeedAttrIn),

    % **** check if there exists a join operator along the path [BaseOuterRelation...TopLevelSeedJoin],
    % **** which is parent of the BaseOuterRelation
    member(parent(JoinNodeID, BaseOuterRelation), GraphIn),
    member(join(JoinNodeID, _, _), NodesIn),
    JoinNodeID \= TopLevelSeedJoin,
    isDescendantNoVGWrapper(JoinNodeID, TopLevelSeedJoin, GraphIn, NodesIn),
    nodeHasSingleParent(JoinNodeID, GraphIn),

    % **** find all the related VGWrappers in that branch
    findall(VGWNodeID, (member(vgwrapper(VGWNodeID, _, _, VGSeedAttrIn, _, _, _, _, _), NodesIn),
                                        member(outerRelation(VGWNodeID, VGWOuterRelNode), OuterRelationsIn),
                                        isDescendant(VGWNodeID, TopLevelSeedJoin, GraphIn),
                                        findVGWrapperBaseOuterRelation(VGWOuterRelNode, BaseOuterRelation, GraphIn)), IntVGWNodes),
    removeDuplicates(IntVGWNodes, [], VGWNodes),

    % **** ensure that  this rule is fired once per VGWrapper branch
    select(VGWrapperNodeID, VGWNodes, RestVGWNodes),
    rankNodes(RestVGWNodes, VGWrapperNodeID),

    findall(VGWOuterNodeID, (member(parent(VGWOuterNodeID, BaseOuterRelation), GraphIn), member(VGWNodeID, VGWNodes),
                                       (VGWOuterNodeID = VGWNodeID ; isDescendant(VGWOuterNodeID, VGWNodeID, GraphIn))), IntVGWOuterNodes),
    removeDuplicates(IntVGWOuterNodes, [], VGWOuterNodes),

    % **** find BaseOuterRelation's parents
    select(parent(JoinNodeID, BaseOuterRelation), GraphIn, IntGraph1),
    findall(parent(BaseOuterRelationParent, BaseOuterRelation),
         member(parent(BaseOuterRelationParent, BaseOuterRelation), IntGraph1), BaseOuterRelationParents),
    swapNodeParents(BaseOuterRelationParents, BaseOuterRelation, JoinNodeID, IntGraph1, IntGraph2, NodesIn, IntNodes1),
    append([parent(JoinNodeID, BaseOuterRelation)], IntGraph2, IntGraph3),

    % **** update outer relation info if necessary
    findall(outerRelation(AnyVGWrapperNodeID, BaseOuterRelation), member(outerRelation(AnyVGWrapperNodeID, BaseOuterRelation), OuterRelationsIn), BaseOuterRelationOuterRelations),
    findall(outerRelation(AnyVGWrapperNodeID, JoinNodeID), member(outerRelation(AnyVGWrapperNodeID, BaseOuterRelation), OuterRelationsIn), JoinOuterRelations),
    subtractSet(OuterRelationsIn, BaseOuterRelationOuterRelations, IntOuterRelations1),
    mergeSets(IntOuterRelations1, JoinOuterRelations, IntOuterRelations2),

    % **** identify new candidate output attributes for join node
    select(outputAttributes(JoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(randomAttributes(JoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    select(seedAttributes(JoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    identifyNodeSeedOutputRandomAttributes(JoinNodeID, IntGraph3, IntNodes1, IntNodes2,
                                                  IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1,
                                                  IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),
    
    % **** add appropriate dedup operators
    createDedupNodesAtVGWrappers(VGWOuterNodes, VGSeedAttrIn, JoinNodeID, IntNodes2, IntNodes3, IntGraph3, IntGraph4,
                                                IntOuterRelations2, IntOuterRelations3, IntOutputAttrs2,
                                                IntOutputAttrs3, IntSeedAttrs2, IntSeedAttrs3, IntRandomAttrs2, IntRandomAttrs3),

    % **** project out any unecessary attributes
    updateOutputAttributes(IntGraph4, GraphOut, IntNodes3, IntNodesOut, IntOutputAttrs3, OutputAttrsOut, IntRandomAttrs3,
                                            RandomAttrsOut, IntSeedAttrs3, SeedAttrsOut, IntOuterRelations3, OuterRelationsOut),

    %**** update candidate key information for entire plan
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut),
                                           member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut,
                                           OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(copyJoinThroughVGWrapperBranch(N)) ->
        retract(appliedTransformation(copyJoinThroughVGWrapperBranch(N))),
        NN is N + 1,
        assert(appliedTransformation(copyJoinThroughVGWrapperBranch(NN)))
        ;
        assert(appliedTransformation(copyJoinThroughVGWrapperBranch(1)))).

%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN A SEMIJOIN OPERATOR PAST A JOIN
% Returns true if (GraphOut, NodesOut) can legally result from pushing a semijoin operator past a join
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSemiJoinDownJoin),

    %**** find a semijoin operator that is above some join
    select(semijoin(SemiJoinNodeID, JoinNodeID, SemiJoinPreds), NodesIn, IntNodes1),
    select(parent(SemiJoinNodeID, JoinNodeID), GraphIn, IntGraph1),
    select(parent(SemiJoinNodeID, OtherSemiJoinChild), IntGraph1, IntGraph1b),
    member(join(JoinNodeID, _, _), IntNodes1),

    %**** rule applies only to nodes with one parent
    nodeHasSingleParent(JoinNodeID, GraphIn),

    %**** find the two children of the join
    member(parent(JoinNodeID, JoinLHSChildID), GraphIn),
    member(parent(JoinNodeID, JoinRHSChildID), GraphIn),
    %**** it is the same transformation if we switch the LHS and RHS
    JoinLHSChildID @> JoinRHSChildID,

    %**** find all attrs that are reachable from either the LHS or the RHS of the join
    member(outputAttributes(JoinLHSChildID, LHSChildOutputAttrs), OutputAttrsIn),
    member(outputAttributes(JoinRHSChildID, RHSChildOutputAttrs), OutputAttrsIn),

    %**** use those attributes to partition all of the semijoin predicates into two subsets: those that
    %**** can be pushed down to the left and those that can be pushed to the right child of the join
    partitionSemiOrAntiJoinPredsOverJoin(SemiJoinPreds, [], LHSPredsOut, [], RHSPredsOut, LHSChildOutputAttrs, RHSChildOutputAttrs),
    %*** transformation is only valid when all the predicates can be pushed down only one path
    (LHSPredsOut = [] ; RHSPredsOut = []),
    
    % **** find parents of semijoin node
    setof(parent(SemiJoinParentID, SemiJoinNodeID),
               member(parent(SemiJoinParentID, SemiJoinNodeID), IntGraph1b), SemiJoinNodeParents),
    % **** and make them the parents of the join node
    swapNodeParents(SemiJoinNodeParents, SemiJoinNodeID, JoinNodeID, IntGraph1b, IntGraph2, IntNodes1, IntNodes2),

    % **** delete semijoin node info from the plan
    select(outputAttributes(SemiJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(randomAttributes(SemiJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    select(seedAttributes(SemiJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, SemiJoinNodeID), member(outerRelation(VGWrapperNodeID, SemiJoinNodeID), OuterRelationsIn), SemiJoinOuterRelations),
    findall(outerRelation(VGWrapperNodeID, JoinNodeID), member(outerRelation(VGWrapperNodeID, SemiJoinNodeID), OuterRelationsIn), JoinOuterRelations),
    subtractSet(OuterRelationsIn, SemiJoinOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, JoinOuterRelations, OuterRelationsOut),

    %**** add new semijoin operations on the LHS and the RHS of the join, if needed
    (LHSPredsOut = [] ->
        IntNodes4 = IntNodes2,
        IntGraph6 = IntGraph2,

        IntOutputAttrs2 = IntOutputAttrs1,
        IntRandomAttrs2 = IntRandomAttrs1,
        IntSeedAttrs2 = IntSeedAttrs1
        ;
        gensym(semijoinNode, LHSSemiJoinNodeID),

        select(parent(JoinNodeID, JoinLHSChildID), IntGraph2, IntGraph3),
        append([parent(JoinNodeID, LHSSemiJoinNodeID)], IntGraph3, IntGraph4),
        append([parent(LHSSemiJoinNodeID, JoinLHSChildID)], IntGraph4, IntGraph5),
        append([parent(LHSSemiJoinNodeID, OtherSemiJoinChild)], IntGraph5, IntGraph6),

        append([semijoin(LHSSemiJoinNodeID, JoinLHSChildID, LHSPredsOut)], IntNodes2, IntNodes3),

        % **** insert output, seed and random attrs
        identifyNodeSeedOutputRandomAttributes(LHSSemiJoinNodeID, IntGraph6, IntNodes3, IntNodes4,
                         IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2)
    ),

    (RHSPredsOut = [] ->
        IntNodes6 = IntNodes4,
        GraphOut = IntGraph6,

        IntOutputAttrs3 = IntOutputAttrs2,
        IntRandomAttrs3 = IntRandomAttrs2,
        IntSeedAttrs3 = IntSeedAttrs2
        ;
        gensym(semijoinNode, RHSSemiJoinNodeID),

        select(parent(JoinNodeID, JoinRHSChildID), IntGraph6, IntGraph7),
        append([parent(JoinNodeID, RHSSemiJoinNodeID)], IntGraph7, IntGraph8),
        append([parent(RHSSemiJoinNodeID, JoinRHSChildID)], IntGraph8, IntGraph9),
        append([parent(RHSSemiJoinNodeID, OtherSemiJoinChild)], IntGraph9, GraphOut),

        append([semijoin(RHSSemiJoinNodeID, JoinRHSChildID, RHSPredsOut)], IntNodes4, IntNodes5),

        % **** insert output, seed and random attrs
        identifyNodeSeedOutputRandomAttributes(RHSSemiJoinNodeID, GraphOut, IntNodes5, IntNodes6,
                         IntSeedAttrs2, IntSeedAttrs3, IntRandomAttrs2, IntRandomAttrs3, IntOutputAttrs2, IntOutputAttrs3)
    ),

    % **** update seed, output and random attributes for join
    select(randomAttributes(JoinNodeID, _), IntRandomAttrs3, IntRandomAttrs4),
    select(seedAttributes(JoinNodeID, _), IntSeedAttrs3, IntSeedAttrs4),
    select(outputAttributes(JoinNodeID, _), IntOutputAttrs3, IntOutputAttrs4),
    identifyNodeSeedOutputRandomAttributes(JoinNodeID, GraphOut, IntNodes6, IntNodesOut,
                         IntSeedAttrs4, IntSeedAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntOutputAttrs4, IntOutputAttrs5),


    updateNodeSeedOutputRandomAttributes(JoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, IntOutputAttrs6, IntRandomAttrs5, IntRandomAttrs6, IntSeedAttrs5, IntSeedAttrs6),

    (RHSPredsOut = [] ->
        IntOutputAttrs7 = IntOutputAttrs6,
        IntRandomAttrs7 = IntRandomAttrs6,
        IntSeedAttrs7 = IntSeedAttrs6
        ;
        updateNodeSeedOutputRandomAttributes(RHSSemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs6, IntOutputAttrs7, IntRandomAttrs6, IntRandomAttrs7, IntSeedAttrs6, IntSeedAttrs7)
    ),

    (LHSPredsOut = [] ->
        OutputAttrsOut = IntOutputAttrs7,
        RandomAttrsOut = IntRandomAttrs7,
        SeedAttrsOut = IntSeedAttrs7
        ;
        updateNodeSeedOutputRandomAttributes(LHSSemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs7, OutputAttrsOut, IntRandomAttrs7, RandomAttrsOut, IntSeedAttrs7, SeedAttrsOut)
    ),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushSemiJoinDownJoin(N)) ->
        retract(appliedTransformation(pushSemiJoinDownJoin(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSemiJoinDownJoin(NN)))
        ;
        assert(appliedTransformation(pushSemiJoinDownJoin(1)))).

%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN A ANTIJOIN OPERATOR PAST A JOIN
% Returns true if (GraphOut, NodesOut) can legally result from pushing a antijoin operator past a join
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushAntiJoinDownJoin),

    %**** find a antijoin operator that is above some join
    select(antijoin(AntiJoinNodeID, JoinNodeID, AntiJoinPreds), NodesIn, IntNodes1),
    select(parent(AntiJoinNodeID, JoinNodeID), GraphIn, IntGraph1),
    select(parent(AntiJoinNodeID, OtherAntiJoinChild), IntGraph1, IntGraph1b),
    member(join(JoinNodeID, _, _), IntNodes1),

    %**** rule applies only to nodes with one parent
    nodeHasSingleParent(JoinNodeID, GraphIn),

    %**** find the two children of the join
    member(parent(JoinNodeID, JoinLHSChildID), GraphIn),
    member(parent(JoinNodeID, JoinRHSChildID), GraphIn),
    %**** it is the same transformation if we switch the LHS and RHS
    JoinLHSChildID @> JoinRHSChildID,

    %**** find all attrs that are reachable from either the LHS or the RHS of the join
    member(outputAttributes(JoinLHSChildID, LHSChildOutputAttrs), OutputAttrsIn),
    member(outputAttributes(JoinRHSChildID, RHSChildOutputAttrs), OutputAttrsIn),

    %**** use those attributes to partition all of the antijoin predicates into two subsets: those that
    %**** can be pushed down to the left and those that can be pushed to the right child of the join
    partitionSemiOrAntiJoinPredsOverJoin(AntiJoinPreds, [], LHSPredsOut, [], RHSPredsOut, LHSChildOutputAttrs, RHSChildOutputAttrs),
    
    % **** transformation is only valid if predicates can be all pushed down only one path
    (LHSPredsOut = [] ; RHSPredsOut = []),

    % **** find parents of antijoin node
    setof(parent(AntiJoinParentID, AntiJoinNodeID),
               member(parent(AntiJoinParentID, AntiJoinNodeID), IntGraph1b), AntiJoinNodeParents),
    % **** and make them the parents of the join node
    swapNodeParents(AntiJoinNodeParents, AntiJoinNodeID, JoinNodeID, IntGraph1b, IntGraph2, IntNodes1, IntNodes2),

    % **** delete antijoin node info from the plan
    select(outputAttributes(AntiJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(randomAttributes(AntiJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    select(seedAttributes(AntiJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, AntiJoinNodeID), member(outerRelation(VGWrapperNodeID, AntiJoinNodeID), OuterRelationsIn), AntiJoinOuterRelations),
    findall(outerRelation(VGWrapperNodeID, JoinNodeID), member(outerRelation(VGWrapperNodeID, AntiJoinNodeID), OuterRelationsIn), JoinOuterRelations),
    subtractSet(OuterRelationsIn, AntiJoinOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, JoinOuterRelations, OuterRelationsOut),

    %**** add new antijoin operations on the LHS and the RHS of the join, if needed
    (LHSPredsOut = [] ->
        IntNodes4 = IntNodes2,
        IntGraph6 = IntGraph2,

        IntOutputAttrs2 = IntOutputAttrs1,
        IntRandomAttrs2 = IntRandomAttrs1,
        IntSeedAttrs2 = IntSeedAttrs1
        ;
        gensym(antijoinNode, LHSAntiJoinNodeID),

        select(parent(JoinNodeID, JoinLHSChildID), IntGraph2, IntGraph3),
        append([parent(JoinNodeID, LHSAntiJoinNodeID)], IntGraph3, IntGraph4),
        append([parent(LHSAntiJoinNodeID, JoinLHSChildID)], IntGraph4, IntGraph5),
        append([parent(LHSAntiJoinNodeID, OtherAntiJoinChild)], IntGraph5, IntGraph6),

        append([antijoin(LHSAntiJoinNodeID, JoinLHSChildID, LHSPredsOut)], IntNodes2, IntNodes3),

        % **** insert output, seed and random attrs
        identifyNodeSeedOutputRandomAttributes(LHSAntiJoinNodeID, IntGraph6, IntNodes3, IntNodes4,
                         IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2)
    ),

    (RHSPredsOut = [] ->
        IntNodes6 = IntNodes4,
        GraphOut = IntGraph6,

        IntOutputAttrs3 = IntOutputAttrs2,
        IntRandomAttrs3 = IntRandomAttrs2,
        IntSeedAttrs3 = IntSeedAttrs2
        ;
        gensym(antijoinNode, RHSAntiJoinNodeID),

        select(parent(JoinNodeID, JoinRHSChildID), IntGraph6, IntGraph7),
        append([parent(JoinNodeID, RHSAntiJoinNodeID)], IntGraph7, IntGraph8),
        append([parent(RHSAntiJoinNodeID, JoinRHSChildID)], IntGraph8, IntGraph9),
        append([parent(RHSAntiJoinNodeID, OtherAntiJoinChild)], IntGraph9, GraphOut),

        append([antijoin(RHSAntiJoinNodeID, JoinRHSChildID, RHSPredsOut)], IntNodes4, IntNodes5),

        % **** insert output, seed and random attrs
        identifyNodeSeedOutputRandomAttributes(RHSAntiJoinNodeID, GraphOut, IntNodes5, IntNodes6,
                         IntSeedAttrs2, IntSeedAttrs3, IntRandomAttrs2, IntRandomAttrs3, IntOutputAttrs2, IntOutputAttrs3)
    ),

    % **** update seed, output and random attributes for join
    select(randomAttributes(JoinNodeID, _), IntRandomAttrs3, IntRandomAttrs4),
    select(seedAttributes(JoinNodeID, _), IntSeedAttrs3, IntSeedAttrs4),
    select(outputAttributes(JoinNodeID, _), IntOutputAttrs3, IntOutputAttrs4),
    identifyNodeSeedOutputRandomAttributes(JoinNodeID, GraphOut, IntNodes6, IntNodesOut,
                         IntSeedAttrs4, IntSeedAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntOutputAttrs4, IntOutputAttrs5),


    updateNodeSeedOutputRandomAttributes(JoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, IntOutputAttrs6, IntRandomAttrs5, IntRandomAttrs6, IntSeedAttrs5, IntSeedAttrs6),

    (RHSPredsOut = [] ->
        IntOutputAttrs7 = IntOutputAttrs6,
        IntRandomAttrs7 = IntRandomAttrs6,
        IntSeedAttrs7 = IntSeedAttrs6
        ;
        updateNodeSeedOutputRandomAttributes(RHSAntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs6, IntOutputAttrs7, IntRandomAttrs6, IntRandomAttrs7, IntSeedAttrs6, IntSeedAttrs7)
    ),

    (LHSPredsOut = [] ->
        OutputAttrsOut = IntOutputAttrs7,
        RandomAttrsOut = IntRandomAttrs7,
        SeedAttrsOut = IntSeedAttrs7
        ;
        updateNodeSeedOutputRandomAttributes(LHSAntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs7, OutputAttrsOut, IntRandomAttrs7, RandomAttrsOut, IntSeedAttrs7, SeedAttrsOut)
    ),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushAntiJoinDownJoin(N)) ->
        retract(appliedTransformation(pushAntiJoinDownJoin(N))),
        NN is N + 1,
        assert(appliedTransformation(pushAntiJoinDownJoin(NN)))
        ;
        assert(appliedTransformation(pushAntiJoinDownJoin(1)))).

%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING A ANTIJOIN PAST THE COMMON OUTER RELATION OF A SET OF VGWRAPPERS
% Returns true if (GraphOut, NodesOut) can legally result from pushing a antijoin past a set of vgwrappers in the query plan
% represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(copyAntiJoinThroughVGWrapperBranch),

    % **** pick a VGWrapper
    member(vgwrapper(VGWrapperNodeID, _, _, VGSeedAttrIn, _, _, _, _, _), NodesIn),

    % **** and find its main relation
    member(outerRelation(VGWrapperNodeID, VGOuterRelationNode), OuterRelationsIn),
    findVGWrapperBaseOuterRelation(VGOuterRelationNode, BaseOuterRelation, GraphIn),

    % **** as well as its top seed join
    findTopLevelSeedJoin(VGWrapperNodeID, TopLevelSeedJoin, GraphIn, NodesIn, VGSeedAttrIn),

    % **** check if there exists a antijoin operator along the path [BaseOuterRelation...TopLevelSeedJoin],
    % **** which is parent of the BaseOuterRelation
    member(parent(AntiJoinNode, BaseOuterRelation), GraphIn),
    member(antijoin(AntiJoinNode, BaseOuterRelation, _), NodesIn),
    isDescendantNoVGWrapper(AntiJoinNode, TopLevelSeedJoin, GraphIn, NodesIn),
    nodeHasSingleParent(AntiJoinNode, GraphIn),

    % **** find all the related VGWrappers in that branch
    findall(VGWNodeID, (member(vgwrapper(VGWNodeID, _, _, VGSeedAttrIn, _, _, _, _, _), NodesIn),
                                        member(outerRelation(VGWNodeID, VGWOuterRelNode), OuterRelationsIn),
                                        isDescendant(VGWNodeID, TopLevelSeedJoin, GraphIn),
                                        findVGWrapperBaseOuterRelation(VGWOuterRelNode, BaseOuterRelation, GraphIn)), IntVGWNodes),
    removeDuplicates(IntVGWNodes, [], VGWNodes),

    % **** ensure that  this rule is fired once per VGWrapper branch
    select(VGWrapperNodeID, VGWNodes, RestVGWNodes),
    rankNodes(RestVGWNodes, VGWrapperNodeID),

    %findall(VGWOuterNodeID, (member(parent(VGWOuterNodeID, BaseOuterRelation), GraphIn), member(VGWNodeID, VGWNodes),
    %                                   (VGWOuterNodeID = VGWNodeID ; isDescendant(VGWOuterNodeID, VGWNodeID, GraphIn))), IntVGWOuterNodes),
    %removeDuplicates(IntVGWOuterNodes, [], VGWOuterNodes),

    % **** find BaseOuterRelation's parents
    select(parent(AntiJoinNode, BaseOuterRelation), GraphIn, IntGraph1),
    setof(parent(BaseOuterRelationParent, BaseOuterRelation),
         member(parent(BaseOuterRelationParent, BaseOuterRelation), IntGraph1), BaseOuterRelationParents),
    swapNodeParents(BaseOuterRelationParents, BaseOuterRelation, AntiJoinNode, IntGraph1, IntGraph2, NodesIn, IntNodes1),
    append([parent(AntiJoinNode, BaseOuterRelation)], IntGraph2, IntGraph3),

    % **** update outer relation info if necessary
    findall(outerRelation(AnyVGWrapperNodeID, BaseOuterRelation), member(outerRelation(AnyVGWrapperNodeID, BaseOuterRelation), OuterRelationsIn), BaseOuterRelationOuterRelations),
    findall(outerRelation(AnyVGWrapperNodeID, AntiJoinNode), member(outerRelation(AnyVGWrapperNodeID, BaseOuterRelation), OuterRelationsIn), AntiJoinOuterRelations),
    subtractSet(OuterRelationsIn, BaseOuterRelationOuterRelations, IntOuterRelations1),
    mergeSets(IntOuterRelations1, AntiJoinOuterRelations, IntOuterRelations2),

    % **** identify new candidate output attributes for antijoin node
    select(outputAttributes(AntiJoinNode, _), OutputAttrsIn, IntOutputAttrs1),
    select(randomAttributes(AntiJoinNode, _), RandomAttrsIn, IntRandomAttrs1),
    select(seedAttributes(AntiJoinNode, _), SeedAttrsIn, IntSeedAttrs1),
    identifyNodeSeedOutputRandomAttributes(AntiJoinNode, IntGraph3, IntNodes1, IntNodes2,
                                                  IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1,
                                                  IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    % **** add appropriate dedup operators
    %createDedupNodesAtVGWrappers(VGWOuterNodes, VGSeedAttrIn, AntiJoinNode, IntNodes2, IntNodes3, IntGraph3, IntGraph4,
    %                                            IntOuterRelations2, IntOuterRelations3, IntOutputAttrs2,
    %                                            IntOutputAttrs3, IntSeedAttrs2, IntSeedAttrs3, IntRandomAttrs2, IntRandomAttrs3),

    % **** project out any unecessary attributes
    updateOutputAttributes(IntGraph3, GraphOut, IntNodes2, IntNodesOut, IntOutputAttrs2, OutputAttrsOut,
                      IntRandomAttrs2, RandomAttrsOut, IntSeedAttrs2, SeedAttrsOut, IntOuterRelations2, OuterRelationsOut),

    %**** update candidate key information for entire plan
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut),
                                           member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut,
                                           OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(copyAntiJoinThroughVGWrapperBranch(N)) ->
        retract(appliedTransformation(copyAntiJoinThroughVGWrapperBranch(N))),
        NN is N + 1,
        assert(appliedTransformation(copyAntiJoinThroughVGWrapperBranch(NN)))
        ;
        assert(appliedTransformation(copyAntiJoinThroughVGWrapperBranch(1)))).

%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PULLING A SEED ABOVE A JOIN
% Returns true if (GraphOut, NodesOut) can legally result from pulling a seed operator above a join operator
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pullSeedUpJoin),

    %**** find a seed operator that is below a join
    member(seed(SeedNodeID, SeedAttr), NodesIn),
    select(parent(JoinNodeID, SeedNodeID), GraphIn, IntGraph1),
    member(join(JoinNodeID, JoinPreds, _), NodesIn),

    %**** rule applies only if join node has one parent
    nodeHasSingleParent(SeedNodeID, GraphIn),

    % **** rule applied if seed attribute is not referenced in join predicates
    extractAttrsFromPredicates(JoinPreds, [], JoinPredReferencedAttrs),
    not(member(SeedAttr, JoinPredReferencedAttrs)),

    % **** find candidate keys of join and seed
    setof(SeedCandidateKey, member(candidateKey(SeedNodeID, SeedCandidateKey), KeysIn), SeedCandidateKeys),
    setof(JoinCandidateKey, member(candidateKey(JoinNodeID, JoinCandidateKey), KeysIn), JoinCandidateKeys),

    % **** rule can be applied if join is 1-N with '1' to the child under consideration
    subset(SeedCandidateKeys, JoinCandidateKeys),

    % **** re-arrange query graph
    select(parent(SeedNodeID, SeedChildID), IntGraph1, IntGraph2),
    setof(parent(JoinParentID, JoinNodeID),
        member(parent(JoinParentID, JoinNodeID), IntGraph2), JoinNodeParents),
    swapNodeParents(JoinNodeParents, JoinNodeID, SeedNodeID, IntGraph2, IntGraph3, NodesIn, IntNodes1tmp),
    append([parent(SeedNodeID, JoinNodeID)], IntGraph3, IntGraph4),
    append([parent(JoinNodeID, SeedChildID)], IntGraph4, GraphOut),

    % ***** update output, seed and random attributes of join node
    select(outputAttributes(JoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(seedAttributes(JoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    select(randomAttributes(JoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    identifyNodeSeedOutputRandomAttributes(JoinNodeID, GraphOut, IntNodes1tmp, IntNodes1,
                                                  IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    % ***** update output, seed and random attributes of seed node
    select(outputAttributes(SeedNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(seedAttributes(SeedNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    select(randomAttributes(SeedNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    identifyNodeSeedOutputRandomAttributes(SeedNodeID, GraphOut, IntNodes1, IntNodesOut,
                                                  IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(SeedNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(JoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    
    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, JoinNodeID), member(outerRelation(VGWrapperNodeID, JoinNodeID), OuterRelationsIn), JoinOuterRelations),
    findall(outerRelation(VGWrapperNodeID, SeedNodeID), member(outerRelation(VGWrapperNodeID, JoinNodeID), OuterRelationsIn), SeedOuterRelations),
    subtractSet(OuterRelationsIn, JoinOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, SeedOuterRelations, OuterRelationsOut),

    %**** update candidate key information for entire plan
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut),
                                           member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut,
                                           OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pullSeedUpJoin(N)) ->
        retract(appliedTransformation(pullSeedUpJoin(N))),
        NN is N + 1,
        assert(appliedTransformation(pullSeedUpJoin(NN)))
        ;
        assert(appliedTransformation(pullSeedUpJoin(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSH OF A ANTIJOIN DOWN A GENERALIZED AGGREGATE
% Returns true if (GraphOut, NodesOut) can legally result from pushing a antijoin operator past a generalized aggregate operator
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushAntiJoinDownGenAgg),

    %**** find a antijoin operator that is above a generalized aggregate
    member(antijoin(AntiJoinNodeID, GenAggNodeID, AntiJoinPreds), NodesIn),
    select(parent(AntiJoinNodeID, GenAggNodeID), GraphIn, IntGraph1),
    member(genagg(GenAggNodeID, _, GroupByAttrs, _, _, AggExprsOutAttrs), NodesIn),

    % **** rule applies if group by attributes are not null
    GroupByAttrs \= [],

    %**** rule applies only if lower generalized aggregate node has one parent
    nodeHasSingleParent(GenAggNodeID, GraphIn),

    % **** find which of the group by attributes are referenced by the antijoin predicates
    flatten(AggExprsOutAttrs, FlattenedAggExprsOutAttrs),
    partitionPredsBasedOnGenAggAttrs(FlattenedAggExprsOutAttrs, AntiJoinPreds, [], TopAntiJoinPreds, [], BtmAntiJoinPreds),

    % **** rule holds if antijoin predicates reference group by attrs
    BtmAntiJoinPreds \= [],

    (TopAntiJoinPreds = [] ->
        % **** move antijoin predicate past generalized aggregate
        select(parent(GenAggNodeID, GenAggNodeChildNode), IntGraph1, IntGraph2),
        setof(parent(AntiJoinParentID, AntiJoinNodeID),
            member(parent(AntiJoinParentID, AntiJoinNodeID), IntGraph2), AntiJoinNodeParents),
        swapNodeParents(AntiJoinNodeParents, AntiJoinNodeID, GenAggNodeID, IntGraph2, IntGraph3, NodesIn, IntNodes1),
        append([parent(GenAggNodeID, AntiJoinNodeID)], IntGraph3, IntGraph4),
        append([parent(AntiJoinNodeID, GenAggNodeChildNode)], IntGraph4, GraphOut),

        select(antijoin(AntiJoinNodeID, _, _), IntNodes1, IntNodes2),
        append([antijoin(AntiJoinNodeID, GenAggNodeChildNode, TopAntiJoinPreds)], IntNodes2, IntNodes3),

        % **** update output, seed, random attributes for swapped nodes
        select(outputAttributes(AntiJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
        select(seedAttributes(AntiJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
        select(randomAttributes(AntiJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
        identifyNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodes3, IntNodes4,
                                                  IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

        select(outputAttributes(GenAggNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
        select(seedAttributes(GenAggNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
        select(randomAttributes(GenAggNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
        identifyNodeSeedOutputRandomAttributes(GenAggNodeID, GraphOut, IntNodes4, IntNodesOut,
                                                  IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

        updateNodeSeedOutputRandomAttributes(GenAggNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
        updateNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
        % ****

        % **** update outer relation info if necessary
        findall(outerRelation(VGWrapperNodeID, AntiJoinNodeID), member(outerRelation(VGWrapperNodeID, AntiJoinNodeID), OuterRelationsIn), AntiJoinOuterRelations),
        findall(outerRelation(VGWrapperNodeID, GenAggNodeID), member(outerRelation(VGWrapperNodeID, AntiJoinNodeID), OuterRelationsIn), GenAggOuterRelations),
        subtractSet(OuterRelationsIn, AntiJoinOuterRelations, IntOuterRelations),
        mergeSets(IntOuterRelations, GenAggOuterRelations, OuterRelationsOut)
        ;
        select(antijoin(AntiJoinNodeID, _, _), NodesIn, IntNodes1),
        append([antijoin(AntiJoinNodeID, GenAggNodeID, TopAntiJoinPreds)], IntNodes1, IntNodes2),

        % **** create a new antijoin predicate past generalized aggregate
        gensym(antiJoinNode, BtmAntiJoinNodeID),
        select(parent(GenAggNodeID, GenAggNodeChildNode), IntGraph1, IntGraph2),
        append([parent(GenAggNodeID, BtmAntiJoinNodeID)], IntGraph2, IntGraph3),
        append([parent(AntiJoinNodeID, GenAggNodeID)], IntGraph3, IntGraph4),
        append([parent(BtmAntiJoinNodeID, GenAggNodeChildNode)], IntGraph4, IntGraph5),
        member(parent(AntiJoinNodeID, OtherAntiJoinChild), IntGraph1),
        append([parent(BtmAntiJoinNodeID, OtherAntiJoinChild)], IntGraph5, GraphOut),

        append([antijoin(BtmAntiJoinNodeID, GenAggNodeChildNode, BtmAntiJoinPreds)], IntNodes2, IntNodes3),

        % **** add output, seed and attributes for btm antijoin node
        identifyNodeSeedOutputRandomAttributes(BtmAntiJoinNodeID, GraphOut, IntNodes3, IntNodes4,
                                                  SeedAttrsIn, IntSeedAttrs1, RandomAttrsIn, IntRandomAttrs1, OutputAttrsIn, IntOutputAttrs1),

        % **** update output, seed, random attributes for generalized aggregate node
        select(outputAttributes(GenAggNodeID, _), IntOutputAttrs1, IntOutputAttrs2),
        select(seedAttributes(GenAggNodeID, _), IntSeedAttrs1, IntSeedAttrs2),
        select(randomAttributes(GenAggNodeID, _), IntRandomAttrs1, IntRandomAttrs2),
        identifyNodeSeedOutputRandomAttributes(GenAggNodeID, GraphOut, IntNodes4, IntNodes5,
                                                  IntSeedAttrs2, IntSeedAttrs3, IntRandomAttrs2, IntRandomAttrs3, IntOutputAttrs2, IntOutputAttrs3),

        % **** update output, seed, random attributes for generalized aggregate node
        select(outputAttributes(AntiJoinNodeID, _), IntOutputAttrs3, IntOutputAttrs4),
        select(seedAttributes(AntiJoinNodeID, _), IntSeedAttrs3, IntSeedAttrs4),
        select(randomAttributes(AntiJoinNodeID, _), IntRandomAttrs3, IntRandomAttrs4),
        identifyNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodes5, IntNodesOut,
                                                  IntSeedAttrs4, IntSeedAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntOutputAttrs4, IntOutputAttrs5),

        updateNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, IntOutputAttrs6, IntRandomAttrs5, IntRandomAttrs6, IntSeedAttrs5, IntSeedAttrs6),
        updateNodeSeedOutputRandomAttributes(GenAggNodeID, GraphOut, IntNodesOut, IntOutputAttrs6, IntOutputAttrs7, IntRandomAttrs6, IntRandomAttrs7, IntSeedAttrs6, IntSeedAttrs7),
        updateNodeSeedOutputRandomAttributes(BtmAntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs7, OutputAttrsOut, IntRandomAttrs7, RandomAttrsOut, IntSeedAttrs7, SeedAttrsOut),

        OuterRelationsOut = OuterRelationsIn
    ),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushAntiJoinDownGenAgg(N)) ->
        retract(appliedTransformation(pushAntiJoinDownGenAgg(N))),
        NN is N + 1,
        assert(appliedTransformation(pushAntiJoinDownGenAgg(NN)))
        ;
        assert(appliedTransformation(pushAntiJoinDownGenAgg(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSH OF A SEMIJOIN DOWN A GENERALIZED AGGREGATE
% Returns true if (GraphOut, NodesOut) can legally result from pushing a semijoin operator past a generalized aggregate operator
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSemiJoinDownGenAgg),

    %**** find a semijoin operator that is above a generalized aggregate
    member(semijoin(SemiJoinNodeID, GenAggNodeID, SemiJoinPreds), NodesIn),
    select(parent(SemiJoinNodeID, GenAggNodeID), GraphIn, IntGraph1),
    member(genagg(GenAggNodeID, _, GroupByAttrs, _, _, AggExprsOutAttrs), NodesIn),

    % **** rule applies if group by attributes are not null
    GroupByAttrs \= [],

    %**** rule applies only if lower generalized aggregate node has one parent
    nodeHasSingleParent(GenAggNodeID, GraphIn),

    % **** find which of the group by attributes are referenced by the semijoin predicates
    flatten(AggExprsOutAttrs, FlattenedAggExprsOutAttrs),
    partitionPredsBasedOnGenAggAttrs(FlattenedAggExprsOutAttrs, SemiJoinPreds, [], TopSemiJoinPreds, [], BtmSemiJoinPreds),

    % **** rule holds if semijoin predicates reference group by attrs
    BtmSemiJoinPreds \= [],

    (TopSemiJoinPreds = [] ->
        % **** move semijoin predicate past generalized aggregate
        select(parent(GenAggNodeID, GenAggNodeChildNode), IntGraph1, IntGraph2),
        setof(parent(SemiJoinParentID, SemiJoinNodeID),
            member(parent(SemiJoinParentID, SemiJoinNodeID), IntGraph2), SemiJoinNodeParents),
        swapNodeParents(SemiJoinNodeParents, SemiJoinNodeID, GenAggNodeID, IntGraph2, IntGraph3, NodesIn, IntNodes1),
        append([parent(GenAggNodeID, SemiJoinNodeID)], IntGraph3, IntGraph4),
        append([parent(SemiJoinNodeID, GenAggNodeChildNode)], IntGraph4, GraphOut),

        select(semijoin(SemiJoinNodeID, _, _), IntNodes1, IntNodes2),
        append([semijoin(SemiJoinNodeID, GenAggNodeChildNode, TopSemiJoinPreds)], IntNodes2, IntNodes3),
        
        % **** update output, seed, random attributes for swapped nodes
        select(outputAttributes(SemiJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
        select(seedAttributes(SemiJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
        select(randomAttributes(SemiJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
        identifyNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodes3, IntNodes4,
                                                  IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

        select(outputAttributes(GenAggNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
        select(seedAttributes(GenAggNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
        select(randomAttributes(GenAggNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
        identifyNodeSeedOutputRandomAttributes(GenAggNodeID, GraphOut, IntNodes4, IntNodesOut,
                                                  IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

        updateNodeSeedOutputRandomAttributes(GenAggNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
        updateNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
        % ****

        % **** update outer relation info if necessary
        findall(outerRelation(VGWrapperNodeID, SemiJoinNodeID), member(outerRelation(VGWrapperNodeID, SemiJoinNodeID), OuterRelationsIn), SemiJoinOuterRelations),
        findall(outerRelation(VGWrapperNodeID, GenAggNodeID), member(outerRelation(VGWrapperNodeID, SemiJoinNodeID), OuterRelationsIn), GenAggOuterRelations),
        subtractSet(OuterRelationsIn, SemiJoinOuterRelations, IntOuterRelations),
        mergeSets(IntOuterRelations, GenAggOuterRelations, OuterRelationsOut)
        ;
        select(semijoin(SemiJoinNodeID, _, _), NodesIn, IntNodes1),
        append([semijoin(SemiJoinNodeID, GenAggNodeID, TopSemiJoinPreds)], IntNodes1, IntNodes2),

        % **** create a new semijoin predicate past generalized aggregate
        gensym(semiJoinNode, BtmSemiJoinNodeID),
        select(parent(GenAggNodeID, GenAggNodeChildNode), IntGraph1, IntGraph2),
        append([parent(GenAggNodeID, BtmSemiJoinNodeID)], IntGraph2, IntGraph3),
        append([parent(SemiJoinNodeID, GenAggNodeID)], IntGraph3, IntGraph4),
        append([parent(BtmSemiJoinNodeID, GenAggNodeChildNode)], IntGraph4, IntGraph5),
        member(parent(SemiJoinNodeID, OtherSemiJoinChild), IntGraph1),
        append([parent(BtmSemiJoinNodeID, OtherSemiJoinChild)], IntGraph5, GraphOut),
        
        append([semijoin(BtmSemiJoinNodeID, GenAggNodeChildNode, BtmSemiJoinPreds)], IntNodes2, IntNodes3),
        
        % **** add output, seed and attributes for btm semijoin node
        identifyNodeSeedOutputRandomAttributes(BtmSemiJoinNodeID, GraphOut, IntNodes3, IntNodes4,
                                                  SeedAttrsIn, IntSeedAttrs1, RandomAttrsIn, IntRandomAttrs1, OutputAttrsIn, IntOutputAttrs1),

        % **** update output, seed, random attributes for generalized aggregate node
        select(outputAttributes(GenAggNodeID, _), IntOutputAttrs1, IntOutputAttrs2),
        select(seedAttributes(GenAggNodeID, _), IntSeedAttrs1, IntSeedAttrs2),
        select(randomAttributes(GenAggNodeID, _), IntRandomAttrs1, IntRandomAttrs2),
        identifyNodeSeedOutputRandomAttributes(GenAggNodeID, GraphOut, IntNodes4, IntNodes5,
                                                  IntSeedAttrs2, IntSeedAttrs3, IntRandomAttrs2, IntRandomAttrs3, IntOutputAttrs2, IntOutputAttrs3),

        % **** update output, seed, random attributes for generalized aggregate node
        select(outputAttributes(SemiJoinNodeID, _), IntOutputAttrs3, IntOutputAttrs4),
        select(seedAttributes(SemiJoinNodeID, _), IntSeedAttrs3, IntSeedAttrs4),
        select(randomAttributes(SemiJoinNodeID, _), IntRandomAttrs3, IntRandomAttrs4),
        identifyNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodes5, IntNodesOut,
                                                  IntSeedAttrs4, IntSeedAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntOutputAttrs4, IntOutputAttrs5),

        updateNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, IntOutputAttrs6, IntRandomAttrs5, IntRandomAttrs6, IntSeedAttrs5, IntSeedAttrs6),
        updateNodeSeedOutputRandomAttributes(GenAggNodeID, GraphOut, IntNodesOut, IntOutputAttrs6, IntOutputAttrs7, IntRandomAttrs6, IntRandomAttrs7, IntSeedAttrs6, IntSeedAttrs7),
        updateNodeSeedOutputRandomAttributes(BtmSemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs7, OutputAttrsOut, IntRandomAttrs7, RandomAttrsOut, IntSeedAttrs7, SeedAttrsOut),

        OuterRelationsOut = OuterRelationsIn
    ),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushSemiJoinDownGenAgg(N)) ->
        retract(appliedTransformation(pushSemiJoinDownGenAgg(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSemiJoinDownGenAgg(NN)))
        ;
        assert(appliedTransformation(pushSemiJoinDownGenAgg(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSH OF A SELECTION DOWN A GENERALIZED AGGREGATE
% Returns true if (GraphOut, NodesOut) can legally result from pushing a selection operator past a generalized aggregate operator
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSelectionDownGenAgg),

    %**** find a selection operator that is above a generalized aggregate
    member(selection(SelectionNodeID, SelectionPreds), NodesIn),
    select(parent(SelectionNodeID, GenAggNodeID), GraphIn, IntGraph1),
    member(genagg(GenAggNodeID, _, GroupByAttrs, _, _, AggExprsOutAttrs), NodesIn),

    % **** rule applies if group by attributes are not null
    GroupByAttrs \= [],

    %**** rule applies only if lower generalized aggregate node has one parent
    nodeHasSingleParent(GenAggNodeID, GraphIn),

    % **** find which of the group by attributes are referenced by the selection predicates
    flatten(AggExprsOutAttrs, FlattenedAggExprsOutAttrs),
    partitionPredsBasedOnGenAggAttrs(FlattenedAggExprsOutAttrs, SelectionPreds, [], TopSelectionPreds, [], BtmSelectionPreds),

    % **** rule holds if selection predicates reference group by attrs
    BtmSelectionPreds \= [],

    (TopSelectionPreds = [] ->
        % **** move selection predicate past generalized aggregate
        select(parent(GenAggNodeID, GenAggNodeChildNode), IntGraph1, IntGraph2),
        setof(parent(SelectionParentID, SelectionNodeID),
            member(parent(SelectionParentID, SelectionNodeID), IntGraph2), SelectionNodeParents),
        swapNodeParents(SelectionNodeParents, SelectionNodeID, GenAggNodeID, IntGraph2, IntGraph3, NodesIn, IntNodes1),
        append([parent(GenAggNodeID, SelectionNodeID)], IntGraph3, IntGraph4),
        append([parent(SelectionNodeID, GenAggNodeChildNode)], IntGraph4, GraphOut),

        % **** update output, seed, random attributes for swapped nodes
        select(outputAttributes(SelectionNodeID, _), OutputAttrsIn, IntOutputAttrs1),
        select(seedAttributes(SelectionNodeID, _), SeedAttrsIn, IntSeedAttrs1),
        select(randomAttributes(SelectionNodeID, _), RandomAttrsIn, IntRandomAttrs1),
        identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes1, IntNodes2,
                                                  IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

        select(outputAttributes(GenAggNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
        select(seedAttributes(GenAggNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
        select(randomAttributes(GenAggNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
        identifyNodeSeedOutputRandomAttributes(GenAggNodeID, GraphOut, IntNodes2, IntNodesOut,
                                                  IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

        updateNodeSeedOutputRandomAttributes(GenAggNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
        updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
        % ****

        % **** update outer relation info if necessary
        findall(outerRelation(VGWrapperNodeID, SelectionNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SelectionOuterRelations),
        findall(outerRelation(VGWrapperNodeID, GenAggNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), GenAggOuterRelations),
        subtractSet(OuterRelationsIn, SelectionOuterRelations, IntOuterRelations),
        mergeSets(IntOuterRelations, GenAggOuterRelations, OuterRelationsOut)
        ;
        select(selection(SelectionNodeID, _), NodesIn, IntNodes1),
        append([selection(SelectionNodeID, TopSelectionPreds)], IntNodes1, IntNodes2),

        % **** create a new selection predicate past generalized aggregate
        gensym(selectionNode, BtmSelectionNodeID),
        append([selection(BtmSelectionNodeID, BtmSelectionPreds)], IntNodes2, IntNodes3),

        select(parent(GenAggNodeID, GenAggNodeChildNode), IntGraph1, IntGraph2),
        append([parent(GenAggNodeID, BtmSelectionNodeID)], IntGraph2, IntGraph3),
        append([parent(SelectionNodeID, GenAggNodeID)], IntGraph3, IntGraph4),
        append([parent(BtmSelectionNodeID, GenAggNodeChildNode)], IntGraph4, GraphOut),

        % **** add output, seed and attributes for btm selection node
        identifyNodeSeedOutputRandomAttributes(BtmSelectionNodeID, GraphOut, IntNodes3, IntNodes4,
                                                  SeedAttrsIn, IntSeedAttrs1, RandomAttrsIn, IntRandomAttrs1, OutputAttrsIn, IntOutputAttrs1),

        % **** update output, seed, random attributes for generalized aggregate node
        select(outputAttributes(GenAggNodeID, _), IntOutputAttrs1, IntOutputAttrs2),
        select(seedAttributes(GenAggNodeID, _), IntSeedAttrs1, IntSeedAttrs2),
        select(randomAttributes(GenAggNodeID, _), IntRandomAttrs1, IntRandomAttrs2),
        identifyNodeSeedOutputRandomAttributes(GenAggNodeID, GraphOut, IntNodes4, IntNodes5,
                                                  IntSeedAttrs2, IntSeedAttrs3, IntRandomAttrs2, IntRandomAttrs3, IntOutputAttrs2, IntOutputAttrs3),

        % **** update output, seed, random attributes for generalized aggregate node
        select(outputAttributes(SelectionNodeID, _), IntOutputAttrs3, IntOutputAttrs4),
        select(seedAttributes(SelectionNodeID, _), IntSeedAttrs3, IntSeedAttrs4),
        select(randomAttributes(SelectionNodeID, _), IntRandomAttrs3, IntRandomAttrs4),
        identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes5, IntNodesOut,
                                                  IntSeedAttrs4, IntSeedAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntOutputAttrs4, IntOutputAttrs5),
                                                  
        updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, IntOutputAttrs6, IntRandomAttrs5, IntRandomAttrs6, IntSeedAttrs5, IntSeedAttrs6),
        updateNodeSeedOutputRandomAttributes(GenAggNodeID, GraphOut, IntNodesOut, IntOutputAttrs6, IntOutputAttrs7, IntRandomAttrs6, IntRandomAttrs7, IntSeedAttrs6, IntSeedAttrs7),
        updateNodeSeedOutputRandomAttributes(BtmSelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs7, OutputAttrsOut, IntRandomAttrs7, RandomAttrsOut, IntSeedAttrs7, SeedAttrsOut),

        OuterRelationsOut = OuterRelationsIn
    ),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),
    
    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushSelectionDownGenAgg(N)) ->
        retract(appliedTransformation(pushSelectionDownGenAgg(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSelectionDownGenAgg(NN)))
        ;
        assert(appliedTransformation(pushSelectionDownGenAgg(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN A ANTIJOIN PAST A SELECTION
% Returns true if (GraphOut, NodesOut) can legally result from pushing a antijoin operation past a selection
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushAntiJoinDownSelection),

    %**** find some selection predicate that is above a antijoin
    select(antijoin(AntiJoinNodeID, SelectionNodeID, AntiJoinPreds), NodesIn, IntNodes1),
    select(parent(AntiJoinNodeID, SelectionNodeID), GraphIn, IntGraph1),
    member(selection(SelectionNodeID, _), IntNodes1),

    %**** rule applies only to nodes with one parent
    nodeHasSingleParent(SelectionNodeID, GraphIn),

    % **** push antijoin down selection
    select(parent(SelectionNodeID, SelectionChildID), IntGraph1, IntGraph2),
    setof(parent(AntiJoinParentID, AntiJoinNodeID),
            member(parent(AntiJoinParentID, AntiJoinNodeID), GraphIn), AntiJoinNodeParents),
    %**** re-arrange the structure of the graph
    swapNodeParents(AntiJoinNodeParents, AntiJoinNodeID, SelectionNodeID, IntGraph2, IntGraph3, IntNodes1, IntNodes2),
    append([parent(SelectionNodeID, AntiJoinNodeID)], IntGraph3, IntGraph4),
    append([parent(AntiJoinNodeID, SelectionChildID)], IntGraph4, GraphOut),

    % **** update antijoin nodes
    append([antijoin(AntiJoinNodeID, SelectionChildID, AntiJoinPreds)], IntNodes2, IntNodes3),

    % **** update output, seed, random attributes for swapped nodes
    select(outputAttributes(AntiJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(seedAttributes(AntiJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    select(randomAttributes(AntiJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    identifyNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodes3, IntNodes4,
                                                  IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    % **** update output, seed, random attributes for antijoin node
    select(outputAttributes(SelectionNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(seedAttributes(SelectionNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    select(randomAttributes(SelectionNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes4, IntNodesOut,
                                                  IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    % ****

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, SelectionNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SelectionOuterRelations),
    findall(outerRelation(VGWrapperNodeID, AntiJoinNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), AntiJoinOuterRelations),
    subtractSet(OuterRelationsIn, SelectionOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, AntiJoinOuterRelations, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushSelectionDownAntiJoin(N)) ->
        retract(appliedTransformation(pushSelectionDownAntiJoin(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSelectionDownAntiJoin(NN)))
        ;
        assert(appliedTransformation(pushSelectionDownAntiJoin(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN A SEMIJOIN PAST A SELECTION
% Returns true if (GraphOut, NodesOut) can legally result from pushing a semijoin operation past a selection
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSemiJoinDownSelection),

    %**** find some selection predicate that is above a semijoin
    select(semijoin(SemiJoinNodeID, SelectionNodeID, SemiJoinPreds), NodesIn, IntNodes1),
    select(parent(SemiJoinNodeID, SelectionNodeID), GraphIn, IntGraph1),
    member(selection(SelectionNodeID, _), IntNodes1),

    %**** rule applies only to nodes with one parent
    nodeHasSingleParent(SelectionNodeID, GraphIn),

    % **** push semijoin down selection
    select(parent(SelectionNodeID, SelectionChildID), IntGraph1, IntGraph2),
    setof(parent(SemiJoinParentID, SemiJoinNodeID),
            member(parent(SemiJoinParentID, SemiJoinNodeID), GraphIn), SemiJoinNodeParents),
    %**** re-arrange the structure of the graph
    swapNodeParents(SemiJoinNodeParents, SemiJoinNodeID, SelectionNodeID, IntGraph2, IntGraph3, IntNodes1, IntNodes2),
    append([parent(SelectionNodeID, SemiJoinNodeID)], IntGraph3, IntGraph4),
    append([parent(SemiJoinNodeID, SelectionChildID)], IntGraph4, GraphOut),

    % **** update semijoin nodes
    append([semijoin(SemiJoinNodeID, SelectionChildID, SemiJoinPreds)], IntNodes2, IntNodes3),
    
    % **** update output, seed, random attributes for swapped nodes
    select(outputAttributes(SemiJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(seedAttributes(SemiJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    select(randomAttributes(SemiJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    identifyNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodes3, IntNodes4,
                                                  IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    % **** update output, seed, random attributes for semijoin node
    select(outputAttributes(SelectionNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(seedAttributes(SelectionNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    select(randomAttributes(SelectionNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes4, IntNodesOut,
                                                  IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    % ****

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, SelectionNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SelectionOuterRelations),
    findall(outerRelation(VGWrapperNodeID, SemiJoinNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SemiJoinOuterRelations),
    subtractSet(OuterRelationsIn, SelectionOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, SemiJoinOuterRelations, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushSelectionDownSemiJoin(N)) ->
        retract(appliedTransformation(pushSelectionDownSemiJoin(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSelectionDownSemiJoin(NN)))
        ;
        assert(appliedTransformation(pushSelectionDownSemiJoin(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN SELECTIONS PAST A ANTIJOIN
% Returns true if (GraphOut, NodesOut) can legally result from pushing a selection operation past a antijoin
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSelectionDownAntiJoin),

    %**** find some selection predicate that is above a antijoin
    member(selection(SelectionNodeID, SelectionPreds), NodesIn),
    select(parent(SelectionNodeID, AntiJoinNodeID), GraphIn, IntGraph1),
    select(antijoin(AntiJoinNodeID, AntiJoinDataSourceChildID, AntiJoinPreds), NodesIn, IntNodes1),

    %**** rule applies only to nodes with one parent
    nodeHasSingleParent(AntiJoinNodeID, GraphIn),

    % **** find the 'left' child of the antijoin
    select(parent(AntiJoinNodeID, AntiJoinDataSourceChildID), IntGraph1, IntGraph2),
    % **** make sure all predicates refer to attributes from that relation
    member(outputAttributes(AntiJoinDataSourceChildID, AntiJoinDataSourceChildOutputAttrs), OutputAttrsIn),
    extractAttrsFromPredicates(SelectionPreds, [], PredReferencedAttrs),
    subset(PredReferencedAttrs, AntiJoinDataSourceChildOutputAttrs),

    % **** update antijoin nodes
    append([antijoin(AntiJoinNodeID, SelectionNodeID, AntiJoinPreds)], IntNodes1, IntNodes2),

    % **** push selection down semi-join
    setof(parent(SelectionParentID, SelectionNodeID),
            member(parent(SelectionParentID, SelectionNodeID), GraphIn), SelectionNodeParents),
    %**** re-arrange the structure of the graph
    swapNodeParents(SelectionNodeParents, SelectionNodeID, AntiJoinNodeID, IntGraph2, IntGraph3, IntNodes2, IntNodes3),
    append([parent(SelectionNodeID, AntiJoinDataSourceChildID)], IntGraph3, IntGraph4),
    append([parent(AntiJoinNodeID, SelectionNodeID)], IntGraph4, GraphOut),

    % **** update output, seed, random attributes for swapped nodes
    select(outputAttributes(SelectionNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(seedAttributes(SelectionNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    select(randomAttributes(SelectionNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes3, IntNodes4,
                                                  IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    % **** update output, seed, random attributes for antijoin node
    select(outputAttributes(AntiJoinNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(seedAttributes(AntiJoinNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    select(randomAttributes(AntiJoinNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    identifyNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodes4, IntNodesOut,
                                                  IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    % ****

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, SelectionNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SelectionOuterRelations),
    findall(outerRelation(VGWrapperNodeID, AntiJoinNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), AntiJoinOuterRelations),
    subtractSet(OuterRelationsIn, SelectionOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, AntiJoinOuterRelations, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushSelectionDownAntiJoin(N)) ->
        retract(appliedTransformation(pushSelectionDownAntiJoin(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSelectionDownAntiJoin(NN)))
        ;
        assert(appliedTransformation(pushSelectionDownAntiJoin(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN SELECTIONS PAST A SEMIJOIN
% Returns true if (GraphOut, NodesOut) can legally result from pushing a selection operation past a semijoin
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSelectionDownSemiJoin),

    %**** find some selection predicate that is above a semijoin
    member(selection(SelectionNodeID, SelectionPreds), NodesIn),
    select(parent(SelectionNodeID, SemiJoinNodeID), GraphIn, IntGraph1),
    select(semijoin(SemiJoinNodeID, SemiJoinDataSourceChildID, SemiJoinPreds), NodesIn, IntNodes1),

    %**** rule applies only to nodes with one parent
    nodeHasSingleParent(SemiJoinNodeID, GraphIn),

    % **** find the 'left' child of the semijoin
    select(parent(SemiJoinNodeID, SemiJoinDataSourceChildID), IntGraph1, IntGraph2),
    % **** make sure all predicates refer to attributes from that relation
    member(outputAttributes(SemiJoinDataSourceChildID, SemiJoinDataSourceChildOutputAttrs), OutputAttrsIn),
    extractAttrsFromPredicates(SelectionPreds, [], PredReferencedAttrs),
    subset(PredReferencedAttrs, SemiJoinDataSourceChildOutputAttrs),

    % **** update semijoin nodes
    append([semijoin(SemiJoinNodeID, SelectionNodeID, SemiJoinPreds)], IntNodes1, IntNodes2),

    % **** push selection down semi-join
    setof(parent(SelectionParentID, SelectionNodeID),
            member(parent(SelectionParentID, SelectionNodeID), GraphIn), SelectionNodeParents),
    %**** re-arrange the structure of the graph
    swapNodeParents(SelectionNodeParents, SelectionNodeID, SemiJoinNodeID, IntGraph2, IntGraph3, IntNodes2, IntNodes3),
    append([parent(SelectionNodeID, SemiJoinDataSourceChildID)], IntGraph3, IntGraph4),
    append([parent(SemiJoinNodeID, SelectionNodeID)], IntGraph4, GraphOut),

    % **** update output, seed, random attributes for swapped nodes
    select(outputAttributes(SelectionNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(seedAttributes(SelectionNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    select(randomAttributes(SelectionNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes3, IntNodes4,
                                                  IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    % **** update output, seed, random attributes for semijoin node
    select(outputAttributes(SemiJoinNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(seedAttributes(SemiJoinNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    select(randomAttributes(SemiJoinNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    identifyNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodes4, IntNodesOut,
                                                  IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    % ****
    
    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, SelectionNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SelectionOuterRelations),
    findall(outerRelation(VGWrapperNodeID, SemiJoinNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SemiJoinOuterRelations),
    subtractSet(OuterRelationsIn, SelectionOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, SemiJoinOuterRelations, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushSelectionDownSemiJoin(N)) ->
        retract(appliedTransformation(pushSelectionDownSemiJoin(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSelectionDownSemiJoin(NN)))
        ;
        assert(appliedTransformation(pushSelectionDownSemiJoin(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN ANTIJOINS PAST A SCALAR FUNCTION
% Returns true if (GraphOut, NodesOut) can legally result from pushing a antijoin operation past a scalar function operator
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushAntiJoinDownScalarFunc),

    %**** find a antijoin operator that is above a scalar function
    select(antijoin(AntiJoinNodeID, ScalarFuncNodeID, AntiJoinPreds), NodesIn, IntNodes1),
    select(parent(AntiJoinNodeID, ScalarFuncNodeID), GraphIn, IntGraph1),
    member(scalarfunc(ScalarFuncNodeID, _, _, FuncExprsOutAttrs), IntNodes1),

    %**** rule applies only if scalar func node has one parent
    nodeHasSingleParent(ScalarFuncNodeID, GraphIn),

    flatten(FuncExprsOutAttrs, FlattenedFuncExprsOutAttrs),
    partitionPredsBasedOnFuncOutputAttrs(FlattenedFuncExprsOutAttrs, AntiJoinPreds, [], TopAntiJoinPreds, [], BtmAntiJoinPreds),

    BtmAntiJoinPreds \= [],

    (TopAntiJoinPreds = [] ->
        % **** move antijoin predicate past scalar function
        select(parent(ScalarFuncNodeID, ScalarFuncChildNode), IntGraph1, IntGraph2),
        setof(parent(AntiJoinParentID, AntiJoinNodeID),
            member(parent(AntiJoinParentID, AntiJoinNodeID), IntGraph2), AntiJoinNodeParents),
        swapNodeParents(AntiJoinNodeParents, AntiJoinNodeID, ScalarFuncNodeID, IntGraph2, IntGraph3, IntNodes1, IntNodes2),
        append([parent(ScalarFuncNodeID, AntiJoinNodeID)], IntGraph3, IntGraph4),
        append([parent(AntiJoinNodeID, ScalarFuncChildNode)], IntGraph4, GraphOut),

        append([antijoin(AntiJoinNodeID, ScalarFuncChildNode, BtmAntiJoinPreds)], IntNodes2, IntNodes3),

        % **** update output, random and seed attributes for antijoin node
        select(outputAttributes(AntiJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
        select(randomAttributes(AntiJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
        select(seedAttributes(AntiJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
        identifyNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodes3, IntNodes4, IntSeedAttrs1, IntSeedAttrs2,
                                             IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

        select(outputAttributes(ScalarFuncNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
        select(randomAttributes(ScalarFuncNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
        select(seedAttributes(ScalarFuncNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
        identifyNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodes4, IntNodesOut, IntSeedAttrs3, IntSeedAttrs4,
                                             IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

        updateNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
        updateNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
        % ****

        % **** update outer relation info if necessary
        findall(outerRelation(VGWrapperNodeID, AntiJoinNodeID), member(outerRelation(VGWrapperNodeID, AntiJoinNodeID), OuterRelationsIn), AntiJoinOuterRelations),
        findall(outerRelation(VGWrapperNodeID, ScalarFuncNodeID), member(outerRelation(VGWrapperNodeID, AntiJoinNodeID), OuterRelationsIn), ScalarFuncOuterRelations),
        subtractSet(OuterRelationsIn, AntiJoinOuterRelations, IntOuterRelations),
        mergeSets(IntOuterRelations, ScalarFuncOuterRelations, OuterRelationsOut)
        ;
        % **** create a new antijoin predicate past generalized aggregate
        append([antijoin(AntiJoinNodeID, ScalarFuncNodeID, TopAntiJoinPreds)], IntNodes1, IntNodes2),
        gensym(antiJoinNode, NewAntiJoinNodeID),

        member(parent(AntiJoinNodeID, OtherChildNode), IntGraph1),
        select(parent(ScalarFuncNodeID, ScalarFuncChildNode), GraphIn, IntGraph2),
        append([parent(ScalarFuncNodeID, NewAntiJoinNodeID)], IntGraph2, IntGraph3),
        append([parent(NewAntiJoinNodeID, ScalarFuncChildNode)], IntGraph3, IntGraph4),
        append([parent(NewAntiJoinNodeID, OtherChildNode)], IntGraph4, GraphOut),

        append([antijoin(NewAntiJoinNodeID, ScalarFuncChildNode, BtmAntiJoinPreds)], IntNodes2, IntNodes3),

        % **** add output and random and attributes for new antijoin node
        identifyNodeSeedOutputRandomAttributes(NewAntiJoinNodeID, GraphOut, IntNodes3, IntNodes4, SeedAttrsIn, IntSeedAttrs1,
                                             RandomAttrsIn, IntRandomAttrs1, OutputAttrsIn, IntOutputAttrs1),

        % **** identify new output and random attributes for scalar function node
        select(outputAttributes(ScalarFuncNodeID, _), IntOutputAttrs1, IntOutputAttrs2),
        select(randomAttributes(ScalarFuncNodeID, _), IntRandomAttrs1, IntRandomAttrs2),
        select(seedAttributes(ScalarFuncNodeID, _), IntSeedAttrs1, IntSeedAttrs2),
        identifyNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodes4, IntNodes5, IntSeedAttrs2, IntSeedAttrs3,
                                             IntRandomAttrs2, IntRandomAttrs3, IntOutputAttrs2, IntOutputAttrs3),

        % **** identify new output and random attributes for antijoin node
        select(outputAttributes(AntiJoinNodeID, _), IntOutputAttrs3, IntOutputAttrs4),
        select(randomAttributes(AntiJoinNodeID, _), IntRandomAttrs3, IntRandomAttrs4),
        select(seedAttributes(AntiJoinNodeID, _), IntSeedAttrs3, IntSeedAttrs4),
        identifyNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodes5, IntNodesOut, IntSeedAttrs4, IntSeedAttrs5,
                                             IntRandomAttrs4, IntRandomAttrs5, IntOutputAttrs4, IntOutputAttrs5),

        updateNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, IntOutputAttrs6, IntRandomAttrs5, IntRandomAttrs6, IntSeedAttrs5, IntSeedAttrs6),
        updateNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodesOut, IntOutputAttrs6, IntOutputAttrs7, IntRandomAttrs6, IntRandomAttrs7, IntSeedAttrs6, IntSeedAttrs7),
        updateNodeSeedOutputRandomAttributes(NewAntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs7, OutputAttrsOut, IntRandomAttrs7, RandomAttrsOut, IntSeedAttrs7, SeedAttrsOut),
        % *****

        OuterRelationsOut = OuterRelationsIn
    ),

    %**** update candidate key information for entire plan
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut),
                                           member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut,
                                           OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushAntiJoinDownScalarFunc(N)) ->
        retract(appliedTransformation(pushAntiJoinDownScalarFunc(N))),
        NN is N + 1,
        assert(appliedTransformation(pushAntiJoinDownScalarFunc(NN)))
        ;
        assert(appliedTransformation(pushAntiJoinDownScalarFunc(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN SEMIJOINS PAST A SCALAR FUNCTION
% Returns true if (GraphOut, NodesOut) can legally result from pushing a semijoin operation past a scalar function operator
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSemiJoinDownScalarFunc),

    %**** find a semijoin operator that is above a scalar function
    select(semijoin(SemiJoinNodeID, ScalarFuncNodeID, SemiJoinPreds), NodesIn, IntNodes1),
    select(parent(SemiJoinNodeID, ScalarFuncNodeID), GraphIn, IntGraph1),
    member(scalarfunc(ScalarFuncNodeID, _, _, FuncExprsOutAttrs), IntNodes1),

    %**** rule applies only if scalar func node has one parent
    nodeHasSingleParent(ScalarFuncNodeID, GraphIn),

    flatten(FuncExprsOutAttrs, FlattenedFuncExprsOutAttrs),
    partitionPredsBasedOnFuncOutputAttrs(FlattenedFuncExprsOutAttrs, SemiJoinPreds, [], TopSemiJoinPreds, [], BtmSemiJoinPreds),

    BtmSemiJoinPreds \= [],

    (TopSemiJoinPreds = [] ->
        % **** move semijoin predicate past scalar function
        select(parent(ScalarFuncNodeID, ScalarFuncChildNode), IntGraph1, IntGraph2),
        setof(parent(SemiJoinParentID, SemiJoinNodeID),
            member(parent(SemiJoinParentID, SemiJoinNodeID), IntGraph2), SemiJoinNodeParents),
        swapNodeParents(SemiJoinNodeParents, SemiJoinNodeID, ScalarFuncNodeID, IntGraph2, IntGraph3, IntNodes1, IntNodes2),
        append([parent(ScalarFuncNodeID, SemiJoinNodeID)], IntGraph3, IntGraph4),
        append([parent(SemiJoinNodeID, ScalarFuncChildNode)], IntGraph4, GraphOut),

        append([semijoin(SemiJoinNodeID, ScalarFuncChildNode, BtmSemiJoinPreds)], IntNodes2, IntNodes3),
        
        % **** update output, random and seed attributes for semijoin node
        select(outputAttributes(SemiJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
        select(randomAttributes(SemiJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
        select(seedAttributes(SemiJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
        identifyNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodes3, IntNodes4, IntSeedAttrs1, IntSeedAttrs2,
                                             IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

        select(outputAttributes(ScalarFuncNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
        select(randomAttributes(ScalarFuncNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
        select(seedAttributes(ScalarFuncNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
        identifyNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodes4, IntNodesOut, IntSeedAttrs3, IntSeedAttrs4,
                                             IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

        updateNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
        updateNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
        % ****

        % **** update outer relation info if necessary
        findall(outerRelation(VGWrapperNodeID, SemiJoinNodeID), member(outerRelation(VGWrapperNodeID, SemiJoinNodeID), OuterRelationsIn), SemiJoinOuterRelations),
        findall(outerRelation(VGWrapperNodeID, ScalarFuncNodeID), member(outerRelation(VGWrapperNodeID, SemiJoinNodeID), OuterRelationsIn), ScalarFuncOuterRelations),
        subtractSet(OuterRelationsIn, SemiJoinOuterRelations, IntOuterRelations),
        mergeSets(IntOuterRelations, ScalarFuncOuterRelations, OuterRelationsOut)
        ;
        % **** create a new semijoin predicate past generalized aggregate
        append([semijoin(SemiJoinNodeID, ScalarFuncNodeID, TopSemiJoinPreds)], IntNodes1, IntNodes2),
        gensym(semiJoinNode, NewSemiJoinNodeID),

        member(parent(SemiJoinNodeID, OtherChildNode), IntGraph1),
        select(parent(ScalarFuncNodeID, ScalarFuncChildNode), GraphIn, IntGraph2),
        append([parent(ScalarFuncNodeID, NewSemiJoinNodeID)], IntGraph2, IntGraph3),
        append([parent(NewSemiJoinNodeID, ScalarFuncChildNode)], IntGraph3, IntGraph4),
        append([parent(NewSemiJoinNodeID, OtherChildNode)], IntGraph4, GraphOut),
        
        append([semijoin(NewSemiJoinNodeID, ScalarFuncChildNode, BtmSemiJoinPreds)], IntNodes2, IntNodes3),
        
        % **** add output and random and attributes for new semijoin node
        identifyNodeSeedOutputRandomAttributes(NewSemiJoinNodeID, GraphOut, IntNodes3, IntNodes4, SeedAttrsIn, IntSeedAttrs1,
                                             RandomAttrsIn, IntRandomAttrs1, OutputAttrsIn, IntOutputAttrs1),

        % **** identify new output and random attributes for scalar function node
        select(outputAttributes(ScalarFuncNodeID, _), IntOutputAttrs1, IntOutputAttrs2),
        select(randomAttributes(ScalarFuncNodeID, _), IntRandomAttrs1, IntRandomAttrs2),
        select(seedAttributes(ScalarFuncNodeID, _), IntSeedAttrs1, IntSeedAttrs2),
        identifyNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodes4, IntNodes5, IntSeedAttrs2, IntSeedAttrs3,
                                             IntRandomAttrs2, IntRandomAttrs3, IntOutputAttrs2, IntOutputAttrs3),

        % **** identify new output and random attributes for semijoin node
        select(outputAttributes(SemiJoinNodeID, _), IntOutputAttrs3, IntOutputAttrs4),
        select(randomAttributes(SemiJoinNodeID, _), IntRandomAttrs3, IntRandomAttrs4),
        select(seedAttributes(SemiJoinNodeID, _), IntSeedAttrs3, IntSeedAttrs4),
        identifyNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodes5, IntNodesOut, IntSeedAttrs4, IntSeedAttrs5,
                                             IntRandomAttrs4, IntRandomAttrs5, IntOutputAttrs4, IntOutputAttrs5),

        updateNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, IntOutputAttrs6, IntRandomAttrs5, IntRandomAttrs6, IntSeedAttrs5, IntSeedAttrs6),
        updateNodeSeedOutputRandomAttributes(ScalarFuncNodeID, GraphOut, IntNodesOut, IntOutputAttrs6, IntOutputAttrs7, IntRandomAttrs6, IntRandomAttrs7, IntSeedAttrs6, IntSeedAttrs7),
        updateNodeSeedOutputRandomAttributes(NewSemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs7, OutputAttrsOut, IntRandomAttrs7, RandomAttrsOut, IntSeedAttrs7, SeedAttrsOut),
        % *****

        OuterRelationsOut = OuterRelationsIn
    ),

    %**** update candidate key information for entire plan
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut),
                                           member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut,
                                           OutputAttrsOut, SeedAttrsOut),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    (appliedTransformation(pushSemiJoinDownScalarFunc(N)) ->
        retract(appliedTransformation(pushSemiJoinDownScalarFunc(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSemiJoinDownScalarFunc(NN)))
        ;
        assert(appliedTransformation(pushSemiJoinDownScalarFunc(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSH OF A ANTIJOIN DOWN A SPLIT
% Returns true if (GraphOut, NodesOut) can legally result from pushing a antijoin operator past a split operator in the query
% plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushAntiJoinDownSplit),

    %**** find a antijoin operator that is above a split
    select(antijoin(AntiJoinNodeID, SplitNodeID, AntiJoinPreds), NodesIn, IntNodes1),
    select(parent(AntiJoinNodeID, SplitNodeID), GraphIn, IntGraph1),
    member(split(SplitNodeID, SplitAttrs), NodesIn),

    %**** rule applies only if lower split node has one parent
    nodeHasSingleParent(SplitNodeID, GraphIn),

    % **** rule can be applied if antijoin predicates do no reference split attributes
    extractAttrsFromPredicates(AntiJoinPreds, [], PredReferencedAttrs),
    intersectSets(SplitAttrs, PredReferencedAttrs, []),

    %**** find the child of the split node
    select(parent(SplitNodeID, SplitNodeChild), IntGraph1, IntGraph2),
    %**** and the parents of the antijoin node
    setof(parent(AntiJoinParentID, AntiJoinNodeID),
         member(parent(AntiJoinParentID, AntiJoinNodeID), GraphIn), AntiJoinNodeParents),
    %**** re-arrange the structure of the graph
    swapNodeParents(AntiJoinNodeParents, AntiJoinNodeID, SplitNodeID, IntGraph2, IntGraph3, IntNodes1, IntNodes2),
    append([parent(AntiJoinNodeID, SplitNodeChild)], IntGraph3, IntGraph4),
    append([parent(SplitNodeID, AntiJoinNodeID)], IntGraph4, GraphOut),

    append([antijoin(AntiJoinNodeID, SplitNodeChild, AntiJoinPreds)], IntNodes2, IntNodes3),

    % **** update output, random and attributes for swapped nodes
    select(outputAttributes(AntiJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(randomAttributes(AntiJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    select(seedAttributes(AntiJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    identifyNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodes3, IntNodes4, IntSeedAttrs1, IntSeedAttrs2,
                                             IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    select(outputAttributes(SplitNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(randomAttributes(SplitNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    select(seedAttributes(SplitNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    identifyNodeSeedOutputRandomAttributes(SplitNodeID, GraphOut, IntNodes4, IntNodesOut, IntSeedAttrs3, IntSeedAttrs4,
                                             IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(SplitNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    % ****

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, AntiJoinNodeID), member(outerRelation(VGWrapperNodeID, AntiJoinNodeID), OuterRelationsIn), AntiJoinOuterRelations),
    findall(outerRelation(VGWrapperNodeID, SplitNodeID), member(outerRelation(VGWrapperNodeID, AntiJoinNodeID), OuterRelationsIn), SplitOuterRelations),
    subtractSet(OuterRelationsIn, AntiJoinOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, SplitOuterRelations, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushAntiJoinDownSplit(N)) ->
        retract(appliedTransformation(pushAntiJoinDownSplit(N))),
        NN is N + 1,
        assert(appliedTransformation(pushAntiJoinDownSplit(NN)))
        ;
        assert(appliedTransformation(pushAntiJoinDownSplit(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSH OF A SEMIJOIN DOWN A SPLIT
% Returns true if (GraphOut, NodesOut) can legally result from pushing a semijoin operator past a split operator in the query
% plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSemiJoinDownSplit),

    %**** find a semijoin operator that is above a split
    select(semijoin(SemiJoinNodeID, SplitNodeID, SemiJoinPreds), NodesIn, IntNodes1),
    select(parent(SemiJoinNodeID, SplitNodeID), GraphIn, IntGraph1),
    member(split(SplitNodeID, SplitAttrs), NodesIn),

    %**** rule applies only if lower split node has one parent
    nodeHasSingleParent(SplitNodeID, GraphIn),
    
    % **** rule can be applied if semijoin predicates do no reference split attributes
    extractAttrsFromPredicates(SemiJoinPreds, [], PredReferencedAttrs),
    intersectSets(SplitAttrs, PredReferencedAttrs, []),
    
    %**** find the child of the split node
    select(parent(SplitNodeID, SplitNodeChild), IntGraph1, IntGraph2),
    %**** and the parents of the semijoin node
    setof(parent(SemiJoinParentID, SemiJoinNodeID),
         member(parent(SemiJoinParentID, SemiJoinNodeID), GraphIn), SemiJoinNodeParents),
    %**** re-arrange the structure of the graph
    swapNodeParents(SemiJoinNodeParents, SemiJoinNodeID, SplitNodeID, IntGraph2, IntGraph3, IntNodes1, IntNodes2),
    append([parent(SemiJoinNodeID, SplitNodeChild)], IntGraph3, IntGraph4),
    append([parent(SplitNodeID, SemiJoinNodeID)], IntGraph4, GraphOut),

    append([semijoin(SemiJoinNodeID, SplitNodeChild, SemiJoinPreds)], IntNodes2, IntNodes3),
    
    % **** update output, random and attributes for swapped nodes
    select(outputAttributes(SemiJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(randomAttributes(SemiJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    select(seedAttributes(SemiJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    identifyNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodes3, IntNodes4, IntSeedAttrs1, IntSeedAttrs2,
                                             IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    select(outputAttributes(SplitNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(randomAttributes(SplitNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    select(seedAttributes(SplitNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    identifyNodeSeedOutputRandomAttributes(SplitNodeID, GraphOut, IntNodes4, IntNodesOut, IntSeedAttrs3, IntSeedAttrs4,
                                             IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(SplitNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    % ****

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, SemiJoinNodeID), member(outerRelation(VGWrapperNodeID, SemiJoinNodeID), OuterRelationsIn), SemiJoinOuterRelations),
    findall(outerRelation(VGWrapperNodeID, SplitNodeID), member(outerRelation(VGWrapperNodeID, SemiJoinNodeID), OuterRelationsIn), SplitOuterRelations),
    subtractSet(OuterRelationsIn, SemiJoinOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, SplitOuterRelations, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushSemiJoinDownSplit(N)) ->
        retract(appliedTransformation(pushSemiJoinDownSplit(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSemiJoinDownSplit(NN)))
        ;
        assert(appliedTransformation(pushSemiJoinDownSplit(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS MERGING OF TWO SPLITS INTO A SINGLE SPLIT
% Returns true if (GraphOut, NodesOut) can legally result from merging two split operators in the query plan represented by
% (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(mergeSplits),

    %**** find a split operator that is above another split
    select(split(TopSplitNodeID, TopSplitNodeAttrs), NodesIn, IntNodes1),
    select(parent(TopSplitNodeID, BtmSplitNodeID), GraphIn, IntGraph1),
    select(split(BtmSplitNodeID, BtmSplitNodeAttrs), IntNodes1, IntNodes2),

    %**** rule applies only if lower split node has one parent
    nodeHasSingleParent(BtmSplitNodeID, GraphIn),

    %**** find the child of the lower split node
    select(parent(BtmSplitNodeID, BtmSplitNodeChild), IntGraph1, IntGraph2),
    %**** re-arrange the structure of the graph
    append([parent(TopSplitNodeID, BtmSplitNodeChild)], IntGraph2, GraphOut),

    %**** delete lower split node from set of output, seed and random attributes
    select(outputAttributes(BtmSplitNodeID, _), OutputAttrsIn, OutputAttrsOut),
    select(randomAttributes(BtmSplitNodeID, _), RandomAttrsIn, RandomAttrsOut),
    select(seedAttributes(BtmSplitNodeID, _), SeedAttrsIn, SeedAttrsOut),

    %**** delete lower split node from set of candidate keys
    deleteCandidateKeyInformation(BtmSplitNodeID, KeysIn, KeysOut),

    % **** merge split attributes so that duplicate attributes appear only once in the plan
    mergeSets(TopSplitNodeAttrs, BtmSplitNodeAttrs, NewSplitNodeAttrs),
    append([split(TopSplitNodeID, NewSplitNodeAttrs)], IntNodes2, NodesOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    OuterRelationsOut = OuterRelationsIn,

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(mergeSplits(N)) ->
        retract(appliedTransformation(mergeSplits(N))),
        NN is N + 1,
        assert(appliedTransformation(mergeSplits(NN)))
        ;
        assert(appliedTransformation(mergeSplits(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSH OF A ANTIJOIN DOWN A DEDUP
% Returns true if (GraphOut, NodesOut) can legally result from pushing a antijoin operator past a dedup operator in the query
% plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushAntiJoinDownDedup),

    %**** find a antijoin operator that is above a split
    select(antijoin(AntiJoinNodeID, DedupNodeID, AntiJoinPreds), NodesIn, IntNodes1),
    select(parent(AntiJoinNodeID, DedupNodeID), GraphIn, IntGraph1),
    select(dedup(DedupNodeID, _), IntNodes1, _),

    %**** rule applies only if lower split node has one parent
    nodeHasSingleParent(DedupNodeID, GraphIn),

    %**** find the child of the split node
    select(parent(DedupNodeID, DedupNodeChild), IntGraph1, IntGraph2),
    %**** and the parents of the antijoin node
    setof(parent(AntiJoinParentID, AntiJoinNodeID),
         member(parent(AntiJoinParentID, AntiJoinNodeID), GraphIn), AntiJoinNodeParents),
    %**** re-arrange the structure of the graph
    swapNodeParents(AntiJoinNodeParents, AntiJoinNodeID, DedupNodeID, IntGraph2, IntGraph3, IntNodes1, IntNodes2),
    append([parent(AntiJoinNodeID, DedupNodeChild)], IntGraph3, IntGraph4),
    append([parent(DedupNodeID, AntiJoinNodeID)], IntGraph4, GraphOut),

    append([antijoin(AntiJoinNodeID, DedupNodeChild, AntiJoinPreds)], IntNodes2, IntNodes3),

    % **** update seed, output, random attribues of swapped nodes
    select(outputAttributes(AntiJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(seedAttributes(AntiJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    select(randomAttributes(AntiJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    identifyNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodes3, IntNodes4,
                         IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    select(outputAttributes(DedupNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(seedAttributes(DedupNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    select(randomAttributes(DedupNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    identifyNodeSeedOutputRandomAttributes(DedupNodeID, GraphOut, IntNodes4, IntNodesOut,
                         IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(DedupNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    %****

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, AntiJoinNodeID), member(outerRelation(VGWrapperNodeID, AntiJoinNodeID), OuterRelationsIn), AntiJoinOuterRelations),
    findall(outerRelation(VGWrapperNodeID, DedupNodeID), member(outerRelation(VGWrapperNodeID, AntiJoinNodeID), OuterRelationsIn), DedupOuterRelations),
    subtractSet(OuterRelationsIn, AntiJoinOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, DedupOuterRelations, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushAntiJoinDownDedup(N)) ->
        retract(appliedTransformation(pushAntiJoinDownDedup(N))),
        NN is N + 1,
        assert(appliedTransformation(pushAntiJoinDownDedup(NN)))
        ;
        assert(appliedTransformation(pushAntiJoinDownDedup(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSH OF A SEMIJOIN DOWN A DEDUP
% Returns true if (GraphOut, NodesOut) can legally result from pushing a semijoin operator past a dedup operator in the query
% plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSemiJoinDownDedup),

    %**** find a semijoin operator that is above a split
    select(semijoin(SemiJoinNodeID, DedupNodeID, SemiJoinPreds), NodesIn, IntNodes1),
    select(parent(SemiJoinNodeID, DedupNodeID), GraphIn, IntGraph1),
    select(dedup(DedupNodeID, _), IntNodes1, _),

    %**** rule applies only if lower split node has one parent
    nodeHasSingleParent(DedupNodeID, GraphIn),

    %**** find the child of the split node
    select(parent(DedupNodeID, DedupNodeChild), IntGraph1, IntGraph2),
    %**** and the parents of the semijoin node
    setof(parent(SemiJoinParentID, SemiJoinNodeID),
         member(parent(SemiJoinParentID, SemiJoinNodeID), GraphIn), SemiJoinNodeParents),
    %**** re-arrange the structure of the graph
    swapNodeParents(SemiJoinNodeParents, SemiJoinNodeID, DedupNodeID, IntGraph2, IntGraph3, IntNodes1, IntNodes2),
    append([parent(SemiJoinNodeID, DedupNodeChild)], IntGraph3, IntGraph4),
    append([parent(DedupNodeID, SemiJoinNodeID)], IntGraph4, GraphOut),

    append([semijoin(SemiJoinNodeID, DedupNodeChild, SemiJoinPreds)], IntNodes2, IntNodes3),
    
    % **** update seed, output, random attribues of swapped nodes
    select(outputAttributes(SemiJoinNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(seedAttributes(SemiJoinNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    select(randomAttributes(SemiJoinNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    identifyNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodes3, IntNodes4,
                         IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    select(outputAttributes(DedupNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(seedAttributes(DedupNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    select(randomAttributes(DedupNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    identifyNodeSeedOutputRandomAttributes(DedupNodeID, GraphOut, IntNodes4, IntNodesOut,
                         IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(DedupNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    %****

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, SemiJoinNodeID), member(outerRelation(VGWrapperNodeID, SemiJoinNodeID), OuterRelationsIn), SemiJoinOuterRelations),
    findall(outerRelation(VGWrapperNodeID, DedupNodeID), member(outerRelation(VGWrapperNodeID, SemiJoinNodeID), OuterRelationsIn), DedupOuterRelations),
    subtractSet(OuterRelationsIn, SemiJoinOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, DedupOuterRelations, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushSemiJoinDownDedup(N)) ->
        retract(appliedTransformation(pushSemiJoinDownDedup(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSemiJoinDownDedup(NN)))
        ;
        assert(appliedTransformation(pushSemiJoinDownDedup(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSH OF A SELECTION DOWN A DEDUP
% Returns true if (GraphOut, NodesOut) can legally result from pushing a selection operator past a dedup operator in the query
% plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSelectionDownDedup),

    %**** find a selection operator that is above a split
    select(selection(SelectionNodeID, _), NodesIn, IntNodes1),
    select(parent(SelectionNodeID, DedupNodeID), GraphIn, IntGraph1),
    select(dedup(DedupNodeID, _), IntNodes1, _),

    %**** rule applies only if lower split node has one parent
    nodeHasSingleParent(DedupNodeID, GraphIn),

    %**** find the child of the split node
    select(parent(DedupNodeID, DedupNodeChild), IntGraph1, IntGraph2),
    %**** and the parents of the selection node
    setof(parent(SelectionParentID, SelectionNodeID),
         member(parent(SelectionParentID, SelectionNodeID), GraphIn), SelectionNodeParents),
    %**** re-arrange the structure of the graph
    swapNodeParents(SelectionNodeParents, SelectionNodeID, DedupNodeID, IntGraph2, IntGraph3, NodesIn, IntNodes2),
    append([parent(SelectionNodeID, DedupNodeChild)], IntGraph3, IntGraph4),
    append([parent(DedupNodeID, SelectionNodeID)], IntGraph4, GraphOut),

    % **** update seed, output, random attribues of swapped nodes
    select(outputAttributes(SelectionNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(seedAttributes(SelectionNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    select(randomAttributes(SelectionNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes2, IntNodes3,
                         IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    select(outputAttributes(DedupNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(seedAttributes(DedupNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    select(randomAttributes(DedupNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    identifyNodeSeedOutputRandomAttributes(DedupNodeID, GraphOut, IntNodes3, IntNodesOut,
                         IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(DedupNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    %****
    
    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, SelectionNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SelectionOuterRelations),
    findall(outerRelation(VGWrapperNodeID, DedupNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), DedupOuterRelations),
    subtractSet(OuterRelationsIn, SelectionOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, DedupOuterRelations, OuterRelationsOut),
    
    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),
    
    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushSelectionDownDedup(N)) ->
        retract(appliedTransformation(pushSelectionDownDedup(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSelectionDownDedup(NN)))
        ;
        assert(appliedTransformation(pushSelectionDownDedup(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN SELECTIONS PAST A SEED
% Returns true if (GraphOut, NodesOut) can legally result from pushing a antijoin operation past a seed operator
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushAntiJoinDownSeed),

    %**** find a antijoin operator that is above a seed
    select(antijoin(AntiJoinNodeID, SeedNodeID, AntiJoinPreds), NodesIn, IntNodes1),
    select(parent(AntiJoinNodeID, SeedNodeID), GraphIn, IntGraph1),
    member(seed(SeedNodeID, _), IntNodes1),

    %**** rule applies only if seed node has one parent
    nodeHasSingleParent(SeedNodeID, GraphIn),

    %**** find the parents of the antijoin node
    setof(parent(AntiJoinParentID, AntiJoinNodeID),
         member(parent(AntiJoinParentID, AntiJoinNodeID), GraphIn), AntiJoinNodeParents),

    %**** and the child of the seed node
    select(parent(SeedNodeID, SeedChildID), IntGraph1, IntGraph2),

    %**** re-arrange the structure of the graph
    swapNodeParents(AntiJoinNodeParents, AntiJoinNodeID, SeedNodeID, IntGraph2, IntGraph3, IntNodes1, IntNodes2),
    append([parent(AntiJoinNodeID, SeedChildID)], IntGraph3, IntGraph4),
    append([parent(SeedNodeID, AntiJoinNodeID)], IntGraph4, GraphOut),

    append([antijoin(AntiJoinNodeID, SeedChildID, AntiJoinPreds)], IntNodes2, IntNodes3),

    % **** update seed, output, random attribues of swapped nodes
    select(outputAttributes(SeedNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(seedAttributes(SeedNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    select(randomAttributes(SeedNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    identifyNodeSeedOutputRandomAttributes(SeedNodeID, GraphOut, IntNodes3, IntNodes4,
                         IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    select(outputAttributes(AntiJoinNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(seedAttributes(AntiJoinNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    select(randomAttributes(AntiJoinNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    identifyNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodes4, IntNodesOut,
                         IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(SeedNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(AntiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    %****

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, AntiJoinNodeID), member(outerRelation(VGWrapperNodeID, AntiJoinNodeID), OuterRelationsIn), AntiJoinOuterRelations),
    findall(outerRelation(VGWrapperNodeID, SeedNodeID), member(outerRelation(VGWrapperNodeID, AntiJoinNodeID), OuterRelationsIn), SeedOuterRelations),
    subtractSet(OuterRelationsIn, AntiJoinOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, SeedOuterRelations, OuterRelationsOut),
    
    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushAntiJoinDownSeed(N)) ->
        retract(appliedTransformation(pushAntiJoinDownSeed(N))),
        NN is N + 1,
        assert(appliedTransformation(pushAntiJoinDownSeed(NN)))
        ;
        assert(appliedTransformation(pushAntiJoinDownSeed(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN SELECTIONS PAST A SEED
% Returns true if (GraphOut, NodesOut) can legally result from pushing a semijoin operation past a seed operator
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSemiJoinDownSeed),

    %**** find a semijoin operator that is above a seed
    select(semijoin(SemiJoinNodeID, SeedNodeID, SemiJoinPreds), NodesIn, IntNodes1),
    select(parent(SemiJoinNodeID, SeedNodeID), GraphIn, IntGraph1),
    member(seed(SeedNodeID, _), IntNodes1),

    %**** rule applies only if seed node has one parent
    nodeHasSingleParent(SeedNodeID, GraphIn),

    %**** find the parents of the semijoin node
    setof(parent(SemiJoinParentID, SemiJoinNodeID),
         member(parent(SemiJoinParentID, SemiJoinNodeID), GraphIn), SemiJoinNodeParents),

    %**** and the child of the seed node
    select(parent(SeedNodeID, SeedChildID), IntGraph1, IntGraph2),

    %**** re-arrange the structure of the graph
    swapNodeParents(SemiJoinNodeParents, SemiJoinNodeID, SeedNodeID, IntGraph2, IntGraph3, IntNodes1, IntNodes2),
    append([parent(SemiJoinNodeID, SeedChildID)], IntGraph3, IntGraph4),
    append([parent(SeedNodeID, SemiJoinNodeID)], IntGraph4, GraphOut),

    append([semijoin(SemiJoinNodeID, SeedChildID, SemiJoinPreds)], IntNodes2, IntNodes3),
    
    % **** update seed, output, random attribues of swapped nodes
    select(outputAttributes(SeedNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(seedAttributes(SeedNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    select(randomAttributes(SeedNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    identifyNodeSeedOutputRandomAttributes(SeedNodeID, GraphOut, IntNodes3, IntNodes4,
                         IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    select(outputAttributes(SemiJoinNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(seedAttributes(SemiJoinNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    select(randomAttributes(SemiJoinNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    identifyNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodes4, IntNodesOut,
                         IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),

    updateNodeSeedOutputRandomAttributes(SeedNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(SemiJoinNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    %****

    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, SemiJoinNodeID), member(outerRelation(VGWrapperNodeID, SemiJoinNodeID), OuterRelationsIn), SemiJoinOuterRelations),
    findall(outerRelation(VGWrapperNodeID, SeedNodeID), member(outerRelation(VGWrapperNodeID, SemiJoinNodeID), OuterRelationsIn), SeedOuterRelations),
    subtractSet(OuterRelationsIn, SemiJoinOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, SeedOuterRelations, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),
    
    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushSemiJoinDownSeed(N)) ->
        retract(appliedTransformation(pushSemiJoinDownSeed(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSemiJoinDownSeed(NN)))
        ;
        assert(appliedTransformation(pushSemiJoinDownSeed(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS PUSHING DOWN SELECTIONS PAST A SEED
% Returns true if (GraphOut, NodesOut) can legally result from pushing a selection operation past a seed operator
% in the query plan represented by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(pushSelectionDownSeed),

    %**** find a selection operator that is above a seed
    select(selection(SelectionNodeID, _), NodesIn, IntNodes1),
    select(parent(SelectionNodeID, SeedNodeID), GraphIn, IntGraph1),
    member(seed(SeedNodeID, _), IntNodes1),

    %**** rule applies only if seed node has one parent
    nodeHasSingleParent(SeedNodeID, GraphIn),

    %**** find the parents of the selection node
    setof(parent(SelectionParentID, SelectionNodeID),
         member(parent(SelectionParentID, SelectionNodeID), GraphIn), SelectionNodeParents),

    %**** and the child of the seed node
    select(parent(SeedNodeID, SeedChildID), IntGraph1, IntGraph2),

    %**** re-arrange the structure of the graph
    swapNodeParents(SelectionNodeParents, SelectionNodeID, SeedNodeID, IntGraph2, IntGraph3, NodesIn, IntNodes2),
    append([parent(SelectionNodeID, SeedChildID)], IntGraph3, IntGraph4),
    append([parent(SeedNodeID, SelectionNodeID)], IntGraph4, GraphOut),

    % **** update seed, output, random attribues of swapped nodes
    select(outputAttributes(SeedNodeID, _), OutputAttrsIn, IntOutputAttrs1),
    select(seedAttributes(SeedNodeID, _), SeedAttrsIn, IntSeedAttrs1),
    select(randomAttributes(SeedNodeID, _), RandomAttrsIn, IntRandomAttrs1),
    identifyNodeSeedOutputRandomAttributes(SeedNodeID, GraphOut, IntNodes2, IntNodes3,
                         IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),
                         
    select(outputAttributes(SelectionNodeID, _), IntOutputAttrs2, IntOutputAttrs3),
    select(seedAttributes(SelectionNodeID, _), IntSeedAttrs2, IntSeedAttrs3),
    select(randomAttributes(SelectionNodeID, _), IntRandomAttrs2, IntRandomAttrs3),
    identifyNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodes3, IntNodesOut,
                         IntSeedAttrs3, IntSeedAttrs4, IntRandomAttrs3, IntRandomAttrs4, IntOutputAttrs3, IntOutputAttrs4),
                         
    updateNodeSeedOutputRandomAttributes(SeedNodeID, GraphOut, IntNodesOut, IntOutputAttrs4, IntOutputAttrs5, IntRandomAttrs4, IntRandomAttrs5, IntSeedAttrs4, IntSeedAttrs5),
    updateNodeSeedOutputRandomAttributes(SelectionNodeID, GraphOut, IntNodesOut, IntOutputAttrs5, OutputAttrsOut, IntRandomAttrs5, RandomAttrsOut, IntSeedAttrs5, SeedAttrsOut),
    %****
    
    % **** update outer relation info if necessary
    findall(outerRelation(VGWrapperNodeID, SelectionNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SelectionOuterRelations),
    findall(outerRelation(VGWrapperNodeID, SeedNodeID), member(outerRelation(VGWrapperNodeID, SelectionNodeID), OuterRelationsIn), SeedOuterRelations),
    subtractSet(OuterRelationsIn, SelectionOuterRelations, IntOuterRelations),
    mergeSets(IntOuterRelations, SeedOuterRelations, OuterRelationsOut),

    % **** update plan's candidateKeys and join types
    findall(candidateKey(NodeName, CandidateKey), (member(tablescan(NodeName, _, _), IntNodesOut), member(candidateKey(NodeName, CandidateKey), KeysIn)), PrelimKeys),
    identifyPlanCandidateKeys(GraphOut, IntNodesOut, NodesOut, PrelimKeys, KeysOut, OuterRelationsOut, OutputAttrsOut, SeedAttrsOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),
    
    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(pushSelectionDownSeed(N)) ->
        retract(appliedTransformation(pushSelectionDownSeed(N))),
        NN is N + 1,
        assert(appliedTransformation(pushSelectionDownSeed(NN)))
        ;
        assert(appliedTransformation(pushSelectionDownSeed(1)))).
        
%*****************************************************************************************************************************
% RULE THAT IMPLEMENTS MERGING OF TWO SELECTIONS INTO A SINGLE SELECTION
% Returns true if (GraphOut, NodesOut) can legally result from merging two selection operators in the query plan represented
% by (GraphIn, NodesIn)
transform(GraphIn, NodesIn, OutputAttrsIn, KeysIn, OuterRelationsIn, RandomAttrsIn, SeedAttrsIn,
          GraphOut, NodesOut, OutputAttrsOut, KeysOut, OuterRelationsOut, RandomAttrsOut, SeedAttrsOut, PlanCost) :-
    applyTransformation(mergeSelections),

    %**** find a selection operator that is above another selection
    select(selection(TopSelectionNodeID, TopSelectionNodePreds), NodesIn, IntNodes1),
    select(parent(TopSelectionNodeID, BtmSelectionNodeID), GraphIn, IntGraph1),
    select(selection(BtmSelectionNodeID, BtmSelectionNodePreds), IntNodes1, IntNodes2),

    %**** rule applies only if lower selection node has one parent
    nodeHasSingleParent(BtmSelectionNodeID, GraphIn),

    %**** find the child of the lower selection node
    select(parent(BtmSelectionNodeID, BtmSelectionNodeChild), IntGraph1, IntGraph2),

    %**** re-arrange the structure of the graph
    append([parent(TopSelectionNodeID, BtmSelectionNodeChild)], IntGraph2, GraphOut),

    %**** delete lower selection node from plan output, random and seed attributes
    select(outputAttributes(BtmSelectionNodeID, _), OutputAttrsIn, OutputAttrsOut),
    select(randomAttributes(BtmSelectionNodeID, _), RandomAttrsIn, RandomAttrsOut),
    select(seedAttributes(BtmSelectionNodeID, _), SeedAttrsIn, SeedAttrsOut),

    %**** delete lower selection node from set of candidate keys
    deleteCandidateKeyInformation(BtmSelectionNodeID, KeysIn, KeysOut),

    % **** merge selection predicates so that duplicate attributes appear only once in the plan
    mergeSets(TopSelectionNodePreds, BtmSelectionNodePreds, NewSelectionNodePreds),
    append([selection(TopSelectionNodeID, NewSelectionNodePreds)], IntNodes2, NodesOut),

    %**** check if the plan has already been generated so as not to return it again
    sort(GraphOut, SGraphOut), hash_term(SGraphOut, GraphHashKey),
    sort(NodesOut, SNodesOut), hash_term(SNodesOut, NodesHashKey),
    (hashedPlan(GraphHashKey, NodesHashKey) -> false; assert(hashedPlan(GraphHashKey, NodesHashKey))),

    OuterRelationsIn = OuterRelationsOut,

    % **** cost plan
    costPlan(GraphOut, NodesOut, OuterRelationsOut, RandomAttrsOut, OutputAttrsOut, PlanCost, _),

    (appliedTransformation(mergeSelections(N)) ->
        retract(appliedTransformation(mergeSelections(N))),
        NN is N + 1,
        assert(appliedTransformation(mergeSelections(NN)))
        ;
        assert(appliedTransformation(mergeSelections(1)))).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%*****************************************************************************************************************************
% RULE THAT DELETES INFORMATION ABOUT A NODE FROM THE SET OF CANDIDATE KEYS
deleteCandidateKeyInformation(WhichNode, KeysIn, KeysOut) :-
    (select(candidateKey(WhichNode, _), KeysIn, IntKeys) ->
        deleteCandidateKeyInformation(WhichNode, IntKeys, KeysOut);
        KeysOut = KeysIn
    ).
    
%*****************************************************************************************************************************
% RULEs THAT ADD A NEW SEED, OUTPUT AND RANDOM ATTRIBUTES OF PARTICULAR NODES
% **** version that deals with a selection node
identifyNodeSeedOutputRandomAttributes(NodeID, Graph, NodesIn, NodesOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OutputAttrsIn, OutputAttrsOut) :-
    member(selection(NodeID, SelectionPreds), NodesIn),
    member(parent(NodeID, ChildID), Graph),

    NodesOut = NodesIn,

    member(seedAttributes(ChildID, ChildSeedAttrs), SeedAttrsIn),
    append([seedAttributes(NodeID, ChildSeedAttrs)], SeedAttrsIn, SeedAttrsOut),

    member(outputAttributes(ChildID, ChildOutputAttrs), OutputAttrsIn),
    member(randomAttributes(ChildID, ChildRandomAttrs), RandomAttrsIn),
    (member(isPres, ChildOutputAttrs) ->
        SelectionOutputAttrs = ChildOutputAttrs,
        SelectionRandomAttrs = ChildRandomAttrs
        ;
        extractAttrsFromPredicates(SelectionPreds, [], PredReferencedAttrs),
        intersectSets(ChildRandomAttrs, PredReferencedAttrs, PredRandomAttrs),
        (PredRandomAttrs = [] ->
            SelectionOutputAttrs = ChildOutputAttrs,
            SelectionRandomAttrs = ChildRandomAttrs
            ;
            append([isPres], ChildOutputAttrs, SelectionOutputAttrs),
            append([isPres], ChildRandomAttrs, SelectionRandomAttrs))
    ),
    append([outputAttributes(NodeID, SelectionOutputAttrs)], OutputAttrsIn, OutputAttrsOut),
    append([randomAttributes(NodeID, SelectionRandomAttrs)], RandomAttrsIn, RandomAttrsOut).

% **** version that deals with a join node
identifyNodeSeedOutputRandomAttributes(NodeID, Graph, NodesIn, NodesOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OutputAttrsIn, OutputAttrsOut) :-
    member(join(NodeID, JoinPreds, _), NodesIn),
    member(parent(NodeID, LHSChildID), Graph),
    member(parent(NodeID, RHSChildID), Graph),
    LHSChildID @> RHSChildID,

    NodesOut = NodesIn,

    member(seedAttributes(LHSChildID, LHSChildSeedAttrs), SeedAttrsIn),
    member(seedAttributes(RHSChildID, RHSChildSeedAttrs), SeedAttrsIn),
    mergeSets(LHSChildSeedAttrs, RHSChildSeedAttrs, JoinSeedAttrs),
    append([seedAttributes(NodeID, JoinSeedAttrs)], SeedAttrsIn, SeedAttrsOut),

    member(outputAttributes(LHSChildID, LHSChildOutputAttrs), OutputAttrsIn),
    member(outputAttributes(RHSChildID, RHSChildOutputAttrs), OutputAttrsIn),
    mergeSets(LHSChildOutputAttrs, RHSChildOutputAttrs, IntJoinOutputAttrs),
    member(randomAttributes(LHSChildID, LHSChildRandomAttrs), RandomAttrsIn),
    member(randomAttributes(RHSChildID, RHSChildRandomAttrs), RandomAttrsIn),
    mergeSets(LHSChildRandomAttrs, RHSChildRandomAttrs, IntJoinRandomAttrs),
    (member(isPres, IntJoinOutputAttrs) ->
        JoinOutputAttrs = IntJoinOutputAttrs,
        JoinRandomAttrs = IntJoinRandomAttrs
        ;
        extractAttrsFromPredicates(JoinPreds, [], PredReferencedAttrs),
        intersectSets(IntJoinRandomAttrs, PredReferencedAttrs, PredRandomAttrs),
        (PredRandomAttrs = [] ->
            JoinOutputAttrs = IntJoinOutputAttrs,
            JoinRandomAttrs = IntJoinRandomAttrs
            ;
            append([isPres], IntJoinOutputAttrs, JoinOutputAttrs),
            append([isPres], IntJoinRandomAttrs, JoinRandomAttrs))
    ),
    append([outputAttributes(NodeID, JoinOutputAttrs)], OutputAttrsIn, OutputAttrsOut),
    append([randomAttributes(NodeID, JoinRandomAttrs)], RandomAttrsIn, RandomAttrsOut).

% **** version that deals with an antijoin node
identifyNodeSeedOutputRandomAttributes(NodeID, Graph, NodesIn, NodesOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OutputAttrsIn, OutputAttrsOut) :-
    member(antijoin(NodeID, DataSourceChildNodeID, AntiJoinPreds), NodesIn),
    select(parent(NodeID, DataSourceChildNodeID), Graph, IntGraph),
    member(parent(NodeID, OtherChildNodeID), IntGraph),

    NodesOut = NodesIn,

    member(seedAttributes(DataSourceChildNodeID, DataSourceChildNodeSeedAttrs), SeedAttrsIn),
    append([seedAttributes(NodeID, DataSourceChildNodeSeedAttrs)], SeedAttrsIn, SeedAttrsOut),

    member(outputAttributes(DataSourceChildNodeID, DataSourceChildNodeOutputAttrs), OutputAttrsIn),
    member(randomAttributes(DataSourceChildNodeID, DataSourceChildNodeRandomAttrs), RandomAttrsIn),
    member(randomAttributes(OtherChildNodeID, OtherChildNodeRandomAttrs), RandomAttrsIn),
    mergeSets(DataSourceChildNodeRandomAttrs, OtherChildNodeRandomAttrs, IntJoinRandomAttrs),
    (member(isPres, DataSourceChildNodeOutputAttrs) ->
        JoinOutputAttrs = DataSourceChildNodeOutputAttrs,
        JoinRandomAttrs = DataSourceChildNodeRandomAttrs
        ;
        extractAttrsFromPredicates(AntiJoinPreds, [], PredReferencedAttrs),
        intersectSets(IntJoinRandomAttrs, PredReferencedAttrs, PredRandomAttrs),
        (PredRandomAttrs = [] ->
            JoinOutputAttrs = DataSourceChildNodeOutputAttrs,
            JoinRandomAttrs = DataSourceChildNodeRandomAttrs
            ;
            append([isPres], DataSourceChildNodeOutputAttrs, JoinOutputAttrs),
            append([isPres], DataSourceChildNodeRandomAttrs, JoinRandomAttrs))
    ),
    append([outputAttributes(NodeID, JoinOutputAttrs)], OutputAttrsIn, OutputAttrsOut),
    append([randomAttributes(NodeID, JoinRandomAttrs)], RandomAttrsIn, RandomAttrsOut).

% **** version that deals with a semijoin node
identifyNodeSeedOutputRandomAttributes(NodeID, Graph, NodesIn, NodesOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OutputAttrsIn, OutputAttrsOut) :-
    member(semijoin(NodeID, DataSourceChildNodeID, SemiJoinPreds), NodesIn),
    select(parent(NodeID, DataSourceChildNodeID), Graph, IntGraph),
    member(parent(NodeID, OtherChildNodeID), IntGraph),

    NodesOut = NodesIn,

    member(seedAttributes(DataSourceChildNodeID, DataSourceChildNodeSeedAttrs), SeedAttrsIn),
    append([seedAttributes(NodeID, DataSourceChildNodeSeedAttrs)], SeedAttrsIn, SeedAttrsOut),

    member(outputAttributes(DataSourceChildNodeID, DataSourceChildNodeOutputAttrs), OutputAttrsIn),
    member(randomAttributes(DataSourceChildNodeID, DataSourceChildNodeRandomAttrs), RandomAttrsIn),
    member(randomAttributes(OtherChildNodeID, OtherChildNodeRandomAttrs), RandomAttrsIn),
    mergeSets(DataSourceChildNodeRandomAttrs, OtherChildNodeRandomAttrs, IntJoinRandomAttrs),
    (member(isPres, DataSourceChildNodeOutputAttrs) ->
        JoinOutputAttrs = DataSourceChildNodeOutputAttrs,
        JoinRandomAttrs = DataSourceChildNodeRandomAttrs
        ;
        extractAttrsFromPredicates(SemiJoinPreds, [], PredReferencedAttrs),
        intersectSets(IntJoinRandomAttrs, PredReferencedAttrs, PredRandomAttrs),
        (PredRandomAttrs = [] ->
            JoinOutputAttrs = DataSourceChildNodeOutputAttrs,
            JoinRandomAttrs = DataSourceChildNodeRandomAttrs
            ;
            append([isPres], DataSourceChildNodeOutputAttrs, JoinOutputAttrs),
            append([isPres], DataSourceChildNodeRandomAttrs, JoinRandomAttrs))
    ),
    append([outputAttributes(NodeID, JoinOutputAttrs)], OutputAttrsIn, OutputAttrsOut),
    append([randomAttributes(NodeID, JoinRandomAttrs)], RandomAttrsIn, RandomAttrsOut).

% **** version that deals with a seed node
identifyNodeSeedOutputRandomAttributes(NodeID, Graph, NodesIn, NodesOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OutputAttrsIn, OutputAttrsOut) :-
    member(seed(NodeID, SeedAttr), NodesIn),
    member(parent(NodeID, ChildID), Graph),

    NodesOut = NodesIn,

    member(seedAttributes(ChildID, ChildSeedAttrs), SeedAttrsIn),
    append([SeedAttr], ChildSeedAttrs, SeedSeedAttrs),
    append([seedAttributes(NodeID, SeedSeedAttrs)], SeedAttrsIn, SeedAttrsOut),

    member(outputAttributes(ChildID, ChildOutputAttrs), OutputAttrsIn),
    append([SeedAttr], ChildOutputAttrs, SeedOutputAttrs),
    append([outputAttributes(NodeID, SeedOutputAttrs)], OutputAttrsIn, OutputAttrsOut),

    member(randomAttributes(ChildID, ChildRandomAttrs), RandomAttrsIn),
    append([randomAttributes(NodeID, ChildRandomAttrs)], RandomAttrsIn, RandomAttrsOut).

% **** version that deals with a split node
identifyNodeSeedOutputRandomAttributes(NodeID, Graph, NodesIn, NodesOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OutputAttrsIn, OutputAttrsOut) :-
    member(split(NodeID, SplitAttrs), NodesIn),
    member(parent(NodeID, ChildID), Graph),

    NodesOut = NodesIn,

    member(seedAttributes(ChildID, ChildSeedAttrs), SeedAttrsIn),
    append([seedAttributes(NodeID, ChildSeedAttrs)], SeedAttrsIn, SeedAttrsOut),

    member(outputAttributes(ChildID, ChildOutputAttrs), OutputAttrsIn),
    member(randomAttributes(ChildID, ChildRandomAttrs), RandomAttrsIn),
    (member(isPres, ChildOutputAttrs) ->
        SplitOutputAttrs = ChildOutputAttrs,
        IntSplitRandomAttrs = ChildRandomAttrs
        ;
        append([isPres], ChildOutputAttrs, SplitOutputAttrs),
        append([isPres], ChildRandomAttrs, IntSplitRandomAttrs)
    ),

    append([outputAttributes(NodeID, SplitOutputAttrs)], OutputAttrsIn, OutputAttrsOut),
    subtractSet(IntSplitRandomAttrs, SplitAttrs, SplitRandomAttrs),
    append([randomAttributes(NodeID, SplitRandomAttrs)], RandomAttrsIn, RandomAttrsOut).

% **** version that deals with a dedup node
identifyNodeSeedOutputRandomAttributes(NodeID, Graph, NodesIn, NodesOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OutputAttrsIn, OutputAttrsOut) :-
    member(dedup(NodeID, _), NodesIn),
    member(parent(NodeID, ChildID), Graph),

    NodesOut = NodesIn,

    member(seedAttributes(ChildID, ChildSeedAttrs), SeedAttrsIn),
    append([seedAttributes(NodeID, ChildSeedAttrs)], SeedAttrsIn, SeedAttrsOut),

    member(outputAttributes(ChildID, ChildOutputAttrs), OutputAttrsIn),
    append([outputAttributes(NodeID, ChildOutputAttrs)], OutputAttrsIn, OutputAttrsOut),

    member(randomAttributes(ChildID, ChildRandomAttrs), RandomAttrsIn),
    append([randomAttributes(NodeID, ChildRandomAttrs)], RandomAttrsIn, RandomAttrsOut).

% **** version that deals with a projection node
identifyNodeSeedOutputRandomAttributes(NodeID, Graph, NodesIn, NodesOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OutputAttrsIn, OutputAttrsOut) :-
    member(projection(NodeID, ProjectionAttrs), NodesIn),
    member(parent(NodeID, ChildID), Graph),

    member(seedAttributes(ChildID, ChildSeedAttrs), SeedAttrsIn),
    intersectSets(ProjectionAttrs, ChildSeedAttrs, ProjectionSeedAttrs),
    append([seedAttributes(NodeID, ProjectionSeedAttrs)], SeedAttrsIn, SeedAttrsOut),

    member(outputAttributes(ChildID, ChildOutputAttrs), OutputAttrsIn),
    intersectSets(ProjectionAttrs, ChildOutputAttrs, IntProjectionOutputAttrs),
    member(randomAttributes(ChildID, ChildRandomAttrs), RandomAttrsIn),
    intersectSets(ProjectionAttrs, ChildRandomAttrs, IntProjectionRandomAttrs),
    (member(isPres, ChildOutputAttrs) -> % preserve isPres in the projected attributes
        mergeSets([isPres], IntProjectionOutputAttrs, ProjectionOutputAttrs),
        mergeSets([isPres], IntProjectionRandomAttrs, ProjectionRandomAttrs)
        ;
        ProjectionOutputAttrs = IntProjectionOutputAttrs,
        ProjectionRandomAttrs = IntProjectionRandomAttrs
    ),
    append([outputAttributes(NodeID, ProjectionOutputAttrs)], OutputAttrsIn, OutputAttrsOut),
    append([randomAttributes(NodeID, ProjectionRandomAttrs)], RandomAttrsIn, RandomAttrsOut),

    select(projection(NodeID, _), NodesIn, IntNodes),
    append([projection(NodeID, ProjectionOutputAttrs)], IntNodes, NodesOut).

% **** version that deals with a generalized aggregate node
identifyNodeSeedOutputRandomAttributes(NodeID, Graph, NodesIn, NodesOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OutputAttrsIn, OutputAttrsOut) :-
    member(genagg(NodeID, _, GroupByAttrs, _, AggExprsInAttrs, AggExprsOutAttrs), NodesIn),
    member(parent(NodeID, ChildID), Graph),

    NodesOut = NodesIn,

    member(seedAttributes(ChildID, ChildSeedAttrs), SeedAttrsIn),
    append([seedAttributes(NodeID, ChildSeedAttrs)], SeedAttrsIn, SeedAttrsOut),

    member(randomAttributes(ChildID, ChildRandomAttrs), RandomAttrsIn),
    identifyExpressionOutputRandomAttrs(AggExprsInAttrs, AggExprsOutAttrs, ChildRandomAttrs, [], FuncOutRandomAttrs),
    append(ChildRandomAttrs, FuncOutRandomAttrs, NodeRandomAttrs),
    append([randomAttributes(NodeID, NodeRandomAttrs)], RandomAttrsIn, RandomAttrsOut),

    flatten(AggExprsOutAttrs, FlattenedAggExprsOutAttrs),
    append(FlattenedAggExprsOutAttrs, GroupByAttrs, IntNodeOutputAttrs1),

    % **** retain isPres and seed attributes
    (member(isPres, NodeRandomAttrs) -> append([isPres], IntNodeOutputAttrs1, IntNodeOutputAttrs2); IntNodeOutputAttrs2 = IntNodeOutputAttrs1),
    append(IntNodeOutputAttrs2, ChildSeedAttrs, NodeOutputAttrs),

    append([outputAttributes(NodeID, NodeOutputAttrs)], OutputAttrsIn, OutputAttrsOut).

% **** version that deals with a scalar function node
identifyNodeSeedOutputRandomAttributes(NodeID, Graph, NodesIn, NodesOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut, OutputAttrsIn, OutputAttrsOut) :-
    member(scalarfunc(NodeID, _, FuncExprsInAttrs, FuncExprsOutAttrs), NodesIn),
    member(parent(NodeID, ChildID), Graph),

    NodesOut = NodesIn,

    member(seedAttributes(ChildID, ChildSeedAttrs), SeedAttrsIn),
    append([seedAttributes(NodeID, ChildSeedAttrs)], SeedAttrsIn, SeedAttrsOut),

    member(randomAttributes(ChildID, ChildRandomAttrs), RandomAttrsIn),
    identifyExpressionOutputRandomAttrs(FuncExprsInAttrs, FuncExprsOutAttrs, ChildRandomAttrs, [], FuncOutRandomAttrs),
    append(ChildRandomAttrs, FuncOutRandomAttrs, NodeRandomAttrs),
    append([randomAttributes(NodeID, NodeRandomAttrs)], RandomAttrsIn, RandomAttrsOut),

    member(outputAttributes(ChildID, ChildOutputAttrs), OutputAttrsIn),
    flatten(FuncExprsOutAttrs, FlattenedFuncExprsOutAttrs),
    append(ChildOutputAttrs, FlattenedFuncExprsOutAttrs, NodeOutputAttrs),
    append([outputAttributes(NodeID, NodeOutputAttrs)], OutputAttrsIn, OutputAttrsOut).
    
%*****************************************************************************************************************************
% RULE FOR UPDATE THE OUTPUT, RANDOM AND SEED ATTRIBUTES FOR A NODE BASED ON THE ATTRIBUTE REQUIREMENTS OF THE NODE'S PARENT
% **** version that deals with a projection node; has to be a top operator
updateNodeSeedOutputRandomAttributes(WhichNode, Graph, Nodes, OutputAttrsIn, OutputAttrsOut, RandomAttrsIn, RandomAttrsOut, SeedAttrsIn, SeedAttrsOut) :-
    member(projection(WhichNode, _), Nodes),

    setof(parent(ProjectionParentID, WhichNode),  % **** check that it is top operator in plan
               member(parent(ProjectionParentID, WhichNode), Graph), [parent(planRoot, WhichNode)]),
    
    select(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn, IntOutputAttrs),
    append([outputAttributes(WhichNode, NodeOutputAttrs)], IntOutputAttrs, OutputAttrsOut),

    select(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrsIn, IntRandomAttrs),
    append([randomAttributes(WhichNode, NodeRandomAttrs)], IntRandomAttrs, RandomAttrsOut),

    select(seedAttributes(WhichNode, NodeSeedAttrs), SeedAttrsIn, IntSeedAttrs),
    append([seedAttributes(WhichNode, NodeSeedAttrs)], IntSeedAttrs, SeedAttrsOut).

% **** version that deals with a selection/scalarfunc/genagg/seed/dedup/split/tablescan node
updateNodeSeedOutputRandomAttributes(WhichNode, Graph, Nodes, OutputAttrsIn, OutputAttrsOut, RandomAttrsIn, RandomAttrsOut, SeedAttrsIn, SeedAttrsOut) :-
    (member(selection(WhichNode, _), Nodes);
     member(scalarfunc(WhichNode, _, _, _), Nodes);
     member(genagg(WhichNode, _, _, _, _, _), Nodes);
     member(seed(WhichNode, _), Nodes);
     member(dedup(WhichNode, _), Nodes);
     member(split(WhichNode, _), Nodes);
     member(tablescan(WhichNode, _, _), Nodes);
     member(join(WhichNode, _, _), Nodes);
     member(semijoin(WhichNode, _, _), Nodes);
     member(vgwrapper(WhichNode, _, _, _, _, _, _, _, _), Nodes);
     member(antijoin(WhichNode, _, _), Nodes)
    ),

    % **** find parent nodes
    setof(NodeParentID, member(parent(NodeParentID, WhichNode), Graph), NodeParents),

    (NodeParents = [planRoot] ->
        OutputAttrsOut = OutputAttrsIn,
        RandomAttrsOut = RandomAttrsIn,
        SeedAttrsOut = SeedAttrsIn
        ;
        identifyRequiredAttributes(NodeParents, [], RequiredAttrs, Graph, Nodes, OutputAttrsIn, OutputAttrsIn),

        select(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrsIn, IntOutputAttrs),
        intersectSets(RequiredAttrs, NodeOutputAttrs, NewNodeOutputAttrs1),
        removeDuplicates(NewNodeOutputAttrs1, [], NewNodeOutputAttrs2),
        append([outputAttributes(WhichNode, NewNodeOutputAttrs2)], IntOutputAttrs, OutputAttrsOut),

        select(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrsIn, IntRandomAttrs),
        intersectSets(RequiredAttrs, NodeRandomAttrs, NewNodeRandomAttrs1),
        removeDuplicates(NewNodeRandomAttrs1, [], NewNodeRandomAttrs2),
        append([randomAttributes(WhichNode, NewNodeRandomAttrs2)], IntRandomAttrs, RandomAttrsOut),

        select(seedAttributes(WhichNode, NodeSeedAttrs), SeedAttrsIn, IntSeedAttrs),
        intersectSets(RequiredAttrs, NodeSeedAttrs, NewNodeSeedAttrs1),
        removeDuplicates(NewNodeSeedAttrs1, [], NewNodeSeedAttrs2),
        append([seedAttributes(WhichNode, NewNodeSeedAttrs2)], IntSeedAttrs, SeedAttrsOut)
    ).
    
%*****************************************************************************************************************************
% RULE THAT CHECKS WHETHER A NODE IS A SEED JOIN
isSeedJoin(CurrentNode, Nodes) :-
    member(join(CurrentNode, JoinPreds, _), Nodes),
    extractAttrsFromPredicates(JoinPreds, [], PredReferencedAttrs),
    findall(OuterSeedAttr, member(seed(_, OuterSeedAttr), Nodes), OuterSeedAttrs),
    findall(VGWrapperSeedAttr, member(vgwrapper(_, _, _, _, _, _, _, _, VGWrapperSeedAttr), Nodes), VGWrapperSeedAttrs),
    append(OuterSeedAttrs, VGWrapperSeedAttrs, PlanSeedAttrs),
    intersectSets(PlanSeedAttrs, PredReferencedAttrs, Seeds),
    Seeds \= [].
    
%*****************************************************************************************************************************
% RULE THAT ENSURES THAT PATH STARTING FROM A SEED JOIN CONTAINS A VGWRAPPER NODE
pathContainsVGWrapperNode(CurrentNode, Nodes, _) :-
    member(vgwrapper(CurrentNode, _, _, _, _, _, _, _, _), Nodes).

pathContainsVGWrapperNode(CurrentNode, Nodes, Graph) :-
    not(member(seed(CurrentNode, _), Nodes)),
    nodeHasSingleParent(CurrentNode, Graph),
    member(parent(CurrentNode, ChildNode), Graph),
    pathContainsVGWrapperNode(ChildNode, Nodes, Graph).
    
%*****************************************************************************************************************************
% RULE THAT DESCRIBES HOW TO PARTITION A LIST OF PREDICATES AMONG TWO JOINS
% Returns true if the first param is partitioned so that (1) all preds referring only to BtmJoinOutputAttrs are found in
% BtmJoinPredsOut and all others are found in the TopJoinPredsOut
partitionPredsAmongJoins([Pred|OtherPreds], TopJoinPredsIn, TopJoinPredsOut, BtmJoinPredsIn, BtmJoinPredsOut, BtmJoinOutputAttrs) :-
    %current_predicate(equals/3),
    %equals(Pred, LHSAtt, RHSAtt),
    %RHSAtt \= literal,

    current_predicate(compExp/5),
    compExp(Pred, equals, LHSAtt, RHSAtt, RHSAttrType),
    RHSAttrType \= literal,
    
    (member(LHSAtt, BtmJoinOutputAttrs) ->
        (member(RHSAtt, BtmJoinOutputAttrs) ->
            append([Pred], BtmJoinPredsIn, IntBtmJoinPreds),
            IntTopJoinPreds = TopJoinPredsIn
            ;
            append([Pred], TopJoinPredsIn, IntTopJoinPreds),
            IntBtmJoinPreds = BtmJoinPredsIn
        )
        ;
        append([Pred], TopJoinPredsIn, IntTopJoinPreds),
        IntBtmJoinPreds = BtmJoinPredsIn
    ),
    partitionPredsAmongJoins(OtherPreds, IntTopJoinPreds, TopJoinPredsOut, IntBtmJoinPreds, BtmJoinPredsOut, BtmJoinOutputAttrs).

partitionPredsAmongJoins([], TopJoinPredsIn, TopJoinPredsOut, BtmJoinPredsIn, BtmJoinPredsOut, _) :-
    TopJoinPredsOut = TopJoinPredsIn,
    BtmJoinPredsOut = BtmJoinPredsIn.
    
%*****************************************************************************************************************************
% RULE THAT CHECKS WHETHER ANY OF THE CANDIDATE KEYS OF A NODE IS SUBSET OF A GIVEN SET OF ATTRIBUTES
dedupCanBeEliminated([Key|OtherKeys], Attrs) :-
  %(subset(Key, Attrs) ->
  (subset(Attrs, Key) ->
      true;
      dedupCanBeEliminated(OtherKeys, Attrs)
  ).
  
dedupCanBeEliminated([], _) :- false.

dedupCanBeEliminated([[]], _) :- false.

%*****************************************************************************************************************************
% RULEs THAT PARTITION PREDICATES BASED ON THE OUTPUT ATTRIBUTES OF A SCALAR FUNCTION
partitionPredsBasedOnFuncOutputAttrs(FuncOutputAttrs, [Pred|OtherPreds], TopSelectionPredsIn, TopSelectionPredsOut, BtmSelectionPredsIn, BtmSelectionPredsOut) :-
    (predicateReferencesAttrs(Pred, FuncOutputAttrs) ->
        append([Pred], TopSelectionPredsIn, IntTopSelectionPreds),
        IntBtmSelectionPreds = BtmSelectionPredsIn
        ;
        append([Pred], BtmSelectionPredsIn, IntBtmSelectionPreds),
        IntTopSelectionPreds = TopSelectionPredsIn
    ),
    partitionPredsBasedOnFuncOutputAttrs(FuncOutputAttrs, OtherPreds, IntTopSelectionPreds, TopSelectionPredsOut, IntBtmSelectionPreds, BtmSelectionPredsOut).

partitionPredsBasedOnFuncOutputAttrs(_, [], TopSelectionPredsIn, TopSelectionPredsOut, BtmSelectionPredsIn, BtmSelectionPredsOut) :-
    TopSelectionPredsOut = TopSelectionPredsIn,
    BtmSelectionPredsOut = BtmSelectionPredsIn.
    
%*****************************************************************************************************************************
% RULEs THAT CHECK WHETHER AN ATTRIBUTE IS REFERENCED IN THE GIVEN SELECTION PREDICATE
% **** version that deals with an equality predicate
predicateReferencesAttrs(Pred, Attrs) :-
    current_predicate(compExp/5),
    compExp(Pred, equals, LHSAttr, RHSAttr, _),
    (member(LHSAttr, Attrs);
     member(RHSAttr, Attrs)).
     
    %current_predicate(equals/3),
    %equals(Pred, LHSAttr, RHSAttr),
    %(member(LHSAttr, Attrs);
     %member(RHSAttr, Attrs)).

% **** version that deals with a non-equality predicates
predicateReferencesAttrs(Pred, Attrs) :-
    %current_predicate(otherComparisonOp/4),
    %otherComparisonOp(Pred, _, LHSAttr, RHSAttr),

    current_predicate(compExp/5),
    compExp(Pred, CompExpType, LHSAttr, RHSAttr, _),
    CompExpType \= equals,
    
    (member(LHSAttr, Attrs);
     member(RHSAttr, Attrs)).

% **** version that deals with an OR predicate
predicateReferencesAttrs(Pred, Attrs) :-
    current_predicate(boolOr/2),
    boolOr(Pred, OrPreds),
    areAttributesReferencedByOrPredicates(OrPreds, Attrs).

%*****************************************************************************************************************************
% RULEs THAT CHECK WHETHER ATTRIBUTEs ARE REFERENCED IN THE GIVEN SET OF SELECTION PREDICATES
areAttributesReferencedByOrPredicates([OrPred|OtherOrPreds], Attrs) :-
    (predicateReferencesAttrs(OrPred, Attrs) -> true
        ;
        areAttributesReferencedByOrPredicates(OtherOrPreds, Attrs)).

areAttributesReferencedByOrPredicates([], _).

%*****************************************************************************************************************************
% RULEs THAT DESCRIBE HOW TO PARTITION A SET OF SELECTION PREDICATES OVER A JOIN
partitionPredsOverJoin([Pred|OtherPreds], TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn,
                                          LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, LHSChildAttrs, RHSChildAttrs) :-
    assignPredicate(Pred, TopSelPredsIn, IntTopSelPreds, JoinPredsIn, IntJoinPreds, LHSSelPredsIn, IntLHSSelPreds, RHSSelPredsIn, IntRHSSelPreds, LHSChildAttrs, RHSChildAttrs),
    partitionPredsOverJoin(OtherPreds, IntTopSelPreds, TopSelPredsOut, IntJoinPreds, JoinPredsOut, IntLHSSelPreds,
                                          LHSSelPredsOut, IntRHSSelPreds, RHSSelPredsOut, LHSChildAttrs, RHSChildAttrs).

partitionPredsOverJoin([], TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, _, _) :-
    TopSelPredsOut = TopSelPredsIn,
    JoinPredsOut = JoinPredsIn,
    LHSSelPredsOut = LHSSelPredsIn,
    RHSSelPredsOut = RHSSelPredsIn.

%*****************************************************************************************************************************
% RULE THAT ASSIGNS A SELECTION PREDICATE DESCRIBES RELATIVE TO A JOIN
% **** version that deals with an equality predicate with a constant
assignPredicate(Pred, TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, LHSChildAttrs, _) :-
    %current_predicate(equals/3),
    %equals(Pred, LHSAttr, literal),

    current_predicate(compExp/5),
    compExp(Pred, equals, LHSAttr, _, literal),
    
    TopSelPredsOut = TopSelPredsIn,
    JoinPredsOut = JoinPredsIn,
    (member(LHSAttr, LHSChildAttrs)  ->
        append([Pred], LHSSelPredsIn, LHSSelPredsOut),
        RHSSelPredsOut = RHSSelPredsIn
        ;
        append([Pred], RHSSelPredsIn, RHSSelPredsOut),
        LHSSelPredsOut = LHSSelPredsIn
    ).

% **** versions (4) that deal with an equality predicate without a constant
assignPredicate(Pred, TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, LHSChildAttrs, RHSChildAttrs) :-
    %current_predicate(equals/3),
    %equals(Pred, LHSAttr, RHSAttr),
    %RHSAttr \= literal,

    current_predicate(compExp/5),
    compExp(Pred, equals, LHSAttr, RHSAttr, RHSAttrType),
    RHSAttrType \= literal,
    
    TopSelPredsOut = TopSelPredsIn,

    member(LHSAttr, LHSChildAttrs),
    member(RHSAttr, RHSChildAttrs),

    % *** assign predicate to join
    RHSSelPredsOut = RHSSelPredsIn,
    LHSSelPredsOut = LHSSelPredsIn,
    append([Pred], JoinPredsIn, JoinPredsOut).

assignPredicate(Pred, TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, LHSChildAttrs, RHSChildAttrs) :-
    %current_predicate(equals/3),
    %equals(Pred, LHSAttr, RHSAttr),
    %RHSAttr \= literal,

    current_predicate(compExp/5),
    compExp(Pred, equals, LHSAttr, RHSAttr, RHSAttrType),
    RHSAttrType \= literal,
    
    TopSelPredsOut = TopSelPredsIn,

    member(LHSAttr, RHSChildAttrs),
    member(RHSAttr, LHSChildAttrs),

    % *** assign predicate to join
    RHSSelPredsOut = RHSSelPredsIn,
    LHSSelPredsOut = LHSSelPredsIn,
    append([Pred], JoinPredsIn, JoinPredsOut).

assignPredicate(Pred, TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, LHSChildAttrs, _) :-
    %current_predicate(equals/3),
    %equals(Pred, LHSAttr, RHSAttr),
    %RHSAttr \= literal,

    current_predicate(compExp/5),
    compExp(Pred, equals, LHSAttr, RHSAttr, RHSAttrType),
    RHSAttrType \= literal,
    
    TopSelPredsOut = TopSelPredsIn,

    member(LHSAttr, LHSChildAttrs),
    member(RHSAttr, LHSChildAttrs),

    % *** assign predicate to left child
    RHSSelPredsOut = RHSSelPredsIn,
    JoinPredsIn = JoinPredsOut,
    append([Pred], LHSSelPredsIn, LHSSelPredsOut).

assignPredicate(Pred, TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, _, RHSChildAttrs) :-
    %current_predicate(equals/3),
    %equals(Pred, LHSAttr, RHSAttr),
    %RHSAttr \= literal,

    current_predicate(compExp/5),
    compExp(Pred, equals, LHSAttr, RHSAttr, RHSAttrType),
    RHSAttrType \= literal,
    
    TopSelPredsOut = TopSelPredsIn,

    member(LHSAttr, RHSChildAttrs),
    member(RHSAttr, RHSChildAttrs),

    % *** assign predicate to right child
    LHSSelPredsOut = LHSSelPredsIn,
    JoinPredsIn = JoinPredsOut,
    append([Pred], RHSSelPredsIn, RHSSelPredsOut).

% **** version that deals with an OR predicate
assignPredicate(Pred, TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, LHSChildAttrs, RHSChildAttrs) :-
    current_predicate(boolOr/2),
    boolOr(Pred, _),
    extractAttrsFromSinglePredicate(Pred, [], PredAttrs),
    (subset(PredAttrs, LHSChildAttrs) ->
        RHSSelPredsOut = RHSSelPredsIn,
        TopSelPredsOut = TopSelPredsIn,
        JoinPredsOut = JoinPredsIn,
        append([Pred], LHSSelPredsIn, LHSSelPredsOut)
        ;
        (subset(PredAttrs, RHSChildAttrs) ->
            LHSSelPredsOut = LHSSelPredsIn,
            TopSelPredsOut = TopSelPredsIn,
            JoinPredsOut = JoinPredsIn,
            append([Pred], RHSSelPredsIn, RHSSelPredsOut)
            ;
            RHSSelPredsOut = RHSSelPredsIn,
            LHSSelPredsOut = LHSSelPredsIn,
            JoinPredsOut = JoinPredsIn,
            append([Pred], TopSelPredsIn, TopSelPredsOut)
        )
    ).
    %RHSSelPredsOut = RHSSelPredsIn,
    %LHSSelPredsOut = LHSSelPredsIn,
    %JoinPredsOut = JoinPredsIn,
    %append([Pred], TopSelPredsIn, TopSelPredsOut).

% **** version that deals with a non-equality predicate with a constant
assignPredicate(Pred, TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, LHSChildAttrs, _) :-
    %current_predicate(otherComparisonOp/4),
    %otherComparisonOp(Pred, _, LHSAttr, literal),
    
    current_predicate(compExp/5),
    compExp(Pred, CompExpType, LHSAttr, _, literal),
    CompExpType \= equals,
    
    TopSelPredsOut = TopSelPredsIn,
    JoinPredsOut = JoinPredsIn,
    (member(LHSAttr, LHSChildAttrs)  ->
        append([Pred], LHSSelPredsIn, LHSSelPredsOut),
        RHSSelPredsOut = RHSSelPredsIn
        ;
        append([Pred], RHSSelPredsIn, RHSSelPredsOut),
        LHSSelPredsOut = LHSSelPredsIn
    ).

% **** versions (4) that deal with a non-equality predicate without a constant
assignPredicate(Pred, TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, LHSChildAttrs, RHSChildAttrs):-
    %current_predicate(otherComparisonOp/4),
    %otherComparisonOp(Pred, _, LHSAttr, RHSAttr),
    %RHSAttr \= literal,

    current_predicate(compExp/5),
    compExp(Pred, CompExpType, LHSAttr, RHSAttr, RHSAttrType),
    CompExpType \= equals,
    RHSAttrType \= literal,
    
    JoinPredsOut = JoinPredsIn,

    member(LHSAttr, LHSChildAttrs),
    member(RHSAttr, RHSChildAttrs),

    % **** assign predicate to top selection
    RHSSelPredsOut = RHSSelPredsIn,
    LHSSelPredsOut = LHSSelPredsIn,
    append([Pred], TopSelPredsIn, TopSelPredsOut).

assignPredicate(Pred, TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, LHSChildAttrs, RHSChildAttrs):-
    %current_predicate(otherComparisonOp/4),
    %otherComparisonOp(Pred, _, LHSAttr, RHSAttr),
    %RHSAttr \= literal,

    current_predicate(compExp/5),
    compExp(Pred, CompExpType, LHSAttr, RHSAttr, RHSAttrType),
    CompExpType \= equals,
    RHSAttrType \= literal,
    
    JoinPredsOut = JoinPredsIn,

    member(LHSAttr, RHSChildAttrs),
    member(RHSAttr, LHSChildAttrs),

    % **** assign predicate to top selection
    RHSSelPredsOut = RHSSelPredsIn,
    LHSSelPredsOut = LHSSelPredsIn,
    append([Pred], TopSelPredsIn, TopSelPredsOut).

assignPredicate(Pred, TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, LHSChildAttrs, _):-
    %current_predicate(otherComparisonOp/4),
    %otherComparisonOp(Pred, _, LHSAttr, RHSAttr),
    %RHSAttr \= literal,

    current_predicate(compExp/5),
    compExp(Pred, CompExpType, LHSAttr, RHSAttr, RHSAttrType),
    CompExpType \= equals,
    RHSAttrType \= literal,
    
    JoinPredsOut = JoinPredsIn,

    member(LHSAttr, LHSChildAttrs),
    member(RHSAttr, LHSChildAttrs),

    % **** assign predicate to left child
    RHSSelPredsOut = RHSSelPredsIn,
    TopSelPredsOut = TopSelPredsIn,
    append([Pred], LHSSelPredsIn, LHSSelPredsOut).

assignPredicate(Pred, TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, _, RHSChildAttrs):-
    %current_predicate(otherComparisonOp/4),
    %otherComparisonOp(Pred, _, LHSAttr, RHSAttr),
    %RHSAttr \= literal,

    current_predicate(compExp/5),
    compExp(Pred, CompExpType, LHSAttr, RHSAttr, RHSAttrType),
    CompExpType \= equals,
    RHSAttrType \= literal,
    
    JoinPredsOut = JoinPredsIn,

    member(LHSAttr, RHSChildAttrs),
    member(RHSAttr, RHSChildAttrs),

    % **** assign predicate to right child
    TopSelPredsOut = TopSelPredsIn,
    LHSSelPredsOut = LHSSelPredsIn,
    append([Pred], RHSSelPredsIn, RHSSelPredsOut).
    
%*****************************************************************************************************************************
% RULEs THAT PARTITION PREDICATES ON THOSE THAT REFERENCE GROUP BY ATTRIBUTES AND THOSE THAT DO NOT
partitionPredsBasedOnGenAggAttrs(GenAggOutAttrs, [Pred|OtherPreds], TopSelectionPredsIn, TopSelectionPredsOut, BtmSelectionPredsIn, BtmSelectionPredsOut) :-
    extractAttrsFromPredicates([Pred], [], PredReferencedAttrs),
    intersectSets(GenAggOutAttrs, PredReferencedAttrs, PredRandomAttrs),
    (PredRandomAttrs = [] ->
        append([Pred], BtmSelectionPredsIn, IntBtmSelectionPreds),
        IntTopSelectionPreds = TopSelectionPredsIn
        ;
        append([Pred], TopSelectionPredsIn, IntTopSelectionPreds),
        IntBtmSelectionPreds = BtmSelectionPredsIn
    ),
    partitionPredsBasedOnGenAggAttrs(GenAggOutAttrs, OtherPreds, IntTopSelectionPreds, TopSelectionPredsOut, IntBtmSelectionPreds, BtmSelectionPredsOut).

partitionPredsBasedOnGenAggAttrs(_, [], TopSelectionPredsIn, TopSelectionPredsOut, BtmSelectionPredsIn, BtmSelectionPredsOut) :-
    TopSelectionPredsOut = TopSelectionPredsIn,
    BtmSelectionPredsOut = BtmSelectionPredsIn.
    
%*****************************************************************************************************************************
% RULE THAT PARTITIONS THE PREDICATES OF A SEMIJOIN OR AN ANTIJOIN AMONG THE CHILDREN OF THE JOIN BELOW THE LATTERS
partitionSemiOrAntiJoinPredsOverJoin([Pred|OtherPreds], LHSPredsIn, LHSPredsOut, RHSPredsIn, RHSPredsOut, LHSChildOutputAttrs, RHSChildOutputAttrs) :-
    %current_predicate(equals/3),
    %equals(Pred, LHSAttr, RHSAttr),
    %RHSAttr \= literal,

    current_predicate(compExp/5),
    compExp(Pred, equals, LHSAttr, RHSAttr, RHSAttrType),
    RHSAttrType \= literal,
    
    ((member(LHSAttr, LHSChildOutputAttrs); member(RHSAttr, LHSChildOutputAttrs)) ->
        append([Pred], LHSPredsIn, IntLHSPreds1),
        IntRHSPreds1 = RHSPredsIn
        ;
        IntLHSPreds1 = LHSPredsIn,
        IntRHSPreds1 = RHSPredsIn
    ),
    ((member(LHSAttr, RHSChildOutputAttrs); member(RHSAttr, RHSChildOutputAttrs)) ->
        append([Pred], IntRHSPreds1, IntRHSPreds2),
        IntLHSPreds2 = IntLHSPreds1
        ;
        IntLHSPreds2 = IntLHSPreds1,
        IntRHSPreds2 = IntRHSPreds1
    ),
    partitionSemiOrAntiJoinPredsOverJoin(OtherPreds, IntLHSPreds2, LHSPredsOut, IntRHSPreds2, RHSPredsOut, LHSChildOutputAttrs, RHSChildOutputAttrs).

partitionSemiOrAntiJoinPredsOverJoin([], LHSPredsIn, LHSPredsOut, RHSPredsIn, RHSPredsOut, _, _) :-
    LHSPredsOut = LHSPredsIn,
    RHSPredsOut = RHSPredsIn.
    
%*****************************************************************************************************************************
% RULEs THAT FIND THE BASE RELATION OF A VGWRAPPER
findVGWrapperBaseOuterRelation(CurrentNode, BaseOuterRelation, Graph) :-
    nodeHasSingleParent(CurrentNode, Graph),
    member(parent(CurrentNode, ChildNode), Graph),
    findVGWrapperBaseOuterRelation(ChildNode, BaseOuterRelation, Graph).

findVGWrapperBaseOuterRelation(CurrentNode, BaseOuterRelation, Graph) :-
    not(nodeHasSingleParent(CurrentNode, Graph)),
    BaseOuterRelation = CurrentNode.
    
%*****************************************************************************************************************************
% RULES THAT FIND THE TOP LEVEL SEED JOIN RELATION OF A VGWRAPPER
findTopLevelSeedJoin(CurrentNode, TopLevelSeedJoinNodeID, Graph, Nodes, JoinSeedAttr) :-
    member(parent(ParentNode, CurrentNode), Graph),
    nodeHasSingleParent(ParentNode, Graph),
    member(join(ParentNode, JoinPreds, _), Nodes),
    %findall(LHSJoinPredAttr, (member(Pred, JoinPreds), equals(Pred, LHSJoinPredAttr, _)), LHSJoinPredAttrs),
    findall(LHSJoinPredAttr, (member(Pred, JoinPreds), compExp(Pred, equals, LHSJoinPredAttr, _, _)), LHSJoinPredAttrs),
    (member(JoinSeedAttr, LHSJoinPredAttrs) ->
        TopLevelSeedJoinNodeID = ParentNode
        ;
        %findall(RHSJoinPredAttr, (member(Pred, JoinPreds), equals(Pred, _, RHSJoinPredAttr)), RHSJoinPredAttrs),
        findall(RHSJoinPredAttr, (member(Pred, JoinPreds), compExp(Pred, equals, _, RHSJoinPredAttr, _)), RHSJoinPredAttrs),
        (member(JoinSeedAttr, RHSJoinPredAttrs) ->
            TopLevelSeedJoinNodeID = ParentNode
            ;
            findTopLevelSeedJoin(ParentNode, TopLevelSeedJoinNodeID, Graph, Nodes, JoinSeedAttr)
        )
    ).

findTopLevelSeedJoin(CurrentNode, TopLevelSeedJoinNodeID, Graph, Nodes, JoinSeedAttr) :-
    member(parent(ParentNode, CurrentNode), Graph),
    nodeHasSingleParent(ParentNode, Graph),
    not(member(join(ParentNode, _, _), Nodes)),
    findTopLevelSeedJoin(ParentNode, TopLevelSeedJoinNodeID, Graph, Nodes, JoinSeedAttr).
    
%*****************************************************************************************************************************
% RULEs TO IMPOSE SOME VGWRAPPER ORDER SO THAT THE SAME RULE IS ONLY FIRED ONCE PER BRANCH
rankNodes([Node|OtherNodes], AnchorNode) :-
    AnchorNode @> Node,
    rankNodes(OtherNodes, AnchorNode).

rankNodes([], _).

%*****************************************************************************************************************************
% RULEs THAT CREATE DEDUP NODES AT EACH VGWRAPPER PATH
createDedupNodesAtVGWrappers([Node|OtherNodes], SeedAttrIn, OuterRelation, NodesIn, NodesOut, GraphIn, GraphOut,
                                                OuterRelationsIn, OuterRelationsOut, OutputAttrsIn, OutputAttrsOut,
                                                SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut) :-
    gensym(dedupNode, NewDedupNodeID),
    append([dedup(NewDedupNodeID, [SeedAttrIn])], NodesIn, IntNodes1),

    select(parent(Node, OuterRelation), GraphIn, IntGraph1),
    append([parent(Node, NewDedupNodeID)], IntGraph1, IntGraph2),
    append([parent(NewDedupNodeID, OuterRelation)], IntGraph2, IntGraph),

    (member(vgwrapper(Node, _, _, _, _, _, _, _, _), IntNodes1) ->
        select(outerRelation(Node, _), OuterRelationsIn, IntOuterRelations1),
        append([outerRelation(Node, NewDedupNodeID)], IntOuterRelations1, IntOuterRelations)
        ;
        IntOuterRelations = OuterRelationsIn
    ),

    % **** add seed, output and random attributes for dedup node
    identifyNodeSeedOutputRandomAttributes(NewDedupNodeID, IntGraph, IntNodes1, IntNodes2, SeedAttrsIn, IntSeedAttrs1,
                                                         RandomAttrsIn, IntRandomAttrs1, OutputAttrsIn, IntOutputAttrs1),

    updatePathToVGWrapper(NewDedupNodeID, IntGraph, IntNodes2, IntNodes3, IntSeedAttrs1, IntSeedAttrs2, IntRandomAttrs1,
                                                         IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    createDedupNodesAtVGWrappers(OtherNodes, SeedAttrIn, OuterRelation, IntNodes3, NodesOut, IntGraph, GraphOut,
                                                IntOuterRelations, OuterRelationsOut, IntOutputAttrs2, OutputAttrsOut,
                                                IntSeedAttrs2, SeedAttrsOut, IntRandomAttrs2, RandomAttrsOut).

createDedupNodesAtVGWrappers([], _, _, NodesIn, NodesOut, GraphIn, GraphOut, OuterRelationsIn, OuterRelationsOut,
                                          OutputAttrsIn, OutputAttrsOut, SeedAttrsIn, SeedAttrsOut, RandomAttrsIn, RandomAttrsOut) :-
    NodesOut = NodesIn,
    GraphOut = GraphIn,
    OuterRelationsOut = OuterRelationsIn,
    OutputAttrsOut = OutputAttrsIn,
    SeedAttrsOut = SeedAttrsIn,
    RandomAttrsOut = RandomAttrsIn.

%*****************************************************************************************************************************
% UPDATE OUTPUT, SEED AND RANDOM ATTRIBUTES ALONG THE PATH FROM AN OPERATOR TO ITS RELATED VGWRAPPER
updatePathToVGWrapper(CurrentNode, Graph, NodesIn, NodesOut, SeedAttrsIn, SeedAttrsOut,
                                                  RandomAttrsIn, RandomAttrsOut, OutputAttrsIn, OutputAttrsOut) :-
    nodeHasSingleParent(CurrentNode, Graph),
    member(parent(ParentNode, CurrentNode), Graph),
    not(member(vgwrapper(ParentNode, _, _, _, _, _, _, _, _), NodesIn)),
    not(member(projection(ParentNode, _), NodesIn)),

    (select(seedAttributes(ParentNode, _), SeedAttrsIn, IntSeedAttrs1) ->  true; IntSeedAttrs1 = SeedAttrsIn),
    (select(randomAttributes(ParentNode, _), RandomAttrsIn, IntRandomAttrs1) ->  true; IntRandomAttrs1 = RandomAttrsIn),
    (select(outputAttributes(ParentNode, _), OutputAttrsIn, IntOutputAttrs1) ->  true; IntOutputAttrs1 = OutputAttrsIn),
    identifyNodeSeedOutputRandomAttributes(ParentNode, Graph, NodesIn, IntNodes, IntSeedAttrs1, IntSeedAttrs2,
                                                         IntRandomAttrs1, IntRandomAttrs2, IntOutputAttrs1, IntOutputAttrs2),

    updatePathToVGWrapper(ParentNode, Graph, IntNodes, NodesOut, IntSeedAttrs2, SeedAttrsOut,
                                                  IntRandomAttrs2, RandomAttrsOut, IntOutputAttrs2, OutputAttrsOut).

updatePathToVGWrapper(CurrentNode, Graph, NodesIn, NodesOut, SeedAttrsIn, SeedAttrsOut,
                                                  RandomAttrsIn, RandomAttrsOut, OutputAttrsIn, OutputAttrsOut) :-
    nodeHasSingleParent(CurrentNode, Graph),
    member(parent(ParentNode, CurrentNode), Graph),
    (member(vgwrapper(ParentNode, _, _, _, _, _, _, _, _), NodesIn);
     member(projection(ParentNode, _), NodesIn)),
    SeedAttrsOut = SeedAttrsIn,
    RandomAttrsOut = RandomAttrsIn,
    OutputAttrsOut = OutputAttrsIn,
    NodesOut = NodesIn.
    
%*****************************************************************************************************************************
% RULEs THAT IDENTIFY AN ANCESTOR OF A VGWRAPPER BELOW WHICH THE VGWRAPPER CAN BE PLACED
% version that deals with a node that does not require the attributes of the VGWrapper
findVGWrapperDependantAncestorPath(CurrentNode, VGWAttrs, Graph, Nodes, RequiredAttrsIn, RequiredAttrsOut, NodePathIn, NodePathOut) :-
    nodeHasSingleParent(CurrentNode, Graph),
    member(parent(ParentNode, CurrentNode), Graph),
    
    % **** VGWrapper cannot be copied past a dedup, genagg and vgwrapper
    not(member(dedup(ParentNode, _), Nodes);
        member(genagg(ParentNode, _, _, _, _, _), Nodes);
        member(vgwrapper(ParentNode, _, _, _, _, _, _, _, _), Nodes);
        member(projection(ParentNode, _), Nodes)),
        
    not(nodeRequiresAttributes(ParentNode, VGWAttrs, _, Nodes)),

    append(NodePathIn, [ParentNode], IntNodePath),
    findVGWrapperDependantAncestorPath(ParentNode, VGWAttrs, Graph, Nodes, RequiredAttrsIn, RequiredAttrsOut,
                                                           IntNodePath, NodePathOut).

findVGWrapperDependantAncestorPath(CurrentNode, VGWAttrs, Graph, Nodes, RequiredAttrsIn, RequiredAttrsOut, NodePathIn, NodePathOut) :-
    nodeHasSingleParent(CurrentNode, Graph),
    member(parent(ParentNode, CurrentNode), Graph),
    
    % **** VGWrapper cannot be copied past a dedup, genagg and vgwrapper
    not(member(dedup(ParentNode, _), Nodes);
        member(genagg(ParentNode, _, _, _, _, _), Nodes);
        member(vgwrapper(ParentNode, _, _, _, _, _, _, _, _), Nodes)),

    nodeRequiresAttributes(ParentNode, VGWAttrs, _, Nodes),

    append(NodePathIn, [ParentNode], NodePathOut),
    RequiredAttrsOut = RequiredAttrsIn.

findVGWrapperDependantAncestorPath(CurrentNode, VGWAttrs, Graph, Nodes, RequiredAttrsIn, RequiredAttrsOut, NodePathIn, NodePathOut) :-
    nodeHasSingleParent(CurrentNode, Graph),
    member(parent(ParentNode, CurrentNode), Graph),
    
    % **** VGWrapper cannot be copied past a dedup, genagg and vgwrapper
    not(member(dedup(ParentNode, _), Nodes);
        member(genagg(ParentNode, _, _, _, _, _), Nodes);
        member(vgwrapper(ParentNode, _, _, _, _, _, _, _, _), Nodes);
        member(projection(ParentNode, _), Nodes)),

    nodeRequiresAttributes(ParentNode, VGWAttrs, RequiredAttrs, Nodes),

    append(NodePathIn, [ParentNode], IntNodePath),
    mergeSets(RequiredAttrsIn, RequiredAttrs, IntRequiredAttrs),
    findVGWrapperDependantAncestorPath(ParentNode, VGWAttrs, Graph, Nodes, IntRequiredAttrs,
                                                RequiredAttrsOut, IntNodePath, NodePathOut).
                                                
%*****************************************************************************************************************************
% RULEs THAT CHECK WHETHER A PARTICULAR OPERATOR REQUIRES A SUBSET OF A GIVEN SET OF ATTRIBUTES AND RETURNS
% WHICH ATTRIBUTES ARE REQUIRED
% version that deals with selections
nodeRequiresAttributes(WhichNode, Attrs, WhichAttrs, Nodes) :-
    (member(selection(WhichNode, Preds), Nodes);
     member(semijoin(WhichNode, _, Preds), Nodes);
     member(antijoin(WhichNode, _, Preds), Nodes);
     member(join(WhichNode, Preds, _), Nodes)
    ),
    extractAttrsFromPredicates(Preds, [], ReferencedAttrs),
    intersectSets(Attrs, ReferencedAttrs, WhichAttrs),
    WhichAttrs \= [].

% version that deals with splits
nodeRequiresAttributes(WhichNode, Attrs, WhichAttrs, Nodes) :-
    member(split(WhichNode, SplitAttrs), Nodes),
    intersectSets(Attrs, SplitAttrs, WhichAttrs),
    WhichAttrs \= [].

% version that deals with projections
nodeRequiresAttributes(WhichNode, Attrs, WhichAttrs, Nodes) :-
    member(projection(WhichNode, ProjectedAttrs), Nodes),
    intersectSets(Attrs, ProjectedAttrs, WhichAttrs),
    WhichAttrs \= [].
    
% version that deals with scalar funcs
nodeRequiresAttributes(WhichNode, Attrs, WhichAttrs, Nodes) :-
    member(scalarfunc(WhichNode, _, FuncExprsInAttrs, _), Nodes),
    flatten(FuncExprsInAttrs, FlattenedFuncExprsInAttrs),
    removeDuplicates(FlattenedFuncExprsInAttrs, [], DFlattenedFuncExprsInAttrs),
    intersectSets(Attrs, DFlattenedFuncExprsInAttrs, WhichAttrs),
    WhichAttrs \= [].
    
%*****************************************************************************************************************************
% RULEs THAT CREATE THE INNER CHILDREN EDGES FOR A VGWRAPPER
addVGWrapperInnerChildren(VGWrapperNodeID, [ChildNode|OtherNodes], GraphIn, GraphOut) :-
    append([parent(VGWrapperNodeID, ChildNode)], GraphIn, IntGraph),
    addVGWrapperInnerChildren(VGWrapperNodeID, OtherNodes, IntGraph, GraphOut).

addVGWrapperInnerChildren(_, [], GraphIn, GraphOut) :-  GraphOut = GraphIn.

%*****************************************************************************************************************************
% RULEs THAT IDENTIFY THE RENAMED ATTRIBUTE PAIRS
identifyRenameAttrPairs([-|OtherFuncExprs], [[InAttr]|OtherInAttrs], [[OutAttr]|OtherOutAttrs], RenameAttrPairsIn, RenameAttrPairsOut) :-
    append([rename(InAttr, OutAttr)], RenameAttrPairsIn, IntRenameAttrPairs),
    identifyRenameAttrPairs(OtherFuncExprs, OtherInAttrs, OtherOutAttrs, IntRenameAttrPairs, RenameAttrPairsOut).
    
identifyRenameAttrPairs([FuncExpr|OtherFuncExprs], [_|OtherInAttrs], [_|OtherOutAttrs], RenameAttrPairsIn, RenameAttrPairsOut) :-
    FuncExpr \= -,
    identifyRenameAttrPairs(OtherFuncExprs, OtherInAttrs, OtherOutAttrs, RenameAttrPairsIn, RenameAttrPairsOut).

identifyRenameAttrPairs([], [], [], RenameAttrPairsIn, RenameAttrPairsOut) :-
    RenameAttrPairsOut = RenameAttrPairsIn.

%*****************************************************************************************************************************
% RULEs THAT PARTITION PREDICATES BASED ON A RENAMING OF ATTRIBUTES
partitionPredsBasedOnRenamedAttrs([Pred|OtherPreds], RenameAttrPairs, FuncOutAttrs, TopSelectionPredsIn, TopSelectionPredsOut, BtmSelectionPredsIn, BtmSelectionPredsOut) :-
    (predicateReferencesRenamedAttrs(Pred, RenamedPred, RenameAttrPairs, FuncOutAttrs) ->
        append([RenamedPred], BtmSelectionPredsIn, IntBtmSelectionPreds),
        IntTopSelectionPreds = TopSelectionPredsIn
        ;
        append([Pred], TopSelectionPredsIn, IntTopSelectionPreds),
        IntBtmSelectionPreds = BtmSelectionPredsIn
    ),
    partitionPredsBasedOnRenamedAttrs(OtherPreds, RenameAttrPairs, FuncOutAttrs, IntTopSelectionPreds, TopSelectionPredsOut, IntBtmSelectionPreds, BtmSelectionPredsOut).

partitionPredsBasedOnRenamedAttrs([], _, _, TopSelectionPredsIn, TopSelectionPredsOut, BtmSelectionPredsIn, BtmSelectionPredsOut) :-
    TopSelectionPredsOut = TopSelectionPredsIn,
    BtmSelectionPredsOut = BtmSelectionPredsIn.
    
%*****************************************************************************************************************************
% RULEs THAT CHECK WHETHER A (RENAMED) ATTRIBUTE IS REFERENCED IN THE GIVEN SELECTION PREDICATE
% **** version that deals with a 'attr op literal' predicate (1)
predicateReferencesRenamedAttrs(Pred, RenamedPred, RenameAttrPairs, _) :-
    current_predicate(compExp/5),
    compExp(Pred, PredType, LHSAttr, RHSAttr, literal),
    member(rename(InLHSAttr, LHSAttr), RenameAttrPairs),
    % **** rename predicate attributes
    gensym(selectionPred, RenamedPred),
    assert(compExp(RenamedPred, PredType, InLHSAttr, RHSAttr, literal)).

% **** version that deals with a 'attr op literal' predicate (2)
predicateReferencesRenamedAttrs(Pred, RenamedPred, RenameAttrPairs, FuncOutAttrs) :-
    current_predicate(compExp/5),
    compExp(Pred, _, LHSAttr, _, literal),
    not(member(rename(_, LHSAttr), RenameAttrPairs)),
    not(member(LHSAttr, FuncOutAttrs)),
    RenamedPred = Pred.
    
% **** version that deals with a 'attr op attr' predicate
predicateReferencesRenamedAttrs(Pred, RenamedPred, RenameAttrPairs, FuncOutAttrs) :-
    current_predicate(compExp/5),
    compExp(Pred, PredType, LHSAttr, RHSAttr, identifier),
    (member(rename(NewLHSAttr, LHSAttr), RenameAttrPairs) ->
        true
        ;
        not(member(LHSAttr, FuncOutAttrs)),
        NewLHSAttr = LHSAttr
    ),
    (member(rename(NewRHSAttr, RHSAttr), RenameAttrPairs) ->
        true
        ;
        not(member(RHSAttr, FuncOutAttrs)),
        NewRHSAttr = RHSAttr
    ),
    (NewRHSAttr = RHSAttr, NewLHSAttr = LHSAttr ->
        RenamedPred = Pred
        ;
        gensym(joinPred, RenamedPred),
        assert(compExp(RenamedPred, PredType, NewLHSAttr, NewRHSAttr, identifier))
    ).

% **** version that deals with an OR predicate
predicateReferencesRenamedAttrs(Pred, RenamedPred, RenameAttrPairs, InAttrs) :-
    current_predicate(boolOr/2),
    boolOr(Pred, OrPreds),
    areRenamedAttributesReferencedByOrPredicates(OrPreds, [], NewOrPreds, RenameAttrPairs, InAttrs),
    (equalSets(NewOrPreds, OrPreds) ->
        RenamedPred = Pred
        ;
        gensym(orPred, RenamedPred),
        assert(orPred(RenamedPred, NewOrPreds))
    ).

%*****************************************************************************************************************************
% RULEs THAT CHECK WHETHER ALL RENAMED ATTRIBUTEs ARE REFERENCED IN THE GIVEN SET OF SELECTION PREDICATES
areRenamedAttributesReferencedByOrPredicates([Pred|OtherPreds], NewOrPredsIn, NewOrPredsOut, RenameAttrPairs, InAttrs) :-
    predicateReferencesRenamedAttrs(Pred, RenamedPred, RenameAttrPairs, InAttrs),
    append([RenamedPred], NewOrPredsIn, IntNewOrPreds),
    areRenamedAttributesReferencedByOrPredicates(OtherPreds, IntNewOrPreds, NewOrPredsOut, RenameAttrPairs, InAttrs).
    
areRenamedAttributesReferencedByOrPredicates([], NewOrPredsIn, NewOrPredsOut, _, _) :-
    NewOrPredsOut = NewOrPredsIn.

%*****************************************************************************************************************************
% RULEs THAT ELIMINATE REDUNDANT GENERATED ATTRIBUTEs
eliminateRedundantAttributeGeneration([FuncExpr|OtherFuncExprs], NewFuncExprsIn, NewFuncExprsOut, [FuncExprsInAttrList|OtherFuncExprsInAttrs],
                NewFuncExprsInAttrsIn, NewFuncExprsInAttrsOut, [FuncExprsOutAttrList|OtherFuncExprsOutAttrs], NewFuncExprsOutAttrsIn, NewFuncExprsOutAttrsOut, OutputAttrs) :-
    (subset(FuncExprsOutAttrList, OutputAttrs) ->
        append([FuncExpr], NewFuncExprsIn, IntNewFuncExprs),
        append([FuncExprsInAttrList], NewFuncExprsInAttrsIn, IntNewFuncExprsInAttrs),
        append([FuncExprsOutAttrList], NewFuncExprsOutAttrsIn, IntNewFuncExprsOutAttrs)
        ;
        IntNewFuncExprs = NewFuncExprsIn,
        IntNewFuncExprsInAttrs = NewFuncExprsInAttrsIn,
        IntNewFuncExprsOutAttrs = NewFuncExprsOutAttrsIn
    ),
    eliminateRedundantAttributeGeneration(OtherFuncExprs, IntNewFuncExprs, NewFuncExprsOut, OtherFuncExprsInAttrs, IntNewFuncExprsInAttrs, NewFuncExprsInAttrsOut,
               OtherFuncExprsOutAttrs, IntNewFuncExprsOutAttrs, NewFuncExprsOutAttrsOut, OutputAttrs).

eliminateRedundantAttributeGeneration([], NewFuncExprsIn, NewFuncExprsOut, [], NewFuncExprsInAttrsIn,
                                  NewFuncExprsInAttrsOut, [], NewFuncExprsOutAttrsIn, NewFuncExprsOutAttrsOut, _) :-
    NewFuncExprsOut = NewFuncExprsIn,
    NewFuncExprsInAttrsOut = NewFuncExprsInAttrsIn,
    NewFuncExprsOutAttrsOut = NewFuncExprsOutAttrsIn.

%*****************************************************************************************************************************
% RULEs THAT FINDS A SUBPLAN THAT IS EQUIVALENT TO A GIVEN MATERIALIZED VIEW
% rule that deals with the case where the two nodes are selection nodes
findEquivalentSubPlan(ViewNode, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNode, PlanGraph, PlanNodes, MPlanNodesIn, MPlanNodesOut) :-
    member(selection(ViewNode, ViewNodeSelectionPreds), MVNodes),
    member(selection(PlanNode, PlanNodeSelectionPreds), PlanNodes),
    length(ViewNodeSelectionPreds, ViewNodeSelectionPredsLen),
    length(PlanNodeSelectionPreds, PlanNodeSelectionPredsLen),
    ViewNodeSelectionPredsLen = PlanNodeSelectionPredsLen,
    
    % **** check whether all selection attributes are equivalent
    findall(compExp(Pred, PredType, LHSAttr, RHSAttr, RHSAttrType), (member(Pred, ViewNodeSelectionPreds), member(compExp(Pred, PredType, LHSAttr, RHSAttr, RHSAttrType), MVPreds)), Preds2a),
    findall(boolOr(OrPred, OrPreds), (member(OrPred, ViewNodeSelectionPreds), member(boolOr(OrPred, OrPreds), MVPreds)), Preds2b),
    append(Preds2a, Preds2b, Preds2),
    allEquivalentPredicates(PlanNodeSelectionPreds, Preds2),
    
    (member(PlanNode, MPlanNodesIn) ->
        IntMPlanNodes = MPlanNodesIn
        ;
        append([PlanNode], MPlanNodesIn, IntMPlanNodes)),
    
    member(parent(ViewNode, ViewNodeChild), MVGraph),
    member(parent(PlanNode, PlanNodeChild), PlanGraph),
    findEquivalentSubPlan(ViewNodeChild, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNodeChild, PlanGraph, PlanNodes, IntMPlanNodes, MPlanNodesOut).

% rule that deals with the case where the two nodes are tablescan nodes
findEquivalentSubPlan(ViewNode, _, MVNodes, _, _, _, PlanNode, _, PlanNodes, MPlanNodesIn, MPlanNodesOut) :-
    member(tablescan(ViewNode, RelationName, OutAttrs1), MVNodes),
    member(tablescan(PlanNode, RelationName, OutAttrs2), PlanNodes),
    equalSets(OutAttrs1, OutAttrs2),
    (member(PlanNode, MPlanNodesIn) ->
        MPlanNodesOut = MPlanNodesIn
        ;
        append([PlanNode], MPlanNodesIn, MPlanNodesOut)).
    
% rule that deals with the case where the two nodes are join nodes
findEquivalentSubPlan(ViewNode, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNode, PlanGraph, PlanNodes, MPlanNodesIn, MPlanNodesOut) :-
    member(join(ViewNode, ViewNodeJoinPreds, JoinType), MVNodes),
    member(join(PlanNode, PlanNodeJoinPreds, JoinType), PlanNodes),
    length(ViewNodeJoinPreds, ViewNodeJoinPredsLen),
    length(PlanNodeJoinPreds, PlanNodeJoinPredsLen),
    ViewNodeJoinPredsLen = PlanNodeJoinPredsLen,

    % **** check whether all join predicates are equivalent
    findall(compExp(Pred, PredType, LHSAttr, RHSAttr, RHSAttrType), (member(Pred, ViewNodeJoinPreds), member(compExp(Pred, PredType, LHSAttr, RHSAttr, RHSAttrType), MVPreds)), Preds2a),
    findall(boolOr(OrPred, OrPreds), (member(OrPred, ViewNodeJoinPreds), member(boolOr(OrPred, OrPreds), MVPreds)), Preds2b),
    append(Preds2a, Preds2b, Preds2),
    allEquivalentPredicates(PlanNodeJoinPreds, Preds2),

    (member(PlanNode, MPlanNodesIn) ->
        IntMPlanNodes1 = MPlanNodesIn
        ;
        append([PlanNode], MPlanNodesIn, IntMPlanNodes1)),
    
    select(parent(ViewNode, ViewNodeLHSChildID), MVGraph, IntMVGraph),
    member(parent(ViewNode, ViewNodeRHSChildID), IntMVGraph),
    ViewNodeLHSChildID @> ViewNodeRHSChildID,
    
    select(parent(PlanNode, PlanNodeLHSChildID), PlanGraph, IntPlanGraph),
    member(parent(PlanNode, PlanNodeRHSChildID), IntPlanGraph),
    
    findEquivalentSubPlan(ViewNodeLHSChildID, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNodeLHSChildID, PlanGraph, PlanNodes, IntMPlanNodes1, IntMPlanNodes2),
    findEquivalentSubPlan(ViewNodeRHSChildID, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNodeRHSChildID, PlanGraph, PlanNodes, IntMPlanNodes2, MPlanNodesOut).
    
% rule that deals with the case where the two nodes are dedup nodes
findEquivalentSubPlan(ViewNode, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNode, PlanGraph, PlanNodes, MPlanNodesIn, MPlanNodesOut) :-
    member(dedup(ViewNode, ViewNodeDedupAttrs), MVNodes),
    member(dedup(PlanNode, PlanNodeDedupAttrs), PlanNodes),
    equalSets(ViewNodeDedupAttrs, PlanNodeDedupAttrs),
    (member(PlanNode, MPlanNodesIn) ->
        IntMPlanNodes = MPlanNodesIn
        ;
        append([PlanNode], MPlanNodesIn, IntMPlanNodes)),
    
    member(parent(ViewNode, ViewNodeChild), MVGraph),
    member(parent(PlanNode, PlanNodeChild), PlanGraph),
    findEquivalentSubPlan(ViewNodeChild, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNodeChild, PlanGraph, PlanNodes, IntMPlanNodes, MPlanNodesOut).

% rule that deals with the case where the two nodes are projection nodes
findEquivalentSubPlan(ViewNode, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNode, PlanGraph, PlanNodes, MPlanNodesIn, MPlanNodesOut) :-
    member(projection(ViewNode, ViewNodeProjectionAttrs), MVNodes),
    member(projection(PlanNode, PlanNodeProjectionAttrs), PlanNodes),
    equalSets(ViewNodeProjectionAttrs, PlanNodeProjectionAttrs),
    (member(PlanNode, MPlanNodesIn) ->
        IntMPlanNodes = MPlanNodesIn
        ;
        append([PlanNode], MPlanNodesIn, IntMPlanNodes)),

    member(parent(ViewNode, ViewNodeChild), MVGraph),
    member(parent(PlanNode, PlanNodeChild), PlanGraph),
    findEquivalentSubPlan(ViewNodeChild, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNodeChild, PlanGraph, PlanNodes, IntMPlanNodes, MPlanNodesOut).

% rule that deals with the case where the two nodes are anti-join nodes
findEquivalentSubPlan(ViewNode, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNode, PlanGraph, PlanNodes, MPlanNodesIn, MPlanNodesOut) :-
    member(antijoin(ViewNode, ViewNodeDataSourceChild, AntiJoinPreds1), MVNodes),
    member(antijoin(PlanNode, PlanNodeDataSourceChild, AntiJoinPreds2), PlanNodes),
    length(AntiJoinPreds1, AntiJoinPreds1Len),
    length(AntiJoinPreds2, AntiJoinPreds2Len),
    AntiJoinPreds1Len = AntiJoinPreds2Len,
    
    % **** check whether all predicates are equivalent
    findall(compExp(Pred, PredType, LHSAttr, RHSAttr, RHSAttrType), (member(Pred, AntiJoinPreds1), member(compExp(Pred, PredType, LHSAttr, RHSAttr, RHSAttrType), MVPreds)), Preds2a),
    findall(boolOr(OrPred, OrPreds), (member(OrPred, AntiJoinPreds1), member(boolOr(OrPred, OrPreds), MVPreds)), Preds2b),
    append(Preds2a, Preds2b, Preds2),
    allEquivalentPredicates(AntiJoinPreds2, Preds2),
    
    (member(PlanNode, MPlanNodesIn) ->
        IntMPlanNodes1 = MPlanNodesIn
        ;
        append([PlanNode], MPlanNodesIn, IntMPlanNodes1)),
    
    findEquivalentSubPlan(ViewNodeDataSourceChild, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNodeDataSourceChild, PlanGraph, PlanNodes, IntMPlanNodes1, IntMPlanNodes2),
    
    select(parent(ViewNode, ViewNodeDataSourceChild), MVGraph, IntMVGraph),
    member(parent(ViewNode, ViewNodeOtherChild), IntMVGraph),
    
    select(parent(PlanNode, PlanNodeDataSourceChild), PlanGraph, IntPlanGraph),
    member(parent(PlanNode, PlanNodeOtherChild), IntPlanGraph),
    findEquivalentSubPlan(ViewNodeOtherChild, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNodeOtherChild, PlanGraph, PlanNodes, IntMPlanNodes2, MPlanNodesOut).
    
% rule that deals with the case where the two nodes are semi-join nodes
findEquivalentSubPlan(ViewNode, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNode, PlanGraph, PlanNodes, MPlanNodesIn, MPlanNodesOut) :-
    member(semijoin(ViewNode, ViewNodeDataSourceChild, SemiJoinPreds1), MVNodes),
    member(semijoin(PlanNode, PlanNodeDataSourceChild, SemiJoinPreds2), PlanNodes),
    length(SemiJoinPreds1, SemiJoinPreds1Len),
    length(SemiJoinPreds2, SemiJoinPreds2Len),
    SemiJoinPreds1Len = SemiJoinPreds2Len,
    
    % **** check whether all predicates are equivalent
    findall(compExp(Pred, PredType, LHSAttr, RHSAttr, RHSAttrType), (member(Pred, SemiJoinPreds1), member(compExp(Pred, PredType, LHSAttr, RHSAttr, RHSAttrType), MVPreds)), Preds2a),
    findall(boolOr(OrPred, OrPreds), (member(OrPred, SemiJoinPreds1), member(boolOr(OrPred, OrPreds), MVPreds)), Preds2b),
    append(Preds2a, Preds2b, Preds2),
    allEquivalentPredicates(SemiJoinPreds2, Preds2),
    
    (member(PlanNode, MPlanNodesIn) ->
        IntMPlanNodes1 = MPlanNodesIn
        ;
        append([PlanNode], MPlanNodesIn, IntMPlanNodes1)),

    findEquivalentSubPlan(ViewNodeDataSourceChild, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNodeDataSourceChild, PlanGraph, PlanNodes, IntMPlanNodes1, IntMPlanNodes2),

    select(parent(ViewNode, ViewNodeDataSourceChild), MVGraph, IntMVGraph),
    member(parent(ViewNode, ViewNodeOtherChild), IntMVGraph),

    select(parent(PlanNode, PlanNodeDataSourceChild), PlanGraph, IntPlanGraph),
    member(parent(PlanNode, PlanNodeOtherChild), IntPlanGraph),
    findEquivalentSubPlan(ViewNodeOtherChild, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNodeOtherChild, PlanGraph, PlanNodes, IntMPlanNodes2, MPlanNodesOut).
    
% rule that deals with the case where the two nodes are scalarfunc nodes
findEquivalentSubPlan(ViewNode, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNode, PlanGraph, PlanNodes, MPlanNodesIn, MPlanNodesOut) :-
    member(scalarfunc(ViewNode, FuncExprs1, _, _), MVNodes),
    member(scalarfunc(PlanNode, FuncExprs2, _, _), PlanNodes),
    length(FuncExprs1, FuncExprs1Len),
    length(FuncExprs2, FuncExprs2Len),
    FuncExprs1Len = FuncExprs2Len,
    serializeArithmeticExpressions(FuncExprs1, [], SerFuncExprs1, MVArithmExprs),
    serializeArithmeticExpressions(FuncExprs2, [], SerFuncExprs2, nothing),
    allEquivalentSerializedExpressions(SerFuncExprs1, SerFuncExprs2),
    (member(PlanNode, MPlanNodesIn) ->
        IntMPlanNodes = MPlanNodesIn
        ;
        append([PlanNode], MPlanNodesIn, IntMPlanNodes)),

    member(parent(ViewNode, ViewNodeChild), MVGraph),
    member(parent(PlanNode, PlanNodeChild), PlanGraph),
    findEquivalentSubPlan(ViewNodeChild, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNodeChild, PlanGraph, PlanNodes, IntMPlanNodes, MPlanNodesOut).

% rule that deals with the case where the two nodes are genagg nodes
findEquivalentSubPlan(ViewNode, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNode, PlanGraph, PlanNodes, MPlanNodesIn, MPlanNodesOut) :-
    member(genagg(ViewNode, _, GroupByAttrs1, AggExprs1, _, _), MVNodes),
    member(genagg(PlanNode, _, GroupByAttrs2, AggExprs2, _, _), PlanNodes),
    equalSets(GroupByAttrs1, GroupByAttrs2),
    length(AggExprs1, AggExprs1Len),
    length(AggExprs2, AggExprs2Len),
    AggExprs1Len = AggExprs2Len,
    serializeAggregateExpressions(AggExprs1, [], SerAggExprs1, MVArithmExprs, MVAggExprs),
    serializeAggregateExpressions(AggExprs2, [], SerAggExprs2, nothing, nothing),
    allEquivalentSerializedExpressions(SerAggExprs1, SerAggExprs2),
    (member(PlanNode, MPlanNodesIn) ->
        IntMPlanNodes = MPlanNodesIn
        ;
        append([PlanNode], MPlanNodesIn, IntMPlanNodes)),

    member(parent(ViewNode, ViewNodeChild), MVGraph),
    member(parent(PlanNode, PlanNodeChild), PlanGraph),
    findEquivalentSubPlan(ViewNodeChild, MVGraph, MVNodes, MVPreds, MVArithmExprs, MVAggExprs, PlanNodeChild, PlanGraph, PlanNodes, IntMPlanNodes, MPlanNodesOut).

% converts aggregate expressions from a recursive form to a flat form
serializeAggregateExpressions([AggExpr|OtherAggExprs], SerAggExprsIn, SerAggExprsOut, MVArithmExprs, MVAggExprs) :-
    serializeSingleAggregateExpression(AggExpr, SerAggExpr, MVArithmExprs, MVAggExprs),
    append([SerAggExpr], SerAggExprsIn, IntSerAggExprs),
    serializeAggregateExpressions(OtherAggExprs, IntSerAggExprs, SerAggExprsOut, MVArithmExprs, MVAggExprs).

serializeAggregateExpressions([], SerAggExprsIn, SerAggExprsOut, _, _) :-
    SerAggExprsOut = SerAggExprsIn.
    
serializeSingleAggregateExpression(AggExpr, SerAggExpr, _, MVAggExprs) :-
    (MVAggExprs = nothing ->
        aggExp(AggExpr, AggExprName, Attr, AttrType)
        ;
        member(aggExp(AggExpr, AggExprName, Attr, AttrType), MVAggExprs)),
    AttrType \= expression,
    SerAggExpr = exp(AggExprName, Attr).

serializeSingleAggregateExpression(AggExpr, SerAggExpr, MVArithmExprs, MVAggExprs) :-
    (MVAggExprs = nothing ->
        aggExp(AggExpr, AggExprName, Attr, AttrType)
        ;
        member(aggExp(AggExpr, AggExprName, Attr, AttrType), MVAggExprs)),
    AttrType = expression,
    serializeSingleArithmeticExpression(Attr, SerAttr, MVArithmExprs),
    SerAggExpr = exp(AggExprName, SerAttr).
    
% converts binary arithmetic expressions from a recursive form to a flat form
serializeArithmeticExpressions([ArithmExpr|OtherArithmExprs], SerArithmExprsIn, SerArithmExprsOut, MVArithmExprs) :-
    serializeSingleArithmeticExpression(ArithmExpr, SerArithmExpr, MVArithmExprs),
    append([SerArithmExpr], SerArithmExprsIn, IntSerArithmExprs),
    serializeArithmeticExpressions(OtherArithmExprs, IntSerArithmExprs, SerArithmExprsOut, MVArithmExprs).
    
serializeArithmeticExpressions([], SerArithmExprsIn, SerArithmExprsOut, _) :-
    SerArithmExprsOut = SerArithmExprsIn.
    
serializeSingleArithmeticExpression(ArithmExpr, SerArithmExpr, MVArithmExprs) :-
    (MVArithmExprs = nothing ->
        arithExp(ArithmExpr, ArithmExprType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType)
        ;
        member(arithExp(ArithmExpr, ArithmExprType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType), MVArithmExprs)),
    LHSAttrType \= expression,
    RHSAttrType \= expression,
    SerArithmExpr = exp(ArithmExprType, LHSAttr, RHSAttr).
    
serializeSingleArithmeticExpression(ArithmExpr, SerArithmExpr, MVArithmExprs) :-
    (MVArithmExprs = nothing ->
        arithExp(ArithmExpr, ArithmExprType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType)
        ;
        member(arithExp(ArithmExpr, ArithmExprType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType), MVArithmExprs)),
    LHSAttrType = expression,
    serializeSingleArithmeticExpression(LHSAttr, SerLHSAttr, MVArithmExprs),
    RHSAttrType \= expression,
    SerArithmExpr = exp(ArithmExprType, SerLHSAttr, RHSAttr).
    
serializeSingleArithmeticExpression(ArithmExpr, SerArithmExpr, MVArithmExprs) :-
    (MVArithmExprs = nothing ->
        arithExp(ArithmExpr, ArithmExprType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType)
        ;
        member(arithExp(ArithmExpr, ArithmExprType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType), MVArithmExprs)),
    LHSAttrType \= expression,
    RHSAttrType = expression,
    serializeSingleArithmeticExpression(RHSAttr, SerRHSAttr, MVArithmExprs),
    SerArithmExpr = exp(ArithmExprType, LHSAttr, SerRHSAttr).
    
serializeSingleArithmeticExpression(ArithmExpr, SerArithmExpr, MVArithmExprs) :-
    (MVArithmExprs = nothing ->
        arithExp(ArithmExpr, ArithmExprType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType)
        ;
        member(arithExp(ArithmExpr, ArithmExprType, LHSAttr, LHSAttrType, RHSAttr, RHSAttrType), MVArithmExprs)),
    LHSAttrType = expression,
    serializeSingleArithmeticExpression(LHSAttr, SerLHSAttr, MVArithmExprs),
    RHSAttrType = expression,
    serializeSingleArithmeticExpression(RHSAttr, SerRHSAttr, MVArithmExprs),
    SerArithmExpr = exp(ArithmExprType, SerLHSAttr, SerRHSAttr).
    
serializeSingleArithmeticExpression(ArithmExpr, SerArithmExpr, MVArithmExprs) :-
    (MVArithmExprs = nothing ->
        function(ArithmExpr, FunctName, FuncAttrs)
        ;
        member(function(ArithmExpr, FunctName, FuncAttrs), MVArithmExprs)),
    serializeFunctionParameters(FuncAttrs, [], SerFuncParams, MVArithmExprs),
    SerArithmExpr = exp(FunctName, SerFuncParams).

serializeFunctionParameters([[FuncAttr, expression]|OtherFuncAttrs], SerFuncParamsIn, SerFuncParamsOut, MVArithmExprs) :-
    serializeSingleArithmeticExpression(FuncAttr, SerFuncAttr, MVArithmExprs),
    append([SerFuncAttr], SerFuncParamsIn, IntSerFuncParams),
    serializeFunctionParameters(OtherFuncAttrs, IntSerFuncParams, SerFuncParamsOut, MVArithmExprs).
    
serializeFunctionParameters([[FuncAttr, FuncAttrType]|OtherFuncAttrs], SerFuncParamsIn, SerFuncParamsOut, MVArithmExprs) :-
    FuncAttrType \= expression,
    SerFuncAttr = exp(param, FuncAttr, null),
    append([SerFuncAttr], SerFuncParamsIn, IntSerFuncParams),
    serializeFunctionParameters(OtherFuncAttrs, IntSerFuncParams, SerFuncParamsOut, MVArithmExprs).
    
serializeFunctionParameters([], SerFuncParamsIn, SerFuncParamsOut, _) :-
    SerFuncParamsOut = SerFuncParamsIn.
    
allEquivalentSerializedExpressions([SerFuncExpr1|OtherSerFuncExprs1], SerFuncExprs2) :-
    select(SerFuncExpr1, SerFuncExprs2, OtherSerFuncExprs2),
    allEquivalentSerializedExpressions(OtherSerFuncExprs1, OtherSerFuncExprs2).

allEquivalentSerializedExpressions([], []).
    
% check whether the predicates in two lists are all equivalent
allEquivalentPredicates([Pred|OtherPreds1], Preds) :-
    hasEquivalentPredicate(Pred, Preds, OtherPreds2),
    allEquivalentPredicates(OtherPreds1, OtherPreds2).

allEquivalentPredicates([], []).

hasEquivalentPredicate(Pred, PredsIn, PredsOut) :-
    current_predicate(compExp/5),
    compExp(Pred, PredType, LHSAttr, RHSAttr, RHSAttrType),
    select(compExp(_, PredType, LHSAttr, RHSAttr, RHSAttrType), PredsIn, PredsOut).

hasEquivalentPredicate(Pred, PredsIn, PredsOut) :-
    current_predicate(compExp/5),
    compExp(Pred, PredType, LHSAttr, RHSAttr, RHSAttrType),
    select(compExp(_, PredType, RHSAttr, LHSAttr, RHSAttrType), PredsIn, PredsOut).

hasEquivalentPredicate(Pred, PredsIn, PredsOut) :-
    current_predicate(boolOr/2),
    boolOr(Pred, OrPreds),
    setof(OrPreds1, member(boolOr(_, OrPreds1), PredsIn), OtherOrPreds),
    oneEquivalentPredicate(OrPreds, OtherOrPreds, PredsIn, PredsOut).
    
oneEquivalentPredicate(OrPreds1, [OrPreds2|OtherOrPreds], PredsIn, PredsOut) :-
    (current_predicate(compExp/5) ->
        findall(compExp(Pred, PredType, LHSAttr, RHSAttr, RHSAttrType), (member(Pred, OrPreds2), compExp(Pred, PredType, LHSAttr, RHSAttr, RHSAttrType)), Preds2a)
        ;
        Preds2a = []
    ),
    (current_predicate(boolOr/2) ->
        findall(boolOr(OrPred, OrPreds), (member(OrPred, OrPreds2), boolOr(OrPred, OrPreds)), Preds2b)
        ;
        Preds2b = []
    ),
    append(Preds2a, Preds2b, Preds2),
    (allEquivalentPredicates(OrPreds1, Preds2) ->
        select(boolOr(_, OrPreds2), PredsIn, PredsOut)
        ;
        oneEquivalentPredicate(OrPreds1, OtherOrPreds, PredsIn, PredsOut)
    ).

%*****************************************************************************************************************************
% RULE THAT ADDS THE PARENTS OF A MATERIALIZED VIEW IN THE PLAN
addMaterializedNodeParents(NodeName, [parent(ParentNode, _)|OtherParents], GraphIn, GraphOut) :-
    append([parent(ParentNode, NodeName)], GraphIn, IntGraph),
    addMaterializedNodeParents(NodeName, OtherParents, IntGraph, GraphOut).
    
addMaterializedNodeParents(_, [], GraphIn, GraphOut) :-
    GraphOut = GraphIn.
    
%*****************************************************************************************************************************
% RULE THAT DELETES A SET OF NODES FROM A PLAN
deleteMaterializedNodesFromPlan([Node|OtherNodes], GraphIn, GraphOut, NodesIn, NodesOut) :-
    findall(parent(ParentNode, Node), member(parent(ParentNode, Node), GraphIn), NodesToDeleteFromGraph),
    subtractSet(GraphIn, NodesToDeleteFromGraph, IntGraph),
    (select(selection(Node, _), NodesIn, IntNodes);
     select(tablescan(Node, _, _), NodesIn, IntNodes);
     select(join(Node, _, _), NodesIn, IntNodes);
     select(antijoin(Node, _, _), NodesIn, IntNodes);
     select(semijoin(Node, _, _), NodesIn, IntNodes);
     select(projection(Node, _), NodesIn, IntNodes);
     select(dedup(Node, _), NodesIn, IntNodes);
     select(genagg(Node, _, _, _, _, _), NodesIn, IntNodes);
     select(scalarfunc(Node, _, _, _), NodesIn, IntNodes)),
    deleteMaterializedNodesFromPlan(OtherNodes, IntGraph, GraphOut, IntNodes, NodesOut).
    
deleteMaterializedNodesFromPlan(_, GraphIn, GraphOut, NodesIn, NodesOut) :-
    GraphOut = GraphIn,
    NodesOut = NodesIn.
    
%*****************************************************************************************************************************
% RULE THAT APPENDS A LIST OF FACTS TO THE GLOBAL MEMORY IF NOT THERE
addToGlogalMemory([Fact|OtherFacts]) :-
    (Fact -> true; assert(Fact)),
    addToGlogalMemory(OtherFacts).

addToGlogalMemory([]).

%*****************************************************************************************************************************
% RULES THAT DESCRIBE HOW TO PARTITION SCALAR FUNCTIONS OVER A JOIN
partitionScalarFuncOverJoin(ScalarFunctionNodeID, LHSChildAttrs, RHSChildAttrs, POS) :-
    scalarfunc(ScalarFunctionNodeID, FuncExprs, FuncExprsInAttrs, FuncExprsOutAttrs),
    flatten(FuncExprsInAttrs, FlattenedFuncExprsInAttrs),
    (subset(FlattenedFuncExprsInAttrs, LHSChildAttrs) ->
        POS is 1
        ;
        (subset(FlattenedFuncExprsInAttrs, RHSChildAttrs) ->
	    POS is 2
            ;
	    POS is 3
        )
    ).

%*****************************************************************************************************************************
% RULE THAT ADDS ATTRIBUTES IN A JOIN PREDICATE TO A PROJECTION
% **** version that deals with an equality predicate with a constant
addAttsToProjection(Pred, TopSelPredsIn, TopSelPredsOut, JoinPredsIn, JoinPredsOut, LHSSelPredsIn, LHSSelPredsOut, RHSSelPredsIn, RHSSelPredsOut, LHSChildAttrs, _) :-
    %current_predicate(equals/3),
    %equals(Pred, LHSAttr, literal),

    current_predicate(compExp/5),
    compExp(Pred, equals, LHSAttr, _, literal),
    
    TopSelPredsOut = TopSelPredsIn,
    JoinPredsOut = JoinPredsIn,
    (member(LHSAttr, LHSChildAttrs)  ->
        append([Pred], LHSSelPredsIn, LHSSelPredsOut),
        RHSSelPredsOut = RHSSelPredsIn
        ;
        append([Pred], RHSSelPredsIn, RHSSelPredsOut),
        LHSSelPredsOut = LHSSelPredsIn
    ).

