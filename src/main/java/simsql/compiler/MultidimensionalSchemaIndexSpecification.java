package simsql.compiler;


public class MultidimensionalSchemaIndexSpecification {

    Integer upperLimit;
    Integer lowerLimit;

    Boolean unconstrained;

    public MultidimensionalSchemaIndexSpecification(String upperLimit, String lowerLimit) {
        this.upperLimit = Integer.parseInt(upperLimit);
        this.lowerLimit = Integer.parseInt(lowerLimit);
        this.unconstrained = false;
    }

    public MultidimensionalSchemaIndexSpecification(String lowerLimit, Boolean unconstrained) {
        this.upperLimit = null;
        this.lowerLimit = Integer.parseInt(lowerLimit);
        this.unconstrained = unconstrained;
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
}
