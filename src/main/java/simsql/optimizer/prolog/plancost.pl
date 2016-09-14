

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
% RULE FOR RECURSIVELY COSTING THE ENTIRE PLAN
costPlan(Graph, Nodes, OuterRelations, RandomAttrs, OutputAttrs, PlanCost, CostsOut) :-
    %projectVGWrapperOuterSeeds(Graph, IntGraph, Nodes, IntNodes, OutputAttrs, IntOutputAttrs,
                                       %OuterRelations, OuterRelations, IntOuterRelations, RandomAttrs, IntRandomAttrs),
    %member(parent(planRoot, ChildNode), IntGraph),
    %computePlanCostMeasures(ChildNode, IntGraph, IntNodes, IntOuterRelations, IntRandomAttrs, IntOutputAttrs, [], CostsOut),
    %computePlanCost(CostsOut, CostsOut, IntGraph, IntNodes, 0, PlanCost).
    member(parent(planRoot, ChildNode), Graph),
    computePlanCostMeasures(ChildNode, Graph, Nodes, OuterRelations, RandomAttrs, OutputAttrs, [], CostsOut),
    computePlanCost(CostsOut, CostsOut, Graph, Nodes, 0, PlanCost).

%*****************************************************************************************************************************
% RULE FOR ESTIMATING THE COST MODEL REGRESSORS
estimateplancosts(Graph, Nodes, OuterRelations, RandomAttrs, OutputAttrs, EstimatedCostsOut) :-
    member(parent(planRoot, ChildNode), Graph),
    computePlanCostMeasures(ChildNode, Graph, Nodes, OuterRelations, RandomAttrs, OutputAttrs, [], IntCostsOut),
    EstimatedCostsIn = [operatorInputEstimate(selection, 0), operatorOutputEstimate(selection, 0), operatorInputEstimate(seed, 0), operatorOutputEstimate(seed, 0), operatorInputEstimate(split, 0), operatorOutputEstimate(split, 0), operatorInputEstimate(join, 0), operatorOutputEstimate(join, 0), operatorInputEstimate(antijoin, 0), operatorOutputEstimate(antijoin, 0), operatorInputEstimate(semijoin, 0), operatorOutputEstimate(semijoin, 0), operatorInputEstimate(dedup, 0), operatorOutputEstimate(dedup, 0), operatorInputEstimate(projection, 0), operatorOutputEstimate(projection, 0), operatorInputEstimate(genagg, 0), operatorOutputEstimate(genagg, 0), operatorInputEstimate(vgwrapper, 0), operatorOutputEstimate(vgwrapper, 0), operatorInputEstimate(scalarfunc, 0), operatorOutputEstimate(scalarfunc, 0)],
    mergePlanCosts(IntCostsOut, IntCostsOut, Graph, Nodes, EstimatedCostsIn, EstimatedCostsOut).
    
%*****************************************************************************************************************************
% RULE THAT WEIGHT-SUMS THE COST FOR EACH OPERATOR IN THE PLAN
mergePlanCosts([stats(WhichNode, _, _, _, _, OutputSize)|OtherCosts], Costs, Graph, Nodes, PlanCostIn, PlanCostOut) :-
        (member(tablescan(WhichNode, _, _), Nodes) ->
        IntPlanCost = PlanCostIn
        ;
        % **** find operator's children
        setof(ChildNode, member(parent(WhichNode, ChildNode), Graph), ChildNodes),
        % **** and add together their relation sizes
        sumOfChildreRelationSize(ChildNodes, Costs, 0, InputSize),

        updateOperatorInputOutputEstimates(WhichNode, Nodes, OutputSize, InputSize, PlanCostIn, IntPlanCost)
    ),
    mergePlanCosts(OtherCosts, Costs, Graph, Nodes, IntPlanCost, PlanCostOut).

mergePlanCosts([], _, _, _, PlanCostIn, PlanCostOut) :-
    PlanCostOut = PlanCostIn.

%**** update estimates for selection operator
updateOperatorInputOutputEstimates(WhichNode, Nodes, OutputSize, InputSize, PlanCostIn, PlanCostOut) :-
    member(selection(WhichNode, _), Nodes),
    (member(operatorInputEstimate(selection, InSize), PlanCostIn) ->
        select(operatorInputEstimate(selection, InSize), PlanCostIn, IntPlanCost1),
        select(operatorOutputEstimate(selection, OutSize), IntPlanCost1, IntPlanCost2),
        ISize is InSize + InputSize,
        OSize is OutSize + OutputSize,
        append([operatorInputEstimate(selection, ISize)], IntPlanCost2, IntPlanCost3),
        append([operatorOutputEstimate(selection, OSize)], IntPlanCost3, PlanCostOut)
        ;
        append([operatorInputEstimate(selection, InputSize)], PlanCostIn, IntPlanCost),
        append([operatorOutputEstimate(selection, OutputSize)], IntPlanCost, PlanCostOut)
    ).

%**** update estimates for seed operator
updateOperatorInputOutputEstimates(WhichNode, Nodes, OutputSize, InputSize, PlanCostIn, PlanCostOut) :-
    member(seed(WhichNode, _), Nodes),
    (member(operatorInputEstimate(seed, InSize), PlanCostIn) ->
        select(operatorInputEstimate(seed, InSize), PlanCostIn, IntPlanCost1),
        select(operatorOutputEstimate(seed, OutSize), IntPlanCost1, IntPlanCost2),
        ISize is InSize + InputSize,
        OSize is OutSize + OutputSize,
        append([operatorInputEstimate(seed, ISize)], IntPlanCost2, IntPlanCost3),
        append([operatorOutputEstimate(seed, OSize)], IntPlanCost3, PlanCostOut)
        ;
        append([operatorInputEstimate(seed, InputSize)], PlanCostIn, IntPlanCost),
        append([operatorOutputEstimate(seed, OutputSize)], IntPlanCost, PlanCostOut)
    ).

%**** update estimates for split operator
updateOperatorInputOutputEstimates(WhichNode, Nodes, OutputSize, InputSize, PlanCostIn, PlanCostOut) :-
    member(split(WhichNode, _), Nodes),
    (member(operatorInputEstimate(split, InSize), PlanCostIn) ->
        select(operatorInputEstimate(split, InSize), PlanCostIn, IntPlanCost1),
        select(operatorOutputEstimate(split, OutSize), IntPlanCost1, IntPlanCost2),
        ISize is InSize + InputSize,
        OSize is OutSize + OutputSize,
        append([operatorInputEstimate(split, ISize)], IntPlanCost2, IntPlanCost3),
        append([operatorOutputEstimate(split, OSize)], IntPlanCost3, PlanCostOut)
        ;
        append([operatorInputEstimate(split, InputSize)], PlanCostIn, IntPlanCost),
        append([operatorOutputEstimate(split, OutputSize)], IntPlanCost, PlanCostOut)
    ).

%**** update estimates for join operator
updateOperatorInputOutputEstimates(WhichNode, Nodes, OutputSize, InputSize, PlanCostIn, PlanCostOut) :-
    member(join(WhichNode, _, _), Nodes),
    (member(operatorInputEstimate(join, InSize), PlanCostIn) ->
        select(operatorInputEstimate(join, InSize), PlanCostIn, IntPlanCost1),
        select(operatorOutputEstimate(join, OutSize), IntPlanCost1, IntPlanCost2),
        ISize is InSize + InputSize,
        OSize is OutSize + OutputSize,
        append([operatorInputEstimate(join, ISize)], IntPlanCost2, IntPlanCost3),
        append([operatorOutputEstimate(join, OSize)], IntPlanCost3, PlanCostOut)
        ;
        append([operatorInputEstimate(join, InputSize)], PlanCostIn, IntPlanCost),
        append([operatorOutputEstimate(join, OutputSize)], IntPlanCost, PlanCostOut)
    ).

%**** update estimates for antijoin operator
updateOperatorInputOutputEstimates(WhichNode, Nodes, OutputSize, InputSize, PlanCostIn, PlanCostOut) :-
    member(antijoin(WhichNode, _, _), Nodes),
    (member(operatorInputEstimate(antijoin, InSize), PlanCostIn) ->
        select(operatorInputEstimate(antijoin, InSize), PlanCostIn, IntPlanCost1),
        select(operatorOutputEstimate(antijoin, OutSize), IntPlanCost1, IntPlanCost2),
        ISize is InSize + InputSize,
        OSize is OutSize + OutputSize,
        append([operatorInputEstimate(antijoin, ISize)], IntPlanCost2, IntPlanCost3),
        append([operatorOutputEstimate(antijoin, OSize)], IntPlanCost3, PlanCostOut)
        ;
        append([operatorInputEstimate(antijoin, InputSize)], PlanCostIn, IntPlanCost),
        append([operatorOutputEstimate(antijoin, OutputSize)], IntPlanCost, PlanCostOut)
    ).

%**** update estimates for semijoin operator
updateOperatorInputOutputEstimates(WhichNode, Nodes, OutputSize, InputSize, PlanCostIn, PlanCostOut) :-
    member(semijoin(WhichNode, _, _), Nodes),
    (member(operatorInputEstimate(semijoin, InSize), PlanCostIn) ->
        select(operatorInputEstimate(semijoin, InSize), PlanCostIn, IntPlanCost1),
        select(operatorOutputEstimate(semijoin, OutSize), IntPlanCost1, IntPlanCost2),
        ISize is InSize + InputSize,
        OSize is OutSize + OutputSize,
        append([operatorInputEstimate(semijoin, ISize)], IntPlanCost2, IntPlanCost3),
        append([operatorOutputEstimate(semijoin, OSize)], IntPlanCost3, PlanCostOut)
        ;
        append([operatorInputEstimate(semijoin, InputSize)], PlanCostIn, IntPlanCost),
        append([operatorOutputEstimate(semijoin, OutputSize)], IntPlanCost, PlanCostOut)
    ).

%**** update estimates for dedup operator
updateOperatorInputOutputEstimates(WhichNode, Nodes, OutputSize, InputSize, PlanCostIn, PlanCostOut) :-
    member(dedup(WhichNode, _), Nodes),
    (member(operatorInputEstimate(dedup, InSize), PlanCostIn) ->
        select(operatorInputEstimate(dedup, InSize), PlanCostIn, IntPlanCost1),
        select(operatorOutputEstimate(dedup, OutSize), IntPlanCost1, IntPlanCost2),
        ISize is InSize + InputSize,
        OSize is OutSize + OutputSize,
        append([operatorInputEstimate(dedup, ISize)], IntPlanCost2, IntPlanCost3),
        append([operatorOutputEstimate(dedup, OSize)], IntPlanCost3, PlanCostOut)
        ;
        append([operatorInputEstimate(dedup, InputSize)], PlanCostIn, IntPlanCost),
        append([operatorOutputEstimate(dedup, OutputSize)], IntPlanCost, PlanCostOut)
    ).

%**** update estimates for projection operator
updateOperatorInputOutputEstimates(WhichNode, Nodes, OutputSize, InputSize, PlanCostIn, PlanCostOut) :-
    member(projection(WhichNode, _), Nodes),
    (member(operatorInputEstimate(projection, InSize), PlanCostIn) ->
        select(operatorInputEstimate(projection, InSize), PlanCostIn, IntPlanCost1),
        select(operatorOutputEstimate(projection, OutSize), IntPlanCost1, IntPlanCost2),
        ISize is InSize + InputSize,
        OSize is OutSize + OutputSize,
        append([operatorInputEstimate(projection, ISize)], IntPlanCost2, IntPlanCost3),
        append([operatorOutputEstimate(projection, OSize)], IntPlanCost3, PlanCostOut)
        ;
        append([operatorInputEstimate(projection, InputSize)], PlanCostIn, IntPlanCost),
        append([operatorOutputEstimate(projection, OutputSize)], IntPlanCost, PlanCostOut)
    ).

%**** update estimates for generalized aggregate operator
updateOperatorInputOutputEstimates(WhichNode, Nodes, OutputSize, InputSize, PlanCostIn, PlanCostOut) :-
    member(genagg(WhichNode, _, _, _, _, _), Nodes),
    (member(operatorInputEstimate(genagg, InSize), PlanCostIn) ->
        select(operatorInputEstimate(genagg, InSize), PlanCostIn, IntPlanCost1),
        select(operatorOutputEstimate(genagg, OutSize), IntPlanCost1, IntPlanCost2),
        ISize is InSize + InputSize,
        OSize is OutSize + OutputSize,
        append([operatorInputEstimate(genagg, ISize)], IntPlanCost2, IntPlanCost3),
        append([operatorOutputEstimate(genagg, OSize)], IntPlanCost3, PlanCostOut)
        ;
        append([operatorInputEstimate(genagg, InputSize)], PlanCostIn, IntPlanCost),
        append([operatorOutputEstimate(genagg, OutputSize)], IntPlanCost, PlanCostOut)
    ).

%**** update estimates for vgwrapper operator
updateOperatorInputOutputEstimates(WhichNode, Nodes, OutputSize, InputSize, PlanCostIn, PlanCostOut) :-
    member(vgwrapper(WhichNode, _, _, _, _, _, _, _, _), Nodes),
    (member(operatorInputEstimate(vgwrapper, InSize), PlanCostIn) ->
        select(operatorInputEstimate(vgwrapper, InSize), PlanCostIn, IntPlanCost1),
        select(operatorOutputEstimate(vgwrapper, OutSize), IntPlanCost1, IntPlanCost2),
        ISize is InSize + InputSize,
        OSize is OutSize + OutputSize,
        append([operatorInputEstimate(vgwrapper, ISize)], IntPlanCost2, IntPlanCost3),
        append([operatorOutputEstimate(vgwrapper, OSize)], IntPlanCost3, PlanCostOut)
        ;
        append([operatorInputEstimate(vgwrapper, InputSize)], PlanCostIn, IntPlanCost),
        append([operatorOutputEstimate(vgwrapper, OutputSize)], IntPlanCost, PlanCostOut)
    ).

