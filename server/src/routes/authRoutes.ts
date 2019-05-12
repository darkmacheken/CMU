import express from "express";
import { passport } from "../config/passport";
import { app, userList } from "../main";
import jwt from "jsonwebtoken";
import { User } from "../classes/user";
import * as googleAuth from "../google/googleAuthClient";
import * as googleapis from "googleapis";

export const router = express.Router();

router.post("/login", (req, res) => {
	console.log("POST /login ", req.body);
	passport.authenticate("local", { session: false }, (err, user) => {
		if (err || !user) {
			if (!err) {
				return res.status(400).json({
					error: "No data in the request."
				});
			} else {
				return res.status(404).json({
					error: err.message
				});
			}
		}

		req.login(user, { session: false }, (err) => {
			if (err) {
				res.send(err);
			}

			// modify secret
			console.log("Signing token for login request.");
			const token = jwt.sign({ id: user.id }, app.get("jwt-secret"), { expiresIn: "24h" });
			return res.json({ token });
		});
		return false;
	})(req, res);
});

// Add new user
router.post("/register", (req, res) => {
	console.log("POST /users", req.body);

	if (!req.body.name || !req.body.userid || !req.body.email || !req.body.accessToken) {
		res.status(400);
		res.send({ error: "Wrong parameters" });
	}

	const user = userList.findUserById(req.body.userid);

	if (user) {
		res.status(409);
		res.send({ error: "User already exists" });
	} else {
		let newUser = new User(req.body.userid, req.body.name, req.body.email, req.body.accessToken);
		userList.addUser(newUser);

		googleAuth
			.authorize(newUser)
			.then((client) => {
				// Create Folder
				return googleapis.google.drive({ version: "v3", auth: client }).files.create({
					requestBody: {
						name: "P2Photo",
						mimeType: googleAuth.TYPE_GOOGLE_FOLDER
					}
				});
			})
			.then((folder) => {
				if (folder.data.id) {
					console.log("Created folder Id: ", folder.data.id);
					newUser.setFolderId(folder.data.id);
					res.end(JSON.stringify({ folderId: folder.data.id }));
				}
			})
			.catch((err) => {
				console.error(err);
				res.status(400);
				res.send({ error: "Error creating file." });
			});
	}
});
