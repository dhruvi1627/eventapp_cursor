package com.example.eventapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.eventapp.models.Event;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "EventApp.db";
    private static final int DATABASE_VERSION = 2;

    // Table name
    public static final String TABLE_EVENTS = "events";

    // Column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_TICKETS = "tickets";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "event_date";
    public static final String COLUMN_IMAGE_BASE64 = "image_base64";
    private static final String COLUMN_IMAGE_PATH_OLD = "image_path";
    public static final String COLUMN_CREATED_BY = "created_by";

    private static final String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
            + COLUMN_ID + " TEXT PRIMARY KEY,"
            + COLUMN_TITLE + " TEXT,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_LOCATION + " TEXT,"
            + COLUMN_PRICE + " REAL,"
            + COLUMN_TICKETS + " INTEGER,"
            + COLUMN_CATEGORY + " TEXT,"
            + COLUMN_DATE + " INTEGER,"
            + COLUMN_IMAGE_BASE64 + " TEXT,"
            + COLUMN_CREATED_BY + " TEXT"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " RENAME TO " + TABLE_EVENTS + "_old");
            db.execSQL(CREATE_EVENTS_TABLE);
            db.execSQL("INSERT INTO " + TABLE_EVENTS + " ("
                    + COLUMN_ID + ", "
                    + COLUMN_TITLE + ", "
                    + COLUMN_DESCRIPTION + ", "
                    + COLUMN_LOCATION + ", "
                    + COLUMN_PRICE + ", "
                    + COLUMN_TICKETS + ", "
                    + COLUMN_CATEGORY + ", "
                    + COLUMN_DATE + ", "
                    + COLUMN_IMAGE_BASE64 + ", "
                    + COLUMN_CREATED_BY
                    + ") SELECT "
                    + COLUMN_ID + ", "
                    + COLUMN_TITLE + ", "
                    + COLUMN_DESCRIPTION + ", "
                    + COLUMN_LOCATION + ", "
                    + COLUMN_PRICE + ", "
                    + COLUMN_TICKETS + ", "
                    + COLUMN_CATEGORY + ", "
                    + COLUMN_DATE + ", "
                    + COLUMN_IMAGE_PATH_OLD + ", "
                    + COLUMN_CREATED_BY
                    + " FROM " + TABLE_EVENTS + "_old");
            db.execSQL("DROP TABLE " + TABLE_EVENTS + "_old");
        }
    }

    public long insertEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID, event.getId());
        values.put(COLUMN_TITLE, event.getTitle());
        values.put(COLUMN_DESCRIPTION, event.getDescription());
        values.put(COLUMN_LOCATION, event.getLocation());
        values.put(COLUMN_PRICE, event.getPrice());
        values.put(COLUMN_TICKETS, event.getCapacity());
        values.put(COLUMN_CATEGORY, event.getCategory());
        values.put(COLUMN_DATE, event.getDate().getTime());
        values.put(COLUMN_IMAGE_BASE64, event.getImageBase64());
        values.put(COLUMN_CREATED_BY, event.getOrganizer() != null ? event.getOrganizer().getId() : null);

        return db.insert(TABLE_EVENTS, null, values);
    }

    public boolean deleteEvent(String eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EVENTS, COLUMN_ID + "=?", new String[]{eventId}) > 0;
    }
} 