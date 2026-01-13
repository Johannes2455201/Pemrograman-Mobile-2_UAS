package com.johanes.mymoviefavorite.data;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Simple in-memory data source that reads from a JSON asset file.
 */
public final class MovieDataSource {

    private static final String TAG = "MovieDataSource";
    private static final String FILE_NAME = "movies.json";

    private static List<Movie> cachedMovies;

    private MovieDataSource() {
    }

    public static List<Movie> getAllMovies(Context context) {
        ensureLoaded(context);
        return new ArrayList<>(cachedMovies);
    }

    public static List<Movie> getWatchlisted(Context context) {
        ensureLoaded(context);
        List<Movie> watchlisted = new ArrayList<>();
        for (Movie movie : cachedMovies) {
            if (movie.isWatchlisted()) {
                watchlisted.add(movie);
            }
        }
        return watchlisted;
    }

    public static List<Movie> search(Context context, String query) {
        ensureLoaded(context);
        if (query == null || query.trim().isEmpty()) {
            return getAllMovies(context);
        }
        String normalized = query.trim().toLowerCase(Locale.US);
        List<Movie> result = new ArrayList<>();
        for (Movie movie : cachedMovies) {
            if (movie.getTitle().toLowerCase(Locale.US).contains(normalized)
                    || movie.getOverview().toLowerCase(Locale.US).contains(normalized)
                    || movie.getGenresAsString().toLowerCase(Locale.US).contains(normalized)) {
                result.add(movie);
            }
        }
        return result;
    }

    public static Movie findById(Context context, String movieId) {
        ensureLoaded(context);
        if (movieId == null) {
            return null;
        }
        for (Movie movie : cachedMovies) {
            if (movieId.equals(movie.getId())) {
                return movie;
            }
        }
        return null;
    }

    public static boolean setWatchlisted(Context context, String movieId, boolean watchlisted) {
        ensureLoaded(context);
        Movie movie = findById(context, movieId);
        if (movie != null) {
            movie.setWatchlisted(watchlisted);
            return true;
        }
        return false;
    }

    public static boolean toggleWatchlisted(Context context, String movieId) {
        ensureLoaded(context);
        Movie movie = findById(context, movieId);
        if (movie != null) {
            movie.setWatchlisted(!movie.isWatchlisted());
            return movie.isWatchlisted();
        }
        return false;
    }

    private static synchronized void ensureLoaded(Context context) {
        if (cachedMovies != null) {
            return;
        }
        cachedMovies = loadMovies(context);
    }

    private static List<Movie> loadMovies(Context context) {
        try (InputStream inputStream = context.getAssets().open(FILE_NAME);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            JSONArray jsonArray = new JSONArray(builder.toString());
            List<Movie> movies = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                movies.add(parseMovie(item));
            }
            return movies;
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to load movie data", e);
            return Collections.emptyList();
        }
    }

    private static Movie parseMovie(JSONObject item) throws JSONException {
        String id = item.optString("id");
        String title = item.optString("title");
        String overview = item.optString("overview");

        List<String> genres = new ArrayList<>();
        JSONArray genresArray = item.optJSONArray("genres");
        if (genresArray != null) {
            for (int i = 0; i < genresArray.length(); i++) {
                genres.add(genresArray.optString(i));
            }
        }

        int year = item.optInt("year");
        int runtime = item.optInt("runtime");
        double rating = item.optDouble("rating");
        boolean watchlisted = item.optBoolean("watchlisted");
        String imageUrl = item.optString("imageUrl");

        return new Movie(id, title, overview, genres, year, runtime, rating, watchlisted, imageUrl);
    }
}
