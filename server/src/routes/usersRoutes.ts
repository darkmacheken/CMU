import express from "express";
import { passport } from "../config/passport";
import { User, IUser } from "../classes/user";
import { userList } from "../main";

export const router = express.Router();

// Get all users
router.get("/", (req, res, next) => {
	console.log("GET /users/");
	passport.authenticate("jwt", { session: false }, (err, userRequest: User) => {
		if (err || !userRequest) {
			res.status(403);
			res.send({ error: err.message });
		} else {
			const responseArray: IUser[] = [];
			for (const user of userList.users) {
				if (user.id !== userRequest.id) {
					responseArray.push(user.getJson());
				}
			}
			res.send(JSON.stringify(responseArray));
		}
	})(req, res, next);
});
