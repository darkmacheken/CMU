export class User {
    id: number;
    username: string;
    pass: string;
    albums: Array<{id:number, name:string}>;
    constructor(id: number, username: string, pass: string) {
        this.id = id;
        this.username = username;
        this.pass = pass;
        this.albums = new Array<{id:number, name:string}>();
    }
    addAlbum(album: {id: number, name: string}) {
        this.albums.push(album);
    }
}