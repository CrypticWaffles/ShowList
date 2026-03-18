package com.prog.queued;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class TvMazeApiClient {

    private static final String SEARCH_URL = "https://api.tvmaze.com/search/shows?q=";

    public interface SearchCallback {
        void onSuccess(List<TvMazeShow> results);
        void onError(String message);
    }

    public static void search(String query, SearchCallback callback) {
        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(query, "UTF-8");
                URL url = new URL(SEARCH_URL + encoded);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("Accept", "application/json");

                int status = conn.getResponseCode();
                if (status != 200) {
                    callback.onError("Server returned " + status);
                    conn.disconnect();
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();

                callback.onSuccess(parseResponse(sb.toString()));

            } catch (Exception e) {
                callback.onError("Network error: " + e.getMessage());
            }
        }).start();
    }

    private static List<TvMazeShow> parseResponse(String json) throws Exception {
        List<TvMazeShow> results = new ArrayList<>();
        JSONArray array = new JSONArray(json);

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            JSONObject show = item.getJSONObject("show");

            String name = show.optString("name", "Unknown");

            double rating = 0;
            if (!show.isNull("rating")) {
                rating = show.getJSONObject("rating").optDouble("average", 0);
            }

            String summary = "";
            if (!show.isNull("summary")) {
                // Strip HTML tags from summary
                summary = show.getString("summary")
                        .replaceAll("<[^>]*>", "")
                        .trim();
            }

            String imageUrl = null;
            if (!show.isNull("image")) {
                imageUrl = show.getJSONObject("image").optString("medium", null);
            }

            results.add(new TvMazeShow(name, rating, summary, imageUrl));
        }

        return results;
    }
}
