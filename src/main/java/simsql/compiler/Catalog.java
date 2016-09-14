

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

import java.util.ArrayList;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;

import static simsql.compiler.MultidimensionalTableSchema.getIndicesFromQualifiedName;
import static simsql.compiler.MultidimensionalTableSchema.getTablePrefixFromQualifiedName;

/**
 * Encapsulates a single attribute, as a (name,type) pair. Also contains the
 * definition of all types.
 *
 * @author Luis, Bamboo
 */

public class Catalog implements simsql.shell.Catalog {

    /**
     * Our data access controller
     */
    private DataAccess ds;

    // nothing!
    public Catalog() {
    }

    // this attempts to get the catalog from a file
    public boolean getFromFile(File where) {
        try {

            // try to open up an existing catalog
            ds = new DataAccess(where, false);

            // everything was OK!
            return true;

        } catch (Exception e) {

            // this means that the guy didn't already exist
            return false;
        }
    }

    // from the simsql.shell.Catalog interface
    public void setUp(File where) {
        try {
            ds = new DataAccess(where, true);
            InitDB.create(ds);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error when trying to set up the new catalog!");
        }
    }

    public void save() {
        ds.close();
    }

    /**
     * Returns true if a given object exists
     */
    public int getObjectType(String object) {
        return ds.getObjectType(object);
    }

	/*
     * -------------------------------------
	 * Relation-------------------------------------------
	 */

    /**
     * Returns all the information about a given Relation
     */
    public Relation getRelation(String relName) {

        // =======================================
        relName = relName.toLowerCase();
        // =======================================

        // we'll return null if not found.
        Relation out = null;

        if (getObjectType(relName) == DataAccess.OBJ_RELATION
                || getObjectType(relName) == DataAccess.OBJ_VALUES_TABLE) {
            try {
                out = new Relation(relName, ds.getRelationFileName(relName),
                        ds.getAttsFromRelation(relName),
                        ds.getRelationTupleNumber(relName),
                        ds.getPrimaryKey(relName), ds.getForeignKeys(relName));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("error in catalog!");
            }
        }

        return (out);
    }

