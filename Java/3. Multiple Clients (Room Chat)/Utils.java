import java.util.HashMap;
import java.util.List;
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
     * Method to generate a unique room id in range 1000-9999
     *
     * @param roomsList list of rooms
     * @return unique room id
     */
    public static int generateRoomId(List<Server.Room> roomsList) {
        int min = 1000;
        int max = 10000;
        int roomId = (int) (Math.random() * (max - min) + min);

        for (Server.Room room : roomsList) {
            if (roomId == Integer.parseInt(room.roomId)) {
                return generateRoomId(roomsList);
            }
        }

        return roomId;
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
