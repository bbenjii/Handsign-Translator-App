package com.example.handsign_translator_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.handsign_translator_app.models.GestureLog;

import java.util.ArrayList;
import java.util.List;

public class GestureLogDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "GestureLogs.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_LOGS = "gesture_logs";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_GESTURE_LABEL = "gesture_label";
    private static final String COLUMN_TRANSLATION = "translation";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE_LOGS =
            "CREATE TABLE " + TABLE_LOGS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_GESTURE_LABEL + " TEXT, " +
                    COLUMN_TRANSLATION + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER)";

    public GestureLogDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LOGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        onCreate(db);
    }

    public long addGestureLog(GestureLog log) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GESTURE_LABEL, log.getGestureLabel());
        values.put(COLUMN_TRANSLATION, log.getTranslation());
        values.put(COLUMN_TIMESTAMP, log.getTimestamp());
        return db.insert(TABLE_LOGS, null, values);
    }

    public List<GestureLog> getAllLogs() {
        List<GestureLog> logs = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_LOGS + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                GestureLog log = new GestureLog(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GESTURE_LABEL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSLATION)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                );
                logs.add(log);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return logs;
    }

    public void clearAllLogs() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOGS, null, null);
    }
} 