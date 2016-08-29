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
        this.lowerLimit = Integer.parseInt(lowerLimit);
        this.unconstrained = unconstrained;
    }
}
