package simsql.compiler.timetable;

import simsql.compiler.operators.Operator;
import simsql.compiler.operators.Operator.OperatorType;

import java.util.HashMap;

public class CostModel {

    private HashMap<Long, Double> costs;

    public CostModel() {
        costs = new HashMap<>();

        // we don't want to remove a projection or selection from a join
        addRule(OperatorType.SELECTION, OperatorType.JOIN, 0.9);
        addRule(OperatorType.PROJECTION, OperatorType.JOIN, 0.8);

        // cutting after a selection is generally bad
        addRule(OperatorType.SELECTION, OperatorType.SELECTION, 1);
        addRule(OperatorType.SELECTION, OperatorType.SCALAR_FUNCTION, 0.8);
        addRule(OperatorType.SELECTION, OperatorType.PROJECTION, 0.8);

	// added by Shangyu from statistics
        addRule(OperatorType.SELECTION, OperatorType.VG_WRAPPER, 0);

        // if there is an aggregate after most operators this is usually a bad choice for cut
        addRule(OperatorType.SELECTION, OperatorType.AGGREGATE, 1);
        addRule(OperatorType.SCALAR_FUNCTION, OperatorType.AGGREGATE, 1);
        addRule(OperatorType.PROJECTION, OperatorType.AGGREGATE, 1);
        addRule(OperatorType.JOIN, OperatorType.AGGREGATE, 1);
        addRule(OperatorType.DUPLICATE_REMOVE, OperatorType.AGGREGATE, 1);


        addRule(OperatorType.SCALAR_FUNCTION, OperatorType.SELECTION, 1);
        addRule(OperatorType.PROJECTION, OperatorType.SELECTION, 1);
        addRule(OperatorType.JOIN, OperatorType.SELECTION, 1);
        addRule(OperatorType.DUPLICATE_REMOVE, OperatorType.SELECTION, 1);


	// added by Shangyu from statistics
	addRule(OperatorType.JOIN, OperatorType.VG_WRAPPER, 0.9);
	addRule(OperatorType.VG_WRAPPER, OperatorType.SELECTION, 1);

        // if we want to cut after an aggregate the cost should be 0
        addRule(OperatorType.AGGREGATE, OperatorType.SELECTION, 0);
        addRule(OperatorType.AGGREGATE, OperatorType.SCALAR_FUNCTION, 0);
        addRule(OperatorType.AGGREGATE, OperatorType.PROJECTION, 0);
        addRule(OperatorType.AGGREGATE, OperatorType.JOIN, 0);
        addRule(OperatorType.AGGREGATE, OperatorType.DUPLICATE_REMOVE, 0);
        addRule(OperatorType.AGGREGATE, OperatorType.VG_WRAPPER, 0);
        addRule(OperatorType.AGGREGATE, OperatorType.UNION_VIEW, 0);

        // if we have a join after a join we might not want to cut after this might be piplined
        addRule(OperatorType.JOIN, OperatorType.JOIN, 0.95);

	// added by Shangyu from statistics
        addRule(OperatorType.JOIN, OperatorType.SELECTION, 1);
    }

    public double getCostFor(Operator child, Operator parent) {
        return costs.getOrDefault(combinedTypeCode(getTypeCodeFor(child.getOperatorType()),
                                                   getTypeCodeFor(parent.getOperatorType())),
                                                0.5);
    }

    public void addRule(OperatorType child, OperatorType parent, double value) {

        long childCode = getTypeCodeFor(child);
        long parentCode = getTypeCodeFor(parent);
        long combinedType = combinedTypeCode(childCode, parentCode);

        costs.put(combinedType, value);
    }

    /**
     * Returns the combined code type
     * @param child the type of the child
     * @param parent the type of the parent
     * @return return the combined type
     */
    private static long combinedTypeCode(long child, long parent) {
        long tmp = child;
        tmp = tmp << 16;
        return tmp | parent;
    }

    /**
     * Returns the code for the operator type
     * @param type the type we want the code for
     * @return the code
     */
    private static long getTypeCodeFor(OperatorType type) {

        switch (type) {
            case AGGREGATE : return 1;
            case DUPLICATE_REMOVE : return 2;
            case FRAME_OUTPUT : return 4;
            case JOIN : return 8;
            case PROJECTION : return 16;
            case SCALAR_FUNCTION : return 32;
            case SEED : return 64;
            case SELECTION : return 128;
            case TABLE_SCAN : return 256;
            case VG_WRAPPER : return 512;
            case UNION_VIEW : return 1024;
        }

        throw new RuntimeException("Unrecognised operator type!");
    }

}
