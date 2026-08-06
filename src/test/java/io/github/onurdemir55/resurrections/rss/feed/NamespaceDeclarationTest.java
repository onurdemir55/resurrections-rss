package io.github.onurdemir55.resurrections.rss.feed;

import io.github.onurdemir55.resurrections.rss.feed.element.AtomLink;
import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.io.RssOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An extension element must have a declared namespace prefix.
 * <p>
 * This is the failure mode that hurts most, because nothing complains at the time. A prefixed
 * element with no matching declaration is not well-formed XML, so the feed is built without
 * protest, written without protest, and then refused by the first reader that receives it —
 * by which point the code that caused it is long out of sight. Rejecting it at
 * {@code build()} is the only place the prefix and the declarations are both known.
 */
class NamespaceDeclarationTest {

    @Nested
    @DisplayName("Rejected while building")
    class Rejected {

        @Test
        @DisplayName("an item extension whose prefix was never declared")
        void undeclaredOnItem() {
            Channel channel = channel()
                    .items(Item.builder()
                            .title(new SimpleValue("an item"))
                            .extension("dc:creator", new SimpleValue("Onur Demir"))
                            .build())
                    .build();

            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                    () -> Rss.builder().channel(channel).build());

            assertAllOf(thrown.getMessage(), "dc:creator", "an item", "\"dc\"", "namespace(\"dc\"");
        }

        @Test
        @DisplayName("a channel extension whose prefix was never declared")
        void undeclaredOnChannel() {
            Channel channel = channel()
                    .extension("dc:language", new SimpleValue("tr"))
                    .build();

            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                    () -> Rss.builder().channel(channel).build());

            assertAllOf(thrown.getMessage(), "dc:language", "the channel");
        }

        @Test
        @DisplayName("a declaration for a different prefix does not help")
        void wrongPrefixDeclared() {
            Channel channel = channel()
                    .extension("dc:language", new SimpleValue("tr"))
                    .build();

            assertThrows(IllegalStateException.class, () -> Rss.builder()
                    .namespace("content", "http://purl.org/rss/1.0/modules/content/")
                    .channel(channel)
                    .build());
        }

        @Test
        @DisplayName("an extension with no prefix at all, which the specification disallows")
        void noPrefix() {
            Channel channel = channel()
                    .extension("mine", new SimpleValue("v"))
                    .build();

            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                    () -> Rss.builder().channel(channel).build());

            assertAllOf(thrown.getMessage(), "mine", "namespace");
        }

        @Test
        @DisplayName("a prefix XML reserves for itself")
        void reservedPrefix() {
            assertThrows(IllegalArgumentException.class,
                    () -> Rss.builder().namespace("xmlns", "http://example.com/ns"));
            assertThrows(IllegalArgumentException.class,
                    () -> Rss.builder().namespace("xml", "http://example.com/ns"));
        }
    }

    @Nested
    @DisplayName("Accepted")
    class Accepted {

        @Test
        @DisplayName("a declared prefix, on the channel and on an item")
        void declared() {
            Channel channel = channel()
                    .extension("dc:language", new SimpleValue("tr"))
                    .items(Item.builder()
                            .title(new SimpleValue("an item"))
                            .extension("content:encoded", new CDATAValue("<p>html</p>"))
                            .build())
                    .build();

            Rss rss = assertDoesNotThrow(() -> Rss.builder()
                    .namespace("dc", "http://purl.org/dc/elements/1.1/")
                    .namespace("content", "http://purl.org/rss/1.0/modules/content/")
                    .channel(channel)
                    .build());

            String xml = RssOutput.outputString(rss);
            assertAllOf(xml, "xmlns:dc=", "xmlns:content=", "<dc:language>", "<content:encoded>");
        }

        @Test
        @DisplayName("atom:link, whose declaration this library adds for you")
        void atomIsDeclaredAutomatically() {
            Channel channel = channel()
                    .atomLink(AtomLink.self("https://example.com/feed.xml"))
                    .build();

            Rss rss = assertDoesNotThrow(() -> Rss.builder().channel(channel).build());

            assertTrue(RssOutput.outputString(rss).contains("xmlns:atom="));
        }

        @Test
        @DisplayName("a feed with no extensions at all")
        void noExtensions() {
            assertDoesNotThrow(() -> Rss.builder().channel(channel().build()).build());
        }
    }

    /**
     * Building must not change the builder. The Atom namespace is added because the channel
     * uses an Atom element, and if that were written into the builder's own declarations it
     * would still be there for the next feed built from it — declared on a document with
     * nothing to declare it for.
     */
    @Nested
    @DisplayName("Building leaves the builder alone")
    class BuilderIsNotMutated {

        @Test
        @DisplayName("the automatic Atom declaration does not carry over to a second feed")
        void atomDoesNotLeakIntoTheNextFeed() {
            Rss.Builder builder = Rss.builder();

            String withAtom = RssOutput.outputString(builder
                    .channel(channel().atomLink(AtomLink.self("https://example.com/f.xml")).build())
                    .build());
            String withoutAtom = RssOutput.outputString(builder
                    .channel(channel().build())
                    .build());

            assertAllOf(withAtom, "xmlns:atom=");
            assertTrue(!withoutAtom.contains("xmlns:atom="),
                    () -> "the second feed uses no Atom element, so it should declare no Atom "
                            + "namespace:\n" + withoutAtom);
        }

        @Test
        @DisplayName("and the declared namespaces of the first feed are unaffected")
        void firstFeedKeepsItsDeclarations() {
            Rss.Builder builder = Rss.builder();

            Rss first = builder
                    .channel(channel().atomLink(AtomLink.self("https://example.com/f.xml")).build())
                    .build();
            builder.channel(channel().build()).build();

            assertEquals(Map.of(AtomLink.PREFIX, AtomLink.NAMESPACE), first.getNamespaces());
        }

        @Test
        @DisplayName("building the same builder twice produces the same document")
        void repeatableBuild() {
            Rss.Builder builder = Rss.builder()
                    .namespace("dc", "http://purl.org/dc/elements/1.1/")
                    .channel(channel().extension("dc:language", new SimpleValue("tr")).build());

            assertEquals(RssOutput.outputString(builder.build()),
                    RssOutput.outputString(builder.build()));
        }
    }

    private static Channel.Builder channel() {
        return Channel.builder()
                .title(new SimpleValue("a title"))
                .link(new SimpleValue("https://example.com/"))
                .description(new SimpleValue("a description"));
    }

    private static void assertAllOf(final String actual, final String... expected) {
        for (String fragment : expected) {
            assertTrue(actual.contains(fragment),
                    () -> "expected \"" + fragment + "\" in: " + actual);
        }
    }
}
