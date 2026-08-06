package io.github.onurdemir55.resurrections.rss;

import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Item;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.element.Category;
import io.github.onurdemir55.resurrections.rss.feed.element.Guid;
import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.PlainValue;
import io.github.onurdemir55.resurrections.rss.io.RssOutput;
import io.github.onurdemir55.resurrections.rss.util.DateParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The example from the README, asserted against the output the README documents.
 * <p>
 * If this test has to change, the README has to change with it.
 * <p>
 * The example is chosen to earn its place rather than to be short. A title carrying an
 * ampersand and a description carrying real markup put the library's one decision side by side
 * in the output: {@code &amp;} in the plain title, and tags left alone inside the CDATA
 * section. The previous example wrapped the words "sample description" in CDATA, which
 * demonstrated the syntax and none of the point, and wrapped a URL in one, which is a habit
 * worth not teaching.
 * <p>
 * The date is fixed. The README used to say {@code Instant.now()} beside an output showing a
 * date in 2002, so the one thing a reader was most likely to do - copy it and run it - could
 * not reproduce what was printed underneath.
 */
class ReadmeExampleTest {

    private static final Instant PUBLISHED = Instant.parse("2026-02-19T08:30:00Z");

    /**
     * Assembled rather than written inline only because the real line is longer than the style
     * limit for source. The value is exactly what the writer produces.
     */
    private static final String ITEM_DESCRIPTION =
            "<description><![CDATA[<p>Full-year profit rose <b>18%</b> and the board raised the "
            + "dividend to <b>$1.24</b>. Read the "
            + "<a href=\"/2026/02/aurora-foods-dividend\">full report</a>.</p>]]></description>";

    @Test
    @DisplayName("the documented example produces the documented feed")
    void readmeExample() {
        String published = DateParser.formatRfc822(PUBLISHED);

        Item item = Item.builder()
                .title(new PlainValue("Aurora Foods beats forecasts & lifts its dividend"))
                .link(new PlainValue("https://example.com/2026/02/aurora-foods-dividend"))
                .description(new CDATAValue(
                        "<p>Full-year profit rose <b>18%</b> and the board raised the dividend "
                                + "to <b>$1.24</b>. Read the "
                                + "<a href=\"/2026/02/aurora-foods-dividend\">full report</a>.</p>"))
                .categories(Category.of("Earnings"), Category.of("Equities"))
                .guid(Guid.of("https://example.com/2026/02/aurora-foods-dividend", true))
                .pubDate(new PlainValue(published))
                .build();

        Channel channel = Channel.builder()
                .title(new PlainValue("Markets & Mornings"))
                .link(new PlainValue("https://example.com/"))
                .description(new CDATAValue(
                        "<p>Good news from the markets, before your <i>first coffee</i>.</p>"))
                .language(new PlainValue("en-us"))
                .pubDate(new PlainValue(published))
                .items(item)
                .build();

        Rss rss = Rss.builder().channel(channel).build();

        String expected = """
                <?xml version='1.0' encoding='UTF-8'?>
                <rss version="2.0">
                  <channel>
                    <title>Markets &amp; Mornings</title>
                    <link>https://example.com/</link>
                    <description><![CDATA[<p>Good news from the markets, before your <i>first coffee</i>.</p>]]></description>
                    <language>en-us</language>
                    <pubDate>Thu, 19 Feb 2026 08:30:00 GMT</pubDate>
                    <item>
                      <title>Aurora Foods beats forecasts &amp; lifts its dividend</title>
                      <link>https://example.com/2026/02/aurora-foods-dividend</link>
                      %s
                      <category>Earnings</category>
                      <category>Equities</category>
                      <guid isPermaLink="true">https://example.com/2026/02/aurora-foods-dividend</guid>
                      <pubDate>Thu, 19 Feb 2026 08:30:00 GMT</pubDate>
                    </item>
                  </channel>
                </rss>
                """;

        assertEquals(expected.formatted(ITEM_DESCRIPTION).strip(),
                RssOutput.outputString(rss).strip());
    }
}
