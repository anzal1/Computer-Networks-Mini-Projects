import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.*
import kotlin.system.exitProcess

object Client {
    //variables for the client
    var socket: Socket? = null
    private var clientId: String? = null
    private var clientName: String? = null
    var reader: BufferedReader? = null
    var writer: BufferedWriter? = null

    /**
     * Method to send a message to the server
     */
    private fun sendMessageToServer() {
        try {
            //get message from console to send
            val scanner = Scanner(System.`in`)
            var msg: String?
            while (socket!!.isConnected) {
                msg = scanner.nextLine()
                val payload = mutableMapOf<String?, String?>()
                payload[Server.KEY_MESSAGE] = msg
                payload[Server.KEY_USER_ID] = clientId
                payload[Server.KEY_USER_NAME] = clientName
                payload[Server.KEY_TYPE] = Server.METHOD_SEND_MSG
                writer!!.write(payload.toString())
                writer!!.newLine()
                writer!!.flush()
            }
        } catch (e: Exception) {
            println("Error sending message to server")
        }
    }

    /**
     * Method to read messages from the server
     */
    fun listenFromServer() {
        while (socket!!.isConnected) {
            try {
                //read data from server
                val data = reader!!.readLine()

                //parse the data to a map
                val payload = Utils.messageToMap(data)

                //if method get id
                if (payload[Server.KEY_TYPE] == Server.METHOD_GET_ID) {
                    getIdFromServer(payload)
                } else if (payload[Server.KEY_TYPE] == Server.METHOD_SEND_MSG) {
                    getMessageFromServer(payload)
                }
            } catch (e: Exception) {
                println("Server Disconnected")
                exitProcess(0)
            }
        }
    }

    /**
     * Method to get id from the server
     *
     * @param payload payload to get the id from
     */
    private fun getIdFromServer(payload: Map<String, String>) {
        clientId = payload[Server.KEY_USER_ID]

        //name is null then stop execution here
        if (getName() == null) return

        Thread { sendMessageToServer() }.start()
        println("Entered the global chat")
    }

    /**
     * Method for getting a message from the server
     *
     * @param payload payload to get the message from
     */
    private fun getMessageFromServer(payload: Map<String, String>) {
        val sender = payload[Server.KEY_USER_NAME]
        val msg = payload[Server.KEY_MESSAGE]
        println("$sender: $msg")
    }

    /**
     * Method to send a new user to the server
     */
    private fun getName(): String? {
        val scanner = Scanner(System.`in`)
        println("Enter your name: ")
        clientName = scanner.nextLine()
        return try {
            val payload: MutableMap<String, String?> = HashMap()
            payload[Server.KEY_USER_ID] = clientId
            payload[Server.KEY_USER_NAME] = clientName
            payload[Server.KEY_TYPE] = Server.METHOD_NEW_USER
            writer!!.write(payload.toString())
            writer!!.newLine()
            writer!!.flush()

            //return value
            clientName!!
        } catch (e: Exception) {
            println("Error entering the global chat")

            //return value
            null
        }
    }

}

/**
 * Main method to start the client
 */

fun main() {
    val socket: Socket
    try {
        socket = Socket("localhost", 8080)
        println("Connected to server")
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
        Client.socket = socket
        Client.reader = reader
        Client.writer = writer
        Client.listenFromServer()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}