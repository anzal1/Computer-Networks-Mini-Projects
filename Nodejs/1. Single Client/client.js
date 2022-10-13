const WebSocket = require('ws');
const socket = new WebSocket('ws://localhost:8080');
const readline = require('readline');

//singleton readline interface
let rl;

/**
 * @description
 * 1. socket.on('open') is triggered when the client is connected to the server
 * 2. socket.on('message') is triggered when the client receives a message from the server
 * 3. socket.on('error') is triggered when the client encounters an error
 */
socket.on('open', () => {
    console.log('Connected to server');

    sendMsg()
});

socket.on('message', (message) => {
    console.log('\nServer: ' + message);

    //regex for integer only
    const intRegex = new RegExp(/^[-+]?[0-9]+$/);

    //regex for float only
    const floatRegex = new RegExp(/^[-+]?[0-9]+\.[0-9]+$/);

    //regex for alphanumeric with spaces only
    const alphaNumRegex = new RegExp(/^[a-zA-Z0-9 ]+$/);

    //regex for alphabets only
    const alphaRegex = new RegExp(/^[a-zA-Z]+$/);

    //regex for special characters
    const specialCharRegex = new RegExp(/[`!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/);


    //checking if the message is an integer
    if(intRegex.test(message)) {
        console.log('Integer');
    } else if(floatRegex.test(message)) {
        console.log('Float');
    } else if(alphaRegex.test(message)) {
        console.log('Alphabets');
    } else if(alphaNumRegex.test(message)) {    
        console.log('Alphanumeric with spaces');
    } else if(specialCharRegex.test(message)) {
        console.log('Special Characters');
    } else {
        console.log('Invalid');
    }

    //message trigger for every message received from server for continuous chat
    sendMsg()
});

socket.on('error', () => {
    console.log('Error connecting to server');
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
        socket.send(msg);

        //message trigger for every message sent from client for continuous chat
        sendMsg()
    })
}