package io.github.onurdemir55.resurrections.rss.io;

import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Item;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.holder.PlainValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the output byte for byte, whitespace included.
 * <p>
 * The other tests that compare whole documents normalize whitespace first, because the XML
 * they compare against is copied from the README and wrapped there for readability. That
 * leaves indentation and the trailing newline unpinned, and indentation is written by hand
 * here — a depth counter in {@link RssWriter} — which makes it exactly the thing most worth
 * a test. Every element the library supports appears below, so a stray space, a missing
 * newline or an element written at the wrong depth fails this and nothing else.
 * <p>
 * If this test fails after a deliberate change, read the diff carefully before updating the
 * expected string: it is the only place that says what the output actually looks like.
 */
class ExactOutputTest {

    /**
     * Assembled rather than written inline only because the real line is longer than the
     * style limit for source. The value is exactly what the writer produces.
     */
    private static final String ROOT_ELEMENT = "<rss version=\"2.0\""
            + " xmlns:content=\"http://purl.org/rss/1.0/modules/content/\""
            + " xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
            + " xmlns:atom=\"http://www.w3.org/2005/Atom\">";

    @Test
    @DisplayName("every element, at exactly the expected indentation")
    void everyElement() {
        String expected = """
                <?xml version='1.0' encoding='UTF-8'?>
                %s
                  <channel>
                    <title><![CDATA[Every <b>element</b>]]></title>
                    <link>https://example.com/</link>
                    <description><![CDATA[Exercises every element.]]></description>
                    <atom:link href="https://example.com/feed.xml" rel="self" type="application/rss+xml"/>
                    <language>en-us</language>
                    <copyright>Copyright 2026, Onur Demir</copyright>
                    <managingEditor>editor@example.com (An Editor)</managingEditor>
                    <webMaster>webmaster@example.com (A Webmaster)</webMaster>
                    <pubDate>Wed, 05 Aug 2026 20:00:00 GMT</pubDate>
                    <lastBuildDate>Wed, 05 Aug 2026 20:00:00 GMT</lastBuildDate>
                    <category>Technology</category>
                    <category domain="Syndic8">1765</category>
                    <generator>resurrections-rss</generator>
                    <docs>https://www.rssboard.org/rss-specification</docs>
                    <cloud domain="rpc.example.com" port="80" path="/RPC2" registerProcedure="myCloud.rssPleaseNotify" protocol="xml-rpc"/>
                    <ttl>60</ttl>
                    <image>
                      <url>https://example.com/logo.png</url>
                      <title>Every element</title>
                      <link>https://example.com/</link>
                      <width>144</width>
                      <height>400</height>
                      <description><![CDATA[The <i>logo</i>]]></description>
                    </image>
                    <rating>(PICS-1.1)</rating>
                    <textInput>
                      <title>Search</title>
                      <description>Search the archive</description>
                      <name>q</name>
                      <link>https://example.com/search</link>
                    </textInput>
                    <skipHours>
                      <hour>0</hour>
                      <hour>23</hour>
                    </skipHours>
                    <skipDays>
                      <day>Saturday</day>
                      <day>Sunday</day>
                    </skipDays>
                    <dc:language>en</dc:language>
                    <item>
                      <title><![CDATA[An <b>item</b>]]></title>
                      <link>https://example.com/items/1</link>
                      <description><![CDATA[<p>Body with a terminator ]]]]><![CDATA[> inside.</p>]]></description>
                      <author>author@example.com (An Author)</author>
                      <category>Newspapers</category>
                      <category domain="http://www.fool.com/cusips">MSFT</category>
                      <comments>https://example.com/items/1/comments</comments>
                      <enclosure url="https://example.com/media/e.mp3" length="12216320" type="audio/mpeg"/>
                      <guid isPermaLink="true">https://example.com/items/1</guid>
                      <pubDate>Wed, 05 Aug 2026 20:00:00 GMT</pubDate>
                      <source url="https://origin.example.com/feed.xml">Origin Feed</source>
                      <content:encoded><![CDATA[<p>rich <b>html</b></p>]]></content:encoded>
                      <dc:creator>Onur Demir</dc:creator>
                    </item>
                  </channel>
                </rss>
                """;

        assertEquals(expected.formatted(ROOT_ELEMENT),
                RssOutput.outputString(ExactOutputSample.feed()));
    }

    @Test
    @DisplayName("a minimal feed, where nothing optional may sneak in")
    void minimal() {
        String expected = """
                <?xml version='1.0' encoding='UTF-8'?>
                <rss version="2.0">
                  <channel>
                    <title>A title</title>
                    <link>https://example.com/</link>
                    <description>A description</description>
                    <item>
                      <title>An item</title>
                    </item>
                  </channel>
                </rss>
                """;

        Rss rss = Rss.builder()
                .channel(Channel.builder()
                        .title(new PlainValue("A title"))
                        .link(new PlainValue("https://example.com/"))
                        .description(new PlainValue("A description"))
                        .items(Item.builder().title(new PlainValue("An item")).build())
                        .build())
                .build();

        assertEquals(expected, RssOutput.outputString(rss));
    }

    @Test
    @DisplayName("the document ends with a newline, so a written feed is a well-formed text file")
    void endsWithNewline() {
        String xml = RssOutput.outputString(ExactOutputSample.feed());

        assertEquals("</rss>\n", xml.substring(xml.length() - "</rss>\n".length()));
    }

}
