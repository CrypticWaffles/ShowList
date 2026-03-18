package com.prog.queued;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AddShowActivity extends AppCompatActivity {
    public static final String EXTRA_SHOWID = "showId";

    private static final String[] STATUS_OPTIONS = {"Plan to Watch", "Watching", "Completed", "Dropped"};

    private DatabaseHelper databaseHelper;
    private int editShowId = -1;
    private String currentImageUrl = null;

    private ActivityResultLauncher<Intent> searchLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_show);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseHelper = new DatabaseHelper(this);

        // Register search launcher before any click handlers
        searchLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String name = data.getStringExtra("name");
                        double rating = data.getDoubleExtra("rating", 0);
                        String summary = data.getStringExtra("summary");
                        currentImageUrl = data.getStringExtra("imageUrl");

                        ((EditText) findViewById(R.id.title_input)).setText(name);
                        ((EditText) findViewById(R.id.rating_input)).setText(
                                rating > 0 ? String.valueOf(rating) : "");
                        ((EditText) findViewById(R.id.description_input)).setText(summary);
                    }
                });

        Spinner statusSpinner = findViewById(R.id.status_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, STATUS_OPTIONS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(spinnerAdapter);

        TextView screenTitle = findViewById(R.id.screen_title);
        Button saveButton = findViewById(R.id.add_button);

        if (getIntent().hasExtra(EXTRA_SHOWID)) {
            editShowId = getIntent().getIntExtra(EXTRA_SHOWID, -1);
            screenTitle.setText("Edit Show");
            saveButton.setText("Save Changes");
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Show");
            loadShowData(editShowId);
        } else {
            screenTitle.setText("Add Show");
            saveButton.setText("Add Show");
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add Show");
        }

        findViewById(R.id.search_online_button).setOnClickListener(v ->
                searchLauncher.launch(new Intent(this, SearchShowActivity.class)));

        saveButton.setOnClickListener(v -> {
            EditText titleInput = findViewById(R.id.title_input);
            EditText ratingInput = findViewById(R.id.rating_input);
            EditText descriptionInput = findViewById(R.id.description_input);

            String title = titleInput.getText().toString().trim();
            String ratingStr = ratingInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String status = STATUS_OPTIONS[statusSpinner.getSelectedItemPosition()];

            if (title.isEmpty()) {
                titleInput.setError("Title is required");
                return;
            }
            if (ratingStr.isEmpty()) {
                ratingInput.setError("Rating is required");
                return;
            }

            double rating;
            try {
                rating = Double.parseDouble(ratingStr);
            } catch (NumberFormatException e) {
                ratingInput.setError("Enter a valid number");
                return;
            }

            if (rating < 0 || rating > 10) {
                ratingInput.setError("Rating must be between 0 and 10");
                return;
            }

            if (editShowId != -1) {
                updateShowInDatabase(editShowId, title, rating, description, status);
            } else {
                addShowToDatabase(title, rating, description, status);
            }
        });
    }

    private void loadShowData(int showId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("SHOW",
                    new String[]{"TITLE", "RATING", "DESCRIPTION", "STATUS", "IMAGE_URL"},
                    "_id = ?", new String[]{String.valueOf(showId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                ((EditText) findViewById(R.id.title_input)).setText(cursor.getString(0));
                ((EditText) findViewById(R.id.rating_input)).setText(String.valueOf(cursor.getDouble(1)));
                ((EditText) findViewById(R.id.description_input)).setText(cursor.getString(2));

                String status = cursor.getString(3);
                currentImageUrl = cursor.getString(4);

                Spinner spinner = findViewById(R.id.status_spinner);
                for (int i = 0; i < STATUS_OPTIONS.length; i++) {
                    if (STATUS_OPTIONS[i].equals(status)) {
                        spinner.setSelection(i);
                        break;
                    }
                }
            }
        } catch (SQLiteException e) {
            Toast.makeText(this, "Failed to load show data", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    private void addShowToDatabase(String title, double rating, String description, String status) {
        SQLiteDatabase db = null;
        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("TITLE", title);
            values.put("RATING", rating);
            values.put("DESCRIPTION", description);
            values.put("IMAGE_RESOURCE_ID", R.drawable.default_image);
            values.put("IMAGE_URL", currentImageUrl);
            values.put("STATUS", status);
            db.insert("SHOW", null, values);
            Toast.makeText(this, "Show added", Toast.LENGTH_SHORT).show();
            finish();
        } catch (SQLiteException e) {
            Toast.makeText(this, "Failed to add show", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) db.close();
        }
    }

    private void updateShowInDatabase(int showId, String title, double rating, String description, String status) {
        SQLiteDatabase db = null;
        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("TITLE", title);
            values.put("RATING", rating);
            values.put("DESCRIPTION", description);
            values.put("STATUS", status);
            values.put("IMAGE_URL", currentImageUrl);
            // IMAGE_RESOURCE_ID preserved (not updated) to keep original drawable for pre-loaded shows
            db.update("SHOW", values, "_id = ?", new String[]{String.valueOf(showId)});
            Toast.makeText(this, "Show updated", Toast.LENGTH_SHORT).show();
            finish();
        } catch (SQLiteException e) {
            Toast.makeText(this, "Failed to update show", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) db.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
