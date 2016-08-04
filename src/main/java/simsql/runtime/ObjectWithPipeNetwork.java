

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



package simsql.runtime;

import java.util.ArrayList;

// this is an object that has a publically accessable PipeNetwork buried inside of it...
// all of the mappers and the reducers used by the SimSQL runtime will implement this 
// interface, because it might be desirable for the implementation to have access to the
// pipe network inside of the object
//
// IMPORTANT: one thing that you need to be careful about is that in general, one input
// can produce multiple outputs... so if more than one piece of code can be using the
// same ObjectWithPipeNetwork, you need to make sure to remove ALL of the records that
// were procuded by your one call to "process", or else you can leave a bunch of records
// in there that someone else might take out later on
//
interface ObjectWithPipeNetwork {

  // move a record through the pipe network
  public void process (Record me);

  // this is called to get a record from the output of the PipeNetwork... returns
  // a null if there is not any output
  public Record getResult ();

  // this is called to let the network know it is about to die... it returns any records
  // that need to be flushed out... keep calling until a NULL is returned
  public Record cleanupNetwork ();

}
