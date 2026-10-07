package raleighnc.events.parser;

import java.util.Optional;
import org.jsoup.nodes.Element;
import raleighnc.events.domain.EventItem;

/**
 * Interface for parsing event information from HTML elements.
 */
public interface EventParser {

    /**
     * Parses a news item from its teaser on the news listing page.
     *
     * @param teaser   the teaser element (title link, dateline and image)
     * @param eventUrl the URL of the news article (used as link and GUID)
     * @return an Optional containing the parsed EventItem, or empty if parsing failed
     */
    Optional<EventItem> parseEvent(Element teaser, String eventUrl);
}
