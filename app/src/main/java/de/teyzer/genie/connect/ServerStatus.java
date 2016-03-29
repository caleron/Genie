package de.teyzer.genie.connect;

import android.graphics.Color;
import android.net.Uri;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ServerStatus implements Serializable, ResponseListener {
    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ONE = 1;
    public static final int REPEAT_MODE_ALL = 2;

    private boolean shuffle = true;
    private int repeatMode = REPEAT_MODE_ALL;
    private boolean playing = false;

    private int playPosition = 0;
    private int trackLength = 100;

    private final Set<StatusChangedListener> listenerList = new HashSet<>();
    private final ServerConnect serverConnect;

    private String currentTitle = "", currentArtist = "", currentAlbum = "";
    private String colorMode;
    private int whiteBrightness;
    private int currentColor;
    private int colorBrightness;

    public ServerStatus(ServerConnect serverConnect) {
        this.serverConnect = serverConnect;
    }

    /**
     * Fordert einen neuen Serverstatus an.
     */
    public void requestNewStatus() {
        serverConnect.executeAction(Action.getStatus(this));
    }

    /**
     * Wendet die neuen Statusdaten an
     *
     * @param str Der Status-String vom Server
     * @return true, wenn sich der Titel geändert hat.
     */
    private boolean parseNewState(String str) {
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
                case "colorMode":
                    colorMode = pair[1];
                    break;
                case "whiteBrightness":
                    whiteBrightness = Integer.parseInt(pair[1]);
                    break;
                case "colorBrightness":
                    colorBrightness = Integer.parseInt(pair[1]);
                    break;
                case "currentColor":
                    currentColor = Integer.parseInt(pair[1]);
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

    /**
     * Wird ausgelöst, wenn eine Antwort vom Server kommt
     *
     * @param sourceAction Die Ursprungsaktion
     * @param response     Die Antwort
     */
    @Override
    public void responseReceived(Action sourceAction, String response) {
        boolean newSong = parseNewState(response);

        for (StatusChangedListener listener : listenerList) {
            listener.serverStatusChanged(newSong);
        }
    }

    /**
     * Fügt einen StatusChangedListener hinzu. Falls der listener bereits hinzugefügt wurde, hat
     * der erneute Aufruf keine Auswirkung, da die listener in einem Set gespeichert werden.
     *
     * @param listener Der neue Listener
     */
    public void addStatusChangedListener(StatusChangedListener listener) {
        listenerList.add(listener);
    }

    /**
     * Startet die Wiedergabe einer Datei
     *
     * @param uri                  Uri zur Datei
     * @param uploadStatusListener UploadStatusListener, für den Fall, dass die Datei noch nicht auf
     *                             dem Server ist.
     */
    public void playFile(Uri uri, UploadStatusListener uploadStatusListener) {
        serverConnect.executeAction(Action.playFile(uri, this, uploadStatusListener));
    }

    /**
     * Spielt den vorigen Track.
     */
    public void playPrevious() {
        serverConnect.executeAction(Action.playPrevious(this));
    }

    /**
     * Spielt den nächsten Track.
     */
    public void playNext() {
        serverConnect.executeAction(Action.playNext(this));
    }

    /**
     * Setzt den Farbmodus. Mögliche Werte: music, custom, colorCircle. Hat keine Auswirkung, falls
     * der Modus dadurch nicht geändert wird.
     *
     * @param mode Der neue Farbmodus
     */
    public void setColorMode(String mode) {
        if (mode != null && mode.length() > 0 && !mode.equals(colorMode)) {
            colorMode = mode;
            serverConnect.executeAction(Action.setColorMode(mode, this));
        }
    }

    /**
     * Gibt den Farbmodus zurück.
     *
     * @return Der aktuelle Farbmodus
     */
    public String getColorMode() {
        return colorMode;
    }

    /**
     * Setzt die Helligkeit der weißen LED
     *
     * @param brightness Zahl zwischen 0 und 100
     */
    public void setWhiteBrightness(int brightness) {
        serverConnect.executeAction(Action.setWhiteBrightness(brightness, this));
    }

    /**
     * Setzt die Lichtfarbe der RGB-LEDs.
     *
     * @param color Die Farbe als int in ARGB-Kodierung.
     */
    public void setRGBColor(int color) {
        currentColor = color;
        serverConnect.executeAction(Action.setColor(color, this));
    }

    /**
     * Setzt die Lichtfarbe der RGB-LEDs mit einzelnen Kanälen. Mögliche Werte zwischen 0 und 255
     *
     * @param red   Die rote Farbkomponente
     * @param green Die grüne Farbkomponente
     * @param blue  Die blaue Farbkomponente
     */
    public void setRGBColor(int red, int green, int blue) {
        currentColor = Color.rgb(red, green, blue);
        serverConnect.executeAction(Action.setRGBColor(red, green, blue, this));
    }

    /**
     * Gibt die letzte Wiedergabeposition zurück
     *
     * @return die letzte Wiedergabeposition
     */
    public int getPlayPosition() {
        return playPosition;
    }

    /**
     * Gibt die Länge des aktuellen Tracks in Sekunden zurück.
     *
     * @return Länge des aktuellen Tracks in Sekunden
     */
    public int getTrackLength() {
        return trackLength;
    }

    /**
     * Gibt den aktuellen Titel zurück.
     *
     * @return der aktuelle Titel
     */
    public String getCurrentTitle() {
        return currentTitle;
    }

    /**
     * Gibt den Künstler des aktuellen Tracks zurück.
     *
     * @return Künstler des aktuellen Tracks
     */
    public String getCurrentArtist() {
        return currentArtist;
    }

    /**
     * Gibt das Album des aktuellen Tracks zurück.
     *
     * @return Album des aktuellen Tracks
     */
    public String getCurrentAlbum() {
        return currentAlbum;
    }

    /**
     * Gibt die verbleibenden Sekunden zurück.
     *
     * @return entspricht getTrackLength - getPlayPosition
     */
    public int getRemainingSeconds() {
        return trackLength - playPosition;
    }

    /**
     * Gibt den Shufflemodus zurück.
     *
     * @return True, wenn Shufflemodus aktiv
     */
    public boolean isShuffle() {
        return shuffle;
    }

    /**
     * Wechselt in den anderen Shufflemodus (an/aus)
     */
    public void toggleShuffle() {
        shuffle = !shuffle;
        serverConnect.executeAction(Action.setShuffle(shuffle, this));
    }

    /**
     * Gibt den aktuellen Wiederholungsmodus wieder
     *
     * @return aktueller Wiederholungsmethode als REPEAT_MODE-Konstante
     */
    public int getRepeatMode() {
        return repeatMode;
    }

    /**
     * Wechselt in den nächsten Wiederholungsmodus
     */
    public void nextRepeatMode() {
        repeatMode = (repeatMode + 1) % 3;
        serverConnect.executeAction(Action.setRepeatMode(repeatMode, this));
    }

    /**
     * Gibt den Playbackstatus zurück.
     *
     * @return True, wenn aktuell ein Song gespielt wird.
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Wechselt zwischen Play/Pause
     */
    public void togglePlaying() {
        playing = !playing;
        serverConnect.executeAction(Action.togglePlayPause(this));
    }

    /**
     * Gibt die Helligkeit des weißen Farbkanals zurück
     *
     * @return Helligkeit des weißen Farbkanals als Zahl zwischen 0 und 100
     */
    public int getWhiteBrightness() {
        return whiteBrightness;
    }

    /**
     * Gibt die Helligkeit der RGB-LEDs zurück
     *
     * @return Die Helligkeit als Zahl zwischen 0 und 100
     */
    public int getColorBrightness() {
        return colorBrightness;
    }

    /**
     * Setzt die Helligkeit der RGB-LEDs.
     *
     * @param colorBrightness Zahl zwischen 0 und 100
     */
    public void setColorBrightness(int colorBrightness) {
        this.colorBrightness = colorBrightness;
        serverConnect.executeAction(Action.setColorBrightness(colorBrightness, this));
    }

    /**
     * Gibt die aktuelle Farbe zurück
     *
     * @return Farbwerd als int in ARGB-Kodierung
     */
    public int getCurrentColor() {
        return currentColor;
    }

    /**
     * Sendet einen String an den Server. Wird in einem Fenster ausgegeben, falls der Server auf
     * Windows ausgeführt wird.
     *
     * @param str Der zu sendende String
     */
    public void sendString(String str) {
        serverConnect.executeAction(Action.sendString(str));
    }
}
