package raleighnc.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventCardFinderTest {

    private EventCardFinder finder;

    @Test
    void findEventCard_prioritizesMainOverRegionContent() {
        String html = "<body>"
            + "<div class='region-content'>"
            + "<h1>Region Content</h1>"
            + "</div>"
            + "<main>"
            + "<h1>Main Content</h1>"
            + "</main>"
            + "</body>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.tagName()).isEqualTo("main");
    }

    @Test
    void findEventCard_prioritizesRegionContentOverArticle() {
        String html = "<body>"
            + "<article>"
            + "<h1>Article Content</h1>"
            + "</article>"
            + "<div class='region-content'>"
            + "<h1>Region Content</h1>"
            + "</div>"
            + "</body>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.className()).contains("region-content");
    }

    @Test
    void findEventCard_withArticle_findsContainer() {
        String html = "<article>"
            + "<h1>News Title</h1>"
            + "<p>News content</p>"
            + "</article>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.tagName()).isEqualTo("article");
    }

    @Test
    void findEventCard_withComplexNestedStructure() {
        String html = "<body>"
            + "<div class='page-wrapper'>"
            + "<div class='content-area'>"
            + "<main>"
            + "<div class='entry-content'>"
            + "<h1>News Title</h1>"
            + "<p>Description</p>"
            + "</div>"
            + "</main>"
            + "</div>"
            + "</div>"
            + "</body>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.tagName()).isEqualTo("main");
    }

    @Test
    void findEventCard_withEmptyDocument_returnsBody() {
        Document doc = Jsoup.parse("");

        Element card = finder.findEventCard(doc);

        assertThat(card.tagName()).isEqualTo("body");
    }

    @Test
    void findEventCard_withMainElement_findsContainer() {
        String html = "<main>"
            + "<h1>News Title</h1>"
            + "<p>News content</p>"
            + "</main>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.tagName()).isEqualTo("main");
    }

    @Test
    void findEventCard_withMainInsideRegionContent_prioritizesMain() {
        String html = "<body>"
            + "<div class='region-content'>"
            + "<main>"
            + "<h1>Main Content</h1>"
            + "</main>"
            + "</div>"
            + "</body>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.tagName()).isEqualTo("main");
    }

    @Test
    void findEventCard_withMultipleArticles_findsFirst() {
        String html = "<body>"
            + "<article id='first'>"
            + "<h1>First Article</h1>"
            + "</article>"
            + "<article id='second'>"
            + "<h1>Second Article</h1>"
            + "</article>"
            + "</body>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.id()).isEqualTo("first");
    }

    @Test
    void findEventCard_withMultipleMainElements_findsFirst() {
        String html = "<body>"
            + "<main id='first'>"
            + "<h1>First News</h1>"
            + "</main>"
            + "<main id='second'>"
            + "<h1>Second News</h1>"
            + "</main>"
            + "</body>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.id()).isEqualTo("first");
    }

    @Test
    void findEventCard_withNestedMain_findsOuterContainer() {
        String html = "<main>"
            + "<div class='header'>"
            + "<h1>News Title</h1>"
            + "</div>"
            + "<div class='content'>"
            + "<p>News content</p>"
            + "</div>"
            + "</main>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.tagName()).isEqualTo("main");
    }

    @Test
    void findEventCard_withNoMatchingSelectors_returnsBody() {
        String html = "<div class='container'>"
            + "<p>Regular content</p>"
            + "</div>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.tagName()).isEqualTo("body");
    }

    @Test
    void findEventCard_withOnlyBodyContent_returnsBody() {
        String html = "<p>Simple paragraph</p>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.tagName()).isEqualTo("body");
    }

    @Test
    void findEventCard_withRealWorldStructure() {
        String html = "<main>"
            + "<article>"
            + "<h1>Parks and Recreation Update</h1>"
            + "<div class='c-dateline__text'>"
            + "<time>June 15, 2026</time>"
            + "</div>"
            + "<div class='paragraph paragraph--type--stories-text'>"
            + "<p>Join us for summer programs at local parks.</p>"
            + "</div>"
            + "<img class='c-teaser__image' src='park.jpg'/>"
            + "</article>"
            + "</main>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.tagName()).isEqualTo("main");
        assertThat(card.select("h1").text()).contains("Parks and Recreation");
    }

    @Test
    void findEventCard_withRegionContentClass_findsContainer() {
        String html = "<div class='layout-container'>"
            + "<div class='region region-content'>"
            + "<h1>News Content</h1>"
            + "</div>"
            + "</div>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.className()).contains("region-content");
    }

    @Test
    void findEventCard_withRegionContent_findsContainer() {
        String html = "<div class='region-content'>"
            + "<h1>News Title</h1>"
            + "<p>News content</p>"
            + "</div>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.className()).contains("region-content");
    }

    @Test
    void findEventCard_withTeaserStructure() {
        String html = "<main>"
            + "<article>"
            + "<a class='c-teaser__title-link' href='/news/summer-events'>"
            + "<h2>Summer Events Announced</h2>"
            + "</a>"
            + "<div class='c-dateline__text'>"
            + "<time datetime='2026-06-15'>June 15</time>"
            + "</div>"
            + "<img class='c-teaser__image' src='event.jpg'/>"
            + "</article>"
            + "</main>";
        Document doc = Jsoup.parse(html);

        Element card = finder.findEventCard(doc);

        assertThat(card.tagName()).isEqualTo("main");
        assertThat(card.select("h2").text()).contains("Summer Events");
    }

    @BeforeEach
    void setUp() {
        finder = new EventCardFinder();
    }
}
