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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class VectorAttributeWithLength extends VectorAttribute {
	
	  private int myLength;
		
	  public VectorAttributeWithLength (int length) {
		  myLength = length;
	  }
	
	  public Attribute readSelfFromTextStream (BufferedReader readFromMe) throws IOException {

		    // this is the space we'll use to do the parsing... we assume it is less than 64 chars
		    char [] myArray = new char[64];

		    // allows us to match the word "null"
		    char [] nullString = {'n', 'u', 'l', 'l'};

		    // records the doubles and nulls we've read in
		    ArrayList <Double> myDoubles = new ArrayList <Double> (64);
		    // ArrayList <Boolean> myNulls = new ArrayList <Boolean> (64);
		    
		    boolean isNull = true;
		   
		    // suck in the '[', but ignore a leading newline character
		    int startChar = readFromMe.read ();
		    while (startChar == '\n') 
		      startChar = readFromMe.read ();

		    // if we got an eof, we are done
		    if (startChar == -1) {
		      return null;
		    }

		    if (startChar != '[') {
		      throw new IOException ("Read in a bad vector start character when reading a vector; expected '['");
		    }

		    // this tells us we did not see a null anywhere in the input
		    boolean gotANull = false;

		    // read in the first char
		    myArray[0] = (char) readFromMe.read ();

		    // keep reading until we find the ']'
		    while (myArray[0] != ']') {

		      // this loop reads in until (and including) the '|'
		      int i;
		      for (i = 1; myArray[i - 1] != ',' && myArray[i-1] != ']'; i++) {
		    	  
		    	if (isNull) {
			        // isNull gets set to false if we find a char that does not match the string 'null'
			        if (i - 1 <= 3 && myArray[i - 1] != nullString[i - 1]) {
			          isNull = false;  
			        }
		    	}
		        myArray[i] = (char) readFromMe.read ();  
		      }

		      // if we got here, we read in a ','
		      if (isNull == true && i == 5) {

		        // this means we got a null!
		        // myDoubles.add (0.0);
		        // gotANull = true;
		    	    // suck in the final '|'
		    	  if (readFromMe.read () != '|') {
		    	    throw new IOException ("Error when I tried to read in a vector: didn't close with a '|'");
		    	  }
		    	  return NullAttribute.NULL;

		      } else {

		        // we did not get a null!
		        try {
		          myDoubles.add (Double.valueOf (new String (myArray, 0, i - 1)));
		        } catch (Exception e) {
		          throw new IOException ("Error when I tried to read in a vector... an entry didn't parse to a double");
		        }
		      }
		      

		      // prime the parse of the next item in the vector
		      if(myArray[i-1] != ']')
		    	  myArray[0] = (char) readFromMe.read ();
		      else
		    	  break;
		    }

		    // suck in the final '|'
		    if (readFromMe.read () != '|') {
		      throw new IOException ("Error when I tried to read in a vector: didn't close with a '|'");
		    }
		    
		    if (myDoubles.size() != myLength) {
		    	throw new IOException ("Error when I tried to read in a vector: the length of the vector does not meet requirement");
		    }

		    // at this point we've read the entire vector, so make an attribute out of it
		    double [] myDoubleArray = new double[myDoubles.size ()];
		    for (int i = 0; i < myDoubles.size (); i++) {
		      myDoubleArray[i] = myDoubles.get (i);
		    }
		    return new VectorAttribute (myDoubleArray); 
	  }


}