%**** update estimates for scalar function operator
updateOperatorInputOutputEstimates(WhichNode, Nodes, OutputSize, InputSize, PlanCostIn, PlanCostOut) :-
    member(scalarfunc(WhichNode, _, _, _), Nodes),
    (member(operatorInputEstimate(scalarfunc, InSize), PlanCostIn) ->
        select(operatorInputEstimate(scalarfunc, InSize), PlanCostIn, IntPlanCost1),
        select(operatorOutputEstimate(scalarfunc, OutSize), IntPlanCost1, IntPlanCost2),
        ISize is InSize + InputSize,
        OSize is OutSize + OutputSize,
        append([operatorInputEstimate(scalarfunc, ISize)], IntPlanCost2, IntPlanCost3),
        append([operatorOutputEstimate(scalarfunc, OSize)], IntPlanCost3, PlanCostOut)
        ;
        append([operatorInputEstimate(scalarfunc, InputSize)], PlanCostIn, IntPlanCost),
        append([operatorOutputEstimate(scalarfunc, OutputSize)], IntPlanCost, PlanCostOut)
    ).
    
%*****************************************************************************************************************************
% RULE THAT SUMS THE COST FOR EACH OPERATOR IN THE PLAN
%computePlanCost([stats(_, _, _, _, _, RelationSize)|OtherCosts], PlanCostIn, PlanCostOut) :-
%    IntPlanCost is PlanCostIn + RelationSize,
%    computePlanCost(OtherCosts, IntPlanCost, PlanCostOut).
%
%computePlanCost([], PlanCostIn, PlanCostOut) :- PlanCostOut = PlanCostIn.

%*****************************************************************************************************************************
% RULE THAT WEIGHT-SUMS THE COST FOR EACH OPERATOR IN THE PLAN
computePlanCost([stats(WhichNode, _, _, _, _, OutputSize)|OtherCosts], Costs, Graph, Nodes, PlanCostIn, PlanCostOut) :-
    (member(tablescan(WhichNode, _, _), Nodes) ->
        IntPlanCost is PlanCostIn
        ;
        % **** find operator's weight
        retrieveOperatorCostWeight(WhichNode, Nodes, OperatorCostWeight),
        
        % **** operator cost is weight sum of output + total input size
        NodeCost1 is  OutputSize * OperatorCostWeight,

        % **** find operator's children
        setof(ChildNode, member(parent(WhichNode, ChildNode), Graph), ChildNodes),
        % **** and add together their relation sizes
        sumOfChildreRelationSize(ChildNodes, Costs, 0, InputSize),
        NodeCost2 is InputSize * OperatorCostWeight,

        IntPlanCost is PlanCostIn + NodeCost1 + NodeCost2
    ),
    computePlanCost(OtherCosts, Costs, Graph, Nodes, IntPlanCost, PlanCostOut).

computePlanCost([], _, _, _, PlanCostIn, PlanCostOut) :-
    PlanCostOut = PlanCostIn.

%**** retrieve cost weight for selection operator
retrieveOperatorCostWeight(NodeName, Nodes, OperatorCostWeight) :-
    member(selection(NodeName, _), Nodes),
    operatorCostWeight(selection, OperatorCostWeight).

%**** retrieve cost weight for seed operator
retrieveOperatorCostWeight(NodeName, Nodes, OperatorCostWeight) :-
    member(seed(NodeName, _), Nodes),
    operatorCostWeight(seed, OperatorCostWeight).
    
%**** retrieve cost weight for split operator
retrieveOperatorCostWeight(NodeName, Nodes, OperatorCostWeight) :-
    member(split(NodeName, _), Nodes),
    operatorCostWeight(split, OperatorCostWeight).
    
%**** retrieve cost weight for join operator
retrieveOperatorCostWeight(NodeName, Nodes, OperatorCostWeight) :-
    member(join(NodeName, _, _), Nodes),
    operatorCostWeight(join, OperatorCostWeight).
    
%**** retrieve cost weight for antijoin operator
retrieveOperatorCostWeight(NodeName, Nodes, OperatorCostWeight) :-
    member(antijoin(NodeName, _, _), Nodes),
    operatorCostWeight(antijoin, OperatorCostWeight).
    
%**** retrieve cost weight for semijoin operator
retrieveOperatorCostWeight(NodeName, Nodes, OperatorCostWeight) :-
    member(semijoin(NodeName, _, _), Nodes),
    operatorCostWeight(semijoin, OperatorCostWeight).
    
%**** retrieve cost weight for dedup operator
retrieveOperatorCostWeight(NodeName, Nodes, OperatorCostWeight) :-
    member(dedup(NodeName, _), Nodes),
    operatorCostWeight(dedup, OperatorCostWeight).
    
%**** retrieve cost weight for projection operator
retrieveOperatorCostWeight(NodeName, Nodes, OperatorCostWeight) :-
    member(projection(NodeName, _), Nodes),
    operatorCostWeight(projection, OperatorCostWeight).

%**** retrieve cost weight for generalized aggregate operator
retrieveOperatorCostWeight(NodeName, Nodes, OperatorCostWeight) :-
    member(genagg(NodeName, _, _, _, _, _), Nodes),
    operatorCostWeight(genagg, OperatorCostWeight).

%**** retrieve cost weight for projection operator
retrieveOperatorCostWeight(NodeName, Nodes, OperatorCostWeight) :-
    member(vgwrapper(NodeName, _, _, _, _, _, _, _, _), Nodes),
    operatorCostWeight(vgwrapper, OperatorCostWeight).
    
%**** retrieve cost weight for scalar function operator
retrieveOperatorCostWeight(NodeName, Nodes, OperatorCostWeight) :-
    member(scalarfunc(NodeName, _, _, _), Nodes),
    operatorCostWeight(scalarfunc, OperatorCostWeight).

% **** rule that sums the relation sizes of a set of nodes
sumOfChildreRelationSize([Node|OtherNodes], Costs, TotalSizeIn, TotalSizeOut) :-
    member(stats(Node, _, _, _, _, NodeSize), Costs),
    TotalSizeInt is TotalSizeIn + NodeSize,
    sumOfChildreRelationSize(OtherNodes, Costs, TotalSizeInt, TotalSizeOut).
    
sumOfChildreRelationSize([], _, TotalSizeIn, TotalSizeOut) :-
    TotalSizeOut = TotalSizeIn.
     
%*****************************************************************************************************************************
% RULES FOR RECURSIVELY COMPUTING THE COST MEASURES OF THE PLAN ANCHORED AT A SPECIFIC NODE
% **** version that deals with a tablescan operator
computePlanCostMeasures(WhichNode, _, NodesIn, _, _, OutputAttrs, CostsIn, CostsOut) :-
    member(tablescan(WhichNode, _, _), NodesIn),

    (member(stats(WhichNode, _, _, _, _, _), CostsIn) ->
        CostsOut = CostsIn;

        stats(WhichNode, IntUniqueAttrValues, IntUniqueAttrValuesPerTupleBundle, IntAttrSizes, NumTuples, _),
        
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
        intersectUniqueAttrValues(IntUniqueAttrValues, NodeOutputAttrs, [], UniqueAttrValues),
        intersectUniqueAttrValuesPerTupleBundle(IntUniqueAttrValuesPerTupleBundle, NodeOutputAttrs, [], UniqueAttrValuesPerTupleBundle),
        intersectAttrSizes(IntAttrSizes, NodeOutputAttrs, [], AttrSizes),
        
        computeTupleSize(AttrSizes, [], 0, TupleSize),
        RelationSize is (TupleSize * NumTuples),
        append([stats(WhichNode, UniqueAttrValues, UniqueAttrValuesPerTupleBundle, AttrSizes, NumTuples, RelationSize)], CostsIn, CostsOut)
    ),!.
    
% **** version that deals with a generalized attribute operator (2)
computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    member(genagg(WhichNode, GenAggName, GroupByAttrs, AggExprs, AggExprsInAttrs, AggExprsOutAttrs), NodesIn),
    AggExprs \= nothing,

    (member(stats(WhichNode, _, _, _, _, _), CostsIn) ->
        CostsOut = CostsIn;

        % **** group by is performed only over constant attributes
        member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrs),
        intersectSets(NodeRandomAttrs, GroupByAttrs, RandomGroupByAttrs),
        RandomGroupByAttrs = [],

        member(parent(WhichNode, ChildNode), GraphIn),
        computePlanCostMeasures(ChildNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts),
        member(stats(ChildNode, ChildUniqueAttrValues, _, ChildAttrSizes, ChildNumTuples, _), IntCosts),

        (ChildNumTuples =< 0.01 ->
            append([stats(WhichNode, zero, zero, zero, 0, 0)], IntCosts, CostsOut)
            ;
            % **** compute number of output tuples as MAX of unique values among the group by attributes
            (GroupByAttrs = [] ->
                GenAggNumTuples = 1
                ;
                findall(GroupByAttrUniqueValueNum, (member(GroupByAttr, GroupByAttrs),
                               member(uniqueValues(GroupByAttr, GroupByAttrUniqueValueNum), ChildUniqueAttrValues)), GroupByUniqueAttrValueNums),
                maxElement(GroupByUniqueAttrValueNums, 0, GenAggNumTuples)
            ),

            % **** project only group by attributes
            findall(uniqueValues(GroupByAttr, GroupByAttrUniqueValueNum), (member(GroupByAttr, GroupByAttrs),
                               member(uniqueValues(GroupByAttr, GroupByAttrUniqueValueNum), ChildUniqueAttrValues)), GroupByUniqueAttrValues),
            findall(attributeSize(GroupByAttr, GroupByAttrSize), (member(GroupByAttr, GroupByAttrs), member(attributeSize(GroupByAttr, GroupByAttrSize), ChildAttrSizes)), GroupByAttrSizes),

            (current_predicate(aggregateproperties/2) -> aggregateproperties(GenAggName, AggExprsOutAttrsDomains); AggExprsOutAttrsDomains = infinite),
            integrateAggregatedAttrs(AggExprsInAttrs, AggExprsOutAttrs, AggExprsOutAttrsDomains, NodeRandomAttrs, GenAggNumTuples, ChildAttrSizes, GroupByUniqueAttrValues,
                             TmpGenAggUniqueAttrValues, [], TmpGenAggUniqueAttrValuesPerTupleBundle, GroupByAttrSizes, TmpGenAggAttrSizes),
            %GenAggAttrSizes = GroupByAttrSizes,
            %GenAggUniqueAttrValues = GroupByUniqueAttrValues,
            %GenAggUniqueAttrValuesPerTupleBundle = [],

            member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
            intersectUniqueAttrValues(TmpGenAggUniqueAttrValues, NodeOutputAttrs, [], GenAggUniqueAttrValues),
            intersectUniqueAttrValuesPerTupleBundle(TmpGenAggUniqueAttrValuesPerTupleBundle, NodeOutputAttrs, [], GenAggUniqueAttrValuesPerTupleBundle),
            intersectAttrSizes(TmpGenAggAttrSizes, NodeOutputAttrs, [], GenAggAttrSizesX),

            updateAggregateAttSizes(AggExprs, AggExprsOutAttrs, ChildNumTuples, GenAggNumTuples, GenAggAttrSizesX, GenAggAttrSizes),
            computeTupleSize(GenAggAttrSizes, NodeRandomAttrs, 0, TupleSize),
            GenAggRelationSize is (GenAggNumTuples * TupleSize),

            append([stats(WhichNode, GenAggUniqueAttrValues, GenAggUniqueAttrValuesPerTupleBundle, GenAggAttrSizes, GenAggNumTuples, GenAggRelationSize)], IntCosts, CostsOut)
        )
    ),!.

% **** version that deals with a scalar function
computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    member(scalarfunc(WhichNode, _, FuncExprsInAttrs, FuncExprsOutAttrs), NodesIn),

    (member(stats(WhichNode, _, _, _, _, _), CostsIn) ->
        CostsOut = CostsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        computePlanCostMeasures(ChildNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts),
        member(stats(ChildNode, ChildUniqueAttrValues, ChildUniqueAttrValuesPerTupleBundle, ChildAttrSizes, ChildNumTuples, _), IntCosts),

        (ChildNumTuples =< 0.01 ->
            append([stats(WhichNode, zero, zero, zero, 0, 0)], IntCosts, CostsOut)
            ;
            % **** collect input attribute cost measures
            FuncInAttrUniqueValues = ChildUniqueAttrValues,
            FuncInAttrSizes = ChildAttrSizes,
            FuncInAttrUniqueValuesPerTupleBundle = ChildUniqueAttrValuesPerTupleBundle,

            ScalarFuncNumTuples = ChildNumTuples,

            % **** add cost measures for outputs of scalar functions
            flatten(FuncExprsOutAttrs, FlattenedFuncExprsOutAttrs),
            monteCarloIterations(MCI),
            member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrs),

            findall(uniqueValues(FuncOutAttr, ScalarFuncNumTuples),
                 (member(FuncOutAttr, FlattenedFuncExprsOutAttrs), not(member(FuncOutAttr, NodeRandomAttrs))), IntFuncOutAttrUniqueValues1),
            ValueNum is MCI * ScalarFuncNumTuples,
            findall(uniqueValues(FuncOutAttr, ValueNum),
                (member(FuncOutAttr, FlattenedFuncExprsOutAttrs), member(FuncOutAttr, NodeRandomAttrs)), IntFuncOutAttrUniqueValues2),
            mergeSets(IntFuncOutAttrUniqueValues1, FuncInAttrUniqueValues, IntScalarFuncUniqueAttrValues),
            mergeSets(IntFuncOutAttrUniqueValues2, IntScalarFuncUniqueAttrValues, TmpScalarFuncUniqueAttrValues),

            findall(uniqueValuesPerTupleBundle(FuncOutAttr, MCI), (member(FuncOutAttr, FlattenedFuncExprsOutAttrs),
                                                            member(FuncOutAttr, NodeRandomAttrs)), FuncOutAttrUniqueValuesPerTupleBundle),
            mergeSets(FuncInAttrUniqueValuesPerTupleBundle, FuncOutAttrUniqueValuesPerTupleBundle, TmpScalarFuncUniqueAttrValuesPerTupleBundle),

            computeFuncOutAttrSizes(FuncExprsInAttrs, FuncExprsOutAttrs, FuncInAttrSizes, [], FuncOutAttrSizes),
            mergeSets(FuncOutAttrSizes, FuncInAttrSizes, TmpScalarFuncAttrSizes),
            % ****

            member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
            intersectUniqueAttrValues(TmpScalarFuncUniqueAttrValues, NodeOutputAttrs, [], ScalarFuncUniqueAttrValues),
            intersectUniqueAttrValuesPerTupleBundle(TmpScalarFuncUniqueAttrValuesPerTupleBundle, NodeOutputAttrs, [], ScalarFuncUniqueAttrValuesPerTupleBundle),
            intersectAttrSizes(TmpScalarFuncAttrSizes, NodeOutputAttrs, [], ScalarFuncAttrSizes),

            computeTupleSize(ScalarFuncAttrSizes, NodeRandomAttrs, 0, TupleSize),
            ScalarFuncRelationSize is (ScalarFuncNumTuples * TupleSize),

            mergeSets([stats(WhichNode, ScalarFuncUniqueAttrValues, ScalarFuncUniqueAttrValuesPerTupleBundle, ScalarFuncAttrSizes, ScalarFuncNumTuples, ScalarFuncRelationSize)], IntCosts, CostsOut)
        )
    ),!.
    
