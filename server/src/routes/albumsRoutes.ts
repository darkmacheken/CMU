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

		const userToAdd = userList.findUserById(req.body.id);
		const album = albumList.findAlbumById(req.params.albumId);

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
			res.send({ error: "User with id " + userToAdd.id + " already is in the album" });
		} else {
			userToAdd.albums.push({ id: album.id, name: album.name });
			album.users.push({ userId: userToAdd.id, folderId: "", fileId: "" });
			albumList.saveToFile();
			userList.saveToFile();
			res.end();
		}
	})(req, res, next);
});

// List all albums
router.get("/", (req, res, next) => {
	passport.authenticate("jwt", { session: false }, (err: Error, user?: User) => {
		if (!user) {
			console.log(`GET /albums { null }`);
			res.status(403);
			res.send({ error: "User not found." });
			return;
		}
		console.log(`GET /albums { id: "${user.id}", name: "${user.name}, email: "${user.email}" }`);
		if (err) {
			res.status(403);
			res.send({ error: err.message });
		} else {
			res.send(albumList.getUserAlbums(user));
		}
	})(req, res, next);
});

// Create a new album
router.post("/", (req, res, next) => {
	passport.authenticate("jwt", { session: false }, (err: Error, user?: User) => {
		if (!user) {
			console.log(`POST /albums { null }`, req.body);
			res.status(403);
			res.send({ error: "User not found." });
			return;
		}
		console.log(`POST /albums { id: "${user.id}", name: "${user.name}, email: "${user.email}" }`, req.body);

		if (err) {
			res.status(403);
			res.send({ error: err.message });
		} else if (!req.body.name) {
			res.status(400);
			res.send({ error: "Wrong parameters" });
		} else {
			const album = new Album(user, req.body.name);
			albumList.addAlbum(album);

			user.albums.push({ id: album.id, name: album.name });
			console.log("userList: " + userList);
			userList.saveToFile();
			res.end(JSON.stringify({ id: album.id }));
		}
	})(req, res, next);
});

// Get a specific album
router.get("/:albumId", (req, res, next) => {
	console.log("GET /albums/" + req.params.albumId);
	passport.authenticate("jwt", { session: false }, (err: Error) => {
		const album = albumList.findAlbumById(req.params.albumId);
		if (err) {
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
function userIsInAlbum(userId: string, album: Album): boolean {
	for (const user of album.users) {
		console.log("userAlbumId: " + album.id);
		if (user.userId === userId) {
			return true;
		}
	}
	return false;
}
