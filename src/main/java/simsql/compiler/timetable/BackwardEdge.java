package simsql.compiler.timetable;

import simsql.compiler.MultidimensionalTableSchema;

/**
 * This class is used to encapsulate the information in a backward edge
 */
public class BackwardEdge {

    /**
     * The qualified name of the edge
     */
    private String qualifiedName;

    /**
     * The brackets name of the edge
     */
    private String bracketsName;

    /**
     * The prefix of the table
     */
    private String prefix;

    BackwardEdge(String bracketsName) {

        // set the brackets name
        this.bracketsName = bracketsName;

        // set the qualified name
        this.qualifiedName = MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(bracketsName);

        // set the prefix
        this.prefix = MultidimensionalTableSchema.getPrefixFromBracketsTableName(bracketsName);
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getBracketsName() {
        return bracketsName;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public int hashCode() {
        return bracketsName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return bracketsName.equals(o);
    }
}
