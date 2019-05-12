import { Album } from "./album";
import fs from "fs";
import { User } from "./user";

const albumsPath = "./storage/albums.json";

export class AlbumList {
	public list: Album[];

	constructor() {
		this.list = [];
		this.readFromFile();
	}

	public addAlbum(album: Album) {
		this.list.push(album);
		this.saveToFile();
	}

	public saveToFile() {
		fs.writeFile(albumsPath, JSON.stringify(this.list, null, "\t"), (err) => {
			if (err) {
				return console.log(err);
			}
			console.log("The file was saved!");
		});
	}

	public findAlbumById(id: string): Album | undefined {
		for (const album of this.list) {
			if (album.id === id) {
				return album;
			}
		}
		return undefined;
	}

	public getUserAlbums(user: User): Album[] {
		const albums = [];
		for (const albumUser of user.albums) {
			const album = this.findAlbumById(albumUser.id);
			if (album) {
				albums.push(album);
			}
		}
		return albums;
	}

	public readFromFile() {
		this.list = JSON.parse(fs.readFileSync(albumsPath, "utf-8"));
		console.log("Albums List");
		console.log(this.list);
	}
}
