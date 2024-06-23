package com.prog.showlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "showList";
    private static final int DB_VERSION = 1;

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
            db.execSQL("ALTER TABLE SHOW ADD COLUMN FAVORITE NUMERIC;");
        }
    }
}