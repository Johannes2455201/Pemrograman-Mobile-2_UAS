package com.johanes.mymoviefavorite.data;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents a movie entry used across the UI.
 */
public class Movie {

    private final String id;
    private final String title;
    private final String overview;
    private final List<String> genres;
    private final int year;
    private final int runtime;
    private final double rating;
    private boolean watchlisted;
    private final String imageUrl;

    public Movie(
            String id,
            String title,
            String overview,
            List<String> genres,
            int year,
            int runtime,
            double rating,
            boolean watchlisted,
            String imageUrl
    ) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.genres = genres == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(genres));
        this.year = year;
        this.runtime = runtime;
        this.rating = rating;
        this.watchlisted = watchlisted;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public List<String> getGenres() {
        return genres;
    }

    public int getYear() {
        return year;
    }

    public int getRuntime() {
        return runtime;
    }

    public double getRating() {
        return rating;
    }

    public boolean isWatchlisted() {
        return watchlisted;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setWatchlisted(boolean watchlisted) {
        this.watchlisted = watchlisted;
    }

    public String getGenresAsString() {
        return genres.isEmpty() ? "" : TextUtils.join(", ", genres);
    }

    public String formatMeta() {
        return String.format(Locale.US, "%d \u2022 %s \u2022 %d min", year, getGenresAsString(), runtime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return Objects.equals(id, movie.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
