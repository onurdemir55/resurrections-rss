package io.github.onurdemir55.resurrections.rss.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Item;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Serialization behaviour of {@link RssOutput}.
 * <p>
 * These assertions deliberately work on the raw output string rather than through an
 * XML-aware comparison. A semantic XML comparison treats a CDATA section and an escaped
 * text node as equivalent, which is exactly the distinction this library exists to make,
 * so comparing parsed documents would silently pass even if CDATA support broke.
 */
class RssOutputTest {

    @Nested
    @DisplayName("CDATA and plain text")
    class TextForm {

        @Test
        @DisplayName("CDATAValue is emitted inside a CDATA section")
        void cdataValueIsWrappedInCdataSection() throws JsonProcessingException {
            String xml = RssOutput.outputString(feedWithTitle(new CDATAValue("hello")));

            assertTrue(xml.contains("<title><![CDATA[hello]]></title>"),
                    () -> "expected a CDATA section, got:\n" + xml);
        }

        @Test
        @DisplayName("SimpleValue is emitted as plain text, without a CDATA section")
        void simpleValueIsPlainText() throws JsonProcessingException {
            String xml = RssOutput.outputString(feedWithTitle(new SimpleValue("hello")));

            assertAll(
                    () -> assertTrue(xml.contains("<title>hello</title>"), () -> xml),
                    () -> assertFalse(xml.contains("CDATA"), () -> "no CDATA expected:\n" + xml));
        }

        @Test
        @DisplayName("markup inside a CDATA section stays readable and is not entity-encoded")
        void markupInsideCdataIsNotEscaped() throws JsonProcessingException {
            String html = "<p class=\"lead\">Ampersand & <b>bold</b></p>";

            String xml = RssOutput.outputString(feedWithTitle(new CDATAValue(html)));

            assertAll(
                    () -> assertTrue(xml.contains("<title><![CDATA[" + html + "]]></title>"), () -> xml),
                    () -> assertFalse(xml.contains("&amp;"), () -> "CDATA must not be escaped:\n" + xml),
                    () -> assertFalse(xml.contains("&lt;"), () -> "CDATA must not be escaped:\n" + xml));
        }

        @Test
        @DisplayName("XML special characters in plain text are escaped")
        void specialCharactersInPlainTextAreEscaped() throws JsonProcessingException {
            String xml = RssOutput.outputString(
                    feedWithTitle(new SimpleValue("a & b < c > d")));

            assertAll(
                    () -> assertTrue(xml.contains("&amp;"), () -> "'&' must be escaped:\n" + xml),
                    () -> assertTrue(xml.contains("&lt;"), () -> "'<' must be escaped:\n" + xml),
                    () -> assertFalse(xml.contains("a & b"), () -> "raw '&' leaked:\n" + xml));
        }

        @Test
        @DisplayName("an empty CDATA value still produces a CDATA section")
        void emptyCdataValue() throws JsonProcessingException {
            String xml = RssOutput.outputString(feedWithTitle(new CDATAValue("")));

            assertTrue(xml.contains("<title><![CDATA[]]></title>"), () -> xml);
        }
    }

    @Nested
    @DisplayName("CDATA section terminator in content")
    class CdataTerminator {

        /**
         * Content containing {@code ]]>} would close the CDATA section early, so it has to
         * be split across two sections. A parser concatenates adjacent CDATA sections, so
         * the original text survives. This matters because the library exists to carry
         * HTML, and HTML can contain {@code ]]>}.
         */
        @Test
        @DisplayName("content containing ']]>' is split across two CDATA sections")
        void contentWithCdataTerminatorIsSplit() throws JsonProcessingException {
            String xml = RssOutput.outputString(feedWithTitle(new CDATAValue("danger ]]> after")));

            assertAll(
                    () -> assertTrue(
                            xml.contains("<title><![CDATA[danger ]]]]><![CDATA[> after]]></title>"),
                            () -> xml),
                    () -> assertEquals("danger ]]> after", concatenatedCdata(xml),
                            () -> "the split must reassemble to the original text:\n" + xml));
        }

        @Test
        @DisplayName("several ']]>' occurrences are all handled")
        void severalTerminators() throws JsonProcessingException {
            String text = "a ]]> b ]]> c";

            String xml = RssOutput.outputString(feedWithTitle(new CDATAValue(text)));

            assertEquals(text, concatenatedCdata(xml), () -> xml);
        }

        @Test
        @DisplayName("real HTML containing ']]>' serializes and reassembles")
        void htmlWithTerminator() throws JsonProcessingException {
            String html = "<script>if (a[b[c]]> 0) x();</script>";

            String xml = RssOutput.outputString(feedWithTitle(new CDATAValue(html)));

            assertEquals(html, concatenatedCdata(xml), () -> xml);
        }

        @Test
        @DisplayName("a lone ']]' without '>' is safe")
        void loneDoubleBracketIsFine() throws JsonProcessingException {
            String xml = RssOutput.outputString(feedWithTitle(new CDATAValue("array]] end")));

            assertTrue(xml.contains("<![CDATA[array]] end]]>"), () -> xml);
        }
    }

    @Nested
    @DisplayName("Document structure")
    class Structure {

