

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
import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;

// path filter classes!

/**
 * This file selects only the SimSQL table files in a given directory.
 */

// no hidden files or log files, and must end on .tbl
class TableFileFilter implements PathFilter {

  public TableFileFilter() {
  }

  public boolean accept(Path p) {
    String name = p.getName();

    if (name.startsWith("_") || name.startsWith("."))
      return false;

    if (name.endsWith(".tbl"))
      return true;
      
    try {
      return (p.getFileSystem(new Configuration()).isDirectory(p));
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}


// no hidden files or log files, and must end on .stats
class StatsFileFilter implements PathFilter {

  public StatsFileFilter() {
  }

  public boolean accept(Path p) {
    String name = p.getName();
    return name.endsWith(".stats") && !name.startsWith("_") && !name.startsWith(".");
  }
}
