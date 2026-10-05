package raleighnc.events.parser.impl;

import static raleighnc.events.parser.impl.CssSelectors.TITLE_H1;
import static raleighnc.events.parser.impl.CssSelectors.TITLE_TEASER_LINK;
import static raleighnc.events.parser.impl.HtmlConstants.TITLE_ATTR;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts title from news card using multiple fallback strategies.
 */
public final class TitleExtractor {

    private static final Logger LOG =
        LoggerFactory.getLogger(TitleExtractor.class);

    /**
     * Extracts the title from a news card element.
     *
     * @param card the news card element
     * @return the extracted title, or null if not found
     */
    public String extractTitle(final Element card) {
        // Strategy 1: Look for page title (h1) - for individual article pages
        final Elements h1 = card.select(TITLE_H1);
        if (!h1.isEmpty()) {
            final String title = h1.first().text().trim();
            if (!title.isEmpty()) {
                return title;
            }
        }

        // Strategy 2: Look for c-teaser__title-link (for listing pages)
        final Elements titleLinks = card.select(TITLE_TEASER_LINK);
        if (!titleLinks.isEmpty()) {
            final String title = titleLinks.first().text().trim();
            if (!title.isEmpty()) {
                return title;
            }
        }

        // Strategy 3: Look for other headings (h2-h6)
        for (int level = 2; level <= 6; level++) {
            final Elements headings = card.select("h" + level);
            if (!headings.isEmpty()) {
                final String title = headings.first().text().trim();
                if (!title.isEmpty()) {
                    return title;
                }
            }
        }

        // Strategy 4: Look for title attribute
        final String titleAttr = card.attr(TITLE_ATTR);
        if (!titleAttr.isEmpty()) {
            return titleAttr;
        }

        LOG.warn("Could not extract title from news card");
        return null;
    }
}
