package io.github.onurdemir55.resurrections.rss.io;

import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.PlainValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * What the caller puts in is what a reader gets back.
 * <p>
 * These tests parse the feed again and compare the text a reader would see against the text
 * that was set, which is a different question from what the document looks like. An audit
 * found the two coming apart: a carriage return inside a CDATA section was lost, because XML
 * normalizes line endings before anything else sees them and a CDATA section cannot escape
 * its way out of that. Nothing pinned the round trip, so nothing noticed. The most ordinary
 * input there is, HTML written on Windows, was affected.
 */
class CdataFidelityTest {

    private static final DocumentBuilderFactory PARSER = namespaceAwareParser();

    @Test
    @DisplayName("a carriage return survives a CDATA section")
    void carriageReturnInCdata() {
        assertRoundTrips("a\rb");
        assertRoundTrips("a\r\nb");
        assertRoundTrips("\r");
        assertRoundTrips("\r\r");
        assertRoundTrips("lead\r");
        assertRoundTrips("\rtrail");
    }

    @Test
    @DisplayName("Windows line endings in markup survive, which is why this matters")
    void windowsLineEndings() {
        assertRoundTrips("<p>first</p>\r\n<p>second</p>\r\n");
    }

    @Test
    @DisplayName("the other line endings and whitespace are untouched")
    void otherWhitespace() {
        assertRoundTrips("a\nb");
        assertRoundTrips("a\tb");
        assertRoundTrips("  spaced  ");
        assertRoundTrips("");
    }

    @Test
    @DisplayName("a carriage return next to the CDATA terminator, since both split the section")
    void carriageReturnBesideTerminator() {
        assertRoundTrips("a]]>\rb");
        assertRoundTrips("a\r]]>b");
        assertRoundTrips("]]>\r]]>");
    }

    @Test
    @DisplayName("markup and entities still survive alongside a carriage return")
    void markupSurvives() {
        assertRoundTrips("<b>a &amp; b</b>\r\n<i>c</i>");
        assertRoundTrips("ünïcödé ığşç 📡\r\nnext");
    }

    @Test
    @DisplayName("and plain text keeps its carriage returns too, as it always did")
    void plainTextUnchanged() {
        assertEquals("a\r\nb", readBack(new PlainValue("a\r\nb")));
        assertEquals("a\rb", readBack(new PlainValue("a\rb")));
    }

    /**
     * Text where the whitespace is the content: an aligned table, a {@code <pre>} block, an
     * indented listing. A feed of market data or release notes carries these, and if a column
     * moves by one space the value is wrong on the page even though nothing threw.
     * <p>
     * Both forms are checked. The plain form has to escape and unescape without touching the
     * layout, and the CDATA form has to leave it alone entirely, including the line endings -
     * which is the case that used to fail.
     */
    @Nested
    @DisplayName("Text whose whitespace carries meaning")
    class Preformatted {

        private static final String TABLE = """
                Hisse    Fiyat    Değişim
                -------  -------  -------
                ODH       124,50    +1,8%
                XYZ        88,20    -0,4%
                """;

        @Test
        @DisplayName("an aligned table survives, in both forms")
        void alignedTable() {
            assertEquals(TABLE, readBack(new CDATAValue(TABLE)));
            assertEquals(TABLE, readBack(new PlainValue(TABLE)));
        }

        @Test
        @DisplayName("the same table written on Windows survives too")
        void alignedTableWithCrLf() {
            String windows = TABLE.replace("\n", "\r\n");

            assertEquals(windows, readBack(new CDATAValue(windows)));
            assertEquals(windows, readBack(new PlainValue(windows)));
        }

        @Test
        @DisplayName("tabs are not turned into spaces, or into anything else")
        void tabs() {
            String tabbed = "Hisse\tFiyat\tDeğişim\nODH\t124,50\t+1,8%\n";

            assertEquals(tabbed, readBack(new CDATAValue(tabbed)));
            assertEquals(tabbed, readBack(new PlainValue(tabbed)));
        }

        @Test
        @DisplayName("a pre block keeps both its tags and its layout")
        void preBlock() {
            String pre = "<pre>\n" + TABLE + "</pre>";

            assertEquals(pre, readBack(new CDATAValue(pre)));
        }

        @Test
        @DisplayName("leading and trailing whitespace is not trimmed away")
        void edgesAreKept() {
            assertEquals("    indented", readBack(new CDATAValue("    indented")));
            assertEquals("trailing    ", readBack(new CDATAValue("trailing    ")));
            assertEquals("\nstarts on line two", readBack(new CDATAValue("\nstarts on line two")));
            assertEquals("     ", readBack(new CDATAValue("     ")));
        }

        @Test
        @DisplayName("runs of spaces are not collapsed, which is what alignment depends on")
        void runsOfSpaces() {
            assertEquals("a     b     c", readBack(new CDATAValue("a     b     c")));
            assertEquals("a     b     c", readBack(new PlainValue("a     b     c")));
        }
    }

    /** Text with no carriage return must still be written as one unbroken section. */
    @Test
    @DisplayName("text without a carriage return is still a single CDATA section")
    void singleSectionWhenNothingToSplit() {
        String xml = feedXml(new CDATAValue("plain markup <b>here</b>"));

        assertEquals(1, countOccurrences(xml, "<![CDATA["),
                () -> "expected one section:\n" + xml);
    }

    private static void assertRoundTrips(final String text) {
        assertEquals(text, readBack(new CDATAValue(text)),
                () -> "CDATA did not survive: [" + visible(text) + "]");
    }

    private static String readBack(final Value value) {
        String xml = feedXml(value);
        try {
            Document document = PARSER.newDocumentBuilder().parse(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            return document.getElementsByTagName("title").item(0).getTextContent();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AssertionError("the feed could not be parsed:\n" + xml, e);
        }
    }

    private static String feedXml(final Value title) {
        return RssOutput.outputString(Rss.builder()
                .channel(Channel.builder()
                        .title(title)
                        .link(new PlainValue("https://example.com/"))
                        .description(new PlainValue("a description"))
                        .build())
                .build());
    }

    private static int countOccurrences(final String text, final String fragment) {
        int count = 0;
        for (int i = text.indexOf(fragment); i >= 0; i = text.indexOf(fragment, i + 1)) {
            count++;
        }
        return count;
    }

    private static String visible(final String text) {
        return text.replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t");
    }

    private static DocumentBuilderFactory namespaceAwareParser() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory;
    }
}
