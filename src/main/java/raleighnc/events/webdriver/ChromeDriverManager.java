package raleighnc.events.webdriver;

import static java.util.Objects.requireNonNull;
import static raleighnc.events.webdriver.ChromeOptionsConstants.CHROME_TOKEN;
import static raleighnc.events.webdriver.ChromeOptionsConstants.DISABLE_AUTOMATION_CONTROLLED;
import static raleighnc.events.webdriver.ChromeOptionsConstants.DISABLE_DEV_SHM;
import static raleighnc.events.webdriver.ChromeOptionsConstants.DISABLE_GPU;
import static raleighnc.events.webdriver.ChromeOptionsConstants.ENABLE_AUTOMATION_SWITCH;
import static raleighnc.events.webdriver.ChromeOptionsConstants.EXCLUDE_SWITCHES_OPTION;
import static raleighnc.events.webdriver.ChromeOptionsConstants.HEADLESS;
import static raleighnc.events.webdriver.ChromeOptionsConstants.HEADLESS_CHROME_TOKEN;
import static raleighnc.events.webdriver.ChromeOptionsConstants.NO_SANDBOX;
import static raleighnc.events.webdriver.ChromeOptionsConstants.USER_AGENT_PREFIX;
import static raleighnc.events.webdriver.ChromeOptionsConstants.USER_AGENT_SCRIPT;
import static raleighnc.events.webdriver.ChromeOptionsConstants.WINDOW_SIZE;

import java.util.List;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raleighnc.events.config.ScraperConfiguration;

/**
 * Manages ChromeDriver for headless browsing.
 */
public final class ChromeDriverManager implements WebDriverManager {

    private static final Logger LOG = LoggerFactory.getLogger(ChromeDriverManager.class);
    private final WebDriver driver;

    /**
     * Creates a new ChromeDriverManager with the given configuration.
     *
     * @param config Scraper configuration
     * @throws NullPointerException if config is null
     */
    public ChromeDriverManager(ScraperConfiguration config) {
        requireNonNull(config, "config must not be null");
        String userAgent = config.getUserAgent().isBlank()
            ? detectBrowserUserAgent()
            : config.getUserAgent();
        driver = new ChromeDriver(createChromeOptions(userAgent));

        driver.manage().timeouts().pageLoadTimeout(config.getPageLoadTimeout());

        LOG.info("Chrome WebDriver initialized in headless mode");
    }

    private static ChromeOptions createBaseChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(HEADLESS);
        options.addArguments(NO_SANDBOX);
        options.addArguments(DISABLE_DEV_SHM);
        options.addArguments(DISABLE_GPU);
        options.addArguments(WINDOW_SIZE);
        options.addArguments(DISABLE_AUTOMATION_CONTROLLED);
        options.setExperimentalOption(EXCLUDE_SWITCHES_OPTION, List.of(ENABLE_AUTOMATION_SWITCH));
        return options;
    }

    private static ChromeOptions createChromeOptions(String userAgent) {
        ChromeOptions options = createBaseChromeOptions();
        options.addArguments(USER_AGENT_PREFIX + userAgent);
        return options;
    }

    /**
     * Derives a non-headless user agent from the installed browser.
     *
     * <p>Starts a short-lived browser to read its user agent and replaces the
     * "HeadlessChrome" token. Cloudflare rejects a user agent whose Chrome version
     * does not match the browser's client hints, and only accepts the override when
     * passed as a launch argument, so a hard-coded or CDP-applied user agent fails.
     *
     * @return the browser's user agent without the headless token
     */
    private static String detectBrowserUserAgent() {
        WebDriver probe = new ChromeDriver(createBaseChromeOptions());
        try {
            String browserUserAgent =
                (String) ((JavascriptExecutor) probe).executeScript(USER_AGENT_SCRIPT);
            String userAgent =
                requireNonNull(browserUserAgent).replace(HEADLESS_CHROME_TOKEN, CHROME_TOKEN);
            LOG.info("Using browser user agent: {}", userAgent);
            return userAgent;
        } finally {
            probe.quit();
        }
    }

    @Override
    public WebDriver getDriver() {
        return driver;
    }

    @Override
    public void quit() {
        if (driver != null) {
            driver.quit();
            LOG.info("Shutting down Chrome WebDriver");
        }
    }
}