% **** version that deals with a projection operator
computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    member(projection(WhichNode, ProjectionAttrs), NodesIn),

    (member(stats(WhichNode, _, _, _, _, _), CostsIn) ->
        CostsOut = CostsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        computePlanCostMeasures(ChildNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts),

        member(stats(ChildNode, ChildUniqueAttrValues, ChildUniqueAttrValuesPerTupleBundle, ChildAttrSizes, ChildNumTuples, _), IntCosts),

        (ChildNumTuples =< 0.01 ->
            append([stats(WhichNode, zero, zero, zero, 0, 0)], IntCosts, CostsOut)
            ;
            findall(attributeSize(AttrName, AttrSize), (member(AttrName, ProjectionAttrs), member(attributeSize(AttrName, AttrSize), ChildAttrSizes)), IntProjectionAttrSizes),
            findall(uniqueValues(AttrName, UniqueAttrValues), (member(AttrName, ProjectionAttrs),
                                               member(uniqueValues(AttrName, UniqueAttrValues), ChildUniqueAttrValues)), IntProjectionUniqueAttrValues),
            findall(uniqueValuesPerTupleBundle(AttrName, UniqueAttrValuesPerTupleBundle), (member(AttrName, ProjectionAttrs),
                                               member(uniqueValuesPerTupleBundle(AttrName, UniqueAttrValuesPerTupleBundle),
                                               ChildUniqueAttrValuesPerTupleBundle)), IntProjectionUniqueAttrValuesPerTupleBundle),

            member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
            intersectUniqueAttrValues(IntProjectionUniqueAttrValues, NodeOutputAttrs, [], ProjectionUniqueAttrValues),
            intersectUniqueAttrValuesPerTupleBundle(IntProjectionUniqueAttrValuesPerTupleBundle, NodeOutputAttrs, [], ProjectionUniqueAttrValuesPerTupleBundle),
            intersectAttrSizes(IntProjectionAttrSizes, NodeOutputAttrs, [], ProjectionAttrSizes),
        
            member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrs),
            computeTupleSize(ProjectionAttrSizes, NodeRandomAttrs, 0, TupleSize),
            RelationSize is (TupleSize * ChildNumTuples),
            append([stats(WhichNode, ProjectionUniqueAttrValues, ProjectionUniqueAttrValuesPerTupleBundle, ProjectionAttrSizes, ChildNumTuples, RelationSize)], IntCosts, CostsOut)
        )
    ),!.
    
% **** version that deals with a join
computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    member(join(WhichNode, JoinPreds, _), NodesIn),

    (member(stats(WhichNode, _, _, _, _, _), CostsIn) ->
        CostsOut = CostsIn;

        member(parent(WhichNode, JoinRHSChildNode), GraphIn),
        member(parent(WhichNode, JoinLHSChildNode), GraphIn),
        JoinLHSChildNode @> JoinRHSChildNode,

        computePlanCostMeasures(JoinRHSChildNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts1),
        computePlanCostMeasures(JoinLHSChildNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, IntCosts1, IntCosts2),

        member(stats(JoinRHSChildNode, JoinRHSChildUniqueAttrValues, JoinRHSChildUniqueAttrValuesPerTupleBundle, JoinRHSChildAttrSizes, JoinRHSChildNumTuples, _), IntCosts2),
        member(stats(JoinLHSChildNode, JoinLHSChildUniqueAttrValues, JoinLHSChildUniqueAttrValuesPerTupleBundle, JoinLHSChildAttrSizes, JoinLHSChildNumTuples, _), IntCosts2),

        ((JoinRHSChildNumTuples =< 0.01; JoinLHSChildNumTuples =< 0.01) ->
            append([stats(WhichNode, zero, zero, zero, 0, 0)], IntCosts2, CostsOut)
            ;
            mergeSets(JoinRHSChildAttrSizes, JoinLHSChildAttrSizes, IntJoinAttrSizes),
            mergeSets(JoinRHSChildUniqueAttrValues, JoinLHSChildUniqueAttrValues, IntJoinUniqueAttrValues1),
            mergeSets(JoinRHSChildUniqueAttrValuesPerTupleBundle, JoinLHSChildUniqueAttrValuesPerTupleBundle, IntJoinUniqueAttrValuesPerTupleBundle1),

            member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrs),
            IntJoinNumTuples is (JoinRHSChildNumTuples * JoinLHSChildNumTuples),
            computeSelectivity(JoinPreds, NodeRandomAttrs, IntJoinUniqueAttrValues1, IntJoinUniqueAttrValues2, IntJoinUniqueAttrValuesPerTupleBundle1,
                                       IntJoinUniqueAttrValuesPerTupleBundle2, IntJoinNumTuples, JoinNumTuples),

            (member(isPres, NodeRandomAttrs) ->
                attributeSize(isPres, IsPresSize),
                mergeSets([attributeSize(isPres, IsPresSize)], IntJoinAttrSizes, TmpJoinAttrSizes),
                mergeSets([uniqueValuesPerTupleBundle(isPres, 2)], IntJoinUniqueAttrValuesPerTupleBundle2, TmpJoinUniqueAttrValuesPerTupleBundle),
                mergeSets([uniqueValues(isPres, 2)], IntJoinUniqueAttrValues2, TmpJoinUniqueAttrValues)
                ;
                TmpJoinAttrSizes = IntJoinAttrSizes,
                TmpJoinUniqueAttrValuesPerTupleBundle = IntJoinUniqueAttrValuesPerTupleBundle2,
                TmpJoinUniqueAttrValues = IntJoinUniqueAttrValues2
            ),

            member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
            intersectUniqueAttrValues(TmpJoinUniqueAttrValues, NodeOutputAttrs, [], JoinUniqueAttrValues),
            intersectUniqueAttrValuesPerTupleBundle(TmpJoinUniqueAttrValuesPerTupleBundle, NodeOutputAttrs, [], JoinUniqueAttrValuesPerTupleBundle),
            intersectAttrSizes(TmpJoinAttrSizes, NodeOutputAttrs, [], JoinAttrSizes),
            
            computeTupleSize(JoinAttrSizes, NodeRandomAttrs, 0, TupleSize),
            RelationSize is (JoinNumTuples * TupleSize),

            append([stats(WhichNode, JoinUniqueAttrValues, JoinUniqueAttrValuesPerTupleBundle, JoinAttrSizes, JoinNumTuples, RelationSize)], IntCosts2, CostsOut)
        )
    ),!.
    
% **** version that deals with a selection operator
computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    member(selection(WhichNode, SelectionPreds), NodesIn),

    (member(stats(WhichNode, _, _, _, _, _), CostsIn) ->
        CostsOut = CostsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        computePlanCostMeasures(ChildNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts),

        member(stats(ChildNode, ChildUniqueAttrValues, ChildUniqueAttrValuesPerTupleBundle, ChildAttrSizes, ChildNumTuples, _), IntCosts),

        (ChildNumTuples =< 0.01 ->
            append([stats(WhichNode, zero, zero, zero, 0, 0)], IntCosts, CostsOut)
            ;
            % **** updates costs measures based on selectivity predicates and add 'isPres' attribute if needed
            member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrs),
            computeSelectivity(SelectionPreds, NodeRandomAttrs, ChildUniqueAttrValues, IntSelectionUniqueAttrValues1, ChildUniqueAttrValuesPerTupleBundle,
                                       IntSelectionUniqueAttrValuesPerTupleBundle1, ChildNumTuples, SelectionNumTuples),

           (member(isPres, NodeRandomAttrs) ->
                attributeSize(isPres, IsPresSize),
                mergeSets([attributeSize(isPres, IsPresSize)], ChildAttrSizes, IntSelectionAttrSizes2),
                mergeSets([uniqueValuesPerTupleBundle(isPres, 2)], IntSelectionUniqueAttrValuesPerTupleBundle1, IntSelectionUniqueAttrValuesPerTupleBundle2),
                mergeSets([uniqueValues(isPres, 2)], IntSelectionUniqueAttrValues1, IntSelectionUniqueAttrValues2)
                ;
                IntSelectionAttrSizes2 = ChildAttrSizes,
                IntSelectionUniqueAttrValuesPerTupleBundle2 = IntSelectionUniqueAttrValuesPerTupleBundle1,
                IntSelectionUniqueAttrValues2 = IntSelectionUniqueAttrValues1
            ),

            member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
            intersectUniqueAttrValues(IntSelectionUniqueAttrValues2, NodeOutputAttrs, [], SelectionUniqueAttrValues),
            intersectUniqueAttrValuesPerTupleBundle(IntSelectionUniqueAttrValuesPerTupleBundle2, NodeOutputAttrs, [], SelectionUniqueAttrValuesPerTupleBundle),
            intersectAttrSizes(IntSelectionAttrSizes2, NodeOutputAttrs, [], SelectionAttrSizes),
            
            computeTupleSize(SelectionAttrSizes, NodeRandomAttrs, 0, TupleSize),
            SelectionRelationSize is (SelectionNumTuples * TupleSize),

            append([stats(WhichNode, SelectionUniqueAttrValues, SelectionUniqueAttrValuesPerTupleBundle, SelectionAttrSizes, SelectionNumTuples, SelectionRelationSize)], IntCosts, CostsOut)
        )
    ),!.
    
% **** version that deals with a seed operator
computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    member(seed(WhichNode, SeedAttr), NodesIn),

    (member(stats(WhichNode, _, _, _, _, _), CostsIn) ->
        CostsOut = CostsIn;

        member(parent(WhichNode, ChildNode), GraphIn),
        computePlanCostMeasures(ChildNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts),

        member(stats(ChildNode, ChildUniqueAttrValues, ChildUniqueAttrValuesPerTupleBundle, ChildAttrSizes, ChildNumTuples, ChildRelationSize), IntCosts),

        (ChildNumTuples =< 0.01 ->
            append([stats(WhichNode, zero, zero, zero, 0, 0)], IntCosts, CostsOut)
            ;
            mergeSets([uniqueValues(SeedAttr, ChildNumTuples)], ChildUniqueAttrValues, IntSeedUniqueAttrValues),
            attributeSize(seed, SeedAttrSize),
            mergeSets([attributeSize(SeedAttr, SeedAttrSize)], ChildAttrSizes, IntSeedAttrSizes),
            
            member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
            intersectUniqueAttrValues(IntSeedUniqueAttrValues, NodeOutputAttrs, [], SeedUniqueAttrValues),
            intersectUniqueAttrValuesPerTupleBundle(ChildUniqueAttrValuesPerTupleBundle, NodeOutputAttrs, [], SeedUniqueAttrValuesPerTupleBundle),
            intersectAttrSizes(IntSeedAttrSizes, NodeOutputAttrs, [], SeedAttrSizes),

            SeedRelationSize is ChildRelationSize + (ChildNumTuples * SeedAttrSize),
            append([stats(WhichNode, SeedUniqueAttrValues, SeedUniqueAttrValuesPerTupleBundle, SeedAttrSizes, ChildNumTuples, SeedRelationSize)], IntCosts, CostsOut)
        )
    ),!.
    
% **** version that deals with a vgwrapper operator
computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    member(vgwrapper(WhichNode, VGName, _, _, _, _, _, _, SeedAttrOut), NodesIn),

    (member(stats(WhichNode, _, _, _, _, _), CostsIn) ->
        CostsOut = CostsIn;

        member(outerRelation(WhichNode, OuterRelationNode), OuterRelations),
        setof(InnerRelationNode, member(parent(WhichNode, InnerRelationNode), GraphIn), VGWrapperChildNodes),
        select(OuterRelationNode, VGWrapperChildNodes, InnerRelationNodes),
        computePlanCostMeasures(OuterRelationNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts1),
        iterateOverVGWrapperInnerRelationsToComputePlanCostMeasures(InnerRelationNodes, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, IntCosts1, IntCosts2),

        vgwrapperproperties(VGName, bundlesPerTuple(BPT), VGWrapperAttrProperties, IntVGWrapperAttrSizes1),
        % **** add seed attribute to the attribute sizes
        attributeSize(seed, SeedAttrSize),
        append([attributeSize(SeedAttrOut, SeedAttrSize)], IntVGWrapperAttrSizes1, IntVGWrapperAttrSizes2),

        member(stats(OuterRelationNode, _, _, _, OuterRelationNumTuples, _), IntCosts2),
        NumTuples is (BPT * OuterRelationNumTuples),

        computeVGWrapperMeasures(VGWrapperAttrProperties, [], IntVGWrapperUniqueAttrValues1, [], IntVGWrapperUniqueAttrValuesPerTupleBundle, NumTuples, VGWrapperChildNodes, IntCosts2),

        mergeSets([uniqueValues(SeedAttrOut, OuterRelationNumTuples)], IntVGWrapperUniqueAttrValues1, IntVGWrapperUniqueAttrValues2),
        
        member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
        intersectUniqueAttrValues(IntVGWrapperUniqueAttrValues2, NodeOutputAttrs, [], VGWrapperUniqueAttrValues),
        intersectUniqueAttrValuesPerTupleBundle(IntVGWrapperUniqueAttrValuesPerTupleBundle, NodeOutputAttrs, [], VGWrapperUniqueAttrValuesPerTupleBundle),
        intersectAttrSizes(IntVGWrapperAttrSizes2, NodeOutputAttrs, [], VGWrapperAttrSizes),

        member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrs),
        computeTupleSize(VGWrapperAttrSizes, NodeRandomAttrs, 0, TupleSize),
        RelationSize is (NumTuples * TupleSize),
        
        append([stats(WhichNode, VGWrapperUniqueAttrValues, VGWrapperUniqueAttrValuesPerTupleBundle, VGWrapperAttrSizes, NumTuples, RelationSize)], IntCosts2, CostsOut)
    ),!.
    
