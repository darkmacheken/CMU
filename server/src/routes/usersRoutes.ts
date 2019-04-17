import express from "express";
import { Album } from "../classes/album";
import { userList, albumList } from "../main";

export const router = express.Router();

// Get all users
router.get("/", (_req, res) => {
	console.log("GET /users");
	res.end(JSON.stringify(userList.list));
});

// Get specific user
router.get("/:id", (req, res) => {
	console.log("GET /users/" + req.params.id);
	const user = userList.findUserById(+req.params.id);
	if (user) {
		res.end(JSON.stringify(user));
	} else {
		res.statusCode = 400;
		res.end();
	}
});

// List all albums from a specific user
router.get("/:id/albums", (req, res) => {
	console.log("GET /users/" + req.params.id + "/albums");
	const user = userList.findUserById(+req.params.id);
	if (user) {
		res.end(JSON.stringify(user.albums));
	} else {
		res.statusCode = 400;
		res.end();
	}
});

// Create a new album
router.post("/:id/albums", (req, res) => {
	console.log("POST /users/" + req.params.id + "/albums");
	const user = userList.findUserById(+req.params.id);
	if (!user) {
		res.statusCode = 400;
		res.end();
		return;
	}
	const album = new Album(albumList.counter, req.body.name);
	album.users.push({ id: user.id, username: user.username, link: "" });
	albumList.addAlbum(album);
	user.albums.push({ id: album.id, name: album.name });
	console.log("New album" + user);
	res.end(JSON.stringify(user));
});
