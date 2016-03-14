package de.teyzer.genie.model;

import java.util.ArrayList;

public class Album {
    String artistName;
    String albumName;
    ArrayList<Track> tracks;

    public Album(String albumName, String artistName) {
        tracks = new ArrayList<>();
        this.artistName = artistName;
        this.albumName = albumName;
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }
}
