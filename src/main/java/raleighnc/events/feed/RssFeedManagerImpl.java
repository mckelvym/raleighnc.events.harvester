package raleighnc.events.feed;

import static java.util.Objects.requireNonNull;
import static raleighnc.events.feed.RssElementNames.CHANNEL;
import static raleighnc.events.feed.RssElementNames.DESCRIPTION;
import static raleighnc.events.feed.RssElementNames.ENCLOSURE;
import static raleighnc.events.feed.RssElementNames.ENCODING_UTF8;
import static raleighnc.events.feed.RssElementNames.EVENT_NAMESPACE_URI;
import static raleighnc.events.feed.RssElementNames.EV_ENDDATE;
import static raleighnc.events.feed.RssElementNames.EV_STARTDATE;
import static raleighnc.events.feed.RssElementNames.GUID;
import static raleighnc.events.feed.RssElementNames.IMAGE_JPEG_TYPE;
import static raleighnc.events.feed.RssElementNames.INDENT_AMOUNT;
import static raleighnc.events.feed.RssElementNames.IS_PERMALINK_ATTR;
import static raleighnc.events.feed.RssElementNames.ITEM;
import static raleighnc.events.feed.RssElementNames.LANGUAGE;
import static raleighnc.events.feed.RssElementNames.LANGUAGE_VALUE;
import static raleighnc.events.feed.RssElementNames.LAST_BUILD_DATE;
import static raleighnc.events.feed.RssElementNames.LINK;
import static raleighnc.events.feed.RssElementNames.PUB_DATE;
import static raleighnc.events.feed.RssElementNames.RSS;
import static raleighnc.events.feed.RssElementNames.RSS_VERSION;
import static raleighnc.events.feed.RssElementNames.TITLE;
import static raleighnc.events.feed.RssElementNames.TRUE_VALUE;
import static raleighnc.events.feed.RssElementNames.TYPE_ATTR;
import static raleighnc.events.feed.RssElementNames.URL_ATTR;
import static raleighnc.events.feed.RssElementNames.VERSION_ATTR;
import static raleighnc.events.feed.RssElementNames.XMLNS_EV_ATTR;
import static raleighnc.events.feed.RssElementNames.XSLT_INDENT_PROPERTY;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import raleighnc.events.config.ScraperConfiguration;
import raleighnc.events.domain.EventItem;

/**
 * RSS feed manager implementation.
 * Handles loading, merging, filtering, and writing RSS 2.0 feeds.
 */
public final class RssFeedManagerImpl implements RssFeedManager {

    // Anything outside XML 1.0's Char production: #x9 | #xA | #xD | [#x20-#xD7FF] |
    // [#xE000-#xFFFD] | [#x10000-#x10FFFF]
    private static final Pattern INVALID_XML_CHARACTERS = Pattern.compile(
        "[^\\x09\\x0A\\x0D\\x{20}-\\x{D7FF}\\x{E000}-\\x{FFFD}\\x{10000}-\\x{10FFFF}]");
    private static final Logger LOG =
        LoggerFactory.getLogger(RssFeedManagerImpl.class);
    private final ScraperConfiguration config;
    private final EventFilter eventFilter;
    private final XmlSecurityConfigurer securityConfigurer;

    /**
     * Creates a new RssFeedManagerImpl.
     *
     * @param config the scraper configuration
     */
    public RssFeedManagerImpl(final ScraperConfiguration config) {
        this.config = requireNonNull(config, "config must not be null");
        this.securityConfigurer = new XmlSecurityConfigurer();
        this.eventFilter = new EventFilter(config);
    }

