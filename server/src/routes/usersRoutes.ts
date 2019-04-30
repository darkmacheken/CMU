import express from "express";
import { passport } from "../config/passport";
import { User, IUser } from "../classes/user";
import { userList } from "../main";

export const router = express.Router();

// Get all users according with parameter
router.get("/", (req, res, next) => {
	console.log("GET /users");
	passport.authenticate("jwt", { session: false }, (err, userRequest: User) => {
		if (err || !userRequest) {
			res.status(403);
			res.send({ error: err.message });
		} else if (!req.body.q || /^\s*$/.test(req.body.q)) {
			const responseArray: IUser[] = [];
			for (const user of userList.users) {
				responseArray.push(user.getJson());
			}
			res.send(JSON.stringify(responseArray));
		} else {
			const responseArray: IUser[] = [];
			for (const user of userList.users) {
				let q = req.body.q.toUpperCase();
				if (
					user.id.toUpperCase().includes(q) ||
					user.name.toUpperCase().includes(q) ||
					user.email.toUpperCase().includes(q)
				) {
					responseArray.push(user.getJson());
				}
			}
			res.send(JSON.stringify(responseArray));
		}
	})(req, res, next);
});
