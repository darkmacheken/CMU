import express from 'express';
import fs from 'fs';
import { User } from './Classes/user';
import { Album } from './Classes/album';
const app = express();

var usersPath = "./storage/users.txt";
var albumsPath = "./storage/albums.txt";

var userCounter = 0;
var userList = new Array<User>();

var albumsCounter = 0;
var albumsList = new Array<Album>();

app.use(express.json());


// Add user to album
app.post('/albums/:id/addUser', function(req, res) {
   console.log("Add user to album");
   var user = findUser(req.body.id);
   var album = findAlbum(req.params.id);
   if(!user || !album) {
      res.statusCode = 400;
      res.end();
      return;
   }
   user.albums.push({id: album.id, name: album.name});
   console.log(user);
   album.users.push({id: user.id, username: user.username, link:""});
   saveToFile();
   console.log(albumsList);
   res.end();
}) 

// List all albums
app.get('/albums', function(_req, res) {
   res.end(JSON.stringify(albumsList));
})

// Get a specific album
app.get('/albums/:id', function(req, res) {
   var album = findAlbum(req.params.id);
   if(album)
      res.end(JSON.stringify(album));
   else {
      res.statusCode = 400;
      res.end();
   }
})

// List all albums from a specific user
app.get('/users/:id/albums', function(req, res) {
   var user = findUser(req.params.id);
   if(user)
      res.end(JSON.stringify(user.albums));
   else {
      res.statusCode = 400;
      res.end();
   }
})

// Create a new album
app.post('/users/:id/albums', function(req, res) {
   var user = findUser(req.params.id);
   if(!user) {
      res.statusCode = 400;
      res.end();
      return;
   }
   var album = new Album(albumsCounter, req.body.name);
   album.users.push({id: user.id, username: user.username, link:""});
   albumsList.push(album);
   user.albums.push({id: album.id, name: album.name});
   albumsCounter++;
   saveToFile();
   console.log("New album" + user);
   res.end(JSON.stringify(user));
})

// Get all users
app.get('/users', function (_req, res) {
   console.log("GET /users");
   res.end(JSON.stringify(userList));
})

// Add new user
app.post('/users', function(req, res) {
   console.log("POST /users");
   var user = new User(userCounter, req.body.username, req.body.pass);
   userList.push(user);
   userCounter++;
   saveToFile();
   console.log(userList);
   res.end(JSON.stringify(user));
})

// Get specific user
app.get('/users/:id', function(req, res) {
   var user = findUser(req.params.id);
   if(user)
      res.end(JSON.stringify(user));
   else {
      res.statusCode = 400;
      res.end();
   }
})

function saveToFile() {
   fs.writeFile(usersPath, JSON.stringify(userList, null, "\t"), function (err) {
      if (err) {
         return console.log(err);
      }
      console.log("The file was saved!");
   });
   fs.writeFile(albumsPath, JSON.stringify(albumsList, null, "\t"), function (err) {
      if (err) {
         return console.log(err);
      }
      console.log("The file was saved!");
   });

}

function findUser(id: number) {
   for(var i = 0; i < userList.length; i++) {
      if(userList[i].id == id) 
         return userList[i];
   }
   return false;
}

function findAlbum(id: number) {
   for(var i = 0; i < albumsList.length; i++) {
      if(albumsList[i].id == id)
         return albumsList[i];
   }
   return false;
}

app.listen(8081, function () {
   //var host = server.address().address
   //var port = server.address().port
   userList = JSON.parse(fs.readFileSync(usersPath, 'utf-8'));
   albumsList = JSON.parse(fs.readFileSync(albumsPath, 'utf-8'));
   if(userList.length > 0) userCounter = userList[userList.length-1].id + 1;
   if(albumsList.length > 0) albumsCounter = albumsList[albumsList.length-1]["id"] + 1;
   console.log(userList);
   console.log(albumsList);
   //console.log("Example app listening at http://%s:%s", host, port);
})