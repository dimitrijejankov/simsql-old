package simsql.compiler;

import simsql.runtime.RelOp;
import simsql.shell.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ParallelExecutor extends Thread {

    private RelOp nextOp;
    private boolean verbose;
    private RuntimeParameter runtimeParameters;
    private String error;
    private boolean errorIndicator;
    private ParallelExecutor parent;

    public ParallelExecutor(RelOp nextOp,
                            boolean verbose,
                            RuntimeParameter runtimeParameters,
                            ParallelExecutor parent) {
        this.nextOp = nextOp;
        this.verbose = verbose;
        this.runtimeParameters = runtimeParameters;
        this.errorIndicator = false;
        this.parent = parent;
    }

    @Override
    public void run() {

        // run the job
        try {

            nextOp.run(runtimeParameters, verbose, parent);

        } catch (Exception e) {

            // grab the error and set the indicator
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            error ="Unable to execute query!\n" + sw.toString();
            errorIndicator = true;
        }
    }

    public String getError() {
        return error;
    }

    public boolean hasError() {
        return errorIndicator;
    }

    public void waitToFinish() {
        try {
            this.join();
        } catch (InterruptedException ignored) { }
    }
}
