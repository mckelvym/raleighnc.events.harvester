package raleighnc.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import raleighnc.events.domain.EventItem;

class EventParserImplTest {

    private static final String URL =
        "https://raleighnc.gov/parks-and-recreation/news/peek-inside-city-parks-greenhouse";

    // Copied from the live listing page (2026-10-07), lazy-load attributes trimmed
    private static final String TEASER = "<article class=\"js--block-link c-teaser c-teaser--news"
        + " c-teaser--has-image\" data-href=\"/parks-and-recreation/news/"
        + "peek-inside-city-parks-greenhouse\"><div class=\"c-teaser__image\"><figure"
        + " class=\"media\"><img class=\"media__element b-lazy b-loaded\" srcset=\""
        + "/sites/default/files/styles/16_9_384x216/public/2026-10/city-parks-greenhouse.png 384w,"
        + " /sites/default/files/styles/16_9_768x432/public/2026-10/city-parks-greenhouse.png"
        + " 768w\" src=\"/sites/default/files/styles/16_9_384x216/public/2026-10/"
        + "city-parks-greenhouse.png\" alt=\"Greenhouse\"></figure></div>"
        + "<div class=\"c-teaser__content clearfix\"><h3 class=\"c-teaser__title\">"
        + "<a href=\"/parks-and-recreation/news/peek-inside-city-parks-greenhouse\""
        + " class=\"c-teaser__title-link\"><span>A Peek Inside the City Parks Greenhouse</span>"
        + " </a></h3><div class=\"c-teaser__dateline\"><p class=\"c-dateline\">"
        + "<span class=\"c-dateline__text\">Oct 7, 2026</span></p></div></div></article>";

    private EventParserImpl parser;

    private static Element teaser(final String html) {
        return Jsoup.parse("<html><body>" + html + "</body></html>", "https://raleighnc.gov")
            .selectFirst("article");
    }

    @BeforeEach
    void setUp() {
        parser = new EventParserImpl();
    }

    @Test
    void parseEvent_buildsItemFromTeaser() {
        final Optional<EventItem> result = parser.parseEvent(teaser(TEASER), URL);

        assertThat(result).isPresent();
        final EventItem item = result.get();
        assertThat(item.title()).isEqualTo("A Peek Inside the City Parks Greenhouse");
        assertThat(item.link()).isEqualTo(URL);
        assertThat(item.guid()).isEqualTo(URL);
        assertThat(item.eventDateStart()).isEqualTo(LocalDate.of(2026, 10, 7));
        assertThat(item.imageUrl()).isEqualTo("https://raleighnc.gov/sites/default/files/styles/"
            + "16_9_384x216/public/2026-10/city-parks-greenhouse.png");
    }

    @Test
    void parseEvent_teaserHasNoDescription() {
        final EventItem item = parser.parseEvent(teaser(TEASER), URL).orElseThrow();

        assertThat(item.sanitizedDescription()).isEmpty();
    }

    @Test
    void parseEvent_withoutImage_hasNoImage() {
        final String html = TEASER.replaceAll("<div class=\"c-teaser__image\">.*?</div>", "");

        final EventItem item = parser.parseEvent(teaser(html), URL).orElseThrow();

        assertThat(item.hasImage()).isFalse();
    }

    @Test
    void parseEvent_withoutTitle_returnsEmpty() {
        final Element bare = teaser("<article class=\"c-teaser\"></article>");

        assertThat(parser.parseEvent(bare, URL)).isEmpty();
    }

    @Test
    void parseEvent_rejectsNullArguments() {
        assertThatThrownBy(() -> parser.parseEvent(null, URL))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> parser.parseEvent(teaser(TEASER), null))
            .isInstanceOf(NullPointerException.class);
    }
}
