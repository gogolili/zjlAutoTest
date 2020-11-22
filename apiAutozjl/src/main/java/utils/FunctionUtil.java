package utils;


import functions.Function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionUtil {
    private static final Map<String,Class<? extends Function>> functionMap = new HashMap<String, Class<? extends Function>>();
    static {
        functionMap.put("bodyfile", null);
        List<Class<?>> classes = ClassFinder.getAllAssignedClass(Function.class);//找到Function类所在文件夹下的所有文件类
        for (Class<?> clazz : classes) {
            try {
                Function tempFunc = (Function) clazz.newInstance();
                String referrncekey = tempFunc.getReferenceKey();
                if (referrncekey.length() > 0) {
                    functionMap.put(referrncekey, tempFunc.getClass());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isFunction(String functionName){
        return functionMap.containsKey(functionName);
    }

    public static String getValue(String functionName,String[] args){
        try {
            return functionMap.get(functionName).newInstance().execute(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
