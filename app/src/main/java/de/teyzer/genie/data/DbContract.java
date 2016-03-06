package de.teyzer.genie.data;

import android.provider.BaseColumns;

public final class DbContract {
    public DbContract() {
    }

    public static abstract class ProductEntry implements BaseColumns {
        public static final String TABLE_NAME = "products";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BARCODE = "barcode";
        public static final String COLUMN_STORE = "store";
        public static final String COLUMN_FOOD_TYPE = "food_type";
        public static final String COLUMN_QUANTITY = "quantity";

        public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME
                + " (" + _ID + " INTEGER PRIMARY KEY, "
                + COLUMN_NAME + " TEXT,"
                + COLUMN_BARCODE + " TEXT,"
                + COLUMN_STORE + " TEXT,"
                + COLUMN_FOOD_TYPE + " INTEGER,"
                + COLUMN_QUANTITY + " REAL);";
    }

    public static abstract class FoodTypeEntry implements BaseColumns {
        public static final String TABLE_NAME = "foodtypes";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_QUANTITY_TYPE = "quantity_type";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_PREFERRED_MEAL = "preferred_meal";
        public static final String COLUMN_COMMON_PACK_SIZE = "common_pack_size";

        public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME
                + " (" + _ID + " INTEGER PRIMARY KEY, "
                + COLUMN_NAME + " TEXT,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_QUANTITY + " REAL, "
                + COLUMN_QUANTITY_TYPE + " TEXT, "
                + COLUMN_PREFERRED_MEAL + " TEXT, "
                + COLUMN_COMMON_PACK_SIZE + " REAL);";
    }

    public static abstract class TrackEntry implements BaseColumns {
        public static final String TABLE_NAME = "tracks";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ARTIST = "artist";
        public static final String COLUMN_ALBUM = "album";
        public static final String COLUMN_PATH = "path";

        public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME
                + " (" + _ID + " INTEGER PRIMARY KEY, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_ARTIST + " TEXT, "
                + COLUMN_ALBUM + " TEXT, "
                + COLUMN_PATH + " TEXT);";

        public static final String PREPARED_INSERT = "INSERT INTO " + TABLE_NAME + " ("
                + COLUMN_TITLE + ", "
                + COLUMN_ARTIST + ", "
                + COLUMN_ALBUM + ", "
                + COLUMN_PATH + ") VALUES (?,?,?,?);";
    }

}
