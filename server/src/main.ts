import express from 'express';
import { UserList } from './Classes/userList';
import { AlbumList } from './Classes/albumList';
import { router as UsersRoutes } from './routes/usersRoutes';
import { router as AlbumRoutes } from './routes/albumsRoutes';

const app = express();

export var userList = new UserList(0);

// export var albumsCounter = 0;
export var albumList = new AlbumList(0);

app.use(express.json());
app.use(UsersRoutes);
app.use(AlbumRoutes);

app.listen(8081, () => {
   //var host = server.address().address
   //var port = server.address().port
   console.log(userList.list);
   console.log(albumList.list);
   //console.log("Example app listening at http://%s:%s", host, port);
})
