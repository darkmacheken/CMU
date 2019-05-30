package pt.ulisboa.tecnico.cmu.dataobjects;

import java.util.List;

public class Catalog {

    private final String targetVirtIp;
    private final String userName;
    private final String albumName;
    private final List<String> catalogLineList;

    public Catalog(String targetVirtIp, String userName, String albumName, List<String> catalogLineList) {
        this.targetVirtIp = targetVirtIp;
        this.userName = userName;
        this.albumName = albumName;
        this.catalogLineList = catalogLineList;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null) {
            return false;
        }
        if (!(otherObject instanceof Catalog)) {
            return false;
        }

        Catalog otherCatalog = (Catalog) otherObject;
        if (this.getUserName().equals(otherCatalog.getUserName()) &&
            this.getAlbumName().equals(otherCatalog.getAlbumName())) {
            return true;
        } else {
            return false;
        }
    }

    public String getTargetVirtIp() {
        return targetVirtIp;
    }

    public String getUserName() {
        return userName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public List<String> getCatalogLineList() {
        return catalogLineList;
    }

    public String getContentString() {
        String contentString = "";
        for (String line : catalogLineList) {
            contentString += line + "\n";
        }
        return contentString;
    }

    @Override
    public String toString() {
        return "Catalog{" +
            "targetVirtIp='" + targetVirtIp + '\'' +
            ", userName='" + userName + '\'' +
            ", albumName='" + albumName + '\'' +
            ", catalogLineList=" + getContentString() +
            '}';
    }
}
