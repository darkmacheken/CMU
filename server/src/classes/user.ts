export class User {
	public id: string;
	public name: string;
	public email: string;
	public albums: Array<{ id: number; name: string }>;

	constructor(id: string, name: string, email: string) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.albums = new Array<{ id: number; name: string }>();
	}

	public addAlbum(album: { id: number; name: string }) {
		this.albums.push(album);
	}
}
