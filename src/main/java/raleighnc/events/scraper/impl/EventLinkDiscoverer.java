package raleighnc.events.scraper.impl;

import static java.util.Objects.requireNonNull;
import static raleighnc.events.parser.impl.CssSelectors.C_TEASER;
import static raleighnc.events.parser.impl.CssSelectors.DATE_DATELINE_TEXT;
import static raleighnc.events.parser.impl.CssSelectors.DATE_TIME;
import static raleighnc.events.parser.impl.CssSelectors.EVENT_LINK_ARTICLE;
import static raleighnc.events.parser.impl.CssSelectors.EVENT_LINK_TEASER;
import static raleighnc.events.parser.impl.HtmlConstants.ABS_HREF_ATTR;
import static raleighnc.events.parser.impl.HtmlConstants.DATETIME_ATTR;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raleighnc.events.parser.impl.DateParser;

/**
 * Discovers event article links from Raleigh NC listing pages.
 * Extracts URLs and publication dates from event teasers.
 */
public final class EventLinkDiscoverer {

    private static final Logger LOG =
        LoggerFactory.getLogger(EventLinkDiscoverer.class);
    private final DateParser dateParser;

    public EventLinkDiscoverer() {
        dateParser = new DateParser();
    }

    private boolean containsUrl(final List<EventLink> links,
                                final String url) {
        for (final EventLink link : links) {
            if (link.getUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Discovers all news links from a listing page.
     *
     * @param document the JSoup document of the listing page
     * @return list of news links with URLs and dates
     */
    public List<EventLink> discoverLinks(final Document document) {
        final List<EventLink> links = new ArrayList<>();

        // Find all news teaser links
        final Elements teaserLinks =
            document.select(EVENT_LINK_TEASER);

        for (final Element link : teaserLinks) {
            final String href = link.attr(ABS_HREF_ATTR);
            if (isValidNewsUrl(href)) {
                final Element teaser = findTeaserContainer(link);
                final LocalDate date = extractDateFromTeaser(teaser);
                links.add(new EventLink(href, date));
            }
        }

        // Fallback: find any article links
        if (links.isEmpty()) {
            final Elements articleLinks =
                document.select(EVENT_LINK_ARTICLE);
            for (final Element link : articleLinks) {
                final String href = link.attr(ABS_HREF_ATTR);
                if (isValidNewsUrl(href) && !containsUrl(links, href)) {
                    final Element article = findArticleContainer(link);
                    final LocalDate date = extractDateFromTeaser(article);
                    links.add(new EventLink(href, date));
                }
            }
        }

        LOG.info("Discovered {} event links", links.size());
        return links;
    }

    private LocalDate extractDateFromTeaser(final Element container) {
        if (container == null) {
            return null;
        }

        // Strategy 1: Look for c-dateline__text
        final Elements dateElems = container.select(DATE_DATELINE_TEXT);
        if (!dateElems.isEmpty()) {
            final String dateText = requireNonNull(dateElems.first()).text().trim();
            final LocalDate parsed = dateParser.parse(dateText);
            if (parsed != null) {
                return parsed;
            }
        }

        // Strategy 2: Look for any time element
        final Elements timeElems = container.select(DATE_TIME);
        for (final Element timeElem : timeElems) {
            final String datetime = timeElem.attr(DATETIME_ATTR);
            if (!datetime.isEmpty()) {
                final LocalDate parsed = dateParser.parse(datetime);
                if (parsed != null) {
                    return parsed;
                }
            }
            final String timeText = timeElem.text().trim();
            final LocalDate parsed = dateParser.parse(timeText);
            if (parsed != null) {
                return parsed;
            }
        }

        return null;
    }

    private Element findArticleContainer(final Element link) {
        Element current = link;
        while (current != null) {
            if (EVENT_LINK_ARTICLE.equals(current.tagName())) {
                return current;
            }
            current = current.parent();
        }
        return requireNonNull(link).parent();
    }

    private Element findTeaserContainer(final Element link) {
        Element current = link;
        while (current != null) {
            if (current.hasClass(C_TEASER)) {
                return current;
            }
            current = current.parent();
        }
        return requireNonNull(link).parent();
    }

    private boolean isValidNewsUrl(final String url) {
        return url != null
            && url.startsWith("https://raleighnc.gov/")
            && url.contains("/news/")
            && !url.endsWith("/news")
            && !url.contains("?");
    }
}
