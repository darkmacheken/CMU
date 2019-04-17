import express from "express";
import { UserList } from "./classes/userList";
import { AlbumList } from "./classes/albumList";
import { router as UsersRoutes } from "./routes/usersRoutes";
import { router as AlbumRoutes } from "./routes/albumsRoutes";
import { router as AuthRoutes } from "./routes/authRoutes";
import fs from "fs";
import * as https from "https";
import { passport } from "./config/passport";

const options = {
	cert: fs.readFileSync("./resources/server.crt", "utf8"),
	key: fs.readFileSync("./resources/server.key", "utf8")
};

export const app = express();

export let userList = new UserList(0);
export let albumList = new AlbumList(0);

app.set("jwt-secret", "thisisasecret");
app.use(express.json());
app.use("/users", passport.authenticate("jwt", { session: false }), UsersRoutes);
app.use("/albums", passport.authenticate("jwt", { session: false }), AlbumRoutes);
app.use(AuthRoutes);
app.use(passport.initialize());

https.createServer(options, app).listen(8443);
