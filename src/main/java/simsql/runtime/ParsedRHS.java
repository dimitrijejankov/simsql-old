

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



package simsql.runtime;

import java.util.Map;
import java.util.ArrayList;


/**
 * This class is used in the parser to store the results of parsing the RHS of a "varDef" rule.
 * Depending up the RHS, a different subclass of this rule should be used.  A generic "ParsedRHS"
 * object contains no data; hence it just returns null all of the time.  Various subclasses store
 * and return data.  If you want to add more RHS values for the varDef rule, you can just add
 * another method (returning null) to the ParsedRHS class, and then create a new sublcass that
 * overrides that method to actually store some data.
 */
class ParsedRHS {
 
  public Map <String, ParsedRHS> getVarList () {
    return null;
  }

  public String getIdentifier () {
    return null; 
  }
  
  public ArrayList <String> getIdentifierList () {
    return null;
  }
  
  public ArrayList <String> getStringList () {
    return null;  
  }
  
  public String getStringLiteral () {
    return null;
  }
  
  public Integer getInteger () {
    return null;
  }
  
  public Expression getExpression () {
    return null;
  }
  
  public ArrayList <Assignment> getAssignmentList () {
    return null;
  }

  public ArrayList<AggregateExp> getAggregateList() {
    return null;
  }
  
  public String print () {
    return "<nothing>"; 
  }
}

// corresponds to the assignmentList production rule
class AssignmentListRHS extends ParsedRHS {
  
  private ArrayList <Assignment> value;
  
  public AssignmentListRHS (ArrayList <Assignment> valueIn) {

      try {
          // HACK ADDED BY LUIS: AVOID ISPRESENT HERE.
          value = new ArrayList<>();
          for (Assignment a : valueIn) {
              if (!a.getIdentifier().equals("isPres"))
                  value.add(a);
          }
      }
      catch (Exception e) {
          throw new RuntimeException("Ninja");
      }

		//    value = valueIn; 
  }
  
  public ArrayList <Assignment> getAssignmentList () {
    return value;
  }
  
  public String print () {
    String returnVal = "";
    for (Assignment a : value) {
      returnVal += a.print () + " ";
    }
    return returnVal;
  }
}

// corresponds to the String production rule
class StringLiteralRHS extends ParsedRHS {
  
  private String value;
  
  public StringLiteralRHS (String valueIn) {
    value = valueIn; 
  }
  
  public String getStringLiteral () {
    return value;
  }
  
  public String print () {
    return value; 
  }
}

// corresponds to the varList production rule
class VarListRHS extends ParsedRHS {
  
  private Map <String, ParsedRHS> value;
  
  public VarListRHS (Map <String, ParsedRHS> valueIn) {
    value = valueIn; 
  }
  
  public Map <String, ParsedRHS> getVarList () {
    return value;
  }
  
  public String print () {
    String returnVal = "";
    for (Map.Entry <String, ParsedRHS> i : value.entrySet ()) {
      returnVal += "<" + i.getKey () + ": " + i.getValue ().print () + "> ";
    }
    return returnVal;
  }
}

// corresponds to the Identifier production rule
class IdentifierRHS extends ParsedRHS {
  
  private String value;
  
  public IdentifierRHS (String valueIn) {
    value = valueIn; 
  }
  
  public String getIdentifier () {
    return value; 
  }
  
  public String print () {
    return value; 
  }
}

// corresponds to the stringList production rule
class StringListRHS extends ParsedRHS {
  
  private ArrayList <String> value;
  
  public StringListRHS (ArrayList <String> valueIn) {
    value = valueIn; 
  }
  
  public ArrayList <String> getStringList () {
    return value; 
  }
  
  public String print () {
    String returnVal = "";
    for (String i : value) {
      returnVal += i + " ";
    }
    return returnVal;
  }
}

// corresponds to the Int production rule
class IntegerRHS extends ParsedRHS {
  
  private Integer value;
  
  public IntegerRHS (Integer valueIn) {
    value = valueIn; 
  }
  
  public Integer getInteger () {
    return value; 
  }
  
