import { User } from "./user";
import fs from 'fs';

const usersPath = "./storage/users.txt";

export class UserList {
    list: Array<User>;
    counter: number;
    constructor(counter: number) {
        this.list = new Array<User>();
        this.counter = counter;
        this.readFromFile();
    }
    addUser(user: User) {
        this.list.push(user);
        this.counter++;
        this.saveToFile();
    }
    saveToFile() {
        fs.writeFile(usersPath, JSON.stringify(this.list, null, "\t"), function (err) {
            if (err) {
               return console.log(err);
            }
            console.log("The file was saved!");
         });
    }
    readFromFile() {
        this.list = JSON.parse(fs.readFileSync(usersPath, 'utf-8'));
        this.updateCounter();
    }
    findUserById(id: number) {
        for(let user of this.list) {
             if(user.id == id)
                return user
         }
         return ;
    }
    updateCounter() {
        let aux = 0;
        for(let user of this.list) {
            if(user.id > aux)
                aux = user.id;
        }
        return aux;
    }
}