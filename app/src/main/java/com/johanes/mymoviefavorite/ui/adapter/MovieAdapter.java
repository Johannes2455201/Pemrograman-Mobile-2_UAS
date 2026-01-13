package com.johanes.mymoviefavorite.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.johanes.mymoviefavorite.R;
import com.johanes.mymoviefavorite.data.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    private final List<Movie> movies = new ArrayList<>();
    private final OnMovieClickListener listener;

    public MovieAdapter(OnMovieClickListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submitList(List<Movie> newMovies) {
        movies.clear();
        if (newMovies != null) {
            movies.addAll(newMovies);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.bind(movie, listener);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @Override
    public long getItemId(int position) {
        return movies.get(position).getId().hashCode();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final TextView textTitle;
        private final TextView textMeta;
        private final TextView textOverview;
        private final Chip chipRating;
        private final ImageView imagePoster;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            textTitle = itemView.findViewById(R.id.textTitle);
            textMeta = itemView.findViewById(R.id.textMeta);
            textOverview = itemView.findViewById(R.id.textOverview);
            chipRating = itemView.findViewById(R.id.chipRating);
            imagePoster = itemView.findViewById(R.id.imagePoster);
        }

        void bind(final Movie movie, final OnMovieClickListener clickListener) {
            textTitle.setText(movie.getTitle());

            String genresText = movie.getGenresAsString();
            String runtimeText = itemView.getContext()
                    .getString(R.string.label_runtime, movie.getRuntime());
            String meta = itemView.getContext()
                    .getString(R.string.label_meta_format,
                            String.valueOf(movie.getYear()),
                            genresText,
                            runtimeText);
            textMeta.setText(meta);
            textOverview.setText(movie.getOverview());

            String rating = String.format(Locale.US, "%.1f", movie.getRating());
            chipRating.setText(itemView.getContext().getString(R.string.label_rating, rating));

            Glide.with(imagePoster.getContext())
                    .load(movie.getImageUrl())
                    .placeholder(R.drawable.bg_movie_poster_placeholder)
                    .error(R.drawable.bg_movie_poster_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(imagePoster);

            int strokeColor = ContextCompat.getColor(itemView.getContext(),
                    movie.isWatchlisted() ? R.color.netflix_red : R.color.netflix_surface);
            cardView.setStrokeColor(strokeColor);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMovieClick(movie);
                }
            });
        }
    }
}