    private void addChannelMetadata(final Document doc,
                                    final Element channel) {
        addElement(doc, channel, TITLE, config.getFeedTitle());
        addElement(doc, channel, LINK, config.getFeedLink());
        addElement(doc, channel, DESCRIPTION,
            config.getFeedDescription());
        addElement(doc, channel, LANGUAGE, LANGUAGE_VALUE);
        addElement(doc, channel, LAST_BUILD_DATE,
            ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    /**
     * Adds a description element wrapped in CDATA, omitting it when empty.
     *
     * @param doc         the XML document
     * @param item        the item element to add to
     * @param description the description HTML or text
     */
    private void addDescriptionElement(final Document doc, final Element item,
                                       final String description) {
        if (description.isEmpty()) {
            return;
        }
        final Element element = doc.createElement(DESCRIPTION);
        element.appendChild(doc.createCDATASection(description));
        item.appendChild(element);
    }

    private void addElement(final Document doc, final Element parent,
                            final String tagName, final String textContent) {
        final Element element = doc.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }

    /**
     * Adds the machine-readable event dates (RSS Event module) used for retention.
     *
     * @param doc   the XML document
     * @param item  the item element to add to
     * @param event the event whose dates to add
     */
    private void addEventDateElements(final Document doc, final Element item,
                                      final EventItem event) {
        final Element startDate = doc.createElement(EV_STARTDATE);
        startDate.setTextContent(event.eventDateStart().toString());
        item.appendChild(startDate);
        if (event.eventDateEnd() != null) {
            final Element endDate = doc.createElement(EV_ENDDATE);
            endDate.setTextContent(event.eventDateEnd().toString());
            item.appendChild(endDate);
        }
    }

    /**
     * Adds the item GUID, which is always the event URL and therefore a permalink.
     *
     * @param doc   the XML document
     * @param item  the item element to add to
     * @param event the event whose GUID to add
     */
    private void addGuidElement(final Document doc, final Element item, final EventItem event) {
        final Element guid = doc.createElement(GUID);
        guid.setAttribute(IS_PERMALINK_ATTR, TRUE_VALUE);
        guid.setTextContent(event.guid());
        item.appendChild(guid);
    }

    private void addNewsItem(final Document doc, final Element channel,
                             final EventItem eventItem) {
        final Element item = doc.createElement(ITEM);

        addElement(doc, item, TITLE, "%s (%s)".formatted(eventItem.title(),
            eventItem.eventDateStart()));
        addElement(doc, item, LINK, eventItem.link());
        addGuidElement(doc, item, eventItem);
        addEventDateElements(doc, item, eventItem);

        // Use current time as pubDate (harvesting time)
        final String pubDate = ZonedDateTime.now()
            .format(DateTimeFormatter.RFC_1123_DATE_TIME);
        addElement(doc, item, PUB_DATE, pubDate);

        addDescriptionElement(doc, item, eventItem.sanitizedDescription());

        if (eventItem.imageUrl() != null) {
            final Element enclosure = doc.createElement(ENCLOSURE);
            enclosure.setAttribute(URL_ATTR, eventItem.imageUrl());
            enclosure.setAttribute(TYPE_ATTR, IMAGE_JPEG_TYPE);
            item.appendChild(enclosure);
        }

        channel.appendChild(item);
    }

    @Override
    public void generateFeed(final String filePath,
                             final List<EventItem> newEvents,
                             final String existingFilePath)
        throws Exception {
        requireNonNull(filePath, "filePath must not be null");
        requireNonNull(newEvents, "newNewsItems must not be null");
        requireNonNull(existingFilePath, "existingFilePath must not be null");

        final Path feedPath = Path.of(filePath);
        final Path existingFeedPath = Path.of(existingFilePath);

        LOG.info("Generating RSS feed with {} new items",
            newEvents.size());

        final DocumentBuilderFactory factory =
            securityConfigurer.createSecureDocumentBuilderFactory();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.newDocument();

        final Element rss = doc.createElement(RSS);
        rss.setAttribute(VERSION_ATTR, RSS_VERSION);
        rss.setAttribute(XMLNS_EV_ATTR, EVENT_NAMESPACE_URI);
        doc.appendChild(rss);

        final Element channel = doc.createElement(CHANNEL);
        rss.appendChild(channel);

        addChannelMetadata(doc, channel);

        // Add new events (sorted by eventDateStart descending), skipping any past retention
        final List<EventItem> sortedEvents = new ArrayList<>(newEvents);
        sortedEvents.sort(Comparator.comparing(EventItem::eventDateStart).reversed());

        // Add new news items
        for (final EventItem event : sortedEvents) {
            if (eventFilter.shouldKeep(event)) {
                addNewsItem(doc, channel, event);
            }
        }

        // Merge existing items
        importExistingEvents(doc, channel, existingFeedPath.toFile());

        // Write the feed
        writeFeed(doc, feedPath);

        LOG.info("RSS feed generated successfully at: {}", filePath);
    }

    /**
     * Imports items from the existing feed, dropping those past the retention period.
     *
     * <p>Errors are logged rather than thrown so a scheduled run still publishes new events.
     *
     * @param doc              the new feed document
     * @param channel          the channel to append items to
     * @param existingFeedFile the existing feed file (may not exist)
     */
    private void importExistingEvents(final Document doc, final Element channel,
                                      final File existingFeedFile) {
        if (!existingFeedFile.exists()) {
            return;
        }
        try {
            final DocumentBuilder builder =
                securityConfigurer.createSecureDocumentBuilderFactory().newDocumentBuilder();
            final NodeList items = builder.parse(existingFeedFile).getElementsByTagName(ITEM);
            int imported = 0;
            for (int i = 0; i < items.getLength(); i++) {
                final Element item = (Element) items.item(i);
                if (eventFilter.shouldKeep(item)) {
                    final Node importedNode = doc.importNode(item, true);
                    removeWhitespaceNodes(importedNode);
                    channel.appendChild(importedNode);
                    imported++;
                }
            }
            LOG.info("Imported {} existing events, dropped {} past retention",
                imported, items.getLength() - imported);
        } catch (final Exception e) {
            LOG.error("Failed to import existing events from {}: {}",
                existingFeedFile, e.getMessage(), e);
        }
    }

    @Override
    public Set<String> loadExistingGuids(final String filePath)
        throws Exception {
        requireNonNull(filePath, "filePath must not be null");
        final Set<String> guids = new HashSet<>();
        final Path feedPath = Path.of(filePath);

        if (!Files.exists(feedPath)) {
            LOG.info("No existing feed found at: {}", filePath);
            return guids;
        }

        final DocumentBuilderFactory factory =
            securityConfigurer.createSecureDocumentBuilderFactory();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(feedPath.toFile());

        final NodeList guidNodes = doc.getElementsByTagName(GUID);
        for (int i = 0; i < guidNodes.getLength(); i++) {
            final String guid = guidNodes.item(i).getTextContent().trim();
            guids.add(guid);
        }

        LOG.info("Loaded {} existing GUIDs from feed", guids.size());
        return guids;
    }

    /**
     * Removes characters that are not allowed in XML 1.0 from all text, CDATA and attributes.
     *
     * <p>Scraped text can contain control characters (e.g. U+0002). The serializer writes them
     * as character references such as {@code &#2;}, which no XML parser accepts, so the next
     * run could not read the feed.
     *
     * @param root the root node to clean
     */
    private void removeInvalidXmlCharacters(final Node root) {
        final Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            final Node current = stack.pop();
            final short type = current.getNodeType();
            if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
                stripInvalidXmlCharacters(current);
            }
            final NamedNodeMap attributes = current.getAttributes();
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    stripInvalidXmlCharacters(attributes.item(i));
                }
            }
            final NodeList children = current.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                stack.push(children.item(i));
            }
        }
    }

    /**
     * Removes whitespace-only text nodes from a DOM tree.
     *
     * <p>This is necessary to ensure proper indentation when writing XML.
     *
     * @param node The root node to clean
     */
    private void removeWhitespaceNodes(final Node node) {
        final Deque<Node> stack = new ArrayDeque<>();
        stack.push(node);

        while (!stack.isEmpty()) {
            final Node current = stack.pop();
            final NodeList children = current.getChildNodes();

            for (int i = children.getLength() - 1; i >= 0; i--) {
                final Node child = children.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    if (child.getTextContent().trim().isEmpty()) {
                        current.removeChild(child);
                    }
                } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                    stack.push(child);
                }
            }
        }
    }

    private void stripInvalidXmlCharacters(final Node node) {
        final String value = node.getNodeValue();
        final String cleaned = INVALID_XML_CHARACTERS.matcher(value).replaceAll("");
        if (!cleaned.equals(value)) {
            node.setNodeValue(cleaned);
        }
    }

    private void writeFeed(final Document doc, final Path feedPath)
        throws TransformerException, IOException {
        removeInvalidXmlCharacters(doc);
        final TransformerFactory transformerFactory =
            securityConfigurer.createSecureTransformerFactory();
        final Transformer transformer =
            transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING_UTF8);
        transformer.setOutputProperty(XSLT_INDENT_PROPERTY, INDENT_AMOUNT);

        final DOMSource source = new DOMSource(doc);
        final File outputFile = feedPath.toFile();
        final File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + parentDir);
        }


        try (FileWriter writer = new FileWriter(outputFile,
            java.nio.charset.StandardCharsets.UTF_8)) {
            final StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
        }

    }
}