        @Test
        @DisplayName("the document declares the XML prolog and rss version 2.0")
        void prologAndVersion() throws JsonProcessingException {
            String xml = RssOutput.outputString(feedWithTitle(new SimpleValue("t")));

            assertAll(
                    () -> assertTrue(xml.startsWith("<?xml"), () -> xml),
                    () -> assertTrue(xml.contains("encoding='UTF-8'") || xml.contains("encoding=\"UTF-8\""),
                            () -> xml),
                    () -> assertTrue(xml.contains("<rss version=\"2.0\">"), () -> xml));
        }

        @Test
        @DisplayName("channel elements are emitted in the order declared by @JsonPropertyOrder")
        void channelElementOrder() throws JsonProcessingException {
            Channel channel = Channel.builder()
                    .title(new SimpleValue("t"))
                    .link(new SimpleValue("https://example.com/"))
                    .description(new SimpleValue("d"))
                    .language(new SimpleValue("en"))
                    .pubDate(new SimpleValue("Sat, 07 Sep 2002 00:00:01 GMT"))
                    .build();

            String xml = RssOutput.outputString(Rss.builder().version("2.0").channel(channel).build());

            assertInOrder(xml, "<title>", "<link>", "<description>", "<language>", "<pubDate>");
        }

        @Test
        @DisplayName("item elements are emitted in the order declared by @JsonPropertyOrder")
        void itemElementOrder() throws JsonProcessingException {
            Item item = Item.builder()
                    .title(new SimpleValue("t"))
                    .link(new SimpleValue("https://example.com/"))
                    .description(new SimpleValue("d"))
                    .category(singleCategory("news"))
                    .pubDate(new SimpleValue("Sat, 07 Sep 2002 00:00:01 GMT"))
                    .build();

            String xml = RssOutput.outputString(feedWithItems(List.of(item)));

            assertInOrder(xml, "<title>", "<link>", "<description>", "<category>", "<pubDate>");
        }

        @Test
        @DisplayName("items are not wrapped in a container element")
        void itemsAreNotWrapped() throws JsonProcessingException {
            Item first = Item.builder().title(new SimpleValue("one")).build();
            Item second = Item.builder().title(new SimpleValue("two")).build();

            String xml = RssOutput.outputString(feedWithItems(List.of(first, second)));

            assertAll(
                    () -> assertFalse(xml.contains("<items>"), () -> "unexpected wrapper:\n" + xml),
                    () -> assertEquals(2, countOccurrences(xml, "<item>"), () -> xml));
        }

        @Test
        @DisplayName("categories repeat the element instead of nesting a wrapper")
        void categoriesRepeatTheElement() throws JsonProcessingException {
            Set<Value> categories = new LinkedHashSet<>();
            categories.add(new SimpleValue("first"));
            categories.add(new SimpleValue("second"));
            Item item = Item.builder().title(new SimpleValue("t")).category(categories).build();

            String xml = RssOutput.outputString(feedWithItems(List.of(item)));

            assertAll(
                    () -> assertEquals(2, countOccurrences(xml, "<category>"), () -> xml),
                    () -> assertFalse(xml.contains("<categories>"), () -> xml));
        }

        @Test
        @DisplayName("unset elements are omitted rather than emitted empty")
        void unsetElementsAreOmitted() throws JsonProcessingException {
            String xml = RssOutput.outputString(feedWithTitle(new SimpleValue("only a title")));

            assertAll(
                    () -> assertFalse(xml.contains("<language"), () -> "language was not set:\n" + xml),
                    () -> assertFalse(xml.contains("<pubDate"), () -> "pubDate was not set:\n" + xml),
                    () -> assertFalse(xml.contains("<item"), () -> "no items were set:\n" + xml));
        }
    }

    // helpers

    private static Rss feedWithTitle(final Value title) {
        return Rss.builder()
                .version("2.0")
                .channel(Channel.builder().title(title).build())
                .build();
    }

    private static Rss feedWithItems(final List<Item> items) {
        return Rss.builder()
                .version("2.0")
                .channel(Channel.builder().title(new SimpleValue("feed")).items(items).build())
                .build();
    }

    private static Set<Value> singleCategory(final String name) {
        Set<Value> categories = new LinkedHashSet<>();
        categories.add(new SimpleValue(name));
        return categories;
    }

    private static void assertInOrder(final String xml, final String... fragments) {
        int previous = -1;
        String previousFragment = null;
        for (String fragment : fragments) {
            int index = xml.indexOf(fragment);
            assertTrue(index >= 0, () -> "missing " + fragment + " in:\n" + xml);
            final int checkpoint = previous;
            final String before = previousFragment;
            assertTrue(index > checkpoint,
                    () -> fragment + " should come after " + before + " in:\n" + xml);
            previous = index;
            previousFragment = fragment;
        }
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

    /**
     * Concatenates the contents of every CDATA section in the document, which is what a
     * parser does with adjacent sections. Used to assert that splitting a section around
     * {@code ]]>} preserves the original text.
     */
    private static String concatenatedCdata(final String xml) {
        StringBuilder text = new StringBuilder();
        String open = "<![CDATA[";
        String close = "]]>";
        int cursor = xml.indexOf(open);
        while (cursor >= 0) {
            int contentStart = cursor + open.length();
            int contentEnd = xml.indexOf(close, contentStart);
            if (contentEnd < 0) {
                throw new AssertionError("unterminated CDATA section in:\n" + xml);
            }
            text.append(xml, contentStart, contentEnd);
            cursor = xml.indexOf(open, contentEnd + close.length());
        }
        return text.toString();
    }
}
