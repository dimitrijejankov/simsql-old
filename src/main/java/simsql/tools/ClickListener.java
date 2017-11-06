package simsql.tools;

import org.graphstream.graph.Graph;
import org.graphstream.ui.swingViewer.ViewerListener;
import simsql.compiler.operators.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Handles the actions that are done on a view
 */
public class ClickListener implements ViewerListener {

    /**
     * The graph for this listener
     */
    private Graph graph;

    /**
     * The status bar
     */
    private JLabel status;

    /**
     * A reference to the main frame
     */
    private JFrame mainFrame;

    /**
     * The table
     */
    private static JScrollPane table;

    ClickListener(Graph graph, JLabel status, JFrame mainFrame) {
        this.graph = graph;
        this.status = status;
        this.mainFrame = mainFrame;
    }

    @Override
    public void viewClosed(String s) {}

    @Override
    public void buttonPushed(String s) {

        String statusMessage = "Operator name : " + s;

        Operator o = graph.getNode(s).getAttribute("operator", Operator.class);
        String[] columnNames = { "Attribute name", "Value"};
        ArrayList<Object[]> data = new ArrayList<>();

            if(o instanceof Aggregate) {
            statusMessage += " , type : aggregate";

            Aggregate a  = (Aggregate) o;

            // add the node name
            data.add(new Object[] {"Node Name", a.getNodeName()});

            // node type
            data.add(new Object[] {"Node Type", "Aggregate"});

            // add the output attributes
            for(String at : a.getOutputAttributeNames()) {
                data.add(new Object[] {"Output Attribute", at});
            }

            // add the group by list
            for(String at : a.getGroupByList()) {
                data.add(new Object[] {"GroupBy Attribute", at});
            }

            // aggregate name
            data.add(new Object[] {"Node Name", a.getAggregateName()});

            replaceTable(new JTable(toArray(data), columnNames));
        }
        else if(o instanceof DuplicateRemove) {
            statusMessage += " , type : duplicate remove";

            DuplicateRemove a  = (DuplicateRemove) o;

            // add the node name
            data.add(new Object[] {"Node Name", a.getNodeName()});

            // node type
            data.add(new Object[] {"Node Type", "DuplicateRemove"});

            // add the node name
            data.add(new Object[] {"Node Name", a.getNodeName()});

            // add the output attributes
            for(String at : a.getOutputAttributeNames()) {
                data.add(new Object[] {"Output Attribute", at});
            }

            replaceTable(new JTable(toArray(data), columnNames));
        }
        else if(o instanceof FrameOutput) {
            statusMessage += " , type : frame output";

            FrameOutput a  = (FrameOutput) o;

            // add the node name
            data.add(new Object[] {"Node Name", a.getNodeName()});

            // node type
            data.add(new Object[] {"Node Type", "FrameOutput"});

            // add the output tables
            for(int i = 0; i < a.getTableList().size(); ++i) {
                data.add(new Object[] {a.getTableList().get(i), a.getChildren().get(i).getNodeName()});
            }

            replaceTable(new JTable(toArray(data), columnNames));
        }
        else if(o instanceof Projection) {
            statusMessage += " , type : projection";

            Projection a  = (Projection) o;

            // add the node name
            data.add(new Object[] {"Node Name", a.getNodeName()});

            // node type
            data.add(new Object[] {"Node Type", "Projection"});

            // add the node name
            data.add(new Object[] {"Node Name", a.getNodeName()});

            // add the output attributes
            for(String at : a.getOutputAttributeNames()) {
                data.add(new Object[] {"Output Attribute", at});
            }

            replaceTable(new JTable(toArray(data), columnNames));
        }
        else if(o instanceof ScalarFunction) {
            statusMessage += " , type : scalar function";

            ScalarFunction a  = (ScalarFunction) o;

            // add the node name
            data.add(new Object[] {"Node Name", a.getNodeName()});

            // node type
            data.add(new Object[] {"Node Type", "ScalarFunction"});

            // add the output attributes
            for(String at : a.getOutputAttributeNames()) {
                data.add(new Object[] {"Output Attribute", at});
            }

            // show the math operators of the columns
            for(String at : a.getInvertedOutputMap().keySet()) {
                data.add(new Object[] {"Attribute : " + at, a.getInvertedOutputMap().get(at).visitNode()});
            }

            replaceTable(new JTable(toArray(data), columnNames));
        }
        else if(o instanceof Seed) {
            statusMessage += " , type : seed";

            Seed a  = (Seed) o;

            // add the node name
            data.add(new Object[] {"Node Name", a.getNodeName()});

            // node type
            data.add(new Object[] {"Node Type", "Seed"});

            // add the output attributes
            for(String at : a.getOutputAttributeNames()) {
                data.add(new Object[] {"Output Attribute", at});
            }

            replaceTable(new JTable(toArray(data), columnNames));
        }
        else if(o instanceof Selection) {
            statusMessage += " , type : selection";

            Selection a  = (Selection) o;

            // add the node name
            data.add(new Object[] {"Node Name", a.getNodeName()});

            // node type
            data.add(new Object[] {"Node Type", "Selection"});

            // add the output attributes
            for(String at : a.getOutputAttributeNames()) {
                data.add(new Object[] {"Output Attribute", at});
            }

            data.add(new Object[] {"Boolean Operator", a.getBooleanOperator().visitNode()});

            replaceTable(new JTable(toArray(data), columnNames));
        }
        else if(o instanceof TableScan) {
            statusMessage += " , type : table scan";

            TableScan a  = (TableScan) o;

            // add the node name
            data.add(new Object[] {"Node Name", a.getNodeName()});

            // node type
            data.add(new Object[] {"Node Type", "TableScan"});

            // add the table
            data.add(new Object[] {"Table", a.getTableName()});

            // add the output attributes
            for(String at : a.getOutputAttributeNames()) {
                data.add(new Object[] {"Output Attribute", at});
            }

            replaceTable(new JTable(toArray(data), columnNames));
        }
        else if(o instanceof UnionView) {
            statusMessage += " , type : unionView";

            UnionView a  = (UnionView) o;

            // add the node name
            data.add(new Object[] {"Node Name", a.getNodeName()});

            // node type
            data.add(new Object[] {"Node Type", "UnionView"});

            // add the output attributes
            for(String at : a.getOutputAttributeNames()) {
                data.add(new Object[] {"Output Attribute", at});
            }

            // add the children
            for(int i = 0; i < a.getChildren().size(); ++i) {
                data.add(new Object[] {"Child " + i, a.getChildren().get(i).getNodeName()});
            }

            replaceTable(new JTable(toArray(data), columnNames));
        }
        else if(o instanceof VGWrapper) {
                statusMessage += " , type : vg-wrapper";

                VGWrapper a  = (VGWrapper) o;

                // add the node name
                data.add(new Object[] {"Node Name", a.getNodeName()});

                // node type
                data.add(new Object[] {"Node Type", "VG Wrapper"});

                // add the output attributes
                for(String at : a.getOutputAttributeNames()) {
                    data.add(new Object[] {"Output Attribute", at});
                }

                // the type of the join
                data.add(new Object[] {"Name of the VGFunction", a.getVgFunctionName()});

                // add the output attributes
                for(String at : a.getInputAttributeNameList()) {
                    data.add(new Object[] {"Input Attribute", at});
                }

                // add the children
                for(int i = 0; i < a.getChildren().size(); ++i) {
                    data.add(new Object[] {"Child " + i, a.getChildren().get(i).getNodeName()});
                }

                replaceTable(new JTable(toArray(data), columnNames));
        }
        else if(o instanceof Join) {
                statusMessage += " , type : join";

                Join a  = (Join) o;

                // add the node name
                data.add(new Object[] {"Node Name", a.getNodeName()});

                // node type
                data.add(new Object[] {"Node Type", "Join"});

                // add the output attributes
                for(String at : a.getOutputAttributeNames()) {
                    data.add(new Object[] {"Output Attribute", at});
                }

                // the type of the join
                data.add(new Object[] {"Join Type", a.getType()});

                // add the children
                for(int i = 0; i < a.getChildren().size(); ++i) {
                    data.add(new Object[] {"Child " + i, a.getChildren().get(i).getNodeName()});
                }

                replaceTable(new JTable(toArray(data), columnNames));
        }

        status.setText(statusMessage);
    }

    private void replaceTable(JTable newTable) {
        if(table != null) {
            mainFrame.remove(table);
        }

        table = new JScrollPane(newTable);
        table.setPreferredSize(new Dimension(500, 1000));

        mainFrame.add(table, BorderLayout.WEST);
    }

    private Object[][] toArray(ArrayList<Object[]> arrayList) {

        Object[][] array = new Object[arrayList.size()][];

        for (int i = 0; i < arrayList.size(); i++) {
            array[i] = arrayList.get(i);
        }

        return array;
    }

    @Override
    public void buttonReleased(String s) {}
}
