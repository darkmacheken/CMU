import fs from "fs";
import { OAuth2Client } from "google-auth-library";
import { User } from "../classes/user";

export const TYPE_GOOGLE_FOLDER = "application/vnd.google-apps.folder";

/**
 * Create an OAuth2 client with the given credentials, and then execute the
 * given callback function.
 *
 * @param {Object} credentials The authorization client credentials.
 */
export function authorize(user: User, callback?: (client: OAuth2Client) => void) {
	// Load client secrets from a local file.
	fs.readFile("./resources/credentials.json", "utf8", (err, content) => {
		if (err) {
			return console.error("Error loading client secret file", err);
		}
		const credentials = JSON.parse(content);
		const { client_secret, client_id } = credentials.web;
		const oauth2Client = new OAuth2Client(client_id, client_secret);

		if (!user.token) {
			return getNewToken(oauth2Client, user, callback);
		} else {
			oauth2Client.credentials = user.token;
		}

		if (callback) {
			callback(oauth2Client);
		}
	});
}

/**
 * Get and store new token after prompting for user authorization, and then
 * execute the given callback with the authorized OAuth2 client.
 *
 * @param {OAuth2Client} oauth2Client The OAuth2 client to get token for.
 */
function getNewToken(oauth2Client: OAuth2Client, user: User, callback?: (client: OAuth2Client) => void) {
	oauth2Client.getToken(user.accessToken, (err: any, token: any) => {
		if (err) {
			return console.error("Error getting token.", err);
		}
		oauth2Client.credentials = token;
		user.setToken(token);

		if (callback) {
			callback(oauth2Client);
		}
	});
}
