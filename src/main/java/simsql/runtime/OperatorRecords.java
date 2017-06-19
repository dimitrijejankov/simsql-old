

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


// contains all the record types used by operators.

package simsql.runtime;

import java.util.*;
import java.nio.ByteBuffer;

// this is a record that is being read into the operator.
abstract class InputRecord extends AbstractRecord {

    abstract public int getNumAttributes();

    abstract public short getTypeCode();

    abstract public HashableRecord runSelectionAndProjection();
}

// this is a record that we get from processing the input to the join or any other operator.
// includes methods for splitting.
abstract class HashableRecord extends AbstractRecord {

    abstract public int getNumAttributes();

    abstract public short getTypeCode();

    abstract public long getHashKey();

    public long getSecondaryHashKey() {
        return -1;
    }

    public HashableRecord() {
    }


    private HashableRecord[] noSplit = new HashableRecord[]{this};

    // the general split mechanism
    protected HashableRecord[] split(int whichAtts[]) {

        // check for empties -- in that case, we don't split
        if (whichAtts != null && whichAtts.length == 0) {
            return noSplit;
        }

        // now, check for splittable attributes and find the number of
        // MCs to split.
        boolean allSingletons = true;
        int numMCs = 1;

        // are we splitting some particular attributes?
        if (whichAtts != null) {
            for (int i = 0; i < whichAtts.length; i++) {

                // did we find a non-singleton?
                if (atts[whichAtts[i]].getSize() > 1) {

                    // if so, break the loop and set the values.
                    allSingletons = false;
                    numMCs = atts[whichAtts[i]].getSize();
                    break;
                }
            }
        }

        // otherwise, check the entire record.
        else {

            // create the whichAtts...
            whichAtts = new int[getNumAttributes()];
            for (int i = 0; i < atts.length; i++) {

                whichAtts[i] = i;

                // did we find a non-singleton?
                if (atts[i].getSize() > 1) {

                    // if so, set the values.
                    allSingletons = false;
                    numMCs = atts[i].getSize();
                }
            }
        }

        // are all atts singletons? if so, just leave
        if (allSingletons) {
            return noSplit;
        }

        // split all the attributes first.
        ArrayList<HashMap<Attribute, Bitstring>> allSplits = new ArrayList<HashMap<Attribute, Bitstring>>();
        for (int i = 0; i < whichAtts.length; i++) {
            allSplits.add(atts[whichAtts[i]].split());
        }

        // now, split each MC iteration.
        ArrayList<HashableRecord> outRecs = new ArrayList<HashableRecord>();
        for (int i = 0; i < numMCs; i++) {

            // make space
            Attribute[] newAtts = new Attribute[getNumAttributes()];
            Bitstring newIsPresent = (isPresent == null) ? (BitstringWithSingleValue.TRUE) : isPresent;

            // copy all attribute values.
            for (int j = 0; j < getNumAttributes(); j++) {
                newAtts[j] = atts[j];
            }

            // split on.
            for (int j = 0; j < whichAtts.length; j++) {

                for (Attribute att : allSplits.get(j).keySet()) {

                    // is the attribute valid for the current MC?
                    Bitstring splitBits = allSplits.get(j).get(att);
                    if (splitBits.getValue(i)) {
                        newAtts[whichAtts[j]] = att;
                        newIsPresent = newIsPresent.and(splitBits);
                    }
                }
            }

            // see if this new record is not a duplicate
            boolean notThere = true;
            for (HashableRecord rec : outRecs) {

                boolean allEqual = true;
                for (int j = 0; j < getNumAttributes(); j++) {
                    allEqual &= rec.atts[j].equals(newAtts[j]).allAreTrue();
                }

                // do we have a duplicate?
                if (allEqual) {

                    // if so, bail out.
                    notThere = false;
                    break;
                }
            }

            // add it to the output set, if possible
            if (newIsPresent.getValue(i) && notThere) {

                // make a new output record.
                HashableRecord newRec = (HashableRecord) buildRecordOfSameType();
                newRec.atts = newAtts;
                newRec.isPresent = newIsPresent;
                outRecs.add(newRec);
            }
        }

        return outRecs.toArray(new HashableRecord[0]);
    }

    // splits *all* the attributes in the record
    public HashableRecord[] splitAll() {

        // "null" means we will split all the attributes.
        return split(null);
    }

