import uuidv4 from "uuid";
import { User } from "./user";
import * as googleAuth from "../google/googleAuthClient";
import * as googleapis from "googleapis";
import { albumList } from "../main";

export interface IAlbum {
	id: string;
	name: string;
}

export interface ILink {
	userId: string;
	folderId: string;
	fileId: string;
}

export class Album implements IAlbum {
	public id: string;
	public name: string;
	public users: ILink[];

	constructor(user: User, name: string) {
		this.id = uuidv4();
		this.name = name;
		this.users = [];

		googleAuth.authorize(user, (client) => {
			if (user.folderId) {
				googleapis.google
					.drive({ version: "v3", auth: client })
					.files.create({
						requestBody: {
							name: this.id,
							mimeType: googleAuth.TYPE_GOOGLE_FOLDER,
							parents: [ user.folderId ]
						}
					})
					.then((res) => {
						console.log("Album Folder created with success with id ", res.data.id);
						if (res.data.id) {
							googleapis.google
								.drive({ version: "v3", auth: client })
								.files.create({
									requestBody: {
										name: this.id + ".json",
										mimeType: "application/json",
										parents: [ res.data.id ]
									},
									media: {
										body: "[]"
									}
								})
								.then((response) => {
									console.log("Metadata file created with success with id ", response.data.id);
									if (res.data.id && response.data.id) {
										this.users.push({
											userId: user.id,
											folderId: res.data.id,
											fileId: response.data.id
										});
										albumList.saveToFile();
									} else {
										console.error("Couldn't get the id of the album's metadata file.");
									}
								})
								.catch((err) => console.error(err));
						} else {
							console.error("Couldn't get the id of the album's folder.");
						}
					})
					.catch((err) => console.error(err));
			}
		});
	}

	public addUser(user: ILink) {
		this.users.push(user);
	}

	public getJson(): IAlbum {
		return { id: this.id, name: this.name };
	}
}
