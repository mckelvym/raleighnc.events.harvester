package raleighnc.events.parser.impl;

import static java.util.Objects.requireNonNull;
import static raleighnc.events.parser.impl.CssSelectors.DESC_PARAGRAPH;
import static raleighnc.events.parser.impl.CssSelectors.DESC_STORY_PARAGRAPH;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts description/content from news articles.
 * Looks for paragraph elements containing the article text.
 */
public final class DescriptionExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(DescriptionExtractor.class);
    private static final int MIN_FALLBACK_LENGTH = 20;

    private void appendParagraphContent(StringBuilder description, Element paragraph,
                                        boolean useHtml, boolean useLengthFilter) {
        final String content = useHtml ? paragraph.html().trim() : paragraph.text().trim();

        if (content.isEmpty()) {
            return;
        }
        if (useLengthFilter && content.length() <= MIN_FALLBACK_LENGTH) {
            return;
        }

        if (!description.isEmpty()) {
            description.append(useHtml ? "<br/><br/>" : "\n\n");
        }

        if (useHtml) {
            description.append("<p>").append(content).append("</p>");
        } else {
            description.append(content);
        }
    }

    /**
     * Extracts the description from a news card element.
     *
     * @param element the news card element
     * @param useHtml whether to preserve HTML tags (links, formatting)
     * @return the extracted description, or null if not found
     */
    public String extract(final Element element, final boolean useHtml) {
        requireNonNull(element, "element must not be null");
        // Look for story text paragraphs first
        String result = extractFromParagraphs(element.select(DESC_STORY_PARAGRAPH), useHtml, false);
        if (result != null) {
            return result;
        }

        // Fallback: look for any paragraph elements
        result = extractFromParagraphs(element.select(DESC_PARAGRAPH), useHtml, true);
        if (result != null) {
            return result;
        }

        LOG.warn("Could not extract description from news card");
        return null;
    }

    private String extractFromParagraphs(Elements paragraphs, boolean useHtml,
                                         boolean useLengthFilter) {
        if (paragraphs.isEmpty()) {
            return null;
        }

        final StringBuilder description = new StringBuilder();
        for (final Element paragraph : paragraphs) {
            appendParagraphContent(description, paragraph, useHtml, useLengthFilter);
        }

        return !description.isEmpty() ? description.toString() : null;
    }
}
