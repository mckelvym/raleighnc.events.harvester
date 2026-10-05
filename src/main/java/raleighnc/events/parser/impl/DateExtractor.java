package raleighnc.events.parser.impl;

import static java.util.Objects.requireNonNull;
import static raleighnc.events.parser.impl.CssSelectors.DATE_DATELINE_TEXT;
import static raleighnc.events.parser.impl.CssSelectors.DATE_TIME;
import static raleighnc.events.parser.impl.HtmlConstants.DATETIME_ATTR;

import java.time.LocalDate;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts and parses dates from news cards.
 * Handles multiple date formats found in Raleigh NC news pages.
 */
public final class DateExtractor {

    private static final Logger LOG =
        LoggerFactory.getLogger(DateExtractor.class);

    private final DateParser dateParser;

    /**
     * Creates a DateExtractor with default DateParser.
     */
    public DateExtractor() {
        this.dateParser = new DateParser();
    }

    /**
     * Extracts date from text containing "Published" keyword.
     * Handles format: "News Published 1/1/2026 Updated 1/1/2026"
     *
     * @param eventElement the element to search
     * @return the extracted date, or null if not found
     */
    private LocalDate extractFromPublishedText(final Element eventElement) {
        final String text = eventElement.text();
        final int publishedIndex = text.indexOf("Published");
        if (publishedIndex == -1) {
            return null;
        }

        // Extract text after "Published "
        final String afterPublished = text.substring(publishedIndex + "Published".length()).trim();

        // Try to extract date (should be first token before "Updated" or whitespace)
        final String[] tokens = afterPublished.split("\\s+");
        if (tokens.length > 0) {
            return parseDate(tokens[0]);
        }

        return null;
    }

    /**
     * Extracts the publication date from a news card element.
     *
     * @param eventElement the news card element
     * @return the extracted date, or current date if not found
     */
    public LocalDate extractLocalDate(final Element eventElement) {
        // Strategy 1: Look for "Published" text pattern
        final LocalDate publishedDate = extractFromPublishedText(eventElement);
        if (publishedDate != null) {
            return publishedDate;
        }

        // Strategy 2: Look for c-dateline__text
        final Elements dateElems = eventElement.select(DATE_DATELINE_TEXT);
        if (!dateElems.isEmpty()) {
            final String dateText = requireNonNull(dateElems.first()).text().trim();
            final LocalDate parsed = parseDate(dateText);
            if (parsed != null) {
                return parsed;
            }
        }

        // Strategy 3: Look for any time element
        final Elements timeElems = eventElement.select(DATE_TIME);
        for (final Element timeElem : timeElems) {
            final String datetime = timeElem.attr(DATETIME_ATTR);
            if (!datetime.isEmpty()) {
                final LocalDate parsed = parseDate(datetime);
                if (parsed != null) {
                    return parsed;
                }
            }
            final String timeText = timeElem.text().trim();
            final LocalDate parsed = parseDate(timeText);
            if (parsed != null) {
                return parsed;
            }
        }

        final LocalDate now = LocalDate.now();
        LOG.warn("Could not extract date, using default: {}", now);
        return now;
    }

    private LocalDate parseDate(final String dateText) {
        return dateParser.parse(dateText);
    }
}
