package io.github.onurdemir55.resurrections.rss.feed.element;

import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Attribute values are checked for characters XML cannot represent, the same as element text.
 * <p>
 * Element text was checked first because that is where a caller puts arbitrary content, but an
 * attribute is written into the same document and breaks it just as thoroughly. Leaving these
 * unchecked meant a stray control character in a URL still produced a failure part-way through
 * writing, on a stream that had already received the first half of the feed.
 */
class AttributeValidationTest {

    private static final String NUL = "\u0000";

    @Test
    @DisplayName("a url attribute")
    void urls() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Source.of("Origin", "https://example.com/" + NUL)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Enclosure.of("https://example.com/a" + NUL, 1L, "audio/mpeg")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> AtomLink.self("https://example.com/feed.xml" + NUL)));
    }

    @Test
    @DisplayName("a non-url attribute")
    void otherAttributes() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Enclosure.of("https://example.com/a.mp3", 1L, "audio/mpeg" + NUL)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Category.of("Tech", "Syndic8" + NUL)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> AtomLink.of("https://example.com/", "self" + NUL)));
    }

    @Test
    @DisplayName("every cloud attribute, since all five are written")
    void cloudAttributes() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Cloud.of("rpc.example.com" + NUL, 80, "/RPC2", "notify", "xml-rpc")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Cloud.of("rpc.example.com", 80, "/RPC2" + NUL, "notify", "xml-rpc")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Cloud.of("rpc.example.com", 80, "/RPC2", "notify" + NUL, "xml-rpc")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Cloud.of("rpc.example.com", 80, "/RPC2", "notify", "xml-rpc" + NUL)));
    }

    @Test
    @DisplayName("the error message names the attribute, so the cause is not a guess")
    void messageNamesTheAttribute() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> Cloud.of("rpc.example.com", 80, "/RPC2", "notify" + NUL, "xml-rpc"));

        assertTrue(thrown.getMessage().contains("cloud registerProcedure"), thrown::getMessage);
    }

    @Test
    @DisplayName("and ordinary attribute values, including an omitted optional one, still pass")
    void validAttributesStillPass() {
        assertAll(
                () -> assertDoesNotThrow(() -> Source.of("Origin", "https://example.com/feed.xml")),
                () -> assertDoesNotThrow(() -> Category.of("Tech", "Syndic8")),
                () -> assertDoesNotThrow(() -> Category.of("Tech")),
                () -> assertDoesNotThrow(() -> AtomLink.of("https://example.com/", "alternate")),
                () -> assertDoesNotThrow(() -> new Category(new SimpleValue("Tech"), null)),
                () -> assertDoesNotThrow(
                        () -> Cloud.of("rpc.example.com", 80, "/RPC2", "notify", "xml-rpc")));
    }
}
