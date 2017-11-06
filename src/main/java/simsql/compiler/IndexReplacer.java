

/**
 * *
 * Copyright 2014 Rice University                                           *
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 * *
 * http://www.apache.org/licenses/LICENSE-2.0                           *
 * *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 * *
 */


/**
 *
 */
package simsql.compiler; // package mcdb.compiler;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.StringBuffer;

public class IndexReplacer {

    private String query;

    public IndexReplacer(String inQuery, simsql.shell.Catalog catalog) {

		/*
                 * Pre-process the input text; if we see anything of the form myTable[34] we look
                 *    for myTable_34_saved in the catalog, and if there, we simply do a text replace
                 */

        // this will match any random table statements
        Pattern p = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*\\s*(\\[\\s*[0-9]+\\s*\\])+");
        query = inQuery;
        Matcher m = p.matcher(query);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {

            // we found a match, so see if the table exists in the catalog
            String current = m.group();
            String tableName = MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(current.replaceAll("\\s", ""));
            tableName += "_saved";

            // OK, so we have the table name... see if it exists in the catalog
            if (catalog.getRelation(tableName) != null) {

                // it is there, so we replace with the table name
                m.appendReplacement(sb, tableName);
            } else {

                // it is not there, so we put it back
                m.appendReplacement(sb, current);
            }
        }
        m.appendTail(sb);
        query = sb.toString();

    }

    public String getOutput() {
        return query;
    }
}

