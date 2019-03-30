import { Album } from "./album";
import fs from 'fs';

const albumsPath = "./storage/albums.txt";

export class AlbumList {
    list: Array<Album>;
    counter: number;
    constructor(counter: number) {
        this.list = new Array<Album>();
        this.counter = counter;
        this.readFromFile();
    }
    addAlbum(album: Album) {
        this.list.push(album);
        this.counter++;
        this.saveToFile();
    }
    saveToFile() {
        fs.writeFile(albumsPath, JSON.stringify(this.list, null, "\t"), function (err) {
            if (err) {
               return console.log(err);
            }
            console.log("The file was saved!");
         });
    }
    readFromFile() {
        this.list = JSON.parse(fs.readFileSync(albumsPath, 'utf-8'));
        this.updateCounter();
    }
    findAlbumById(id: number) {
        for(let album of this.list) {
             if(album.id == id)
                return album
         }
         return false
    }
    updateCounter() {
        let aux = 0;
        for(let album of this.list) {
            if(album.id > aux)
                aux = album.id;
        }
        return aux;
    }
}