% **** version that deals with a frame output operator
computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    member(frameoutput(WhichNode, FrameOutputChildren, _), NodesIn),
    iterateOverFrameOutputChildrenToComputePlanCostMeasures(FrameOutputChildren, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut).
    
% **** version that deals with a dedup operator
computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    member(dedup(WhichNode, DedupAttrs), NodesIn),

    (member(stats(WhichNode, _, _, _, _, _), CostsIn) ->
        CostsOut = CostsIn;

        % **** dedup is performed only over constant attributes
        member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrs),
        intersectSets(NodeRandomAttrs, DedupAttrs, RandomDedupAttrs),
        RandomDedupAttrs = [],

        member(parent(WhichNode, ChildNode), GraphIn),
        computePlanCostMeasures(ChildNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts),

        member(stats(ChildNode, ChildUniqueAttrValues, ChildUniqueAttrValuesPerTupleBundle, ChildAttrSizes, ChildNumTuples, _), IntCosts),

        (ChildNumTuples =< 0.01 ->
            append([stats(WhichNode, zero, zero, zero, 0, 0)], IntCosts, CostsOut)
            ;
            findall(DedupAttrUniqueValueNum, (member(DedupAttr, DedupAttrs), member(uniqueValues(DedupAttr, DedupAttrUniqueValueNum), ChildUniqueAttrValues)), DedupAttrsUniqueValueNum),
            maxElement(DedupAttrsUniqueValueNum, 0, DedupNumTuples),

            %updateRelationCostMeasures(ChildUniqueAttrValues, [], DedupUniqueAttrValues, DedupNumTuples),
            IntDedupUniqueAttrValues = ChildUniqueAttrValues,

            member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
            intersectUniqueAttrValues(IntDedupUniqueAttrValues, NodeOutputAttrs, [], DedupUniqueAttrValues),
            intersectUniqueAttrValuesPerTupleBundle(ChildUniqueAttrValuesPerTupleBundle, NodeOutputAttrs, [], DedupUniqueAttrValuesPerTupleBundle),
            intersectAttrSizes(ChildAttrSizes, NodeOutputAttrs, [], DedupAttrSizes),

            computeTupleSize(DedupAttrSizes, NodeRandomAttrs, 0, TupleSize),
            DedupRelationSize is (DedupNumTuples * TupleSize),

            append([stats(WhichNode, DedupUniqueAttrValues, DedupUniqueAttrValuesPerTupleBundle, DedupAttrSizes, DedupNumTuples, DedupRelationSize)], IntCosts, CostsOut)
        )
    ),!.

% **** version that deals with a split operator
computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    member(split(WhichNode, SplitAttrs), NodesIn),

    (member(stats(WhichNode, _, _, _, _, _), CostsIn) ->
        CostsOut = CostsIn;

        % ***** set of attributes to be split has to be subset of the set of random attributes of the relation input
        member(parent(WhichNode, ChildNode), GraphIn),
        member(randomAttributes(ChildNode, ChildRandomAttrs), RandomAttrs),
        subset(SplitAttrs, ChildRandomAttrs),

        computePlanCostMeasures(ChildNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts),

        member(stats(ChildNode, ChildUniqueAttrValues, ChildUniqueAttrValuesPerTupleBundle, ChildAttrSizes, ChildNumTuples, _), IntCosts),

        (ChildNumTuples =< 0.01 ->
            append([stats(WhichNode, zero, zero, zero, 0, 0)], IntCosts, CostsOut)
            ;
            computeSplitMeasures(SplitAttrs, ChildUniqueAttrValuesPerTupleBundle, IntSplitUniqueAttrValuesPerTupleBundle1, MaxUniqueAttrValuePerTupleBundleNum),
            (member(attributeSize(isPres, _), ChildAttrSizes) ->  % **** isPres is already present in the relation
                IntSplitUniqueAttrValues1 = ChildUniqueAttrValues,
                IntSplitAttrSizes = ChildAttrSizes,
                IntSplitUniqueAttrValuesPerTupleBundle2 = IntSplitUniqueAttrValuesPerTupleBundle1
                ;
                attributeSize(isPres, IsPresSize),
                mergeSets([attributeSize(isPres, IsPresSize)], ChildAttrSizes, IntSplitAttrSizes),
                mergeSets([uniqueValues(isPres, 2)], ChildUniqueAttrValues, IntSplitUniqueAttrValues1),
                mergeSets([uniqueValuesPerTupleBundle(isPres, 2)], IntSplitUniqueAttrValuesPerTupleBundle1, IntSplitUniqueAttrValuesPerTupleBundle2)
            ),

            member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
            intersectUniqueAttrValues(IntSplitUniqueAttrValues1, NodeOutputAttrs, [], IntSplitUniqueAttrValues2),
            intersectUniqueAttrValuesPerTupleBundle(IntSplitUniqueAttrValuesPerTupleBundle2, NodeOutputAttrs, [], SplitUniqueAttrValuesPerTupleBundle),
            intersectAttrSizes(IntSplitAttrSizes, NodeOutputAttrs, [], SplitAttrSizes),

            member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrs),
            computeTupleSize(SplitAttrSizes, NodeRandomAttrs, 0, TupleSize),

	    %%% changed by Luis because this was messing up the join order in the GMM.
            %%% SplitNumTuples is (MaxUniqueAttrValuePerTupleBundleNum * ChildNumTuples),
	    SplitNumTuples is (MaxUniqueAttrValuePerTupleBundleNum * 0) + ChildNumTuples,
            %updateRelationCostMeasures(IntSplitUniqueAttrValues, [], SplitUniqueAttrValues, SplitNumTuples),
            SplitUniqueAttrValues = IntSplitUniqueAttrValues2,
            SplitRelationSize is (SplitNumTuples * TupleSize),

            append([stats(WhichNode, SplitUniqueAttrValues, SplitUniqueAttrValuesPerTupleBundle, SplitAttrSizes, SplitNumTuples, SplitRelationSize)], IntCosts, CostsOut)
        )
    ),!.

% **** version that deals with an antijoin
computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    member(antijoin(WhichNode, DataSourceChildID, AntiJoinPreds), NodesIn),

    (member(stats(WhichNode, _, _, _, _, _), CostsIn) ->
        CostsOut = CostsIn;

        select(parent(WhichNode, DataSourceChildID), GraphIn, IntGraph),
        member(parent(WhichNode, OtherChildID), IntGraph),

        computePlanCostMeasures(DataSourceChildID, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts1),
        computePlanCostMeasures(OtherChildID, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, IntCosts1, IntCosts2),

        member(stats(DataSourceChildID, DataSourceChildUniqueAttrValues, DataSourceChildUniqueAttrValuesPerTupleBundle, DataSourceChildAttrSizes, DataSourceChildNumTuples, _), IntCosts2),
        member(stats(OtherChildID, OtherChildUniqueAttrValues, _, _, OtherChildNumTuples, _), IntCosts2),

        ((DataSourceChildNumTuples =< 0.01 ; OtherChildNumTuples =< 0.01) ->
            append([stats(WhichNode, zero, zero, zero, 0, 0)], IntCosts2, CostsOut)
            ;
            member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrs),
            member(randomAttributes(OtherChildID, OtherChildRandomAttrs), RandomAttrs),
            mergeSets(NodeRandomAttrs, OtherChildRandomAttrs, IntRandomAttrs),
            computeAntiJoinSelectivity(AntiJoinPreds, IntRandomAttrs, DataSourceChildUniqueAttrValues, IntAntiJoinUniqueAttrValues, OtherChildUniqueAttrValues,
                                       _, DataSourceChildUniqueAttrValuesPerTupleBundle, IntAntiJoinUniqueAttrValuesPerTupleBundle, DataSourceChildNumTuples, AntiJoinNumTuples),

            (member(isPres, NodeRandomAttrs) ->
                attributeSize(isPres, IsPresSize),
                mergeSets([attributeSize(isPres, IsPresSize)], DataSourceChildAttrSizes, TmpAntiJoinAttrSizes),
                (AntiJoinNumTuples > 2 -> No is 2; No is AntiJoinNumTuples),
                mergeSets([uniqueValuesPerTupleBundle(isPres, No)], IntAntiJoinUniqueAttrValuesPerTupleBundle, TmpAntiJoinUniqueAttrValuesPerTupleBundle),
                mergeSets([uniqueValues(isPres, No)], IntAntiJoinUniqueAttrValues, TmpAntiJoinUniqueAttrValues)
                ;
                TmpAntiJoinAttrSizes = DataSourceChildAttrSizes,
                TmpAntiJoinUniqueAttrValuesPerTupleBundle = IntAntiJoinUniqueAttrValuesPerTupleBundle,
                TmpAntiJoinUniqueAttrValues = IntAntiJoinUniqueAttrValues
            ),
            
            member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
            intersectUniqueAttrValues(TmpAntiJoinUniqueAttrValues, NodeOutputAttrs, [], AntiJoinUniqueAttrValues),
            intersectUniqueAttrValuesPerTupleBundle(TmpAntiJoinUniqueAttrValuesPerTupleBundle, NodeOutputAttrs, [], AntiJoinUniqueAttrValuesPerTupleBundle),
            intersectAttrSizes(TmpAntiJoinAttrSizes, NodeOutputAttrs, [], AntiJoinAttrSizes),

            computeTupleSize(AntiJoinAttrSizes, NodeRandomAttrs, 0, TupleSize),
            RelationSize is (AntiJoinNumTuples * TupleSize),

            append([stats(WhichNode, AntiJoinUniqueAttrValues, AntiJoinUniqueAttrValuesPerTupleBundle, AntiJoinAttrSizes, AntiJoinNumTuples, RelationSize)], IntCosts2, CostsOut)
        )
    ),!.
    
% **** version that deals with a semijoin
computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    member(semijoin(WhichNode, DataSourceChildID, SemiJoinPreds), NodesIn),

    (member(stats(WhichNode, _, _, _, _, _), CostsIn) ->
        CostsOut = CostsIn;

        select(parent(WhichNode, DataSourceChildID), GraphIn, IntGraph),
        member(parent(WhichNode, OtherChildID), IntGraph),

        computePlanCostMeasures(DataSourceChildID, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts1),
        computePlanCostMeasures(OtherChildID, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, IntCosts1, IntCosts2),

        member(stats(DataSourceChildID, DataSourceChildUniqueAttrValues, DataSourceChildUniqueAttrValuesPerTupleBundle, DataSourceChildAttrSizes, DataSourceChildNumTuples, _), IntCosts2),
        member(stats(OtherChildID, OtherChildUniqueAttrValues, _, _, OtherChildNumTuples, _), IntCosts2),

        ((DataSourceChildNumTuples =< 0.01 ; OtherChildNumTuples =< 0.01) ->
            append([stats(WhichNode, zero, zero, zero, 0, 0)], IntCosts2, CostsOut)
            ;
            member(randomAttributes(WhichNode, NodeRandomAttrs), RandomAttrs),
            member(randomAttributes(OtherChildID, OtherChildRandomAttrs), RandomAttrs),
            mergeSets(NodeRandomAttrs, OtherChildRandomAttrs, IntRandomAttrs),
            computeSemiJoinSelectivity(SemiJoinPreds, IntRandomAttrs, DataSourceChildUniqueAttrValues, IntSemiJoinUniqueAttrValues, OtherChildUniqueAttrValues,
                                       _, DataSourceChildUniqueAttrValuesPerTupleBundle, IntSemiJoinUniqueAttrValuesPerTupleBundle, DataSourceChildNumTuples, SemiJoinNumTuples),

            (member(isPres, NodeRandomAttrs) ->
                attributeSize(isPres, IsPresSize),
                mergeSets([attributeSize(isPres, IsPresSize)], DataSourceChildAttrSizes, TmpSemiJoinAttrSizes),
                (SemiJoinNumTuples > 2 -> No is 2; No is SemiJoinNumTuples),
                mergeSets([uniqueValuesPerTupleBundle(isPres, No)], IntSemiJoinUniqueAttrValuesPerTupleBundle, TmpSemiJoinUniqueAttrValuesPerTupleBundle),
                mergeSets([uniqueValues(isPres, No)], IntSemiJoinUniqueAttrValues, TmpSemiJoinUniqueAttrValues)
                ;
                TmpSemiJoinAttrSizes = DataSourceChildAttrSizes,
                TmpSemiJoinUniqueAttrValuesPerTupleBundle = IntSemiJoinUniqueAttrValuesPerTupleBundle,
                TmpSemiJoinUniqueAttrValues = IntSemiJoinUniqueAttrValues
            ),

            member(outputAttributes(WhichNode, NodeOutputAttrs), OutputAttrs),
            intersectUniqueAttrValues(TmpSemiJoinUniqueAttrValues, NodeOutputAttrs, [], SemiJoinUniqueAttrValues),
            intersectUniqueAttrValuesPerTupleBundle(TmpSemiJoinUniqueAttrValuesPerTupleBundle, NodeOutputAttrs, [], SemiJoinUniqueAttrValuesPerTupleBundle),
            intersectAttrSizes(TmpSemiJoinAttrSizes, NodeOutputAttrs, [], SemiJoinAttrSizes),

            computeTupleSize(SemiJoinAttrSizes, NodeRandomAttrs, 0, TupleSize),
            RelationSize is (SemiJoinNumTuples * TupleSize),

            append([stats(WhichNode, SemiJoinUniqueAttrValues, SemiJoinUniqueAttrValuesPerTupleBundle, SemiJoinAttrSizes, SemiJoinNumTuples, RelationSize)], IntCosts2, CostsOut)
        )
    ),!.
    
%*****************************************************************************************************************************
% RULEs THAT ITERATE OVER A VGWRAPPER's INNER RELATIONS TO COMPUTE THE COST MEASURES OF THE SUB-PLANS
iterateOverVGWrapperInnerRelationsToComputePlanCostMeasures([WhichNode|OtherNodes], GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts),
    iterateOverVGWrapperInnerRelationsToComputePlanCostMeasures(OtherNodes, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, IntCosts, CostsOut).

