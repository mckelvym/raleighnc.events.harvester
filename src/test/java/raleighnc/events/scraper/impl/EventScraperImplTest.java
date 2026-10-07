package raleighnc.events.scraper.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import raleighnc.events.config.ScraperConfiguration;
import raleighnc.events.config.impl.ScraperConfigurationImpl;
import raleighnc.events.domain.EventItem;
import raleighnc.events.parser.impl.EventParserImpl;
import raleighnc.events.webdriver.PageLoader;

class EventScraperImplTest {

    private static final String SITE = "https://raleighnc.gov";
    private static final String ARTICLE_ONE = SITE + "/parks-and-recreation/news/article-one";
    private static final String ARTICLE_TWO = SITE + "/parks-and-recreation/news/article-two";

    private ScraperConfiguration config;
    private PageLoader pageLoader;
    private EventScraperImpl scraper;

    private static String teaser(final String url, final String title) {
        return "<article class=\"c-teaser\"><h3 class=\"c-teaser__title\">"
            + "<a class=\"c-teaser__title-link\" href=\"" + url + "\"><span>" + title
            + "</span></a></h3><p class=\"c-dateline\"><span class=\"c-dateline__text\">"
            + "Oct 7, 2026</span></p></article>";
    }

    private static Document listing() {
        return Jsoup.parse("<html><body>" + teaser(ARTICLE_ONE, "Article One")
            + teaser(ARTICLE_TWO, "Article Two") + "</body></html>", SITE);
    }

    @BeforeEach
    void setUp() {
        config = new ScraperConfigurationImpl();
        pageLoader = mock(PageLoader.class);
        scraper = new EventScraperImpl(config, pageLoader, new EventParserImpl(),
            new EventLinkDiscoverer());
    }

    @Test
    void scrapeEventsBuildsItemsFromListingWithoutLoadingArticlePages() {
        when(pageLoader.loadPage(config.getBaseUrl())).thenReturn(listing());

        final List<EventItem> events = scraper.scrapeEvents(Set.of());

        assertThat(events).extracting(EventItem::guid).containsExactly(ARTICLE_ONE, ARTICLE_TWO);
        assertThat(events).extracting(EventItem::title)
            .containsExactly("Article One", "Article Two");
        verify(pageLoader, times(1)).loadPage(anyString());
    }

    @Test
    void scrapeEventsSkipsExistingGuids() {
        when(pageLoader.loadPage(config.getBaseUrl())).thenReturn(listing());

        final List<EventItem> events = scraper.scrapeEvents(Set.of(ARTICLE_ONE));

        assertThat(events).extracting(EventItem::guid).containsExactly(ARTICLE_TWO);
    }

    @Test
    void scrapeEventsReturnsEmptyListWhenListingPageFails() {
        when(pageLoader.loadPage(anyString()))
            .thenThrow(new IllegalStateException("Bot challenge did not clear"));

        assertThat(scraper.scrapeEvents(Set.of())).isEmpty();
    }
}
