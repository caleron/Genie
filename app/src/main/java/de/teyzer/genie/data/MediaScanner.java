package de.teyzer.genie.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import java.util.ArrayList;

import de.teyzer.genie.model.Track;

public class MediaScanner implements Prefs {

    /**
     * Prüft, ob sich der Medienbestand geändert hat und gleicht diesen bei Bedarf mit der Datenbank ab.
     *
     * @param activity    Eine Activity
     * @param dataManager Der Datamanager, der die neuen Songs erhält
     */
    public static void checkAndScanSongs(Activity activity, DataManager dataManager) {
        String mediaStoreVersion = MediaStore.getVersion(activity);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        String lastMediastoreVersion = sharedPreferences.getString(PREF_LAST_MEDIASTORE_VERSION, "");

        if (!mediaStoreVersion.equals(lastMediastoreVersion)) {
            boolean success = performSongScan(activity, dataManager);

            if (success) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_LAST_MEDIASTORE_VERSION, mediaStoreVersion);
                editor.apply();
            }
        }
    }


    private static boolean performSongScan(Context context, DataManager dataManager) {
        ArrayList<Track> songs = null;
        boolean noErrorOccured = true;

        //IS_MUSIC ist !=0, wenn es Musik ist
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        final String[] projection = new String[]{
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA};

        //Nach Titel sortieren
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
        Cursor cursor = null;

        try {
            //Abzufragende Tabelle setzen
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            //Abfrage ausführen
            cursor = context.getContentResolver().query(uri,
                    projection, selection, null, sortOrder);

            String title, artist, album, path;

            if (cursor != null) {
                songs = new ArrayList<>(cursor.getCount());
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    title = cursor.getString(0);
                    artist = cursor.getString(1);
                    album = cursor.getString(2);
                    path = cursor.getString(3);

                    if (path.toLowerCase().endsWith(".mp3")) {
                        Track track = new Track(-1, title, artist, album, path);
                        songs.add(track);
                    }
                    cursor.moveToNext();
                }
            }
        } catch (Exception ignored) {
            noErrorOccured = false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        dataManager.updateTracks(songs);
        return noErrorOccured;
    }
}
