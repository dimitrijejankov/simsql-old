

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


%*********************************************************************************************************************************
% RULEs THAT CHECKS WHETHER TWO SETS ARE EQUAL
equalSets([], []).

equalSets([Member|OtherMembers], Set2) :-
    select(Member, Set2, IntSet2),
    equalSets(OtherMembers, IntSet2).
    
%*********************************************************************************************************************************
% RULE THAT CHECKS WHETHER THE LEFT SET IS SUBSET OF THE RIGHT SET
subset([H|T], S) :- member(H,S), subset(T, S).
subset([], _).

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
% RULE THAT FINDS THE INTERSECTION OF TWO SETS
intersectSets(Set1, [Member|OtherSetMembers], NewSet) :-
    intersectSets(Set1, OtherSetMembers, IntSet),
    (member(Member, Set1) -> append([Member], IntSet, NewSet); NewSet = IntSet).

intersectSets(_, [], []).

%*********************************************************************************************************************************
% RULE THAT FINDS THE MAXIMUM ELEMENT IN A LIST
maxElement([Elem|OtherElems], MaxIn, MaxOut) :-
    (Elem > MaxIn -> IntMax is Elem; IntMax is MaxIn),
    maxElement(OtherElems, IntMax, MaxOut).

maxElement([], MaxElem, MaxElem).

%*********************************************************************************************************************************
% RULE THAT REPLACES ALL APPEARANCES OF A GIVEN ELEMENT IN A LIST WITH A NEW ELEMENT
replaceInList(OldElement, NewElement, [Element|OtherElements], NewListIn, NewListOut) :-
    (OldElement = Element ->
        append(NewListIn, [NewElement], IntNewList)
        ;
        append(NewListIn, [Element], IntNewList)
    ),
    replaceInList(OldElement, NewElement, OtherElements, IntNewList, NewListOut).
    
replaceInList(_, _, [], NewListIn, NewListOut) :-
    NewListOut = NewListIn.
    
%******************************************************************************************************************************
% RULE FOR MERGING ALL OF THE ELEMENTS OF A LIST OF LISTS SO THAT YOU GET A SINGLE LIST
% assuming that the first arg is a list of lists, this returns true if the second arg results from appending all of those lists
% together; for example, dePartition ([[1,2,3],[3],[4,5]], [1,2,3,3,4,5]) is true.
%
%dePartition([], []).
%dePartition([Head|Tail], ListOut) :-
%        dePartition(Tail, TempList), append(Head, TempList, ListOut).
