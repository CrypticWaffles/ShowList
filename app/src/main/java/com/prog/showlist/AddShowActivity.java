package com.prog.showlist;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddShowActivity extends Activity {
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_show);

        databaseHelper = new DatabaseHelper(this);

        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText titleInput = findViewById(R.id.title_input);
                EditText ratingInput = findViewById(R.id.rating_input);
                EditText descriptionInput = findViewById(R.id.description_input);

                String title = titleInput.getText().toString();
                double rating = Double.parseDouble(ratingInput.getText().toString());
                String description = descriptionInput.getText().toString();

                addShowToDatabase(title, rating, description);
            }
        });
    }

    private void addShowToDatabase(String title, double rating, String description) {
        SQLiteDatabase db = null;
        try {
            db = databaseHelper.getWritableDatabase();

            ContentValues showValues = new ContentValues();
            showValues.put("TITLE", title);
            showValues.put("RATING", rating);
            showValues.put("DESCRIPTION", description);
            showValues.put("IMAGE_RESOURCE_ID", R.drawable.default_image); // Set a default image

            db.insert("SHOW", null, showValues);

            Toast.makeText(this, "Show added successfully", Toast.LENGTH_SHORT).show();
            finish(); // Finish the activity and return to previous activity
        } catch (SQLiteException e) {
            Toast.makeText(this, "Failed to add show to database", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

}
