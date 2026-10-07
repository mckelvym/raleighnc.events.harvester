package raleighnc.events.parser.impl;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Optional;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raleighnc.events.domain.EventItem;
import raleighnc.events.parser.EventParser;

/**
 * Parses news items from listing-page teasers.
 *
 * <p>Teasers carry a title, date and image but no summary text, so items have no description.
 */
public final class EventParserImpl implements EventParser {

    private static final Logger LOG =
        LoggerFactory.getLogger(EventParserImpl.class);

    private final DateExtractor dateExtractor;
    private final ImageExtractor imageExtractor;
    private final TitleExtractor titleExtractor;

    /**
     * Creates a new EventParserImpl.
     */
    public EventParserImpl() {
        this.titleExtractor = new TitleExtractor();
        this.dateExtractor = new DateExtractor();
        this.imageExtractor = new ImageExtractor();
    }

    @Override
    public Optional<EventItem> parseEvent(final Element teaser, final String eventUrl) {
        requireNonNull(teaser, "teaser must not be null");
        requireNonNull(eventUrl, "eventUrl must not be null");

        final String title = titleExtractor.extractTitle(teaser);
        if (title == null) {
            LOG.warn("Failed to extract title from teaser for: {}", eventUrl);
            return Optional.empty();
        }

        final LocalDate date = dateExtractor.extractLocalDate(teaser);
        final String imageUrl = imageExtractor.extractImageUrl(teaser);

        return Optional.of(
            new EventItem(eventUrl, title, eventUrl, null, date, null, imageUrl, null));
    }
}
