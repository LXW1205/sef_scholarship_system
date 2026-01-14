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
        while (start < json.length() && (Character.isWhitespace(json.charAt(start)) || json.charAt(start) == '"')) {
            start++;
        }

        int end = start;
        boolean inQuotes = json.charAt(start - 1) == '"';

        if (inQuotes) {
            end = json.indexOf("\"", start);
        } else {
            // Find end of number or boolean
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}'
                    && !Character.isWhitespace(json.charAt(end))) {
                end++;
            }
        }

        if (end == -1)
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
