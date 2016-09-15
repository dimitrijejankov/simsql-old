

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

import simsql.code_generator.translator.expressions.*;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.*;

/**
 * This file contains all the classes used to represent expressions
 * in the query, such as boolean predicates, arithmetics, function calls, etc.
 * <p>
 * These are constructed from the prolog tuples and provide a visitor interface
 * for traversal and translation.
 *
 * @author Luis.
 */

/** The general expression type -- all expressions must extend this class. */
public abstract class PrologQueryExpression {

    // acceptable types. 
    public enum Type {
        ATTRIBUTE, LITERAL, COMPEXP, BOOLEXP, AGGEXP, ARITHEXP, FUNCTION
    }

    protected String name = "<null>";

    public String getName() {
        return name;
    }

    // all of them must return their type
    public abstract PrologQueryExpression.Type getType();

    // and accept a visitor
    public abstract <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor);

    // the constant terms...
    private static HashMap<AtomTerm, PrologQueryExpression> constTerms = new HashMap<AtomTerm, PrologQueryExpression>();

    static {

        // count-all
        constTerms.put(new AtomTerm("star"), new PrologAttributeExp());

        // assignment.
        constTerms.put(new AtomTerm("minus"), new PrologAttributeExp());
    }

    // used to create from a tuple.
    public static PrologQueryExpression fromTuple(TupleTerm t,
                                                  HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                                                  HashMap<AtomTerm, TupleTerm> exprTuples) {

        if (t.getAtom().equals(new AtomTerm("__const"))) {
            return constTerms.get(t.getTerm(0));
        }

        if (t.getAtom().equals(new AtomTerm("compExp"))) {
            return new PrologCompExp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("boolAnd"))) {
            return new PrologBoolExp(t, PrologBoolExp.Type.AND, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("boolOr"))) {
            return new PrologBoolExp(t, PrologBoolExp.Type.OR, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("boolNot"))) {
            return new PrologBoolExp(t, PrologBoolExp.Type.NOT, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("set"))) {
            return new PrologBoolExp(t, PrologBoolExp.Type.SET, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("aggExp"))) {
            return new PrologAggExp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("arithExp"))) {
            return new PrologArithExp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        if (t.getAtom().equals(new AtomTerm("verbatim"))) {
            return new PrologLiteralExp((AtomTerm) t.getTerm(1));
        }

        if (t.getAtom().equals(new AtomTerm("function"))) {
            return new PrologFunctionExp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        throw new RuntimeException("Unrecognized expression tuple " + t);
    }

    // used to create from a list of expressions.
    public static ArrayList<PrologQueryExpression> fromListOfTuples(ListTerm t, HashMap<AtomTerm, TupleTerm> vgTuples,
                                                                    HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                                                                    HashMap<AtomTerm, TupleTerm> exprTuples) {

        ArrayList<PrologQueryExpression> outExp = new ArrayList<PrologQueryExpression>();
        for (PrologTerm tt : t) {
            outExp.add(PrologQueryExpression.fromTuple(exprTuples.get(tt), vgTuples, attributeTuples, relationTuples, exprTuples));
        }

        return outExp;
    }

    // used to create from a child tuple of the form (exp,type) like (o_orderkey,identifier).
    public static PrologQueryExpression fromChildTuple(AtomTerm t, AtomTerm cType, HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples,
                                                       HashMap<AtomTerm, TupleTerm> relationTuples, HashMap<AtomTerm, TupleTerm> exprTuples) {

        // check the cType
        if (cType.equals(new AtomTerm("literal"))) {
            return new PrologLiteralExp(t);
        }

        if (cType.equals(new AtomTerm("identifier"))) {
            return new PrologAttributeExp(attributeTuples.get(t));
        }

        if (cType.equals(new AtomTerm("expression"))) {
            return PrologQueryExpression.fromTuple(exprTuples.get(t), vgTuples, attributeTuples, relationTuples, exprTuples);
        }

        throw new RuntimeException("Unrecognized child expression tuple " + t + " " + cType);
    }
}

/**** ---- ALL THE SPECIFIC TYPES AHEAD ---- ****/

