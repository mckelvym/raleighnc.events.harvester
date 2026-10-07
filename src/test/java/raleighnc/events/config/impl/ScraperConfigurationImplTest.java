package raleighnc.events.config.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ScraperConfigurationImpl.
 */
class ScraperConfigurationImplTest {

    private ScraperConfigurationImpl config;

    @BeforeEach
    void setUp() {
        config = new ScraperConfigurationImpl();
    }

    @Test
    void testGetBaseUrl() {
        assertThat(config.getBaseUrl())
                .isEqualTo("https://raleighnc.gov/news?department=All&service=41");
    }

    @Test
    void testGetPageLoadTimeout() {
        assertThat(config.getPageLoadTimeout().toSecondsPart())
                .isEqualTo(10);
    }

    @Test
    void testGetRetentionDays() {
        assertThat(config.getRetentionDays())
                .isEqualTo(7);
    }

    @Test
    void testGetChallengeTimeout() {
        assertThat(config.getChallengeTimeout())
                .isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void testGetRequestDelay() {
        assertThat(config.getRequestDelay())
                .isEqualTo(Duration.ofSeconds(1));
    }

    @Test
    void testGetUserAgent() {
        assertThat(config.getUserAgent())
                .isBlank();
    }

    @Test
    void testGetFeedTitle() {
        assertThat(config.getFeedTitle())
                .isEqualTo("Raleigh NC Parks and Recreation News");
    }

    @Test
    void testGetFeedDescription() {
        assertThat(config.getFeedDescription())
                .isEqualTo("Latest news from Raleigh Parks and Recreation");
    }

    @Test
    void testGetFeedLink() {
        assertThat(config.getFeedLink())
                .isEqualTo("https://raleighnc.gov/news?department=All&service=41");
    }

    @Test
    void testGetPagesToFetch() {
        assertThat(config.getPagesToFetch())
                .isEqualTo(1);
    }

}
