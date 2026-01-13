package com.johanes.mymoviefavorite.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Firebase-backed data source with a local cache for UI usage.
 */
public final class MovieDataSource {

    private static final String TAG = "MovieDataSource";
    private static final String FILE_NAME = "movies.json";
    private static final String PATH_MOVIES = "movies";

    private static final List<Movie> cachedMovies = new ArrayList<>();
    private static final Set<MoviesListener> listeners = new CopyOnWriteArraySet<>();

    private static DatabaseReference moviesRef;
    private static ValueEventListener moviesListener;
    private static Context appContext;
    private static boolean seeded;

    private MovieDataSource() {
    }

    public interface MoviesListener {
        void onMoviesChanged(List<Movie> movies);

        void onError(String message);
    }

    public interface CompletionListener {
        void onComplete(boolean success, @Nullable String message);
    }

    public static void addListener(Context context, MoviesListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
        ensureListening(context);
        listener.onMoviesChanged(new ArrayList<>(cachedMovies));
    }

    public static void removeListener(MoviesListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            detachListener();
        }
    }

    public static List<Movie> getAllMovies(Context context) {
        ensureListening(context);
        return new ArrayList<>(cachedMovies);
    }

    public static List<Movie> getWatchlisted(Context context) {
        ensureListening(context);
        List<Movie> watchlisted = new ArrayList<>();
        for (Movie movie : cachedMovies) {
            if (movie.isWatchlisted()) {
                watchlisted.add(movie);
            }
        }
        return watchlisted;
    }

    public static List<Movie> search(Context context, String query) {
        ensureListening(context);
        if (query == null || query.trim().isEmpty()) {
            return getAllMovies(context);
        }
        String normalized = query.trim().toLowerCase(Locale.US);
        List<Movie> result = new ArrayList<>();
        for (Movie movie : cachedMovies) {
            if (containsNormalized(movie.getTitle(), normalized)
                    || containsNormalized(movie.getOverview(), normalized)
                    || containsNormalized(movie.getGenresAsString(), normalized)) {
                result.add(movie);
            }
        }
        return result;
    }

    public static Movie findById(Context context, String movieId) {
        ensureListening(context);
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

    public static void addMovie(Movie movie, @Nullable CompletionListener listener) {
        if (movie == null) {
            notifyCompletion(listener, false, "Movie is null");
            return;
        }
        Movie target = movie;
        if (movie.getId() == null || movie.getId().trim().isEmpty()) {
            String newId = getMoviesReference().push().getKey();
            if (newId == null) {
                newId = java.util.UUID.randomUUID().toString();
            }
            target = new Movie(
                    newId,
                    movie.getTitle(),
                    movie.getOverview(),
                    movie.getGenres(),
                    movie.getYear(),
                    movie.getRuntime(),
                    movie.getRating(),
                    movie.isWatchlisted(),
                    movie.getImageUrl()
            );
        }
        DatabaseReference ref = getMoviesReference();
        ref.child(target.getId())
                .setValue(movieToMap(target))
                .addOnCompleteListener(task -> notifyCompletion(listener, task.isSuccessful(),
                        task.getException() != null ? task.getException().getMessage() : null));
    }

    public static void updateMovie(Movie movie, @Nullable CompletionListener listener) {
        if (movie == null || movie.getId() == null) {
            notifyCompletion(listener, false, "Movie or ID is null");
            return;
        }
        DatabaseReference ref = getMoviesReference();
        ref.child(movie.getId())
                .setValue(movieToMap(movie))
                .addOnCompleteListener(task -> notifyCompletion(listener, task.isSuccessful(),
                        task.getException() != null ? task.getException().getMessage() : null));
    }

    public static void deleteMovie(String movieId, @Nullable CompletionListener listener) {
        if (movieId == null) {
            notifyCompletion(listener, false, "Movie ID is null");
            return;
        }
        removeFromCache(movieId);
        notifyListeners();
        DatabaseReference ref = getMoviesReference();
        ref.child(movieId)
                .removeValue()
                .addOnCompleteListener(task -> notifyCompletion(listener, task.isSuccessful(),
                        task.getException() != null ? task.getException().getMessage() : null));
    }

    public static boolean setWatchlisted(Context context, String movieId, boolean watchlisted) {
        ensureListening(context);
        Movie movie = findById(context, movieId);
        if (movie != null) {
            movie.setWatchlisted(watchlisted);
            getMoviesReference().child(movieId).child("watchlisted").setValue(watchlisted);
            notifyListeners();
            return true;
        }
        return false;
    }

    public static boolean toggleWatchlisted(Context context, String movieId) {
        ensureListening(context);
        Movie movie = findById(context, movieId);
        if (movie != null) {
            boolean newState = !movie.isWatchlisted();
            movie.setWatchlisted(newState);
            getMoviesReference().child(movieId).child("watchlisted").setValue(newState);
            notifyListeners();
            return newState;
        }
        return false;
    }

    private static void notifyCompletion(@Nullable CompletionListener listener, boolean success, @Nullable String message) {
        if (listener != null) {
            listener.onComplete(success, message);
        }
    }

    private static synchronized void ensureListening(@Nullable Context context) {
        if (moviesListener != null) {
            return;
        }
        if (context != null) {
            appContext = context.getApplicationContext();
        }
        moviesRef = FirebaseDatabase.getInstance().getReference(PATH_MOVIES);
        moviesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChildren()) {
                    if (!seeded && appContext != null) {
                        seeded = true;
                        seedFromAssets(appContext);
                    }
                    cachedMovies.clear();
                    notifyListeners();
                    return;
                }

                List<Movie> movies = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Movie movie = parseSnapshot(child);
                    if (movie != null) {
                        movies.add(movie);
                    }
                }
                cachedMovies.clear();
                cachedMovies.addAll(movies);
                notifyListeners();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load movies", error.toException());
                for (MoviesListener listener : listeners) {
                    listener.onError(error.getMessage());
                }
            }
        };
        moviesRef.addValueEventListener(moviesListener);
    }

    private static synchronized void detachListener() {
        if (moviesRef != null && moviesListener != null) {
            moviesRef.removeEventListener(moviesListener);
        }
        moviesListener = null;
    }

    private static DatabaseReference getMoviesReference() {
        if (moviesRef == null) {
            moviesRef = FirebaseDatabase.getInstance().getReference(PATH_MOVIES);
        }
        return moviesRef;
    }

    private static void notifyListeners() {
        List<Movie> snapshot = new ArrayList<>(cachedMovies);
        for (MoviesListener listener : listeners) {
            listener.onMoviesChanged(snapshot);
        }
    }

    private static void removeFromCache(String movieId) {
        for (int i = cachedMovies.size() - 1; i >= 0; i--) {
            if (movieId.equals(cachedMovies.get(i).getId())) {
                cachedMovies.remove(i);
                return;
            }
        }
    }

    private static boolean containsNormalized(String value, String normalized) {
        return value != null && value.toLowerCase(Locale.US).contains(normalized);
    }

    @Nullable
    private static Movie parseSnapshot(DataSnapshot item) {
        String id = item.child("id").getValue(String.class);
        if (id == null) {
            id = item.getKey();
        }
        if (id == null) {
            return null;
        }

        String title = item.child("title").getValue(String.class);
        String overview = item.child("overview").getValue(String.class);
        int year = readInt(item, "year");
        int runtime = readInt(item, "runtime");
        double rating = readDouble(item, "rating");
        Boolean watchlistedValue = item.child("watchlisted").getValue(Boolean.class);
        String imageUrl = item.child("imageUrl").getValue(String.class);

        List<String> genres = new ArrayList<>();
        DataSnapshot genresSnapshot = item.child("genres");
        if (genresSnapshot.exists()) {
            for (DataSnapshot child : genresSnapshot.getChildren()) {
                String genre = child.getValue(String.class);
                if (genre != null) {
                    genres.add(genre);
                }
            }
        }

        boolean watchlisted = watchlistedValue != null && watchlistedValue;

        return new Movie(id, title, overview, genres, year, runtime, rating, watchlisted, imageUrl);
    }

    private static int readInt(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        return 0;
    }

    private static double readDouble(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ex) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private static void seedFromAssets(Context context) {
        List<Movie> movies = loadMoviesFromAssets(context);
        DatabaseReference ref = getMoviesReference();
        for (Movie movie : movies) {
            if (movie.getId() == null) {
                continue;
            }
            ref.child(movie.getId()).setValue(movieToMap(movie));
        }
    }

    private static List<Movie> loadMoviesFromAssets(Context context) {
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

    private static Map<String, Object> movieToMap(Movie movie) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", movie.getId());
        data.put("title", movie.getTitle());
        data.put("overview", movie.getOverview());
        data.put("genres", new ArrayList<>(movie.getGenres()));
        data.put("year", movie.getYear());
        data.put("runtime", movie.getRuntime());
        data.put("rating", movie.getRating());
        data.put("watchlisted", movie.isWatchlisted());
        data.put("imageUrl", movie.getImageUrl());
        return data;
    }
}
