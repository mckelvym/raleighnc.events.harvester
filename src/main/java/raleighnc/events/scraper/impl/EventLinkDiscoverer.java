package raleighnc.events.scraper.impl;

import static java.util.Objects.requireNonNull;
import static raleighnc.events.parser.impl.CssSelectors.ARTICLE_TAG;
import static raleighnc.events.parser.impl.CssSelectors.C_TEASER;
import static raleighnc.events.parser.impl.CssSelectors.EVENT_LINK_ARTICLE;
import static raleighnc.events.parser.impl.CssSelectors.EVENT_LINK_TEASER;
import static raleighnc.events.parser.impl.HtmlConstants.ABS_HREF_ATTR;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers news teasers on Raleigh NC news listing pages.
 *
 * <p>Each teaser carries everything the feed needs (title, link, date, image), so items are
 * built from the listing alone. Article pages sit behind a Cloudflare challenge that does not
 * clear from the production server.
 */
public final class EventLinkDiscoverer {

    private static final Logger LOG =
        LoggerFactory.getLogger(EventLinkDiscoverer.class);

    /**
     * Discovers news teasers keyed by their absolute article URL (the item GUID).
     *
     * @param document the listing page
     * @return teaser container elements keyed by article URL, in page order
     */
    public Map<String, Element> discoverTeasers(final Document document) {
        requireNonNull(document, "document must not be null");
        final Map<String, Element> teasers = new LinkedHashMap<>();

        for (final Element link : document.select(EVENT_LINK_TEASER)) {
            final String href = link.attr(ABS_HREF_ATTR);
            if (isValidNewsUrl(href)) {
                teasers.putIfAbsent(href, findTeaserContainer(link));
            }
        }

        // Fallback: any article links, should the teaser markup change
        if (teasers.isEmpty()) {
            for (final Element link : document.select(EVENT_LINK_ARTICLE)) {
                final String href = link.attr(ABS_HREF_ATTR);
                if (isValidNewsUrl(href)) {
                    teasers.putIfAbsent(href, findArticleContainer(link));
                }
            }
        }

        LOG.info("Discovered {} event links", teasers.size());
        return teasers;
    }

    private Element findArticleContainer(final Element link) {
        final Element article = link.closest(ARTICLE_TAG);
        return article != null ? article : requireNonNull(link.parent());
    }

    private Element findTeaserContainer(final Element link) {
        final Element teaser = link.closest("." + C_TEASER);
        return teaser != null ? teaser : requireNonNull(link.parent());
    }

    private boolean isValidNewsUrl(final String url) {
        return url != null
            && url.startsWith("https://raleighnc.gov/")
            && url.contains("/news/")
            && !url.endsWith("/news")
            && !url.contains("?");
    }
}
