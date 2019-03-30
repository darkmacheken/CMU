import express from 'express';
import { userList } from '../main';
import { albumList } from '../main';

export const router = express.Router();

// Add user to album
router.post('/albums/:id/addUser', (req, res) => {
    console.log("Add user to album");
    var user = userList.findUserById(req.body.id);
    var album = albumList.findAlbumById(req.params.id);
    if(!user || !album) {
       res.statusCode = 400;
       res.end();
       return;
    }
    user.addAlbum({id: album.id, name: album.name});
    console.log(user);
    album.addUser({id: user.id, username: user.username, link:""});
    albumList.saveToFile();
    console.log(albumList.list);
    res.end();
 }) 
 
 // List all albums
 router.get('/albums', (_req, res) => {
    res.end(JSON.stringify(albumList.list));
 })
 
 // Get a specific album
 router.get('/albums/:id', (req, res) => {
    var album = albumList.findAlbumById(req.params.id);
    if(album)
       res.end(JSON.stringify(album));
    else {
       res.statusCode = 400;
       res.end();
    }
 })