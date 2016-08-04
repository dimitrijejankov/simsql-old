

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


package simsql.functions;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function that returns a positive integer if an input attribute is
 * NULL, zero otherwise.
 *
 * @author Luis.
 */
public class nullfn extends Function {

	// simple constructor
	public nullfn() {
		super(new AttributeType(new DoubleType()));
	}

	public Attribute apply(Attribute... atts) {
		if (atts.length == 0)
			return new IntAttribute(1);

		return atts[0].isNull().toInt();
	}

	public Attribute eval() {
		return new IntAttribute(0);
	}

	public String getName() {
		return "nullfn";
	}

	public AttributeType getOutputType() {
		return new AttributeType(new IntType());
	}
}
