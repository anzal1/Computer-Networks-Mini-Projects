import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket

class Server internal constructor(private var serverSocket: ServerSocket) {
    // Client class for storing client information
    data class Client(
        var clientId: String? = null,
        var clientName: String? = null,
        var reader: BufferedReader? = null,
        var writer: BufferedWriter? = null,
        var socket: Socket? = null,
    )

    // Room class for storing room information
    data class Room(
        var roomId: String? = null, var clients: MutableList<Client>? = null
    )

    companion object {
        // Constants
        const val METHOD_GET_ID = "get id"
        const val METHOD_NEW_USER = "new user"
        const val METHOD_SEND_MSG = "send message"
        const val METHOD_CREATE_ROOM = "create room"
        const val METHOD_JOIN_ROOM = "join room"
        const val KEY_TYPE = "type"
        const val KEY_USER_ID = "userId"
        const val KEY_USER_NAME = "userName"
        const val KEY_MESSAGE = "message"
        const val KEY_ROOM_ID = "roomId"
    }

    // List of rooms
    private var rooms = mutableListOf<Room>()

    /**
     * Method to start the server
     */
    fun startServer() {
        //infinite loop for accepting new clients
        while (!serverSocket.isClosed) {
            try {
                //wait for client to connect
                val socket = serverSocket.accept()

                //generate a unique id for the client and send it to the client
                val clientId = Utils.generateId()

                //create a new client
                val client = Client(
                    clientId,
                    null,
                    BufferedReader(InputStreamReader(socket.getInputStream())),
                    BufferedWriter(OutputStreamWriter(socket.getOutputStream())),
                    socket
                )

                sendIDToClient(client)

                //forward each client to a new thread
                Thread {
                    while (!client.socket!!.isClosed) {
                        try {
                            //read data from client
                            val data = client.reader!!.readLine()

                            //parse the data to a map
                            val map = Utils.messageToMap(data)

                            //if method new user
                            if (map[KEY_TYPE] == METHOD_NEW_USER) {
                                newClient(map)
                            } else if (map[KEY_TYPE] == METHOD_SEND_MSG) {
                                sendChat(map)
                            } else if (map[KEY_TYPE] == METHOD_CREATE_ROOM) {
                                createRoom(client)
                            } else if (map[KEY_TYPE] == METHOD_JOIN_ROOM) {
                                joinRoom(map, client)
                            }
                        } catch (e: Exception) {
                            disconnectClient(client)
                        }
                    }
                }.start()
            } catch (e: Exception) {
                println("Server Disconnected")
            }
        }
    }

    /**
     * Method for sending id to the client
     *
     * @param client client to send the id to
     */
    private fun sendIDToClient(client: Client) {
        try {
            val map = mutableMapOf<String?, String?>()
            map[KEY_USER_ID] = client.clientId
            map[KEY_USER_NAME] = client.clientName
            map[KEY_TYPE] = METHOD_GET_ID
            client.writer!!.write(map.toString())
            client.writer!!.newLine()
            client.writer!!.flush()
        } catch (e: Exception) {
            println("Error sending id to client")
        }
    }

    /**
     * Method to send a unique id to the client
     *
     * @param map map containing the client information
     */
    private fun newClient(map: Map<String, String>) {
        //get client info from map
        val senderId = map[KEY_USER_ID]
        val senderName = map[KEY_USER_NAME]
        val roomId = map[KEY_ROOM_ID]

        val room = rooms.find { it.roomId == roomId }
        val roomIndex = rooms.indexOf(room)

        //setting the client name in client list in the room
        val clients = rooms[roomIndex].clients
        for (i in clients!!.indices) {
            if (clients[i].clientId == senderId) {
                clients[i].clientName = senderName
                break
            }
        }

        //updating the room list as update only happens in the copy of client list
        val updatedRoom = Room(
            roomId, clients
        )
        rooms[roomIndex] = updatedRoom
        println("Client Connected: $senderName")
        //broadcast the new client to all the clients
        broadcastMessage("joined the chat", senderId, senderName, clients)
    }

    /**
     * Method to send a message to all the clients
     *
     * @param map map containing the message information
     */
    private fun sendChat(map: Map<String, String>) {
        //getting sender info from map
        val senderId = map[KEY_USER_ID]
        val senderName = map[KEY_USER_NAME]
        val roomId = map[KEY_ROOM_ID]
        val msg = map[KEY_MESSAGE]

        //getting the room, if the room is not found then return
        val room = rooms.find { it.roomId == roomId } ?: return

        //getting client list from the room
        val clients = room.clients

        //broadcast the message to all the clients
        broadcastMessage(msg, senderId, senderName, clients)
    }

