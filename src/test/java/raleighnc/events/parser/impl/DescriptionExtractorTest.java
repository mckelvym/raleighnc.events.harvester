package raleighnc.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DescriptionExtractorTest {

    private DescriptionExtractor extractor;

    @Test
    void extract_withEmptyParagraphs_returnsNull() {
        String html = "<div>"
            + "<p></p>"
            + "<p>   </p>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element, false);

        assertThat(result).isNull();
    }

    @Test
    void extract_withFallbackParagraphsHtml_returnsJoinedHtml() {
        String html = "<div>"
            + "<p>A longer paragraph with more than 20 chars</p>"
            + "<p>Another long paragraph text here</p>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element, true);

        assertThat(result).isEqualTo("<p>A longer paragraph with more than 20 "
            + "chars</p><br/><br/><p>Another long paragraph text here</p>");
    }

    @Test
    void extract_withFallbackParagraphsText_returnsJoinedText() {
        String html = "<div>"
            + "<p>A longer paragraph with more than 20 chars</p>"
            + "<p>Another long paragraph text here</p>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element, false);

        assertThat(result).isEqualTo("A longer paragraph with more than 20 chars\n\nAnother long "
            + "paragraph text here");
    }

    @Test
    void extract_withLinksInHtml_preservesLinks() {
        String html = "<div>"
            + "<div class=\"paragraph paragraph--type--stories-text\">"
            + "<p>Check out <a href=\"/link\">this link</a></p>"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element, true);

        assertThat(result).contains("<a href");
    }

    @Test
    void extract_withNoMatchingParagraphs_returnsNull() {
        String html = "<div><span>No paragraphs</span></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element, false);

        assertThat(result).isNull();
    }

    @Test
    void extract_withShortParagraphs_ignoresShort() {
        String html = "<div>"
            + "<p>Short</p>"
            + "<p>This is a longer paragraph with more content</p>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element, false);

        assertThat(result).isEqualTo("This is a longer paragraph with more content");
    }

    @Test
    void extract_withStoryTextParagraphsHtml_returnsJoinedHtml() {
        String html = "<div>"
            + "<div class=\"paragraph paragraph--type--stories-text\">"
            + "<p>First paragraph</p>"
            + "<p>Second paragraph</p>"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element, true);

        assertThat(result).isEqualTo("<p>First paragraph</p><br/><br/><p>Second paragraph</p>");
    }

    @Test
    void extract_withStoryTextParagraphsText_returnsJoinedText() {
        String html = "<div>"
            + "<div class=\"paragraph paragraph--type--stories-text\">"
            + "<p>First paragraph</p>"
            + "<p>Second paragraph</p>"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element, false);

        assertThat(result).isEqualTo("First paragraph\n\nSecond paragraph");
    }

    @BeforeEach
    void setUp() {
        extractor = new DescriptionExtractor();
    }
}
