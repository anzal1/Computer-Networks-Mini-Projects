const WebSocket = require('ws');
const socket = new WebSocket('ws://localhost:8080');
const readline = require('readline');

//singleton readline interface
let rl;
let isUsernameSaved = false;
let userId;

/**
 * @description
 * 1. socket.on('open') is triggered when the client is connected to the server
 * 2. socket.on('message') is triggered when the client receives a message from the server
 * 3. socket.on('error') is triggered when the client encounters an error
 * 4. socket.on('close') is triggered when the server is disconnected from the client
 */
socket.on('open', () => {
    console.log('connected');
})

socket.on('message', (data) => {
    //parse the data to get the payload
    const payload = JSON.parse(data);
    const method = payload.method;

    //if the method is new user, save the id for future requests
    if (method === "get id") {
        userId = payload.id;

        //get username after getting the id
        getName();
    }
    //if the method is chat message, print the message
    else if (method === "chat message") {
        console.log(`\n${payload.name}: ${payload.msg}`);

        /**
         * when multiple users joins at the same time,
         * then the streams of other users gets forwarded
         * and name of other users cannot be saved so,
         * it keeps asking for name on every msg received
         */
        if (!isUsernameSaved) getName();
        // otherwise, message prompt will continue to be shown for every msg received
        else sendMsg();
    }
})

socket.on('error', () => {
    console.log("Error Connecting to Server");
})

socket.on('close', () => {
    console.log("\nServer Disconnected");
    process.exit();
})


/**
 * @description
 * Method for sending messages to server
 */
const sendMsg = () => {
    //close the previous readline interface
    if (rl) rl.close();

    //create a new readline interface
    rl = readline.createInterface({
        input: process.stdin, output: process.stdout,
    });
    rl.question(">> ", (msg) => {
        const payload = {
            method: "chat message", msg, id: userId
        }
        socket.send(JSON.stringify(payload));

        //message prompt will continue to be shown for every msg sent for continuous chat
        sendMsg();
    });
};

/**
 * @description
 * Method for getting username from user
 */
const getName = () => {
    //close the previous readline interface
    if (rl) rl.close();

    //create a new readline interface
    rl = readline.createInterface({
        input: process.stdin, output: process.stdout,
    });
    rl.question("Enter your name: ", (name) => {

        const payload = {
            method: "new user", name, id: userId
        }
        socket.send(JSON.stringify(payload));

        //save the name
        isUsernameSaved = true;

        console.log("Entered in global chat");
        //start the message prompt for this user
        sendMsg();
    });
}
