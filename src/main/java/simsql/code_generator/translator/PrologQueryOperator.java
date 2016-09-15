

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


package simsql.code_generator.translator;

import simsql.code_generator.translator.operators.*;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.*;

/**
 * This file contains all the classes used to represent relational operations
 * in the query.
 * <p>
 * These are constructed from the prolog tuples and provide a visitor interface
 * for traversal and translation.
 *
 * @author Luis.
 */


/** A general query operator type. */
public abstract class PrologQueryOperator {
    public enum Type {
        SELECTION, PROJECTION, JOIN, TABLESCAN, SEED, VGWRAPPER, SCALARFUNC, GENAGG, SPLIT, ANTIJOIN, SEMIJOIN, DEDUP, FRAMEOUTPUT
    }


    // the name, always the first element in the tuple.
    protected String name;

    public String getName() {
        return name;
    }

    // all operators must return their type.
    public abstract PrologQueryOperator.Type getType();

    // and accept a visitor
    public abstract <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor);

    // creates an operator from a prolog tuple.
    public static PrologQueryOperator fromTuple(TupleTerm t,
                                                HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                                                HashMap<AtomTerm, TupleTerm> exprTuples) {

        // use the operator type to decide.
        if (t.getAtom().equals(new AtomTerm("selection"))) {
            return new PrologSelectionOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("projection"))) {
            return new PrologProjectionOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("join"))) {
            return new PrologJoinOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("tablescan"))) {
            return new PrologTablescanOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("seed"))) {
            return new PrologSeedOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("vgwrapper"))) {
            return new PrologVGWrapperOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("scalarfunc"))) {
            return new PrologScalarFunctionOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("genagg"))) {
            return new PrologGeneralAggregateOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("split"))) {
            return new PrologSplitOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("antijoin"))) {
            return new PrologAntiJoinOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("semijoin"))) {
            return new PrologSemiJoinOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("dedup"))) {
            return new PrologDedupOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("frameoutput"))) {
            return new PrologFrameOutputOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        throw new RuntimeException("Unrecognized operator type " + t);
    }
}

/**** ---- ALL THE SPECIFIC TYPES AHEAD ---- ****/

