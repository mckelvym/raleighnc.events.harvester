package raleighnc.events.webdriver;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads web pages using Selenium and parses them with JSoup.
 * Handles page load timeouts and waits for content to be ready, including
 * waiting out the Cloudflare bot challenge interstitial. Requests are paced
 * with a randomized delay to look less like automated traffic.
 *
 * @param driver           the WebDriver to use
 * @param timeout          how long to wait for a page to finish loading
 * @param challengeTimeout how long to wait for a bot challenge to clear
 * @param requestDelay     base delay before each request; the actual pause is
 *                         randomized between this value and three times it
 */
public record PageLoader(WebDriver driver, Duration timeout, Duration challengeTimeout,
                         Duration requestDelay) {

    /**
     * Title prefix of the Cloudflare bot challenge interstitial page.
     */
    private static final String CHALLENGE_TITLE_PREFIX = "Just a moment";
    /**
     * Expected value when document is fully loaded.
     */
    private static final String DOCUMENT_READY_STATE_COMPLETE = "complete";
    /**
     * JavaScript to check document ready state.
     */
    private static final String DOCUMENT_READY_STATE_SCRIPT =
        "return document.readyState";
    private static final Logger LOG =
        LoggerFactory.getLogger(PageLoader.class);

    /**
     * Creates a new PageLoader.
     *
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if requestDelay is negative
     */
    public PageLoader {
        requireNonNull(driver, "driver must not be null");
        requireNonNull(timeout, "timeout must not be null");
        requireNonNull(challengeTimeout, "challengeTimeout must not be null");
        requireNonNull(requestDelay, "requestDelay must not be null");
        if (requestDelay.isNegative()) {
            throw new IllegalArgumentException("requestDelay must not be negative");
        }
    }

    private static boolean isChallengePage(final WebDriver webDriver) {
        final String title = webDriver.getTitle();
        return title != null && title.startsWith(CHALLENGE_TITLE_PREFIX);
    }

    private static boolean isDocumentReady(final WebDriver webDriver) {
        return DOCUMENT_READY_STATE_COMPLETE.equals(
            ((JavascriptExecutor) webDriver).executeScript(DOCUMENT_READY_STATE_SCRIPT));
    }

    /**
     * Loads a URL and returns the page as a JSoup document.
     *
     * @param url the URL to load
     * @return the parsed JSoup document
     * @throws IllegalStateException if the bot challenge does not clear within
     *                               the challenge timeout
     */
    public Document loadPage(final String url) {
        requireNonNull(url, "url must not be null");
        pause();
        LOG.info("Loading page: {}", url);
        driver.get(url);

        try {
            new WebDriverWait(driver, timeout).until(PageLoader::isDocumentReady);
        } catch (final TimeoutException e) {
            if (!isChallengePage(driver)) {
                throw e;
            }
        }

        if (isChallengePage(driver)) {
            waitForChallengeToClear(url);
        }

        final String pageSource = driver.getPageSource();
        return Jsoup.parse(requireNonNull(pageSource), url);
    }

    private void pause() {
        if (requestDelay.isZero()) {
            return;
        }
        final long baseMillis = requestDelay.toMillis();
        final long pauseMillis =
            baseMillis + ThreadLocalRandom.current().nextLong(baseMillis * 2 + 1);
        try {
            Thread.sleep(pauseMillis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForChallengeToClear(final String url) {
        LOG.info("Waiting up to {}s for bot challenge to clear: {}",
            challengeTimeout.toSeconds(), url);
        try {
            new WebDriverWait(driver, challengeTimeout)
                .until(webDriver -> !isChallengePage(webDriver) && isDocumentReady(webDriver));
        } catch (final TimeoutException e) {
            throw new IllegalStateException("Bot challenge did not clear for: " + url, e);
        }
        LOG.info("Bot challenge cleared: {}", url);
    }
}
