package com.johanes.mymoviefavorite.ui;

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
    private Movie movie;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        String movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        movie = MovieDataSource.findById(this, movieId);
        if (movie == null) {
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.detailToolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        toolbar.setTitle(movie.getTitle());

        ImageView posterView = findViewById(R.id.imagePoster);
        TextView titleView = findViewById(R.id.textTitle);
        TextView metaView = findViewById(R.id.textMeta);
        TextView genresView = findViewById(R.id.textGenres);
        TextView overviewView = findViewById(R.id.textOverview);
        watchlistButton = findViewById(R.id.buttonWatchlist);
        backButton = findViewById(R.id.buttonBack);

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

        watchlistButton.setOnClickListener(v -> {
            boolean newState = MovieDataSource.toggleWatchlisted(this, movie.getId());
            movie = MovieDataSource.findById(this, movie.getId());
            updateWatchlistButton();
            int message = newState ? R.string.watchlist_added_message : R.string.watchlist_removed_message;
            Snackbar.make(v, message, Snackbar.LENGTH_SHORT).show();
        });
        backButton.setOnClickListener(v -> finish());
    }

    private void updateWatchlistButton() {
        boolean watchlisted = movie.isWatchlisted();
        watchlistButton.setText(watchlisted ? R.string.action_remove_watchlist : R.string.action_add_watchlist);
        watchlistButton.setIconResource(watchlisted ? R.drawable.ic_remove_watchlist : R.drawable.ic_add_watchlist);
    }
}
