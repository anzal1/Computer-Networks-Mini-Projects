const WebSocket = require('ws');
const socket = new WebSocket('ws://localhost:8080');
const readline = require('readline');

//singleton readline interface
let rl;
let isUsernameSaved = false;
let userId;
let roomId;

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
        userId = payload.userId;

        //show the choice after getting id to create or join a room
        choice();
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
    //if the method is create room, save the room id and get the name of the user
    else if (method === "create room") {
        //save the room id received from the server for future requests
        roomId = payload.roomId;
        console.log(`Room ${roomId} created`);

        //now get the username after creating the room
        getName()
    }
    //if the method is join room, save the room id and get the name of the user
    else if (method === "join room") {
        //checking if the room exists or not
        if (payload.msg === 'success') {
            console.log(`Room ${payload.roomId} joined`);

            //save the room id received from server for future use
            roomId = payload.roomId;

            //now get the username after joining the room
            getName();
        } else {
            console.log("\nRoom not found\n");

            //if room not found, then ask for choice again
            choice();
        }
    }
})

socket.on('error', () => {
    console.log("Error Connecting to Server");
})

socket.on('close', () => {
    console.log('\nServer Disconnected');
    process.exit();
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
                const payload = { method: "create room" };
                socket.send(JSON.stringify(payload));
                break;
            case "2":
                joinRoom();
                break;
            case "3":
                process.exit(0);
                break;

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
        const payload = { method: "join room", roomId: id };

        //emit the join room event to the server
        socket.send(JSON.stringify(payload));
    });
}

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
            method: "chat message", msg, userId, roomId
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
            method: "new user", name, userId, roomId
        }
        socket.send(JSON.stringify(payload));

        isUsernameSaved = true;

        console.log(`Entered in room ${roomId}`);
        //start the message prompt for this user
        sendMsg();
    });
}
