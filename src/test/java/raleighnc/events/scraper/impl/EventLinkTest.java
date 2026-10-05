package raleighnc.events.scraper.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EventLinkTest {

    @Test
    void eventLink_toString_containsUrlAndDate() {
        String url = "https://raleighnc.gov/news/article";
        LocalDate date = LocalDate.of(2024, 12, 15);

        EventLink link = new EventLink(url, date);

        String result = link.toString();

        assertThat(result).contains(url);
        assertThat(result).contains(date.toString());
    }

    @Test
    void eventLink_toString_withNullDate_containsNull() {
        String url = "https://raleighnc.gov/news/article";

        EventLink link = new EventLink(url, null);

        String result = link.toString();

        assertThat(result).contains(url);
        assertThat(result).contains("null");
    }

    @Test
    void eventLink_withNullDate_allowsNullDate() {
        String url = "https://raleighnc.gov/news/article";

        EventLink link = new EventLink(url, null);

        assertThat(link.getUrl()).isEqualTo(url);
        assertThat(link.getDate()).isNull();
    }

    @Test
    void eventLink_withNullUrl_throwsNullPointerException() {
        LocalDate date = LocalDate.of(2024, 12, 15);

        assertThatThrownBy(() -> new EventLink(null, date))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("url cannot be null");
    }

    @Test
    void eventLink_withValidUrlAndDate_createsInstance() {
        String url = "https://raleighnc.gov/news/article-one";
        LocalDate date = LocalDate.of(2024, 12, 15);

        EventLink link = new EventLink(url, date);

        assertThat(link.getUrl()).isEqualTo(url);
        assertThat(link.getDate()).isEqualTo(date);
    }
}
