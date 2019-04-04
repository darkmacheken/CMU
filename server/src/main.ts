import express from "express";
import { UserList } from "./classes/userList";
import { AlbumList } from "./classes/albumList";
import { router as UsersRoutes } from "./routes/usersRoutes";
import { router as AlbumRoutes } from "./routes/albumsRoutes";
import fs from "fs";
import * as https from "https";

const options = {
	cert: fs.readFileSync("./resources/server.crt", "utf8"),
	key: fs.readFileSync("./resources/server.key", "utf8")
};

const app = express();

export let userList = new UserList(0);
export let albumList = new AlbumList(0);

app.use(express.json());
app.use(UsersRoutes);
app.use(AlbumRoutes);

https.createServer(options, app).listen(8443);
