package com.johanes.mymoviefavorite.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.johanes.mymoviefavorite.R;
import com.johanes.mymoviefavorite.data.Movie;
import com.johanes.mymoviefavorite.data.MovieDataSource;

import java.util.ArrayList;
import java.util.List;

public class MovieFormActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_ID = "extra_movie_id";

    private TextInputLayout titleLayout;
    private TextInputLayout overviewLayout;
    private TextInputLayout genresLayout;
    private TextInputLayout yearLayout;
    private TextInputLayout runtimeLayout;
    private TextInputLayout ratingLayout;
    private TextInputLayout imageUrlLayout;

    private TextInputEditText titleInput;
    private TextInputEditText overviewInput;
    private TextInputEditText genresInput;
    private TextInputEditText yearInput;
    private TextInputEditText runtimeInput;
    private TextInputEditText ratingInput;
    private TextInputEditText imageUrlInput;

    private String movieId;
    private boolean watchlisted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_form);

        MaterialToolbar toolbar = findViewById(R.id.formToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        titleLayout = findViewById(R.id.layoutTitle);
        overviewLayout = findViewById(R.id.layoutOverview);
        genresLayout = findViewById(R.id.layoutGenres);
        yearLayout = findViewById(R.id.layoutYear);
        runtimeLayout = findViewById(R.id.layoutRuntime);
        ratingLayout = findViewById(R.id.layoutRating);
        imageUrlLayout = findViewById(R.id.layoutImageUrl);

        titleInput = findViewById(R.id.editTitle);
        overviewInput = findViewById(R.id.editOverview);
        genresInput = findViewById(R.id.editGenres);
        yearInput = findViewById(R.id.editYear);
        runtimeInput = findViewById(R.id.editRuntime);
        ratingInput = findViewById(R.id.editRating);
        imageUrlInput = findViewById(R.id.editImageUrl);

        MaterialButton saveButton = findViewById(R.id.buttonSave);
        MaterialButton cancelButton = findViewById(R.id.buttonCancel);

        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        if (!TextUtils.isEmpty(movieId)) {
            Movie movie = MovieDataSource.findById(this, movieId);
            if (movie == null) {
                Toast.makeText(this, R.string.error_movie_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            toolbar.setTitle(R.string.title_edit_movie);
            populateForm(movie);
        } else {
            toolbar.setTitle(R.string.title_add_movie);
        }

        saveButton.setOnClickListener(v -> handleSave());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void populateForm(Movie movie) {
        watchlisted = movie.isWatchlisted();
        titleInput.setText(movie.getTitle());
        overviewInput.setText(movie.getOverview());
        genresInput.setText(movie.getGenresAsString());
        yearInput.setText(String.valueOf(movie.getYear()));
        runtimeInput.setText(String.valueOf(movie.getRuntime()));
        ratingInput.setText(String.valueOf(movie.getRating()));
        imageUrlInput.setText(movie.getImageUrl());
    }

    private void handleSave() {
        clearErrors();

        String title = getText(titleInput);
        String overview = getText(overviewInput);
        String genresText = getText(genresInput);
        String yearText = getText(yearInput);
        String runtimeText = getText(runtimeInput);
        String ratingText = getText(ratingInput);
        String imageUrl = getText(imageUrlInput);

        boolean valid = true;
        valid &= requireField(titleLayout, title);
        valid &= requireField(overviewLayout, overview);
        valid &= requireField(genresLayout, genresText);
        valid &= requireField(yearLayout, yearText);
        valid &= requireField(runtimeLayout, runtimeText);
        valid &= requireField(ratingLayout, ratingText);
        valid &= requireField(imageUrlLayout, imageUrl);

        Integer year = parseInt(yearLayout, yearText);
        Integer runtime = parseInt(runtimeLayout, runtimeText);
        Double rating = parseDouble(ratingLayout, ratingText);
        if (year == null || runtime == null || rating == null) {
            valid = false;
        }

        if (!valid) {
            return;
        }

        List<String> genres = parseGenres(genresText);
        Movie movie = new Movie(
                TextUtils.isEmpty(movieId) ? null : movieId,
                title,
                overview,
                genres,
                year,
                runtime,
                rating,
                watchlisted,
                imageUrl
        );

        if (TextUtils.isEmpty(movieId)) {
            MovieDataSource.addMovie(movie, (success, message) -> handleResult(success, message));
        } else {
            MovieDataSource.updateMovie(movie, (success, message) -> handleResult(success, message));
        }
    }

    private void handleResult(boolean success, @Nullable String message) {
        if (success) {
            finish();
            return;
        }
        String errorMessage = message == null ? getString(R.string.error_save_movie) : message;
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private String getText(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void clearErrors() {
        titleLayout.setError(null);
        overviewLayout.setError(null);
        genresLayout.setError(null);
        yearLayout.setError(null);
        runtimeLayout.setError(null);
        ratingLayout.setError(null);
        imageUrlLayout.setError(null);
    }

    private boolean requireField(TextInputLayout layout, String value) {
        if (TextUtils.isEmpty(value)) {
            layout.setError(getString(R.string.error_required));
            return false;
        }
        return true;
    }

    @Nullable
    private Integer parseInt(TextInputLayout layout, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            if (!TextUtils.isEmpty(value)) {
                layout.setError(getString(R.string.error_invalid_number));
            }
            return null;
        }
    }

    @Nullable
    private Double parseDouble(TextInputLayout layout, String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            if (!TextUtils.isEmpty(value)) {
                layout.setError(getString(R.string.error_invalid_number));
            }
            return null;
        }
    }

    private List<String> parseGenres(String text) {
        List<String> genres = new ArrayList<>();
        if (TextUtils.isEmpty(text)) {
            return genres;
        }
        String[] parts = text.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                genres.add(trimmed);
            }
        }
        return genres;
    }
}
