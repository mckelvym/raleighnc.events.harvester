package raleighnc.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TitleExtractorTest {

    private TitleExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new TitleExtractor();
    }

    @Test
    void extractTitle_withH1_returnsTitle() {
        String html = "<div><h1>News Article</h1></div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("News Article");
    }

    @Test
    void extractTitle_withH1Whitespace_returnsTrimmedTitle() {
        String html = "<div><h1>  News Title  </h1></div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("News Title");
    }

    @Test
    void extractTitle_withTeaserTitleLink_returnsTitle() {
        String html = "<div><a class=\"c-teaser__title-link\">News Link</a></div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("News Link");
    }

    @Test
    void extractTitle_withH2_returnsTitle() {
        String html = "<div><h2>Section Title</h2></div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("Section Title");
    }

    @Test
    void extractTitle_withH3_returnsTitle() {
        String html = "<div><h3>Subsection</h3></div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("Subsection");
    }

    @Test
    void extractTitle_withTitleAttribute_returnsTitle() {
        String html = "<html><body><div title=\"Title Attribute\"></div></body></html>";
        Document doc = Jsoup.parse(html);
        Element card = doc.selectFirst("div");

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("Title Attribute");
    }

    @Test
    void extractTitle_withH1Priority_ignoresTeaserLink() {
        String html = "<div>"
                + "<h1>H1 Title</h1>"
                + "<a class=\"c-teaser__title-link\">Teaser Title</a>"
                + "</div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("H1 Title");
    }

    @Test
    void extractTitle_withTeaserLinkPriority_ignoresH2() {
        String html = "<div>"
                + "<h1>   </h1>"
                + "<a class=\"c-teaser__title-link\">Teaser Title</a>"
                + "<h2>H2 Title</h2>"
                + "</div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("Teaser Title");
    }

    @Test
    void extractTitle_withHeadingsPriority_ignoresTitleAttribute() {
        String html = "<div title=\"Title Attr\">"
                + "<h1>  </h1>"
                + "<a class=\"c-teaser__title-link\">   </a>"
                + "<h2>H2 Title</h2>"
                + "</div>";
        Element card = Jsoup.parse(html);

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("H2 Title");
    }

    @Test
    void extractTitle_withBlankH1_usesTeaserLink() {
        String html = "<div>"
                + "<h1>   </h1>"
                + "<a class=\"c-teaser__title-link\">Teaser Title</a>"
                + "</div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("Teaser Title");
    }

    @Test
    void extractTitle_withBlankTeaserLink_usesH2() {
        String html = "<div>"
                + "<h1>   </h1>"
                + "<a class=\"c-teaser__title-link\">  </a>"
                + "<h2>H2 Title</h2>"
                + "</div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("H2 Title");
    }

    @Test
    void extractTitle_withBlankHeadings_usesTitleAttribute() {
        String html = "<html><body><div title=\"Title Attr\">"
                + "<h1>   </h1>"
                + "<a class=\"c-teaser__title-link\">   </a>"
                + "<h2>  </h2>"
                + "<h3></h3>"
                + "<h4>   </h4>"
                + "<h5></h5>"
                + "<h6>  </h6>"
                + "</div></body></html>";
        Document doc = Jsoup.parse(html);
        Element card = doc.selectFirst("div");

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("Title Attr");
    }

    @Test
    void extractTitle_withNoElements_returnsNull() {
        String html = "<div><p>Some content</p></div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isNull();
    }

    @Test
    void extractTitle_withBlankTitleAttribute_returnsNull() {
        String html = "<div title=\"\"><h1>  </h1></div>";
        Element card = Jsoup.parse(html);

        String result = extractor.extractTitle(card);

        assertThat(result).isNull();
    }

    @Test
    void extractTitle_withMultipleH1_returnsFirstOne() {
        String html = "<div>"
                + "<h1>First Title</h1>"
                + "<h1>Second Title</h1>"
                + "</div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("First Title");
    }

    @Test
    void extractTitle_withMultipleTeaserLinks_returnsFirstOne() {
        String html = "<div>"
                + "<h1>   </h1>"
                + "<a class=\"c-teaser__title-link\">First Title</a>"
                + "<a class=\"c-teaser__title-link\">Second Title</a>"
                + "</div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("First Title");
    }

    @Test
    void extractTitle_withH4_returnsTitle() {
        String html = "<div>"
                + "<h1>   </h1>"
                + "<a class=\"c-teaser__title-link\">   </a>"
                + "<h2></h2>"
                + "<h3>   </h3>"
                + "<h4>H4 Title</h4>"
                + "</div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("H4 Title");
    }

    @Test
    void extractTitle_withH5_returnsTitle() {
        String html = "<div>"
                + "<h1>   </h1>"
                + "<a class=\"c-teaser__title-link\">   </a>"
                + "<h2></h2>"
                + "<h3>   </h3>"
                + "<h4></h4>"
                + "<h5>H5 Title</h5>"
                + "</div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("H5 Title");
    }

    @Test
    void extractTitle_withH6_returnsTitle() {
        String html = "<div>"
                + "<h1>   </h1>"
                + "<a class=\"c-teaser__title-link\">   </a>"
                + "<h2></h2>"
                + "<h3>   </h3>"
                + "<h4></h4>"
                + "<h5>   </h5>"
                + "<h6>H6 Title</h6>"
                + "</div>";
        Element card = Jsoup.parse(html).body();

        String result = extractor.extractTitle(card);

        assertThat(result).isEqualTo("H6 Title");
    }
}
