import passport from "passport";
import passportLocal from "passport-local";
import passportJWT, { ExtractJwt } from "passport-jwt";
import { userList } from "../main";

const localStrategy = passportLocal.Strategy;
const jwtStrategy = passportJWT.Strategy;

passport.use(
	new localStrategy((username, password, done) => {
		userList.findUserByName(username, (err, user) => {
			if (!user || err) {
				return done(err);
			}
			if (user.pass === password) {
				return done(null, user);
			}
			return done(new Error("Wrong password!"));
		});
	})
);

passport.use(
	new jwtStrategy(
		{
			jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
			secretOrKey: "thisisasecret"
		},
		(payload, done) => {
			const user = userList.findUserById(payload.id);
			if (!user) {
				return done(new Error("User not found!"));
			} else {
				return done(null, user);
			}
		}
	)
);

export { passport };
