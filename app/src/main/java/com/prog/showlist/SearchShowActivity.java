package com.prog.showlist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchShowActivity extends AppCompatActivity {

    private static final int SEARCH_DELAY_MS = 400;

    private EditText searchInput;
    private ProgressBar progressBar;
    private TextView emptyState;
    private RecyclerView recyclerView;
    private SearchResultAdapter adapter;
    private final List<TvMazeShow> results = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_show);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search TVmaze");
        }

        searchInput = findViewById(R.id.search_input);
        progressBar = findViewById(R.id.progress_bar);
        emptyState = findViewById(R.id.empty_state);

        recyclerView = findViewById(R.id.recycler_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchResultAdapter(results);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(show -> {
            Intent result = new Intent();
            result.putExtra("name", show.getName());
            result.putExtra("rating", show.getRating());
            result.putExtra("summary", show.getSummary());
            result.putExtra("imageUrl", show.getImageUrl());
            setResult(RESULT_OK, result);
            finish();
        });

        findViewById(R.id.search_button).setOnClickListener(v -> performSearch());

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchHandler.removeCallbacks(pendingSearch);
                performSearch();
                return true;
            }
            return false;
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(pendingSearch);
                if (s.toString().trim().isEmpty()) {
                    results.clear();
                    adapter.notifyDataSetChanged();
                    emptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                pendingSearch = () -> performSearch();
                searchHandler.postDelayed(pendingSearch, SEARCH_DELAY_MS);
            }
        });
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            searchInput.setError("Enter a show name");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        TvMazeApiClient.search(query, new TvMazeApiClient.SearchCallback() {
            @Override
            public void onSuccess(List<TvMazeShow> searchResults) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    progressBar.setVisibility(View.GONE);
                    results.clear();
                    results.addAll(searchResults);
                    adapter.notifyDataSetChanged();
                    if (results.isEmpty()) {
                        emptyState.setText("No results for \"" + query + "\"");
                        emptyState.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SearchShowActivity.this, message, Toast.LENGTH_LONG).show();
                    emptyState.setText("Search failed. Check your connection.");
                    emptyState.setVisibility(View.VISIBLE);
                });
            }
        });
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
