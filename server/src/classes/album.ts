import uuidv4 from "uuid";

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
	public wifi: boolean;
	public users: ILink[];

	constructor(name: string, wifi: boolean) {
		this.id = uuidv4();
		this.name = name;
		this.users = [];
		this.wifi = wifi;
	}

	public addUser(user: ILink) {
		this.users.push(user);
	}

	public getJson(): IAlbum {
		return { id: this.id, name: this.name };
	}
}
