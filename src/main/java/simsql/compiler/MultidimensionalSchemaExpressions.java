package simsql.compiler;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static simsql.compiler.MultidimensionalSchemaIndices.labelingOrder;

public class MultidimensionalSchemaExpressions {

    private HashMap<String, String> expressionList;

    public MultidimensionalSchemaExpressions(String parseString) {

        this.expressionList = new HashMap<String, String>();

        Pattern pattern = Pattern.compile("\\[(.+?)\\]");
        Matcher matcher = pattern.matcher(parseString);

        // find all expressions and put them into the expressionList...
        int i = 0;
        while (matcher.find()) {
            String exp = matcher.group();
            this.expressionList.put(labelingOrder[i], exp.substring(1, exp.length()-1));
            i++;
        }
    }

    public HashMap<String, Integer> evaluateExpressions(HashMap<String, Integer> indices) {

        MPNGenerator mpn = new MPNGenerator();
        HashMap<String, Integer> ret = new HashMap<String, Integer>();

        for(String index : expressionList.keySet()) {
            int value = (int)mpn.compute(expressionList.get(index), indices);
            ret.put(index, value);
        }

        return ret;
    }
}
