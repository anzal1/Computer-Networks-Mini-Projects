const { Server } = require("socket.io");
const io = new Server(3000);

console.log("listening on port 3000");

//store all the rooms
//every room will have a unique id
//every room will have a list of clients in object format
const rooms = {};

/**
 * @description
 * 1. io.on('connection') is triggered when a client is connected to the server
 * 2. socket.on('create room') is triggered when a client creates a room
 * 3. socket.on('join room') is triggered when a client joins a room
 * 4. socket.on('new user') is triggered when a client sends his name
 * 5. socket.on('chat message') is triggered when a client sends a message
 * 6. socket.on('disconnect') is triggered when a client disconnects from the server
 */
io.on("connection", (socket) => {

    socket.on("create room", () => {
        //generate a unique room id
        const id = generateId();

        const clients = {};

        //add client id to clients list object and add clients list object to room
        clients[socket.id] = { socket, name: null };
        rooms[id] = { clients };

        //send the room id to the client
        socket.emit("create room", id);
    });

    socket.on("join room", (payload) => {
        //get the room id from the payload
        const room = rooms[payload.roomId];

        //if the room exists
        if (room) {

            //get the clients list object from the room
            const clients = room.clients;

            //add client id to clients list object and add clients list object to room
            clients[socket.id] = { socket, name: null };

            //replace the clients list object in the room with the updated one
            rooms[payload.roomId] = { clients };

            //send the room id to the client
            socket.emit("join room", { msg: "success", roomId: payload.roomId });
        } else {
            //if the room doesn't exist, send an error message to the client
            socket.emit("join room", { msg: "fail", roomId: payload.roomId });
        }
    });


    socket.on('new user', (payload) => {
        console.log("New user connected: " + payload.name);

        //get clients list object from the room
        const clients = rooms[payload.roomId].clients;

        //add the name to the client id in the clients list object
        clients[socket.id].name = payload.name;

        //broadcast the new user to all the clients in the room
        broadcastMsg(socket, clients, "has joined the chat");
    });

    socket.on("chat message", (payload) => {
        //get clients list object from the room
        const clients = rooms[payload.roomId].clients;

        //broadcast the message to all the clients in the room
        broadcastMsg(socket, clients, payload.msg);
    });

    socket.on("disconnect", () => {
        //get all the room ids
        const roomKeys = Object.keys(rooms);

        //search the client in all the rooms
        roomKeys.forEach((roomId) => {
            //get clients list object from current room
            const clients = rooms[roomId].clients;

            //if the client exists in the current room
            if (!clients[socket.id]) return;

            //remove the client from the clients list object
            const name = clients[socket.id].name;

            //client disconnected before sending his name
            if(!name) return;

            console.log(`${name} disconnected`);
            delete clients[socket.id];

            //update the clients list object in the room
            rooms[roomId] = { clients };

            //broadcast the disconnection to all the clients in the room
            Object.keys(clients).forEach((client) => {
                const payload = {
                    msg: `has left the chat`, name,
                }
                clients[client].socket.emit("chat message", payload);
            })

            //if client list object is empty then delete that room
            if (Object.keys(clients).length === 0) {
                console.log("room", roomId, "deleted");
                delete rooms[roomId];
            }
        })
    })
});

/**
 * @description
 * Method for broadcasting messages to all the clients in the room
 * @param {Socket} socket
 * socket of the client who sent the message
 * @param {Object} clients
 * clients list object of the room
 * @param {String} msg
 * message to be broadcast
 */
const broadcastMsg = (socket, clients, msg) => {
    //get the name of the client who sent the message
    const sender = clients[socket.id].name;

    //filter out all other clients except the sender
    const otherClients = Object.keys(clients).filter(id => id !== socket.id);

    //send the message to all the other clients
    otherClients.forEach((client) => {
        const payload = {
            msg, name: sender,
        }
        clients[client].socket.emit("chat message", payload);
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