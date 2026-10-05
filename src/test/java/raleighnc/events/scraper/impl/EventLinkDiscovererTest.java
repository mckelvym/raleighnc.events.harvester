package raleighnc.events.scraper.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventLinkDiscovererTest {

    private EventLinkDiscoverer discoverer;

    @Test
    void discoverLinks_convertsRelativeToAbsoluteUrls() {
        String html = "<html><body>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news/local-article\">Local</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://raleighnc.gov");

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUrl()).startsWith("https://raleighnc.gov");
    }

    @Test
    void discoverLinks_extractsDateFromDateline() {
        String html = "<html><body>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news/article\">Article</a>"
            + "<div class=\"c-dateline__text\">January 20, 2025</div>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://raleighnc.gov");

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2025, 1, 20));
    }

    @Test
    void discoverLinks_extractsDateFromTimeElement() {
        String html = "<html><body>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news/article\">Article</a>"
            + "<time datetime=\"2024-12-25\">Christmas 2024</time>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://raleighnc.gov");

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2024, 12, 25));
    }

    @Test
    void discoverLinks_fallbackToArticleLinks_whenNoTeaserLinks() {
        String html = "<html><body>"
            + "<article>"
            + "<a href=\"/news/fallback-article\">Fallback</a>"
            + "</article>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://raleighnc.gov");

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUrl()).contains("fallback-article");
    }

    @Test
    void discoverLinks_supportsMultipleDateFormats() {
        String html = "<html><body>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news/article1\">Article 1</a>"
            + "<div class=\"c-dateline__text\">Dec 5, 2024</div>"
            + "</div>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news/article2\">Article 2</a>"
            + "<div class=\"c-dateline__text\">December 15, 2024</div>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://raleighnc.gov");

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDate()).isNotNull();
        assertThat(result.get(1).getDate()).isNotNull();
    }

    @Test
    void discoverLinks_withDuplicateUrls_avoidsReturnsUnique() {
        String html = "<html><body>"
            + "<article>"
            + "<a href=\"/news/article\">Link 1</a>"
            + "<a href=\"/news/article\">Link 2</a>"
            + "<a href=\"/news/other\">Link 3</a>"
            + "</article>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://raleighnc.gov");

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    void discoverLinks_withEmptyDocument_returnsEmptyList() {
        String html = "<html><body></body></html>";
        Document doc = Jsoup.parse(html);

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).isEmpty();
    }

    @Test
    void discoverLinks_withInvalidDateFormat_acceptsNullDate() {
        String html = "<html><body>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news/article\">Article</a>"
            + "<div class=\"c-dateline__text\">Invalid Date</div>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://raleighnc.gov");

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isNull();
    }

    @Test
    void discoverLinks_withInvalidUrls_filtersThemOut() {
        String html = "<html><body>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news/valid-article\">Valid</a>"
            + "</div>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news\">Invalid - ends with /news</a>"
            + "</div>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news/article?page=2\">Invalid - has "
            + "query</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://raleighnc.gov");

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUrl()).contains("valid-article");
    }

    @Test
    void discoverLinks_withMissingDate_acceptsNullDate() {
        String html = "<html><body>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news/article\">Article</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://raleighnc.gov");

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isNull();
    }

    @Test
    void discoverLinks_withMultipleNews_returnsAllLinks() {
        String html = "<html><body>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news/article-one\">Article 1</a>"
            + "</div>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news/article-two\">Article 2</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://raleighnc.gov");

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUrl()).contains("article-one");
        assertThat(result.get(1).getUrl()).contains("article-two");
    }

    @Test
    void discoverLinks_withNoNews_returnsEmptyList() {
        String html = "<html><body>"
            + "<a href=\"/about/\">About</a>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://raleighnc.gov");

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).isEmpty();
    }

    @Test
    void discoverLinks_withSingleValidNewsLink_returnsOneLink() {
        String html = "<html><body>"
            + "<div class=\"c-teaser\">"
            + "<a class=\"c-teaser__title-link\" href=\"/news/article-one\">Article</a>"
            + "<div class=\"c-dateline__text\">Dec 15, 2024</div>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://raleighnc.gov");

        List<EventLink> result = discoverer.discoverLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUrl()).isEqualTo("https://raleighnc.gov/news/article-one");
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2024, 12, 15));
    }

    @BeforeEach
    void setUp() {
        discoverer = new EventLinkDiscoverer();
    }
}
