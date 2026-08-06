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
        @DisplayName("a letter outside ASCII, which XML allows")
        void nonAscii() {
            assertEquals("başlık", XmlNames.requireElementName("başlık", "a name"));
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
         * XML does allow letters above the basic plane in a name, so this is stricter than the
         * specification. It is pinned rather than fixed because the strictness errs the safe
         * way — a rejected name cannot corrupt a document, an accepted bad one can — and
         * because a namespace prefix outside the basic plane does not occur in practice. If
         * that ever changes, the check needs to walk code points rather than chars.
         */
        @Test
        @DisplayName("a letter above the basic plane, which this rejects though XML allows it")
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