iterateOverVGWrapperInnerRelationsToComputePlanCostMeasures([], _, _, _, _, _, CostsIn, CostsOut) :-
    CostsOut = CostsIn.
    
%*****************************************************************************************************************************
% RULEs THAT ITERATE OVER A FRAME OUTPUT'S CHILDREN TO COMPUTE THE COST MEASURES OF THE SUB-PLANS
iterateOverFrameOutputChildrenToComputePlanCostMeasures([WhichNode|OtherNodes], GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, CostsOut) :-
    computePlanCostMeasures(WhichNode, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, CostsIn, IntCosts),
    iterateOverFrameOutputChildrenToComputePlanCostMeasures(OtherNodes, GraphIn, NodesIn, OuterRelations, RandomAttrs, OutputAttrs, IntCosts, CostsOut).

iterateOverFrameOutputChildrenToComputePlanCostMeasures([], _, _, _, _, _, CostsIn, CostsOut) :-
    CostsOut = CostsIn.
    
%*****************************************************************************************************************************
% RULEs THAT COMPUTE THE DINSTINCT RANDOM VALUES PER TUPLE BUNDLE AND TOTAL DISTINCT RANDOM VALUES FOR A VGWRAPPER OPERATOR
% version that deals with an infinite number of attribute values
computeVGWrapperMeasures([domain(AttrName, nothing, infinite)| OtherHints], UniqueAttrValuesIn, UniqueAttrValuesOut,
                                              UniqueAttrValuesPerTupleBundleIn, UniqueAttrValuesPerTupleBundleOut, OutputTuplesNum, VGWrapperChildNodes, CostsIn) :-
    % **** distinct values per tuple bundle equals the monte carlo iterations
    monteCarloIterations(MCI),
    mergeSets([uniqueValuesPerTupleBundle(AttrName, MCI)], UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle),
    % **** total number of distinct values is equal the output tuples
    AttrUniqueValues is (MCI * OutputTuplesNum),
    mergeSets([uniqueValues(AttrName, AttrUniqueValues)], UniqueAttrValuesIn, IntUniqueAttrValues),
    computeVGWrapperMeasures(OtherHints, IntUniqueAttrValues, UniqueAttrValuesOut, IntUniqueAttrValuesPerTupleBundle,
                                                       UniqueAttrValuesPerTupleBundleOut, OutputTuplesNum, VGWrapperChildNodes, CostsIn).
    
% version that deals with a constant number of attribute values
computeVGWrapperMeasures([domain(AttrName, nothing, constant(ValNum))| OtherHints], UniqueAttrValuesIn, UniqueAttrValuesOut,
                                              UniqueAttrValuesPerTupleBundleIn, UniqueAttrValuesPerTupleBundleOut, OutputTuplesNum, VGWrapperChildNodes, CostsIn) :-
    monteCarloIterations(MCI),
    (MCI > ValNum ->   % **** distinct values per tuple bundle equals ValNum (if < MCI)
        mergeSets([uniqueValuesPerTupleBundle(AttrName, ValNum)], UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle)
        ;
        mergeSets([uniqueValuesPerTupleBundle(AttrName, MCI)], UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle)
    ),

    (OutputTuplesNum > ValNum ->   % **** total number of distinct values is equal to ValNum
        mergeSets([uniqueValues(AttrName, ValNum)], UniqueAttrValuesIn, IntUniqueAttrValues)
        ;
        mergeSets([uniqueValues(AttrName, OutputTuplesNum)], UniqueAttrValuesIn, IntUniqueAttrValues)
    ),
    computeVGWrapperMeasures(OtherHints, IntUniqueAttrValues, UniqueAttrValuesOut, IntUniqueAttrValuesPerTupleBundle,
                                              UniqueAttrValuesPerTupleBundleOut, OutputTuplesNum, VGWrapperChildNodes, CostsIn).
    
computeVGWrapperMeasures([domain(AttrName, FuncID, InputAttrName)| OtherHints], UniqueAttrValuesIn, UniqueAttrValuesOut,
                                              UniqueAttrValuesPerTupleBundleIn, UniqueAttrValuesPerTupleBundleOut, OutputTuplesNum, VGWrapperChildNodes, CostsIn) :-
    FuncID \= nothing,
    
    findInputAttributeUniqueValues(InputAttrName, VGWrapperChildNodes, CostsIn, InputAttrNameUniqueValueCount),
    
    function(FuncID, InputAttrNameUniqueValueCount, ValNum),

    monteCarloIterations(MCI),
    (MCI > ValNum ->   % **** distinct values per tuple bundle equals ValNum (if < MCI)
        mergeSets([uniqueValuesPerTupleBundle(AttrName, ValNum)], UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle)
        ;
        mergeSets([uniqueValuesPerTupleBundle(AttrName, MCI)], UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle)
    ),

    (OutputTuplesNum > ValNum ->   % **** total number of distinct values is equal to ValNum
        mergeSets([uniqueValues(AttrName, ValNum)], UniqueAttrValuesIn, IntUniqueAttrValues)
        ;
        mergeSets([uniqueValues(AttrName, OutputTuplesNum)], UniqueAttrValuesIn, IntUniqueAttrValues)
    ),
    
    computeVGWrapperMeasures(OtherHints, IntUniqueAttrValues, UniqueAttrValuesOut, IntUniqueAttrValuesPerTupleBundle,
                                                  UniqueAttrValuesPerTupleBundleOut, OutputTuplesNum, VGWrapperChildNodes, CostsIn).
    
computeVGWrapperMeasures([], AttrValueCountsIn, AttrValueCountsOut, AttrValuePerTupleCountsIn, AttrValuePerTupleCountsOut, _, _, _) :-
    AttrValueCountsOut = AttrValueCountsIn,
    AttrValuePerTupleCountsOut = AttrValuePerTupleCountsIn.

%*****************************************************************************************************************************
% RULE THAT FINDS THE COST MEASURES FOR A GIVEN ATTRIBUTE
findInputAttributeUniqueValues(InputAttrName, [InputNode|OtherNodes], CostsIn, InputAttrNameUniqueValueNum) :-
    member(stats(InputNode, NodeUniqueAttrValues, _, _, _, _), CostsIn),
    (member(uniqueValues(InputAttrName, InputAttrUniqueValues), NodeUniqueAttrValues) ->
        InputAttrNameUniqueValueNum = InputAttrUniqueValues
        ;
        findInputAttributeUniqueValues(InputAttrName, OtherNodes, CostsIn, InputAttrNameUniqueValueNum)
    ).

%*****************************************************************************************************************************
% RULEs THAT COMPUTE THE DINSTINCT RANDOM VALUES PER TUPLE BUNDLE AND TOTAL DISTINCT RANDOM VALUES FOR A SPLIT OPERATOR
computeSplitMeasures(SplitAttrs, UniqueAttrValuesPerTupleBundleIn, UniqueAttrValuesPerTupleBundleOut, MaxUniqueAttrValuePerTupleBundleNum) :-
    % find aggregate unique values per tuple bundle for the set of the split attributes
    findall(ValNum, (member(AttrName, SplitAttrs), member(uniqueValuesPerTupleBundle(AttrName, ValNum), UniqueAttrValuesPerTupleBundleIn)), UniqueAttrValuesPerTupleBundle),
    maxElement(UniqueAttrValuesPerTupleBundle, 0, MaxUniqueAttrValuePerTupleBundleNum),

    findall(uniqueValuesPerTupleBundle(AttrName, ValNum), (member(AttrName, SplitAttrs),
                      member(uniqueValuesPerTupleBundle(AttrName, ValNum), UniqueAttrValuesPerTupleBundleIn)), IntUniqueAttrValuesPerTupleBundle),
    subtractSet(UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle, UniqueAttrValuesPerTupleBundleOut).
    
%*****************************************************************************************************************************
% RULES THAT COMPUTE THE SELECTION AND JOIN COST MEASURES BASED ON PREDICATE SELECTIVITIES
% ***** version that deals with a conjuction of predicates (represented as a comma separated list of predicates)
computeSelectivity([PredID|OtherPreds], RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn,
                                       UniqueAttrValuesPerTupleBundleOut, NumTuplesIn, NumTuplesOut) :-
    computeSinglePredicateSelectivity(PredID, RandomAttrs, UniqueAttrValuesIn, IntUniqueAttrValues, UniqueAttrValuesPerTupleBundleIn,
                                       IntUniqueAttrValuesPerTupleBundle, NumTuplesIn, IntNumTuples),
    computeSelectivity(OtherPreds, RandomAttrs, IntUniqueAttrValues, UniqueAttrValuesOut, IntUniqueAttrValuesPerTupleBundle,
                                       UniqueAttrValuesPerTupleBundleOut, IntNumTuples, NumTuplesOut).

computeSelectivity([], _, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn,
                                       UniqueAttrValuesPerTupleBundleOut, NumTuplesIn, NumTuplesOut) :-
    UniqueAttrValuesOut = UniqueAttrValuesIn,
    UniqueAttrValuesPerTupleBundleOut = UniqueAttrValuesPerTupleBundleIn,
    NumTuplesOut = NumTuplesIn.

% **** version that deals with an equality predicate with a literal value and a constant attribute        HERE
computeSinglePredicateSelectivity(PredID, RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn,
                                       UniqueAttrValuesPerTupleBundleOut, NumTuplesIn, NumTuplesOut) :-
    %current_predicate(equals/3),
    %equals(PredID, LHSAttr, literal),
    current_predicate(compExp/5),
    compExp(PredID, equals, LHSAttr, _, RHSAttrType),
    RHSAttrType = literal,
    not(member(LHSAttr, RandomAttrs)),  % **** constant attribute
    select(uniqueValues(LHSAttr, AttrUniqueValues), UniqueAttrValuesIn, IntUniqueAttrValues1),
    mergeSets([uniqueValues(LHSAttr, 1)], IntUniqueAttrValues1, IntUniqueAttrValues2),
    UniqueAttrValuesPerTupleBundleOut = UniqueAttrValuesPerTupleBundleIn,
    NumTuplesOut is ceiling(NumTuplesIn / AttrUniqueValues),
    
    %updateRelationCostMeasures(IntUniqueAttrValues2, [], UniqueAttrValuesOut, NumTuplesOut).
    UniqueAttrValuesOut = IntUniqueAttrValues2.
    
% **** version that deals with an equality predicate with a literal value and a random attribute
computeSinglePredicateSelectivity(PredID, RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn,
                                       UniqueAttrValuesPerTupleBundleOut, NumTuplesIn, NumTuplesOut) :-
    %current_predicate(equals/3),
    %equals(PredID, LHSAttr, literal),
    current_predicate(compExp/5),
    compExp(PredID, equals, LHSAttr, _, RHSAttrType),
    RHSAttrType = literal,
    
    member(LHSAttr, RandomAttrs), % **** random attribute

    select(uniqueValues(LHSAttr, _), UniqueAttrValuesIn, IntUniqueAttrValues1),
    mergeSets([uniqueValues(LHSAttr, 1)], IntUniqueAttrValues1, IntUniqueAttrValues2),
    
    select(uniqueValuesPerTupleBundle(LHSAttr, _), UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle1),
    mergeSets([uniqueValuesPerTupleBundle(LHSAttr, 1)], IntUniqueAttrValuesPerTupleBundle1, IntUniqueAttrValuesPerTupleBundle2),

    NumTuplesOut = NumTuplesIn,
    %updateRelationCostMeasures(IntUniqueAttrValues2, [], UniqueAttrValuesOut, NumTuplesOut),
    UniqueAttrValuesOut = IntUniqueAttrValues2,
    %monteCarloIterations(MCI),
    %updateRelationCostMeasures(IntUniqueAttrValuesPerTupleBundle2, [], UniqueAttrValuesPerTupleBundleOut, MCI).
    UniqueAttrValuesPerTupleBundleOut =  IntUniqueAttrValuesPerTupleBundle2.
    
% **** version that deals with an equality predicate with two constant attributes
computeSinglePredicateSelectivity(PredID, RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn,
                                       UniqueAttrValuesPerTupleBundleOut, NumTuplesIn, NumTuplesOut) :-
    %current_predicate(equals/3),
    %equals(PredID, LHSAttr, RHSAttr),
    %RHSAttr \= literal,
    current_predicate(compExp/5),
    compExp(PredID, equals, LHSAttr, RHSAttr, RHSAttrType),
    RHSAttrType \= literal,
    not(member(LHSAttr, RandomAttrs)),  % **** constant attributes
    not(member(RHSAttr, RandomAttrs)),
    select(uniqueValues(LHSAttr, LHSAttrUniqueValues), UniqueAttrValuesIn, IntUniqueAttrValues1),
    select(uniqueValues(RHSAttr, RHSAttrUniqueValues), IntUniqueAttrValues1, IntUniqueAttrValues2),
    (LHSAttrUniqueValues < RHSAttrUniqueValues -> AttrUniqueValues is LHSAttrUniqueValues; AttrUniqueValues is RHSAttrUniqueValues),
    mergeSets([uniqueValues(LHSAttr, AttrUniqueValues)], IntUniqueAttrValues2, IntUniqueAttrValues3),
    mergeSets([uniqueValues(RHSAttr, AttrUniqueValues)], IntUniqueAttrValues3, IntUniqueAttrValues4),
    UniqueAttrValuesPerTupleBundleOut = UniqueAttrValuesPerTupleBundleIn,
    NumTuplesOut is ceiling(AttrUniqueValues * NumTuplesIn / (LHSAttrUniqueValues * RHSAttrUniqueValues)),
    
    %updateRelationCostMeasures(IntUniqueAttrValues4, [], UniqueAttrValuesOut, NumTuplesOut).
    UniqueAttrValuesOut = IntUniqueAttrValues4.
    
