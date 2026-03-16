package com.prog.showlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "showList";
    private static final int DB_VERSION = 4;

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateMyDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db, oldVersion, newVersion);
    }

    private static void insertShow(SQLiteDatabase db, String title, double rating, String description, int resourceId) {
        ContentValues showValues = new ContentValues();
        showValues.put("TITLE", title);
        showValues.put("RATING", rating);
        showValues.put("DESCRIPTION", description);
        showValues.put("IMAGE_RESOURCE_ID", resourceId);
        db.insert("SHOW", null, showValues);
    }

    // The original app (DB_VERSION=1) ran updateMyDatabase(db, 0, 1) on first install.
    // Because 0 < 2, the FAVORITE column was already added to every existing device.
    // Checking before ALTER TABLE prevents a "duplicate column" crash during upgrade.
    private boolean columnExists(SQLiteDatabase db, String table, String column) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
        try {
            int nameIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                if (column.equalsIgnoreCase(cursor.getString(nameIndex))) {
                    return true;
                }
            }
        } finally {
            cursor.close();
        }
        return false;
    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            db.execSQL("CREATE TABLE SHOW (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "TITLE TEXT, "
                    + "RATING REAL, "
                    + "DESCRIPTION TEXT, "
                    + "IMAGE_RESOURCE_ID INTEGER);");

            insertShow(db, "The Expanse", 8.5, "The disappearance of rich-girl-turned-political-activist links the lives of Ceres detective, accidental ship captain and U.N. politician. Amidst political tension between Earth, Mars and the Belt, they unravel the greatest conspiracy.", R.drawable.expanse);
            insertShow(db, "The Boys", 8.7, "A group of vigilantes set out to take down corrupt superheroes who abuse their superpowers.", R.drawable.boys);
            insertShow(db, "Vincenzo", 8.4, "During a visit to his motherland, a Korean-Italian Mafia lawyer gives an unrivaled conglomerate a taste of its own medicine with a side of justice.", R.drawable.vincenzo);
        }

        if (oldVersion < 2) {
            if (!columnExists(db, "SHOW", "FAVORITE")) {
                db.execSQL("ALTER TABLE SHOW ADD COLUMN FAVORITE INTEGER DEFAULT 0;");
            }
        }

        if (oldVersion < 3) {
            if (!columnExists(db, "SHOW", "STATUS")) {
                db.execSQL("ALTER TABLE SHOW ADD COLUMN STATUS TEXT DEFAULT 'Plan to Watch';");
            }
        }

        if (oldVersion < 4) {
            if (!columnExists(db, "SHOW", "IMAGE_URL")) {
                db.execSQL("ALTER TABLE SHOW ADD COLUMN IMAGE_URL TEXT;");
            }
        }
    }
}