    private fun createRoom(client: Client) {

        //generate a unique room id in range 1000-9999
        val roomId = Utils.generateRoomId(rooms)

        //create a new room with the generated id
        val room = Room(
            roomId,
            //also create client list and add the client to the list
            mutableListOf(client)
        )

        rooms.add(room)
        try {
            //send the room id to the client
            val response = mutableMapOf<String?, String?>()
            response[KEY_TYPE] = METHOD_CREATE_ROOM
            response[KEY_ROOM_ID] = roomId
            client.writer!!.write(response.toString())
            client.writer!!.newLine()
            client.writer!!.flush()
        } catch (e: Exception) {
            println("Error sending room id to client")
        }
    }

    private fun joinRoom(map: Map<String, String>, client: Client) {

        //get room id from map to which the client wants to join
        val roomId = map[KEY_ROOM_ID]

        //for checking if the room exists
        val room = rooms.find { it.roomId == roomId }

        //if room exists
        if (room != null) {
            for (i in rooms.indices) {
                if (rooms[i].roomId == roomId) {
                    //add the client to the client list of the room
                    rooms[i].clients!!.add(client)
                    break
                }
            }
            try {
                //send the room id to the client with success message
                val response = mutableMapOf<String?, String?>()
                response[KEY_TYPE] = METHOD_JOIN_ROOM
                response[KEY_ROOM_ID] = roomId
                response[KEY_MESSAGE] = "success"
                client.writer!!.write(response.toString())
                client.writer!!.newLine()
                client.writer!!.flush()
            } catch (e: Exception) {
                println("Error sending room id to client")
            }
        }
        //if room does not exist
        else {
            try {
                //send failure message to the client
                val response = mutableMapOf<String?, String?>()
                response[KEY_TYPE] = METHOD_JOIN_ROOM
                response[KEY_MESSAGE] = "fail"
                client.writer!!.write(response.toString())
                client.writer!!.newLine()
                client.writer!!.flush()
            } catch (e: Exception) {
                println("Error sending room id to client")
            }
        }
    }

    /**
     * Method to disconnect a client
     *
     * @param client client to disconnect
     */
    private fun disconnectClient(client: Client) {
        //client disconnected before joining any room
        if (client.clientName == null) {
            return
        }
        println("Client Disconnected: ${client.clientName}")

        //label to break out of the outer loop
        run breakLoop@{

            //iterate through all the rooms for searching the client to disconnect
            rooms.forEach { room ->

                //iterate through all the clients in the current room
                val clients = room.clients
                val clientIndex = clients!!.indexOfFirst { it.clientId == client.clientId }

                //if client is not found in the current room then continue to next room
                if (clientIndex == -1) return@forEach

                //remove the client from the client list
                clients.removeAt(clientIndex)

                //update the room list as update only happens in the copy of client list
                val updatedRoom = Room(
                    room.roomId, clients
                )
                rooms[rooms.indexOf(room)] = updatedRoom

                //broadcast the client disconnect to all the clients in the room
                broadcastMessage("left the chat", client.clientId, client.clientName, clients)

                //if client list is empty then remove the room from the room list
                if (clients.size == 0) {
                    rooms.remove(room)
                    println("Room ${room.roomId} deleted")

                    //break out of the outer loop
                    return@breakLoop
                }

            }
        }
        try {
            client.socket!!.close()

            //terminate the thread of the client
            Thread.currentThread().join()
        } catch (ex: Exception) {
            println("Error in disconnecting client")
        }
    }

    /**
     * Method to broadcast a message to all the clients
     *
     * @param message    message to broadcast
     * @param senderId   id of the sender
     * @param senderName name of the sender
     */
    private fun broadcastMessage(message: String?, senderId: String?, senderName: String?, clients: List<Client>?) {
        clients!!.forEach { client ->
            //send the message to all the clients except the sender
            if (client.clientId == senderId) return@forEach
            try {
                val map = mutableMapOf<String?, String?>()
                map[KEY_TYPE] = METHOD_SEND_MSG
                map[KEY_MESSAGE] = message
                map[KEY_USER_NAME] = senderName
                client.writer!!.write(map.toString())
                client.writer!!.newLine()
                client.writer!!.flush()
            } catch (e: Exception) {
                println("Error broadcasting message")
            }
        }
    }


}

/**
 * Main method to start the server
 */
fun main() {
    val serverSocket: ServerSocket
    try {
        //create a new server socket
        serverSocket = ServerSocket(8080)
        println("Server running on port 8080")

        //create a new server
        val server = Server(serverSocket)
        server.startServer()
    } catch (e: Exception) {
        println("Error starting server on port 8080")
    }
}