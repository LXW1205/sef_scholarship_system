package com.scholarship.utils;

public class JsonUtils {

    public static String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return null;
        
        start += search.length();
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) {
            start++;
        }
        
        int end = json.indexOf("\"", start);
        if (end == -1) end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        
        if (end == -1) return null;
        
        return json.substring(start, end).trim();
    }
    
    
    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
    
    // Alias for consistency with other code
    public static String extractJsonValue(String json, String key) {
        return extractValue(json, key);
    }
}
