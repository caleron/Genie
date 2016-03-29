package de.teyzer.genie.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Artist {
    public final ArrayList<Album> albums;
    private final String name;

    public Artist(String name) {
        albums = new ArrayList<>();
        this.name = name;
    }

    public void addAlbum(Album album) {
        albums.add(album);
    }

    public String getName() {
        return name;
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public ArrayList<Track> getAllTracks() {
        ArrayList<Track> tracks = new ArrayList<>();
        for (Album album : albums) {
            tracks.addAll(album.getTracks());
        }

        Collections.sort(tracks, new Comparator<Track>() {
            @Override
            public int compare(Track lhs, Track rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });
        return tracks;
    }
}
