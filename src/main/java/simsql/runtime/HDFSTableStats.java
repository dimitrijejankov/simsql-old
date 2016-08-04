

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
import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.hadoop.conf.Configuration;

/**
 * Collects the statistics for a given table.
 */
public class HDFSTableStats implements Serializable {

	// total number of tuples in the relation
	private static final long BIG_PRIME = 2L*3L*5L*7L*11L*13L*17L*19L*23L*29L*31L*37L*41L*43L + 1L;
	private static final long COUNT_THRESHOLD = 1000;
	private static final long COMPRESS_THRESHOLD = 10000;
	
	private long numTuples;
	private int numAttribute; 

	// Each column has a attributeMap to compute the unique values in this column.
	private ArrayList<AttributeHashTable> attributeMapList;
	private ArrayList<Double> attributeMaxMappedValueList;
	
	public HDFSTableStats() {
		attributeMapList = new ArrayList<AttributeHashTable>();
		attributeMaxMappedValueList = new ArrayList<Double>();
		clear();
	}

	// clear up the structure.
	public void clear() {
		attributeMapList.clear();
		attributeMaxMappedValueList.clear();
		numTuples = 0L;
		numAttribute = 0;
	}

	// take an input record.
	public void take(Record inRec){
		if (numTuples == 0) {
			numAttribute = inRec.getNumAttributes();
			for (int i = 0; i < numAttribute; i++) {
			        attributeMapList.add(new AttributeHashTable(0.1, 1024));
				attributeMaxMappedValueList.add(1.0);
			}
		}

		// increase the counter
		numTuples++;
		
		// Here, we do not consider the BitString
		for (int i = 0; i < numAttribute; i++) {
			Attribute attribute = inRec.getIthAttribute(i).getSingleton();
			long recordValue = attribute.getHashCode();
			
			double digestValue = getPositionValue(recordValue);
			if(digestValue >= attributeMaxMappedValueList.get(i)||
			   attributeMapList.get(i).contains(attribute, recordValue)) {
			  //nothing to do
			}
			else {
			  attributeMapList.get(i).add(attribute, recordValue);

			}

		}
		
		if(numTuples % COMPRESS_THRESHOLD == 0)
		{
			constrainMapList();
		}
	}
	
	/*
	 * organize the map for each attribute, and make sure the size of each map smaller than or equal with COUNT_THRESHOLD.
	 * If a map's size is larger than COUNT_THRESHOLD, then divide the upThreshold of this map by 2, and then remove all
	 * the elements that has hash value larger than the upThreshold.
	 */
	
	private void constrainMapList()
	{
		long hashCode;
		Attribute attribute;
		double positionValue;
		
		for(int i = 0; i < numAttribute; i++)
		{
			AttributeHashTable oldMap = attributeMapList.get(i);
			AttributeHashTable newMap;
			
			double upThreshold = attributeMaxMappedValueList.get(i);

			// a bit of optimization here -- L
			if (oldMap.size() > COUNT_THRESHOLD) {
			    
			    Iterator<Attribute> it = oldMap.iterator();
			    while (true) {

				ArrayList<Attribute> newGuy = new ArrayList<Attribute>();
				upThreshold = upThreshold/2;
				while (it.hasNext()) {
				    attribute = it.next();
				    hashCode = attribute.getHashCode();
				    positionValue = getPositionValue(hashCode);

				    if(positionValue < upThreshold) {
					newGuy.add(attribute);
				    }
				}

				if (newGuy.size() > COUNT_THRESHOLD) {
				    it = newGuy.iterator();
				} else {

				    // at this point, we're done.
				    newMap = new AttributeHashTable(0.1, oldMap.size());
				    for (Attribute ax : newGuy) {
					newMap.add(ax, ax.getHashCode());
				    }

				    oldMap = newMap;
				    break;
				}
			    }
			}
			
			attributeMaxMappedValueList.remove(i);
			attributeMaxMappedValueList.add(i, upThreshold);
			
			attributeMapList.remove(i);
			attributeMapList.add(i, oldMap);
		}
	}
	
	//return a position belonging to [0, 1)
	private static double getPositionValue(long hashKey) {
		hashKey = Long.reverse(hashKey);
		
		if(hashKey <= 0){
			hashKey = - hashKey;
			
			if(hashKey <= 0)
				return 0.5;
		}
		
		hashKey = hashKey % BIG_PRIME;
		return hashKey/(double)BIG_PRIME;
	}

