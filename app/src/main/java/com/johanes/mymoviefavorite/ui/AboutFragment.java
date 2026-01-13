package com.johanes.mymoviefavorite.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.johanes.mymoviefavorite.R;
import com.johanes.mymoviefavorite.data.MovieDataSource;

public class AboutFragment extends Fragment {

    private TextView watchlistTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(R.string.menu_about);
        watchlistTextView = view.findViewById(R.id.textAboutWatchlist);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWatchlistSummary();
    }

    private void updateWatchlistSummary() {
        int watchlistCount = MovieDataSource.getWatchlisted(requireContext()).size();
        watchlistTextView.setText(getString(R.string.about_watchlist_format, watchlistCount));
    }
}
