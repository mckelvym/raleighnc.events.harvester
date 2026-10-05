package raleighnc.events.scraper.impl;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Simple data class to hold a news URL and its publication date.
 * Used when discovering news links from listing pages.
 */
public final class EventLink {
    private final LocalDate date;
    private final String url;

    /**
     * Creates a new EventLink.
     *
     * @param url  the news article URL
     * @param date the publication date (may be null)
     */
    public EventLink(final String url, final LocalDate date) {
        this.url = Objects.requireNonNull(url, "url cannot be null");
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "EventLink{url='" + url + "', date=" + date + '}';
    }
}
