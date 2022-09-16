package ru.iamdvz.magika.utils;

import org.bukkit.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class getFromBrackets {
    public static String get(String str) {
        Pattern ptrn = Pattern.compile("(\\((.*?)\\))");
        Matcher mtchr = ptrn.matcher(str);
        if (mtchr.find()) {
            return mtchr.group(2);
        }
        return null;
    }
}