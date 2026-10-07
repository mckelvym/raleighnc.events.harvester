package raleighnc.events.config;

import java.time.Duration;

/**
 * Configuration interface for scraper settings.
 * Provides all necessary parameters for web scraping and RSS generation.
 */
public interface ScraperConfiguration {

    /**
     * Gets the base URL to scrape.
     *
     * @return the news listing URL
     */
    String getBaseUrl();

    /**
     * Gets the RSS feed description.
     *
     * @return the feed description
     */
    String getFeedDescription();

    /**
     * Gets the RSS feed link.
     *
     * @return the feed link
     */
    String getFeedLink();

    /**
     * Gets the RSS feed title.
     *
     * @return the feed title
     */
    String getFeedTitle();

    /**
     * Gets the timeout duration for page loads.
     *
     * @return the timeout duration
     */
    Duration getPageLoadTimeout();

    /**
     * Gets the number of days to retain news items in the feed.
     *
     * @return the retention days
     */
    int getRetentionDays();

    /**
     * Gets the user agent string for HTTP requests.
     *
     * @return the user agent string, or blank to derive it from the browser
     */
    String getUserAgent();

    /**
     * Gets the number of pages to fetch from the listing.
     *
     * @return the number of pages
     */
    int getPagesToFetch();

    /**
     * Gets how long to wait for a Cloudflare bot challenge to clear.
     *
     * @return the challenge timeout
     */
    Duration getChallengeTimeout();

    /**
     * Gets the base delay before each page request.
     * The actual pause is randomized between this value and three times it.
     *
     * @return the base request delay, or zero for no delay
     */
    Duration getRequestDelay();
}
