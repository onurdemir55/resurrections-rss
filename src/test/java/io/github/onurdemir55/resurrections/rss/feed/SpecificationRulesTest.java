package io.github.onurdemir55.resurrections.rss.feed;

import io.github.onurdemir55.resurrections.rss.feed.element.Enclosure;
import io.github.onurdemir55.resurrections.rss.feed.element.Image;
import io.github.onurdemir55.resurrections.rss.feed.element.Source;
import io.github.onurdemir55.resurrections.rss.feed.element.TextInput;
import io.github.onurdemir55.resurrections.rss.feed.holder.PlainValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The rules the RSS 2.0 specification states, enforced when a feed is built.
 * <p>
 * There is no lenient mode. A feed that breaks these rules is invalid, and finding that out
 * from an exception at build time is better than finding out from a reader that refuses the
 * feed.
 */
class SpecificationRulesTest {

    @Nested
    @DisplayName("required channel elements")
    class RequiredChannelElements {

        @Test
        @DisplayName("title, link and description are each required")
        void eachIsRequired() {
            assertAll(
                    () -> assertMissing("title", Channel.builder()
                            .link(new PlainValue("https://example.com/"))
                            .description(new PlainValue("d"))),
                    () -> assertMissing("link", Channel.builder()
                            .title(new PlainValue("t"))
                            .description(new PlainValue("d"))),
                    () -> assertMissing("description", Channel.builder()
                            .title(new PlainValue("t"))
                            .link(new PlainValue("https://example.com/"))));
        }

        @Test
        @DisplayName("a channel with all three builds")
        void allThreePresent() {
            assertDoesNotThrow(() -> Channel.builder()
                    .title(new PlainValue("t"))
                    .link(new PlainValue("https://example.com/"))
                    .description(new PlainValue("d"))
                    .build());
        }

        private void assertMissing(final String element, final Channel.Builder builder) {
            IllegalStateException thrown = assertThrows(IllegalStateException.class, builder::build);
            assertTrue(thrown.getMessage().contains(element),
                    () -> "the message should name " + element + ", was: " + thrown.getMessage());
        }
    }

    @Nested
    @DisplayName("item requires a title or a description")
    class ItemTitleOrDescription {

        @Test
        @DisplayName("an item with neither is rejected, even when other elements are set")
        void neitherIsRejected() {
            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                    () -> Item.builder()
                            .link(new PlainValue("https://example.com/item"))
                            .pubDate(new PlainValue("Sat, 07 Sep 2002 00:00:01 GMT"))
                            .build());

            assertTrue(thrown.getMessage().contains("title"), thrown::getMessage);
        }

        @Test
        @DisplayName("either one on its own is enough")
        void eitherIsEnough() {
            assertAll(
                    () -> assertDoesNotThrow(() -> Item.builder()
                            .title(new PlainValue("t")).build()),
                    () -> assertDoesNotThrow(() -> Item.builder()
                            .description(new PlainValue("d")).build()));
        }
    }

    @Nested
    @DisplayName("rss version")
    class Version {

        @Test
        @DisplayName("a version other than 2.0 is rejected")
        void otherVersionsRejected() {
            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                    () -> Rss.builder().version("1.0").channel(channel()).build());

            assertTrue(thrown.getMessage().contains(Rss.VERSION_2_0), thrown::getMessage);
        }

        @Test
        @DisplayName("a null version is rejected too, rather than omitting the attribute")
        void nullRejected() {
            assertThrows(IllegalStateException.class,
                    () -> Rss.builder().version(null).channel(channel()).build());
        }

        @Test
        @DisplayName("a document without a channel is rejected")
        void channelRequired() {
            assertThrows(IllegalStateException.class, () -> Rss.builder().build());
        }
    }

    @Nested
    @DisplayName("link and url elements need a URI scheme")
    class UriSchemes {

        @Test
        @DisplayName("a channel link without a scheme is rejected")
        void channelLink() {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> Channel.builder()
                            .title(new PlainValue("t"))
                            .link(new PlainValue("www.example.com"))
                            .description(new PlainValue("d"))
                            .build());

            assertTrue(thrown.getMessage().contains("scheme"), thrown::getMessage);
        }

        @Test
        @DisplayName("an item link without a scheme is rejected")
        void itemLink() {
            assertThrows(IllegalArgumentException.class, () -> Item.builder()
                    .title(new PlainValue("t"))
                    .link(new PlainValue("example.com/item"))
                    .build());
        }

        @Test
        @DisplayName("schemes other than http are accepted, since the specification allows them")
        void otherSchemesAccepted() {
            assertAll(
                    () -> assertDoesNotThrow(() -> channelWithLink("mailto:someone@example.com")),
                    () -> assertDoesNotThrow(() -> channelWithLink("ftp://files.example.com/")),
                    () -> assertDoesNotThrow(() -> channelWithLink("news://news.example.com/")),
                    () -> assertDoesNotThrow(() -> channelWithLink("gemini://example.com/")));
        }

        @Test
        @DisplayName("image url and link are checked")
        void imageUrls() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> Image.of("logo.png", "Logo", "https://example.com/")),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> Image.of("https://example.com/logo.png", "Logo", "example.com")));
        }

        @Test
        @DisplayName("a source url is checked")
        void sourceUrl() {
            assertThrows(IllegalArgumentException.class,
                    () -> Source.of("Realm", "www.tomalak.org/links2.xml"));
        }

        @Test
        @DisplayName("a textInput link is checked")
        void textInputLink() {
            assertThrows(IllegalArgumentException.class,
                    () -> TextInput.of("Submit", "Search", "q", "example.com/search"));
        }

        private static Channel channelWithLink(final String link) {
            return Channel.builder()
                    .title(new PlainValue("t"))
                    .link(new PlainValue(link))
                    .description(new PlainValue("d"))
                    .build();
        }
    }

    @Nested
    @DisplayName("enclosure")
    class EnclosureRules {

        @Test
        @DisplayName("the specification requires an http url")
        void httpRequired() {
            assertAll(
                    () -> assertDoesNotThrow(() -> Enclosure.of(
                            "http://example.com/a.mp3", 1L, "audio/mpeg")),
                    () -> assertDoesNotThrow(() -> Enclosure.of(
                            "https://example.com/a.mp3", 1L, "audio/mpeg")),
                    () -> assertThrows(IllegalArgumentException.class, () -> Enclosure.of(
                            "ftp://example.com/a.mp3", 1L, "audio/mpeg")),
                    () -> assertThrows(IllegalArgumentException.class, () -> Enclosure.of(
                            "example.com/a.mp3", 1L, "audio/mpeg")));
        }

        @Test
        @DisplayName("a negative length is rejected")
        void negativeLength() {
            assertThrows(IllegalArgumentException.class,
                    () -> Enclosure.of("http://example.com/a.mp3", -1L, "audio/mpeg"));
        }
    }

    private static Channel channel() {
        return Channel.builder()
                .title(new PlainValue("t"))
                .link(new PlainValue("https://example.com/"))
                .description(new PlainValue("d"))
                .build();
    }
}
