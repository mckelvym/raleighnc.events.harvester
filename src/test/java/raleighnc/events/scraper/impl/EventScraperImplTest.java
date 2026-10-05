package raleighnc.events.scraper.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import raleighnc.events.config.ScraperConfiguration;
import raleighnc.events.config.impl.ScraperConfigurationImpl;
import raleighnc.events.domain.EventItem;
import raleighnc.events.parser.EventParser;
import raleighnc.events.webdriver.PageLoader;

class EventScraperImplTest {

    private static final String SITE = "https://raleighnc.gov";
    private static final String ARTICLE_ONE = SITE + "/parks-and-recreation/news/article-one";
    private static final String ARTICLE_THREE = SITE + "/parks-and-recreation/news/article-three";

    private ScraperConfiguration config;
    private PageLoader pageLoader;
    private EventScraperImpl scraper;

    private static Document listing(final String articleUrl) {
        return Jsoup.parse("<html><body><div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"" + articleUrl + "\">Article</a>"
            + "</div></body></html>", SITE);
    }

    @BeforeEach
    void setUp() {
        config = new ScraperConfigurationImpl();
        pageLoader = mock(PageLoader.class);
        final EventParser eventParser = mock(EventParser.class);
        when(eventParser.parseEvent(any(Document.class), anyString())).thenAnswer(invocation -> {
            final String url = invocation.getArgument(1);
            return Optional.of(new EventItem(url, "Title", url, null, LocalDate.now(), null,
                null, null));
        });
        scraper = new EventScraperImpl(config, pageLoader, eventParser, new EventLinkDiscoverer());
    }

    @Test
    void scrapeEventsKeepsLinksFromOtherPagesWhenOneListingPageFails() {
        final String base = config.getBaseUrl();
        when(pageLoader.loadPage(anyString())).thenReturn(Jsoup.parse("<html></html>", SITE));
        when(pageLoader.loadPage(base)).thenReturn(listing(ARTICLE_ONE));
        when(pageLoader.loadPage(base + "&page=1"))
            .thenThrow(new IllegalStateException("Bot challenge did not clear for: page 2"));
        when(pageLoader.loadPage(base + "&page=2")).thenReturn(listing(ARTICLE_THREE));

        final List<EventItem> events = scraper.scrapeEvents(Set.of());

        assertThat(events).extracting(EventItem::guid)
            .containsExactlyInAnyOrder(ARTICLE_ONE, ARTICLE_THREE);
    }
}
