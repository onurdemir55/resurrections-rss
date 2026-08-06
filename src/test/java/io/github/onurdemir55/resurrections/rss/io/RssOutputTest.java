package io.github.onurdemir55.resurrections.rss.io;

import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Item;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.element.Category;
import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.PlainValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        void cdataValueIsWrappedInCdataSection() {
            String xml = RssOutput.outputString(feedWithTitle(new CDATAValue("hello")));

            assertTrue(xml.contains("<title><![CDATA[hello]]></title>"),
                    () -> "expected a CDATA section, got:\n" + xml);
        }

        @Test
        @DisplayName("PlainValue is emitted as plain text, without a CDATA section")
        void simpleValueIsPlainText() {
            String xml = RssOutput.outputString(feedWithTitle(new PlainValue("hello")));

            assertAll(
                    () -> assertTrue(xml.contains("<title>hello</title>"), () -> xml),
                    () -> assertFalse(xml.contains("CDATA"), () -> "no CDATA expected:\n" + xml));
        }

        @Test
        @DisplayName("markup inside a CDATA section stays readable and is not entity-encoded")
        void markupInsideCdataIsNotEscaped() {
            String html = "<p class=\"lead\">Ampersand & <b>bold</b></p>";

            String xml = RssOutput.outputString(feedWithTitle(new CDATAValue(html)));

            assertAll(
                    () -> assertTrue(xml.contains("<title><![CDATA[" + html + "]]></title>"), () -> xml),
                    () -> assertFalse(xml.contains("&amp;"), () -> "CDATA must not be escaped:\n" + xml),
                    () -> assertFalse(xml.contains("&lt;"), () -> "CDATA must not be escaped:\n" + xml));
        }

        @Test
        @DisplayName("XML special characters in plain text are escaped")
        void specialCharactersInPlainTextAreEscaped() {
            String xml = RssOutput.outputString(
                    feedWithTitle(new PlainValue("a & b < c > d")));

            assertAll(
                    () -> assertTrue(xml.contains("&amp;"), () -> "'&' must be escaped:\n" + xml),
                    () -> assertTrue(xml.contains("&lt;"), () -> "'<' must be escaped:\n" + xml),
                    () -> assertFalse(xml.contains("a & b"), () -> "raw '&' leaked:\n" + xml));
        }

        @Test
        @DisplayName("an empty CDATA value still produces a CDATA section")
        void emptyCdataValue() {
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
        void contentWithCdataTerminatorIsSplit() {
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
        void severalTerminators() {
            String text = "a ]]> b ]]> c";

            String xml = RssOutput.outputString(feedWithTitle(new CDATAValue(text)));

            assertEquals(text, concatenatedCdata(xml), () -> xml);
        }

        @Test
        @DisplayName("real HTML containing ']]>' serializes and reassembles")
        void htmlWithTerminator() {
            String html = "<script>if (a[b[c]]> 0) x();</script>";

            String xml = RssOutput.outputString(feedWithTitle(new CDATAValue(html)));

            assertEquals(html, concatenatedCdata(xml), () -> xml);
        }

        @Test
        @DisplayName("a lone ']]' without '>' is safe")
        void loneDoubleBracketIsFine() {
            String xml = RssOutput.outputString(feedWithTitle(new CDATAValue("array]] end")));

            assertTrue(xml.contains("<![CDATA[array]] end]]>"), () -> xml);
        }
    }

    @Nested
    @DisplayName("Document structure")
    class Structure {

        @Test
        @DisplayName("the document declares the XML prolog and rss version 2.0")
        void prologAndVersion() {
            String xml = RssOutput.outputString(feedWithTitle(new PlainValue("t")));

            assertAll(
                    () -> assertTrue(xml.startsWith("<?xml"), () -> xml),
                    () -> assertTrue(xml.contains("encoding='UTF-8'") || xml.contains("encoding=\"UTF-8\""),
                            () -> xml),
                    () -> assertTrue(xml.contains("<rss version=\"2.0\">"), () -> xml));
        }

        @Test
        @DisplayName("channel elements are emitted in the order the specification lists them")
        void channelElementOrder() {
            Channel channel = Channel.builder()
                    .title(new PlainValue("t"))
                    .link(new PlainValue("https://example.com/"))
                    .description(new PlainValue("d"))
                    .language(new PlainValue("en"))
                    .pubDate(new PlainValue("Sat, 07 Sep 2002 00:00:01 GMT"))
                    .build();

            String xml = RssOutput.outputString(Rss.builder().version("2.0").channel(channel).build());

            assertInOrder(xml, "<title>", "<link>", "<description>", "<language>", "<pubDate>");
        }

        @Test
        @DisplayName("item elements are emitted in the order the specification lists them")
        void itemElementOrder() {
            Item item = Item.builder()
                    .title(new PlainValue("t"))
                    .link(new PlainValue("https://example.com/"))
                    .description(new PlainValue("d"))
                    .categories(Category.of("news"))
                    .pubDate(new PlainValue("Sat, 07 Sep 2002 00:00:01 GMT"))
                    .build();

            String xml = RssOutput.outputString(feedWithItems(List.of(item)));

            assertInOrder(xml, "<title>", "<link>", "<description>", "<category>", "<pubDate>");
        }

        @Test
        @DisplayName("items are not wrapped in a container element")
        void itemsAreNotWrapped() {
            Item first = Item.builder().title(new PlainValue("one")).build();
            Item second = Item.builder().title(new PlainValue("two")).build();

            String xml = RssOutput.outputString(feedWithItems(List.of(first, second)));

            assertAll(
                    () -> assertFalse(xml.contains("<items>"), () -> "unexpected wrapper:\n" + xml),
                    () -> assertEquals(2, countOccurrences(xml, "<item>"), () -> xml));
        }

        @Test
        @DisplayName("categories repeat the element, in the order given")
        void categoriesRepeatTheElementInOrder() {
            Item item = Item.builder()
                    .title(new PlainValue("t"))
                    .categories(Category.of("first"), Category.of("second"))
                    .build();

            String xml = RssOutput.outputString(feedWithItems(List.of(item)));

            assertAll(
                    () -> assertEquals(2, countOccurrences(xml, "<category>"), () -> xml),
                    () -> assertFalse(xml.contains("<categories>"), () -> xml),
                    () -> assertInOrder(xml, "<category>first</category>", "<category>second</category>"));
        }

        @Test
        @DisplayName("the List and varargs category forms behave identically")
        void categoryOverloadsAgree() {
            Item fromVarargs = Item.builder()
                    .title(new PlainValue("t"))
                    .categories(Category.of("a"), Category.of("b", "urn:taxonomy"))
                    .build();
            Item fromList = Item.builder()
                    .title(new PlainValue("t"))
                    .categories(List.of(Category.of("a"), Category.of("b", "urn:taxonomy")))
                    .build();

            assertEquals(
                    RssOutput.outputString(feedWithItems(List.of(fromVarargs))),
                    RssOutput.outputString(feedWithItems(List.of(fromList))));
        }

        @Test
        @DisplayName("repeated category text is kept, since taxonomies may overlap")
        void repeatedCategoryTextIsKept() {
            Item item = Item.builder()
                    .title(new PlainValue("t"))
                    .categories(Category.of("same"), Category.of("same"))
                    .build();

            String xml = RssOutput.outputString(feedWithItems(List.of(item)));

            assertEquals(2, countOccurrences(xml, "<category>same</category>"), () -> xml);
        }

        @Test
        @DisplayName("unset elements are omitted rather than emitted empty")
        void unsetElementsAreOmitted() {
            String xml = RssOutput.outputString(feedWithTitle(new PlainValue("only a title")));

            assertAll(
                    () -> assertFalse(xml.contains("<language"), () -> "language was not set:\n" + xml),
                    () -> assertFalse(xml.contains("<pubDate"), () -> "pubDate was not set:\n" + xml),
                    () -> assertFalse(xml.contains("<item"), () -> "no items were set:\n" + xml));
        }
    }

    @Nested
    @DisplayName("Output targets")
    class OutputTargets {

        @Test
        @DisplayName("all targets produce the same document as outputString")
        void targetsAgree(@TempDir final Path dir) throws IOException {
            Rss rss = feedWithTitle(new CDATAValue("same everywhere"));
            String expected = RssOutput.outputString(rss);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            RssOutput.output(rss, bytes);

            StringWriter writer = new StringWriter();
            RssOutput.output(rss, writer);

            Path file = dir.resolve("feed.xml");
            RssOutput.output(rss, file);

            assertAll(
                    () -> assertEquals(expected, bytes.toString(StandardCharsets.UTF_8)),
                    () -> assertEquals(expected, writer.toString()),
                    () -> assertEquals(expected, Files.readString(file, StandardCharsets.UTF_8)));
        }

        @Test
        @DisplayName("the stream is written as UTF-8, matching the declared encoding")
        void streamIsUtf8() throws IOException {
            Rss rss = feedWithTitle(new PlainValue("ünïcödé ığşç"));

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            RssOutput.output(rss, bytes);

            String decoded = bytes.toString(StandardCharsets.UTF_8);
            assertAll(
                    () -> assertTrue(decoded.contains("ünïcödé ığşç"), () -> decoded),
                    () -> assertTrue(decoded.contains("UTF-8"), () -> decoded));
        }
    }

    @Nested
    @DisplayName("Defaults")
    class Defaults {

        @Test
        @DisplayName("version defaults to 2.0 without being set")
        void versionDefaultsToTwoPointZero() {
            Rss rss = Rss.builder().channel(channel().build()).build();

            String xml = RssOutput.outputString(rss);

            assertTrue(xml.contains("<rss version=\"2.0\">"), () -> xml);
        }
    }

    @Nested
    @DisplayName("Null arguments")
    class NullArguments {

        @Test
        @DisplayName("outputString rejects a null feed")
        void outputStringRejectsNull() {
            assertThrows(NullPointerException.class, () -> RssOutput.outputString(null));
        }

        @Test
        @DisplayName("the OutputStream, Writer and Path overloads reject null arguments")
        void outputOverloadsRejectNull() throws IOException {
            Rss rss = feedWithTitle(new PlainValue("t"));

            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> RssOutput.output(null, new ByteArrayOutputStream())),
                    () -> assertThrows(NullPointerException.class,
                            () -> RssOutput.output(rss, (java.io.OutputStream) null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> RssOutput.output(null, new StringWriter())),
                    () -> assertThrows(NullPointerException.class,
                            () -> RssOutput.output(rss, (java.io.Writer) null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> RssOutput.output(null, Path.of("x"))),
                    () -> assertThrows(NullPointerException.class,
                            () -> RssOutput.output(rss, (Path) null)));
        }

        @Test
        @DisplayName("output(Rss, OutputStream) does not close the caller's stream")
        void outputStreamIsNotClosed() throws IOException {
            Rss rss = feedWithTitle(new PlainValue("t"));
            class TrackingStream extends ByteArrayOutputStream {
                boolean closed;

                @Override
                public void close() throws IOException {
                    closed = true;
                    super.close();
                }
            }
            TrackingStream out = new TrackingStream();

            RssOutput.output(rss, out);

            assertFalse(out.closed);
        }

        @Test
        @DisplayName("output(Rss, Writer) does not close the caller's writer")
        void writerIsNotClosed() throws IOException {
            Rss rss = feedWithTitle(new PlainValue("t"));
            class TrackingWriter extends StringWriter {
                boolean closed;

                @Override
                public void close() throws IOException {
                    closed = true;
                    super.close();
                }
            }
            TrackingWriter out = new TrackingWriter();

            RssOutput.output(rss, out);

            assertFalse(out.closed);
        }
    }

    // helpers

    /** The specification requires a channel to carry title, link and description. */
    private static Channel.Builder channel() {
        return Channel.builder()
                .title(new PlainValue("feed"))
                .link(new PlainValue("https://example.com/"))
                .description(new PlainValue("a feed"));
    }

    private static Rss feedWithTitle(final Value title) {
        return Rss.builder().channel(channel().title(title).build()).build();
    }

    private static Rss feedWithItems(final List<Item> items) {
        return Rss.builder().channel(channel().items(items).build()).build();
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
