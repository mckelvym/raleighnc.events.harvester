package raleighnc.events.parser.impl;

import static java.util.Objects.requireNonNull;
import static raleighnc.events.parser.impl.CssSelectors.IMAGE_ANY;
import static raleighnc.events.parser.impl.CssSelectors.IMAGE_TEASER;
import static raleighnc.events.parser.impl.HtmlConstants.SRC_ATTR;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Extracts image URLs from news cards.
 */
public final class ImageExtractor {

    /**
     * Extracts the image URL from a news card element.
     *
     * @param card the news card element
     * @return the image URL, or null if not found
     */
    public String extractImageUrl(final Element card) {
        // Strategy 1: Look for img within c-teaser__image
        final Elements imageContainers = card.select(IMAGE_TEASER);
        if (!imageContainers.isEmpty()) {
            final String src = extractSrcFromImg(requireNonNull(imageContainers.first()));
            if (src != null) {
                return src;
            }
        }

        // Strategy 2: Look for any img element
        final Elements anyImages = card.select(IMAGE_ANY);
        if (!anyImages.isEmpty()) {
            return extractSrcFromImg(requireNonNull(anyImages.first()));
        }

        return null;
    }

    private String extractSrcFromImg(final Element img) {
        // Check srcset first (for responsive images)
        final String srcset = img.attr("srcset");
        if (!srcset.isEmpty()) {
            // Extract the first URL from srcset
            final String[] parts = srcset.split(",");
            if (parts.length > 0) {
                final String firstEntry = parts[0].trim();
                final String url = firstEntry.split("\\s+")[0];
                return makeAbsoluteUrl(img, url);
            }
        }

        // Check src attribute
        final String src = img.attr(SRC_ATTR);
        if (!src.isEmpty() && !src.contains("placeholder")
            && !src.startsWith("data:")) {
            return makeAbsoluteUrl(img, src);
        }

        return null;
    }

    private String makeAbsoluteUrl(final Element img, final String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return img.absUrl(SRC_ATTR);
    }
}
