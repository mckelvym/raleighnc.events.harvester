package raleighnc.events.feed;

import java.util.List;
import java.util.Set;
import raleighnc.events.domain.EventItem;

/**
 * Interface for managing RSS feed operations.
 */
public interface RssFeedManager {

    /**
     * Generates and writes an RSS feed with new and existing events.
     *
     * @param filePath         the path to write the feed
     * @param newsItems        the news items to include
     * @param existingFilePath the path to the existing feed (for merging)
     * @throws Exception if generation fails
     */
    void generateFeed(String filePath, List<EventItem> newsItems,
                      String existingFilePath)
        throws Exception;

    /**
     * Loads existing event GUIDs from an RSS feed file.
     *
     * @param filePath the path to the existing feed file
     * @return set of GUIDs already in the feed
     * @throws Exception if loading fails
     */
    Set<String> loadExistingGuids(String filePath)
        throws Exception;
}
