import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Utils {
    /**
     * Method to generate a unique id
     *
     * @return unique id
     */
    public static String generateId() {
        return UUID.randomUUID().toString();
    }


    /**
     * Method to convert a message to a map
     *
     * @param data data to convert
     * @return convert  map
     */
    public static Map<String, String> messageToMap(String data) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = data.substring(1, data.length() - 1).split(", ");

        for (String pair : pairs) {
            String[] keyValue = pair.trim().split("=");
            map.put(keyValue[0], keyValue[1]);
        }
        return map;
    }
}
