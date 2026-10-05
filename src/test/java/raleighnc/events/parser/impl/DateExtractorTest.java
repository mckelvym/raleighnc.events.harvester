package raleighnc.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for DateExtractor.
 * Tests multi-strategy date extraction with multiple format support.
 */
class DateExtractorTest {

    private DateExtractor extractor;

    @Test
    void extractDate_withComplexStructure_findsLocalDate() {
        String html = """
            <div class="card">
                <div class="c-dateline">
                    <div class="c-dateline__text">April 5, 2026</div>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2026, Month.APRIL, 5));
    }

    // Tests for c-dateline__text strategy

    @Test
    void extractDate_withDatelineText_returnsLocalDate() {
        String html = """
            <div>
                <div class="c-dateline__text">Dec 15, 2025</div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractDate_withInvalidFormats_fallsBackToCurrentLocalDate() {
        String html = """
            <div>
                <div class="c-dateline__text">Not a date</div>
                <time datetime="not-valid">Invalid</time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.now());
    }

    @Test
    void extractDate_withNoDateElements_returnsCurrentLocalDate() {
        String html = "<div><p>No date information</p></div>";
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.now());
    }

    // Tests for time element strategy

    @Test
    void extractDate_withTimeElementDatetime_returnsLocalDate() {
        String html = """
            <div>
                <time datetime="2025-12-15">Event Date</time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractLocalDate_withAllMonths_parsesCorrectly() {
        String[][] testCases = {
            {"Jan 1, 2026", "2026-01-01"},
            {"February 15, 2026", "2026-02-15"},
            {"Mar 20, 2026", "2026-03-20"},
            {"December 31, 2025", "2025-12-31"}
        };

        for (String[] testCase : testCases) {
            String dateString = testCase[0];
            String expectedIso = testCase[1];
            LocalDate expected = LocalDate.parse(expectedIso);

            String html = String.format("""
                <div>
                    <div class="c-dateline__text">%s</div>
                </div>
                """, dateString);
            Element element = Jsoup.parse(html).body();

            LocalDate result = extractor.extractLocalDate(element);

            assertThat(result).as("Date string: " + dateString)
                .isEqualTo(expected);
        }
    }

    @Test
    void extractLocalDate_withBothStrategies_prefersDatelineText() {
        String html = """
            <div>
                <div class="c-dateline__text">Dec 15, 2025</div>
                <time datetime="2026-01-20">January 20, 2026</time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    // Tests for strategy priority

    @Test
    void extractLocalDate_withEmptyDatelineText_fallsBackToTime() {
        String html = """
            <div>
                <div class="c-dateline__text"></div>
                <time datetime="2025-12-15">Date</time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractLocalDate_withFullMonthFormat_parsesCorrectly() {
        String html = """
            <div>
                <div class="c-dateline__text">January 20, 2026</div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2026, Month.JANUARY, 20));
    }

    // Tests for fallback behavior

    @Test
    void extractLocalDate_withInvalidDatelineText_fallsBackToTime() {
        String html = """
            <div>
                <div class="c-dateline__text">Invalid Date</div>
                <time datetime="2025-12-15">December 15, 2025</time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractLocalDate_withIsoFormatInDatetime_parsesCorrectly() {
        String html = """
            <div>
                <time datetime="2025-12-15">Some text</time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractLocalDate_withMultipleDatelineTexts_usesFirst() {
        String html = """
            <div>
                <div class="c-dateline__text">Dec 15, 2025</div>
                <div class="c-dateline__text">Jan 20, 2026</div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    // Tests for edge cases

    @Test
    void extractLocalDate_withMultipleTimeElements_usesFirst() {
        String html = """
            <div>
                <time datetime="2025-12-15">Date 1</time>
                <time datetime="2026-01-20">Date 2</time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractLocalDate_withNestedElements_findsCorrectly() {
        String html = """
            <div>
                <div class="outer">
                    <div class="c-dateline__text">March 10, 2026</div>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2026, Month.MARCH, 10));
    }

    @Test
    void extractLocalDate_withPublishedTextDifferentFormat_extractsDate() {
        String html = """
            <div>
                <p>Article Published 3/15/2026 by Author</p>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2026, Month.MARCH, 15));
    }

    @Test
    void extractLocalDate_withPublishedTextInNestedElement_extractsDate() {
        String html = """
            <div>
                <div class="metadata">
                    <span>News Published 6/1/2026 Updated 6/2/2026</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2026, Month.JUNE, 1));
    }

    @Test
    void extractLocalDate_withPublishedTextNoUpdated_extractsDate() {
        String html = """
            <div>
                <p>Published 12/25/2025</p>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 25));
    }

    @Test
    void extractLocalDate_withPublishedText_extractsDate() {
        String html = """
            <div>
                <p>News Published 1/1/2026 Updated 1/1/2026</p>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2026, Month.JANUARY, 1));
    }

    // Tests for Published text strategy

    @Test
    void extractLocalDate_withShortMonthFormat_parsesCorrectly() {
        String html = """
            <div>
                <div class="c-dateline__text">Jan 20, 2026</div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2026, Month.JANUARY, 20));
    }

    @Test
    void extractLocalDate_withTimeElementText_parsesCorrectly() {
        String html = """
            <div>
                <time>Dec 15, 2025</time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractLocalDate_withTwoDigitDay_parsesCorrectly() {
        String html = """
            <div>
                <div class="c-dateline__text">Dec 05, 2025</div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 5));
    }

    @Test
    void extractLocalDate_withWhitespace_trimsCorrectly() {
        String html = """
            <div>
                <div class="c-dateline__text">  Dec 15, 2025  </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @BeforeEach
    void setUp() {
        extractor = new DateExtractor();
    }
}
