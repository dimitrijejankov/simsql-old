

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


:- dynamic function/3.
:- dynamic attributeSize/2.
:- multifile function/3.

%%% This "loader" is responsible for handling command-line parameters.

go :-
    current_prolog_flag(argv, Args),
    go(Args).

go([_, Command, FileIn, NumIters1, MaxPlans1, FileOut]) :-
    command_ok(Command),
    style_check(-discontiguous),
    load_files([FileIn], [silent(true), scope_settings(true)]),
    atom_number(NumIters1, NumIters),
    atom_number(MaxPlans1, MaxPlans),
    !,
    optimize(Command, NumIters, MaxPlans, NumPlans, TotalTime, Applied, PlansIter, FileOut),
    !,
    writeq(NumPlans), nl,
    writeq(TotalTime), nl,
    writeq(Applied), nl,
    writeq(PlansIter), nl,
    !,
    halt(0).

go([_|_]) :-
    halt(1).

command_ok(optimize).
command_ok(cut).
command_ok(computeCost).

