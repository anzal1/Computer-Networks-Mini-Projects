import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.system.exitProcess

private var serverSocket: ServerSocket? = null

fun main() {
    try {
        // Create a server socket
        serverSocket = ServerSocket(1234)
        println("Server is running...")

        // Listen for a connection request
        val socket = serverSocket!!.accept()

        // Separate Thread for reading from the client
        Thread {
            //infinite loop to keep reading from the client
            while (socket.isConnected) {
                try {
                    // Create an input stream to receive data from the client
                    val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    val str = bufferedReader.readLine()

                    // Display to the console
                    println("Client: $str")
                } catch (e: IOException) {
                    println("Client Disconnected")

                    // Close the socket
                    closeSocket(socket)
                }
            }
        }.start()

        // Separate Thread for writing to the client
        Thread {
            //infinite loop to keep writing to the client
            while (socket.isConnected) {
                try {
                    // Create an input stream to receive data from the console
                    val bufferedReader = BufferedReader(InputStreamReader(System.`in`))

                    // Create an output stream to send data to the client
                    val bufferedWriter = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                    bufferedWriter.write(bufferedReader.readLine())
                    bufferedWriter.newLine()

                    // Flush the stream, it is important to do this
                    bufferedWriter.flush()
                } catch (e: IOException) {
                    println("Error Writing To Client")

                    // Close the socket
                    closeSocket(socket)
                }
            }
        }.start()
    } catch (e: IOException) {
        println("Error Creating Server Socket")
    }
}

private fun closeSocket(socket: Socket) {
    try {
        socket.close()
        exitProcess(0)
    } catch (e: IOException) {
        println("Error Closing Socket")
    }
}
