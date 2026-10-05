package raleighnc.events.parser.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized date parsing utility
 */
public final class DateParser {

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US),
        DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US),
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.RFC_1123_DATE_TIME
    };
    private static final Logger LOG =
        LoggerFactory.getLogger(DateParser.class);

    /**
     * Parses a date string using multiple format strategies.
     *
     * @param dateStr the date string to parse
     * @return the parsed LocalDate, or null if parsing fails
     */
    public LocalDate parse(final String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        final String trimmed = dateStr.trim();
        for (final DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (final DateTimeParseException e) {
                // Try next formatter
            }
        }

        LOG.warn("Failed to parse date: {}", trimmed);
        return null;
    }
}
