const { io } = require('socket.io-client')
const socket = io('http://localhost:3000')
const readline = require('readline')

//singleton readline
let rl = null

/**
 * @description
 * 1. socket.on('connect') is triggered when the client is connected to the server
 * 2. socket.on('chat message') is triggered when the server sends a message to the client
 */
socket.on('connect', () => {
    console.log('connected')

    //first message trigger
    sendMsg()
})

socket.on('chat message', (msg) => {
    console.log('\nServer: ' + msg)

    //message trigger for every message received from server for continuous chat
    sendMsg()
})

/**
 * @description
 * Method for sending messages to server
 */
const sendMsg = () => {
    //closing the previous readline
    if (rl) rl.close()

    //creating a new readline
    rl = readline.createInterface({
        input: process.stdin, output: process.stdout,
    })
    rl.question('>> ', (msg) => {
        socket.emit('chat message', msg)

        //message trigger for every message sent from client for continuous chat
        sendMsg()
    })
}
