import passport from "passport";
import passportLocal from "passport-local";
import passportJWT, { ExtractJwt } from "passport-jwt";
import { userList } from "../main";
import { User } from "../classes/user";
const { OAuth2Client } = require("google-auth-library");

const localStrategy = passportLocal.Strategy;
const jwtStrategy = passportJWT.Strategy;

passport.use(
	new localStrategy(
		{
			usernameField: "userid",
			passwordField: "oauthToken"
		},
		(userid, oauthToken, done) => {
			userList.findUserById(userid, (err, user) => {
				if (!user || err) {
					return done(err);
				}
				verify(oauthToken, user).then((isValidTicket) => {
					if (isValidTicket) {
						return done(null, user);
					} else {
						return done(new Error("Token is not valid."));
					}
				});
			});
		}
	)
);

passport.use(
	new jwtStrategy(
		{
			passReqToCallback: true,
			jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
			secretOrKey: "thisisasecret"
		},
		(request: any, payload: any, done: any) => {
			const id = request.params.id;
			const user = userList.findUserById(payload.id);
			if ((user && id == payload.id) || (id === undefined && user)) {
				return done(null, user);
			} else {
				return done(new Error("Users do not match or invalid"));
			}
		}
	)
);
const CLIENT_ID = "949378650699-kg0f2lpje8lbq25v5b55ueien1dneeoa.apps.googleusercontent.com";
const client = new OAuth2Client(CLIENT_ID);
async function verify(token: string, user: User): Promise<boolean> {
	let ticket;
	try {
		ticket = await client.verifyIdToken({
			idToken: token,
			audience: CLIENT_ID
		});
	} catch (error) {
		return false;
	}

	const payload = ticket.getPayload();
	if (payload) {
		const userid = payload["sub"];
		const name = payload["name"];
		const email = payload["email"];
		if (userid === user.id && name === user.name && email === user.email) {
			return true;
		}
	}
	return false;
}

export { passport };
