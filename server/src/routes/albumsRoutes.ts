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
			album.users.push({ id: userToAdd.id, link: "" });
			albumList.saveToFile();
			userList.saveToFile();
			res.end();
		}
	})(req, res, next);
});

// List all albums
router.get("/", (req, res, next) => {
	passport.authenticate("jwt", { session: false }, (err: Error, user: User) => {
		console.log("GET /albums", user);
		if (err) {
			res.status(403);
			res.send({ error: err.message });
		} else if (!user) {
			res.status(403);
			res.send({ error: "User not found." });
		} else {
			res.send(user.albums);
		}
	})(req, res, next);
});

// Create a new album
router.post("/", (req, res, next) => {
	passport.authenticate("jwt", { session: false }, (err: Error, user: User) => {
		console.log("POST /albums", user, req.body.name);
		if (err) {
			res.status(403);
			res.send({ error: err.message });
		} else if (!user) {
			res.status(403);
			res.send({ error: "User not found." });
		} else if (!req.body.name) {
			res.status(400);
			res.send({ error: "Wrong parameters" });
		} else {
			const album = new Album(albumList.counter, req.body.name);
			album.users.push({ id: user.id, link: "" });
			albumList.addAlbum(album);
			user.albums.push({ id: album.id, name: album.name });
			console.log("userList: " + userList);
			userList.saveToFile();
			res.end(JSON.stringify(user));
		}
	})(req, res, next);
});

// Get a specific album
router.get("/:albumId", (req, res, next) => {
	console.log("GET /albums/" + req.params.albumId);
	passport.authenticate("jwt", { session: false }, (err: Error) => {
		const album = albumList.findAlbumById(+req.params.albumId);
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
		if (user.id === userId) {
			return true;
		}
	}
	return false;
}
