package raleighnc.events.parser.impl;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raleighnc.events.config.ScraperConfiguration;
import raleighnc.events.domain.EventItem;
import raleighnc.events.parser.EventParser;

/**
 * Parses and extracts event fields.
 */
public final class EventParserImpl implements EventParser {

    private static final Logger LOG =
        LoggerFactory.getLogger(EventParserImpl.class);

    private final EventCardFinder cardFinder;
    private final ScraperConfiguration config;
    private final DateExtractor dateExtractor;
    private final DescriptionExtractor descriptionExtractor;
    private final ImageExtractor imageExtractor;
    private final TitleExtractor titleExtractor;

    /**
     * Creates a new EventParserImpl with all necessary extractors.
     *
     * @param config the scraper configuration
     */
    public EventParserImpl(final ScraperConfiguration config) {
        this.config = requireNonNull(config);
        this.cardFinder = new EventCardFinder();
        this.titleExtractor = new TitleExtractor();
        this.dateExtractor = new DateExtractor();
        this.imageExtractor = new ImageExtractor();
        this.descriptionExtractor = new DescriptionExtractor();
    }

    @Override
    public Optional<EventItem> parseEvent(final Document doc, final String eventUrl) {
        requireNonNull(doc, "doc must not be null");
        requireNonNull(eventUrl, "eventUrl must not be null");
        final Element card = cardFinder.findEventCard(doc);
        if (card == null) {
            LOG.warn("Failed to find event card for: {}", eventUrl);
            return Optional.empty();
        }

        final String baseTitle = titleExtractor.extractTitle(card);
        if (baseTitle == null) {
            LOG.warn("Failed to extract title from: {}", eventUrl);
            return Optional.empty();
        }

        final LocalDate date = dateExtractor.extractLocalDate(card);
        final String imageUrl = imageExtractor.extractImageUrl(card);
        final String description = descriptionExtractor.extract(
            card, config.useHtmlDescription());

        final EventItem eventItem =
            new EventItem(eventUrl, baseTitle, eventUrl, description, date, null, imageUrl, null);
        return Optional.of(eventItem);
    }
}
