import express from "express";
import { userList } from "../main";
import { albumList } from "../main";
import { passport } from "../config/passport";
import { Album } from "../classes/album";
import { User } from "../classes/user";

export const router = express.Router();

// Add user to album
router.post("/:albumId/addUser", (req, res, next) => {
	console.log("POST /albums/" + req.params.id + "/addUser");
	passport.authenticate("jwt", { session: false }, (err, user) => {
		if (!req.body.id) {
			res.status(400);
			res.send({ error: "Wrong parameters" });
			return;
		}

		const userToAdd = userList.findUserById(+req.body.id);
		const album = albumList.findAlbumById(+req.params.albumId);

		if (err || !user) {
			res.status(403);
			res.send({ error: err.message });
		} else if (album === undefined) {
			res.status(400);
			res.send({ error: "Album not found" });
		} else if (userToAdd === undefined) {
			res.status(400);
			res.send({ error: "User not found" });
		} else if (!userIsInAlbum(user.id, album)) {
			res.status(400);
			res.send({ error: "User doesn't belong to album with id " + album.id });
		} else if (userIsInAlbum(userToAdd.id, album)) {
			res.status(400);
			res.send({ error: "User with id " + userToAdd.id + " already is the album" });
		} else {
			userToAdd.albums.push({ id: album.id, name: album.name });
			album.users.push({ id: userToAdd.id, username: userToAdd.username, link: "" });
			albumList.saveToFile();
			userList.saveToFile();
			res.end();
		}
	})(req, res, next);
});

// List all albums
router.get("/", (req, res, next) => {
	console.log("GET /albums");
	passport.authenticate("jwt", { session: false }, (err: Error, user: User) => {
		if (err || !user) {
			res.status(403);
			res.send({ error: err.message });
		} else {
			res.send(user.albums);
		}
	})(req, res, next);
});

// Get a specific album
router.get("/:albumId", (req, res, next) => {
	console.log("GET /albums/" + req.params.albumId);
	passport.authenticate("jwt", { session: false }, (err: Error, user: User) => {
		// verificar se user pertence ao album?
		const album = albumList.findAlbumById(+req.params.albumId);
		if (err || !user) {
			res.status(403);
			res.send({ error: err.message });
		} else if (album === undefined) {
			res.status(400);
			res.send({ error: "Album not found" });
		} else {
			res.send(album);
		}
	})(req, res, next);
});

// checks if user belongs to album
function userIsInAlbum(userId: number, album: Album): boolean {
	for (const user of album.users) {
		console.log("userAlbumId: " + album.id);
		if (user.id === userId) {
			return true;
		}
	}
	return false;
}
