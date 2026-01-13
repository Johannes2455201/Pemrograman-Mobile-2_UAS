package com.johanes.mymoviefavorite.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.johanes.mymoviefavorite.R;
import com.johanes.mymoviefavorite.data.Movie;
import com.johanes.mymoviefavorite.data.MovieDataSource;
import com.johanes.mymoviefavorite.data.SessionManager;

import java.util.List;

public class AboutFragment extends Fragment {

    private TextView watchlistTextView;
    private View rootView;

    private final MovieDataSource.MoviesListener moviesListener = new MovieDataSource.MoviesListener() {
        @Override
        public void onMoviesChanged(List<Movie> movies) {
            updateWatchlistSummary();
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
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(R.string.menu_about);
        rootView = view;
        watchlistTextView = view.findViewById(R.id.textAboutWatchlist);
        MaterialButton logoutButton = view.findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(v -> performLogout());
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

    private void updateWatchlistSummary() {
        int watchlistCount = MovieDataSource.getWatchlisted(requireContext()).size();
        watchlistTextView.setText(getString(R.string.about_watchlist_format, watchlistCount));
    }

    private void performLogout() {
        SessionManager.clearSession(requireContext());
        android.content.Intent intent = new android.content.Intent(requireContext(), LoginActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
