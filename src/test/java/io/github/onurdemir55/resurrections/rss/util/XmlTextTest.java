package io.github.onurdemir55.resurrections.rss.util;

import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.PlainValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link XmlText} rejects text XML cannot represent.
 * <p>
 * What it accepts matters as much as what it rejects. A check like this is easy to write too
 * strictly, and text going into a feed is real-world text: emoji, accents and non-Latin
 * scripts all have to survive it.
 */
class XmlTextTest {

    @Nested
    @DisplayName("Accepted")
    class Accepted {

        @Test
        @DisplayName("ordinary text, and empty text")
        void ordinary() {
            assertEquals("hello", XmlText.requireWritable("hello"));
            assertEquals("", XmlText.requireWritable(""));
        }

        @Test
        @DisplayName("tab, newline and carriage return, the three controls XML permits")
        void permittedWhitespace() {
            assertEquals("a\tb\nc\rd", XmlText.requireWritable("a\tb\nc\rd"));
        }

        @Test
        @DisplayName("non-Latin scripts and accents")
        void nonAscii() {
            assertEquals("ığşçöü ĞŞÇÖÜ", XmlText.requireWritable("ığşçöü ĞŞÇÖÜ"));
            assertEquals("日本語", XmlText.requireWritable("日本語"));
        }

        @Test
        @DisplayName("an emoji, which is a surrogate pair and must not be mistaken for one half")
        void emoji() {
            assertEquals("feed 📡", XmlText.requireWritable("feed 📡"));
        }

        @Test
        @DisplayName("markup, which is a matter for escaping rather than legality")
        void markup() {
            assertEquals("<b>a & b</b>", XmlText.requireWritable("<b>a & b</b>"));
        }
    }

    @Nested
    @DisplayName("Rejected")
    class Rejected {

        @Test
        @DisplayName("a NUL byte, naming the code point and where it was")
        void nul() {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> XmlText.requireWritable("a\u0000b"));

            assertAllOf(thrown.getMessage(), "U+0000", "index 1");
        }

        @Test
        @DisplayName("other C0 control characters")
        void otherControls() {
            assertThrows(IllegalArgumentException.class, () -> XmlText.requireWritable("\u0001"));
            assertThrows(IllegalArgumentException.class, () -> XmlText.requireWritable("\u000B"));
            assertThrows(IllegalArgumentException.class, () -> XmlText.requireWritable("\u001F"));
        }

        @Test
        @DisplayName("an unpaired surrogate, which is not a character at all")
        void unpairedSurrogate() {
            assertThrows(IllegalArgumentException.class, () -> XmlText.requireWritable("a\uD83Db"));
        }

        @Test
        @DisplayName("the two non-characters at the end of the basic plane")
        void nonCharacters() {
            assertThrows(IllegalArgumentException.class, () -> XmlText.requireWritable("\uFFFE"));
            assertThrows(IllegalArgumentException.class, () -> XmlText.requireWritable("\uFFFF"));
        }
    }

    @Nested
    @DisplayName("Enforced where values are built, not where they are written")
    class EnforcedAtConstruction {

        @Test
        @DisplayName("PlainValue rejects it")
        void simpleValue() {
            assertThrows(IllegalArgumentException.class, () -> new PlainValue("a\u0000b"));
        }

        @Test
        @DisplayName("CDATAValue rejects it too, since a CDATA section cannot encode it either")
        void cdataValue() {
            assertThrows(IllegalArgumentException.class, () -> new CDATAValue("a\u0000b"));
        }

        @Test
        @DisplayName("and both still accept everything a feed legitimately carries")
        void bothAcceptRealText() {
            assertEquals("<p>iyi 📡</p>", new CDATAValue("<p>iyi 📡</p>").value());
            assertEquals("a & b", new PlainValue("a & b").value());
        }
    }

    private static void assertAllOf(final String actual, final String... expected) {
        for (String fragment : expected) {
            assertTrue(actual.contains(fragment),
                    () -> "expected \"" + fragment + "\" in: " + actual);
        }
    }
}
