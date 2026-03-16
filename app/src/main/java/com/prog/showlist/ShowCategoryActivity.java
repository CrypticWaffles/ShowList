package com.prog.showlist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShowCategoryActivity extends AppCompatActivity {

    private final List<Show> allShows = new ArrayList<>();
    private final List<Show> displayedShows = new ArrayList<>();
    private ShowAdapter adapter;
    private TextView emptyState;
    private String currentSort = "default";
    private boolean favoritesOnly = false;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_category);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recycler_shows);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShowAdapter(displayedShows);
        recyclerView.setAdapter(adapter);

        emptyState = findViewById(R.id.empty_state);

        adapter.setOnItemClickListener(show -> {
            Intent intent = new Intent(this, ShowActivity.class);
            intent.putExtra(ShowActivity.EXTRA_SHOWID, show.getId());
            startActivity(intent);
        });

        adapter.setOnItemLongClickListener((show, anchorView) -> {
            PopupMenu popup = new PopupMenu(this, anchorView);
            popup.getMenu().add(0, 0, 0, "Edit");
            popup.getMenu().add(0, 1, 1, "Delete");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    Intent intent = new Intent(this, AddShowActivity.class);
                    intent.putExtra(AddShowActivity.EXTRA_SHOWID, show.getId());
                    startActivity(intent);
                } else {
                    confirmDelete(show);
                }
                return true;
            });
            popup.show();
        });

        adapter.setOnFavoriteClickListener(show -> {
            boolean newFavorite = !show.isFavorite();
            show.setFavorite(newFavorite);
            updateFavoriteInDb(show.getId(), newFavorite);
            applyFiltersAndSort();
        });

        EditText searchInput = findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                applyFiltersAndSort();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        Spinner sortSpinner = findViewById(R.id.sort_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.sort_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1: currentSort = "name"; break;
                    case 2: currentSort = "rating"; break;
                    case 3: currentSort = "status"; break;
                    default: currentSort = "default"; break;
                }
                applyFiltersAndSort();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        ToggleButton favToggle = findViewById(R.id.btn_favorites);
        favToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            favoritesOnly = isChecked;
            applyFiltersAndSort();
        });

        FloatingActionButton fab = findViewById(R.id.fab_add_show);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddShowActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchShowsFromDatabase();
    }

    private void fetchShowsFromDatabase() {
        new Thread(() -> {
            List<Show> fetched = new ArrayList<>();
            DatabaseHelper databaseHelper = new DatabaseHelper(this);
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                db = databaseHelper.getReadableDatabase();
                cursor = db.query("SHOW",
                        new String[]{"_id", "TITLE", "RATING", "DESCRIPTION", "IMAGE_RESOURCE_ID", "STATUS", "FAVORITE", "IMAGE_URL"},
                        null, null, null, null, "_id ASC");
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    double rating = cursor.getDouble(2);
                    String description = cursor.getString(3);
                    int imageResourceId = cursor.getInt(4);
                    String status = cursor.getString(5);
                    boolean favorite = cursor.getInt(6) == 1;
                    String imageUrl = cursor.getString(7);
                    fetched.add(new Show(id, title, rating, description, imageResourceId, imageUrl, status, favorite));
                }
            } catch (SQLiteException e) {
                runOnUiThread(() -> Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT).show());
            } finally {
                if (cursor != null) cursor.close();
                if (db != null) db.close();
            }
            final List<Show> result = fetched;
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    allShows.clear();
                    allShows.addAll(result);
                    applyFiltersAndSort();
                }
            });
        }).start();
    }

    private void applyFiltersAndSort() {
        List<Show> result = new ArrayList<>(allShows);

        if (favoritesOnly) {
            List<Show> favs = new ArrayList<>();
            for (Show s : result) {
                if (s.isFavorite()) favs.add(s);
            }
            result = favs;
        }

        if (!searchQuery.isEmpty()) {
            String query = searchQuery.toLowerCase();
            List<Show> filtered = new ArrayList<>();
            for (Show s : result) {
                if (s.getTitle().toLowerCase().contains(query)) filtered.add(s);
            }
            result = filtered;
        }

        switch (currentSort) {
            case "name":
                Collections.sort(result, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
                break;
            case "rating":
                Collections.sort(result, (a, b) -> Double.compare(b.getRatingValue(), a.getRatingValue()));
                break;
            case "status":
                List<String> order = Arrays.asList("Watching", "Plan to Watch", "Completed", "Dropped");
                Collections.sort(result, (a, b) -> {
                    int ai = order.indexOf(a.getStatus());
                    int bi = order.indexOf(b.getStatus());
                    return Integer.compare(ai < 0 ? 99 : ai, bi < 0 ? 99 : bi);
                });
                break;
        }

        displayedShows.clear();
        displayedShows.addAll(result);
        adapter.notifyDataSetChanged();
        emptyState.setVisibility(displayedShows.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmDelete(Show show) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Show")
                .setMessage("Delete \"" + show.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteShow(show))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteShow(Show show) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.delete("SHOW", "_id = ?", new String[]{String.valueOf(show.getId())});
            allShows.remove(show);
            Toast.makeText(this, "\"" + show.getTitle() + "\" deleted", Toast.LENGTH_SHORT).show();
            applyFiltersAndSort();
        } catch (SQLiteException e) {
            Toast.makeText(this, "Failed to delete show", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    private void updateFavoriteInDb(int showId, boolean favorite) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("FAVORITE", favorite ? 1 : 0);
            db.update("SHOW", values, "_id = ?", new String[]{String.valueOf(showId)});
        } catch (SQLiteException e) {
            Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }
}
