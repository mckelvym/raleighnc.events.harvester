package raleighnc.events.webdriver;

/**
 * Constants for Chrome WebDriver options and arguments.
 *
 * <p>This class centralizes all Chrome-specific command-line arguments
 * used when configuring the headless Chrome browser for web scraping.
 */
public final class ChromeOptionsConstants {

    /**
     * Enables the new headless mode in Chrome.
     * The new headless mode provides better compatibility and performance.
     */
    public static final String HEADLESS = "--headless=new";

    /**
     * Disables GPU hardware acceleration.
     * Required for headless mode and helps avoid crashes in containerized environments.
     */
    public static final String DISABLE_GPU = "--disable-gpu";

    /**
     * Disables Chrome's sandbox security feature.
     * Often required in Docker containers where sandboxing may not work properly.
     */
    public static final String NO_SANDBOX = "--no-sandbox";

    /**
     * Disables /dev/shm usage.
     * Prevents crashes in Docker environments with limited shared memory.
     */
    public static final String DISABLE_DEV_SHM = "--disable-dev-shm-usage";

    /**
     * Prefix for user agent argument.
     * Followed by the custom user agent string.
     */
    public static final String USER_AGENT_PREFIX = "--user-agent=";

    /**
     * Prefix for window size argument.
     * Followed by "{width},{height}" format.
     */
    public static final String WINDOW_SIZE_PREFIX = "--window-size=";

    /**
     * Default window size (1920x1080).
     */
    public static final String DEFAULT_WINDOW_SIZE = "1920,1080";

    /**
     * Full window size argument.
     */
    public static final String WINDOW_SIZE = WINDOW_SIZE_PREFIX + DEFAULT_WINDOW_SIZE;

    /**
     * Hides the navigator.webdriver automation flag.
     * Required to pass the Cloudflare bot challenge on raleighnc.gov detail pages.
     */
    public static final String DISABLE_AUTOMATION_CONTROLLED =
        "--disable-blink-features=AutomationControlled";

    /**
     * Chrome experimental option name for excluding default command-line switches.
     */
    public static final String EXCLUDE_SWITCHES_OPTION = "excludeSwitches";

    /**
     * Default switch that marks the browser as automated; excluded to avoid bot detection.
     */
    public static final String ENABLE_AUTOMATION_SWITCH = "enable-automation";

    /**
     * User agent token used by headless Chrome; flagged by bot detection.
     */
    public static final String HEADLESS_CHROME_TOKEN = "HeadlessChrome";

    /**
     * User agent token used by regular (headed) Chrome.
     */
    public static final String CHROME_TOKEN = "Chrome";

    /**
     * JavaScript returning the browser's user agent.
     */
    public static final String USER_AGENT_SCRIPT = "return navigator.userAgent";

    private ChromeOptionsConstants() {
        // Utility class - prevent instantiation
    }
}