    public boolean hasReferencingTable(String relName) {
        // =======================================
        relName = relName.toLowerCase();
        // =======================================

        if (getObjectType(relName) == DataAccess.OBJ_RELATION
                || getObjectType(relName) == DataAccess.OBJ_VALUES_TABLE) {
            if (ds.getReferenceTables(relName).size() >= 1) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    /**
     * Add a new relation to the catalog
     *
     * @throws Exception
     */
    public void addRelation(Relation rel) {

        // =======================================
        rel.setName(rel.getName().toLowerCase());
        // =======================================

        if (getObjectType(rel.getName()) != DataAccess.OBJ_RELATION) {
            if (checkAttributeNameList(rel.getAttributes())) {
                ds.addRelation(rel);
            }
        } else {
            System.err.println("An object called " + rel.getName()
                    + " already exists in the catalog!");
            throw new RuntimeException("error in catalog!");
        }
    }

    /**
     * Remove a relation from the catalog
     */
    public void dropRelation(String relationName) {
        if (getObjectType(relationName) == DataAccess.OBJ_RELATION) {
            ds.dropRelation(relationName);
        } else {
            System.err.println("Relation " + relationName + " doesn't exist!");
        }
    }

    /**
     * clean the ValuesTable
     */
    public void cleanValuesTable() throws Exception {
        ArrayList<String> nameList;
        try {
            nameList = ds.getValuesTableNames();

            for (int i = 0; i < nameList.size(); i++) {
                dropValueTable(nameList.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("error in catalog!");
        }
    }

    /**
     * Remove a ValueTable from the catalog
     */
    public void dropValueTable(String relationName) {
        if (getObjectType(relationName) == DataAccess.OBJ_VALUES_TABLE) {
            ds.dropRelation(relationName);
        } else {
            System.err.println("Relation " + relationName + " doesn't exist!");
        }
    }

    public void dropIndexTable(String indexTableName) {
        ds.dropIndexTable(indexTableName);
    }

    public ArrayList<String> getIndexTableList(String realTableName)
            throws Exception {
        return ds.getIndexTable(realTableName);
    }

    public void addIndexTable(String realTable, String indexTable) {
        ds.addIndexTable(realTable, indexTable);
    }

    public ArrayList<String> getAllObjectsOfType(int whichType) {

        ArrayList<String> ret = new ArrayList<String>();

        // get all objects
        try {
            for (String s : ds.getObjectNames()) {
                if (getObjectType(s) == whichType)
                    ret.add(s);
            }
        } catch (Exception e) {
        }

        return ret;
    }

	/*
	 * ------------------------------------- "Regular" (non-VG) Function
	 * -----------------------------------------
	 */

    /**
     * Returns all the information about a given Function
     */
    public Function getFunction(String fName) throws Exception {

        fName = fName.toLowerCase();

        // we'll return null if not found
        Function out = null;

        if (getObjectType(fName) == DataAccess.OBJ_GENERAL_FUNCTION) {
            try {
                out = new Function(fName, ds.getInputAttsFromFunction(fName),
                        ds.getOutputAttFromFunction(fName));
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("error in catalog!");
            }
        }

        return (out);
    }

    /**
     * Add a new function to the catalog
     *
     * @throws Exception
     */
    public void addFunction(Function myFunc) throws Exception {

        if (getObjectType(myFunc.getName()) != DataAccess.OBJ_GENERAL_FUNCTION) {
            ds.addFunction(myFunc);
        } else {
            System.err.println("An object called " + myFunc.getName()
                    + " already exists in the catalog!");
            throw new Exception("Catalog error!");
        }
    }

    /**
     * Remove a VGF from the catalog
     */
    public void dropFunction(String funcName) throws Exception {
        try {
            if (getObjectType(funcName) == DataAccess.OBJ_GENERAL_FUNCTION) {
                ds.dropFunction(funcName);
            } else {
                System.err.println("Function " + funcName + " doesn't exist!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("error in catalog!");
        }
    }

	/*
	 * ------------------------------------- VGFunction
	 * -------------------------------------------
	 */

    /**
     * Returns all the information about a given VG Function
     */
    public VGFunction getVGFunction(String vgfName) throws Exception {

        // =======================================
        vgfName = vgfName.toLowerCase();
        // =======================================

        // we'll return null if not found
        VGFunction out = null;

        if (getObjectType(vgfName) == DataAccess.OBJ_VGFUNCTION) {
            try {
                out = new VGFunction(vgfName, ds.getTundlesPerTuple(vgfName),
                        ds.getInputAttsFromVGFunction(vgfName),
                        ds.getOutputAttsFromVGFunction(vgfName));
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("error in catalog!");
            }
        }

        return (out);
    }

    /**
     * Add a new VGF to the catalog
     *
     * @throws Exception
     */
    public void addVGFunction(VGFunction vgf) throws Exception {

        if (getObjectType(vgf.getName()) != DataAccess.OBJ_VGFUNCTION) {
            ds.addVGFunction(vgf);
        } else {
            System.err.println("An object called " + vgf.getName()
                    + " already exists in the catalog!");
            throw new Exception("Catalog error!");
        }
    }

    /**
     * Remove a VGF from the catalog
     */
    public void dropVGFunction(String vgfName) throws Exception {
        try {
            if (getObjectType(vgfName) == DataAccess.OBJ_VGFUNCTION) {
                ds.dropVGFunction(vgfName);
            } else {
                System.err
                        .println("VG Function " + vgfName + " doesn't exist!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("error in catalog!");
        }
    }

	/*
	 * ------------------------------------------------ View
	 * ------------------------------------------
	 */

    /**
     * Returns all the information about a given VG Function
     */
    public View getView(String viewName) throws RuntimeException {
        // =======================================
        viewName = viewName.toLowerCase();
        // =======================================

        // we'll return null if not found
        View view = null;

        try {

            int objectType = getObjectType(viewName);

            if (objectType == DataAccess.OBJ_VIEW || objectType == DataAccess.OBJ_RANDRELATION ||
                    objectType == DataAccess.OBJ_UNION_VIEW || objectType == DataAccess.OBJ_MULRELATION) {
                view = new View(viewName, ds.getSQL(viewName), ds.getAttsFromView(viewName), objectType);
            } else if (viewName.matches("^[^_]+(_[a-z])+$")) {
                String tempViewName = viewName.substring(0, viewName.indexOf("_"));

                ArrayList<String> indexTableNameList = ds.getIndexTable(tempViewName);
                if (indexTableNameList.size() != 0) {
                    Collections.sort(indexTableNameList);
                    viewName = indexTableNameList.get(indexTableNameList.size() - 1);
                    view = new View(viewName, ds.getSQL(viewName), ds.getAttsFromView(viewName), getObjectType(viewName));
                }
            } else if (viewName.matches("^[^_]+(_[0-9]+)+$")) {
				/*
				 * If the viewname could not be found and it's format matches a constant index table, then consider the general
				 * indexTable.
				 */

                HashMap<String, Integer> indices = getIndicesFromQualifiedName(viewName);

                String tempName = getTablePrefixFromQualifiedName(viewName);
                ArrayList<String> indexTableNameList = ds.getIndexTable(tempName);

                if (indices.size() == 1 && indexTableNameList.size() != 0 && indexTableNameList.contains(tempName + "_i")) {
                    viewName = tempName + "_i";
                    view = new View(viewName, ds.getSQL(viewName), ds.getAttsFromView(viewName), getObjectType(viewName));
                } else if (indices.size() != 0 && indexTableNameList.size() != 0) {
                    for (String vn : indexTableNameList) {
                        MultidimensionalSchemaIndices ids = new MultidimensionalSchemaIndices(vn);
                        if (ids.areIndicesForThisTable(indices)) {
                            view = new View(vn, ds.getSQL(vn), ds.getAttsFromView(vn), getObjectType(vn));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error in catalog!");
        }

        return view;
    }

    /**
     * Returns all the information about a given VG Function
     */
    public View getSchemaView(String viewName) throws Exception {

        // =======================================
        viewName = viewName.toLowerCase();
        // =======================================

        // we'll return null if not found
        View view = null;

        try {
            int objectType = getObjectType(viewName);

            if (objectType == DataAccess.OBJ_VIEW || objectType == DataAccess.OBJ_RANDRELATION ||
                    objectType == DataAccess.OBJ_UNION_VIEW || objectType == DataAccess.OBJ_MULRELATION) {

                view = new View(viewName, ds.getSQL(viewName), ds.getAttsFromView(viewName), objectType);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("error in catalog!");
        }

        return view;
    }

    /**
     * Add a new view to the catalog
     *
     * @throws Exception
     */
    public void addView(View view) throws Exception {
        int type = getObjectType(view.getName());

        if (type != DataAccess.OBJ_VIEW && type != DataAccess.OBJ_RANDRELATION &&
                type != DataAccess.OBJ_UNION_VIEW && type != DataAccess.OBJ_MULRELATION) {
            if (checkAttributeNameList(view.getAttributes())) {
                ds.addView(view);
            }
        } else {
            System.err.println("An object called " + view.getName()
                    + " already exists in the catalog!");
            throw new Exception("Catalog error!");
        }
    }

    /**
     * Remove a view from the catalog
     */
    public void dropView(String viewName) {
        if (getObjectType(viewName) == DataAccess.OBJ_VIEW
                || getObjectType(viewName) == DataAccess.OBJ_RANDRELATION
                || getObjectType(viewName) == DataAccess.OBJ_MULRELATION
                || getObjectType(viewName) == DataAccess.OBJ_UNION_VIEW) {
            ds.dropView(viewName);
        } else {
            System.err.println("view " + viewName + " doesn't exist!");
        }
    }

    /*
     * Support the dependency of random tables.
     */
    public void addMCDependecy(String view1, ArrayList<String> dependedList) {
        ds.addMCDependency(view1, dependedList);
    }

    public void deleteMCDependecy(String view1) {
        ds.deleteMCDependecy(view1);
    }

    public ArrayList<String> getMCDependedTables(String realTableName) throws Exception {
        return ds.getMCDependedTables(realTableName);
    }

    public ArrayList<String> getMCForwardingTables(String realTableName) throws Exception {
        return ds.getMCForwardingTables(realTableName);
    }

    public String getMonteCarloIterations() {
        return ds.getConfigObjectName("MCI");
    }

    public String getDefaultSelectivity() {
        return ds.getConfigObjectName("dfltSel");
    }

    public String getIsPresentSize() {
        return ds.getConfigObjectName("isPresSize");
    }

    public String getSeedSize() {
        return ds.getConfigObjectName("seedSize");
    }

    public String getVGFunctionThreads() {
        return ds.getConfigObjectName("vgThreads");
    }

    public String getVGTuples() {
        return ds.getConfigObjectName("vgTuples");
    }

    public String getPipeSize() {
        return ds.getConfigObjectName("vgPipeSize");
    }

    public String getSelectionCostWeight() {
        return ds.getConfigObjectName("selectionCostWeight");
    }

    public String getTablescanCostWeight() {
        return ds.getConfigObjectName("tablescanCostWeight");
    }

    public String getSeedCostWeight() {
        return ds.getConfigObjectName("seedCostWeight");
    }

    public String getSplitCostWeight() {
        return ds.getConfigObjectName("splitCostWeight");
    }

    public String getJoinCostWeight() {
        return ds.getConfigObjectName("joinCostWeight");
    }

    public String getAntijoinCostWeight() {
        return ds.getConfigObjectName("antijoinCostWeight");
    }

    public String getSemijoinCostWeight() {
        return ds.getConfigObjectName("semijoinCostWeight");
    }

    public String getDedupCostWeight() {
        return ds.getConfigObjectName("dedupCostWeight");
    }

    public String getProjectionCostWeight() {
        return ds.getConfigObjectName("projectionCostWeight");
    }

    public String getGenaggCostWeight() {
        return ds.getConfigObjectName("genaggCostWeight");
    }

    public String getVgWrapperCostWeight() {
        return ds.getConfigObjectName("vgwrapperCostWeight");
    }

    public String getScalarCostWeight() {
        return ds.getConfigObjectName("scalarfuncCostWeight");
    }

    public String getDataTypeSize(String dataType) {
        return ds.getConfigObjectName(dataType);
    }

    public int getLatestQuery() {
        return (ds.getLatestQuery());
    }

    public ArrayList<Integer> getAllQueries() {
        return (ds.getAllQueries());
    }

    public int getRunningTime(int queryID) {
        return (ds.getRunningTime(queryID));
    }

    public ArrayList<Regressor> getRegressors(int queryID) {
        return (ds.getRegressors(queryID));
    }

    public void addQueryInfo(int queryID, int runningTime,
                             ArrayList<Regressor> regressors) {
        ds.addQueryInfo(queryID, runningTime, regressors);
    }

    public void listTables() {

        System.out.format("%-50s%n", "table name");
        System.out.format("%s", new String(new char[50]).replace("\0", "-"));
        System.out.format("%n");

        for (String s : getAllObjectsOfType(DataAccess.OBJ_RELATION)) {

            // display a relation name that looks like rel_12_saved as rel[12]
            if (s.endsWith("_saved")) {
                s = MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(s.substring(0, s.length() - 6));
            }
            System.out.format("%-50s%n", s);
        }

        for (String s : getAllObjectsOfType(DataAccess.OBJ_RANDRELATION)) {
            if (s.endsWith("_i") || s.endsWith("_0")) {
                String[] parts = s.split("_");
                String newString = parts[0];
                for (int i = 1; i < parts.length - 1; i++) {
                    newString += "_" + parts[i];
                }
                s = newString + "[" + parts[parts.length - 1] + "]";
            }
            System.out.format("%-50s%n", s + "*");
        }

        for (String s : getAllObjectsOfType(DataAccess.OBJ_MULRELATION)) {

            // display a multidimensional relation if it looks like mmd_2to_2_3to4
            if (s.matches("^[^_]+(_[0-9]+to[0-9]+|_[0-9]+to|_[0-9]+)+$")) {
                String prefix = MultidimensionalTableSchema.getTablePrefixFromGeneralName(s);
                MultidimensionalSchemaIndices indices = new MultidimensionalSchemaIndices(s);
                s = prefix + indices.getPresentationSuffix() + "*";
            }

            System.out.format("%-50s%n", s);
        }

        System.out.format("%n");
    }

    public void listViews() {
        System.out.format("%-50s%n", "view name");
        System.out.format("%s", new String(new char[50]).replace("\0", "-"));
        System.out.format("%n");

        for (String s : getAllObjectsOfType(DataAccess.OBJ_VIEW)) {
            System.out.format("%-50s%n", s);
        }

        System.out.format("%n");
    }

    // table stats...
    public void updateTableStatistics(Relation r, simsql.shell.TableStatistics s) {

        // avoid weird stuff.
        if (r.getAttributes().size() != s.numAttributes())
            return;

        // set the number of tuples
        r.setTupleNum(s.numTuples());

        // set the uniques for each attribute.
        for (int i = 0; i < s.numAttributes(); i++) {
            r.getAttributes().get(i).setUniqueValue(s.numUniques(i));
        }

        // update in the database.
        ds.updateStatistics(r);
    }

    public void updateTableStatistics(Relation r, Relation s) {

        // avoid weird stuff.
        if (r.getAttributes().size() != s.getAttributes().size())
            return;

        // set the number of tuples
        r.setTupleNum(s.getTupleNum());

        // set the uniques for each attribute.
        for (int i = 0; i < s.getAttributes().size(); i++) {
            r.getAttributes().get(i).setUniqueValue(s.getAttributes().get(i).getUniqueValue());
        }

        // update in the database.
        ds.updateStatistics(r);
    }

    private boolean checkAttributeNameList(ArrayList<Attribute> attributeList) {
        for (int i = 0; i < attributeList.size(); i++) {
            //this predicate is to filter out SQL like
            //create view D as select A.B from A; Otherwise, we have D.A.B.
            if (!isIdentifier(attributeList.get(i).getName())) {
                throw new RuntimeException("The attribute name [" + attributeList.get(i).getFullName() + "] is not valid!");
            }
        }

        return true;
    }

    public boolean isIdentifier(String s) {
        if (s == null || s.length() == 0) {
            return false;
        } else {
            char firstLetter = s.charAt(0);
            if (!(firstLetter >= 'a' && firstLetter <= 'z' ||
                    firstLetter >= 'A' && firstLetter <= 'Z' ||
                    firstLetter == '_')) {
                return false;
            } else {
                for (int i = 1; i < s.length(); i++) {
                    char letter = s.charAt(i);
                    if (!(letter >= 'a' && letter <= 'z' ||
                            letter >= 'A' && letter <= 'Z' ||
                            letter >= '0' && letter <= '9' ||
                            letter == '_')) {
                        return false;
                    }
                }

                return true;
            }
        }
    }
}
