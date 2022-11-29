const { WebSocketServer } = require("ws");
const wss = new WebSocketServer({ port: 8080 });
const { v4: uuid } = require("uuid");

console.log("listening on port 8080");

//store all the clients
const clients = {};

/**
 * @description
 * 1. wss.on('connection') is triggered when a client connects to the server
 * 2. ws.on('message') is triggered when the server receives a message from a client
 * 3. ws.on('close') is triggered when the server receives a close event from a client
 */
wss.on("connection", (ws) => {
    //generate a unique id for the client
    const id = uuid();
    clients[id] = { name: null, ws };

    const payload = {
        method: "get id", id,
    }
    //send the id to the client
    ws.send(JSON.stringify(payload));

    ws.on("message", (data) => {
        //parse the data received from the client
        const payload = JSON.parse(data);
        const method = payload.method;

        //if method is new user, save the name of the client
        if (method === "new user") {
            const name = payload.name;
            const userId = payload.id;
            clients[userId].name = name;
            console.log(`${name} connected`);

            //send a message to all other clients that a new user has joined
            broadcastMsg(userId, 'has joined the chat');
        }
        //if method is chat message, broadcast the message to all other clients
        else if (method === "chat message") {
            const msg = payload.msg;
            const userId = payload.id;
            broadcastMsg(userId, msg);
        }
    })

    ws.on("close", () => {
        //id remain persisted because of closure
        const name = clients[id].name;

        //when client disconnected without sending name
        if(!name) return;

        console.log(`${name} disconnected`);

        //remove the disconnected client from the clients object
        delete clients[id];

        //broadcast the disconnection to all other clients
        Object.keys(clients).forEach((clientId) => {
            const payload = {
                method: "chat message", msg: "has left the chat", name,
            }
            clients[clientId].ws.send(JSON.stringify(payload));
        })
    })
})


/**
 * @description
 * Method for broadcasting a message to all other clients
 * @param {String} userId
 * id of the client who sent the message
 * @param {String} msg
 * message to be broadcast
 */
const broadcastMsg = (userId, msg) => {
    //get the name of the sender
    const sender = clients[userId].name;

    //filter out all other clients except the sender
    const otherClientIds = Object.keys(clients).filter(id => id !== userId);

    //send the msg to all other clients
    otherClientIds.forEach((clientId) => {
        const payload = {
            method: "chat message", msg, name: sender,
        }
        clients[clientId].ws.send(JSON.stringify(payload));
    });
}