    // splits a few attributes in the record -- override and use in
    // the template.
    public HashableRecord[] splitSome() {

        // default -- none, override at template level.
        return split(new int[0]);
    }

    // for Java collections -- uses all attributes.
    // child types must override.
    public boolean equals(Object obj) {

        // same object?
        if (obj == this)
            return true;

        // other type?
        if (!(obj instanceof HashableRecord))
            return false;

        HashableRecord rec = (HashableRecord) obj;

        // other typecode?
        if (rec.getTypeCode() != getTypeCode())
            return false;

        // equivalent atts?
        for (int i = 0; i < getNumAttributes(); i++) {
            if (!(atts[i].equals(rec.atts[i]).allAreTrue()))
                return false;
        }

        return true;
    }

    // similarly, for Java collections -- uses all attributes.
    public int hashCode() {
        return (int) getHashKey();
    }
}

// generic class for the groupby/aggregate that consumes others of the
// same type, produces a final record with the desired aggregates, and
// can be put into a HashMap, using the grouping attributes as the
// key.
abstract class AggregatorRecord extends HashableRecord {

    abstract public int getNumAttributes();

    abstract public short getTypeCode();

    abstract public long getHashKey();

    abstract public boolean isFromSameGroup(AggregatorRecord rec);

    abstract public void consume(AggregatorRecord me);

    abstract public Record getFinal();

    // for the Java collections
    public boolean equals(Object obj) {

        // same object?
        if (obj == this)
            return true;

        // other type?
        if (!(obj instanceof AggregatorRecord))
            return false;

        // equivalent groups?
        return isFromSameGroup((AggregatorRecord) obj);
    }

    // for the Java collections, too.
    public int hashCode() {
        return (int) getHashKey();
    }
}

// this is a record that we get from splitting the input to the inference
abstract class ConstantRecord extends HashableRecord {

    abstract public int getNumAttributes();

    abstract public short getTypeCode();

    abstract public long getHashKey();

    abstract public ConstantRecord[] runSplit();

    abstract public ConstantRecord[] runSplit(int[] attsindex);

    abstract public boolean isFromSameGroup(ConstantRecord rec);

    // for the Java collections
    public boolean equals(Object obj) {

        // same object?
        if (obj == this)
            return true;

        // other type?
        if (!(obj instanceof ConstantRecord))
            return false;

        // equivalent groups?
        return isFromSameGroup((ConstantRecord) obj);
    }

    // for the Java collections, too.
    public int hashCode() {
        return (int) getHashKey();
    }

}

//this is a record that we get as the final result of an inference
abstract class InferResultRecord extends AbstractRecord {

    abstract public int getNumAttributes();

    abstract public short getTypeCode();

    public static Record inference(ConstantRecord input) {
        return null;
    }
}

// this is a record that we get as the final result of a join
abstract class JoinResultRecord extends AbstractRecord {

    abstract public int getNumAttributes();

    abstract public short getTypeCode();

    public static Record join(AbstractRecord left, AbstractRecord right) {
        return null;
    }

    ;

    // these two methods are for the semijoins
    public static Bitstring test(AbstractRecord left, AbstractRecord right) {
        return BitstringWithSingleValue.FALSE;
    }

    public static Record compose(AbstractRecord left, AbstractRecord right, Bitstring predResult) {
        return null;
    }

}

// dummy result class for selections
class SelectionOut extends HashableRecord {
    public int getNumAttributes() {
        return 0;
    }

    public short getTypeCode() {
        return -1;
    }

    public long getHashKey() {
        return 0;
    }

    public SelectionOut() {
    }
}

// dummy result for union
class UnionOutputRecord extends AbstractRecord {
    public int getNumAttributes() {
        return 0;
    }

    public short getTypeCode() {
        return -1;
    }

    public UnionOutputRecord() {
    }
}

// dummy result class for joins
class Result extends JoinResultRecord {

    public int getNumAttributes() {
        return 0;
    }

    public Result() {
    }

    static public Record join(AbstractRecord left, AbstractRecord right) {
        return null;
    }

    public short getTypeCode() {
        return 0;
    }

    public static Bitstring test(AbstractRecord left, AbstractRecord right) {
        return BitstringWithSingleValue.FALSE;
    }