  public String print () {
    return "" + value;
  }
}

// corresponds to the expression production rule
class ExpressionRHS extends ParsedRHS {
  
  private Expression value;
  
  public ExpressionRHS (Expression valueIn) {
    value = valueIn; 
  }
  
  public Expression getExpression () {
    return value; 
  }
  
  public String print () {
    return value.print (); 
  }
}

// corresponds to the identifierList production rule
class IdentifierListRHS extends ParsedRHS {
  
  private ArrayList <String> value;
  
  public IdentifierListRHS (ArrayList <String> valueIn) {

		// HACK ADDED BY LUIS: AVOID ISPRESENT HERE.
		value = new ArrayList<String>();
		for (String a: valueIn) {
			if (!a.equals("isPres"))
				value.add(a);
		}

		//    value = valueIn; 
  }
  
  public ArrayList <String> getIdentifierList () {
    return value; 
  }
  
  public String print () {
    String returnVal = "";
    for (String i : value) {
      returnVal += i + " ";
    }
    return returnVal;
  }
}

// corresponds to the aggregateList production rule
class AggregateListRHS extends ParsedRHS {

	private ArrayList<AggregateExp> value;

	public AggregateListRHS(ArrayList<AggregateExp> valueIn) {
		value = valueIn;
	}

	public ArrayList<AggregateExp> getAggregateList() {
		return value;
	}

	public String print() {
		String returnVal = "";
		for (AggregateExp a: value) {
	    returnVal += a.print() + " ";
		}

		return returnVal;
	}
}

// corresponds to the rowList production rule
class RowListRHS extends ParsedRHS {

	private ArrayList<ArrayList<Expression>> value;

	public RowListRHS(ArrayList<ArrayList<Expression>> valueIn) {
		value = valueIn;
	}

	private AttributeType getType(Expression e) {
		if (e.getType().equals("literal string"))
			return new AttributeType(new StringType());

		if (e.getType().equals("literal float"))
			return new AttributeType(new DoubleType());

		if (e.getType().equals("literal int"))
			return new AttributeType(new IntType());
		
		if (e.getType().equals("unary minus"))
			return getType(e.getSubexpression());

		// otherwise
		return new AttributeType(new NullType());
	}

	private Attribute getValue(Expression e) {
		if (e.getType().equals("literal string"))
			return new StringAttribute(e.getValue().replaceAll("\"", ""));

		if (e.getType().equals("literal float"))
			return new DoubleAttribute(Double.valueOf(e.getValue()));

		if (e.getType().equals("literal int"))
			return new IntAttribute(Integer.valueOf(e.getValue()));

		if (e.getType().equals("unary minus"))
			return new IntAttribute(0).subtract(getValue(e.getSubexpression()));

		// otherwise
		return new NullAttribute();
	}

	public AttributeType[] getCheckTypes() {

		// no rows?
		if (value.size() == 0)
			return new AttributeType[0];

		AttributeType[] ret = new AttributeType[value.get(0).size()];
		for (int i=0;i<ret.length;i++) {
			ret[i] = getType(value.get(0).get(i));
		}

		for (ArrayList<Expression> ex: value) {

			// all rows must have the same number of columns
			if (ex.size() != ret.length) {
				throw new RuntimeException("Inconsistent row schema in temporary table (number of columns)");
			}

			for (int i=0;i<ret.length;i++) {
				if (ret[i].getTypeCode() == TypeCode.INT && getType(ex.get(i)).getTypeCode() == TypeCode.DOUBLE) {
					ret[i] = new AttributeType(new DoubleType());
					continue;
				}
			}
		}

		return ret;
	}

	public int getNumRows() {
		return value.size();
	}

	// returns the i^th record's j^th attribute.
	public Attribute getValue(int i, int j) {
		return getValue(value.get(i).get(j));
	}

	public String print() {

		String returnVal = "";
		for (ArrayList<Expression> a: value) {
	    for (Expression b: a) {
				returnVal += getValue(b).print(100);
	    }
	    returnVal += "\n";
		}

		return returnVal;
	}
}
