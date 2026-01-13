package com.johanes.mymoviefavorite.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.johanes.mymoviefavorite.R;
import com.johanes.mymoviefavorite.data.Movie;
import com.johanes.mymoviefavorite.data.MovieDataSource;
import com.johanes.mymoviefavorite.ui.adapter.MovieAdapter;

import java.util.List;

public class SearchFragment extends Fragment {

    private MovieAdapter adapter;
    private TextView emptyStateView;
    private TextInputEditText searchInput;
    private View rootView;

    private final MovieDataSource.MoviesListener moviesListener = new MovieDataSource.MoviesListener() {
        @Override
        public void onMoviesChanged(List<Movie> movies) {
            performSearch(getCurrentQuery());
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
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(R.string.menu_search);
        rootView = view;
        RecyclerView recyclerView = view.findViewById(R.id.recyclerSearchResults);
        emptyStateView = view.findViewById(R.id.textSearchEmpty);
        searchInput = view.findViewById(R.id.editSearch);

        adapter = new MovieAdapter(this::openMovieDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                performSearch(s != null ? s.toString() : "");
            }
        });

        performSearch("");
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

    private void performSearch(String query) {
        List<Movie> result = MovieDataSource.search(requireContext(), query);
        adapter.submitList(result);
        boolean isEmpty = result.isEmpty();
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void openMovieDetail(Movie movie) {
        Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.getId());
        startActivity(intent);
    }

    private String getCurrentQuery() {
        return searchInput != null && searchInput.getText() != null
                ? searchInput.getText().toString()
                : "";
    }
}
