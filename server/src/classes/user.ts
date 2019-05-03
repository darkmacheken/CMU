import { userList } from "../main";
import { IAlbum } from "./album";
import { Credentials } from "google-auth-library";

export interface IUser {
	id: string;
	name: string;
	email: string;
}

export class User implements IUser {
	public id: string;
	public name: string;
	public email: string;
	public token?: Credentials;
	public accessToken: string;
	public folderId?: string;
	public albums: IAlbum[];

	constructor(id: string, name: string, email: string, accessToken: string) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.accessToken = accessToken;
		this.albums = [];
	}

	public addAlbum(album: IAlbum) {
		this.albums.push(album);
	}

	public setToken(token: Credentials) {
		this.token = token;
		userList.saveToFile();
	}

	public setFolderId(id: string) {
		this.folderId = id;
		userList.saveToFile();
	}
}
