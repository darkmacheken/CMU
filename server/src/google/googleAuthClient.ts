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
export async function authorize(user: User): Promise<OAuth2Client> {
	// Load client secrets from a local file.
	return new Promise((resolve, reject) => {
		fs.readFile("./resources/credentials.json", "utf8", (err, content) => {
			if (err) {
				reject(err);
			}
			const credentials = JSON.parse(content);
			const { client_secret, client_id } = credentials.web;
			const oauth2Client = new OAuth2Client(client_id, client_secret);

			if (!user.token) {
				resolve(getNewToken(oauth2Client, user));
			} else {
				oauth2Client.credentials = user.token;
				resolve(oauth2Client);
			}
		});
	});
}

/**
 * Get and store new token after prompting for user authorization, and then
 * execute the given callback with the authorized OAuth2 client.
 *
 * @param {OAuth2Client} oauth2Client The OAuth2 client to get token for.
 */
async function getNewToken(oauth2Client: OAuth2Client, user: User): Promise<OAuth2Client> {
	return oauth2Client
		.getToken(user.accessToken)
		.then((token) => {
			oauth2Client.credentials = token.tokens;
			user.setToken(token.tokens);
			return oauth2Client;
		})
		.catch((error) => {
			return error;
		});
}
