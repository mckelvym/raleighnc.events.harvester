package raleighnc.events.config.impl;

import java.time.Duration;
import raleighnc.events.config.ScraperConfiguration;

/**
 * Configuration implementation for Raleigh NC news scraping.
 */
public final class ScraperConfigurationImpl implements ScraperConfiguration {

    private static final String BASE_URL =
            "https://raleighnc.gov/news?department=All&service=41";
    private static final Duration CHALLENGE_TIMEOUT = Duration.ofSeconds(30);
    private static final String FEED_DESCRIPTION =
            "Latest news from Raleigh Parks and Recreation";
    private static final String FEED_TITLE =
            "Raleigh NC Parks and Recreation News";
    /**
     * Only the first listing page: from the production server, later listing pages and all
     * article pages get a Cloudflare challenge that never clears. Page 1 holds the newest 12
     * teasers, several days of news, and the job runs several times a day.
     */
    private static final int PAGES_TO_FETCH = 1;
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_DELAY = Duration.ofSeconds(1);
    private static final int RETENTION_DAYS = 7;
    /**
     * Blank: derive the user agent from the browser (see ChromeDriverManager).
     * raleighnc.gov's Cloudflare challenge rejects user agents whose Chrome
     * version does not match the actual browser.
     */
    private static final String USER_AGENT = "";

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    public Duration getChallengeTimeout() {
        return CHALLENGE_TIMEOUT;
    }

    @Override
    public String getFeedDescription() {
        return FEED_DESCRIPTION;
    }

    @Override
    public String getFeedLink() {
        return getBaseUrl();
    }

    @Override
    public String getFeedTitle() {
        return FEED_TITLE;
    }

    @Override
    public Duration getPageLoadTimeout() {
        return PAGE_LOAD_TIMEOUT;
    }

    @Override
    public int getPagesToFetch() {
        return PAGES_TO_FETCH;
    }

    @Override
    public Duration getRequestDelay() {
        return REQUEST_DELAY;
    }

    @Override
    public int getRetentionDays() {
        return RETENTION_DAYS;
    }

    @Override
    public String getUserAgent() {
        return USER_AGENT;
    }

}
