const { io } = require("socket.io-client");
const socket = io("http://localhost:3000");
const readline = require("readline");

//singleton readline interface
let rl = null;
let isUsernameSaved = null;
let roomId = null;

/**
 * @description
 * 1. socket.on('connect') is triggered when the client is connected to the server
 * 2. socket.on('chat message') is triggered when the server sends a message to the client
 * 3. socket.on('new user') is triggered when a new user joins the room
 * 4. socket.on('create room') is triggered when a room is created
 * 5. socket.on('join room') is triggered when a room is joined
 * 6. socket.on('disconnect') is triggered when the server is disconnected from the client
 */
socket.on("connect", () => {
    console.log("connected");

    //show the choice on startup to create or join a room
    choice();
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
    if (!isUsernameSaved) getName();
    // otherwise, message prompt will continue to be shown for every msg received
    else sendMsg();
});

socket.on('create room', (id) => {
    //save the room id received from server for future use
    roomId = id;
    console.log(`Room ${id} created`);

    //now get the username after creating the room
    getName();
});

socket.on("disconnect", () => {
    console.log("\nServer Disconnected");
    process.exit();
})

socket.on('join room', (payload) => {
    //checking if the room exists or not
    if (payload.msg === 'success') {
        console.log(`Room ${payload.roomId} joined`);

        //save the room id received from server for future use
        roomId = payload.roomId;

        //now get the username after joining the room
        getName();
    } else {
        console.log("Room not found");

        //if room not found, then ask for choice again
        choice();
    }
});

/**
 * @description
 * Method for showing the choice to create or join a room
 */
const choice = () => {
    console.log("1. Create room");
    console.log("2. Join room");
    console.log("3. Exit");

    //close the previous readline interface
    if (rl) rl.close();

    //create a new readline interface
    rl = readline.createInterface({
        input: process.stdin, output: process.stdout,
    });
    rl.question("Choose: ", (val) => {
        switch (val) {
            case "1":
                socket.emit("create room");
                break;
            case "2":
                joinRoom();
                break;
            case "3":
                process.exit();

            //if choice is not 1, 2 or 3, then ask again
            default:
                choice();
        }
    });
}

/**
 * @description
 * Method for joining a room
 */
const joinRoom = () => {
    //close the previous readline interface
    if (rl) rl.close();

    //create a new readline interface
    rl = readline.createInterface({
        input: process.stdin, output: process.stdout,
    });
    rl.question("Enter room id: ", (id) => {
        const payload = { name: isUsernameSaved, roomId: id };

        //emit the join room event to the server
        socket.emit("join room", payload);
    });
}

/**
 * @description
 * Method for getting the username
 */
const getName = () => {
    //close the previous readline interface
    if (rl) rl.close();

    //create a new readline interface
    rl = readline.createInterface({
        input: process.stdin, output: process.stdout,
    });
    rl.question("Enter your name: ", (name) => {
        const payload = { name, roomId };

        //emit the new user event to the server
        socket.emit("new user", payload);
        //save the username for future use
        isUsernameSaved = name;

        console.log("Entered in the chat room");
        //start the message prompt for this user
        sendMsg();
    });
}

/**
 * @description
 * Method for sending the message
 */
const sendMsg = () => {
    //close the previous readline interface
    if (rl) rl.close();

    //create a new readline interface
    rl = readline.createInterface({
        input: process.stdin, output: process.stdout,
    });
    rl.question(">> ", (msg) => {
        const payload = { msg, roomId };

        socket.emit("chat message", payload);

        //continue the message prompt
        sendMsg();
    });
};