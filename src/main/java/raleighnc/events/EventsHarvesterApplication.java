package raleighnc.events;

import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import raleighnc.events.config.ScraperConfiguration;
import raleighnc.events.config.impl.ScraperConfigurationImpl;
import raleighnc.events.domain.EventItem;
import raleighnc.events.feed.RssFeedManager;
import raleighnc.events.feed.RssFeedManagerImpl;
import raleighnc.events.parser.EventParser;
import raleighnc.events.parser.impl.EventParserImpl;
import raleighnc.events.scraper.EventScraper;
import raleighnc.events.scraper.impl.EventLinkDiscoverer;
import raleighnc.events.scraper.impl.EventScraperImpl;
import raleighnc.events.webdriver.ChromeDriverManager;
import raleighnc.events.webdriver.PageLoader;
import raleighnc.events.webdriver.WebDriverManager;

/**
 * Main application for scraping and generating RSS feed.
 */
public final class EventsHarvesterApplication {

    private static final String DEFAULT_OUTPUT_FILE = "events.xml";
    private static final Logger LOG =
        LoggerFactory.getLogger(EventsHarvesterApplication.class);

    private EventsHarvesterApplication() {
        // utility
    }

    private static void configureLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    /**
     * Main entry point for the application.
     *
     * @param args command line arguments (optional output file path)
     */
    public static void main(final String[] args) {
        configureLogging();

        final String outputFile = args.length > 0
            ? args[0] : DEFAULT_OUTPUT_FILE;

        LOG.info("Starting Raleigh NC News Harvester");
        LOG.info("Output file: {}", outputFile);

        try {
            new EventsHarvesterApplication().run(outputFile);
            LOG.info("Harvesting completed successfully");
        } catch (final Exception e) {
            LOG.error("Application failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private void run(final String feedPath)
        throws Exception {
        final ScraperConfiguration config =
            new ScraperConfigurationImpl();
        final RssFeedManager feedManager = new RssFeedManagerImpl(config);

        LOG.info("Loading existing feed");
        final Set<String> existingGuids = feedManager.loadExistingGuids(
            feedPath);
        LOG.info("Found {} existing events", existingGuids.size());

        try (WebDriverManager driverManager = new ChromeDriverManager(config)) {
            final EventParser eventParser = new EventParserImpl();
            final PageLoader pageLoader = new PageLoader(
                driverManager.getDriver(), config.getPageLoadTimeout(),
                config.getChallengeTimeout(), config.getRequestDelay());
            final EventScraper scraper = new EventScraperImpl(
                config, pageLoader, eventParser, new EventLinkDiscoverer());

            LOG.info("Starting news scraping");
            final List<EventItem> newNewsItems =
                scraper.scrapeEvents(existingGuids);
            LOG.info("Found {} new news items", newNewsItems.size());

            LOG.info("Generating RSS feed");
            feedManager.generateFeed(feedPath, newNewsItems, feedPath);
        }
    }
}
