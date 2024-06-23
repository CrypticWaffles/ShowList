package com.prog.showlist;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class ShowActivity extends Activity {
    public static final String EXTRA_SHOWID = "showId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        // Get the show ID from the intent
        int showId = (Integer) getIntent().getExtras().get(EXTRA_SHOWID);

        SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
        try {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            Cursor cursor = db.query("SHOW",
                    new String[]{"TITLE", "RATING", "DESCRIPTION", "IMAGE_RESOURCE_ID"},
                    "_id = ?", new String[]{Integer.toString(showId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                String titleText = cursor.getString(0);
                double ratingNum = cursor.getDouble(1);
                String ratingText = ratingNum + "/10";
                String descriptionText = cursor.getString(2);
                int photoId = cursor.getInt(3);

                // Populate the UI elements
                TextView title = findViewById(R.id.title);
                title.setText(titleText);

                TextView rating = findViewById(R.id.rating);
                rating.setText(ratingText);

                TextView description = findViewById(R.id.description);
                description.setText(descriptionText);

                ImageView photo = findViewById(R.id.photo);
                photo.setImageResource(photoId);
                photo.setContentDescription(titleText);
            }

            cursor.close();
            db.close();
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
