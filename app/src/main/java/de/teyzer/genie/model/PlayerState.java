package de.teyzer.genie.model;

import java.io.Serializable;

public class PlayerState implements Serializable {
    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ONE = 1;
    public static final int REPEAT_MODE_ALL = 2;

    private boolean shuffle = true;
    private int repeatMode = REPEAT_MODE_ALL;
    private boolean playing = false;

    private int playPosition = 0;
    private int trackLength = 100;

    private String currentTitle = "", currentArtist = "", currentAlbum = "";

    public PlayerState(boolean shuffle, int repeatMode) {
        this.shuffle = shuffle;
        this.repeatMode = repeatMode;
    }

    /**
     * Wendet die neuen Statusdaten an
     *
     * @param str Der Status-String vom Server
     * @return true, wenn sich der Titel geändert hat.
     */
    public boolean parsePlayerState(String str) {
        if (str == null)
            return false;

        String[] strings = str.split(";");

        String oldTitle = currentTitle;
        String oldArtist = currentArtist;

        currentTitle = "";
        currentArtist = "";
        currentAlbum = "";
        playPosition = 0;
        trackLength = 100;

        for (String attr : strings) {
            String[] pair = attr.split(":");

            switch (pair[0]) {
                case "playing":
                    playing = Boolean.parseBoolean(pair[1]);
                    break;
                case "shuffle":
                    shuffle = Boolean.parseBoolean(pair[1]);
                    break;
                case "repeat":
                    repeatMode = Integer.parseInt(pair[1]);
                    break;
                case "currentTitle":
                    currentTitle = pair[1];
                    break;
                case "currentArtist":
                    currentArtist = pair[1];
                    break;
                case "currentAlbum":
                    currentAlbum = pair[1];
                    break;
                case "trackLength":
                    trackLength = Integer.parseInt(pair[1]);
                    break;
                case "playPosition":
                    playPosition = Integer.parseInt(pair[1]);
                    break;
                default:
                    System.out.println("unknown attribute: " + pair[0]);
                    break;
            }
        }
        System.out.println("new play position: " + playPosition);
        if (currentArtist.isEmpty() && oldArtist.isEmpty()) {
            //Falls der Dateiname als Titel herhalten musste, etwa wenn keine Tags vorhanden sind
            return currentTitle.equals(oldTitle);
        }
        return !(currentArtist.equals(oldArtist) && currentTitle.equals(oldTitle));
    }

    public int getPlayPosition() {
        return playPosition;
    }

    public int getTrackLength() {
        return trackLength;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public String getCurrentArtist() {
        return currentArtist;
    }

    public String getCurrentAlbum() {
        return currentAlbum;
    }

    public int getRemainingSeconds() {
        return trackLength - playPosition;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void toggleShuffle() {
        shuffle = !shuffle;
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public void nextRepeatMode() {
        repeatMode = (repeatMode + 1) % 3;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void togglePlaying() {
        playing = !playing;
    }
}
