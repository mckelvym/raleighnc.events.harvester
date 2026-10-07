package raleighnc.events.parser.impl;

/**
 * Constants for CSS selectors used in HTML parsing.
 *
 * <p>This class centralizes all CSS selector strings used throughout the
 * parser implementation to avoid magic strings and improve maintainability.
 */
public final class CssSelectors {

    // Title selectors
    public static final String TITLE_H1 = "h1";
    public static final String TITLE_TEASER_LINK = "a.c-teaser__title-link";

    // Date selectors
    public static final String DATE_DATELINE_TEXT = ".c-dateline__text";
    public static final String DATE_TIME = "time";

    // Image selectors
    public static final String IMAGE_TEASER = ".c-teaser__image img";
    public static final String IMAGE_ANY = "img";

    // Event link discovery selectors (scraper package)
    public static final String EVENT_LINK_TEASER = "a.c-teaser__title-link";
    public static final String EVENT_LINK_ARTICLE = "article a[href*='/news/']";
    public static final String C_TEASER = "c-teaser";
    public static final String ARTICLE_TAG = "article";

    private CssSelectors() {
        // Utility class - prevent instantiation
    }
}
