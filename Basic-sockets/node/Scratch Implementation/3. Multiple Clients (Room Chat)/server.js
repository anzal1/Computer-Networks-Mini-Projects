const { WebSocketServer } = require("ws");
const wss = new WebSocketServer({ port: 8080 });
const { v4: uuid } = require("uuid");

console.log("listening on port 8080");

//store all the rooms
//every room will have a unique id
//every room will have a list of clients in object format
const rooms = {};
/**
 * @description
 * 1. wss.on('connection') is triggered when a client connects to the server
 * 2. ws.on('message') is triggered when the server receives a message from a client
 * 3. ws.on('close') is triggered when the server receives a close event from a client
 */
wss.on("connection", (ws) => {
    //generate a unique id for the client
    const clientId = uuid();

    const payload = {
        method: "get id", userId: clientId,
    }
    //send the id to the client
    ws.send(JSON.stringify(payload));

    //when the server receives a message from the client
    ws.on("message", (data) => {
        //parse the data received from the client
        const payload = JSON.parse(data);
        const method = payload.method;

        //if the method is create room
        if (method === "create room") {
            //generate a unique room id
            const roomId = generateId();

            const clients = {};

            //add client id to clients list object and add clients list object to room
            clients[clientId] = { ws, name: null };
            rooms[roomId] = { clients };

            //send the room id to the client
            const sendPayload = {
                method: "create room", roomId,
            }
            ws.send(JSON.stringify(sendPayload));
        }
        //if the method is join room, then add the client to the room
        else if (method === "join room") {
            //get the room id from the payload
            const room = rooms[payload.roomId];

            //if the room exists
            if (room) {

                //get the clients list object from the room
                const clients = room.clients;

                //add client id to clients list object and add clients list object to room
                clients[clientId] = { ws, name: null };

                //replace the clients list object in the room with the updated one
                rooms[payload.roomId] = { clients };

                //send the room id to the client
                const sendPayload = {
                    method: "join room", msg: "success", roomId: payload.roomId,
                }
                ws.send(JSON.stringify(sendPayload));
            } else {
                //if the room doesn't exist, send an error message to the client
                const sendPayload = {
                    method: "join room", msg: "fail",
                }
                ws.send(JSON.stringify(sendPayload));
            }
        }
        //if method is new user, save the name of the client
        else if (method === "new user") {
            const name = payload.name;
            const userId = payload.userId;

            console.log(`${name} connected`);

            //get clients list object from the room
            const clients = rooms[payload.roomId].clients;

            clients[clientId].name = name;

            //broadcast the message to all the clients in the room
            broadcastMsg(userId, clients, "has joined the chat");
        }
        //if method is chat message, broadcast the message to all the clients in the room
        else if (method === "chat message") {
            const msg = payload.msg;
            const userId = payload.userId;

            //get clients list object from the room
            const clients = rooms[payload.roomId].clients;

            //broadcast the message to all the clients in the room
            broadcastMsg(userId, clients, msg);
        }
    })

    ws.on("close", () => {
        //get all room ids
        const roomKeys = Object.keys(rooms);

        //search for the room in which the client is present
        roomKeys.forEach((roomId) => {
            //get clients list object from the current room
            const clients = rooms[roomId].clients;

            //if the client is present in the current room only then proceed
            if (!clients[clientId]) return;

            //remove the client from the clients list object
            const name = clients[clientId].name;

            //when client is disconnected without sending a name
            if(!name) return;

            console.log(`${name} disconnected`);
            delete clients[clientId];

            //update the clients list object in the room
            rooms[roomId] = { clients };

            //broadcast the disconnection message to all other clients
            Object.keys(clients).forEach((clientId) => {
                const payload = {
                    method: "chat message", msg: "has left the chat", name,
                }
                clients[clientId].ws.send(JSON.stringify(payload));
            });

            //if client list object is empty then delete that room
            if (Object.keys(clients).length === 0) {
                console.log("room", roomId, "deleted");
                delete rooms[roomId];
            }
        });
    })
})


/**
 * @description
 * Method for broadcasting messages to all the clients in the room
 * @param {String} userId
 * id of the client who sent the message
 * @param {Object} clients
 * clients list object of the room
 * @param {String} msg
 * message to be broadcast
 */
const broadcastMsg = (userId, clients, msg) => {
    //get the name of the client who sent the message
    const sender = clients[userId].name;

    //filter out all other clients except the sender
    const otherClients = Object.keys(clients).filter(id => id !== userId);

    //send the message to all the other clients
    otherClients.forEach((client) => {
        const payload = {
            method: "chat message", msg, name: sender,
        }
        clients[client].ws.send(JSON.stringify(payload));
    });
}

/**
 * @description
 * Method for generating a unique room id
 * @returns {String}
 * unique id
 */
const generateId = () => {
    //generate a random number
    const roomId = String(Math.round(Math.random() * 10000)).padStart(4, "0");

    //if the number is already used then match is true
    const match = Object.keys(rooms).find((id) => id === roomId);

    //recursively call the method until a unique number is generated
    return match ? generateId() : roomId;
}