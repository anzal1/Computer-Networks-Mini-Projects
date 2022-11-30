import Server.Room
import java.util.*

class Utils {
    companion object {

        /**
         * Method to generate a unique id
         *
         * @return unique id
         */
        fun generateId(): String {
            return UUID.randomUUID().toString()
        }

        /**
         * Method to generate a unique room id in range 1000-9999
         *
         * @param roomsList list of rooms
         * @return unique room id
         */
        fun generateRoomId(roomsList: List<Room>): String {
            val min = 1000
            val max = 10000
            val roomId = (Math.random() * (max - min) + min).toInt().toString()

            //check if room id already exists
            return if (roomsList.any { roomId == it.roomId }) {
                //if room id already exists, generate a new one
                generateRoomId(roomsList)
            } else {
                //if room id does not exist, return it
                roomId
            }
        }

        /**
         * Method to convert a message to a map
         *
         * @param data data to convert
         * @return convert  map
         */
        fun messageToMap(data: String): Map<String, String> {
            val map = mutableMapOf<String, String>()
            val pairs =
                data.substring(1, data.length - 1).split(", ")
            for (pair in pairs) {
                val keyValue =
                    pair.trim { it <= ' ' }.split("=")
                map[keyValue[0]] = keyValue[1]
            }
            return map
        }
    }
}
