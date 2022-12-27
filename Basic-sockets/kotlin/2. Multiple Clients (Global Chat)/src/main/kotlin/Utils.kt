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
         * Method to convert a message to a map
         *
         * @param data data to convert
         * @return convert  map
         */
        fun messageToMap(data: String): Map<String, String> {
            val map = mutableMapOf<String, String>()
            val pairs = data.substring(1, data.length - 1).split(", ")
            for (pair in pairs) {
                val keyValue = pair.trim { it <= ' ' }.split("=")
                map[keyValue[0]] = keyValue[1]
            }
            return map
        }
    }
}