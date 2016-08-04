

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

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class AbstractRecord implements Record {

    // this is (obviously) the bitstring that tells us if the record is valid
    protected Bitstring isPresent;
  
    // this contains all of the Record's attributes
    protected Attribute [] atts;
  
    // this is the value of the sort attribute; -1 means that it is not used
    private long sortVal = -1;
 
    // this is used to searialize Attribute objects
    static AttributeIOMachine attributeIOMachine = new AttributeIOMachine ();
  
    // and this is used to serialize Bitstring objects
    static BitstringIOMachine bitstringIOMachine = new BitstringIOMachine ();
  
    // abstract methods to be implemented by a child
    abstract public short getTypeCode ();
    abstract public int getNumAttributes ();

    public long getSortAttribute () {
      return sortVal;
    }

    public boolean hasSortAttribute () {
      return sortVal >= 0;
    }

    public void setSortAttribute (long toMe) {
      sortVal = toMe; 
    }
  
    public Bitstring getIsPresent () {
	return isPresent; 
    }
    
    public void setIsPresent (Bitstring toMe) {
	isPresent = toMe; 
    }
  
    public AbstractRecord () {
	atts = new Attribute [getNumAttributes ()]; 
    }
    
    public Attribute getIthAttribute (int i) {
	return atts[i]; 
    }
  
    public void setIthAttribute (int i, Attribute setToMe) {
	atts[i] = setToMe;
    }
  
    public int writeSelfToStream (DataOutputStream writeToMe) throws IOException {
    
	// write the typecode for the record
	int total = 2;
	writeToMe.writeShort (getTypeCode ());
    
	// write the bitstring
	total += bitstringIOMachine.writeToStream (isPresent, writeToMe);
    
        // write the sort attribute, if it is there
        if (hasSortAttribute ()) 
          total += attributeIOMachine.writeToStream (getSortAttribute (), writeToMe);

	// and write the attributes
	for (int i = 0; i < getNumAttributes (); i++) {
	    total += attributeIOMachine.writeToStream (atts[i], writeToMe);
	}
    
	// return the number of bytes written
	return total;
    }
  
    public Record buildRecordOfSameType () {
	try {
	    return (Record) getClass ().newInstance (); 
	} catch (InstantiationException e) {
	    throw new RuntimeException ("I could not create a record of the same type (1) " + getClass (), e);
	} catch (IllegalAccessException e) {
	    throw new RuntimeException ("I could not create a record of the same type (2)", e);
	}
    }
  
    public int readSelfFromStream (DataInputStream readFromMe) throws IOException {

	// note: it is assumed that the typecode has already been read
    
	// read the bitstring
	WriteableInt numBytes = new WriteableInt ();
	isPresent = bitstringIOMachine.readFromStream (readFromMe, numBytes);
	long total = numBytes.get ();

	// and now read all of the attributes
        WriteableInt longVal = new WriteableInt ();
	for (int i = 0; i < getNumAttributes (); i++) {
	    atts[i] = attributeIOMachine.readFromStream (readFromMe, numBytes, longVal);

            // if we got back a null, it means that a long value (the sort key) was written to that slot
            if (atts[i] == null) {
              sortVal = longVal.get ();
              i--;
            }

	    total += numBytes.get ();
	}
    
	// return the number of bytes read
	return (int) total;
    }

    // returns the size in bytes of this record in memory
    public long sizeInMemory() throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	DataOutputStream dos = new DataOutputStream(baos);

	writeSelfToStream(dos);
	dos.close();
	return baos.size();
    }

    protected RecordPool myPool = null;

    public void setPoolForRecycling(RecordPool pool) {

      // avoid sending to multiple pools.
      if (myPool == null) {
	myPool = pool;
	
	// clear up the record.
	sortVal = -1;	
	isPresent = null;
	for (int i=0;i<getNumAttributes();i++) {
	  atts[i] = null;
	}
      }
    }

    public void recycle() {

      // recycle something only once.
      if (myPool != null) {
	myPool.put(this);
	myPool = null;
      }
    }

    public void copyMyselfFrom (Record me) {
         if (me.getNumAttributes () != getNumAttributes ())
             throw new RuntimeException ("can't do a record copy where the num atts does not match!");

         if (me.hasSortAttribute ()) {
             setSortAttribute (me.getSortAttribute ());
         } else {
             sortVal = -1; 
         }

         for (int i = 0; i < me.getNumAttributes (); i++) {
             setIthAttribute (i, me.getIthAttribute (i));
	 }

	 setIsPresent (me.getIsPresent ());
    }
}
