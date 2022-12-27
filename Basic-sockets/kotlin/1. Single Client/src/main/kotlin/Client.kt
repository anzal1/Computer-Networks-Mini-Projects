import java.io.*
import java.net.Socket
import kotlin.system.exitProcess


private var socket: Socket? = null

fun main() {
    try {
        socket = Socket("localhost", 1234)
        println("Connected to Server")

        // Separate Thread for reading from the server
        Thread {
            //infinite loop to keep reading from the server
            while (socket!!.isConnected) {
                try {
                    // Create an input stream to receive data from the server
                    val bufferedReader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                    val str = bufferedReader.readLine()

                    // Display to the console
                    println("Server: $str")
                } catch (e: Exception) {
                    println("Server Disconnected")

                    // Close the socket
                    closeSocket(socket)
                }
            }
        }.start()

        // Separate Thread for writing to the server
        Thread {
            //infinite loop to keep writing to the server
            while (socket!!.isConnected) {
                try {
                    // Create an input stream to receive data from the console
                    val bufferedReader = BufferedReader(InputStreamReader(System.`in`))

                    // Create an output stream to send data to the server
                    val bufferedWriter = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
                    bufferedWriter.write(bufferedReader.readLine())
                    bufferedWriter.newLine()

                    // Flush the stream, it is important to do this
                    bufferedWriter.flush()
                } catch (e: Exception) {
                    println("Error Writing To Server")

                    // Close the socket
                    closeSocket(socket)
                }
            }
        }.start()
    } catch (e: IOException) {
        println("Error Connecting To Server")
    }
}

private fun closeSocket(socket: Socket?) {
    try {
        socket!!.close()
        exitProcess(0)
    } catch (e: IOException) {
        println("Error Closing Socket")
    }
}
