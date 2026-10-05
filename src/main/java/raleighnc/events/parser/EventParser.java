package raleighnc.events.parser;

import java.util.Optional;
import org.jsoup.nodes.Document;
import raleighnc.events.domain.EventItem;

/**
 * Interface for parsing event information from HTML elements.
 */
public interface EventParser {

    /**
     * Parses a news item from a JSoup document with a known publication date.
     *
     * @param document the JSoup document to parse
     * @param eventUrl the URL of the event page
     * @return an Optional containing the parsed EventItem, or empty if parsing failed
     */
    Optional<EventItem> parseEvent(Document document, String eventUrl);
}
