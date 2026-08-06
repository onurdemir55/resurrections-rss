package io.github.onurdemir55.resurrections.rss.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link XmlNames} decides whether a caller-supplied name may become an element name.
 * <p>
 * This matters more than it looks. Element text is escaped on the way out, so bad text is
 * merely ugly; an element name is written as markup, so a bad name produces a document no
 * parser will accept, and a name assembled from untrusted input could inject markup of its
 * own choosing.
 */
class XmlNamesTest {

    @Nested
    @DisplayName("Accepted")
    class Accepted {

        @Test
        @DisplayName("a plain name")
        void plain() {
            assertEquals("title", XmlNames.requireElementName("title", "a name"));
        }

        @Test
        @DisplayName("a prefixed name, which is what extensions use")
        void prefixed() {
            assertEquals("content:encoded",
                    XmlNames.requireElementName("content:encoded", "a name"));
        }

        @Test
        @DisplayName("underscores, hyphens, dots and digits after the first character")
        void permittedCharacters() {
            assertEquals("_a-b.c9", XmlNames.requireElementName("_a-b.c9", "a name"));
        }

        @Test
        @DisplayName("the prefixes of every extension module in real use")
        void realWorldModules() {
            for (String name : new String[] {
                "dc:creator", "content:encoded", "itunes:author", "media:content",
                "slash:comments", "sy:updatePeriod", "georss:point", "wfw:commentRss",
                "admin:generatorAgent", "atom:link"}) {
                assertEquals(name, XmlNames.requireElementName(name, "a name"));
            }
        }
    }

    /**
     * Names are held to ASCII, which is narrower than XML allows, and the reason is worth
     * keeping written down.
     * <p>
     * The check used to ask {@link Character#isLetter}, which is a Unicode category test and
     * not the production XML gives. Five characters were found that it accepted and that
     * {@code DocumentBuilder} then refused, so the library was writing feeds a reader would
     * drop. Two of them are not legal in any edition of XML; the other three the fifth edition
     * added and the parser in the JDK, which implements the fourth, still rejects.
     * <p>
     * Matching the fifth edition exactly would have kept that last group emittable and
     * unreadable. ASCII is the subset everything agrees on, and it costs nothing real: every
     * published extension module spells its names in ASCII, as the test above shows.
     */
    @Nested
    @DisplayName("Rejected because a reader would refuse them")
    class RejectedForInteroperability {

        @Test
        @DisplayName("characters no edition of XML allows in a name")
        void neverLegal() {
            // U+00AA FEMININE ORDINAL INDICATOR and U+00B5 MICRO SIGN are letters to Unicode
            // but fall below the range where XML's own list starts.
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("\u00AAname", "a name"));
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("\u00B5name", "a name"));
        }

        @Test
        @DisplayName("characters the fifth edition added that the JDK's parser still refuses")
        void legalButUnreadable() {
            // U+02B0 MODIFIER LETTER SMALL H, U+2113 SCRIPT SMALL L, U+FF10 FULLWIDTH ZERO.
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("\u02B0name", "a name"));
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("name\u2113", "a name"));
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("name\uFF10", "a name"));
        }

        @Test
        @DisplayName("a letter from another script, refused for the same reason")
        void nonAscii() {
            for (String name : new String[] {"ba\u015Fl\u0131k", "\u03B1lpha", "\u4E2D", "\u0430"}) {
                assertThrows(IllegalArgumentException.class,
                        () -> XmlNames.requireElementName(name, "a name"));
            }
        }

        @Test
        @DisplayName("and the message names the character, so the cause is not a guess")
        void messageNamesTheCharacter() {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("\u00AAname", "an extension name"));

            assertTrue(thrown.getMessage().contains("U+00AA"), thrown::getMessage);
            assertTrue(thrown.getMessage().contains("an extension name"), thrown::getMessage);
        }
    }

    @Nested
    @DisplayName("Rejected")
    class Rejected {

        @Test
        @DisplayName("null")
        void nullName() {
            assertThrows(NullPointerException.class,
                    () -> XmlNames.requireElementName(null, "a name"));
        }

        @Test
        @DisplayName("empty, and an empty prefix or local part")
        void empty() {
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("", "a name"));
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName(":encoded", "a name"));
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("content:", "a name"));
        }

        @Test
        @DisplayName("a space, which would end the element name and start an attribute")
        void space() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("bad name", "an extension name"));
            assertTrue(e.getMessage().contains("an extension name"), e::getMessage);
        }

        @Test
        @DisplayName("markup, which is the case that would otherwise corrupt the document")
        void markup() {
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("<evil>", "a name"));
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("a\"b", "a name"));
        }

        @Test
        @DisplayName("a leading digit or hyphen")
        void badStart() {
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("1leading", "a name"));
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("-leading", "a name"));
        }

        @Test
        @DisplayName("more than one prefix")
        void twoColons() {
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("a:b:c", "a name"));
        }

        /**
         * Covered by the ASCII rule now, but kept as its own case because it used to be the
         * only one of these the old check got right, and for a reason that no longer applies:
         * it walked chars rather than code points, so it saw two surrogate halves, neither of
         * which is a letter.
         */
        @Test
        @DisplayName("a character above the basic multilingual plane")
        void supplementaryCharacterIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("\uD840\uDC0Bname", "a name"));
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireElementName("name\uD840\uDC0B", "a name"));
        }
    }

    @Nested
    @DisplayName("A namespace prefix is a name with two extra rules")
    class NamespacePrefixes {

        @Test
        @DisplayName("an ordinary prefix passes")
        void ordinary() {
            assertEquals("content", XmlNames.requireNamespacePrefix("content"));
        }

        @Test
        @DisplayName("a prefix cannot itself be prefixed")
        void noColon() {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireNamespacePrefix("a:b"));

            assertTrue(thrown.getMessage().contains("colon"), thrown::getMessage);
        }

        @Test
        @DisplayName("xml and xmlns are reserved, and declaring either produces illegal XML")
        void reserved() {
            for (String reserved : new String[] {"xml", "xmlns"}) {
                IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                        () -> XmlNames.requireNamespacePrefix(reserved));
                assertTrue(thrown.getMessage().contains("reserved"), thrown::getMessage);
            }
        }

        @Test
        @DisplayName("and the name rules still apply")
        void nameRulesStillApply() {
            assertThrows(IllegalArgumentException.class,
                    () -> XmlNames.requireNamespacePrefix("1bad"));
            assertThrows(NullPointerException.class,
                    () -> XmlNames.requireNamespacePrefix(null));
        }
    }
}
