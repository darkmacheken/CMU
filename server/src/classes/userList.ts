import { User } from "./user";
import fs from "fs";

const usersPath = "./storage/users.json";

export class UserList {
	public list: User[];
	public counter: number;

	constructor(counter: number) {
		this.list = [];
		this.counter = counter;
		this.readFromFile();
	}

	public addUser(user: User) {
		this.list.push(user);
		this.counter++;
		this.saveToFile();
	}

	public saveToFile() {
		fs.writeFile(usersPath, JSON.stringify(this.list, null, "\t"), (err) => {
			if (err) {
				return console.log(err);
			}
			console.log("The file was saved!");
		});
	}

	public findUserByName(name: string, done?: (err: any, user?: User) => void) {
		for (const user of this.list) {
			if (user.username === name) {
				if (done) {
					return done(null, user);
				} else {
					return user;
				}
			}
		}
		if (done) {
			return done(new Error("User not found!"));
		} else {
			return undefined;
		}
	}

	public findUserById(id: number): User | undefined {
		for (const user of this.list) {
			if (user.id === id) {
				return user;
			}
		}
		return undefined;
	}

	private readFromFile() {
		this.list = JSON.parse(fs.readFileSync(usersPath, "utf-8"));
		this.counter = this.updateCounter();
		console.log("Users List");
		console.log(this.list);
		console.log("Counter > " + this.counter);
	}

	private updateCounter() {
		let aux = 0;
		for (const user of this.list) {
			if (user.id > aux) {
				aux = user.id;
			}
		}
		return aux + 1;
	}
}
