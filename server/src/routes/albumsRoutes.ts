import express from "express";
import { userList } from "../main";
import { albumList } from "../main";

export const router = express.Router();

// Add user to album
router.post("/albums/:id/addUser", (req, res) => {
	console.log("POST /albums/" + req.params.id + "/addUser");
	const user = userList.findUserById(+req.body.id);
	const album = albumList.findAlbumById(+req.params.id);

	if (!user || !album) {
		res.statusCode = 400;
		res.end();
		return;
	}

	user.addAlbum({ id: album.id, name: album.name });
	album.addUser({ id: user.id, username: user.username, link: "" });
	albumList.saveToFile();
	res.end();
});

// List all albums
router.get("/albums", (_req, res) => {
	console.log("GET /albums");
	res.end(JSON.stringify(albumList.list));
});

// Get a specific album
router.get("/albums/:id", (req, res) => {
	console.log("GET /albums/" + req.params.id);
	const album = albumList.findAlbumById(+req.params.id);
	if (album) {
		res.end(JSON.stringify(album));
	} else {
		res.statusCode = 400;
		res.end();
	}
});
