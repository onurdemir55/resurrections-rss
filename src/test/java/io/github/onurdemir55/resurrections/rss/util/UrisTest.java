package io.github.onurdemir55.resurrections.rss.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * What counts as a URI scheme.
 * <p>
 * A scheme is a letter followed by letters, digits, {@code +}, {@code -} or {@code .},
 * ending at the first colon. The check exists to catch a missing scheme, not to police the
 * IANA register, so anything shaped like a scheme is accepted.
 */
class UrisTest {

    @Test
    @DisplayName("values shaped like a scheme are accepted")
    void accepted() {
        assertAll(
                () -> assertDoesNotThrow(() -> Uris.requireScheme("link", "http://example.com")),
                () -> assertDoesNotThrow(() -> Uris.requireScheme("link", "https://example.com")),
                () -> assertDoesNotThrow(() -> Uris.requireScheme("link", "mailto:a@example.com")),
                () -> assertDoesNotThrow(() -> Uris.requireScheme("link", "news://example.com")),
                () -> assertDoesNotThrow(() -> Uris.requireScheme("link", "x-custom-1.0:opaque")),
                () -> assertDoesNotThrow(() -> Uris.requireScheme("link", "HTTP://EXAMPLE.COM")));
    }

    @Test
    @DisplayName("the value is returned unchanged, so it can be used inline")
    void returnsValue() {
        assertEquals("https://example.com", Uris.requireScheme("link", "https://example.com"));
        assertEquals("http://example.com", Uris.requireHttpScheme("url", "http://example.com"));
    }

    @Test
    @DisplayName("a missing or malformed scheme is rejected")
    void rejected() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Uris.requireScheme("link", "example.com")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Uris.requireScheme("link", ":no-scheme")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Uris.requireScheme("link", "1http://example.com")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Uris.requireScheme("link", "ht tp://example.com")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Uris.requireScheme("link", "ht_tp://example.com")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Uris.requireScheme("link", "")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Uris.requireScheme("link", null)));
    }

    @Test
    @DisplayName("the http check accepts only http and https, in any case")
    void httpOnly() {
        assertAll(
                () -> assertDoesNotThrow(() -> Uris.requireHttpScheme("url", "HTTPS://a.example")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Uris.requireHttpScheme("url", "ftp://a.example/f.mp3")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Uris.requireHttpScheme("url", "a.example/f.mp3")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Uris.requireHttpScheme("url", null)));
    }

    @Test
    @DisplayName("the error message names the element and shows the value")
    void message() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> Uris.requireScheme("channel link", "example.com"));

        assertAll(
                () -> assertEquals(true, thrown.getMessage().contains("channel link")),
                () -> assertEquals(true, thrown.getMessage().contains("example.com")));
    }

    /**
     * RFC 3986 spells a scheme with ASCII letters. The check used to ask
     * {@link Character#isLetter}, which is a Unicode category test, and so accepted
     * {@code ürl://} and {@code ℓink://} - the same confusion that let five characters into
     * element names.
     */
    @Nested
    @DisplayName("A scheme is spelled in ASCII")
    class SchemeIsAscii {

        @Test
        @DisplayName("a scheme with a letter from another script is refused")
        void nonAsciiScheme() {
            assertThrows(IllegalArgumentException.class,
                    () -> Uris.requireScheme("test", "\u00FCrl://example.com/"));
            assertThrows(IllegalArgumentException.class,
                    () -> Uris.requireScheme("test", "\u2113ink://example.com/"));
        }

        @Test
        @DisplayName("and the shapes RFC 3986 allows still pass")
        void permittedSchemes() {
            for (String uri : new String[] {
                "http://example.com/", "https://example.com/", "mailto:a@example.com",
                "urn:isbn:0451450523", "x+y-1.2://example.com/", "HTTP://example.com/"}) {
                assertEquals(uri, Uris.requireScheme("test", uri));
            }
        }
    }
}
