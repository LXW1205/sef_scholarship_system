package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Alias for consistency with other code
    public static String extractJsonValue(String json, String key) {
        return extractValue(json, key);
    }

    public static Map<String, String> parseBody(InputStream requestBody) throws IOException {
        String json = new BufferedReader(new InputStreamReader(requestBody, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isEmpty())
            return map;

        // Very basic parser - assumes flat JSON object string:string
        // This is fragile but sufficient for this project's constraints
        // Ideally use a library like Jackson or Gson

        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
        }

        // Split by comma, but not inside quotes (simplified)
        // A robust regex or state machine is better, but here's a simple split
        // Since we control the input primarily, assuming standard JSON structure

        // Manual parsing loop
        int start = 0;
        boolean inQuote = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
            }
            if (c == ',' && !inQuote) {
                parsePair(json.substring(start, i), map);
                start = i + 1;
            }
        }
        parsePair(json.substring(start), map);

        return map;
    }

    private static void parsePair(String pair, Map<String, String> map) {
        String[] parts = pair.split(":", 2);
        if (parts.length == 2) {
            String key = cleanString(parts[0]);
            String val = cleanString(parts[1]);
            map.put(key, val);
        }
    }

    private static String cleanString(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
            s = s.replace("\\\"", "\"");
        }
        return s;
    }

    public static String toJson(Object o) {
        if (o == null)
            return "null";
        if (o instanceof String)
            return "\"" + escape((String) o) + "\"";
        if (o instanceof Number || o instanceof Boolean)
            return o.toString();

        if (o instanceof List) {
            List<?> list = (List<?>) o;
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0)
                    sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }

        if (o instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) o;
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (i > 0)
                    sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":");
                sb.append(toJson(entry.getValue()));
                i++;
            }
            sb.append("}");
            return sb.toString();
        }

        return "\"" + o.toString() + "\""; // Fallback
    }

}