% **** version that deals with an equality predicate with at least one random attribute
computeSinglePredicateSelectivity(PredID, RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn,
                                       UniqueAttrValuesPerTupleBundleOut, NumTuplesIn, NumTuplesOut) :-
    %current_predicate(equals/3),
    %equals(PredID, LHSAttr, RHSAttr),
    %RHSAttr \= literal,
    current_predicate(compExp/5),
    compExp(PredID, equals, LHSAttr, RHSAttr, RHSAttrType),
    RHSAttrType \= literal,
    (member(LHSAttr, RandomAttrs);  % **** at least one random attribute
     member(RHSAttr, RandomAttrs)),

    select(uniqueValues(LHSAttr, LHSAttrUniqueValues), UniqueAttrValuesIn, IntUniqueAttrValues1),
    select(uniqueValues(RHSAttr, RHSAttrUniqueValues), IntUniqueAttrValues1, IntUniqueAttrValues2),
    (LHSAttrUniqueValues < RHSAttrUniqueValues -> AttrUniqueValues is LHSAttrUniqueValues; AttrUniqueValues is RHSAttrUniqueValues),
    mergeSets([uniqueValues(LHSAttr, AttrUniqueValues)], IntUniqueAttrValues2, IntUniqueAttrValues3),
    mergeSets([uniqueValues(RHSAttr, AttrUniqueValues)], IntUniqueAttrValues3, IntUniqueAttrValues4),

    monteCarloIterations(MCI),
    (member(uniqueValuesPerTupleBundle(LHSAttr, LHSAttrUniqueValuesPerTupleBundle), UniqueAttrValuesPerTupleBundleIn) ->
        select(uniqueValuesPerTupleBundle(LHSAttr, LHSAttrUniqueValuesPerTupleBundle), UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle1)
        ;
        LHSAttrUniqueValuesPerTupleBundle = MCI,
        IntUniqueAttrValuesPerTupleBundle1 = UniqueAttrValuesPerTupleBundleIn
    ),

    (member(uniqueValuesPerTupleBundle(RHSAttr, RHSAttrUniqueValuesPerTupleBundle), IntUniqueAttrValuesPerTupleBundle1) ->
        select(uniqueValuesPerTupleBundle(RHSAttr, RHSAttrUniqueValuesPerTupleBundle), IntUniqueAttrValuesPerTupleBundle1, IntUniqueAttrValuesPerTupleBundle2)
        ;
        RHSAttrUniqueValuesPerTupleBundle = MCI,
        IntUniqueAttrValuesPerTupleBundle2 = IntUniqueAttrValuesPerTupleBundle1
    ),

    (LHSAttrUniqueValuesPerTupleBundle < RHSAttrUniqueValuesPerTupleBundle ->
        AttrUniqueValuesPerTupleBundle is LHSAttrUniqueValuesPerTupleBundle
        ;
        AttrUniqueValuesPerTupleBundle is RHSAttrUniqueValuesPerTupleBundle
    ),
    
    (member(LHSAttr, RandomAttrs) ->
        mergeSets([uniqueValuesPerTupleBundle(LHSAttr, AttrUniqueValuesPerTupleBundle)], IntUniqueAttrValuesPerTupleBundle2, IntUniqueAttrValuesPerTupleBundle3)
        ;
        IntUniqueAttrValuesPerTupleBundle3 = IntUniqueAttrValuesPerTupleBundle2
    ),
    
    (member(RHSAttr, RandomAttrs) ->
        mergeSets([uniqueValuesPerTupleBundle(RHSAttr, AttrUniqueValuesPerTupleBundle)], IntUniqueAttrValuesPerTupleBundle3, IntUniqueAttrValuesPerTupleBundle4)
        ;
        IntUniqueAttrValuesPerTupleBundle4 = IntUniqueAttrValuesPerTupleBundle3
    ),
    
    NumTuplesOut is NumTuplesIn,
    
    %updateRelationCostMeasures(IntUniqueAttrValues4, [], UniqueAttrValuesOut, NumTuplesOut),
    UniqueAttrValuesOut = IntUniqueAttrValues4,
    %updateRelationCostMeasures(IntUniqueAttrValuesPerTupleBundle4, [], UniqueAttrValuesPerTupleBundleOut, MCI).
    UniqueAttrValuesPerTupleBundleOut =  IntUniqueAttrValuesPerTupleBundle4.
    
% **** version that deals with a non-equality predicate with a literal and a constant or random attribute
computeSinglePredicateSelectivity(PredID, RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn,
                                       UniqueAttrValuesPerTupleBundleOut, NumTuplesIn, NumTuplesOut) :-
    %current_predicate(otherComparisonOp/4),
    %otherComparisonOp(PredID, _, LHSAttr, literal),
    current_predicate(compExp/5),
    compExp(PredID, CompExpType, LHSAttr, _, RHSAttrType),
    CompExpType \= equals,
    RHSAttrType = literal,
    select(uniqueValues(LHSAttr, AttrUniqueValues), UniqueAttrValuesIn, IntUniqueAttrValues),
    defaultSelectivity(DS),
    NewAttrUniqueValues is ceiling(AttrUniqueValues * DS),
    mergeSets([uniqueValues(LHSAttr, NewAttrUniqueValues)], IntUniqueAttrValues, UniqueAttrValuesOut),
    
    (member(LHSAttr, RandomAttrs) ->  % **** random attribute
        select(uniqueValuesPerTupleBundle(LHSAttr, AttrUniqueValuesPerTupleBundle), UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle),
        NewAttrUniqueValuesPerTupleBundle is ceiling(AttrUniqueValuesPerTupleBundle * DS),
        mergeSets([uniqueValuesPerTupleBundle(LHSAttr, NewAttrUniqueValuesPerTupleBundle)], IntUniqueAttrValuesPerTupleBundle, UniqueAttrValuesPerTupleBundleOut),
        NumTuplesOut = NumTuplesIn
        ;
        UniqueAttrValuesPerTupleBundleOut = UniqueAttrValuesPerTupleBundleIn,
        NumTuplesOut is ceiling(NumTuplesIn * DS)
    ).

% **** version that deals with a non-equality predicate with two constant/random attributes
computeSinglePredicateSelectivity(PredID, RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn,
                                       UniqueAttrValuesPerTupleBundleOut, NumTuplesIn, NumTuplesOut) :-
    %current_predicate(otherComparisonOp/4),
    %otherComparisonOp(PredID, _, LHSAttr, RHSAttr),
    %RHSAttr \= literal,

    current_predicate(compExp/5),
    compExp(PredID, CompExpType, LHSAttr, RHSAttr, RHSAttrType),
    CompExpType \= equals,
    RHSAttrType \= literal,
    
    select(uniqueValues(LHSAttr, LHSAttrUniqueValues), UniqueAttrValuesIn, IntUniqueAttrValues1),
    select(uniqueValues(RHSAttr, RHSAttrUniqueValues), IntUniqueAttrValues1, IntUniqueAttrValues2),
    defaultSelectivity(DS),
    NewLHSAttrUniqueValues is ceiling(LHSAttrUniqueValues * DS),
    NewRHSAttrUniqueValues is ceiling(RHSAttrUniqueValues * DS),
    mergeSets([uniqueValues(LHSAttr, NewLHSAttrUniqueValues)], IntUniqueAttrValues2, IntUniqueAttrValues3),
    mergeSets([uniqueValues(RHSAttr, NewRHSAttrUniqueValues)], IntUniqueAttrValues3, UniqueAttrValuesOut),

    (member(LHSAttr, RandomAttrs) ->  % **** random attribute
        select(uniqueValuesPerTupleBundle(LHSAttr, LHSAttrUniqueValuesPerTupleBundle), UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle1),
        NewLHSAttrUniqueValuesPerTupleBundle is ceiling(LHSAttrUniqueValuesPerTupleBundle * DS),
        mergeSets([uniqueValuesPerTupleBundle(LHSAttr, NewLHSAttrUniqueValuesPerTupleBundle)], IntUniqueAttrValuesPerTupleBundle1, IntUniqueAttrValuesPerTupleBundle2)
        ;
        IntUniqueAttrValuesPerTupleBundle2 = UniqueAttrValuesPerTupleBundleIn
    ),

    (member(RHSAttr, RandomAttrs) ->  % **** random attribute
        select(uniqueValuesPerTupleBundle(RHSAttr, RHSAttrUniqueValuesPerTupleBundle), IntUniqueAttrValuesPerTupleBundle2, IntUniqueAttrValuesPerTupleBundle3),
        NewRHSAttrUniqueValuesPerTupleBundle is ceiling(RHSAttrUniqueValuesPerTupleBundle * DS),
        mergeSets([uniqueValuesPerTupleBundle(LHSAttr, NewRHSAttrUniqueValuesPerTupleBundle)], IntUniqueAttrValuesPerTupleBundle3, UniqueAttrValuesPerTupleBundleOut)
        ;
        UniqueAttrValuesPerTupleBundleOut = IntUniqueAttrValuesPerTupleBundle2
    ),
    
    ((member(LHSAttr, RandomAttrs);member(RHSAttr, RandomAttrs)) ->
        NumTuplesOut is NumTuplesIn
        ;
        NumTuplesOut is ceiling(NumTuplesIn * DS)
    ).

% **** version that deals with an OR predicate
computeSinglePredicateSelectivity(PredID, RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn,
                                       UniqueAttrValuesPerTupleBundleOut, NumTuplesIn, NumTuplesOut) :-
    current_predicate(boolOr/2),
    boolOr(PredID, OrPreds),
    length(OrPreds, OrPredsLen),
    OrPredsLen > 1,  % **** have to have at least two predicates to OR
    nth0(0, OrPreds, Pred1),
    nth0(1, OrPreds, Pred2),
    select(Pred1, OrPreds, IntOrPreds1),
    select(Pred2, IntOrPreds1, IntOrPreds2),
    computeSinglePredicateSelectivity(Pred1, RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut1, UniqueAttrValuesPerTupleBundleIn,
                                       UniqueAttrValuesPerTupleBundleOut1, NumTuplesIn, NumTuplesOut1),
    computeSinglePredicateSelectivity(Pred2, RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut2, UniqueAttrValuesPerTupleBundleIn,
                                       UniqueAttrValuesPerTupleBundleOut2, NumTuplesIn, NumTuplesOut2),
    IntNumTuples is ceiling(NumTuplesOut1 + NumTuplesOut2 - (NumTuplesOut1 * NumTuplesOut2) / NumTuplesIn),
    %
    orAttributeLists(UniqueAttrValuesOut1, UniqueAttrValuesOut2, UniqueAttrValuesIn, [], IntUniqueAttrValues),
    orAttributeLists(UniqueAttrValuesPerTupleBundleOut1, UniqueAttrValuesPerTupleBundleOut2, UniqueAttrValuesPerTupleBundleIn, [], IntUniqueAttrValuesPerTupleBundle),
    mergeOrPredSelectivity(IntOrPreds2, RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn,
                             UniqueAttrValuesPerTupleBundleOut, NumTuplesIn, IntNumTuplesOut, IntNumTuples, IntUniqueAttrValues, IntUniqueAttrValuesPerTupleBundle),
    %
    
    extractAttrsFromPredicates(OrPreds, [], PredReferencedAttrs),
    intersectSets(PredReferencedAttrs, RandomAttrs, RandomOrAttrs),
    (RandomOrAttrs = [] ->  % **** predicates do not reference random attributes
        NumTuplesOut = IntNumTuplesOut
        ;
        NumTuplesOut = NumTuplesIn
    ).
    
mergeOrPredSelectivity([Pred1|OtherPreds], RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn,
                             UniqueAttrValuesPerTupleBundleOut, NumTuplesIn, NumTuplesOut, NumTuplesOut2, UniqueAttrValuesOut2, UniqueAttrValuesPerTupleBundleOut2) :-
    computeSinglePredicateSelectivity(Pred1, RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut1, UniqueAttrValuesPerTupleBundleIn,
                                       UniqueAttrValuesPerTupleBundleOut1, NumTuplesIn, NumTuplesOut1),
    IntNumTuples is ceiling(NumTuplesOut1 + NumTuplesOut2 - (NumTuplesOut1 * NumTuplesOut2) / NumTuplesIn),
    orAttributeLists(UniqueAttrValuesOut1, UniqueAttrValuesOut2, UniqueAttrValuesIn, [], IntUniqueAttrValues),
    orAttributeLists(UniqueAttrValuesPerTupleBundleOut1, UniqueAttrValuesPerTupleBundleOut2, UniqueAttrValuesPerTupleBundleIn, [], IntUniqueAttrValuesPerTupleBundle),
    mergeOrPredSelectivity(OtherPreds, RandomAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn,
                             UniqueAttrValuesPerTupleBundleOut, NumTuplesIn, NumTuplesOut, IntNumTuples, IntUniqueAttrValues, IntUniqueAttrValuesPerTupleBundle).
                             
mergeOrPredSelectivity([], _, UniqueAttrValuesIn, UniqueAttrValuesOut, UniqueAttrValuesPerTupleBundleIn, UniqueAttrValuesPerTupleBundleOut, _, NumTuplesOut, IntNumTuplesOut, _, _) :-
    UniqueAttrValuesOut = UniqueAttrValuesIn,
    UniqueAttrValuesPerTupleBundleOut = UniqueAttrValuesPerTupleBundleIn,
    NumTuplesOut = IntNumTuplesOut.
    
%*****************************************************************************************************************************
% RULEs THAT UNION THE UNIQUE VALUE LIST FOR OR'ed RELATIONS
orAttributeLists([uniqueValues(Attr, LHSAttrNo)|OtherLHSAttrs], RHSAttrs, OriginalAttrValues, AttrValuesIn, AttrValuesOut) :-
    select(uniqueValues(Attr, RHSAttrNo), RHSAttrs, OtherRHSAttrs),
    select(uniqueValues(Attr, OrigAttrNo), OriginalAttrValues, OtherAttrs),
    AttrNo is ceiling(LHSAttrNo + RHSAttrNo - (LHSAttrNo * RHSAttrNo) / OrigAttrNo),
    mergeSets([uniqueValues(Attr, AttrNo)], AttrValuesIn, IntAttrValues),
    orAttributeLists(OtherLHSAttrs, OtherRHSAttrs, OtherAttrs, IntAttrValues, AttrValuesOut).
    
orAttributeLists([uniqueValuesPerTupleBundle(Attr, LHSAttrNo)|OtherLHSAttrs], RHSAttrs, OriginalAttrValues, AttrValuesIn, AttrValuesOut) :-
    select(uniqueValuesPerTupleBundle(Attr, RHSAttrNo), RHSAttrs, OtherRHSAttrs),
    select(uniqueValuesPerTupleBundle(Attr, OrigAttrNo), OriginalAttrValues, OtherAttrs),
    AttrNo is ceiling(LHSAttrNo + RHSAttrNo - (LHSAttrNo * RHSAttrNo) / OrigAttrNo),
    mergeSets([uniqueValuesPerTupleBundle(Attr, AttrNo)], AttrValuesIn, IntAttrValues),
    orAttributeLists(OtherLHSAttrs, OtherRHSAttrs, OtherAttrs, IntAttrValues, AttrValuesOut).
    
