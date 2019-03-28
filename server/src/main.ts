import express from 'express';
const server = express();

server.listen(8080, () => {
    console.log("Server is up and listening on port 8080...");
});

server.get("/", (_req, res) => {
    res.send("Hello!")
})