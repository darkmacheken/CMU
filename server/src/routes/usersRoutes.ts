import express from "express";
import { passport } from "../config/passport";
import { User, IUser } from "../classes/user";
import { userList } from "../main";

export const router = express.Router();

router.post("/", (req, res, next) => {
	passport.authenticate("jwt", { session: false }, (err: Error, user?: User) => {
		if (!user) {
			console.log(`POST /users { null }`, req.body);
			res.status(403);
			res.send({ error: "User not found." });
			return;
		}
		console.log(`POST /users { id: "${user.id}", name: "${user.name}, email: "${user.email}" }`, req.body);

		if (err) {
			res.status(403);
			res.send({ error: err.message });
		} else if (!req.body.q) {
			res.status(400);
			res.send({ error: "Wrong parameters" });
		} else {
			const responseArray: IUser[] = [];
			for (const userQ of userList.users) {
				const q = req.body.q.toUpperCase();
				if (
					userQ.id.toUpperCase().includes(q) ||
					userQ.name.toUpperCase().includes(q) ||
					userQ.email.toUpperCase().includes(q)
				) {
					responseArray.push(userQ.getJson());
				}
			}
			res.end(JSON.stringify(responseArray));
		}
	})(req, res, next);
});
