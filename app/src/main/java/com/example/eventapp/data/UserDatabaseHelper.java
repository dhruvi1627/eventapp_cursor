package com.example.eventapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserAuth.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "UserDatabaseHelper";

    // Table name
    public static final String TABLE_USERS = "users";

    // Column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_NAME = "name";

    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
            + COLUMN_PASSWORD_HASH + " TEXT NOT NULL,"
            + COLUMN_NAME + " TEXT"
            + ")";

    public UserDatabaseHelper(Context context) {
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

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            return null;
        }
    }

    public long registerUser(String email, String password, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String passwordHash = hashPassword(password);
        if (passwordHash == null) return -1;

        values.put(COLUMN_EMAIL, email.toLowerCase());
        values.put(COLUMN_PASSWORD_HASH, passwordHash);
        values.put(COLUMN_NAME, name);

        try {
            return db.insertOrThrow(TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error registering user", e);
            return -1;
        }
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String passwordHash = hashPassword(password);
        if (passwordHash == null) return false;

        String[] columns = { COLUMN_ID };
        String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD_HASH + " = ?";
        String[] selectionArgs = { email.toLowerCase(), passwordHash };

        try (Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    public String getUserEmail() {
        // In a real app, you'd want to store the current user's email in SharedPreferences
        // This is just a placeholder implementation
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_EMAIL}, null, null, null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            }
        }
        return null;
    }
} 