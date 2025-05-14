// DatabaseHelper.java
package com.prac.learning;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user_data.db";
    private static final int DATABASE_VERSION = 2;


    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_INTERESTS = "interests";

    private static final String TABLE_HISTORY = "quiz_history";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "(" +
        COLUMN_USERNAME + " TEXT PRIMARY KEY, " +
        COLUMN_EMAIL + " TEXT, " +
        COLUMN_PASSWORD + " TEXT, " +
        COLUMN_INTERESTS + " TEXT);";

    private static final String CREATE_QUIZ_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "username TEXT, " +
        "question TEXT, " +
        "user_answer TEXT, " +
        "correct_answer TEXT, " +
        "options TEXT, " +
        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_QUIZ_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public boolean addUser(String username, String email, String password, String interests) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "INSERT INTO " + TABLE_USERS + " ("
            + COLUMN_USERNAME + ", "
            + COLUMN_EMAIL + ", "
            + COLUMN_PASSWORD + ", "
            + COLUMN_INTERESTS + ") VALUES ('"
            + username + "', '"
            + email + "', '"
            + password + "', '"
            + interests + "');";

        try {
            db.execSQL(query);
            db.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Cursor getUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        return db.rawQuery(query, new String[]{username});
    }

    public boolean updateUserInterests(String username, String interestsCsv) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INTERESTS, interestsCsv);

        int rows = db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?", new String[]{username});
        db.close();
        return rows > 0;
    }

    public void insertQuizHistory(String username, String question, String userAnswer, String correctAnswer, String optionsCsv) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("question", question);
        values.put("user_answer", userAnswer);
        values.put("correct_answer", correctAnswer);
        values.put("options", optionsCsv);
        db.insert(TABLE_HISTORY, null, values);
        db.close();
    }

    public Cursor getQuizHistoryForUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HISTORY + " WHERE username = ? ORDER BY timestamp DESC", new String[]{username});
    }
}
