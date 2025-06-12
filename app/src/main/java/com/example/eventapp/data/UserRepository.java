package com.example.eventapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class UserRepository extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Users.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "UserRepository";

    // Table name
    public static final String TABLE_USERS = "users";

    // Column names
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PROFILE_IMAGE = "profile_image";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_CREATED_AT = "created_at";

    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_EMAIL + " TEXT PRIMARY KEY,"
            + COLUMN_NAME + " TEXT NOT NULL,"
            + COLUMN_PROFILE_IMAGE + " TEXT,"
            + COLUMN_PASSWORD + " TEXT NOT NULL,"
            + COLUMN_CREATED_AT + " INTEGER NOT NULL"
            + ")";

    public UserRepository(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean updateUser(String email, String name, String profileImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, name);
        if (profileImage != null) {
            values.put(COLUMN_PROFILE_IMAGE, profileImage);
        }

        String whereClause = COLUMN_EMAIL + " = ?";
        String[] whereArgs = {email};

        try {
            int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);
            if (rowsAffected == 0) {
                // User doesn't exist, insert new record
                values.put(COLUMN_EMAIL, email);
                values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
                return db.insert(TABLE_USERS, null, values) != -1;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user", e);
            return false;
        }
    }

    public boolean updatePassword(String email, String hashedPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, hashedPassword);

        String whereClause = COLUMN_EMAIL + " = ?";
        String[] whereArgs = {email};

        try {
            int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);
            if (rowsAffected == 0) {
                // User doesn't exist, insert new record
                values.put(COLUMN_EMAIL, email);
                values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
                return db.insert(TABLE_USERS, null, values) != -1;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating password", e);
            return false;
        }
    }

    public JSONObject getUserProfile(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        JSONObject profile = new JSONObject();

        String[] columns = {COLUMN_NAME, COLUMN_PROFILE_IMAGE, COLUMN_PASSWORD};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        try (Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, 
                null, null, null)) {
            if (cursor.moveToFirst()) {
                profile.put("name", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                String profileImage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE));
                if (profileImage != null) {
                    profile.put("profileImage", profileImage);
                }
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                if (password != null) {
                    profile.put("password", password);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user profile", e);
        }

        return profile;
    }
} 