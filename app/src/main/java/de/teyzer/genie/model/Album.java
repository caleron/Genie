package de.teyzer.genie.model;

import android.util.SparseArray;

public class Album {
    String artistName;
    String albumName;
    SparseArray<Track> tracks;

    public Album(String albumName, String artistName) {
        tracks = new SparseArray<>();
        this.artistName = artistName;
        this.albumName = albumName;
    }

    public void addTrack(int id, Track track) {
        tracks.put(id, track);
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtistName() {
        return artistName;
    }
}
