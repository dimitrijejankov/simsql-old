

/*****************************************************************************
 *                                                                           *
 *  Copyright 2014 Rice University                                           *
 *                                                                           *
 *  Licensed under the Apache License, Version 2.0 (the "License");          *
 *  you may not use this file except in compliance with the License.         *
 *  You may obtain a copy of the License at                                  *
 *                                                                           *
 *      http://www.apache.org/licenses/LICENSE-2.0                           *
 *                                                                           *
 *  Unless required by applicable law or agreed to in writing, software      *
 *  distributed under the License is distributed on an "AS IS" BASIS,        *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 *  See the License for the specific language governing permissions and      *
 *  limitations under the License.                                           *
 *                                                                           *
 *****************************************************************************/



package simsql.shell;

import java.math.BigDecimal;

// encapsulates an SQL optimizer
public interface Optimizer <Input extends CompiledQuery, Output extends OptimizedQuery> {
  
  // this optimizes the compiled query, returning an optimized version of the query
  Output optimize (Input optimizeMe, RuntimeParameter param);

  // this computes the cost of a compiled query, returning a BigDecimal with the final cost.
  BigDecimal computeCost (Input optimizeMe, RuntimeParameter param);
}

