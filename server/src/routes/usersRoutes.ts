import express from "express";
import { passport } from "../config/passport";
import { User } from "../classes/user";

export const router = express.Router();

// Get all users
router.get("/", (req, res, next) => {
	console.log("GET /users/");
	passport.authenticate("jwt", { session: false }, (err, user) => {
		if (err || !user) {
			res.status(403);
			res.send({ error: err.message });
		} else {
			res.send(JSON.stringify(user));
		}
	})(req, res, next);
});

// Get specific user
router.get("/:id", (req, res, next) => {
	console.log("GET /users/" + req.params.id);
	passport.authenticate("jwt", { session: false }, (err, user: User) => {
		if (err || !user) {
			res.status(403);
			res.send({ error: err.message });
		} else {
			res.send(JSON.stringify(user.getJson()));
		}
	})(req, res, next);
});

// List all albums from a specific user
router.get("/:id/albums", (req, res, next) => {
	console.log("GET /users/" + req.params.id + "/albums");
	passport.authenticate("jwt", { session: false }, (err: Error, user: User) => {
		if (err || !user) {
			res.status(403);
			res.send({ error: err.message });
		} else {
			res.send(JSON.stringify(user.albums));
		}
	})(req, res, next);
});
