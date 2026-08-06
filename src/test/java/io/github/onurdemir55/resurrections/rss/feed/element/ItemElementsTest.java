package io.github.onurdemir55.resurrections.rss.feed.element;

import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Item;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.io.RssOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The item sub-elements that carry attributes, checked against the examples given in the
 * RSS 2.0 specification.
 */
class ItemElementsTest {

    @Nested
    @DisplayName("guid")
    class GuidElement {

        @Test
        @DisplayName("without isPermaLink the attribute is omitted, so readers assume true")
        void withoutAttribute() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .guid(Guid.of("http://some.server.com/weblogItem3207"))
                    .build());

            assertAll(
                    () -> assertTrue(xml.contains(
                            "<guid>http://some.server.com/weblogItem3207</guid>"), () -> xml),
                    () -> assertFalse(xml.contains("isPermaLink"), () -> xml));
        }

        @Test
        @DisplayName("matches the specification example for a permalink")
        void permaLink() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .guid(Guid.of("http://inessential.com/2002/09/01.php#a2", true))
                    .build());

            assertTrue(xml.contains(
                    "<guid isPermaLink=\"true\">http://inessential.com/2002/09/01.php#a2</guid>"),
                    () -> xml);
        }

        @Test
        @DisplayName("isPermaLink false is emitted, because it differs from the default")
        void notAPermaLink() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .guid(Guid.of("opaque-id-42", false))
                    .build());

            assertTrue(xml.contains("<guid isPermaLink=\"false\">opaque-id-42</guid>"), () -> xml);
        }

        @Test
        @DisplayName("a guid without a value is rejected")
        void valueIsRequired() {
            assertThrows(NullPointerException.class, () -> Guid.of((String) null));
        }
    }

    @Nested
    @DisplayName("enclosure")
    class EnclosureElement {

        @Test
        @DisplayName("matches the specification example, as an empty element")
        void specificationExample() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .enclosure(Enclosure.of(
                            "http://www.scripting.com/mp3s/weatherReportSuite.mp3",
                            12216320L,
                            "audio/mpeg"))
                    .build());

            assertTrue(xml.contains("<enclosure url=\"http://www.scripting.com/mp3s/"
                            + "weatherReportSuite.mp3\" length=\"12216320\" type=\"audio/mpeg\"/>"),
                    () -> xml);
        }

        @Test
        @DisplayName("url and type are required")
        void requiredAttributes() {
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> Enclosure.of(null, 1L, "audio/mpeg")),
                    () -> assertThrows(NullPointerException.class,
                            () -> Enclosure.of("http://example.com/a.mp3", 1L, null)));
        }
    }

    @Nested
    @DisplayName("source")
    class SourceElement {

        @Test
        @DisplayName("matches the specification example")
        void specificationExample() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .source(Source.of("Tomalak's Realm", "http://www.tomalak.org/links2.xml"))
                    .build());

            // An apostrophe needs no escaping in element content, only in attribute values
            // that are delimited by single quotes.
            assertTrue(xml.contains("<source url=\"http://www.tomalak.org/links2.xml\">"
                    + "Tomalak's Realm</source>"), () -> xml);
        }

        @Test
        @DisplayName("both the value and the url are required")
        void requiredParts() {
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> Source.of((String) null, "http://example.com/feed.xml")),
                    () -> assertThrows(NullPointerException.class,
                            () -> Source.of("A Feed", null)));
        }
    }

    @Nested
    @DisplayName("category")
    class CategoryElement {

        @Test
        @DisplayName("matches both specification examples")
        void specificationExamples() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .category(
                            Category.of("Grateful Dead"),
                            Category.of("MSFT", "http://www.fool.com/cusips"))
                    .build());

            assertAll(
                    () -> assertTrue(xml.contains("<category>Grateful Dead</category>"), () -> xml),
                    () -> assertTrue(xml.contains(
                            "<category domain=\"http://www.fool.com/cusips\">MSFT</category>"),
                            () -> xml));
        }

        @Test
        @DisplayName("the same value under different domains is kept twice")
        void sameValueDifferentDomains() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .category(Category.of("X", "urn:a"), Category.of("X", "urn:b"))
                    .build());

            assertAll(
                    () -> assertTrue(xml.contains("<category domain=\"urn:a\">X</category>"), () -> xml),
                    () -> assertTrue(xml.contains("<category domain=\"urn:b\">X</category>"), () -> xml));
        }

        @Test
        @DisplayName("a category without a value is rejected")
        void valueIsRequired() {
            assertThrows(NullPointerException.class, () -> Category.of((String) null));
        }
    }

    @Nested
    @DisplayName("author and comments")
    class PlainTextElements {

        @Test
        @DisplayName("author matches the specification example")
        void author() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .author(new SimpleValue("lawyer@boyer.net (Lawyer Boyer)"))
                    .build());

            assertTrue(xml.contains("<author>lawyer@boyer.net (Lawyer Boyer)</author>"), () -> xml);
        }

        @Test
        @DisplayName("comments matches the specification example")
        void comments() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .comments(new SimpleValue("http://ekzemplo.com/entry/4403/comments"))
                    .build());

            assertTrue(xml.contains(
                    "<comments>http://ekzemplo.com/entry/4403/comments</comments>"), () -> xml);
        }

        @Test
        @DisplayName("these elements still accept CDATA, since they carry no attributes")
        void cdataStillAvailable() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .comments(new CDATAValue("http://example.com/?a=1&b=2"))
                    .build());

            assertTrue(xml.contains("<comments><![CDATA[http://example.com/?a=1&b=2]]></comments>"),
                    () -> xml);
        }
    }

    @Nested
    @DisplayName("CDATA in elements that also carry attributes")
    class CdataWithAttributes {

        @Test
        @DisplayName("a category keeps its domain attribute alongside a CDATA value")
        void categoryWithDomainAndCdata() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .category(Category.cdata("Top/News & <b>Sports</b>", "urn:taxonomy"))
                    .build());

            assertTrue(xml.contains(
                    "<category domain=\"urn:taxonomy\"><![CDATA[Top/News & <b>Sports</b>]]></category>"),
                    () -> xml);
        }

        @Test
        @DisplayName("a source keeps its url attribute alongside a CDATA value")
        void sourceWithUrlAndCdata() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .source(Source.cdata("Tomalak's <i>Realm</i>", "http://www.tomalak.org/links2.xml"))
                    .build());

            assertTrue(xml.contains("<source url=\"http://www.tomalak.org/links2.xml\">"
                    + "<![CDATA[Tomalak's <i>Realm</i>]]></source>"), () -> xml);
        }

        @Test
        @DisplayName("a guid keeps its isPermaLink attribute alongside a CDATA value")
        void guidWithAttributeAndCdata() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .guid(Guid.cdata("urn:id:a&b", false))
                    .build());

            assertTrue(xml.contains("<guid isPermaLink=\"false\"><![CDATA[urn:id:a&b]]></guid>"),
                    () -> xml);
        }

        @Test
        @DisplayName("the cdata factories work without an attribute too")
        void cdataWithoutAttribute() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .category(Category.cdata("Top/News & <b>Sports</b>"))
                    .guid(Guid.cdata("urn:id:a&b"))
                    .build());

            assertAll(
                    () -> assertTrue(xml.contains(
                            "<category><![CDATA[Top/News & <b>Sports</b>]]></category>"), () -> xml),
                    () -> assertTrue(xml.contains("<guid><![CDATA[urn:id:a&b]]></guid>"), () -> xml));
        }

        @Test
        @DisplayName("the plain form of the same element escapes instead of wrapping")
        void plainFormStillEscapes() {
            String xml = itemXml(Item.builder()
                    .title(new SimpleValue("t"))
                    .category(Category.of("a & b"))
                    .build());

            assertAll(
                    () -> assertTrue(xml.contains("<category>a &amp; b</category>"), () -> xml),
                    () -> assertFalse(xml.contains("CDATA[a"), () -> xml));
        }
    }

    @Test
    @DisplayName("all item elements are emitted in the order the specification lists them")
    void specificationElementOrder() {
        String xml = itemXml(Item.builder()
                .title(new SimpleValue("title"))
                .link(new SimpleValue("http://example.com/item"))
                .description(new CDATAValue("<p>body</p>"))
                .author(new SimpleValue("a@example.com"))
                .category(Category.of("cat"))
                .comments(new SimpleValue("http://example.com/comments"))
                .enclosure(Enclosure.of("http://example.com/a.mp3", 1234L, "audio/mpeg"))
                .guid(Guid.of("urn:id:1", false))
                .pubDate(new SimpleValue("Sat, 07 Sep 2002 00:00:01 GMT"))
                .source(Source.of("Origin", "http://origin.example.com/feed.xml"))
                .build());

        int previous = -1;
        String[] specificationOrder = {"<title>", "<link>", "<description>", "<author>",
                                       "<category>", "<comments>", "<enclosure ", "<guid ",
                                       "<pubDate>", "<source "};
        for (String element : specificationOrder) {
            int index = xml.indexOf(element, xml.indexOf("<item>"));
            assertTrue(index > previous,
                    () -> element + " is missing or out of specification order in:\n" + xml);
            previous = index;
        }
    }

    private static String itemXml(final Item item) {
        return RssOutput.outputString(Rss.builder()
                .channel(Channel.builder()
                        .title(new SimpleValue("feed"))
                        .link(new SimpleValue("https://example.com/"))
                        .description(new SimpleValue("a feed"))
                        .items(List.of(item))
                        .build())
                .build());
    }
}
