package com.prog.showlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "showList";
    private static final int DB_VERSION = 5;

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

    // Used in v<1 block — only base columns exist at that point
    private static void insertShow(SQLiteDatabase db, String title, double rating, String description, int resourceId) {
        ContentValues values = new ContentValues();
        values.put("TITLE", title);
        values.put("RATING", rating);
        values.put("DESCRIPTION", description);
        values.put("IMAGE_RESOURCE_ID", resourceId);
        db.insert("SHOW", null, values);
    }

    // Used in v<5 block — STATUS, FAVORITE, and IMAGE_URL columns all exist by then
    private static void insertShowFull(SQLiteDatabase db, String title, double rating,
                                       String description, String status, boolean favorite) {
        ContentValues values = new ContentValues();
        values.put("TITLE", title);
        values.put("RATING", rating);
        values.put("DESCRIPTION", description);
        values.put("IMAGE_RESOURCE_ID", R.drawable.default_image);
        values.put("STATUS", status);
        values.put("FAVORITE", favorite ? 1 : 0);
        db.insert("SHOW", null, values);
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

            insertShow(db, "The Expanse", 8.5, "The disappearance of rich-girl-turned-political-activist links the lives of a Ceres detective, accidental ship captain, and U.N. politician. Amidst political tension between Earth, Mars, and the Belt, they unravel the greatest conspiracy in human history.", R.drawable.expanse);
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

        if (oldVersion < 5) {
            insertShowFull(db, "Breaking Bad", 9.5,
                    "A high school chemistry teacher diagnosed with inoperable lung cancer turns to manufacturing and selling methamphetamine to secure his family's future before he dies.",
                    "Completed", true);

            insertShowFull(db, "Chernobyl", 9.4,
                    "An exploration of one of the worst man-made catastrophes in history and of the brave men and women who sacrificed to save Europe from unimaginable disaster.",
                    "Completed", true);

            insertShowFull(db, "Succession", 8.9,
                    "The Roy family controls one of the biggest media and entertainment conglomerates in the world. But the patriarch's grip is faltering, and the whole family is at each other's throats.",
                    "Completed", true);

            insertShowFull(db, "Better Call Saul", 8.9,
                    "The story of small-time attorney Jimmy McGill as he transforms into morally challenged lawyer Saul Goodman, straddling the line between good and evil.",
                    "Plan to Watch", false);

            insertShowFull(db, "Dark", 8.8,
                    "A family saga with a supernatural twist, set in a German town where the disappearance of two young children exposes the tangled relationships among four families across multiple timelines.",
                    "Completed", false);

            insertShowFull(db, "The Last of Us", 8.8,
                    "Joel, a hardened survivor, is hired to smuggle Ellie out of an oppressive quarantine zone. What starts as a small job soon becomes a brutal, heartbreaking journey across a post-apocalyptic America.",
                    "Plan to Watch", false);

            insertShowFull(db, "Peaky Blinders", 8.8,
                    "A gangster family epic set in 1919 Birmingham, England, centred on a gang who sew razor blades in the peaks of their caps. Tommy Shelby navigates post-war crime and politics on his ruthless rise to power.",
                    "Plan to Watch", false);

            insertShowFull(db, "Game of Thrones", 9.2,
                    "Nine noble families fight for control over the lands of Westeros, while an ancient enemy returns after being dormant for millennia.",
                    "Completed", false);

            insertShowFull(db, "Stranger Things", 8.7,
                    "When a young boy disappears, his mother, a police chief, and his friends must confront terrifying supernatural forces in order to get him back — and uncover a secret government experiment.",
                    "Watching", false);

            insertShowFull(db, "The Mandalorian", 8.7,
                    "The travels of a lone bounty hunter in the outer reaches of the galaxy, far from the authority of the New Republic, and his unlikely bond with a mysterious alien child.",
                    "Watching", false);

            insertShowFull(db, "Severance", 8.7,
                    "Office workers have their memories surgically divided between their work and personal lives. When a mysterious colleague appears outside of work, it begins a journey to uncover the unsettling truth about their employer.",
                    "Plan to Watch", false);

            insertShowFull(db, "Squid Game", 8.0,
                    "Hundreds of cash-strapped players accept a strange invitation to compete in children's games. Inside, a tempting prize awaits — but losing means death.",
                    "Completed", false);
        }
    }
}
