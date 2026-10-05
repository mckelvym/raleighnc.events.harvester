package raleighnc.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for DateParser.
 * Verifies date parsing with multiple format strategies.
 */
class DateParserTest {

    private DateParser parser;

    @BeforeEach
    void setUp() {
        parser = new DateParser();
    }

    // Tests for parse() method - Full month name formats

    @Test
    void parse_withFullMonthSingleDigitDay_returnsLocalDate() {
        LocalDate result = parser.parse("December 5, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 5));
    }

    @Test
    void parse_withFullMonthDoubleDigitDay_returnsLocalDate() {
        LocalDate result = parser.parse("December 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withFullMonthPaddedDay_returnsLocalDate() {
        LocalDate result = parser.parse("January 05, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    // Tests for parse() method - Abbreviated month formats

    @Test
    void parse_withAbbreviatedMonthSingleDigitDay_returnsLocalDate() {
        LocalDate result = parser.parse("Dec 5, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 5));
    }

    @Test
    void parse_withAbbreviatedMonthDoubleDigitDay_returnsLocalDate() {
        LocalDate result = parser.parse("Dec 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withAbbreviatedMonthPaddedDay_returnsLocalDate() {
        LocalDate result = parser.parse("Jan 05, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    // Tests for parse() method - Slash formats

    @Test
    void parse_withSingleDigitSlashFormat_returnsLocalDate() {
        LocalDate result = parser.parse("1/5/2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    @Test
    void parse_withDoubleDigitSlashFormat_returnsLocalDate() {
        LocalDate result = parser.parse("12/15/2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withPaddedSlashFormat_returnsLocalDate() {
        LocalDate result = parser.parse("01/05/2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    // Tests for parse() method - ISO and RFC formats

    @Test
    void parse_withIsoFormat_returnsLocalDate() {
        LocalDate result = parser.parse("2025-12-15");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withRfc1123Format_returnsLocalDate() {
        LocalDate result = parser.parse("Mon, 15 Dec 2025 10:00:00 GMT");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withRfc1123WithNumericTimezone_returnsNull() {
        // RFC_1123_DATE_TIME doesn't parse numeric timezone offsets like -0500
        LocalDate result = parser.parse("Tue, 31 Dec 2025 23:59:59 -0500");

        assertThat(result).isNull();
    }

    // Tests for parse() method - Null and blank inputs

    @Test
    void parse_withNull_returnsNull() {
        LocalDate result = parser.parse(null);

        assertThat(result).isNull();
    }

    @Test
    void parse_withEmptyString_returnsNull() {
        LocalDate result = parser.parse("");

        assertThat(result).isNull();
    }

    @Test
    void parse_withBlankString_returnsNull() {
        LocalDate result = parser.parse("   ");

        assertThat(result).isNull();
    }

    @Test
    void parse_withTabsAndSpaces_returnsNull() {
        LocalDate result = parser.parse("\t  \t");

        assertThat(result).isNull();
    }

    // Tests for parse() method - Whitespace handling

    @Test
    void parse_withLeadingWhitespace_trimsAndParses() {
        LocalDate result = parser.parse("  December 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withTrailingWhitespace_trimsAndParses() {
        LocalDate result = parser.parse("December 15, 2025  ");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withSurroundingWhitespace_trimsAndParses() {
        LocalDate result = parser.parse("  December 15, 2025  ");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    // Tests for parse() method - Invalid inputs

    @Test
    void parse_withInvalidFormat_returnsNull() {
        LocalDate result = parser.parse("not a date");

        assertThat(result).isNull();
    }

    @Test
    void parse_withPartialDate_returnsNull() {
        LocalDate result = parser.parse("December 2025");

        assertThat(result).isNull();
    }

    @Test
    void parse_withInvalidDay_returnsNull() {
        LocalDate result = parser.parse("December 32, 2025");

        assertThat(result).isNull();
    }

    @Test
    void parse_withInvalidMonth_returnsNull() {
        LocalDate result = parser.parse("13/15/2025");

        assertThat(result).isNull();
    }

    @Test
    void parse_withGarbageText_returnsNull() {
        LocalDate result = parser.parse("xyz123");

        assertThat(result).isNull();
    }

    // Tests for parse() method - Edge cases

    @Test
    void parse_withLeapYearDate_returnsLocalDate() {
        LocalDate result = parser.parse("February 29, 2024");

        assertThat(result).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    void parse_withInvalidLeapYearDate_returnsDate() {
        // DateTimeFormatter is lenient and adjusts Feb 29, 2025 to Feb 28, 2025
        LocalDate result = parser.parse("February 29, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    void parse_withJanuaryFirst_returnsLocalDate() {
        LocalDate result = parser.parse("January 1, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void parse_withDecemberThirtyFirst_returnsLocalDate() {
        LocalDate result = parser.parse("December 31, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    // Tests for parse() method - Multiple format attempts

    @Test
    void parse_withMixedFormatInput_parsesCorrectly() {
        // This tests that the parser tries multiple formatters
        LocalDate result1 = parser.parse("March 15, 2025");
        LocalDate result2 = parser.parse("Mar 15, 2025");
        LocalDate result3 = parser.parse("3/15/2025");
        LocalDate result4 = parser.parse("2025-03-15");

        assertThat(result1).isEqualTo(LocalDate.of(2025, 3, 15));
        assertThat(result2).isEqualTo(LocalDate.of(2025, 3, 15));
        assertThat(result3).isEqualTo(LocalDate.of(2025, 3, 15));
        assertThat(result4).isEqualTo(LocalDate.of(2025, 3, 15));
    }
}
