

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


package simsql.compiler; // package mcdb.catalog;

import java.sql.*;
import java.util.*;
import java.io.File;
import simsql.runtime.TypeMachine;

/**
 * This class takes care of accessing the SQLite database.
 * 
 * @author Luis, Bamboo
 */

public class DataAccess {

	// some constants for object types
	public static final int OBJ_RELATION = 0;
	public static final int OBJ_VGFUNCTION = 1;
	public static final int OBJ_VIEW = 2;
	public static final int OBJ_RANDRELATION = 3;
	public static final int OBJ_VALUES_TABLE = 4;
	public static final int OBJ_GENERAL_FUNCTION = 5;
	public static final int OBJ_UNION_VIEW = 6;

	/** Database connection */
	Connection conn;

	/** Our constructor... */
	public DataAccess (File where, boolean okToCreate) throws Exception{

                // first, we see if this file exits
                if (!where.isFile () && !okToCreate) {
                  throw new RuntimeException ("The file was not there, and I was not allowed to create it");
                }

		try {
		  Class.forName("org.sqlite.JDBC");
		  conn = DriverManager.getConnection("jdbc:sqlite:" + where.getPath ());
		} catch (Exception e) {
		  System.err.println("Unable to load SQLite database!");
                  throw new RuntimeException ("Error trying to create the catalog");
		}
	}

	/** Kind of a destructor... just kill the connection */
	public void close(){
		try {
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	/*
	 * -------------------------------- All Database Objects ---------------------------------------
	 */
	
	/**
	 * Retrieves the names of *all* database objects (relations, vg functions,
	 * views, random tables)
	 */
	public ArrayList<String> getObjectNames() throws Exception {
		ArrayList<String> out = new ArrayList<String>();

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT objName FROM DBObject");

			// run it
			ResultSet rs = ps.executeQuery();

			// fill the output structure
			while (rs.next()) {
				out.add(rs.getString(1));
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
		}

		return (out);
	}
	
	/**
	 * Retrieves the names of *all* ValuesTable objects.
	 */
	public ArrayList<String> getValuesTableNames() throws Exception{
		ArrayList<String> out = new ArrayList<String>();

		// prepare the SQL statement
		PreparedStatement ps = conn
				.prepareStatement("SELECT objName FROM DBObject where objType = " + OBJ_VALUES_TABLE);

		// run it
		ResultSet rs = ps.executeQuery();

		// fill the output structure
		while (rs.next()) {
			out.add(rs.getString(1));
		}

		return (out);
	}
	
	/* retrieve the value of a configuration object given its name */
	public String getConfigObjectName(String objName) {
		try {
			/* prepare the SQL statement */
			PreparedStatement ps = conn
					.prepareStatement("SELECT objValue FROM DBConfigObject WHERE objName='" + objName + "'");

			/* run it */
			ResultSet rs = ps.executeQuery();

			/* return value */
			if (rs.next()) {
				return (rs.getString(1));
			}
		} catch (Exception e) {
			System.err.println("Unable to retrieve config object value :-(");
			e.printStackTrace();
			System.err.println("Unable to find the object " + objName);
		}

		return (null);
	}
	

	public int getObjectType(String object)
	{
		try
		{
			PreparedStatement ps = conn.prepareStatement("SELECT objType FROM DBObject where objName " +
					"= '" + object + "'");

			// run it
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				return rs.getInt(1);
			}
		}
		catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the object " + object);
		}
		return -1;
	}
	
	/*
	 * -------------------------------- Relation -----------------------------------------------
	 */

