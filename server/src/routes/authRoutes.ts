import express from "express";
import { passport } from "../config/passport";
import { app, userList } from "../main";
import jwt from "jsonwebtoken";
import { User } from "../classes/user";
import bcrypt from "bcryptjs";

export const router = express.Router();

router.post("/login", (req, res) => {
	passport.authenticate("local", { session: false }, (err, user) => {
		if (err || !user) {
			return res.status(400).json({
				error: err.message
			});
		}
		req.login(user, { session: false }, (err) => {
			if (err) {
				res.send(err);
			}

			// modify secret
			console.log("Signing token with secret: " + app.get("jwt-secret"));
			const token = jwt.sign({ id: user.id }, app.get("jwt-secret"), { expiresIn: "5h" });
			return res.json({ token });
		});
		return false;
	})(req, res);
});

// Add new user
router.post("/register", (req, res) => {
	console.log("POST /users");
	const salt = bcrypt.genSaltSync(10);
	let user = userList.findUserByName(req.body.username);
	if (user) {
		res.status(400);
		res.send({ error: "User already exists" });
	} else if (!req.body.name || !req.body.username || !req.body.password) {
		res.status(400);
		res.send({ error: "Wrong parameters" });
	} else {
		user = new User(userList.counter, req.body.name, req.body.username, bcrypt.hashSync(req.body.password, salt));
		userList.addUser(user);
		console.log(userList);
		res.end(JSON.stringify(user));
	}
});
