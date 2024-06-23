package com.prog.showlist;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class ShowCategoryActivity extends Activity {
    private ArrayList<Show> shows;
    private ShowAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_category);

        shows = new ArrayList<>();
        adapter = new ShowAdapter(this, shows);
        listView = findViewById(R.id.list_shows);
        listView.setAdapter(adapter);

        // Register listView for context menu (for long press)
        registerForContextMenu(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ShowCategoryActivity.this, ShowActivity.class);
                intent.putExtra(ShowActivity.EXTRA_SHOWID, position + 1); // +1 because SQLite _id starts from 1
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchShowsFromDatabase(); // Refresh data when activity resumes
    }

    private void fetchShowsFromDatabase() {
        SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("SHOW",
                    new String[]{"_id", "TITLE", "RATING", "DESCRIPTION", "IMAGE_RESOURCE_ID"},
                    null, null, null, null, null);

            shows.clear(); // Clear existing data before adding new data

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                double rating = cursor.getDouble(2);
                String description = cursor.getString(3);
                int imageResourceId = cursor.getInt(4);

                Show show = new Show(title, rating, description, imageResourceId);
                shows.add(show);
            }

            adapter.notifyDataSetChanged();
        } catch (SQLiteException e) {
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu);
        menu.clear(); // Clear existing menu items
        menu.add(Menu.NONE, MENU_ITEM_DELETE, Menu.NONE, "Delete"); // Add delete action dynamically
    }

    private static final int MENU_ITEM_DELETE = 1; // Constant for delete action

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        int itemId = item.getItemId();

        switch (itemId) {
            case MENU_ITEM_DELETE:
                confirmDelete(position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    private void confirmDelete(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this show?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteShow(position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteShow(int position) {
        SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        try {
            int showIdToDelete = position + 1; // Adjust based on your database schema
            db.delete("SHOW", "_id = ?", new String[]{String.valueOf(showIdToDelete)});
            Toast.makeText(this, "Show deleted", Toast.LENGTH_SHORT).show();
            fetchShowsFromDatabase(); // Refresh the list after deletion
        } catch (SQLiteException e) {
            Toast.makeText(this, "Failed to delete show", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    public void launchAddShowActivity(View view) {
        Intent intent = new Intent(this, AddShowActivity.class);
        startActivity(intent);
    }
}
