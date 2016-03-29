package de.teyzer.genie.connect;

public interface StatusChangedListener {

    /**
     * Wird ausgelöst, wenn der Serverstatus aktualisiert wurde
     *
     * @param newSong True, wenn ein neuer Song gespielt wird
     */
    void serverStatusChanged(boolean newSong);
}