    public static Record compose(AbstractRecord left, AbstractRecord right, Bitstring predResult) {
        return null;
    }
}


// dummy classes that can be referenced by the join, when dealing with self-joins.
class RightIn extends InputRecord {
    public int getNumAttributes() {
        return 0;
    }

    public short getTypeCode() {
        return -1;
    }

    public HashableRecord runSelectionAndProjection() {
        return null;
    }
}

class LeftIn extends InputRecord {
    public int getNumAttributes() {
        return 0;
    }

    public short getTypeCode() {
        return -1;
    }

    public HashableRecord runSelectionAndProjection() {
        return null;
    }
}

class RightOut extends HashableRecord {
    public int getNumAttributes() {
        return 0;
    }

    public short getTypeCode() {
        return -1;
    }

    public long getHashKey() {
        return 0;
    }

    public static AbstractRecord getNull() {
        return null;
    }

    public RightOut() {
    }
}

// dummy result class for inferences
class InferResult extends InferResultRecord {

    public int getNumAttributes() {
        return 0;
    }

    public short getTypeCode() {
        return 0;
    }

    public InferResult() {
    }

    static public Record inference(ConstantRecord input) {
        return null;
    }
}

// this is the configuration object used by the VGWrapper
class VGWrapperConfig {
    public VGFunction function = null;

    public int getNumMC() {
        return 0;
    }

    public int getBuffSize() {
        return 0;
    }

    public int getBuffMaxSize() {
        return 0;
    }

    public int getPosSize() {
        return 0;
    }
}

// this is the type of record that goes into the VGWrapper operator.
abstract class VGRecord extends HashableRecord {

    public abstract int getNumAttributes();

    public abstract short getTypeCode();

    public abstract long getHashKey();

    public abstract long getSecondaryHashKey();

    public abstract boolean containsSeed();

    public abstract boolean isOuterRecord();

    public abstract boolean isFromSameSeedGroup(VGRecord me);

    public abstract boolean allAreEqual();

    public abstract void initializeVG(VGFunction f, int k);

    public abstract Attribute[] getVGInputAtts();

    // for the Java collections
    public boolean equals(Object obj) {

        // same object?
        if (obj == this)
            return true;

        // other type?
        if (!(obj instanceof VGRecord))
            return false;

        // equivalent groups?
        return isFromSameSeedGroup((VGRecord) obj);
    }

    // for the Java collections, too.
    public int hashCode() {
        return (int) getHashKey();
    }

    // a constructor that sets everything null.
    private static Attribute myNullAtt = NullAttribute.NULL;

    public VGRecord() {
        atts = new Attribute[getNumAttributes()];
        isPresent = BitstringWithSingleValue.TRUE;
        for (int i = 0; i < getNumAttributes(); i++) {
            atts[i] = myNullAtt;
        }
    }

    public void setPoolForRecycling(RecordPool pool) {
        super.setPoolForRecycling(pool);
        isPresent = BitstringWithSingleValue.TRUE;
        for (int i = 0; i < getNumAttributes(); i++) {
            atts[i] = myNullAtt;
        }
    }

    public void recycle() {
        super.recycle();
        isPresent = BitstringWithSingleValue.TRUE;
        for (int i = 0; i < getNumAttributes(); i++) {
            atts[i] = myNullAtt;
        }
    }
}

// this is the type of record that leave the VGWrapper operator.

class VGOutputRecord extends AbstractRecord {

    public int getNumAttributes() {
        return 0;
    }

    public short getTypeCode() {
        return 0;
    }

    public boolean runSelection() {
        return false;
    }

    public VGOutputRecord() {
    }

    public VGOutputRecord(Bitstring outerIsPresent, VGRecord rec, Attribute[] inputAtts) {
    }

    public void setMeUp(Bitstring outerIsPresent, VGRecord rec, Attribute[] inputAtts) {
    }
}

// this class is used to get the order in which groups of input records are presented to the reducer
class Ordering {
    static Class<?>[] getOrdering() {
        Class<?>[] temp = {LoaderRecord.class};
        return temp;
    }
}

// this is used by the deserializers to obtain the list of types that they need to deserialize
class RecTypeList {
    static Class<?>[] getPossibleRecordTypes() {
        Class<?>[] temp = {LoaderRecord.class};
        return temp;
    }
}
