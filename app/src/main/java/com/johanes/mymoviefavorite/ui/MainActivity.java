package com.johanes.mymoviefavorite.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.johanes.mymoviefavorite.R;
import com.johanes.mymoviefavorite.data.SessionManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_MOVIES = "tag_movies";
    private static final String TAG_SEARCH = "tag_search";
    private static final String TAG_WATCHLIST = "tag_watchlist";
    private static final String TAG_ABOUT = "tag_about";

    private MaterialToolbar toolbar;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new android.content.Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_movies) {
                showFragment(new MovieListFragment(), TAG_MOVIES, getString(R.string.menu_movies));
                return true;
            } else if (itemId == R.id.navigation_search) {
                showFragment(new SearchFragment(), TAG_SEARCH, getString(R.string.menu_search));
                return true;
            } else if (itemId == R.id.navigation_watchlist) {
                showFragment(new WatchlistFragment(), TAG_WATCHLIST, getString(R.string.menu_watchlist));
                return true;
            } else if (itemId == R.id.navigation_about) {
                showFragment(new AboutFragment(), TAG_ABOUT, getString(R.string.menu_about));
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_movies);
        } else {
            restoreCurrentFragment(bottomNavigation.getSelectedItemId());
        }
    }

    private void showFragment(@NonNull Fragment fragment, @NonNull String tag, @NonNull String title) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        Fragment existing = fragmentManager.findFragmentByTag(tag);

        if (currentFragment != null && currentFragment != existing) {
            transaction.hide(currentFragment);
        }

        Fragment target;
        if (existing == null) {
            target = fragment;
            transaction.add(R.id.nav_host_fragment, target, tag);
        } else {
            target = existing;
            transaction.show(existing);
        }

        transaction.commit();
        currentFragment = target;
        toolbar.setTitle(title);
    }

    private void restoreCurrentFragment(int selectedItemId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment movies = fragmentManager.findFragmentByTag(TAG_MOVIES);
        Fragment search = fragmentManager.findFragmentByTag(TAG_SEARCH);
        Fragment watchlist = fragmentManager.findFragmentByTag(TAG_WATCHLIST);
        Fragment about = fragmentManager.findFragmentByTag(TAG_ABOUT);

        Fragment visible = null;
        if (movies != null && !movies.isHidden()) {
            visible = movies;
        } else if (search != null && !search.isHidden()) {
            visible = search;
        } else if (watchlist != null && !watchlist.isHidden()) {
            visible = watchlist;
        } else if (about != null && !about.isHidden()) {
            visible = about;
        }

        currentFragment = visible;
        updateToolbarTitle(selectedItemId);
    }

    private void updateToolbarTitle(int selectedItemId) {
        if (selectedItemId == R.id.navigation_movies) {
            toolbar.setTitle(R.string.menu_movies);
        } else if (selectedItemId == R.id.navigation_search) {
            toolbar.setTitle(R.string.menu_search);
        } else if (selectedItemId == R.id.navigation_watchlist) {
            toolbar.setTitle(R.string.menu_watchlist);
        } else if (selectedItemId == R.id.navigation_about) {
            toolbar.setTitle(R.string.menu_about);
        } else {
            toolbar.setTitle(R.string.app_name);
        }
    }
}
