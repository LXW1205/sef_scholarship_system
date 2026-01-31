package scholarship.test;

import org.junit.jupiter.api.Test;
import scholarship.utils.JsonUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilsTest {

    @Test
    void testExtractValue() {
        String json = "{\"name\": \"John\", \"age\": 30, \"isStudent\": false, \"hobbies\": [\"reading\", \"coding\"]}";

        assertEquals("John", JsonUtils.extractValue(json, "name"));
        assertEquals("30", JsonUtils.extractValue(json, "age"));
        assertEquals("false", JsonUtils.extractValue(json, "isStudent"));
        assertEquals("[\"reading\", \"coding\"]", JsonUtils.extractValue(json, "hobbies"));
        assertNull(JsonUtils.extractValue(json, "nonexistent"));
    }

    @Test
    void testExtractValueNested() {
        String json = "{\"user\": {\"id\": 1, \"username\": \"jdoe\"}, \"status\": \"active\"}";
        assertEquals("{\"id\": 1, \"username\": \"jdoe\"}", JsonUtils.extractValue(json, "user"));
        assertEquals("active", JsonUtils.extractValue(json, "status"));
    }

    @Test
    void testEscape() {
        assertEquals("Hello \\\"World\\\"", JsonUtils.escape("Hello \"World\""));
        assertEquals("Line1\\nLine2", JsonUtils.escape("Line1\nLine2"));
        assertEquals("\\\\path\\\\to\\\\file", JsonUtils.escape("\\path\\to\\file"));
    }

    @Test
    void testToJson() {
        assertEquals("\"Hello\"", JsonUtils.toJson("Hello"));
        assertEquals("123", JsonUtils.toJson(123));
        assertEquals("true", JsonUtils.toJson(true));

        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("[\"a\",\"b\",\"c\"]", JsonUtils.toJson(list));

        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        assertEquals("{\"key\":\"value\"}", JsonUtils.toJson(map));

        assertNull(JsonUtils.extractValue(null, "key"));
    }
}
