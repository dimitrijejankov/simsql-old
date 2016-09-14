

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
% RULEs THAT CHECK WHETHER THE FIRST NODE IS DESCENDANT OF THE SECOND IN A PATH THAT HAS NO VGWRAPPERS THE QUERY GRAPH
isDescendantNoVGWrapper(FirstNode, SecondNode, Graph, Nodes) :-
    member(parent(ParentNode, FirstNode), Graph),
    not(member(vgwrapper(ParentNode, _, _, _, _, _, _, _, _), Nodes)),
    (SecondNode = ParentNode -> true;
        isDescendantNoVGWrapper(ParentNode, SecondNode, Graph, Nodes)
    ).

isDescendantNoVGWrapper(Node, Node, _).

%*****************************************************************************************************************************
% RULE THAT CHECKS WHETHER A NODE IS AN ANCESTOR OF ANOTHER NODE IN A GIVEN GRAPH
isAncestor(AncestorCandidate, Node, Graph) :-
    member(parent(AncestorCandidate, Node), Graph).

isAncestor(AncestorCandidate, Node, Graph) :-
    member(parent(AncestorCandidate, IntNode), Graph),
    isAncestor(IntNode, Node, Graph).
    
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
    
    (select(frameoutput(ParentNodeID, FrameOutputChildren, FrameOutputFiles), IntNodes4, IntNodes5) ->
        replaceInList(FromWhichNode, ToWhichNode, FrameOutputChildren, [], NewFrameOutputChildren),
        append([frameoutput(ParentNodeID, NewFrameOutputChildren, FrameOutputFiles)], IntNodes5, IntNodes6)
        ;
        IntNodes6 = IntNodes4
    ),
    swapNodeParents(OtherParents, FromWhichNode, ToWhichNode, IntGraph2, GraphOut, IntNodes6, NodesOut).

swapNodeParents([], _, _, GraphIn, GraphOut, NodesIn, NodesOut) :-
    GraphOut = GraphIn,
    NodesOut = NodesIn.

%*****************************************************************************************************************************
% RULE THAT CHECKS WHETHER A NODE HAS A SINGLE PARENT
nodeHasSingleParent(WhichNode, Graph) :-
    setof(parent(ParentNode, WhichNode), member(parent(ParentNode, WhichNode), Graph), NodeParents),
    length(NodeParents, NodeParentsSetLen),
    NodeParentsSetLen is 1.
    
%*****************************************************************************************************************************
% RULE THAT FINDs THE COMMON ANCESTOR FOR A SET OF NODES
findLeastCommonAncestor([AnchorNode|OtherNodes], Graph, LCANode) :-
  (isNodeLCA(AnchorNode, OtherNodes, Graph) ->
      LCANode = AnchorNode
      ;
      member(parent(ParentNode, AnchorNode), Graph),
      findLeastCommonAncestor([ParentNode|OtherNodes], Graph, LCANode)
  ).

%*****************************************************************************************************************************
% RULEs THAT CHECK WHETHER A NODE IS THE COMMON ANCESTOR FOR A SET OF NODES
isNodeLCA(LCACandidateNode, [Node|OtherNodes], Graph) :-
    isDescendant(Node, LCACandidateNode, Graph),
    isNodeLCA(LCACandidateNode, OtherNodes, Graph).

isNodeLCA(_, [], _).
