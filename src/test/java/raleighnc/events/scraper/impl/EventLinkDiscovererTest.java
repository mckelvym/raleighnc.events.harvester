package raleighnc.events.scraper.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventLinkDiscovererTest {

    private static final String SITE = "https://raleighnc.gov";

    private EventLinkDiscoverer discoverer;

    /**
     * A teaser as it appears on the live listing page (image markup trimmed).
     */
    private static String teaser(final String path, final String title, final String date) {
        return "<article class=\"js--block-link c-teaser c-teaser--news c-teaser--has-image\""
            + " data-href=\"" + path + "\">"
            + "<div class=\"c-teaser__image\"><img src=\"/sites/default/files/teaser.png\"></div>"
            + "<div class=\"c-teaser__content clearfix\"><h3 class=\"c-teaser__title\">"
            + "<a href=\"" + path + "\" class=\"c-teaser__title-link\"><span>" + title
            + "</span></a></h3>"
            + "<div class=\"c-teaser__dateline\"><p class=\"c-dateline\">"
            + "<span class=\"c-dateline__text\">" + date + "</span></p></div></div></article>";
    }

    private static Document page(final String body) {
        return Jsoup.parse("<html><body>" + body + "</body></html>", SITE);
    }

    @BeforeEach
    void setUp() {
        discoverer = new EventLinkDiscoverer();
    }

    @Test
    void discoverTeasers_keysTeaserContainersByAbsoluteUrl() {
        final Document doc = page(
            teaser("/parks-and-recreation/news/peek-inside-city-parks-greenhouse",
                "A Peek Inside the City Parks Greenhouse", "Oct 7, 2026"));

        final Map<String, Element> result = discoverer.discoverTeasers(doc);

        assertThat(result).containsOnlyKeys(
            SITE + "/parks-and-recreation/news/peek-inside-city-parks-greenhouse");
        final Element teaserElement = result.values().iterator().next();
        assertThat(teaserElement.hasClass("c-teaser")).isTrue();
        assertThat(teaserElement.select(".c-dateline__text").text()).isEqualTo("Oct 7, 2026");
    }

    @Test
    void discoverTeasers_keepsPageOrderAndDeduplicates() {
        final Document doc = page(
            teaser("/parks-and-recreation/news/first", "First", "Oct 7, 2026")
                + teaser("/parks-and-recreation/news/second", "Second", "Oct 6, 2026")
                + teaser("/parks-and-recreation/news/first", "First again", "Oct 7, 2026"));

        final Map<String, Element> result = discoverer.discoverTeasers(doc);

        assertThat(result.keySet()).containsExactly(
            SITE + "/parks-and-recreation/news/first",
            SITE + "/parks-and-recreation/news/second");
    }

    @Test
    void discoverTeasers_filtersInvalidUrls() {
        final Document doc = page(
            teaser("/parks-and-recreation/news/valid-article", "Valid", "Oct 7, 2026")
                + teaser("/news", "Ends with /news", "Oct 7, 2026")
                + teaser("/news/article?page=2", "Has query", "Oct 7, 2026")
                + teaser("https://example.com/news/elsewhere", "Other site", "Oct 7, 2026"));

        final Map<String, Element> result = discoverer.discoverTeasers(doc);

        assertThat(result).containsOnlyKeys(SITE + "/parks-and-recreation/news/valid-article");
    }

    @Test
    void discoverTeasers_fallsBackToArticleLinksWhenNoTeaserLinks() {
        final Document doc = page("<article><h3>Fallback</h3>"
            + "<a href=\"/news/fallback-article\">Fallback</a></article>");

        final Map<String, Element> result = discoverer.discoverTeasers(doc);

        assertThat(result).containsOnlyKeys(SITE + "/news/fallback-article");
        assertThat(result.values().iterator().next().tagName()).isEqualTo("article");
    }

    @Test
    void discoverTeasers_withNoNews_returnsEmptyMap() {
        assertThat(discoverer.discoverTeasers(page("<p>No news</p>"))).isEmpty();
    }

    @Test
    void discoverTeasers_rejectsNullDocument() {
        assertThatThrownBy(() -> discoverer.discoverTeasers(null))
            .isInstanceOf(NullPointerException.class);
    }
}
