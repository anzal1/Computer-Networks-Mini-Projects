const { Server } = require("socket.io");
const io = new Server(3000);

console.log("listening on port 3000");

//store all the clients
const clients = {};

/**
 * @description
 * 1. io.on('connection') is triggered when a client is connected to the server
 * 2. socket.on('chat message') is triggered when the client sends a message to the server
 * 3. socket.on('new user') is triggered when the client sends a message to the server
 * 4. socket.on('disconnect') is triggered when the client disconnects from the server
 */
io.on("connection", (socket) => {

    //add the new client to the clients object
    clients[socket.id] = { name: null, socket };

    socket.on('new user', (name) => {
        console.log("New user connected: " + name);

        //save the name of the client
        clients[socket.id].name = name;

        //send a message to all other clients that a new user has joined
        broadcastMsg(socket, "has joined the chat");
    });


    socket.on("chat message", (msg) => {
        //when a new msg is received, broadcast it to all other clients
        broadcastMsg(socket, msg);
    });


    socket.on('disconnect', () => {
        //getting the name of the client who disconnected
        const name = clients[socket.id].name;

        //client disconnected before sending his name
        if(!name) return;

        console.log(`${name} disconnected`);

        //remove the disconnected client from the clients object
        delete clients[socket.id];

        //broadcast the disconnection to all other clients
        Object.keys(clients).forEach((client) => {
            const payload = {
                msg: `has left the chat`, name,
            }
            clients[client].socket.emit("chat message", payload);
        });
    })

});

/**
 * @description
 * Method for broadcasting a message to all other clients
 * @param {Socket.io} socket
 * socket of the client who sent the message
 * @param {String} msg
 * message to be broadcasted
 */
const broadcastMsg = (socket, msg) => {
    //get the name of the sender
    const sender = clients[socket.id].name;

    //filter out all other clients except the sender
    const otherClients = Object.keys(clients).filter(id => id !== socket.id);

    //send the msg to all other clients
    otherClients.forEach((client) => {
        const payload = {
            msg, name: sender,
        }
        clients[client].socket.emit("chat message", payload);
    });
}
