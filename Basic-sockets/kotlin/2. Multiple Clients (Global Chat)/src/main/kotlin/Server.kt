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
        var socket: Socket? = null
    )

    companion object {
        // Constants
        const val METHOD_GET_ID = "get id"
        const val METHOD_NEW_USER = "new user"
        const val METHOD_SEND_MSG = "send message"
        const val KEY_TYPE = "type"
        const val KEY_USER_ID = "userId"
        const val KEY_USER_NAME = "userName"
        const val KEY_MESSAGE = "message"

    }

    // List of clients
    private var clients = mutableListOf<Client>()

    /**
     * Method to start the server
     */
    fun startServer() {
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
                clients.add(client)

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
        val clientId = map[KEY_USER_ID]
        val clientName = map[KEY_USER_NAME]

        //search for the client in the list
        val client = clients.find { it.clientId == clientId }
        val index = clients.indexOf(client)

        if (client != null) {
            client.clientName = clientName
            clients[index] = client
        } else {
            println("Client not found")
            return
        }

        println("Client Connected: $clientName")

        //broadcast the new client to all the clients
        broadcastMessage("joined the chat", clientId, clientName)
    }

    /**
     * Method to send a message to all the clients
     *
     * @param map map containing the message information
     */
    private fun sendChat(map: Map<String, String>) {
        val senderId = map[KEY_USER_ID]
        val senderName = map[KEY_USER_NAME]
        val msg = map[KEY_MESSAGE]

        //broadcast the message to all the clients
        broadcastMessage(msg, senderId, senderName)
    }

    /**
     * Method to disconnect a client
     *
     * @param client client to disconnect
     */
    private fun disconnectClient(client: Client) {
        //client disconnected before without entering a name
        if (client.clientName == null) {
            return
        }
        println("Client Disconnected: ${client.clientName}")

        //remove the client from the list
        clients.remove(client)

        //broadcast the client disconnection to all the clients
        broadcastMessage("left the chat", client.clientId, client.clientName)
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
    private fun broadcastMessage(message: String?, senderId: String?, senderName: String?) {
        clients.forEach { client ->
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