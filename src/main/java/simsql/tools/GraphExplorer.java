package simsql.tools;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.ViewerPipe;
import simsql.compiler.operators.Operator;
import simsql.compiler.timetable.GraphCutter;
import simsql.compiler.timetable.TimeTableNode;


/**
 * Tool to explore the graph
 */
public class GraphExplorer {

    /**
     * The main frame of the graph explorer
     */
    private JFrame mainFrame;

    /**
     * The the tab pane
     */
    private JTabbedPane tabbedPane;

    /**
     * The status bar
     */
    private JLabel status;

    /**
     * The current cut
     */
    private int currentCut;

    /**
     * The thing that does the graph cutting...
     */
    private GraphCutter graphCutter;

    private HashMap<View, Graph> grapForView;

    private GraphExplorer(){

        // init
        grapForView = new HashMap<>();

        // prepare the GUI
        prepareGUI();

        // clear all parameters
        clear();
    }

    public static void main(String[] args){
        // set the appropriate renderer
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        GraphExplorer graphExplorer = new GraphExplorer();
    }

    private void clear() {

        // reset the cut
        currentCut = 0;

        // clear the graph for view map
        grapForView.clear();

        // remove all tabs
        tabbedPane.removeAll();

        // null the graph cutter
        graphCutter = null;
    }

