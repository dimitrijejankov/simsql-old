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

package simsql.compiler.operators;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import simsql.compiler.CopyHelper;
import simsql.compiler.TranslatorHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * The class that represents the Operator in the logical query plan
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "class-name")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Aggregate.class, name = "aggregate"),
        @JsonSubTypes.Type(value = DuplicateRemove.class, name = "duplicate-remove"),
        @JsonSubTypes.Type(value = FrameOutput.class, name = "frame-output"),
        @JsonSubTypes.Type(value = Join.class, name = "join"),
        @JsonSubTypes.Type(value = Projection.class, name = "projection"),
        @JsonSubTypes.Type(value = ScalarFunction.class, name = "scalar-function"),
        @JsonSubTypes.Type(value = Seed.class, name = "seed"),
        @JsonSubTypes.Type(value = Selection.class, name = "selection"),
        @JsonSubTypes.Type(value = TableScan.class, name = "table-scan"),
        @JsonSubTypes.Type(value = VGWrapper.class, name = "vg-wrapper")
})
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public abstract class Operator {

    /**
     * the name of the node in the logical query plan
     */
    @JsonProperty("node-name")
    private String nodeName;

    /**
     * the children of the operators in the logical query plan
     */
    @JsonProperty("children")
    private ArrayList<Operator> children;

    /**
     * the parents of the operators in the logical query plan
     */
    @JsonProperty("parents")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
    private ArrayList<Operator> parents;

    /**
     * used to replace the names of the attributes
     */
    @JsonProperty("name-map")
    private HashMap<String, String> nameMap;

    /**
     * contains all the mapped attributes
     */
    @JsonProperty("map-space-name-set")
    private HashSet<String> mapSpaceNameSet;

    /**
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents  the parent operators
     */
    public Operator(String nodeName, ArrayList<Operator> children, ArrayList<Operator> parents) {
        this.nodeName = nodeName;
        this.children = children;
        this.parents = parents;

        // Initialization of nameMap
        nameMap = new HashMap<String, String>();
        mapSpaceNameSet = new HashSet<String>();
    }


    /**
     * @param nodeName        the name of the operator
     * @param children        the children of the operator
     * @param parents         the parent operators
     * @param nameMap         the name map to be used
     * @param mapSpaceNameSet the map space name set to be used...
     */
    public Operator(String nodeName, ArrayList<Operator> children, ArrayList<Operator> parents, HashMap<String, String> nameMap, HashSet<String> mapSpaceNameSet) {
        super();
        this.nodeName = nodeName;
        this.children = children;
        this.parents = parents;
        this.nameMap = nameMap;
        this.mapSpaceNameSet = mapSpaceNameSet;
    }

    /**
     * @return gets the node name
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @param nodeName sets the node name
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * @return returns the list of children associated with this node
     */
    public ArrayList<Operator> getChildren() {
        return children;
    }

    /**
     * @param children sets the list of children associated with this node
     */
    public void setChildren(ArrayList<Operator> children) {
        this.children = children;
    }

    /**
     * @return gets the list of parents associated with this node
     */
    public ArrayList<Operator> getParents() {
        return parents;
    }

    /**
     * @param parents sets the list of parents associated with this node
     */
    public void setParents(ArrayList<Operator> parents) {
        this.parents = parents;
    }

    /**
     * @param operator adds a new parent
     */
    public void addParent(Operator operator) {
        parents.add(operator);
    }

    /**
     * Adds a new child at a certain position
     *
     * @param index    the position of child in the list of children
     * @param operator the child operator
     */
    public void addChild(int index, Operator operator) {
        children.add(index, operator);
    }


    /**
     * Replaces a child operator in the child list with the given operator
     *
     * @param op1 the child operator to be replaced
     * @param op2 the new child operator
     */
    public void replaceChild(Operator op1, Operator op2) {
        int index = children.indexOf(op1);
        if (index >= 0) {
            children.remove(op1);
            children.add(index, op2);
        } else {
            children.add(op2);
        }
    }

    /**
     * Adds a child operator to the children list
     *
     * @param operator the new operator
     */
    public void addChild(Operator operator) {
        children.add(operator);
    }

    /**
     * remove a parent from the list of parent operators
     *
     * @param operator the parent operator to be removed
     */
    public void removeParent(Operator operator) {
        parents.remove(operator);
    }

    /**
     * remove a child from the child operator list
     *
     * @param operator the child to be removed
     */
    public void removeChild(Operator operator) {
        children.remove(operator);
    }

    /**
     * removes all the parents and children from this node
     */
    public void clearLinks() {
        parents.clear();
        children.clear();
    }

    /**
     * @return the string that has the node structure
     */
    @JsonIgnore
    public String getNodeStructureString() {
        String result = "";
        ArrayList<Operator> temp = getChildren();
        if (temp != null) {
            for (Operator aTemp : temp) {
                result += "parent(" + getNodeName() + ", " + aTemp.getNodeName() + ").\r\n";
            }
        }
        return result;
    }

    /**
     * converts the list of strings to a string in the form of "[s1, s2, ...]" where s1, s2 are elements of the list
     *
     * @param list the list to convert
     * @return a string in the described form
     */
    String getListString(ArrayList<String> list) {
        String result = "[";
        for (int i = 0; i < list.size(); i++) {
            result += list.get(i);

            if (i != list.size() - 1) {
                result += ", ";
            }
        }

        result += "]";
        return result;
    }

    /**
     * Checks whether an attribute is mapped
     *
     * @param s the name of the attribute we are testing
     * @return true if the attribute is mapped
     */
    public boolean isMapped(String s) {
        return nameMap.containsKey(s);
    }

    /**
     * puts a attribute in the nameMap and mapSpaceNameSet
     *
     * @param s1 the attribute to be renamed
     * @param s2 the new name of the attribute
     */
    public void putNameMap(String s1, String s2) {
        nameMap.put(s1, s2);
        mapSpaceNameSet.add(s2);
    }

    /**
     * checks whether an attribute is in the mapSpaceNameSet
     *
     * @param s2 the name of the attribute
     * @return returns true if its in the map space name set
     */
    public boolean isSpaceNamein(String s2) {
        return mapSpaceNameSet.contains(s2);
    }

    /**
     * returns a reference to the name map
     *
     * @return reference to the name map
     */
    public HashMap<String, String> getNameMap() {
        return nameMap;
    }

    /**
     * sets the name map to ne input value
     *
     * @param nameMap the new nameMap
     */
    public void setNameMap(HashMap<String, String> nameMap) {
        this.nameMap = nameMap;
    }

    /**
     * returns a reference to the mapSpaceNameSet
     *
     * @return reference to the mapSpaceNameSet
     */
    public HashSet<String> getMapSpaceNameSet() {
        return mapSpaceNameSet;
    }

    /**
     * sets a new mapSpaceNameSet to the input value
     *
     * @param mapSpaceNameSet the new mapSpaceNameSet
     */
    public void setMapSpaceNameSet(HashSet<String> mapSpaceNameSet) {
        this.mapSpaceNameSet = mapSpaceNameSet;
    }

    /**
     * returns the node as a string...
     *
     * @return the returned string
     */
    public String toString() {
        String result;
        try {
            result = nodeName + ".\r\n" + visitNode();
        } catch (Exception e) {
            result = nodeName;
        }

        return result;
    }

    /**
     * clears all the attribute renaming info
     */
    public void clearRenamingInfo() {
        clearNameMap();
        clearMapSpaceNameSet();
    }

    /**
     * clears just the name map
     */
    private void clearNameMap() {
        nameMap.clear();
    }

    /**
     * clears the map space name set
     */
    private void clearMapSpaceNameSet() {
        mapSpaceNameSet.clear();
    }

    /**
     * @return returns the string file representation of this operator
     */
    public abstract String visitNode() throws Exception;


    /**
     * Changes the node based on the type and the provided indices. Basically it evaluates the predicates,
     * arithmetic expressions and sets the names of some operators and updates the relation statistics
     * @param indices
     * @param translatorHelper
     */
    public void changeNodeProperty(HashMap<String, Integer> indices, TranslatorHelper translatorHelper) {
        // sets a new name for the node
        setNodeName("node_" + translatorHelper.getInstantiateNodeIndex());
    }

    /**
     * @param copyHelper an instance of the copy helper class
     * @return the deep copy of an operator
     * @throws Exception if the operation fails
     */
    public abstract Operator copy(CopyHelper copyHelper) throws Exception;

}
