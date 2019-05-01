import express from "express";
import { userList } from "../main";
import { albumList } from "../main";
import { passport } from "../config/passport";
import { Album, IAlbum } from "../classes/album";
import { User, IUser } from "../classes/user";
import * as googleapis from "googleapis";
import * as googleAuth from "../google/googleAuthClient";

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
			addUserToAlbum(userToAdd, album).then(() => res.end()).catch((error) => {
				console.error(error);
				res.status(400);
				res.send({ error: "Error adding user to album." });
			});
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
			createAlbum(user, req.body.name, req.body.users)
				.then((album) => {
					res.end(JSON.stringify(album));
				})
				.catch((error) => {
					console.error(error);
					res.status(400);
					res.send({ error: "Error adding user to album." });
				});
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

async function createAlbum(user: User, name: string, users?: IUser[]): Promise<IAlbum> {
	const album = new Album(name);
	albumList.addAlbum(album);
	user.albums.push({ id: album.id, name: album.name });

	// Create folder and
	await googleAuth.authorize(user, (client) => {
		if (user.folderId) {
			googleapis.google
				.drive({ version: "v3", auth: client })
				.files.create({
					requestBody: {
						name: album.id,
						mimeType: googleAuth.TYPE_GOOGLE_FOLDER,
						parents: [ user.folderId ]
					}
				})
				.then((res) => {
					console.log("Album Folder created with success with id ", res.data.id);
					if (res.data.id) {
						googleapis.google
							.drive({ version: "v3", auth: client })
							.files.create({
								requestBody: {
									name: album.id + ".json",
									mimeType: "application/json",
									parents: [ res.data.id ]
								},
								media: {
									body: "[]"
								}
							})
							.then((response) => {
								console.log("Metadata file created with success with id ", response.data.id);
								if (res.data.id && response.data.id) {
									album.users.push({
										userId: user.id,
										folderId: res.data.id,
										fileId: response.data.id
									});
									console.log("userList: " + userList);
									userList.saveToFile();

									if (users) {
										addMultipleUsersToAlbum(users, album);
									}

									albumList.saveToFile();
								} else {
									console.error("Couldn't get the id of the album's metadata file.");
								}
							})
							.catch((err) => console.error(err));
					} else {
						console.error("Couldn't get the id of the album's folder.");
					}
				})
				.catch((err) => console.error(err));
		}
	});
	return album.getJson();
}

async function addMultipleUsersToAlbum(users: IUser[], album: Album) {
	for (const user of users) {
		const userObj = userList.findUserById(user.id);
		if (userObj) {
			await addUserToAlbum(userObj, album);
		}
	}
}

async function addUserToAlbum(user: User, album: Album) {
	// First thing is create folder and metadata file in G Drive of user
	await googleAuth.authorize(user, (client) => {
		if (user.folderId) {
			googleapis.google
				.drive({ version: "v3", auth: client })
				.files.create({
					requestBody: {
						name: album.id,
						mimeType: googleAuth.TYPE_GOOGLE_FOLDER,
						parents: [ user.folderId ]
					}
				})
				.then((res) => {
					console.log("Album Folder created with success with id ", res.data.id);
					if (res.data.id) {
						googleapis.google
							.drive({ version: "v3", auth: client })
							.files.create({
								requestBody: {
									name: album.id + ".json",
									mimeType: "application/json",
									parents: [ res.data.id ]
								},
								media: {
									body: "[]"
								}
							})
							.then((response) => {
								console.log("Metadata file created with success with id ", response.data.id);
								if (res.data.id && response.data.id) {
									album.users.push({
										userId: user.id,
										folderId: res.data.id,
										fileId: response.data.id
									});

									// Add permissions to the other users
									for (const userLinks of album.users) {
										const userObject = userList.findUserById(userLinks.userId);
										if (userLinks.userId !== user.id && userObject) {
											googleapis.google
												.drive({ version: "v3", auth: client })
												.permissions.create({
													fileId: res.data.id,
													requestBody: {
														type: "user",
														role: "reader",
														emailAddress: userObject.email
													}
												});
										}
									}

									// save state
									user.albums.push({ id: album.id, name: album.name });
									albumList.saveToFile();
									userList.saveToFile();
								}
							});
					}
				});
		}
	});

	// Add permission to the user
	for (const userLinks of album.users) {
		const userObject = userList.findUserById(userLinks.userId);

		if (userLinks.userId !== user.id && userObject) {
			googleAuth.authorize(userObject, (client) => {
				googleapis.google.drive({ version: "v3", auth: client }).permissions.create({
					fileId: userLinks.folderId,
					requestBody: {
						type: "user",
						role: "reader",
						emailAddress: user.email
					}
				});
			});
		}
	}
}
