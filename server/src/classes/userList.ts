import { User } from "./user";
import fs from "fs";

const usersPath = "./storage/users.json";

export class UserList {
	public users: User[];

	constructor() {
		this.users = [];
		this.readFromFile();
	}

	public addUser(user: User) {
		this.users.push(user);
		this.saveToFile();
	}

	public saveToFile() {
		fs.writeFile(usersPath, JSON.stringify(this.users, null, "\t"), (err) => {
			if (err) {
				return console.log(err);
			}
			console.log("The file users.json was saved!");
		});
	}

	public findUserByName(name: string, done?: (err: any, user?: User) => void) {
		for (const user of this.users) {
			if (user.name === name) {
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

	public findUserById(id: string, done?: (err: any, user?: User) => void): User | undefined | void {
		for (const user of this.users) {
			if (user.id === id) {
				if (done) {
					return done(null, user);
				} else {
					return user;
				}
			}
		}

		if (done) {
			return done(new Error(`User with id ${id} not found!`));
		} else {
			return undefined;
		}
	}

	private readFromFile() {
		this.users = JSON.parse(fs.readFileSync(usersPath, "utf-8"));

		console.log("Users List");
		console.log(this.users);
	}
}
