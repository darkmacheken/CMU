import { Album } from "./album";
import fs from "fs";

const albumsPath = "./storage/albums.json";

export class AlbumList {
    
    public list: Album[];
    public counter: number;
    
    constructor(counter: number) {
        this.list = [];
        this.counter = counter;
        this.readFromFile();
    }
    
    public addAlbum(album: Album) {
        this.list.push(album);
        this.counter++;
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
    
    public findAlbumById(id: number) {
        for(const album of this.list) {
            if(album.id === id) {
                return album;
            }
        }
        return false;
    }

    public readFromFile() {
        this.list = JSON.parse(fs.readFileSync(albumsPath, "utf-8"));
        this.counter = this.updateCounter();
        console.log("Albums List");
        console.log(this.list);
        console.log("Counter > " + this.counter);
    }
    
    private updateCounter() {
        let aux = 0;
        for(const album of this.list) {
            if(album.id > aux) {
                aux = album.id;
            }
        }
        return aux + 1;
    }

}