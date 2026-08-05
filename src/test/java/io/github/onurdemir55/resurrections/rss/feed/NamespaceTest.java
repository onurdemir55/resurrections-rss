package io.github.onurdemir55.resurrections.rss.feed;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.onurdemir55.resurrections.rss.feed.element.AtomLink;
import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.io.RssOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Extending a feed with elements from other namespaces.
 * <p>
 * The specification permits elements it does not describe only when they are defined in a
 * namespace, so a declaration on the root element and a prefix on the element name are both
 * required for the document to be well formed.
 */
class NamespaceTest {

    @Nested
    @DisplayName("declarations")
    class Declarations {

        @Test
        @DisplayName("a declared namespace becomes an xmlns attribute on rss")
        void declared() throws JsonProcessingException {
            String xml = RssOutput.outputString(Rss.builder()
                    .namespace("content", "http://purl.org/rss/1.0/modules/content/")
                    .channel(channel().build())
                    .build());

            assertTrue(xml.contains(
                    "xmlns:content=\"http://purl.org/rss/1.0/modules/content/\""), () -> xml);
        }

        @Test
        @DisplayName("declarations keep the order they were made in")
        void orderIsStable() throws JsonProcessingException {
            String xml = RssOutput.outputString(Rss.builder()
                    .namespace("content", "urn:content")
                    .namespace("dc", "urn:dc")
                    .namespace("media", "urn:media")
                    .channel(channel().build())
                    .build());

            assertTrue(xml.indexOf("xmlns:content") < xml.indexOf("xmlns:dc")
                    && xml.indexOf("xmlns:dc") < xml.indexOf("xmlns:media"), () -> xml);
        }

        @Test
        @DisplayName("a feed with no extensions declares no namespaces")
        void noneByDefault() throws JsonProcessingException {
            String xml = RssOutput.outputString(Rss.builder().channel(channel().build()).build());

            assertAll(
                    () -> assertFalse(xml.contains("xmlns"), () -> xml),
                    () -> assertTrue(xml.contains("<rss version=\"2.0\">"), () -> xml));
        }

        @Test
        @DisplayName("prefix and URI are both required")
        void bothRequired() {
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> Rss.builder().namespace(null, "urn:x")),
                    () -> assertThrows(NullPointerException.class,
                            () -> Rss.builder().namespace("x", null)));
        }
    }

    @Nested
    @DisplayName("atom:link")
    class Atom {

        @Test
        @DisplayName("the self link the Best Practices Profile recommends")
        void selfLink() throws JsonProcessingException {
            String xml = feedWithAtomLink(AtomLink.self("https://example.com/feed.xml"));

            assertTrue(xml.contains("<atom:link href=\"https://example.com/feed.xml\" "
                    + "rel=\"self\" type=\"application/rss+xml\"/>"), () -> xml);
        }

        @Test
        @DisplayName("using it declares the Atom namespace without being asked")
        void namespaceIsAutomatic() throws JsonProcessingException {
            String xml = feedWithAtomLink(AtomLink.self("https://example.com/feed.xml"));

            assertTrue(xml.contains("xmlns:atom=\"" + AtomLink.NAMESPACE + "\""),
                    () -> "a prefixed element without its declaration is malformed:\n" + xml);
        }

        @Test
        @DisplayName("an explicit declaration is not duplicated")
        void notDuplicated() throws JsonProcessingException {
            String xml = RssOutput.outputString(Rss.builder()
                    .namespace(AtomLink.PREFIX, AtomLink.NAMESPACE)
                    .channel(channel()
                            .atomLink(AtomLink.self("https://example.com/feed.xml"))
                            .build())
                    .build());

            assertEquals(1, countOccurrences(xml, "xmlns:atom"), () -> xml);
        }

        @Test
        @DisplayName("a link without a media type omits the attribute")
        void withoutType() throws JsonProcessingException {
            String xml = feedWithAtomLink(AtomLink.of("https://example.com/next", "next"));

            assertAll(
                    () -> assertTrue(xml.contains("rel=\"next\""), () -> xml),
                    () -> assertFalse(xml.contains("type="), () -> xml));
        }

        @Test
        @DisplayName("href and rel are required, and href needs a URI scheme")
        void validation() {
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> new AtomLink(null, "self", null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> new AtomLink("https://example.com/", null, null)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> AtomLink.self("example.com/feed.xml")));
        }

        private String feedWithAtomLink(final AtomLink link) throws JsonProcessingException {
            return RssOutput.outputString(Rss.builder()
                    .channel(channel().atomLink(link).build())
                    .build());
        }
    }

    @Nested
    @DisplayName("extension elements")
    class Extensions {

        @Test
        @DisplayName("a channel extension is written with its prefixed name")
        void channelExtension() throws JsonProcessingException {
            String xml = RssOutput.outputString(Rss.builder()
                    .namespace("dc", "http://purl.org/dc/elements/1.1/")
                    .channel(channel().extension("dc:language", new SimpleValue("en")).build())
                    .build());

            assertTrue(xml.contains("<dc:language>en</dc:language>"), () -> xml);
        }

        @Test
        @DisplayName("an item extension can carry CDATA, like any other element")
        void itemExtensionWithCdata() throws JsonProcessingException {
            Item item = Item.builder()
                    .title(new SimpleValue("t"))
                    .extension("content:encoded", new CDATAValue("<p>rich <b>html</b></p>"))
                    .build();

            String xml = RssOutput.outputString(Rss.builder()
                    .namespace("content", "http://purl.org/rss/1.0/modules/content/")
                    .channel(channel().items(item).build())
                    .build());

            assertTrue(xml.contains(
                    "<content:encoded><![CDATA[<p>rich <b>html</b></p>]]></content:encoded>"),
                    () -> xml);
        }

        @Test
        @DisplayName("extensions keep the order they were added in")
        void orderIsStable() throws JsonProcessingException {
            Item item = Item.builder()
                    .title(new SimpleValue("t"))
                    .extension("dc:creator", new SimpleValue("first"))
                    .extension("dc:date", new SimpleValue("second"))
                    .extension("dc:subject", new SimpleValue("third"))
                    .build();

            String xml = RssOutput.outputString(Rss.builder()
                    .namespace("dc", "urn:dc")
                    .channel(channel().items(item).build())
                    .build());

            assertTrue(xml.indexOf("dc:creator") < xml.indexOf("dc:date")
                    && xml.indexOf("dc:date") < xml.indexOf("dc:subject"), () -> xml);
        }

        @Test
        @DisplayName("name and value are both required")
        void bothRequired() {
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> Item.builder().extension(null, new SimpleValue("x"))),
                    () -> assertThrows(NullPointerException.class,
                            () -> Item.builder().extension("dc:creator", null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> Channel.builder().extension(null, new SimpleValue("x"))),
                    () -> assertThrows(NullPointerException.class,
                            () -> Channel.builder().extension("dc:creator", null)));
        }
    }

    private static Channel.Builder channel() {
        return Channel.builder()
                .title(new SimpleValue("Sample"))
                .link(new SimpleValue("https://example.com/"))
                .description(new SimpleValue("Sample feed"));
    }

    private static int countOccurrences(final String haystack, final String needle) {
        int count = 0;
        int index = haystack.indexOf(needle);
        while (index >= 0) {
            count++;
            index = haystack.indexOf(needle, index + needle.length());
        }
        return count;
    }
}
