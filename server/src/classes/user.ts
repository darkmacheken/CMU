export class User {
	public id: number;
	public name: string;
	public username: string;
	public pass: string;
	public albums: Array<{ id: number; name: string }>;

	constructor(id: number, name: string, username: string, pass: string) {
		this.id = id;
		this.name = name;
		this.username = username;
		this.pass = pass;
		this.albums = new Array<{ id: number; name: string }>();
	}

	public addAlbum(album: { id: number; name: string }) {
		this.albums.push(album);
	}
}
