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

public class StringAttributeWithLength extends StringAttribute {
	
	  private int myLength;
	
	  public StringAttributeWithLength (int length) {
		  myLength = length;
	  }
	
	  public Attribute readSelfFromTextStream (BufferedReader readFromMe) throws IOException {
		    if (myLength == 0)
			  return new StringAttribute();
	
		    // check if there is an array, signified by a leaing '<'
		    readFromMe.mark (1);
		    int firstChar = readFromMe.read ();
		    while (firstChar == '\n')
		      firstChar = readFromMe.read ();
	
		    if (firstChar == '<') {
		      readFromMe.reset ();
		      return new StringArrayAttribute ().readSelfFromTextStream (readFromMe);
	
		    // see if we hit an EOF
		    } else if (firstChar < 0) {
		      return null;
		    }
	
		    // there was not a reading '<', so read in the single double
		    // this is the space we'll use to do the parsing... we assume it is less than 64 chars
		    int curLen = 64;
		    char [] myArray = new char[curLen];
	
		    // allows us to match the word "null"
		    char [] nullString = {'n', 'u', 'l', 'l'};
	
		    // read in the first char
		    myArray[0] = (char) firstChar;
	
		    // this loop reads in until (and including) the '|'
		    int i;
		    boolean isNull = true;
		    for (i = 1; myArray[i - 1] != '|'; i++) {
		      if (i == myLength) {
		    	  i++;
		    	  break;
		      }
	
		      // double the size of the buffer if needed
		      if (i == curLen) {
		        char [] myNewArray = new char[curLen * 2];
		        for (int j = 0; j < curLen; j++) {
		          myNewArray[j] = myArray[j];
		        }
		        myArray = myNewArray;
		        curLen *= 2;
		      }
	
		      // isNull gets set to false if we find a char that does not match the string 'null'
		      if (i - 1 <= 3 && myArray[i - 1] != nullString[i - 1]) {
		        isNull = false;
		      }
		      myArray[i] = (char) readFromMe.read ();
		    }
		    
		    // complete the empty entries
		    for (i = i-1; i <= myLength; i++) {
		    	myArray[i] = '\0';
		    }
		    
	
		    // if we got here, we read in a '|'
		    if (isNull == true && i == 5) {
	
		      // this means we got a null!
		      return NullAttribute.NULL;
	
		    } else {
	
		      // we did not get a null!
		      try {
		        return new StringAttribute (new String (myArray, 0, i - 1));
		      } catch (Exception e) {
		        throw new IOException ("Error when I tried to read in a String att... didn't parse to a string");
		      }
		    }
		  }

}
