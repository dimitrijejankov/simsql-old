package simsql.compiler;


public class MultidimensionalSchemaIndexSpecification {

    Integer upperLimit;
    Integer lowerLimit;

    Boolean unconstrained;

    public MultidimensionalSchemaIndexSpecification(String lowerLimit, String upperLimit) {
        this.upperLimit = Integer.parseInt(upperLimit);
        this.lowerLimit = Integer.parseInt(lowerLimit);
        this.unconstrained = false;
    }

    public MultidimensionalSchemaIndexSpecification(String lowerLimit, Boolean unconstrained) {
        this.upperLimit = null;
        this.lowerLimit = Integer.parseInt(lowerLimit);
        this.unconstrained = unconstrained;
    }

    /**
     * Parses a MultidimensionalSchemaIndexSpecification from string.
     * @param indexString the string format should look like the following examples : 1 1to 1to2
     */
    public MultidimensionalSchemaIndexSpecification(String indexString) {

        String[] parts = indexString.split("to");

        if(parts.length == 2)
        {
            lowerLimit = Integer.parseInt(parts[0]);
            upperLimit = Integer.parseInt(parts[1]);
        }
        else if(parts.length == 1){
            lowerLimit = Integer.parseInt(parts[0]);
            this.unconstrained = indexString.endsWith("to");
        }
        else {
            throw new RuntimeException("MultidimensionalSchemaIndexSpecification: Wrong index format.");
        }
    }

    String getStringValue(){
        if(unconstrained){
            return "_" + lowerLimit + "to";
        }

        if(upperLimit != null){
            return "_" + lowerLimit + "to" + upperLimit;
        }

        return "_" + lowerLimit;
    }

    public boolean checkRange(Integer value) {
        if(upperLimit != null) {
            return value <= upperLimit && value >= lowerLimit;
        }
        else if(unconstrained) {
            return value >= lowerLimit;
        }

        return value.equals(lowerLimit);
    }
}