	/*
	 * Actions with Relation:
	 * 1. Add Relation
	 * 2. Drop Relation
	 * 3. Get Relation File
	 * 4. Get Foreign keys
	 * 5. Get Primary keys
	 * 6. Get Attributes from relation
	 */
	public void addRelation(Relation rel) {

		try {

			// first, we'll add an object
			PreparedStatement ps0 = conn
					.prepareStatement("INSERT INTO DBObject VALUES(?, ?)");

			ps0.setString(1, rel.getName());
			if(rel.isTemporaryTable())
			{
				ps0.setInt(2, DataAccess.OBJ_VALUES_TABLE);
			}
			else
			{
				ps0.setInt(2, DataAccess.OBJ_RELATION);
			}
			ps0.executeUpdate();
			
			/*
			PreparedStatement ps_1 = conn.prepareStatement("SELECT * FROM DBObject");
			ResultSet resultSet = ps_1.executeQuery();
			while(resultSet.next())
			{
				System.out.println(resultSet.getString(1));
			}
			*/

			// then, we'll add the relation
			PreparedStatement ps1 = conn
					.prepareStatement("INSERT INTO Relation VALUES(?, ?, ?)");

			ps1.setString(1, rel.getName());
			ps1.setString(2, rel.getFileName());
			ps1.setLong(3, rel.getTupleNum());
			ps1.executeUpdate();

			// now, we'll add each attribute
			int i = 0;
			for (Attribute ax : rel.getAttributes()) {
				PreparedStatement ps2 = conn
						.prepareStatement("INSERT INTO RelationAttribute VALUES(?, ?, ?, ?, ?)");

				ps2.setString(1, rel.getName());
				ps2.setString(2, ax.getName());
				ps2.setInt(3, i);
				ps2.setString(4, ax.getType().writeOut());
				ps2.setLong(5, ax.getUniqueValue());
				ps2.executeUpdate();

				i++;
			}

			// then, the primary keys
			if(rel.getPrimaryKey() != null)
			{
				for (String sx : rel.getPrimaryKey()) {
					PreparedStatement ps3 = conn
							.prepareStatement("INSERT INTO PrimaryKey VALUES(?, ?)");

					ps3.setString(1, rel.getName());
					ps3.setString(2, sx);
					ps3.executeUpdate();
				}
			}
			

			if(rel.getForeignKeys() != null)
			{
				for (ForeignKey fx : rel.getForeignKeys()) {
					PreparedStatement ps4 = conn
							.prepareStatement("INSERT INTO ForeignKey VALUES(?, ?, ?, ?)");
	
					ps4.setString(1, fx.getSrcAtt().getRelName());
					ps4.setString(2, fx.getSrcAtt().getName());
					ps4.setString(3, fx.getDestAtt().getRelName());
					ps4.setString(4, fx.getDestAtt().getName());
					ps4.executeUpdate();
				}
			}

		} catch (Exception e) {
			System.err.println("Unable to add the new relation!");
			e.printStackTrace();
		}
	}

	/** Drops a relation */
	public void dropRelation(String relName) {

		try {

			// first, delete the foreign keys
			PreparedStatement ps1 = conn
					.prepareStatement("DELETE FROM ForeignKey "
							+ "WHERE relName = '" + relName + "'");

			ps1.executeUpdate();

			// then, the primary keys
			PreparedStatement ps2 = conn
					.prepareStatement("DELETE FROM PrimaryKey "
							+ "WHERE relName = '" + relName + "'");

			ps2.executeUpdate();

			// then, the attributes
			PreparedStatement ps3 = conn
					.prepareStatement("DELETE FROM RelationAttribute "
							+ "WHERE relName = '" + relName + "'");

			ps3.executeUpdate();

			// then, the relation itself
			PreparedStatement ps4 = conn
					.prepareStatement("DELETE FROM Relation "
							+ "WHERE relName = '" + relName + "'");

			ps4.executeUpdate();

			// then, the object
			PreparedStatement ps5 = conn
					.prepareStatement("DELETE FROM DBObject "
							+ "WHERE objName = '" + relName + "'");

			ps5.executeUpdate();

		} catch (Exception e) {
			System.err.println("Unable to drop the relation!");
			e.printStackTrace();
		}
	}

	/** Retrives all modulo relations **/

    public List<String> getAllModuloRelationNames(String relName){

        LinkedList<String> ret = new LinkedList<String>();

        try {

            // prepare the SQL statement
            PreparedStatement ps = conn
                    .prepareStatement("SELECT relName FROM Relation WHERE relName like ?");

            ps.setString(1, relName + "_mod_%_%");

            // run it
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                ret.add(rs.getString(1));
            }
        }
        catch (Exception e) {
            System.err.println("Unable to execute SQL query!");
            e.printStackTrace();
            System.err.println("Can't find a modulo relationships for relation name " + relName);
        }

