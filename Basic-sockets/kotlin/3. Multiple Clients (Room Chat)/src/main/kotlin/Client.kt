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
    private var roomId: String? = null
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
                payload[Server.KEY_ROOM_ID] = roomId.toString()
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
                    getClientIdFromServer(payload)
                } else if (payload[Server.KEY_TYPE] == Server.METHOD_SEND_MSG) {
                    getMessageFromServer(payload)
                } else if (payload[Server.KEY_TYPE] == Server.METHOD_CREATE_ROOM) {
                    getRoomIdFromServer(payload)
                } else if (payload[Server.KEY_TYPE] == Server.METHOD_JOIN_ROOM) {
                    joinRoomFromServer(payload)
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
    private fun getClientIdFromServer(payload: Map<String, String>) {
        //get the id from the payload
        clientId = payload[Server.KEY_USER_ID]

        //now show the menu
        showChoices()
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
     * Method to get room id from the server
     *
     * @param payload payload to get the room id from
     */
    private fun getRoomIdFromServer(payload: Map<String, String>) {
        //get the room id from the payload
        roomId = payload[Server.KEY_ROOM_ID]
        println("Room $roomId created")

        //now get the name of the user
        //if name is null then stop execution here
        if (getName() == null) return

        //start sending messages to the server
        Thread { sendMessageToServer() }.start()
        println("Entered in the chat room")
    }

    /**
     * Method to join a room from the server
     *
     * @param payload payload to get the room id from
     */
    private fun joinRoomFromServer(payload: Map<String, String>) {
        //get message from the payload
        val msg = payload[Server.KEY_MESSAGE]
        roomId = payload[Server.KEY_ROOM_ID]

        //if room exists
        if (msg == "fail") {
            println("Room not found")

            //show choices again
            showChoices()

            // don't go further after showChoices recursion call returns
            return
        } else {
            println("Room $roomId joined")
        }

        //now get the name of the user
        //if name is null then stop execution here
        if (getName() == null) return

        //start sending messages to the server
        Thread { sendMessageToServer() }.start()
        println("Entered in the chat room")
    }//send the name to the server//get name from console

    /**
     * Method to send a new user to the server
     */
    private fun getName(): String? {
        //get name from console
        val scanner = Scanner(System.`in`)
        println("Enter your name")
        clientName = scanner.nextLine()
        return try {
            //send the name to the server
            val payload = mutableMapOf<String?, String?>()
            payload[Server.KEY_USER_ID] = clientId
            payload[Server.KEY_USER_NAME] = clientName
            payload[Server.KEY_ROOM_ID] = roomId.toString()
            payload[Server.KEY_TYPE] = Server.METHOD_NEW_USER
            writer!!.write(payload.toString())
            writer!!.newLine()
            writer!!.flush()

            clientName!!
        } catch (e: Exception) {
            println("Error sending name to server")

            null
        }
    }

    /**
     * Method to show the choices to the user
     */
    private fun showChoices() {
        println("1. Create Room")
        println("2. Join Room")
        println("3. Exit")
        val scanner = Scanner(System.`in`)
        when (scanner.nextInt()) {
            1 -> createRoom()
            2 -> joinRoom()
            3 -> exitProcess(0)
            else -> {
                println("Invalid choice")
                showChoices()
            }
        }
    }

    /**
     * Method to create a room
     */
    private fun createRoom() {
        try {
            //send req to the server to create a room
            val payload = mutableMapOf<String?, String?>()
            payload[Server.KEY_TYPE] = Server.METHOD_CREATE_ROOM
            writer!!.write(payload.toString())
            writer!!.newLine()
            writer!!.flush()
        } catch (e: Exception) {
            println("Error sending request to create room")
        }
    }

    /**
     * Method to join a room
     */
    private fun joinRoom() {
        //get room id from console
        val scanner = Scanner(System.`in`)
        println("Enter room id: ")
        val roomId = scanner.nextLine()
        try {
            //send req to the server to join a room
            val payload = mutableMapOf<String?, String?>()
            payload[Server.KEY_TYPE] = Server.METHOD_JOIN_ROOM
            payload[Server.KEY_ROOM_ID] = roomId
            writer!!.write(payload.toString())
            writer!!.newLine()
            writer!!.flush()
        } catch (e: Exception) {
            println("Error sending request to join room")
        }
    }


}

/**
 * Main method to start the client
 */
fun main() {
    val socket: Socket
    try {
        //connect to the server
        socket = Socket("localhost", 8080)
        println("Connected to server")
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
        Client.socket = socket
        Client.reader = reader
        Client.writer = writer

        //start listening to the server
        Client.listenFromServer()
    } catch (e: Exception) {
        println("Error connecting to server")
    }
}