package raleighnc.events.scraper.impl;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raleighnc.events.config.ScraperConfiguration;
import raleighnc.events.domain.EventItem;
import raleighnc.events.parser.EventParser;
import raleighnc.events.scraper.EventScraper;
import raleighnc.events.webdriver.PageLoader;

/**
 * Scrapes Raleigh NC news from the teasers on the news listing page.
 * Implements the unified 3-phase flow: discover, filter, parse.
 *
 * <p>Items are built from listing teasers only; article pages are never loaded because they
 * sit behind a Cloudflare challenge that does not clear from the production server.
 */
public record EventScraperImpl(ScraperConfiguration config,
                               PageLoader pageLoader,
                               EventParser eventParser,
                               EventLinkDiscoverer linkDiscoverer) implements EventScraper {

    private static final Logger LOG =
        LoggerFactory.getLogger(EventScraperImpl.class);

    public EventScraperImpl {
        requireNonNull(config, "config must not be null");
        requireNonNull(pageLoader, "pageLoader must not be null");
        requireNonNull(eventParser, "eventParser must not be null");
        requireNonNull(linkDiscoverer, "linkDiscoverer must not be null");
    }

    /**
     * Builds the URL for a specific page number.
     *
     * @param page Zero-based page number
     * @return Full page URL
     */
    private String buildPageUrl(final int page) {
        final String baseUrl = config.getBaseUrl();
        return page == 0 ? baseUrl : "%s&page=%d".formatted(baseUrl, page);
    }

    /**
     * Phase 1: Discovers news teasers on the listing page(s).
     *
     * @param teaserMap Map to store URL to teaser Element mappings for later parsing
     * @return List of discovered event URLs
     */
    private List<String> discoverEventUrls(final Map<String, Element> teaserMap) {
        final int pagesToFetch = config.getPagesToFetch();

        for (int page = 0; page < pagesToFetch; page++) {
            final String pageUrl = buildPageUrl(page);
            LOG.info("Discovering links on page {}/{}", page + 1, pagesToFetch);

            // A failed listing page (e.g. an uncleared bot challenge) must not discard
            // the links already discovered on earlier pages
            try {
                final Document listingPage = pageLoader.loadPage(pageUrl);
                linkDiscoverer.discoverTeasers(listingPage).forEach(teaserMap::putIfAbsent);
            } catch (final Exception e) {
                LOG.warn("Skipping listing page {}/{} ({}): {}", page + 1, pagesToFetch, pageUrl,
                    e.getMessage());
            }
        }

        return new ArrayList<>(teaserMap.keySet());
    }

    /**
     * Phase 2: Filters URLs to only those not in existingGuids.
     *
     * @param urls          All discovered URLs
     * @param existingGuids Set of existing event GUIDs
     * @return Filtered list of new URLs
     */
    private List<String> filterNewUrls(final List<String> urls,
                                       final Set<String> existingGuids) {
        return urls.stream()
            .filter(url -> !existingGuids.contains(url))
            .toList();
    }

    /**
     * Parses a single event from its teaser and adds it to results.
     *
     * @param url     Event URL
     * @param teaser  Teaser element from the listing page
     * @param current Current event number (1-based)
     * @param total   Total number of events
     * @param results List to add parsed event to
     */
    private void parseAndAddEvent(final String url, final Element teaser, final int current,
                                  final int total, final List<EventItem> results) {
        final Optional<EventItem> eventOpt = eventParser.parseEvent(teaser, url);

        if (eventOpt.isPresent()) {
            final EventItem event = eventOpt.get();
            results.add(event);
            LOG.info("Event {}/{}: {} ({})", current, total,
                event.title(), event.eventDateStart());
        } else {
            LOG.warn("No event returned for: {}", url);
        }
    }

    /**
     * Phase 3: Parses events from the filtered URLs.
     *
     * @param urls      URLs to parse
     * @param teaserMap Map of URL to teaser Element
     * @return List of successfully parsed EventItem objects
     */
    private List<EventItem> parseEvents(final List<String> urls,
                                        final Map<String, Element> teaserMap) {
        final List<EventItem> events = new ArrayList<>();
        int current = 0;
        for (final String url : urls) {
            current++;
            try {
                parseAndAddEvent(url, teaserMap.get(url), current, urls.size(), events);
            } catch (final Exception e) {
                LOG.error("Failed to parse event from {}: {}", url, e.getMessage(), e);
            }
        }
        return events;
    }

    @Override
    public List<EventItem> scrapeEvents(final Set<String> existingGuids) {
        requireNonNull(existingGuids, "existingGuids must not be null");

        LOG.info("Starting event scraping from: {}", config.getBaseUrl());

        try {
            // PHASE 1: Discover event URLs
            final Map<String, Element> teaserMap = new LinkedHashMap<>();
            final List<String> allEventLinks = discoverEventUrls(teaserMap);
            LOG.info("Phase 1 complete: Discovered {} event links", allEventLinks.size());

            // PHASE 2: Filter to new URLs only
            final List<String> newEventLinks = filterNewUrls(allEventLinks, existingGuids);
            LOG.info("Phase 2 complete: {} new events after filtering", newEventLinks.size());

            // PHASE 3: Parse each event
            final List<EventItem> events = parseEvents(newEventLinks, teaserMap);
            LOG.info("Phase 3 complete: Parsed {} events", events.size());

            return events;
        } catch (final Exception e) {
            LOG.error("Unable to parse events", e);
            return List.of();
        }
    }
}