orAttributeLists([], [], [], AttrValuesIn, AttrValuesOut) :-
    AttrValuesOut = AttrValuesIn.
    
%*****************************************************************************************************************************
% RULES THAT COMPUTE THE ANTIJOIN COST MEASURES BASED ON PREDICATE SELECTIVITIES
% ***** version that deals with a conjuction of predicates (represented as a comma separated list of predicates)
computeAntiJoinSelectivity([PredID|OtherPreds], RandomAttrs, RHSUniqueAttrValuesIn, RHSUniqueAttrValuesOut, LHSUniqueAttrValuesIn,
                                       LHSUniqueAttrValuesOut, RHSUniqueAttrValuesPerTupleBundleIn, RHSUniqueAttrValuesPerTupleBundleOut,
                                       RHSNumTuplesIn, RHSNumTuplesOut) :-
    computeAntiJoinSinglePredicateSelectivity(PredID, RandomAttrs, RHSUniqueAttrValuesIn, IntRHSUniqueAttrValues, LHSUniqueAttrValuesIn,
                                       IntLHSUniqueAttrValues, RHSUniqueAttrValuesPerTupleBundleIn, IntRHSUniqueAttrValuesPerTupleBundle,
                                       RHSNumTuplesIn, IntRHSNumTuples),
    computeAntiJoinSelectivity(OtherPreds, RandomAttrs, IntRHSUniqueAttrValues, RHSUniqueAttrValuesOut, IntLHSUniqueAttrValues,
                                       LHSUniqueAttrValuesOut, IntRHSUniqueAttrValuesPerTupleBundle, RHSUniqueAttrValuesPerTupleBundleOut,
                                       IntRHSNumTuples, RHSNumTuplesOut).

computeAntiJoinSelectivity([], _, RHSUniqueAttrValuesIn, RHSUniqueAttrValuesOut, _, _, RHSUniqueAttrValuesPerTupleBundleIn,
                                  RHSUniqueAttrValuesPerTupleBundleOut, RHSNumTuplesIn, RHSNumTuplesOut) :-
    RHSUniqueAttrValuesOut = RHSUniqueAttrValuesIn,
    RHSUniqueAttrValuesPerTupleBundleOut = RHSUniqueAttrValuesPerTupleBundleIn,
    RHSNumTuplesOut = RHSNumTuplesIn.
    
% **** version that deals with an equality predicate with two constant attributes
computeAntiJoinSinglePredicateSelectivity(PredID, RandomAttrs, RHSUniqueAttrValuesIn, RHSUniqueAttrValuesOut, LHSUniqueAttrValuesIn,
                                       LHSUniqueAttrValuesOut, RHSUniqueAttrValuesPerTupleBundleIn, RHSUniqueAttrValuesPerTupleBundleOut,
                                       RHSNumTuplesIn, RHSNumTuplesOut) :-
    %current_predicate(equals/3),
    %equals(PredID, LHSAttr, RHSAttr),
    %RHSAttr \= literal,
    current_predicate(compExp/5),
    compExp(PredID, equals, LHSAttr, RHSAttr, RHSAttrType),
    RHSAttrType \= literal,
    not(member(LHSAttr, RandomAttrs)),  % **** constant attributes
    not(member(RHSAttr, RandomAttrs)),
    
    (select(uniqueValues(RHSAttr, RHSAttrUniqueValues), RHSUniqueAttrValuesIn, IntRHSUniqueAttrValues) ->
        select(uniqueValues(LHSAttr, LHSAttrUniqueValues), LHSUniqueAttrValuesIn, IntLHSUniqueAttrValues),
        (RHSAttrUniqueValues < LHSAttrUniqueValues ->
            NewLHSAttrUniqueValues is (LHSAttrUniqueValues - RHSAttrUniqueValues),
            mergeSets([uniqueValues(RHSAttr, 0.0001)], IntRHSUniqueAttrValues, RHSUniqueAttrValuesOut),
            mergeSets([uniqueValues(LHSAttr, NewLHSAttrUniqueValues)], IntLHSUniqueAttrValues, LHSUniqueAttrValuesOut),
            RHSNumTuplesOut = 0.01
            ;
            NewRHSAttrUniqueValues is (RHSAttrUniqueValues - LHSAttrUniqueValues),
            mergeSets([uniqueValues(RHSAttr, NewRHSAttrUniqueValues)], IntRHSUniqueAttrValues, RHSUniqueAttrValuesOut),
            mergeSets([uniqueValues(LHSAttr, 0.0001)], IntLHSUniqueAttrValues, LHSUniqueAttrValuesOut),
            RHSNumTuplesOut is (RHSNumTuplesIn * NewRHSAttrUniqueValues / RHSAttrUniqueValues)
        )
        ;
        select(uniqueValues(LHSAttr, RHSAttrUniqueValues), RHSUniqueAttrValuesIn, IntRHSUniqueAttrValues),
        select(uniqueValues(RHSAttr, LHSAttrUniqueValues), LHSUniqueAttrValuesIn, IntLHSUniqueAttrValues),
        (RHSAttrUniqueValues < LHSAttrUniqueValues ->
            NewLHSAttrUniqueValues is (LHSAttrUniqueValues - RHSAttrUniqueValues),
            mergeSets([uniqueValues(LHSAttr, 0.0001)], IntRHSUniqueAttrValues, RHSUniqueAttrValuesOut),
            mergeSets([uniqueValues(RHSAttr, NewLHSAttrUniqueValues)], IntLHSUniqueAttrValues, LHSUniqueAttrValuesOut),
            RHSNumTuplesOut = 0.01
            ;
            NewRHSAttrUniqueValues is (RHSAttrUniqueValues - LHSAttrUniqueValues),
            mergeSets([uniqueValues(LHSAttr, NewRHSAttrUniqueValues)], IntRHSUniqueAttrValues, RHSUniqueAttrValuesOut),
            mergeSets([uniqueValues(RHSAttr, 0.0001)], IntLHSUniqueAttrValues, LHSUniqueAttrValuesOut),
            RHSNumTuplesOut is (RHSNumTuplesIn * NewRHSAttrUniqueValues / RHSAttrUniqueValues)
        )
    ),
    RHSUniqueAttrValuesPerTupleBundleOut = RHSUniqueAttrValuesPerTupleBundleIn.
    
%*****************************************************************************************************************************
% RULES THAT COMPUTE THE SEMIJOIN COST MEASURES BASED ON PREDICATE SELECTIVITIES
% ***** version that deals with a conjuction of predicates (represented as a comma separated list of predicates)
computeSemiJoinSelectivity([PredID|OtherPreds], RandomAttrs, RHSUniqueAttrValuesIn, RHSUniqueAttrValuesOut, LHSUniqueAttrValuesIn,
                                       LHSUniqueAttrValuesOut, RHSUniqueAttrValuesPerTupleBundleIn, RHSUniqueAttrValuesPerTupleBundleOut,
                                       RHSNumTuplesIn, RHSNumTuplesOut) :-
    computeSemiJoinSinglePredicateSelectivity(PredID, RandomAttrs, RHSUniqueAttrValuesIn, IntRHSUniqueAttrValues, LHSUniqueAttrValuesIn,
                                       IntLHSUniqueAttrValues, RHSUniqueAttrValuesPerTupleBundleIn, IntRHSUniqueAttrValuesPerTupleBundle,
                                       RHSNumTuplesIn, IntRHSNumTuples),
    computeSemiJoinSelectivity(OtherPreds, RandomAttrs, IntRHSUniqueAttrValues, RHSUniqueAttrValuesOut, IntLHSUniqueAttrValues,
                                       LHSUniqueAttrValuesOut, IntRHSUniqueAttrValuesPerTupleBundle, RHSUniqueAttrValuesPerTupleBundleOut,
                                       IntRHSNumTuples, RHSNumTuplesOut).

computeSemiJoinSelectivity([], _, RHSUniqueAttrValuesIn, RHSUniqueAttrValuesOut, _, _, RHSUniqueAttrValuesPerTupleBundleIn,
                                  RHSUniqueAttrValuesPerTupleBundleOut, RHSNumTuplesIn, RHSNumTuplesOut) :-
    RHSUniqueAttrValuesOut = RHSUniqueAttrValuesIn,
    RHSUniqueAttrValuesPerTupleBundleOut = RHSUniqueAttrValuesPerTupleBundleIn,
    RHSNumTuplesOut = RHSNumTuplesIn.

% **** version that deals with an equality predicate with two constant attributes
computeSemiJoinSinglePredicateSelectivity(PredID, RandomAttrs, RHSUniqueAttrValuesIn, RHSUniqueAttrValuesOut, LHSUniqueAttrValuesIn,
                                       LHSUniqueAttrValuesOut, RHSUniqueAttrValuesPerTupleBundleIn, RHSUniqueAttrValuesPerTupleBundleOut,
                                       RHSNumTuplesIn, RHSNumTuplesOut) :-
    %current_predicate(equals/3),
    %equals(PredID, RHSAttr, LHSAttr),
    %RHSAttr \= literal,
    current_predicate(compExp/5),
    compExp(PredID, equals, LHSAttr, RHSAttr, RHSAttrType),
    RHSAttrType \= literal,
    not(member(LHSAttr, RandomAttrs)),  % **** constant attributes
    not(member(RHSAttr, RandomAttrs)),

    (select(uniqueValues(RHSAttr, RHSAttrUniqueValues), RHSUniqueAttrValuesIn, IntRHSUniqueAttrValues) ->
        select(uniqueValues(LHSAttr, LHSAttrUniqueValues), LHSUniqueAttrValuesIn, IntLHSUniqueAttrValues),
        (RHSAttrUniqueValues < LHSAttrUniqueValues ->
            mergeSets([uniqueValues(RHSAttr, RHSAttrUniqueValues)], IntRHSUniqueAttrValues, RHSUniqueAttrValuesOut),
            mergeSets([uniqueValues(LHSAttr, RHSAttrUniqueValues)], IntLHSUniqueAttrValues, LHSUniqueAttrValuesOut),
            RHSNumTuplesOut is (RHSNumTuplesIn * RHSAttrUniqueValues / RHSAttrUniqueValues)
            ;
            mergeSets([uniqueValues(RHSAttr, LHSAttrUniqueValues)], IntRHSUniqueAttrValues, RHSUniqueAttrValuesOut),
            mergeSets([uniqueValues(LHSAttr, LHSAttrUniqueValues)], IntLHSUniqueAttrValues, LHSUniqueAttrValuesOut),
            RHSNumTuplesOut is (RHSNumTuplesIn * LHSAttrUniqueValues / RHSAttrUniqueValues)
        )
        ;
        select(uniqueValues(LHSAttr, RHSAttrUniqueValues), RHSUniqueAttrValuesIn, IntRHSUniqueAttrValues),
        select(uniqueValues(RHSAttr, LHSAttrUniqueValues), LHSUniqueAttrValuesIn, IntLHSUniqueAttrValues),
        (RHSAttrUniqueValues < LHSAttrUniqueValues ->
            mergeSets([uniqueValues(LHSAttr, RHSAttrUniqueValues)], IntRHSUniqueAttrValues, RHSUniqueAttrValuesOut),
            mergeSets([uniqueValues(RHSAttr, RHSAttrUniqueValues)], IntLHSUniqueAttrValues, LHSUniqueAttrValuesOut),
            RHSNumTuplesOut is (RHSNumTuplesIn * RHSAttrUniqueValues / RHSAttrUniqueValues)
            ;
            mergeSets([uniqueValues(LHSAttr, LHSAttrUniqueValues)], IntRHSUniqueAttrValues, RHSUniqueAttrValuesOut),
            mergeSets([uniqueValues(RHSAttr, LHSAttrUniqueValues)], IntLHSUniqueAttrValues, LHSUniqueAttrValuesOut),
            RHSNumTuplesOut is (RHSNumTuplesIn * LHSAttrUniqueValues / RHSAttrUniqueValues)
        )
    ),
    RHSUniqueAttrValuesPerTupleBundleOut = RHSUniqueAttrValuesPerTupleBundleIn.
    
%*****************************************************************************************************************************
% RULEs THAT UPDATE THE COST OF THE UNIQUE VALUES PER ATTRIBUTE AFTER A PREDICATE HAS BEEN APPLIED
updateRelationCostMeasures([uniqueValues(LHSAttr, LHSAttrUniqueValues)|OtherAttrs], UniqueAttrValuesIn, UniqueAttrValuesOut, MAXNUM) :-
    (LHSAttrUniqueValues >  MAXNUM ->
        mergeSets([uniqueValues(LHSAttr, MAXNUM)], UniqueAttrValuesIn, IntUniqueAttrValues)
        ;
        mergeSets([uniqueValues(LHSAttr, LHSAttrUniqueValues)], UniqueAttrValuesIn, IntUniqueAttrValues)
    ),
    updateRelationCostMeasures(OtherAttrs, IntUniqueAttrValues, UniqueAttrValuesOut, MAXNUM).
    
updateRelationCostMeasures([uniqueValuesPerTupleBundle(LHSAttr, LHSAttrUniqueValues)|OtherAttrs], UniqueAttrValuesPerTupleBundleIn, UniqueAttrValuesPerTupleBundleOut, MAXNUM) :-
    (LHSAttrUniqueValues >  MAXNUM ->
        mergeSets([uniqueValuesPerTupleBundle(LHSAttr, MAXNUM)], UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle)
        ;
        mergeSets([uniqueValuesPerTupleBundle(LHSAttr, LHSAttrUniqueValues)], UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle)
    ),
    updateRelationCostMeasures(OtherAttrs, IntUniqueAttrValuesPerTupleBundle, UniqueAttrValuesPerTupleBundleOut, MAXNUM).
    
updateRelationCostMeasures([], UniqueAttrValuesPerTupleBundleIn, UniqueAttrValuesPerTupleBundleOut, _) :-
    UniqueAttrValuesPerTupleBundleOut = UniqueAttrValuesPerTupleBundleIn.
                             