    private void prepareGUI(){

        // initialize the main frame
        mainFrame = new JFrame("Query Graph Explorer");
        mainFrame.setSize(400,400);
        mainFrame.setLayout(new BorderLayout());

        // make a status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY), new EmptyBorder(4, 4, 4, 4)));

        status = new JLabel();
        statusBar.add(status);

        // add the status bar
        mainFrame.add(statusBar, BorderLayout.SOUTH);

        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });

        // create a tabbed pane
        tabbedPane = new JTabbedPane();
        mainFrame.add(tabbedPane, BorderLayout.CENTER);

        // preapare the menu bar
        prepareMenuBar();

        mainFrame.setVisible(true);
    }

    private void prepareMenuBar() {

        // create the menu bar
        JMenuBar menuBar = new JMenuBar();

        // create the menus
        JMenu file = new JMenu("File");
        JMenu commands = new JMenu("Commands");
        JMenu view = new JMenu("View");

        // add the menu to the bar
        menuBar.add(file);
        menuBar.add(commands);
        menuBar.add(view);

        // create menu items
        JMenuItem open = new JMenuItem("Open");
        JMenuItem exit = new JMenuItem("Exit");

        JMenuItem clear = new JMenuItem("Clear");
        JMenuItem nextCut = new JMenuItem("Show Next Cut");

        JMenuItem zoomIn = new JMenuItem("Zoom In");
        JMenuItem zoomOut = new JMenuItem("Zoom Out");
        JMenuItem search = new JMenuItem("Search");

        // connect the menu items
        file.add(open);
        file.add(exit);

        commands.add(clear);
        commands.add(nextCut);

        view.add(zoomIn);
        view.add(zoomOut);
        view.add(search);

        // add the menu bar
        mainFrame.add(menuBar, BorderLayout.NORTH);

        // init the actions
        open.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select a directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                    open(chooser.getSelectedFile());
                }
                else {
                    System.out.println("No Selection ");
                }
            }
        });

        open.setAccelerator(KeyStroke.getKeyStroke('o'));

        exit.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });

        clear.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                clear();
            }
        });

        nextCut.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                nextCut();
            }
        });

        nextCut.setAccelerator(KeyStroke.getKeyStroke('c'));

        zoomIn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                View v = (View) ((BorderLayout)((JPanel)tabbedPane.getSelectedComponent()).getLayout()).getLayoutComponent(BorderLayout.CENTER);
                double zoom = v.getCamera().getViewPercent();
                v.getCamera().setViewPercent(zoom * 0.5);
            }
        });

        zoomOut.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                View v = (View) ((BorderLayout)((JPanel)tabbedPane.getSelectedComponent()).getLayout()).getLayoutComponent(BorderLayout.CENTER);
                double zoom = v.getCamera().getViewPercent();
                v.getCamera().setViewPercent(zoom * 2);
            }
        });

        search.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            search();
            }
        });
    }

    private void open(File selectedFile) {
        // figure out where this is
        String path = selectedFile.getAbsolutePath();
        //path = "/home/dimitrije/Documents/queries/dp_activation_1_1";

        // JSON object loader
        ObjectMapper mapper = new ObjectMapper();

        try {
            // opens the table operation map
            File tableOperationMapFile = new File(path + "/tableOperationMap.json");

            // deserialize the table operation map
            HashMap<String, Operator> tableOperationMap = mapper.readValue(tableOperationMapFile, new TypeReference<HashMap<String, Operator>>() {});

            // open the required tables file
            File requiredTablesFile = new File(path + "/requiredTables.json");

            // deserialize the required tables
            LinkedList<TimeTableNode> requiredTables = mapper.readValue(requiredTablesFile, new TypeReference<LinkedList<TimeTableNode>>() {});

            // open the required tables file
            File backwardEdgesFile = new File(path + "/backwardEdges.json");

            // deserialize the backward edges
            HashMap<String, HashSet<String>> backwardEdges = mapper.readValue(backwardEdgesFile, new TypeReference<HashMap<String, HashSet<String>>>() {});

            // open the required tables file
            File queriesFile = new File(path + "/queries.json");

            // queries
            ArrayList<Operator> queries = mapper.readValue(queriesFile, new TypeReference<ArrayList<Operator>>() {});

            // if we have something loaded unload it
            if(graphCutter != null) {
                clear();
            }

            // generate the bipartite graph
            graphCutter = new GraphCutter(requiredTables, backwardEdges, queries, tableOperationMap);

            JComponent containerPanel = new JPanel(new BorderLayout());
            tabbedPane.addTab("The Graph", null, containerPanel, "The whole graph");

            Graph graph = constructGraph(graphCutter.getSourceOperators(), "Whole Graph");

            Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_SWING_THREAD);
            viewer.enableAutoLayout();

            View view = viewer.addDefaultView(false);
            grapForView.put(view, graph);

            ViewerPipe fromViewer = viewer.newViewerPipe();
            fromViewer.addViewerListener(new ClickListener(graph, status, mainFrame));
            fromViewer.addSink(graph);

            // add a mouse listener to pump messages...
            view.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    fromViewer.pump();
                    super.mousePressed(mouseEvent);
                }
            });

            containerPanel.add(view, BorderLayout.CENTER);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Failed to load the files.",
                    "Open failed!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Graph constructGraph(LinkedList<Operator> sourceOperators, String graphName) {

        // nodes we need to visit
        LinkedList<Operator> nodesToVisit = new LinkedList<>(sourceOperators);
        HashSet<Operator> visitedNodes = new HashSet<>();

        // the graph we are going to construct
        Graph graph = new SingleGraph(graphName);

        graph.addAttribute("ui.stylesheet", "node { text-size : 14; shape: box; fill-color: yellow; size-mode: fit;}");

        // add the nodes in the graph
        while(!nodesToVisit.isEmpty()) {

            Operator o = nodesToVisit.getFirst();

            // add the node to the graph
            Node n = graph.addNode(o.getNodeName());
            n.setAttribute("ui.label", o.getNodeName() + " (" +  o.getOperatorType().toString().toLowerCase().replace("_", " ") + ")");
            n.setAttribute("operator", o);
            visitedNodes.add(o);

            // add the all unvisited operators
            for(Operator c : o.getParents())  {
                if(!visitedNodes.contains(c) && !nodesToVisit.contains(c)) {
                    nodesToVisit.addLast(c);
                }
            }

            // remove node
            nodesToVisit.removeFirst();
        }

        // reinitialize this
        nodesToVisit = new LinkedList<>(sourceOperators);
        visitedNodes = new HashSet<>();

        // the number of edges
        int edgeNumber = 0;

        // add all the connections
        while(!nodesToVisit.isEmpty()) {

            Operator o = nodesToVisit.getFirst();

            // add the node to the graph
            visitedNodes.add(o);

            // add the all unvisited operators
            for(Operator p : o.getParents())  {
                if(!visitedNodes.contains(p) && !nodesToVisit.contains(p)) {
                    nodesToVisit.addLast(p);
                }

                // add the edge
                graph.addEdge("edge" + edgeNumber++, o.getNodeName(), p.getNodeName(), true);
            }

            // remove node
            nodesToVisit.removeFirst();
        }

        return graph;
    }

    private void search() {

        String s = (String)JOptionPane.showInputDialog(
                mainFrame,
                "Search for node with name : ",
                "Search Dialog",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");

        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {

            try {
                View v = (View) ((BorderLayout) ((JPanel) tabbedPane.getSelectedComponent()).getLayout()).getLayoutComponent(BorderLayout.CENTER);
                Graph g = grapForView.get(v);
                Node node = g.getNode(s);

                if(node != null) {
                    node.addAttribute("ui.style", "fill-color: rgb(0,100,255);");
                }
            }
            catch (Exception ignore) {}

            return;
        }

        JOptionPane.showMessageDialog(mainFrame,
                "You didn't enter anything",
                "Open failed!",
                JOptionPane.WARNING_MESSAGE);
    }

    private Graph constructSinkGraph(LinkedList<Operator> sinkListOperators, String graphName) {

        // nodes we need to visit
        LinkedList<Operator> nodesToVisit = new LinkedList<>(sinkListOperators);
        HashSet<Operator> visitedNodes = new HashSet<>();

        // the graph we are going to construct
        Graph graph = new SingleGraph(graphName);

        graph.addAttribute("ui.stylesheet", "node { text-size : 14; shape: box; fill-color: yellow; size-mode: fit;}");

        // add the nodes in the graph
        while(!nodesToVisit.isEmpty()) {

            Operator o = nodesToVisit.getFirst();

            // add the node to the graph
            Node n = graph.addNode(o.getNodeName());
            n.setAttribute("ui.label", o.getNodeName() + " (" +  o.getOperatorType().toString().toLowerCase().replace("_", " ") + ")");
            n.setAttribute("operator", o);
            visitedNodes.add(o);

            // add the all unvisited operators
            for(Operator c : o.getChildren())  {
                if(!visitedNodes.contains(c) && !nodesToVisit.contains(c)) {
                    nodesToVisit.addLast(c);
                }
            }

            // remove node
            nodesToVisit.removeFirst();
        }

        // reinitialize this
        nodesToVisit = new LinkedList<>(sinkListOperators);
        visitedNodes = new HashSet<>();

        // the number of edges
        int edgeNumber = 0;

        // add all the connections
        while(!nodesToVisit.isEmpty()) {

            Operator o = nodesToVisit.getFirst();

            // add the node to the graph
            visitedNodes.add(o);

            // add the all unvisited operators
            for(Operator c : o.getChildren())  {
                if(!visitedNodes.contains(c) && !nodesToVisit.contains(c)) {
                    nodesToVisit.addLast(c);
                }

                // add the edge
                graph.addEdge("edge" + edgeNumber++, c.getNodeName(), o.getNodeName(), true);
            }

            // remove node
            nodesToVisit.removeFirst();
        }

        return graph;
    }

    private void nextCut() {

        JComponent containerPanel = new JPanel(new BorderLayout());

        try {
            ArrayList<Operator> sinkList = graphCutter.getCut();

            tabbedPane.addTab("Cut " + currentCut++, null, containerPanel, "One of the cuts");

            Graph graph = constructSinkGraph(new LinkedList<>(sinkList), "Cut " + currentCut);

            Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_SWING_THREAD);
            viewer.enableAutoLayout();

            View view = viewer.addDefaultView(false);
            grapForView.put(view, graph);

            ViewerPipe fromViewer = viewer.newViewerPipe();
            fromViewer.addViewerListener(new ClickListener(graph, status, mainFrame));
            fromViewer.addSink(graph);

            // add a mouse listener to pump messages...
            view.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    fromViewer.pump();
                    super.mousePressed(mouseEvent);
                }
            });

            containerPanel.add(view, BorderLayout.CENTER);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Could not extract a cut.",
                    "Cutting failed!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}