package de.teyzer.genie.model;

import java.util.HashMap;

public class Artist {
    public HashMap<String, Album> albums;
    String name;

    public Artist(String name) {
        albums = new HashMap<>();
        this.name = name;
    }

    public void addAlbum(String name, Album album) {
        albums.put(name, album);
    }

    public String getName() {
        return name;
    }
}
