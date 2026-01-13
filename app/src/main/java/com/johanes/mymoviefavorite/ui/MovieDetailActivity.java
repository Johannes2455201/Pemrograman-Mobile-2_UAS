package com.johanes.mymoviefavorite.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.johanes.mymoviefavorite.R;
import com.johanes.mymoviefavorite.data.Movie;
import com.johanes.mymoviefavorite.data.MovieDataSource;

import java.util.Locale;

public class MovieDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_ID = "extra_movie_id";

    private MaterialButton watchlistButton;
    private MaterialButton backButton;
    private MaterialButton editButton;
    private MaterialToolbar toolbar;
    private ImageView posterView;
    private TextView titleView;
    private TextView metaView;
    private TextView genresView;
    private TextView overviewView;
    private Movie movie;
    private String movieId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        movie = MovieDataSource.findById(this, movieId);
        if (movie == null) {
            finish();
            return;
        }

        toolbar = findViewById(R.id.detailToolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        posterView = findViewById(R.id.imagePoster);
        titleView = findViewById(R.id.textTitle);
        metaView = findViewById(R.id.textMeta);
        genresView = findViewById(R.id.textGenres);
        overviewView = findViewById(R.id.textOverview);
        watchlistButton = findViewById(R.id.buttonWatchlist);
        editButton = findViewById(R.id.buttonEdit);
        backButton = findViewById(R.id.buttonBack);

        bindMovie();

        watchlistButton.setOnClickListener(v -> {
            boolean newState = MovieDataSource.toggleWatchlisted(this, movie.getId());
            movie = MovieDataSource.findById(this, movie.getId());
            updateWatchlistButton();
            int message = newState ? R.string.watchlist_added_message : R.string.watchlist_removed_message;
            Snackbar.make(v, message, Snackbar.LENGTH_SHORT).show();
        });
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MovieFormActivity.class);
            intent.putExtra(MovieFormActivity.EXTRA_MOVIE_ID, movie.getId());
            startActivity(intent);
        });
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Movie updated = MovieDataSource.findById(this, movieId);
        if (updated == null) {
            finish();
            return;
        }
        movie = updated;
        bindMovie();
    }

    private void bindMovie() {
        toolbar.setTitle(movie.getTitle());
        Glide.with(this)
                .load(movie.getImageUrl())
                .placeholder(R.drawable.bg_movie_poster_placeholder)
                .error(R.drawable.bg_movie_poster_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(posterView);
        titleView.setText(movie.getTitle());

        String rating = String.format(Locale.US, "%.1f", movie.getRating());
        String meta = getString(R.string.label_meta_format,
                String.valueOf(movie.getYear()),
                getString(R.string.label_rating, rating),
                getString(R.string.label_runtime, movie.getRuntime()));
        metaView.setText(meta);
        genresView.setText(movie.getGenresAsString());
        overviewView.setText(movie.getOverview());
        updateWatchlistButton();
    }

    private void updateWatchlistButton() {
        boolean watchlisted = movie.isWatchlisted();
        watchlistButton.setText(watchlisted ? R.string.action_remove_watchlist : R.string.action_add_watchlist);
        watchlistButton.setIconResource(watchlisted ? R.drawable.ic_remove_watchlist : R.drawable.ic_add_watchlist);
    }
}
