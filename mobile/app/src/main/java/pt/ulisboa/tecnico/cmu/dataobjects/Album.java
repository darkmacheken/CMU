package pt.ulisboa.tecnico.cmu.dataobjects;

import java.util.List;

public class Album {

    private String id;
    private String name;
    private List<Link> users;

    public Album() {
    }

    public Album(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Link> getUsers() {
        return users;
    }

    public void setUsers(List<Link> users) {
        this.users = users;
    }
}
