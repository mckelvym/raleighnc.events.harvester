package raleighnc.events.parser.impl;

import static raleighnc.events.parser.impl.CssSelectors.EVENT_CARD_ARTICLE;
import static raleighnc.events.parser.impl.CssSelectors.EVENT_CARD_MAIN;
import static raleighnc.events.parser.impl.CssSelectors.EVENT_CARD_REGION_CONTENT;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finds news card containers in Raleigh NC news pages.
 * Uses CSS selectors to locate the main content container.
 */
public final class EventCardFinder {

    private static final Logger LOG = LoggerFactory.getLogger(EventCardFinder.class);

    /**
     * Finds the news card container in a document.
     *
     * @param document the document to search
     * @return the container element, or null if not found
     */
    public Element findEventCard(final Document document) {
        // For individual article pages, look for the main content region
        final Elements mainContent = document.select(EVENT_CARD_MAIN);
        if (!mainContent.isEmpty()) {
            return mainContent.first();
        }

        // Try to find content region
        final Elements contentRegion = document.select(EVENT_CARD_REGION_CONTENT);
        if (!contentRegion.isEmpty()) {
            return contentRegion.first();
        }

        // Fallback: try to find any article element
        final Elements anyArticles = document.select(EVENT_CARD_ARTICLE);
        if (!anyArticles.isEmpty()) {
            return anyArticles.first();
        }

        LOG.warn("Could not find news card container");
        return document.body();
    }
}
