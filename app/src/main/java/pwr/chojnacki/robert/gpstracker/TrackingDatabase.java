package pwr.chojnacki.robert.gpstracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

public final class TrackingDatabase {
    private static final String ID_TYPE = " INTEGER";
    private static final String ID_PARAMS = " PRIMARY KEY";
    private static final String STRING_TYPE = " TEXT";
    private static final String DATE_TYPE = " DATE";
    private static final String DATE_PARAMS = " NOT NULL DEFAULT CURRENT_DATE";
    private static final String TIME_TYPE = " TIME";
    private static final String TIME_PARAMS = " NOT NULL DEFAULT CURRENT_TIME";
    private static final String DOUBLE_TYPE = " REAL";
    private static final String DOUBLE_PARAMS = " NOT NULL";
    private static final String SEPARATOR = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TrackingRecord.TABLE_NAME + " (" +
                    TrackingRecord._ID + ID_TYPE + ID_PARAMS + SEPARATOR +
                    TrackingRecord.COLUMN_NAME_DATE + DATE_TYPE + DATE_PARAMS + SEPARATOR +
                    TrackingRecord.COLUMN_NAME_TIME + TIME_TYPE + TIME_PARAMS + SEPARATOR +
                    TrackingRecord.COLUMN_NAME_LATITUDE + DOUBLE_TYPE + DOUBLE_PARAMS + SEPARATOR +
                    TrackingRecord.COLUMN_NAME_LONGITUDE + DOUBLE_TYPE + DOUBLE_PARAMS + ");";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TrackingRecord.TABLE_NAME;

    private static TrackingDatabaseHelper helper = null;
    private static SQLiteDatabase db = null;

    public static void init(Context c) {
        helper = new TrackingDatabaseHelper(c);
    }

    public static ArrayList<TrackingRecordClass> select() {
        ArrayList<TrackingRecordClass> result = new ArrayList<>();
        if (helper != null) {
            // Get the database in read mode
            db = helper.getReadableDatabase();
            String[] projection = {
                    TrackingRecord._ID,
                    TrackingRecord.COLUMN_NAME_DATE,
                    TrackingRecord.COLUMN_NAME_TIME,
                    TrackingRecord.COLUMN_NAME_LATITUDE,
                    TrackingRecord.COLUMN_NAME_LONGITUDE
            };
            String sortOrder = TrackingRecord._ID + " DESC";

            Cursor c = db.query(
                    TrackingRecord.TABLE_NAME,       // The table to query
                    projection,                      // The columns to return
                    null,                            // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                            // don't group the rows
                    null,                            // don't filter by row groups
                    sortOrder                        // The sort order
            );

            while (c.moveToNext()) {
                TrackingRecordClass record = new TrackingRecordClass();
                record.id = c.getLong(c.getColumnIndex(TrackingRecord._ID));
                record.date = c.getString(c.getColumnIndex(TrackingRecord.COLUMN_NAME_DATE));
                record.time = c.getString(c.getColumnIndex(TrackingRecord.COLUMN_NAME_TIME));
                record.latitude = c.getDouble(c.getColumnIndex(TrackingRecord.COLUMN_NAME_LATITUDE));
                record.longitude = c.getDouble(c.getColumnIndex(TrackingRecord.COLUMN_NAME_LONGITUDE));
                result.add(record);
            }

            c.close();
            return result;
        } else {
            Log.e("TrackingDatabase", "Helper class not initialized");
            return null;
        }
    }

    public static long insert(double latitude, double longitude) {
        if (helper != null) {
            // Get the database in write mode
            db = helper.getWritableDatabase();
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(TrackingRecord.COLUMN_NAME_LATITUDE, latitude);
            values.put(TrackingRecord.COLUMN_NAME_LONGITUDE, longitude);
            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(TrackingRecord.TABLE_NAME, null, values);
            return newRowId;
        } else {
            Log.e("TrackingDatabase", "Helper class not initialized");
            return -1;
        }
    }

    public static long delete(long id) {
        if (helper != null) {
            // Get the database in write mode
            db = helper.getWritableDatabase();
            // Define 'where' part of query
            String selection = "_ID=?";
            // Specify arguments in placeholder order
            String[] selectionArgs = { String.valueOf(id) };
            // Issue SQL statement
            long deletedRowId = db.delete(TrackingRecord.TABLE_NAME, selection, selectionArgs);
            return deletedRowId;
        } else {
            Log.e("TrackingDatabase", "Helper class not initialized");
            return -1;
        }
    }

    /* Inner class that defines the table contents */
    public static class TrackingRecord implements BaseColumns {
        public static final String TABLE_NAME = "tracking_records";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
    }

    public static class TrackingRecordClass {
        public long id;
        public String date;
        public String time;
        public double latitude;
        public double longitude;
    }

    public static class TrackingDatabaseHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "TrackingDatabase.db";

        public TrackingDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}

