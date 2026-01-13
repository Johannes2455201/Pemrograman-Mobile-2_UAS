package com.johanes.mymoviefavorite.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.johanes.mymoviefavorite.R;
import com.johanes.mymoviefavorite.data.Movie;
import com.johanes.mymoviefavorite.data.MovieDataSource;
import com.johanes.mymoviefavorite.ui.adapter.MovieAdapter;

import java.util.List;

public class MovieListFragment extends Fragment {

    private MovieAdapter adapter;
    private TextView emptyStateView;
    private View rootView;

    private final MovieDataSource.MoviesListener moviesListener = new MovieDataSource.MoviesListener() {
        @Override
        public void onMoviesChanged(List<Movie> movies) {
            adapter.submitList(movies);
            toggleEmptyState(movies.isEmpty());
        }

        @Override
        public void onError(String message) {
            if (rootView != null) {
                String text = TextUtils.isEmpty(message)
                        ? getString(R.string.error_generic)
                        : message;
                Snackbar.make(rootView, text, Snackbar.LENGTH_SHORT).show();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(R.string.menu_movies);
        rootView = view;
        RecyclerView recyclerView = view.findViewById(R.id.recyclerMovies);
        emptyStateView = view.findViewById(R.id.textEmptyState);
        FloatingActionButton addButton = view.findViewById(R.id.fabAddMovie);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MovieAdapter(this::openMovieDetail);
        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(v -> openMovieForm());
        setupSwipeToDelete(recyclerView);
    }

    @Override
    public void onStart() {
        super.onStart();
        MovieDataSource.addListener(requireContext(), moviesListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        MovieDataSource.removeListener(moviesListener);
    }

    private void toggleEmptyState(boolean isEmpty) {
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void openMovieDetail(Movie movie) {
        Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.getId());
        startActivity(intent);
    }

    private void openMovieForm() {
        Intent intent = new Intent(requireContext(), MovieFormActivity.class);
        startActivity(intent);
    }

    private void setupSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                Movie movie = adapter.getMovieAt(position);
                if (movie == null) {
                    adapter.notifyItemChanged(position);
                    return;
                }
                MovieDataSource.deleteMovie(movie.getId(), null);
                if (rootView != null) {
                    Snackbar.make(rootView, R.string.movie_deleted_message, Snackbar.LENGTH_SHORT).show();
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }
}