%*****************************************************************************************************************************
% RULEs THAT ADD THE COST MEASURES OF THE OUTPUT ATTRIBUTES OF A GENERALIZED AGGREGATE TO THE SET OF OUTPUT ATTRIBUTES
integrateAggregatedAttrs([InAttrs|OtherInAttrs], [OutAttrs|OtherOutAttrs], OutAttrsRanges, RandomAttrs, NumTuples, AttrSizes, GenAggUniqueAttrValuesIn,
                    GenAggUniqueAttrValuesOut, GenAggUniqueAttrValuesPerTupleBundleIn, GenAggUniqueAttrValuesPerTupleBundleOut, GenAggByAttrSizesIn, GenAggAttrSizesOut) :-
    findall(InAttrSize, (member(InAttr, InAttrs), member(attributeSize(InAttr, InAttrSize), AttrSizes)), InAttrSizes),
    maxElement(InAttrSizes, 0, MaxAttrSize),
    setof(attributeSize(OutAttr, MaxAttrSize), member(OutAttr, OutAttrs), OutAttrSizes),
    mergeSets(OutAttrSizes, GenAggByAttrSizesIn, IntGenAggByAttrSizes),
    
    computeOutputAttrUniqueValues(OutAttrs, OutAttrsRanges, RandomAttrs, NumTuples, GenAggUniqueAttrValuesIn,
                                 IntGenAggUniqueAttrValues, GenAggUniqueAttrValuesPerTupleBundleIn, IntGenAggUniqueAttrValuesPerTupleBundle),
    
    integrateAggregatedAttrs(OtherInAttrs, OtherOutAttrs, OutAttrsRanges, RandomAttrs, NumTuples, AttrSizes, IntGenAggUniqueAttrValues,
                    GenAggUniqueAttrValuesOut, IntGenAggUniqueAttrValuesPerTupleBundle, GenAggUniqueAttrValuesPerTupleBundleOut, IntGenAggByAttrSizes, GenAggAttrSizesOut).

integrateAggregatedAttrs([], [], _, _, _, _, GenAggUniqueAttrValuesIn, GenAggUniqueAttrValuesOut, GenAggUniqueAttrValuesPerTupleBundleIn,
                                                              GenAggUniqueAttrValuesPerTupleBundleOut, GenAggAttrSizesIn, GenAggAttrSizesOut) :-
    GenAggUniqueAttrValuesOut = GenAggUniqueAttrValuesIn,
    GenAggUniqueAttrValuesPerTupleBundleOut = GenAggUniqueAttrValuesPerTupleBundleIn,
    GenAggAttrSizesOut = GenAggAttrSizesIn.

%*****************************************************************************************************************************
% RULEs THAT ESTIMATE UNIQUE VALUE CARDINALITY FOR OUTPUTS OF SCALAR EXPRESSIONS
computeOutputAttrUniqueValues([OutAttr|OtherOutAttrs], OutAttrsRanges, RandomAttrs, NumTuples, GenAggUniqueAttrValuesIn,
                                 GenAggUniqueAttrValuesOut, GenAggUniqueAttrValuesPerTupleBundleIn, GenAggUniqueAttrValuesPerTupleBundleOut) :-
    OutAttrsRanges \= infinite,
    member(range(OutAttr, infinite), OutAttrsRanges),
    mergeSets([uniqueValues(OutAttr, NumTuples)], GenAggUniqueAttrValuesIn, IntGenAggUniqueAttrValues),
    (member(OutAttr, RandomAttrs) ->
        monteCarloIterations(MCI),
        mergeSets([uniqueValuesPerTupleBundle(OutAttr, MCI)], GenAggUniqueAttrValuesPerTupleBundleIn, IntGenAggUniqueAttrValuesPerTupleBundle)
        ;
        IntGenAggUniqueAttrValuesPerTupleBundle = GenAggUniqueAttrValuesPerTupleBundleIn
    ),
    computeOutputAttrUniqueValues(OtherOutAttrs, OutAttrsRanges, RandomAttrs, NumTuples, IntGenAggUniqueAttrValues,
                                 GenAggUniqueAttrValuesOut, IntGenAggUniqueAttrValuesPerTupleBundle, GenAggUniqueAttrValuesPerTupleBundleOut).

computeOutputAttrUniqueValues([OutAttr|OtherOutAttrs], infinite, RandomAttrs, NumTuples, GenAggUniqueAttrValuesIn,
                                 GenAggUniqueAttrValuesOut, GenAggUniqueAttrValuesPerTupleBundleIn, GenAggUniqueAttrValuesPerTupleBundleOut) :-
    mergeSets([uniqueValues(OutAttr, NumTuples)], GenAggUniqueAttrValuesIn, IntGenAggUniqueAttrValues),
    (member(OutAttr, RandomAttrs) ->
        monteCarloIterations(MCI),
        mergeSets([uniqueValuesPerTupleBundle(OutAttr, MCI)], GenAggUniqueAttrValuesPerTupleBundleIn, IntGenAggUniqueAttrValuesPerTupleBundle)
        ;
        IntGenAggUniqueAttrValuesPerTupleBundle = GenAggUniqueAttrValuesPerTupleBundleIn
    ),
    computeOutputAttrUniqueValues(OtherOutAttrs, infinite, RandomAttrs, NumTuples, IntGenAggUniqueAttrValues,
                                 GenAggUniqueAttrValuesOut, IntGenAggUniqueAttrValuesPerTupleBundle, GenAggUniqueAttrValuesPerTupleBundleOut).
                                 
computeOutputAttrUniqueValues([OutAttr|OtherOutAttrs], OutAttrsRanges, RandomAttrs, NumTuples, GenAggUniqueAttrValuesIn,
                                 GenAggUniqueAttrValuesOut, GenAggUniqueAttrValuesPerTupleBundleIn, GenAggUniqueAttrValuesPerTupleBundleOut) :-
    OutAttrsRanges \= infinite,
    member(range(OutAttr, constant(ValueNum)), OutAttrsRanges),

    (ValueNum < NumTuples ->
        mergeSets([uniqueValues(OutAttr, ValueNum)], GenAggUniqueAttrValuesIn, IntGenAggUniqueAttrValues)
        ;
        mergeSets([uniqueValues(OutAttr, NumTuples)], GenAggUniqueAttrValuesIn, IntGenAggUniqueAttrValues)
    ),
    (member(OutAttr, RandomAttrs) ->
        monteCarloIterations(MCI),
        (ValueNum < MCI ->
            mergeSets([uniqueValuesPerTupleBundle(OutAttr, ValueNum)], GenAggUniqueAttrValuesPerTupleBundleIn, IntGenAggUniqueAttrValuesPerTupleBundle)
            ;
            mergeSets([uniqueValuesPerTupleBundle(OutAttr, MCI)], GenAggUniqueAttrValuesPerTupleBundleIn, IntGenAggUniqueAttrValuesPerTupleBundle)
        )
        ;
        IntGenAggUniqueAttrValuesPerTupleBundle = GenAggUniqueAttrValuesPerTupleBundleIn
    ),
    computeOutputAttrUniqueValues(OtherOutAttrs, OutAttrsRanges, RandomAttrs, NumTuples, IntGenAggUniqueAttrValues,
                                 GenAggUniqueAttrValuesOut, IntGenAggUniqueAttrValuesPerTupleBundle, GenAggUniqueAttrValuesPerTupleBundleOut).
                                 
computeOutputAttrUniqueValues([], _, _, _, GenAggUniqueAttrValuesIn, GenAggUniqueAttrValuesOut, GenAggUniqueAttrValuesPerTupleBundleIn, GenAggUniqueAttrValuesPerTupleBundleOut) :-
    GenAggUniqueAttrValuesOut = GenAggUniqueAttrValuesIn,
    GenAggUniqueAttrValuesPerTupleBundleOut = GenAggUniqueAttrValuesPerTupleBundleIn.
                                 
%*****************************************************************************************************************************
% RULE THAT ESTIMATES THE SIZES OF THE OUTPUT ATTRIBUTES OF SCALAR EXPRESSIONS
computeFuncOutAttrSizes([InAttrs|OtherInAttrs], [OutAttrs|OtherOutAttrs], FuncInAttrSizes, FuncOutAttrSizesIn, FuncOutAttrSizesOut) :-
    findall(InAttrSize, (member(InAttr, InAttrs), member(attributeSize(InAttr, InAttrSize), FuncInAttrSizes)), InAttrSizes),
    maxElement(InAttrSizes, 0, MaxAttrSize),
    setof(attributeSize(OutAttr, MaxAttrSize), member(OutAttr, OutAttrs), AttrSizes),
    mergeSets(AttrSizes, FuncOutAttrSizesIn, IntFuncOutAttrSizes),
    computeFuncOutAttrSizes(OtherInAttrs, OtherOutAttrs, FuncInAttrSizes, IntFuncOutAttrSizes, FuncOutAttrSizesOut).
    
computeFuncOutAttrSizes([], [], _, FuncOutAttrSizesIn, FuncOutAttrSizesOut) :-
    FuncOutAttrSizesOut = FuncOutAttrSizesIn.

%*****************************************************************************************************************************
% RULE THAT COMPUTES THE TOTAL BYTES COMING OUT OF AN OPERATOR NODE
computeTupleSize([attributeSize(AttrName, AttrSize1)| OtherAttrs], NodeRandomAttrs, TupleSizeIn, TupleSizeOut) :-

    (attributeSize(AttrName, AttrSizeX) ->
     AttrSize = AttrSizeX
     ;
     AttrSize = AttrSize1),

%         AttrSize = AttrSize1,
    (member(AttrName, NodeRandomAttrs) ->
        monteCarloIterations(MCNum),
        IntTupleSize is (TupleSizeIn + MCNum * AttrSize)
        ;
        IntTupleSize is (TupleSizeIn + AttrSize)
    ),
    computeTupleSize(OtherAttrs, NodeRandomAttrs, IntTupleSize, TupleSizeOut).
    
computeTupleSize([], _, TupleSizeIn, TupleSizeOut) :-
    TupleSizeOut = TupleSizeIn.
    
%*****************************************************************************************************************************
% RULES THAT COMPUTE THE INTERSETCTION BETWEEN THE OUTPUT ATTRIBUTES AND THE COST MEASURES OF A NODE
intersectUniqueAttrValues([uniqueValues(Attr, NO)|OtherUniqueAttrValues], NodeOutputAttrs, UniqueAttrValuesIn, UniqueAttrValuesOut) :-
    (member(Attr, NodeOutputAttrs) ->
        mergeSets([uniqueValues(Attr, NO)], UniqueAttrValuesIn, IntUniqueAttrValues)
        ;
        IntUniqueAttrValues = UniqueAttrValuesIn
    ),
    intersectUniqueAttrValues(OtherUniqueAttrValues, NodeOutputAttrs, IntUniqueAttrValues, UniqueAttrValuesOut).
    
intersectUniqueAttrValues([], _, UniqueAttrValuesIn, UniqueAttrValuesOut) :-
    UniqueAttrValuesOut = UniqueAttrValuesIn.
    
intersectUniqueAttrValuesPerTupleBundle([uniqueValuesPerTupleBundle(Attr, NO)|OtherUniqueAttrValuesPerTupleBundle], NodeOutputAttrs, UniqueAttrValuesPerTupleBundleIn, UniqueAttrValuesPerTupleBundleOut) :-
    (member(Attr, NodeOutputAttrs) ->
        mergeSets([uniqueValuesPerTupleBundle(Attr, NO)], UniqueAttrValuesPerTupleBundleIn, IntUniqueAttrValuesPerTupleBundle)
        ;
        IntUniqueAttrValuesPerTupleBundle = UniqueAttrValuesPerTupleBundleIn
    ),
    intersectUniqueAttrValuesPerTupleBundle(OtherUniqueAttrValuesPerTupleBundle, NodeOutputAttrs, IntUniqueAttrValuesPerTupleBundle, UniqueAttrValuesPerTupleBundleOut).
    
intersectUniqueAttrValuesPerTupleBundle([], _, UniqueAttrValuesPerTupleBundleIn, UniqueAttrValuesPerTupleBundleOut) :-
    UniqueAttrValuesPerTupleBundleOut = UniqueAttrValuesPerTupleBundleIn.
    
intersectAttrSizes([attributeSize(Attr, NO)|OtherAttrSizes], NodeOutputAttrs, AttrSizesIn, AttrSizesOut) :-
    (member(Attr, NodeOutputAttrs) ->
        mergeSets([attributeSize(Attr, NO)], AttrSizesIn, IntAttrSizes)
        ;
        IntAttrSizes = AttrSizesIn
    ),
    intersectAttrSizes(OtherAttrSizes, NodeOutputAttrs, IntAttrSizes, AttrSizesOut).
    
intersectAttrSizes([], _, AttrSizesIn, AttrSizesOut) :-
    AttrSizesOut = AttrSizesIn.



%%% ADDED BY LUIS: VECTOR/MATRIX AGGREGATES
%%updateAggregateAttSizes(AggExprs,AggExprOutAtts, ChildNumTuples, GenAggNumTuples, AttSizesIn, AttSizesOut) 

updateAggregateAttSizes([], _, _, _, AttSizes, AttSizes) :-
  !.

updateAggregateAttSizes([AggExp | T1], [OutAtts | T2], ChildNumTuples, GenAggNumTuples, AttSizesIn, AttSizesOut) :-
  aggExp(AggExp, AggType, _, _),
  is_vector_aggType(AggType),  
  !,
  OutAtts = [OutAttName | _],
  selectchk(attributeSize(OutAttName, AttSize), AttSizesIn, AttSizesIn1),
  (ChildNumTuples = GenAggNumTuples ->
   VecAttSize is (AttSize)
   ;
   VecAttSize is (AttSize * (ChildNumTuples / GenAggNumTuples))
  ),
  !,
  retractall(attributeSize(OutAttName, _)),
  updateAggregateAttSizes(T1, T2, ChildNumTuples, GenAggNumTuples, [attributeSize(OutAttName, VecAttSize) | AttSizesIn1], AttSizesOut).

updateAggregateAttSizes([ _ | T1], [_ | T2], ChildNumTuples, GenAggNumTuples, AttSizesIn, AttSizesOut) :-
  !,
  updateAggregateAttSizes(T1, T2, ChildNumTuples, GenAggNumTuples, AttSizesIn, AttSizesOut).

is_vector_aggType(vector).
is_vector_aggType(rowmatrix).
is_vector_aggType(colmatrix).
