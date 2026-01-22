package com.scholarship.utils;

public class JsonUtils {

    public static String extractValue(String json, String key) {
        if (json == null || key == null)
            return null;
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1)
            return null;

        int colonIndex = json.indexOf(":", keyIndex + search.length());
        if (colonIndex == -1)
            return null;

        int start = colonIndex + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (start >= json.length())
            return null;

        char firstChar = json.charAt(start);
        int end;

        if (firstChar == '"') {
            start++;
            end = start;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') {
                    break;
                }
                end++;
            }
        } else if (firstChar == '[') {
            int bracketCount = 0;
            end = start;
            while (end < json.length()) {
                if (json.charAt(end) == '[')
                    bracketCount++;
                else if (json.charAt(end) == ']')
                    bracketCount--;

                end++;
                if (bracketCount == 0)
                    break;
            }
        } else if (firstChar == '{') {
            int braceCount = 0;
            end = start;
            while (end < json.length()) {
                if (json.charAt(end) == '{')
                    braceCount++;
                else if (json.charAt(end) == '}')
                    braceCount--;

                end++;
                if (braceCount == 0)
                    break;
            }
        } else {
            end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']'
                    && !Character.isWhitespace(json.charAt(end))) {
                end++;
            }
        }

        if (end > json.length())
            end = json.length();
        String val = json.substring(start, end).trim();
        // Remove escaping from quotes if it's a string
        if (firstChar == '"') {
            val = val.replace("\\\"", "\"");
        }
        return val;
    }

    public static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\"", "\\\"");
    }

    // Alias for consistency with other code
    public static String extractJsonValue(String json, String key) {
        return extractValue(json, key);
    }
}
