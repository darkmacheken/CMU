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
		} else if (!req.body.q || req.body.q === "") {
			const responseArray: IUser[] = [];
			for (const userQ of userList.users) {
				responseArray.push({ id: userQ.id, name: userQ.name, email: userQ.email });
			}
			if (responseArray.length > 100) {
				res.end(JSON.stringify(responseArray.slice(0, 99)));
			} else {
				res.end(JSON.stringify(responseArray));
			}
		} else {
			const responseArray: IUser[] = [];
			for (const userQ of userList.users) {
				const q = req.body.q.toUpperCase();
				if (
					userQ.id.toUpperCase().includes(q) ||
					userQ.name.toUpperCase().includes(q) ||
					userQ.email.toUpperCase().includes(q)
				) {
					responseArray.push({ id: userQ.id, name: userQ.name, email: userQ.email });
				}
			}
			res.end(JSON.stringify(responseArray));
		}
	})(req, res, next);
});
