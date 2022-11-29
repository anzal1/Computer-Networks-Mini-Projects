const { Server } = require('socket.io')
const io = new Server(3000)
const readline = require("readline");

//singleton readline
let rl = null

/**
 * @description
 * 1. io.on('connection') is triggered when the client is connected to the server
 * 2. socket.on('chat message') is triggered when the client sends a message to the server
 */
io.on('connection', (socket) => {
    console.log('a user connected')

    //first message trigger
    sendMsg()

    socket.on('chat message', (msg) => {
        console.log('\nClient: ' + msg)

        //message trigger for every message received from client for continuous chat
        sendMsg()
    })
})

/**
 * @description
 * Method for sending messages to client
 */
const sendMsg = () => {
    //closing the previous readline
    if (rl) rl.close()

    //creating a new readline
    rl = readline.createInterface({
        input: process.stdin, output: process.stdout,
    })
    rl.question('>> ', (msg) => {
        io.emit('chat message', msg)

        //message trigger for every message sent from server for continuous chat
        sendMsg()
    })
}