package de.teyzer.genie.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import de.teyzer.genie.model.Album;
import de.teyzer.genie.model.Artist;
import de.teyzer.genie.model.FoodType;
import de.teyzer.genie.model.Product;
import de.teyzer.genie.model.Track;

public class DataManager {
    private SparseArray<FoodType> foodTypes;
    private SparseArray<Product> products;

    private final ArrayList<Track> tracks;
    private final ArrayList<Artist> artists;
    private final ArrayList<Album> albums;

    private final DbHelper dbHelper;

    public DataManager(Context context) {
        dbHelper = new DbHelper(context);

        tracks = new ArrayList<>();
        artists = new ArrayList<>();
        albums = new ArrayList<>();
    }

    /**
     * Lädt alle Daten aus der Datenbank
     */
    public void loadData() {
        loadFoodTypes();
        loadProducts();
        loadTracks();
    }

    private void loadFoodTypes() {
        foodTypes = new SparseArray<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Nahrungsmitteltypen laden
        Cursor cursor = db.query(DbContract.FoodTypeEntry.TABLE_NAME, new String[]{"*"}, "", null, "", "", "");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.FoodTypeEntry._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.FoodTypeEntry.COLUMN_NAME));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.FoodTypeEntry.COLUMN_CATEGORY));
            String preferredMeal = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.FoodTypeEntry.COLUMN_PREFERRED_MEAL));
            String quantityType = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.FoodTypeEntry.COLUMN_QUANTITY_TYPE));
            double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(DbContract.FoodTypeEntry.COLUMN_QUANTITY));
            double commonPackSize = cursor.getDouble(cursor.getColumnIndexOrThrow(DbContract.FoodTypeEntry.COLUMN_COMMON_PACK_SIZE));

            FoodType foodType = new FoodType(id, name, category, quantity, quantityType, preferredMeal, commonPackSize);
            foodTypes.put(id, foodType);
        }
        cursor.close();
    }

    private void loadProducts() {
        products = new SparseArray<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Produkte laden
        Cursor cursor = db.query(DbContract.ProductEntry.TABLE_NAME, new String[]{"*"}, "", null, "", "", "");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.ProductEntry._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.ProductEntry.COLUMN_NAME));
            String barcode = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.ProductEntry.COLUMN_BARCODE));
            String store = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.ProductEntry.COLUMN_STORE));
            int foodTypeId = cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.ProductEntry.COLUMN_FOOD_TYPE));
            double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(DbContract.ProductEntry.COLUMN_QUANTITY));

            FoodType foodType = foodTypes.get(foodTypeId);

            Product product = new Product(id, name, store, foodType, quantity, barcode);
            products.put(id, product);
        }
        cursor.close();
    }

    private void loadTracks() {
        tracks.clear();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            //Produkte laden
            String sortOrder = DbContract.TrackEntry.COLUMN_TITLE + " ASC";
            Cursor cursor = db.query(DbContract.TrackEntry.TABLE_NAME, new String[]{"*"}, "", null, "", "", sortOrder);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.TrackEntry._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TrackEntry.COLUMN_TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TrackEntry.COLUMN_ARTIST));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TrackEntry.COLUMN_ALBUM));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TrackEntry.COLUMN_PATH));

                Track track = new Track(id, title, artist, album, path);
                tracks.add(track);
            }
            cursor.close();
        } catch (Exception ex) {
            db.execSQL(DbContract.TrackEntry.SQL_CREATE);
        }

        loadAlbumAndArtists();
    }

    private void loadAlbumAndArtists() {
        albums.clear();
        artists.clear();

        for (int i = 0, size = tracks.size(); i < size; i++) {
            Track track = tracks.get(i);

            //Künstler raussuchen
            Artist artist = null;
            for (Artist listArtist : artists) {
                if (track.getArtist().equals(listArtist.getName())) {
                    artist = listArtist;
                    break;
                }
            }

            //Wenn nicht gefunden, neu erstellen und hinzufügen
            if (artist == null) {
                artist = new Artist(track.getArtist());
                artists.add(artist);
            }

            //Album suchen
            Album album = null;
            for (Album listAlbum : albums) {
                if (track.getAlbum().equals(listAlbum.getAlbumName())) {
                    album = listAlbum;
                    break;
                }
            }
            //Wenn nicht gefunden, neu erstellen, in Liste einfügen und zum Künstler hinzufügen
            if (album == null) {
                //Neues Album hinzufügen
                album = new Album(track.getAlbum(), track.getArtist());
                albums.add(album);
                artist.addAlbum(album);
            }
            //Track zum Album hinzufügen
            album.addTrack(track);
        }

        //Alben sortieren
        Collections.sort(albums, new Comparator<Album>() {
            @Override
            public int compare(Album lhs, Album rhs) {
                return lhs.getAlbumName().compareTo(rhs.getAlbumName());
            }
        });

        //Künstler sortieren
        Collections.sort(artists, new Comparator<Artist>() {
            @Override
            public int compare(Artist lhs, Artist rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        System.out.println(albums.size() + " --- " + artists.size());
    }

    /**
     * Dient zum Hinzufügen eines neuen Nahrungsmitteltyps
     *
     * @param name          Der Name
     * @param category      Kategorie, etwa Kühlware
     * @param quantity      Anzahl einer Einheit
     * @param quantityType  Die Einheit, in der gemessen wird
     * @param preferredMeal Art, wie das Nahrungsmittel am wahrscheinlichsten verbraucht wird
     * @return ID des neuen Nahrungsmitteltyps
     */
    public int addFoodType(String name, String category, double quantity, String quantityType, String preferredMeal, double commonPackSize) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.FoodTypeEntry.COLUMN_NAME, name);
        values.put(DbContract.FoodTypeEntry.COLUMN_CATEGORY, category);
        values.put(DbContract.FoodTypeEntry.COLUMN_QUANTITY, quantity);
        values.put(DbContract.FoodTypeEntry.COLUMN_QUANTITY_TYPE, quantityType);
        values.put(DbContract.FoodTypeEntry.COLUMN_PREFERRED_MEAL, preferredMeal);
        values.put(DbContract.FoodTypeEntry.COLUMN_COMMON_PACK_SIZE, commonPackSize);

        int id = (int) db.insertOrThrow(DbContract.FoodTypeEntry.TABLE_NAME, null, values);

        FoodType foodType = new FoodType(id, name, category, quantity, quantityType, preferredMeal, commonPackSize);

        foodTypes.put(id, foodType);

        return id;
    }

    /**
     * Fügt ein neues Produkt hinzu
     *
     * @param name     Der Name
     * @param store    Der Laden, wo das Produkt gekauft werden kann
     * @param foodType Nahrungsmitteltyp, etwa Eier
     * @param quantity Menge in der vom Nahrungsmitteltyp gegebenen Einheit
     * @param barCode  Der Barcode des Produkts
     * @return ID des neuen Produkts
     */
    public Product addProduct(String name, String store, FoodType foodType, double quantity, String barCode) {
        //damit die Referenz mit Sicherheit richtig gesetzt wird
        foodType = foodTypes.get(foodType.getId());

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.ProductEntry.COLUMN_NAME, name);
        values.put(DbContract.ProductEntry.COLUMN_STORE, store);
        values.put(DbContract.ProductEntry.COLUMN_FOOD_TYPE, foodType.getId());
        values.put(DbContract.ProductEntry.COLUMN_QUANTITY, quantity);
        values.put(DbContract.ProductEntry.COLUMN_BARCODE, barCode);

        int id = (int) db.insert(DbContract.ProductEntry.TABLE_NAME, null, values);
        Product product = new Product(id, name, store, foodType, quantity, barCode);

        products.put(id, product);

        return product;
    }

    public FoodType productScanned(String barcode, Context context) {
        Product product = getProductWithBarcode(barcode);

        if (product == null) {
            return null;
        }
        FoodType foodType = product.getFoodType();

        addProductToQuantity(product);
        foodType.setLastAddedQuantity(product.getQuantity(), context);

        return foodType;
    }

    public Product getProductWithBarcode(String barcode) {
        int length = products.size();

        for (int i = 0; i < length; i++) {
            if (Objects.equals(products.valueAt(i).getBarCode(), barcode)) {
                return products.valueAt(i);
            }
        }
        return null;
    }

    public void addProductToQuantity(Product product) {
        FoodType foodType = product.getFoodType();

        updateFoodQuantity(foodType.getId(), foodType.getQuantity() + product.getQuantity());
    }

    public void addCommonPackSizeToFoodQuantity(int id) {
        FoodType foodType = foodTypes.get(id);
        updateFoodQuantity(id, foodType.getCommonPackSize() + foodType.getQuantity());
    }

    public void updateFoodQuantity(int id, double quantity) {
        foodTypes.get(id).setQuantity(quantity);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.FoodTypeEntry.COLUMN_QUANTITY, quantity);

        String selection = DbContract.FoodTypeEntry._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.update(DbContract.FoodTypeEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public void updateFoodType(int id, String name, String category, double quantity, String quantityType, String preferredMeal, double commonPackSize) {
        foodTypes.get(id).updateData(name, category, quantity, quantityType, preferredMeal, commonPackSize);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.FoodTypeEntry.COLUMN_NAME, name);
        values.put(DbContract.FoodTypeEntry.COLUMN_CATEGORY, category);
        values.put(DbContract.FoodTypeEntry.COLUMN_QUANTITY, quantity);
        values.put(DbContract.FoodTypeEntry.COLUMN_QUANTITY_TYPE, quantityType);
        values.put(DbContract.FoodTypeEntry.COLUMN_PREFERRED_MEAL, preferredMeal);
        values.put(DbContract.FoodTypeEntry.COLUMN_COMMON_PACK_SIZE, commonPackSize);

        String selection = DbContract.FoodTypeEntry._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.update(DbContract.FoodTypeEntry.TABLE_NAME, values, selection, selectionArgs);
    }


    public void updateProduct(int id, String name, String store, FoodType foodType, Double packSize, String barcode) {
        //damit die Referenz mit Sicherheit richtig gesetzt wird
        foodType = foodTypes.get(foodType.getId());

        products.get(id).updateProduct(name, store, foodType, packSize, barcode);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.ProductEntry.COLUMN_NAME, name);
        values.put(DbContract.ProductEntry.COLUMN_STORE, store);
        values.put(DbContract.ProductEntry.COLUMN_FOOD_TYPE, foodType.getId());
        values.put(DbContract.ProductEntry.COLUMN_QUANTITY, packSize);
        values.put(DbContract.ProductEntry.COLUMN_BARCODE, barcode);

        String selection = DbContract.ProductEntry._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.update(DbContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public void removeProduct(int id) {
        products.remove(id);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = DbContract.ProductEntry._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.delete(DbContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
    }

    public void removeFoodType(int id) {
        foodTypes.remove(id);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = DbContract.FoodTypeEntry._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.delete(DbContract.FoodTypeEntry.TABLE_NAME, selection, selectionArgs);
    }

    public SparseArray<FoodType> getFoodTypes() {
        return foodTypes;
    }

    public FoodType[] getFoodTypeArray() {
        FoodType[] arr = new FoodType[foodTypes.size()];
        for (int i = 0; i < foodTypes.size(); i++) {
            arr[i] = foodTypes.valueAt(i);
        }
        return arr;
    }

    public SparseArray<Product> getProducts() {
        return products;
    }

    /**
     * Gleicht die eingelesenen Tracks mit denen in der Datenbank ab
     *
     * @param newTracks Die neue Liste mit eingelesenen Tracks
     */
    public void updateTracks(ArrayList<Track> newTracks) {
        if (newTracks == null)
            return;

        ArrayList<Integer> deleteDbTracks = new ArrayList<>();

        for (int i = 0; i < tracks.size(); i++) {
            Track dbTrack = tracks.get(i);

            boolean found = false;
            for (Track newTrack : newTracks) {
                if (newTrack.equals(dbTrack)) {
                    found = true;
                    newTracks.remove(newTrack);
                    break;
                }
            }

            if (!found) {
                //Track ist nicht mehr vorhanden --> entfernen
                deleteDbTracks.add(dbTrack.getId());
            }
        }

        //Überschüssige DB-Einträge entfernen
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String ids = TextUtils.join(", ", deleteDbTracks.toArray());

        String sql = String.format("DELETE FROM " + DbContract.TrackEntry.TABLE_NAME + " WHERE " +
                DbContract.TrackEntry._ID + " IN (%s)", ids);
        db.execSQL(sql);

        Log.w("DataManager", deleteDbTracks.size() + " gelöscht ");

        //Neue Songs einfügen
        try {
            db.beginTransaction();
            SQLiteStatement statement = db.compileStatement(DbContract.TrackEntry.PREPARED_INSERT);
            for (Track newTrack : newTracks) {
                statement.clearBindings();

                statement.bindString(1, newTrack.getTitle());
                statement.bindString(2, newTrack.getArtist());
                statement.bindString(3, newTrack.getAlbum());
                statement.bindString(4, newTrack.getPath());

                statement.executeInsert();
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DataManager", e.toString());
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        Log.w("DataManager", newTracks.size() + " eingefügt");
        //Tracks neu laden
        loadTracks();
    }

    /**
     * Fügt einen neuen Track hinzu
     *
     * @param title  Titel
     * @param artist Künstler
     * @param album  Album
     * @param path   Pfad zur Datei
     * @return ID des neuen Produkts
     */
    public Track addTrack(String title, String artist, String album, String path) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.TrackEntry.COLUMN_TITLE, title);
        values.put(DbContract.TrackEntry.COLUMN_ARTIST, artist);
        values.put(DbContract.TrackEntry.COLUMN_ALBUM, album);
        values.put(DbContract.TrackEntry.COLUMN_PATH, path);

        int id = (int) db.insert(DbContract.TrackEntry.TABLE_NAME, null, values);
        Track track = new Track(id, title, artist, album, path);

        tracks.add(track);

        return track;
    }

    /**
     * Löscht einen Track
     *
     * @param id Die ID des zu löschenden Tracks
     */
    public void removeTrack(int id) {
        tracks.remove(id);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = DbContract.TrackEntry._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.delete(DbContract.TrackEntry.TABLE_NAME, selection, selectionArgs);
    }

    /**
     * Gibt die Liste aller Tracks zurück
     *
     * @return SparseArray aus Tracks
     */
    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public ArrayList<Artist> getArtists() {
        return artists;
    }
}
