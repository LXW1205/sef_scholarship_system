package com.scholarship.utils;

public class JsonUtils {

    public static String extractValue(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1)
            return null;

        int colonIndex = json.indexOf(":", keyIndex + search.length());
        if (colonIndex == -1)
            return null;

        int start = colonIndex + 1;
        while (start < json.length() && (Character.isWhitespace(json.charAt(start)))) {
            start++;
        }

        if (start >= json.length())
            return null;

        int end = start;
        char firstChar = json.charAt(start);

        if (firstChar == '"') {
            start++;
            end = json.indexOf("\"", start);
        } else if (firstChar == '[') {
            int bracketCount = 0;
            for (int i = start; i < json.length(); i++) {
                if (json.charAt(i) == '[')
                    bracketCount++;
                else if (json.charAt(i) == ']') {
                    bracketCount--;
                    if (bracketCount == 0) {
                        end = i + 1;
                        break;
                    }
                }
            }
        } else if (firstChar == '{') {
            int braceCount = 0;
            for (int i = start; i < json.length(); i++) {
                if (json.charAt(i) == '{')
                    braceCount++;
                else if (json.charAt(i) == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        end = i + 1;
                        break;
                    }
                }
            }
        } else {
            // Find end of number or boolean
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']'
                    && !Character.isWhitespace(json.charAt(end))) {
                end++;
            }
        }

        if (end == -1 || end <= start)
            return null;

        return json.substring(start, end).trim();
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
