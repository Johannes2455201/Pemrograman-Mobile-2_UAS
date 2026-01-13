package com.johanes.mymoviefavorite.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.johanes.mymoviefavorite.R;
import com.johanes.mymoviefavorite.data.Movie;
import com.johanes.mymoviefavorite.data.MovieDataSource;
import com.johanes.mymoviefavorite.ui.adapter.MovieAdapter;

import java.util.List;

public class WatchlistFragment extends Fragment {

    private MovieAdapter adapter;
    private TextView emptyStateView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(R.string.menu_watchlist);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerMovies);
        emptyStateView = view.findViewById(R.id.textEmptyState);
        emptyStateView.setText(R.string.empty_state_watchlist);

        adapter = new MovieAdapter(this::openMovieDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        renderWatchlist();
    }

    private void renderWatchlist() {
        List<Movie> watchlist = MovieDataSource.getWatchlisted(requireContext());
        adapter.submitList(watchlist);
        emptyStateView.setVisibility(watchlist.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openMovieDetail(Movie movie) {
        Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.getId());
        startActivity(intent);
    }
}