        return ret;
    }

	/** Retrieves the file name of a given relation */
	public String getRelationFileName(String relName) {
		String out = "";

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT fileName FROM Relation "
							+ "WHERE relName = ?");

			ps.setString(1, relName);

			// run it
			ResultSet rs = ps.executeQuery();

			// fill the output structure
			out = rs.getString(1);

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the Relation[ " + relName + "]");
		}

		return (out);
	}
	
	/** Retrieves the file name of a given relation */
	public long getRelationTupleNumber(String relName) {
		long out  = -1;

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT tupleNum FROM Relation "
							+ "WHERE relName = ?");

			ps.setString(1, relName);

			// run it
			ResultSet rs = ps.executeQuery();

			// fill the output structure
			out = rs.getLong(1);

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the Relation[ " + relName + "]");
		}

		return (out);
	}
	
	/** Retrieves the foreign keys for a given relation */
	public ArrayList<ForeignKey> getForeignKeys(String relName) {
		ArrayList<ForeignKey> out = new ArrayList<ForeignKey>();

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT f.relName, f.attName, f.refRelName, f.refAttName, a.typeID "
							+ "FROM ForeignKey f, RelationAttribute a "
							+ "WHERE f.refRelName = a.relName "
							+ "AND   f.refAttName = a.attName "
							+ "AND   f.relName = ?");

			ps.setString(1, relName);

			// run it
			ResultSet rs = ps.executeQuery();

			// fill the output structure
			while (rs.next()) {
				Attribute src = new Attribute(rs.getString(2), TypeMachine.fromString(rs.getString(5)),
						rs.getString(1));
				Attribute dst = new Attribute(rs.getString(4), TypeMachine.fromString(rs.getString(5)),
						rs.getString(3));

				out.add(new ForeignKey(src, dst));
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
		}

		return (out);
	}
	
	/** Retrieves the foreign keys for a given relation */
	public ArrayList<String> getReferenceTables(String relName) {
		ArrayList<String> out = new ArrayList<String>();

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT f.refAttName"
							+ " FROM ForeignKey as f"
							+ " WHERE f.refRelName = ?");

			ps.setString(1, relName);

			// run it
			ResultSet rs = ps.executeQuery();

			// fill the output structure
			while (rs.next()) {
				out.add(rs.getString(1));
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
		}

		return (out);
	}
	
	/** Retrieves the primary key for a given relation */
	public ArrayList<String> getPrimaryKey(String relName) {
		ArrayList<String> out = new ArrayList<String>();

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT attName FROM PrimaryKey "
							+ "WHERE relName = ?");

			ps.setString(1, relName);

			// run it
			ResultSet rs = ps.executeQuery();

			// fill the output structure
			while (rs.next()) {
				out.add(rs.getString(1));
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
		}

		return (out);
	}

	/** Retrieves the attributes from a given relation */
	public ArrayList<Attribute> getAttsFromRelation(String relName) throws Exception{
		ArrayList<Attribute> out = new ArrayList<Attribute>();

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT attName, typeID, uniqueValue FROM RelationAttribute "
							+ "WHERE relName = ? " + "ORDER BY indexOf");

			// run it
			ps.setString(1, relName);
			ResultSet rs = ps.executeQuery();

			// create the attributes
			while (rs.next()) {
				Attribute att = new Attribute(rs.getString(1), TypeMachine.fromString(rs.getString(2)), relName, rs.getLong(3)
						);
				out.add(att);
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the Relation[ " + relName + "]");
			throw new Exception();
		}

		return (out);
	}
	
	/*
	 * -------------------------------- "Regular" (non-VG) function -----------------------------------------------
	 */
	
	/** Creates a new Function */
	public void addFunction(Function myFunc) {

		try {

			// first, we'll add an object
			PreparedStatement ps0 = conn.prepareStatement("INSERT INTO DBObject VALUES(?, ?)");

			ps0.setString(1, myFunc.getName());
			ps0.setInt(2, DataAccess.OBJ_GENERAL_FUNCTION);
			ps0.executeUpdate();

			// first, add the function
			PreparedStatement ps1 = conn.prepareStatement("INSERT INTO Function VALUES(?, ?)");

			ps1.setString(1, myFunc.getName());
			ps1.setString(2, myFunc.getOutputAtt ().getType().writeOut ());
			ps1.executeUpdate();

			// now, add all the input attributes
			int i = 0;
			for (Attribute a : myFunc.getInputAtts()) {
				PreparedStatement psI = conn.prepareStatement("INSERT INTO FunctionAttribute VALUES(?, ?, ?, ?, ?, ?)");
						
				psI.setString(1, myFunc.getName());
				psI.setString(2, a.getName());
				psI.setInt(3, i);
				psI.setString(4, a.getType().writeOut());
				psI.setString(5, a.getDomainType());
				psI.setString(6, a.getDomainSize());
				psI.executeUpdate();

				i++;
			}

		} catch (Exception e) {
			System.err.println("Unable to add the new Function!");
			e.printStackTrace();
		}
	}

        /** Retrieves the set of input attributes for a given "regular" function */
	public ArrayList<Attribute> getInputAttsFromFunction(String funcName)throws Exception {
		ArrayList<Attribute> out = new ArrayList<Attribute>();

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT attName, typeID FROM FunctionAttribute "
							+ "WHERE fName = ? "
							+ "ORDER BY indexOf");

			// run it
			ps.setString(1, funcName);
			ResultSet rs = ps.executeQuery();

			// create the attributes
			while (rs.next()) {
				Attribute att = new Attribute(rs.getString(1), TypeMachine.fromString(rs.getString(2)),
						funcName);
				out.add(att);
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the regular function[ " + funcName + "]");
			throw new Exception("getInputAttsFromVGFunction()");
		}

		return (out);
	}

	/** Retrieves the output attribute from a given "regular" function */
	public Attribute getOutputAttFromFunction(String funcName)throws Exception {

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT outputtype FROM function "
							+ "WHERE fName = ? ");

			// run it
			ps.setString(1, funcName);
			ResultSet rs = ps.executeQuery();

			// create the attributes
			if (rs.next()) {
				return new Attribute(funcName + "_output", TypeMachine.fromString(rs.getString(1)),
						funcName);
			} else {
				throw new RuntimeException ("Unable to find the regular function[ " + funcName + "]");
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the regular function[ " + funcName + "]");
			throw new Exception("getInputAttsFromVGFunction()");
		}

	}

	/*
	 * -------------------------------- VGFunction -----------------------------------------------
	 */
	
	/*
	 * Actions with VGFunction:
	 * 1. Add VGFunction
	 * 2. Drop VGFunction
	 * 3. Get VGFunction file name (removed by Chris; no longer needed)
	 * 4. Get input attributes from VGFunction
	 * 5. Get output attributes from VGFunction
	 */
	
	/** Creates a new VG Function */
	public void addVGFunction(VGFunction vgf) {

		try {

			// first, we'll add an object
			PreparedStatement ps0 = conn.prepareStatement("INSERT INTO DBObject VALUES(?, ?)");
					

			ps0.setString(1, vgf.getName());
			ps0.setInt(2, DataAccess.OBJ_VGFUNCTION);
			ps0.executeUpdate();

			// first, add the function
			PreparedStatement ps1 = conn.prepareStatement("INSERT INTO VGFunction VALUES(?, ?)");

			ps1.setString(1, vgf.getName());
			ps1.setInt(2, vgf.getBundlesPerTuple());
			ps1.executeUpdate();

			// now, add all the input attributes
			int i = 0;
			for (Attribute a : vgf.getInputAtts()) {
				PreparedStatement psI = conn.prepareStatement("INSERT INTO VGFunctionAttribute VALUES(?, ?, ?, ?, ?, ?, ?)");
						
				psI.setString(1, vgf.getName());
				psI.setString(2, a.getName());
				psI.setInt(3, i);
				psI.setString(4, "I");
				psI.setString(5, a.getType().writeOut());
				psI.setString(6, a.getDomainType());
				psI.setString(7, a.getDomainSize());
				psI.executeUpdate();

				i++;
			}
			i = 0;

			// now, add all the output attributes
			for (Attribute a : vgf.getOutputAtts()) {
				PreparedStatement psO = conn.prepareStatement("INSERT INTO VGFunctionAttribute VALUES(?, ?, ?, ?, ?, ?, ?)");
						

				psO.setString(1, vgf.getName());
				psO.setString(2, a.getName());
				psO.setInt(3, i);
				psO.setString(4, "O");
				psO.setString(5, a.getType().writeOut());
				psO.setString(6, a.getDomainType());
				psO.setString(7, a.getDomainSize());
				psO.executeUpdate();

				i++;
			}

		} catch (Exception e) {
			System.err.println("Unable to add the new VG Function!");
			e.printStackTrace();
		}
	}

	/** Drops a Function 
	 * @throws Exception */
	public void dropFunction(String funcName) throws Exception {

		try {

			// first, kill the attributes
			PreparedStatement ps1 = conn
					.prepareStatement("DELETE FROM FunctionAttribute WHERE fName = ?");

			ps1.setString(1, funcName);
			ps1.executeUpdate();

			// now, kill the function
			PreparedStatement ps2 = conn
					.prepareStatement("DELETE FROM Function WHERE fName = ?");

			ps2.setString(1, funcName);
			ps2.executeUpdate();

			// now, kill the object
			PreparedStatement ps3 = conn
					.prepareStatement("DELETE FROM DBObject WHERE objName = ?");

			ps3.setString(1, funcName);
			ps3.executeUpdate();

		} catch (Exception e) {
			System.err.println("Unable to remove the Function!");
			e.printStackTrace();
			System.err.println("Function [" + funcName + "] does not exist!");
			throw new Exception();
		}

	}
	/** Drops a VG Function 
	 * @throws Exception */
	public void dropVGFunction(String vgfName) throws Exception {

		try {

			// first, kill the attributes
			PreparedStatement ps1 = conn
					.prepareStatement("DELETE FROM VGFunctionAttribute WHERE vgfName = ?");

			ps1.setString(1, vgfName);
			ps1.executeUpdate();

			// now, kill the function
			PreparedStatement ps2 = conn
					.prepareStatement("DELETE FROM VGFunction WHERE vgfName = ?");

			ps2.setString(1, vgfName);
			ps2.executeUpdate();

			// now, kill the object
			PreparedStatement ps3 = conn
					.prepareStatement("DELETE FROM DBObject WHERE objName = ?");

			ps3.setString(1, vgfName);
			ps3.executeUpdate();

		} catch (Exception e) {
			System.err.println("Unable to remove the VG Function!");
			e.printStackTrace();
			System.err.println("Function [" + vgfName + "] does not exist!");
			throw new Exception();
		}

	}
	
	/** Retrieves the file name of a given VG Function */
	public int getTundlesPerTuple(String relName) throws Exception{
		String out = "";

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT bundlesPerTuple FROM VGFunction "
							+ "WHERE vgfName = ?");

			ps.setString(1, relName);

			// run it
			ResultSet rs = ps.executeQuery();

			// fill the output structure
			out = rs.getString(1);
			int result = new Integer(out.trim()).intValue();
			return result;

		} 
		catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the VGFunction[ " + relName + "]");
			throw new Exception("getTundlesPerTuple()");
		}
	}
	

	/** Retrieves the set of input attributes for a given VG Function */
	public ArrayList<Attribute> getInputAttsFromVGFunction(String relName)throws Exception {
		ArrayList<Attribute> out = new ArrayList<Attribute>();

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT attName, typeID FROM VGFunctionAttribute "
							+ "WHERE vgfName = ? "
							+ "AND inputOrOutput = 'I' "
							+ "ORDER BY indexOf");

			// run it
			ps.setString(1, relName);
			ResultSet rs = ps.executeQuery();

			// create the attributes
			while (rs.next()) {
				Attribute att = new Attribute(rs.getString(1), TypeMachine.fromString(rs.getString(2)),
						relName);
				out.add(att);
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the VGFunction[ " + relName + "]");
			throw new Exception("getInputAttsFromVGFunction()");
		}

		return (out);
	}

	/** Retrieves the set of output attributes for a given VG Function */
	public ArrayList<Attribute> getOutputAttsFromVGFunction(String relName) throws Exception{
		ArrayList<Attribute> out = new ArrayList<Attribute>();

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT attName, typeID, domainType, domainSize " 
							+ "FROM VGFunctionAttribute "
							+ "WHERE vgfName = ? "
							+ "AND inputOrOutput = 'O' "
							+ "ORDER BY indexOf");

			// run it
			ps.setString(1, relName);
			ResultSet rs = ps.executeQuery();

			// create the attributes
			while (rs.next()) {

				Attribute att = new Attribute(rs.getString(1), 
						TypeMachine.fromString(rs.getString(2)), 
						relName,
						rs.getString(3), 
						rs.getString(4));
				out.add(att);
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			
			System.err.println("Unable to find the VGFunction[ " + relName + "]");
			throw new Exception("getOutputAttsFromVGFunction()");
		}

		return (out);
	}
	
	/*
	 * -------------------------------- View -----------------------------------------------
	 */
	/*
	 * Actions with View:
	 * 1. Add View
	 * 2. Drop View
	 * 3. Get Attributes from View
	 */
	public void addView(View view) {

		try {
			
			// 1. we'll add an object (view)
			PreparedStatement ps0 = conn
					.prepareStatement("INSERT INTO DBObject VALUES(?, ?)");

			ps0.setString(1, view.getName());
			ps0.setInt(2, view.getType());
			ps0.executeUpdate();

			// 2. we'll add the view
			PreparedStatement ps1 = conn
					.prepareStatement("INSERT INTO View VALUES(?, ?)");

			ps1.setString(1, view.getName());
			ps1.setString(2, view.getSql());
			ps1.executeUpdate();

			// 3. we'll add each attribute
			int i = 0;
			for (Attribute attribute : view.getAttributes()) {
				PreparedStatement ps2 = conn
						.prepareStatement("INSERT INTO ViewAttribute VALUES(?, ?, ?, ?)");

				ps2.setString(1, view.getName());
				ps2.setString(2, attribute.getName());
				ps2.setInt(3, i);
				ps2.setString(4, attribute.getType().writeOut());
				ps2.executeUpdate();

				i++;
				
			}
		} 
		catch (Exception e) 
		{
			System.err.println("Unable to add the new relation!");
			e.printStackTrace();
		}
	}
	
	public void dropView(String viewName) {

		try {

			// 1. the attributes
			PreparedStatement ps3 = conn
					.prepareStatement("DELETE FROM ViewAttribute "
							+ "WHERE viewName = ?");

			ps3.setString(1, viewName);
			ps3.executeUpdate();

			// then, the relation itself
			PreparedStatement ps4 = conn
					.prepareStatement("DELETE FROM View "
							+ "WHERE viewName = ?");

			ps4.setString(1, viewName);
			ps4.executeUpdate();

			// then, the object
			PreparedStatement ps5 = conn
					.prepareStatement("DELETE FROM DBObject "
							+ "WHERE objName = ?");

			ps5.setString(1, viewName);
			ps5.executeUpdate();

		} catch (Exception e) {
			System.err.println("Unable to drop the relation!");
			e.printStackTrace();
		}
	}

	public String getSQL(String viewName) throws Exception
	{
		String sql = null;
		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT queryPlan FROM View "
							+ "WHERE viewName = ? ");

			// run it
			ps.setString(1, viewName);
			ResultSet rs = ps.executeQuery();

			// create the attributes
			if (rs.next()) {
				sql = rs.getString(1);
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the view[ " + viewName + "]");
			throw new Exception("getSQL");
		}

		return sql;
	}
	
	public ArrayList<Attribute> getAttsFromView(String viewName) throws Exception {
		ArrayList<Attribute> out = new ArrayList<Attribute>();

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT attName, typeID FROM ViewAttribute "
							+ "WHERE viewName = ? " + "ORDER BY indexOf");

			// run it
			ps.setString(1, viewName);
			ResultSet rs = ps.executeQuery();

			// create the attributes
			while (rs.next()) {
				Attribute att = new Attribute(rs.getString(1), TypeMachine.fromString(rs.getString(2)),
						viewName);
				out.add(att);
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the view[ " + viewName + "]");
			throw new Exception();
		}

		return out;
	}

	/**
	 * @param indexTableName
	 */
	public void dropIndexTable(String indexTableName) {
		try {

			// 1. the attributes
			PreparedStatement ps3 = conn
					.prepareStatement("DELETE FROM indextable "
							+ "WHERE indextablename = ?");

			ps3.setString(1, indexTableName);
			ps3.executeUpdate();
			
		} catch (Exception e) {
			System.err.println("Unable to drop the relation!");
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getIndexTable(String realTableName) throws Exception
	{
		ArrayList<String> out = new ArrayList<String>();

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn
					.prepareStatement("SELECT indextablename FROM indextable "
							+ "WHERE realtablename = ?");

			// run it
			ps.setString(1, realTableName);
			ResultSet rs = ps.executeQuery();

			// create the attributes
			while (rs.next()) {
				out.add(rs.getString(1));
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the view[ " + realTableName + "]");
			throw new Exception();
		}

		return out;
	}
	
	public void addIndexTable(String realTable, String indexTable) {

		try {
			
			// 1. we'll add an object (view)
			PreparedStatement ps0 = conn
					.prepareStatement("INSERT INTO indextable VALUES(?, ?)");

			ps0.setString(1, realTable);
			ps0.setString(2, indexTable);
			ps0.executeUpdate();

		} 
		catch (Exception e) 
		{
			System.err.println("Unable to add the new relation!");
			e.printStackTrace();
		}
	}
	
	/*
	 * adding for query interface 12/24/2013 mcdependency(viewname1, viewname2)
	 */
	public void addMCDependency(String view1, ArrayList<String> dependedList) {

		try {
			
			// 1. we'll add an object (view)
			PreparedStatement ps0 = conn.prepareStatement("INSERT INTO mcdependency VALUES(?, ?)");
			
			for(int i = 0; i < dependedList.size(); i++)
			{
				ps0.setString(1, view1);
				ps0.setString(2, dependedList.get(i));
				ps0.executeUpdate();
			}
		} 
		catch (Exception e) 
		{
			System.err.println("Unable to add the new relation!");
			e.printStackTrace();
		}
	}
	
	public void deleteMCDependecy(String view1) {

		try {
			// 1. we'll add an object (view)
			PreparedStatement ps0 = conn.prepareStatement("DELETE FROM mcdependency " + 
					" WHERE viewname1 = ?");
			
			ps0.setString(1, view1);
			ps0.executeUpdate();
		} 
		catch (Exception e) 
		{
			System.err.println("Unable delete the mc dependencies!");
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getMCDependedTables(String realTableName) throws Exception
	{
		ArrayList<String> out = new ArrayList<String>();

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn.prepareStatement("SELECT viewname2 FROM mcdependency "
							+ "WHERE viewname1 = ?");

			// run it
			ps.setString(1, realTableName);
			ResultSet rs = ps.executeQuery();

			// create the attributes
			while (rs.next()) {
				out.add(rs.getString(1));
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the depended random tables for random table [ " + realTableName + "]");
			throw new Exception();
		}

		return out;
	}
	
	public ArrayList<String> getMCForwardingTables(String realTableName) throws Exception
	{
		ArrayList<String> out = new ArrayList<String>();

		try {

			// prepare the SQL statement
			PreparedStatement ps = conn.prepareStatement("SELECT viewname1 FROM mcdependency "
							+ "WHERE viewname2 = ?");

			// run it
			ps.setString(1, realTableName);
			ResultSet rs = ps.executeQuery();

			// create the attributes
			while (rs.next()) {
				out.add(rs.getString(1));
			}

		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
			System.err.println("Unable to find the depended random tables for random table [ " + realTableName + "]");
			throw new Exception();
		}

		return out;
	}
	
	
	
	/*
     * -------------------------------- Regressors -----------------------------------------------
     */

    public int getLatestQuery() {
	int val = 0;
	try {
		PreparedStatement ps = conn.prepareStatement("SELECT MAX(QueryID) FROM QueryTime");
		ResultSet rs = ps.executeQuery();

		// get that value!
		while (rs.next()) {
			val = rs.getInt(1);
		}
       } catch (Exception e) {
		System.err.println("Unable to execute SQL query!");
		e.printStackTrace();
       }

	return(val);
    }

    public ArrayList<Integer> getAllQueries() {
       ArrayList<Integer> val = new ArrayList<Integer>();

       try {
		PreparedStatement ps = conn.prepareStatement("SELECT QueryID FROM QueryTime");
		ResultSet rs = ps.executeQuery();

		// get those values!
		while (rs.next()) {
			val.add(rs.getInt(1));
		}
       } catch (Exception e) {
		System.err.println("Unable to execute SQL query!");
		e.printStackTrace();
       }

       return(val);
    }

    public int getRunningTime(int queryID) {
	int val = 0;
	try {
		PreparedStatement ps = conn.prepareStatement("SELECT RunningTime FROM QueryTime WHERE QueryID = ?");
		ps.setInt(1, queryID);
		ResultSet rs = ps.executeQuery();

		// get that value!
		while (rs.next()) {
			val = rs.getInt(1);
		}
       } catch (Exception e) {
		System.err.println("Unable to execute SQL query!");
		e.printStackTrace();
       }

	return(val);
    }

	public ArrayList<Regressor> getRegressors(int queryID) {
		ArrayList<Regressor> val = new ArrayList<Regressor>();

		try {
			PreparedStatement ps = conn
					.prepareStatement("SELECT RegressorID, Input, Output FROM Regressor WHERE QueryID = ?");
			ps.setInt(1, queryID);
			ResultSet rs = ps.executeQuery();

			// get those values!
			while (rs.next()) {
				val.add(new Regressor(queryID, rs.getString(1),
						rs.getDouble(2), rs.getDouble(3)));
			}
		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
		}

		return (val);
	}

	public void addQueryInfo(int queryID, int runningTime, ArrayList<Regressor> regressors)
	{
		try {
			PreparedStatement ps = conn
					.prepareStatement("INSERT INTO QueryTime VALUES(?, ?)");
			ps.setInt(1, queryID);
			ps.setInt(2, runningTime);
			ps.executeUpdate();

			for (Regressor rx : regressors)
			{
				PreparedStatement ps2 = conn
						.prepareStatement("INSERT INTO Regressor VALUES(?, ?, ?, ?)");
				ps2.setInt(1, queryID);
				ps2.setString(2, rx.getRegressorName());
				ps2.setDouble(3, rx.getInput());
				ps2.setDouble(4, rx.getOutput());
				ps2.executeUpdate();
			}
		} catch (Exception e) {
			System.err.println("Unable to execute SQL query!");
			e.printStackTrace();
		}
	}

  // ---------- TABLE STATISTICS ---------- //
  public void updateStatistics(Relation rel) {
    
    // first, update the number of tuples.
    try {
      PreparedStatement ps = conn.prepareStatement
	("UPDATE Relation SET tuplenum = ? WHERE relname = ?");
      
      ps.setLong(1, rel.getTupleNum());
      ps.setString(2, rel.getName());
      ps.executeUpdate();
    } catch (Exception e) {
      System.err.println("(1) Unable to update relation statistics!");
      e.printStackTrace();
    }

    try {
      for (Attribute ax: rel.getAttributes()) {
	PreparedStatement ps = conn.prepareStatement
	  ("UPDATE RelationAttribute SET uniquevalue = ? WHERE relname = ? AND attname = ?");

	ps.setLong(1, ax.getUniqueValue());
	ps.setString(2, rel.getName());
	ps.setString(3, ax.getName());
	ps.executeUpdate();
      }

    } catch (Exception e) {
      System.err.println("(2) Unable to update relation statistics!");
      e.printStackTrace();
    }
  }
}
