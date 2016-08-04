

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


package simsql.compiler; // package mcdb.catalog;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.util.ArrayList;


/**
 * This class takes care of intializing the SQLite database.
 * 
 * @author Luis, Bamboo
 */

public class InitDB {

	public static boolean create (DataAccess ds)  {
		try {

			// go ahead and create tables..
			Vector<String> ddl = new Vector<String> ();
			ddl.add ("create table dbconfigobject (objname char(32), objvalue char(32), primary key (objname))");
			ddl.add ("create table dbobject (objname char(32), objtype char(32), primary key (objname))");
			ddl.add ("create table relation (relname char(32), filename char(32), tuplenum integer, primary key (relname), foreign key (relname) references dbobject(objname))");
			ddl.add ("create table relationattribute (relname char(32), attname char(32), indexof integer,typeid char(32), uniquevalue integer, primary key (relname, attname, indexof), foreign key (relname) references relation(relname))");
			ddl.add ("create table primarykey (relname char(32), attname char(32), foreign key (relname) references relationattribute(relname), foreign key (attname) references relationattribute(attname))");
			ddl.add ("create table foreignkey (relname char(32), attname char(32), refrelname char(32), refattname char(32), primary key (relname, attname), foreign key (relname) references relationattributes(relname), foreign key (refrelname) references relationattribute(relname), foreign key (attname) references relationattribute(attname), foreign key (refattname) references relationattribute(attname))");
			ddl.add ("create table vgfunction (vgfname char(32), bundlespertuple integer, primary key (vgfname), foreign key (vgfname) references dbobject(objname))");
			ddl.add ("create table vgfunctionattribute (vgfname char(32), attname char(32), indexof integer, inputoroutput char(1), typeid char(32), domaintype char(32), domainsize char(32), primary key (vgfname, attname, indexof, inputoroutput), foreign key (vgfname) references vgfunction (vgfname), constraint io_check check (inputoroutput in ('I','O')))");
			ddl.add ("create table function (fname char(32), outputtype char(32), primary key (fname), foreign key (fname) references dbobject(objname))");
			ddl.add ("create table functionattribute (fname char(32), attname char(32), indexof integer, typeid char(32), domaintype char(32), domainsize char(32), primary key (fname, attname, indexof), foreign key (fname) references function (fname))");
			ddl.add ("create table view (viewname char(32), queryplan text, primary key (viewname), foreign key (viewname) references dbobject (objname))");
			ddl.add ("create table viewattribute (viewname char(32), attname char(32), indexof integer,typeid char(32), primary key (viewname, attname, indexof), foreign key (viewname) references view(viewame))");
			ddl.add ("create table randomrelation (relname char(32), queryplan text, primary key (relname), foreign key (relname) references dbobject(objname))");
			ddl.add ("create table randomattribute (relname char(32), attname char(32), indexof integer,typeid char(32), primary key (relname, attname, indexof), foreign key (relname) references randomrelation(relname))");
			ddl.add ("create table querytime (queryid integer, runningtime integer)");
			ddl.add ("create table regressor (queryid integer, regressorid char(32), input float, output float)");
			ddl.add ("create table indextable (realtablename char(32), indextablename char (32))");
			//adding for query interface 12/24/2013
			ddl.add ("create table mcdependency (viewname1 char(32), viewname2 char(32))");
			
			PreparedStatement ps;
			for (int i = 0; i < ddl.size (); i++) {
				String stmt = ddl.get(i);
				ps = ds.conn.prepareStatement(stmt);
				int res = ps.executeUpdate();
			}

			//next go ahead and set up default values for various parameters 
			//needed by the parser, optimizer and translator
			Vector<String> dml = new Vector<String> ();
			dml.add("INSERT INTO DBConfigObject VALUES('MCI', '1')");
			dml.add("INSERT INTO DBConfigObject VALUES('dfltSel', '0.3')");
			dml.add("INSERT INTO DBConfigObject VALUES('isPresSize', '0')");
			dml.add("INSERT INTO DBConfigObject VALUES('seedSize', '64')");
			dml.add("INSERT INTO DBConfigObject VALUES('vgThreads', '1')");
			dml.add("INSERT INTO DBConfigObject VALUES('vgTuples', '1')");
			dml.add("INSERT INTO DBConfigObject VALUES('vgPipeSize', '100000')");

			dml.add("INSERT INTO DBConfigObject VALUES('selectionCostWeight', '0.1')");
			dml.add("INSERT INTO DBConfigObject VALUES('tablescanCostWeight', '0.1')");
			dml.add("INSERT INTO DBConfigObject VALUES('seedCostWeight', '0.1')");
			dml.add("INSERT INTO DBConfigObject VALUES('splitCostWeight', '0.0')");
			dml.add("INSERT INTO DBConfigObject VALUES('joinCostWeight', '0.1')");
			dml.add("INSERT INTO DBConfigObject VALUES('antijoinCostWeight', '0.1')");
			dml.add("INSERT INTO DBConfigObject VALUES('semijoinCostWeight', '0.1')");
			dml.add("INSERT INTO DBConfigObject VALUES('dedupCostWeight', '0.1')");
			dml.add("INSERT INTO DBConfigObject VALUES('projectionCostWeight', '0.0')");
			dml.add("INSERT INTO DBConfigObject VALUES('genaggCostWeight', '0.1')");
			dml.add("INSERT INTO DBConfigObject VALUES('vgwrapperCostWeight', '0.1')");
			dml.add("INSERT INTO DBConfigObject VALUES('scalarfuncCostWeight', '0.0')");

			dml.add("INSERT INTO DBConfigObject VALUES('integerSize', '32')");
			dml.add("INSERT INTO DBConfigObject VALUES('doubleSize', '64')");
			dml.add("INSERT INTO DBConfigObject VALUES('stochintSize', '32')");
			dml.add("INSERT INTO DBConfigObject VALUES('stochdoubleSize', '64')");

			for (int i = 0; i < dml.size (); i++) {
				String stmt = dml.get(i);
				ps = ds.conn.prepareStatement(stmt);
				int res = ps.executeUpdate();
			}

			// Attribute type definitions/constants 
			// Attribute a = new Attribute ("init",1,"tmp");
		 //    int INTEGER = a.INTEGER;		int DOUBLE = a.DOUBLE;				int STRING = a.STRING;
		 //    int DATE = a.DATE;			int STOCHINT = a.STOCHINT;			int STOCHDBL = a.STOCHDBL;
			// int STOCHSTR = a.STOCHSTR;		int STOCHDAT = a.STOCHDAT;			int SEED = a.SEED;
			// int BOTTOM = a.BOTTOM;		int MULTITYPE = a.MULTITYPE;			int TOTALTYPE = a.TOTALTYPE;
			// int UNKNOWN = a.UNKNOWN;		int HYBRID = a.HYBRID; 			


			/***************************************************
                         * All of this has been commented out, since this  *
                         * should all be done from within the shell... if  *
                         * we need to, we can have a SimSQL SQL script     *
                         * that loads up all of the functions and all of   *
                         * the VG fuctions                                 *
                         ***************************************************/

			// some constants for object types
			/*int OBJ_VGFUNCTION = DataAccess.OBJ_VGFUNCTION;
			int OBJ_GENERAL_FUNCTION = DataAccess.OBJ_GENERAL_FUNCTION;

			//next go ahead and register the "builtin" functions
			Vector<String> sys = new Vector<String> ();
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('isnull','input',0,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('isnull','value',1,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('isnull','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('isnull',OBJ_GENERAL_FUNCTION)");

			// nullif returns null if the two params are equal..otherwise value of first param is returned
			// sql ignores trailing spaces in strings..so even if a string isn't empty but 
			// contains all spaces, it will still return null.. so nullif('      ', '') => returns null..
			// great way to ensure empty strings are always returned as nulls.. 
			// so nullif(override,'') => will always return either null or a string with 
			// atleast one character present..
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('nullif','input0',0,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('nullif','input1',1,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('nullif','output',1,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('nullif','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('nullif',OBJ_GENERAL_FUNCTION)");

			sys.add ("INSERT INTO VGATTRIBUTE VALUES('likefn','pattern',0,'I',STRING,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('likefn','teststr',1,'I',STRING,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('likefn','result',0,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('likefn','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('likefn',OBJ_GENERAL_FUNCTION)");

			sys.add ("INSERT INTO VGATTRIBUTE VALUES('caseexpr','testexpr',0,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('caseexpr','trueexpr',1,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('caseexpr','falseexpr',2,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('caseexpr','result',0,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('caseexpr','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('caseexpr',OBJ_GENERAL_FUNCTION)");

			sys.add ("INSERT INTO VGATTRIBUTE VALUES('substring','input',0,'I',STRING,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('substring','start_index',1,'I',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('substring','end_index',2,'I',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('substring','output',0,'O',STRING,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('substring','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('substring',OBJ_GENERAL_FUNCTION)");

			sys.add ("INSERT INTO VGATTRIBUTE VALUES('strcat','input1',0,'I',STRING,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('strcat','input2',1,'I',STRING,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('strcat','output',0,'O',STRING,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('strcat','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('strcat',OBJ_GENERAL_FUNCTION)");

			// date functions..safe in the sense they can be used over a random attribute..
			// actually this makes no sense..all these functions just have two evals one for single value 
			// and one over a jsonarray..isn't that not how all jaql functions are implemented..they work 
			// with arrays by default?
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('year','input',0,'I',STRING,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('year','value',0,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('year','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('year',OBJ_GENERAL_FUNCTION)");

			sys.add ("INSERT INTO VGATTRIBUTE VALUES('month','input',0,'I',STRING,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('month','value',0,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('month','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('month',OBJ_GENERAL_FUNCTION)");

			sys.add ("INSERT INTO VGATTRIBUTE VALUES('day','input',0,'I',STRING,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('day','value',0,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('day','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('day',OBJ_GENERAL_FUNCTION)");

			// FIXME
			// math functions
			// math functions throw runtime exception if they are passed anything other than long or double
			// fix it so runtime exceptions are avoided..what does postgres do?
			// exp (x)
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('exp','input',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('exp','value',0,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('exp','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('exp',OBJ_GENERAL_FUNCTION)");

			// pow (b, e)
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('pow','base',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('pow','expt',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('pow','value',0,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('pow','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('pow',OBJ_GENERAL_FUNCTION)");

			//rint (x)
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('rint','input',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('rint','value',0,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('rint','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('rint',OBJ_GENERAL_FUNCTION)");

			//ceil (x)
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('ceil','input',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('ceil','value',0,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('ceil','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('ceil',OBJ_GENERAL_FUNCTION)");

			//floor (x)
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('floor','input',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('floor','value',0,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('floor','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('floor',OBJ_GENERAL_FUNCTION)");

			//sqrt (x)
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('sqrt','input',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('sqrt','value',0,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('sqrt','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('sqrt',OBJ_GENERAL_FUNCTION)");

			//log (x)
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('log','input',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('log','value',0,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('log','builtin',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('log',OBJ_GENERAL_FUNCTION)");

			//internal functions
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('eq','arg1',0,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('eq','arg2',1,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('eq','val',1,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('eq','internal',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('eq',OBJ_GENERAL_FUNCTION)");

			sys.add ("INSERT INTO VGATTRIBUTE VALUES('neq','arg1',0,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('neq','arg2',1,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('neq','val',1,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('neq','internal',1)");

			sys.add ("INSERT INTO DBOBJECT VALUES('neq',OBJ_GENERAL_FUNCTION)");

			sys.add ("INSERT INTO VGATTRIBUTE VALUES('lt','arg1',0,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('lt','arg2',1,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('lt','val',1,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('lt','internal',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('lt',OBJ_GENERAL_FUNCTION)");
			
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('gt','arg1',0,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('gt','arg2',1,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('gt','val',1,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('gt','internal',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('gt',OBJ_GENERAL_FUNCTION)");

			sys.add ("INSERT INTO VGATTRIBUTE VALUES('orfn','arg1',0,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('orfn','arg2',1,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('orfn','val',1,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('orfn','internal',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('orfn',OBJ_GENERAL_FUNCTION)");

			sys.add ("INSERT INTO VGATTRIBUTE VALUES('andfn','arg1',0,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('andfn','arg2',1,'I',MULTITYPE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('andfn','val',1,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('andfn','internal',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('andfn',OBJ_GENERAL_FUNCTION)");

			// add the vgfunctions that are shipped with mcdb..
			// poisson XXX mcdb1.sql
			// FIXME we shouldn't hard code paths like these..
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('poisson','lambda',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('poisson','value',0,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('poisson','''Poisson.vg.so''',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('poisson',OBJ_VGFUNCTION)");

			// discretegamma mcdb2.sql
			// 1 integer 2 double 3 string
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('discretegamma','input0',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('discretegamma','input1',1,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('discretegamma','attr0',0,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('discretegamma','attr1',1,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('discretegamma','''DiscreteGamma.vg.so''',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('discretegamma',OBJ_VGFUNCTION)");

			// randomwalk mcdb3.sql
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('randomwalk','i0',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('randomwalk','i1',1,'I',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('randomwalk','i2',2,'I',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('randomwalk','i3',3,'I',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('randomwalk','i4',4,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('randomwalk','i5',5,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('randomwalk','month',0,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('randomwalk','year',1,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('randomwalk','value',2,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('randomwalk','''RandomWalk.vg.so''',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('randomwalk',OBJ_VGFUNCTION)");

			// bayesian mcdb4.sql
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bayesian','curprice',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bayesian','l_quantity',1,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bayesian','p0shape',2,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bayesian','d0shape',3,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bayesian','p0scale',4,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bayesian','d0scale',5,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bayesian','newprice',6,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bayesian','value',0,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('bayesian','''Bayesian.vg.so''',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('bayesian',OBJ_VGFUNCTION)");

			// bernoulli mcdb5.sql
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bernoulli_q5','shipdate',0,'I',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bernoulli_q5','mktsegment',1,'I',STRING,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bernoulli_q5','orderdate',2,'I',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bernoulli_q5','value',0,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('bernoulli_q5','''Bernoulli_Q5.vg.so''',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('bernoulli_q5',OBJ_VGFUNCTION)");

			// bernoulli mcdb6.sql
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bernoulli_q6','quantity',0,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bernoulli_q6','discount',1,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bernoulli_q6','shipdate',2,'I',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('bernoulli_q6','value',0,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('bernoulli_q6','''Bernoulli_Q6.vg.so''',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('bernoulli_q6',OBJ_VGFUNCTION)");

			// multinormal mcdb7.sql
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('multinormal','num_rv',0,'I',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('multinormal','rv',1,'I',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('multinormal','mean_value',2,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('multinormal','rv_X',3,'I',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('multinormal','rv_Y',4,'I',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('multinormal','covar',5,'I',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('multinormal','rv_out',0,'O',INTEGER,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGATTRIBUTE VALUES('multinormal','value',1,'O',DOUBLE,'nothing','infinite',64)");
			sys.add ("INSERT INTO VGFUNCTION VALUES('multinormal','''MultiNormal.vg.so''',1)");
			sys.add ("INSERT INTO DBOBJECT VALUES('multinormal',OBJ_VGFUNCTION)");
			
			// now register everyone in the catalog..
			for (int i = 0; i < sys.size (); i++) {
				String stmt = sys.get(i);
				stmt = stmt.replaceAll ("MULTITYPE",Integer.toString(MULTITYPE));
				stmt = stmt.replaceAll ("INTEGER",Integer.toString(INTEGER));
				stmt = stmt.replaceAll ("DOUBLE",Integer.toString(DOUBLE));
				stmt = stmt.replaceAll ("STRING",Integer.toString(STRING));
				stmt = stmt.replaceAll ("SEED",Integer.toString(SEED));
				stmt = stmt.replaceAll ("BOTTOM",Integer.toString(BOTTOM));
				stmt = stmt.replaceAll ("DATE",Integer.toString(DATE));
				stmt = stmt.replaceAll ("UNKNOWN",Integer.toString(UNKNOWN));
				stmt = stmt.replaceAll ("HYBRID",Integer.toString(HYBRID));
				stmt = stmt.replaceAll ("TOTALTYPE",Integer.toString(TOTALTYPE));
				stmt = stmt.replaceAll ("OBJ_VGFUNCTION",Integer.toString(OBJ_VGFUNCTION));
				stmt = stmt.replaceAll ("OBJ_GENERAL_FUNCTION",Integer.toString(OBJ_GENERAL_FUNCTION));
				//System.out.println (stmt);
				ps = ds.conn.prepareStatement(stmt);
				// run it
				int res = ps.executeUpdate();
				//System.out.println (" STMT RESULT " + res);
				if (i % 10 == 0 && i > 1) {
					System.out.print (".");
					System.out.flush ();
				}
			}
			System.out.println ();*/

			// we are done..yay!

		} catch (Exception e) { 
			e.printStackTrace ();
			System.err.println (" Exception in InitDB.create ()");
			return false;
		}

		return true;
	}

}

