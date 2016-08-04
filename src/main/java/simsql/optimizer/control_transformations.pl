

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


% **** by (un)commenting the desired rules one can control
% **** the application of transformation rules

%applyTransformation(composeMaterializedView).

applyTransformation(changeJoinOrders).

%applyTransformation(mergeSelections).
applyTransformation(pushSelectionDownJoin).

applyTransformation(pushSelectionDownSemiJoin).
applyTransformation(pushSelectionDownAntiJoin).
applyTransformation(pushSelectionDownSeed).
applyTransformation(pushSelectionDownDedup).
applyTransformation(pushSelectionDownSplit).
applyTransformation(pushSelectionDownScalarFunc).
applyTransformation(pushSelectionDownRename).
applyTransformation(pushSelectionDownGenAgg).

%applyTransformation(pushSemiJoinDownJoin).
%applyTransformation(pushSemiJoinDownSelection).
%applyTransformation(pushSemiJoinDownSeed).
%applyTransformation(pushSemiJoinDownDedup).
%applyTransformation(pushSemiJoinDownSplit).
%applyTransformation(pushSemiJoinDownScalarFunc).
%applyTransformation(pushSemiJoinDownGenAgg).

%applyTransformation(pushAntiJoinDownSelection).
%applyTransformation(pushAntiJoinDownSeed).
%applyTransformation(pushAntiJoinDownDedup).
%applyTransformation(pushAntiJoinDownSplit).
%applyTransformation(pushAntiJoinDownScalarFunc).
%applyTransformation(pushAntiJoinDownGenAgg).
%applyTransformation(pushAntiJoinDownJoin).

applyTransformation(simplifyScalarFunc).

applyTransformation(mergeSplits).

applyTransformation(pushJoinDownRename).
applyTransformation(eliminateDedup).

applyTransformation(pullSeedUpJoin).

applyTransformation(copySelectionThroughVGWrapperBranch).
applyTransformation(copySemiJoinThroughVGWrapperBranch).
applyTransformation(copyAntiJoinThroughVGWrapperBranch).
applyTransformation(copyJoinThroughVGWrapperBranch).

applyTransformation(pushSingleVGWrapperUpInPlan).
%applyTransformation(pushGroupedVGWrapperUpInPlan).
