const { io } = require("socket.io-client");
const socket = io("http://localhost:3000");
const readline = require("readline");

//singleton readline interface
let rl = null;
let isUsernameSaved = null;

/**
 * @description
 * 1. socket.on('connect') is triggered when the client is connected to the server
 * 2. socket.on('chat message') is triggered when the server sends a message to the client
 * 3. socket.on('new user') is triggered when the server sends a message to the client
 * 4. socket.on('disconnect') is triggered when the server is disconnected from the client
 */
socket.on("connect", () => {
    console.log("connected");

    //get username on startup
    getName();
});

socket.on("chat message", (payload) => {
    //print every msg received from server
    console.log(`\n${payload.name}: ${payload.msg}`);

    /**
     * when multiple users joins at the same time,
     * then the streams of other users gets forwarded
     * and name of other users cannot be saved so,
     * it keeps asking for name on every msg received
     */
    if (isUsernameSaved == null) getName();
    // otherwise, message prompt will continue to be shown for every msg received
    else sendMsg();
});

socket.on("disconnect", () => {
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
        socket.emit("chat message", msg);

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
        socket.emit("new user", name);

        //save the name
        isUsernameSaved = name;

        console.log("Entered in the chat");
        //start the message prompt for this user
        sendMsg();
    });
}
