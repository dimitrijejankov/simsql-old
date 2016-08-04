

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
 * A function for seeding.
 *
 * @author Luis
 */
public class seed extends Function {
    
	// a static global array for seed values.
	private static byte[] seedComponents = null;

	// the RNG itself
	private static Random rng = null;

	static {

		// let's build a basic seed with the following components:
		//   - mac address
		//   - current time
		//   - hadoop job task ID.

		// add the process ID
		buildIntoSeed(ManagementFactory.getRuntimeMXBean().getName().getBytes());

		// add the mac address
		try {
	    buildIntoSeed(NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress());
		} catch (Exception e) { }

		// add the current time
		buildIntoSeed(Calendar.getInstance().getTime().toString().getBytes());
	};

	// simple constructor
	public seed() {

		// call the base constructor.
		super(new AttributeType(new IntType()));
	}

	// static method for adding values for the hash of the seed.
	public static void buildIntoSeed(byte[] inputBytes) {

		// do we have anything at all?
		if (seedComponents == null) {
	    seedComponents = inputBytes;
		}
		else {

	    // if not, then expand the array.
	    byte[] newSeedComponents = new byte[seedComponents.length + inputBytes.length];
	    for (int i=0;i<seedComponents.length;i++) {
				newSeedComponents[i] = seedComponents[i];
	    }

	    for (int i=0;i<inputBytes.length;i++) {
				newSeedComponents[seedComponents.length + i] = inputBytes[i];
	    }

	    seedComponents = newSeedComponents;
		}

		// then we hash the components and build an RNG with the resulting long.
		rng = new Random(Hash.hashMe(seedComponents));
	}


	@Override
    public Attribute apply(Attribute... atts) {
		if (atts.length == 0 || atts[0] instanceof NullAttribute)
	    return new SeedAttribute(rng.nextLong());
	
		atts[0].injectSelf(this);

		if (numMC > 1) {
	    throw new RuntimeException("Array attributes not allowed for seeding!");
		}

		curParam = 0;
		numMC = 1;	

		return new SeedAttribute((Long)inParams[0].getValue(0));
	}

	public Attribute eval() {
		return new SeedAttribute(rng.nextLong());
	}

	public String getName() {
		return "seed";
	}

	public AttributeType getOutputType() {
		return new AttributeType(new IntType());
	}

}
