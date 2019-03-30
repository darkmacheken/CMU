export class Album {
    id: number;
    name: string;
    users: Array<{id:number, username: string, link: string}>;
    constructor(id: number, name: string) {
        this.id = id;
        this.name = name;
        this.users = new Array<{id: number, username: string, link: string}>();
    }
    addUser(user: {id: number, username: string, link: string}) {
        this.users.push(user);
    }
}