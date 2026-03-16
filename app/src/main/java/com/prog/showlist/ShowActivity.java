package com.prog.showlist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

public class ShowActivity extends AppCompatActivity {
    public static final String EXTRA_SHOWID = "showId";

    private int showId;
    private boolean currentFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        showId = getIntent().getIntExtra(EXTRA_SHOWID, -1);
        loadShow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_edit) {
            Intent intent = new Intent(this, AddShowActivity.class);
            intent.putExtra(AddShowActivity.EXTRA_SHOWID, showId);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadShow() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("SHOW",
                    new String[]{"TITLE", "RATING", "DESCRIPTION", "IMAGE_RESOURCE_ID", "STATUS", "FAVORITE", "IMAGE_URL"},
                    "_id = ?", new String[]{String.valueOf(showId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                String titleText = cursor.getString(0);
                double ratingNum = cursor.getDouble(1);
                String descriptionText = cursor.getString(2);
                int photoId = cursor.getInt(3);
                String status = cursor.getString(4);
                currentFavorite = cursor.getInt(5) == 1;
                String imageUrl = cursor.getString(6);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(titleText);
                }

                ((TextView) findViewById(R.id.title)).setText(titleText);
                ((TextView) findViewById(R.id.rating)).setText(ratingNum + "/10");
                ((TextView) findViewById(R.id.description)).setText(descriptionText);

                ImageView photo = findViewById(R.id.photo);
                photo.setContentDescription(titleText);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).placeholder(R.drawable.default_image).into(photo);
                } else {
                    photo.setImageResource(photoId);
                }

                TextView statusView = findViewById(R.id.status);
                statusView.setText(status != null ? status : "Plan to Watch");
                switch (status != null ? status : "") {
                    case "Watching":
                        statusView.setTextColor(Color.parseColor("#1565C0")); break;
                    case "Completed":
                        statusView.setTextColor(Color.parseColor("#2E7D32")); break;
                    case "Dropped":
                        statusView.setTextColor(Color.parseColor("#C62828")); break;
                    default:
                        statusView.setTextColor(Color.parseColor("#757575")); break;
                }

                TextView favoriteView = findViewById(R.id.favorite_toggle);
                updateFavoriteView(favoriteView);
                favoriteView.setOnClickListener(v -> {
                    currentFavorite = !currentFavorite;
                    updateFavoriteView(favoriteView);
                    saveFavoriteToDb();
                });
            }
        } catch (SQLiteException e) {
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload in case the user edited and came back
        loadShow();
    }

    private void updateFavoriteView(TextView favoriteView) {
        favoriteView.setText(currentFavorite ? "★" : "☆");
        favoriteView.setTextColor(currentFavorite
                ? Color.parseColor("#FFC107")
                : Color.parseColor("#AAAAAA"));
    }

    private void saveFavoriteToDb() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("FAVORITE", currentFavorite ? 1 : 0);
            db.update("SHOW", values, "_id = ?", new String[]{String.valueOf(showId)});
        } catch (SQLiteException e) {
            Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }
}
