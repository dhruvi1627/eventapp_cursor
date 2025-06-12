package com.example.eventapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.eventapp.models.Event;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class FavoritesRepository extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Favorites.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "FavoritesRepository";
    private final Gson gson = new Gson();

    // Table name
    public static final String TABLE_FAVORITES = "favorites";

    // Column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EVENT_ID = "event_id";
    public static final String COLUMN_USER_EMAIL = "user_email";
    public static final String COLUMN_EVENT_DATA = "event_data"; // JSON string of event data

    private static final String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EVENT_ID + " TEXT NOT NULL,"
            + COLUMN_USER_EMAIL + " TEXT NOT NULL,"
            + COLUMN_EVENT_DATA + " TEXT NOT NULL,"
            + "UNIQUE(" + COLUMN_EVENT_ID + "," + COLUMN_USER_EMAIL + ")"
            + ")";

    public FavoritesRepository(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    public boolean addFavorite(String userEmail, Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_EVENT_ID, event.getId());
        values.put(COLUMN_USER_EMAIL, userEmail);
        values.put(COLUMN_EVENT_DATA, gson.toJson(event));

        try {
            db.insertOrThrow(TABLE_FAVORITES, null, values);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error adding favorite", e);
            return false;
        }
    }

    public boolean removeFavorite(String userEmail, String eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_EVENT_ID + " = ? AND " + COLUMN_USER_EMAIL + " = ?";
        String[] whereArgs = {eventId, userEmail};

        try {
            int rowsDeleted = db.delete(TABLE_FAVORITES, whereClause, whereArgs);
            return rowsDeleted > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error removing favorite", e);
            return false;
        }
    }

    public List<Event> getFavorites(String userEmail) {
        List<Event> favorites = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_EVENT_DATA};
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {userEmail};

        try (Cursor cursor = db.query(TABLE_FAVORITES, columns, selection, selectionArgs, null, null, null)) {
            while (cursor.moveToNext()) {
                String eventJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DATA));
                Event event = gson.fromJson(eventJson, Event.class);
                if (event != null) {
                    favorites.add(event);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting favorites", e);
        }

        return favorites;
    }

    public boolean isFavorite(String userEmail, String eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_EVENT_ID + " = ? AND " + COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {eventId, userEmail};

        try (Cursor cursor = db.query(TABLE_FAVORITES, null, selection, selectionArgs, null, null, null)) {
            return cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, "Error checking favorite status", e);
            return false;
        }
    }
} 