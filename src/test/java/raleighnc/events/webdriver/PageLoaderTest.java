package raleighnc.events.webdriver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.time.Duration;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

class PageLoaderTest {

    private static final Duration LOAD_TIMEOUT = Duration.ofMillis(200);
    private static final String URL = "https://raleighnc.gov/parks-and-recreation/news/example";

    private WebDriver driver;

    private PageLoader pageLoader(final Duration challengeTimeout) {
        return new PageLoader(driver, LOAD_TIMEOUT, challengeTimeout, Duration.ZERO);
    }

    @BeforeEach
    void setUp() {
        driver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
        when(((JavascriptExecutor) driver).executeScript(anyString())).thenReturn("complete");
    }

    @Test
    void loadPageReturnsDocumentWhenPageIsReady() {
        when(driver.getTitle()).thenReturn("Example | Raleighnc.gov");
        when(driver.getPageSource()).thenReturn("<html><body><h1>Example</h1></body></html>");

        final Document doc = pageLoader(Duration.ofMillis(200)).loadPage(URL);

        assertThat(doc.selectFirst("h1").text()).isEqualTo("Example");
        assertThat(doc.location()).isEqualTo(URL);
    }

    @Test
    void loadPageWaitsForChallengeToClear() {
        when(driver.getTitle())
            .thenReturn("Just a moment...", "Just a moment...", "Example | Raleighnc.gov");
        when(driver.getPageSource()).thenReturn("<html><body><h1>Example</h1></body></html>");

        final Document doc = pageLoader(Duration.ofSeconds(5)).loadPage(URL);

        assertThat(doc.selectFirst("h1").text()).isEqualTo("Example");
    }

    @Test
    void loadPageThrowsWhenChallengeDoesNotClear() {
        when(driver.getTitle()).thenReturn("Just a moment...");

        assertThatThrownBy(() -> pageLoader(Duration.ofMillis(200)).loadPage(URL))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Bot challenge did not clear")
            .hasMessageContaining(URL);
    }

    @Test
    void loadPageRejectsNullUrl() {
        assertThatThrownBy(() -> pageLoader(Duration.ofMillis(200)).loadPage(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void loadPagePausesForAtLeastTheRequestDelay() {
        when(driver.getTitle()).thenReturn("Example | Raleighnc.gov");
        when(driver.getPageSource()).thenReturn("<html><body></body></html>");
        final PageLoader paced =
            new PageLoader(driver, LOAD_TIMEOUT, LOAD_TIMEOUT, Duration.ofMillis(100));

        final long start = System.nanoTime();
        paced.loadPage(URL);

        assertThat(Duration.ofNanos(System.nanoTime() - start)).isGreaterThanOrEqualTo(
            Duration.ofMillis(100));
    }

    @Test
    void constructorRejectsNullAndNegativeArguments() {
        assertThatThrownBy(() -> new PageLoader(null, LOAD_TIMEOUT, LOAD_TIMEOUT, Duration.ZERO))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new PageLoader(driver, LOAD_TIMEOUT, null, Duration.ZERO))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new PageLoader(driver, LOAD_TIMEOUT, LOAD_TIMEOUT, null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(
            () -> new PageLoader(driver, LOAD_TIMEOUT, LOAD_TIMEOUT, Duration.ofMillis(-1)))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
