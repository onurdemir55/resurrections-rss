package io.github.onurdemir55.resurrections.rss;

import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Item;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.element.AtomLink;
import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.io.RssOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The "Extending the feed" example from the README, asserted against the exact output the
 * README documents.
 * <p>
 * This is a full string comparison rather than a set of {@code contains(...)} checks
 * elsewhere in the test suite, on purpose. A getter added without {@code @JsonIgnore} once
 * leaked an unwanted {@code <namespaces>} element into this exact output, and every
 * {@code contains(...)}-based assertion in {@code NamespaceTest} kept passing because none
 * of them checked that nothing extra was there. Only a full comparison catches that class
 * of regression.
 */
class ReadmeExtendingExampleTest {

    @Test
    @DisplayName("the documented extension example produces exactly the documented feed")
    void readmeExtendingExample() {
        Item item = Item.builder()
                .title(new SimpleValue("An item"))
                .extension("content:encoded", new CDATAValue("<p>rich <b>html</b></p>"))
                .extension("dc:creator", new SimpleValue("Onur Demir"))
                .build();

        Channel channel = Channel.builder()
                .title(new SimpleValue("Sample"))
                .link(new SimpleValue("https://example.com/"))
                .description(new SimpleValue("Sample feed"))
                .atomLink(AtomLink.self("https://example.com/feed.xml"))
                .items(item)
                .build();

        Rss rss = Rss.builder()
                .namespace("content", "http://purl.org/rss/1.0/modules/content/")
                .namespace("dc", "http://purl.org/dc/elements/1.1/")
                .channel(channel)
                .build();

        String expected = """
                <?xml version='1.0' encoding='UTF-8'?>
                <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/"
                     xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:atom="http://www.w3.org/2005/Atom">
                  <channel>
                    <title>Sample</title>
                    <link>https://example.com/</link>
                    <description>Sample feed</description>
                    <atom:link href="https://example.com/feed.xml" rel="self" type="application/rss+xml"/>
                    <item>
                      <title>An item</title>
                      <content:encoded><![CDATA[<p>rich <b>html</b></p>]]></content:encoded>
                      <dc:creator>Onur Demir</dc:creator>
                    </item>
                  </channel>
                </rss>
                """;

        // The README wraps the opening tag across two lines for readability; the actual
        // output does not. Normalize whitespace rather than the content.
        String normalizedExpected = expected.strip().replaceAll("\\s+", " ");
        String normalizedActual = RssOutput.outputString(rss).strip().replaceAll("\\s+", " ");

        assertEquals(normalizedExpected, normalizedActual);
    }
}
