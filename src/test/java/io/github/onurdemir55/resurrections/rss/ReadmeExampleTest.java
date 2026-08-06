package io.github.onurdemir55.resurrections.rss;

import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Item;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.element.Category;
import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.io.RssOutput;
import io.github.onurdemir55.resurrections.rss.util.DateParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The example from the README, asserted against the output the README documents.
 * <p>
 * If this test has to change, the README has to change with it.
 */
class ReadmeExampleTest {

    private static final Instant PUBLISHED = Instant.parse("2002-09-07T00:00:01Z");

    @Test
    @DisplayName("the documented example produces the documented feed")
    void readmeExample() {
        String published = DateParser.formatRfc822(PUBLISHED);

        Item item = Item.builder()
                .title(new SimpleValue("sample title"))
                .link(new CDATAValue("https://www.google.com/"))
                .description(new CDATAValue("sample description"))
                .categories(Category.of("category-1"), Category.of("category-2"))
                .pubDate(new SimpleValue(published))
                .build();

        Channel channel = Channel.builder()
                .title(new CDATAValue("sample title"))
                .link(new SimpleValue("https://www.google.com/"))
                .description(new CDATAValue("sample description"))
                .language(new SimpleValue("en"))
                .pubDate(new SimpleValue(published))
                .items(List.of(item))
                .build();

        Rss rss = Rss.builder().channel(channel).build();

        String expected = """
                <?xml version='1.0' encoding='UTF-8'?>
                <rss version="2.0">
                  <channel>
                    <title><![CDATA[sample title]]></title>
                    <link>https://www.google.com/</link>
                    <description><![CDATA[sample description]]></description>
                    <language>en</language>
                    <pubDate>Sat, 07 Sep 2002 00:00:01 GMT</pubDate>
                    <item>
                      <title>sample title</title>
                      <link><![CDATA[https://www.google.com/]]></link>
                      <description><![CDATA[sample description]]></description>
                      <category>category-1</category>
                      <category>category-2</category>
                      <pubDate>Sat, 07 Sep 2002 00:00:01 GMT</pubDate>
                    </item>
                  </channel>
                </rss>
                """;

        assertEquals(expected.strip(), RssOutput.outputString(rss).strip());
    }
}
