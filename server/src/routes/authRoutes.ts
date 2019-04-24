import express from "express";
import { passport } from "../config/passport";
import { app, userList } from "../main";
import jwt from "jsonwebtoken";
import { User } from "../classes/user";

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

	if (!req.body.name || !req.body.userid || !req.body.email) {
		res.status(400);
		res.send({ error: "Wrong parameters" });
	}

	let user = userList.findUserById(req.body.userid);

	if (user) {
		res.status(409);
		res.send({ error: "User already exists" });
	} else {
		user = new User(req.body.userid, req.body.name, req.body.email);
		userList.addUser(user);
		console.log(userList);
		res.end(JSON.stringify(user));
	}
});