	// take a collector from another reducer.
	public void consume(HDFSTableStats singleStatistcs) {
		
		// case 0: if the new coming is empty
		if (singleStatistcs.numTuples() == 0)
		{
			return;
		}
		// case 1: if the current reducer is empty
		else if(numTuples == 0){
			numAttribute = singleStatistcs.numAttribute;
			numTuples = singleStatistcs.numTuples();
			attributeMapList = singleStatistcs.getAttributeMapList();
			attributeMaxMappedValueList = singleStatistcs.getAttributeMaxMappedValueList();
		}
		//case 2: if both reducer are not empty, but they are not compatible.
		else if(numAttribute != singleStatistcs.numAttribute)
		{
			throw new RuntimeException("The number of attributes from two reducers are not equal!");
		}
		//case 3: both reducer are correct.
		else
		{
			long hashCode;
			Attribute attribute;
			double positionValue;
			
			for(int i = 0; i < numAttribute; i++)
			{
				AttributeHashTable map1 = attributeMapList.get(i);
				double upThreshold1 = attributeMaxMappedValueList.get(i);
				
				AttributeHashTable map2 = singleStatistcs.getAttributeMapList().get(i);
				double upThreshold2 = singleStatistcs.getAttributeMaxMappedValueList().get(i);
				
				double minThreshold = upThreshold1;
				
				if(minThreshold > upThreshold2)
				{
					minThreshold = upThreshold2;
				}
				
				AttributeHashTable newTable = new AttributeHashTable(0.1, map1.size());
				
				Iterator<Attribute> it = map1.iterator();
				//add the associated elements in map1 to new table
				while(it.hasNext())
				{
					attribute = it.next();
					hashCode = attribute.getHashCode();
					positionValue = getPositionValue(hashCode);
					
					if(positionValue < minThreshold && !newTable.contains(attribute, hashCode))
					{
						newTable.add(attribute, hashCode);
					}
				}
				
				//add the associated elements in map2 to new table
				it = map2.iterator();
				while(it.hasNext())
				{
					attribute = it.next();
					hashCode = attribute.getHashCode();
					positionValue = getPositionValue(hashCode);
					
					if(positionValue < minThreshold && !newTable.contains(attribute, hashCode))
					{
						newTable.add(attribute, hashCode);
					}
				}
				
				attributeMaxMappedValueList.remove(i);
				attributeMaxMappedValueList.add(i, minThreshold);
				
				attributeMapList.remove(i);
				attributeMapList.add(i, newTable);
			}
			
			//Constrain the statistics from the current reducer.
			constrainMapList();

			//add the number of tuples.
			numTuples += singleStatistcs.numTuples();
		}
	}

	// load from an HDFS file or directory.
	@SuppressWarnings("unchecked")
	public void load(String path) throws IOException, ClassNotFoundException {

		// look up the input file...
		Path file = new Path(path);
		Configuration conf = new Configuration();
		FileSystem fs = file.getFileSystem(conf);

		// is it a directory?
		if (fs.exists(file) && fs.isDirectory(file)) {

			// if so, traverse all of it.
			clear();
			for (FileStatus ff : fs.listStatus(file, new StatsFileFilter())) {

				HDFSTableStats guyMerged = new HDFSTableStats();
				guyMerged.load(ff.getPath().toUri().getPath());
				consume(guyMerged);
			}
		} else if (fs.exists(file)) {

			// otherwise, just read it in.
			FSDataInputStream fileIn = fs.open(file);
			ObjectInputStream in = new ObjectInputStream(fileIn);

			HDFSTableStats newGuy = (HDFSTableStats) in.readObject();
			in.close();

			// destroy our contents and read.
			clear();
			consume(newGuy);
		}
	}

	// save to an HDFS file.
	public void save(String path) throws IOException {

		if (!path.endsWith(".stats")) {
			path += ".stats";
		}

		Path file = new Path(path);
		Configuration conf = new Configuration();
		FileSystem fs = file.getFileSystem(conf);
		FSDataOutputStream fileOut = fs.create(file, true);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
	}

	// getters
	public long numTuples() {
		return numTuples;
	}

	public int numAttributes() {
		return numAttribute;
	}

	public long numUniques(int att) {
		if(att >= numAttribute)
		{
			throw new RuntimeException("The att is larger the the number of attributes in the table");
		}
		
		AttributeHashTable map = attributeMapList.get(att);
		double upThreshold = attributeMaxMappedValueList.get(att);
		
		if(upThreshold < 0.5 && map.size() == 0)
		{
			return Long.MAX_VALUE;
		}
		else
		{	
			return (long)((double)map.size()/upThreshold);
		}
	}
	
	public ArrayList<AttributeHashTable> getAttributeMapList() {
		return attributeMapList;
	}

	public void setAttributeMapList(
			ArrayList<AttributeHashTable> attributeMapList) {
		this.attributeMapList = attributeMapList;
	}

	public ArrayList<Double> getAttributeMaxMappedValueList() {
		return attributeMaxMappedValueList;
	}

	public void setAttributeMaxMappedValueList(
			ArrayList<Double> attributeMaxMappedValueList) {
		this.attributeMaxMappedValueList = attributeMaxMappedValueList;
	}

	public String toString() {
		String outStr = "{" + numTuples + ", [";

		if (numAttribute > 0) {

			outStr += numUniques(0);
			for (int i = 1; i < numAttribute; i++) {
				outStr += ", " + numUniques(i);
			}
		}

		outStr += "]}";
		return outStr;
	}
}
