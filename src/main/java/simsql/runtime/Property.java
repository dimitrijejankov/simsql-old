

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

import java.util.*;
import java.io.*;

/**
 * Interacts with the property file in a static manner, facilitating
 * retrieval of properties in a single line.
 *
 * @author Luis
 */

public class Property {
    private static Properties props;
    
    // default loading block
    static {
	props = new Properties();
	try {
	    InputStream in = Property.class.getResourceAsStream("simsql.properties");
	    props.load(in);
	    in.close();
	} catch (IOException e) {
	    throw new RuntimeException("Could not load default properties file!", e);
	}
    }

    /** Returns a given property. */
    public static String get(String propName) {
	String ret = props.getProperty(propName);
	if (ret == null) {
	    throw new RuntimeException("Could not retrieve property " + propName);
	}
	
	return(ret);
    }
}
