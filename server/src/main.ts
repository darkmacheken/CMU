import express from "express";
import { UserList } from "./classes/userList";
import { AlbumList } from "./classes/albumList";
import { router as UsersRoutes } from "./routes/usersRoutes";
import { router as AlbumRoutes } from "./routes/albumsRoutes";

const app = express();

export let userList = new UserList(0);
export let albumList = new AlbumList(0);

app.use(express.json());
app.use(UsersRoutes);
app.use(AlbumRoutes);

app.listen(8080, () => {
   console.log("Server is running!");
});
