package de.teyzer.genie.model;

public class Track {
    private final int id;

    private final String title;
    private final String artist;
    private final String album;
    private final String path;

    public Track(int id, String title, String artist, String album, String path) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Track) {
            Track otherTrack = (Track) o;
            if (otherTrack.path.equals(path)) {
                if (otherTrack.title.equals(title) && otherTrack.artist.equals(artist) && otherTrack.album.equals(album)) {
                    return true;
                }
            }
        }
        return false;
    }
}
