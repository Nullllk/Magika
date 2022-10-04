package ru.iamdvz.magika.utils;

public class ParserUtil {
    public static String getFrom(String str, String charOne, String charTwo) {
        if (str.contains(charOne) || str.contains(charTwo)) {
            return str.substring(str.indexOf(charOne)+1, str.lastIndexOf(charTwo));
        }
        return null;
    }
    public static String getFrom(String str) {
        if (str.contains("{") && str.contains("}")) {
            return str.substring(str.indexOf("{")+1, str.lastIndexOf("}"));
        }
        return null;
    }
    public static String getParameter(String str) {
        if (str.contains(":")) {
            return str.substring(str.indexOf(":")+1);
        }
        return null;
    }
    public static String getParameterFromArr(String[] strArr, String parameterName) {
        for (String str : strArr) {
            if (str.contains(parameterName)) {
                return str.substring(str.indexOf(":")+1);
            }
        }
        return null;
    }
    public static String getParameter(String str, String parameterName) {
        if (str.contains(parameterName)) {
            return str.substring(str.indexOf(parameterName)+1, str.indexOf(","));
        }
        return null;
    }
    public static String getParameter(String str, String parameter, String separator) {
        if (str.contains(parameter)) {
            return str.substring(str.indexOf(separator)+1);
        }
        return null;
    }
    public static String[] getParameters(String str) {
        return str.split(",");
    }
}
