package io.github.onurdemir55.resurrections.rss.feed;

import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.PlainValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What these print in a log line or a debugger.
 * <p>
 * The element records get a {@code toString} from being records; the three classes built through
 * a builder had none, so printing a channel gave {@code Channel@1b6d3586}. Effective Java asks
 * for one on every class and this is the case it has in mind: a value the caller assembled, that
 * they will want to look at when a feed is not what they expected.
 * <p>
 * These assertions check that the output identifies the object and stays short. They do not pin
 * the exact wording, which would make the format a contract for no benefit - {@code toString} is
 * for reading, and {@code RssOutput} is for output.
 */
class ToStringTest {

    @Test
    @DisplayName("a channel says what it is and how many items it carries")
    void channel() {
        Channel channel = channel(2);

        String text = channel.toString();

        assertAll(
                () -> assertTrue(text.startsWith("Channel["), text),
                () -> assertTrue(text.contains("a title"), text),
                () -> assertTrue(text.contains("https://example.com/"), text),
                () -> assertTrue(text.contains("items=2"), text),
                () -> assertTrue(oneLine(text), text));
    }

    @Test
    @DisplayName("an item is identified by its title")
    void itemWithTitle() {
        String text = Item.builder()
                .title(new PlainValue("a headline"))
                .link(new PlainValue("https://example.com/1"))
                .build()
                .toString();

        assertAll(
                () -> assertTrue(text.startsWith("Item["), text),
                () -> assertTrue(text.contains("a headline"), text),
                () -> assertTrue(oneLine(text), text));
    }

    /** An item may carry a description instead of a title, so that is what identifies it. */
    @Test
    @DisplayName("an item with no title falls back to its description")
    void itemWithoutTitle() {
        String text = Item.builder()
                .description(new CDATAValue("<p>the body</p>"))
                .build()
                .toString();

        assertAll(
                () -> assertTrue(text.contains("the body"), text),
                () -> assertFalse(text.contains("title="), text),
                () -> assertTrue(oneLine(text), text));
    }

    @Test
    @DisplayName("a document says its version and which namespaces it declares")
    void rss() {
        String text = Rss.builder()
                .namespace("dc", "http://purl.org/dc/elements/1.1/")
                .channel(channel(1))
                .build()
                .toString();

        assertAll(
                () -> assertTrue(text.startsWith("Rss["), text),
                () -> assertTrue(text.contains("version=2.0"), text),
                () -> assertTrue(text.contains("dc"), text),
                () -> assertFalse(text.contains("purl.org"),
                        () -> "the prefixes are enough; the URIs make the line unreadable: " + text),
                () -> assertTrue(oneLine(text), text));
    }

    @Test
    @DisplayName("and none of them is the feed, which is RssOutput's job")
    void notTheFeed() {
        String text = Rss.builder().channel(channel(1)).build().toString();

        assertAll(
                () -> assertFalse(text.contains("<?xml"), text),
                () -> assertFalse(text.contains("<rss"), text),
                () -> assertFalse(text.contains("<channel>"), text));
    }

    private static boolean oneLine(final String text) {
        return !text.contains("\n") && text.length() < 300;
    }

    /**
     * The items are passed in one call because a builder setter replaces rather than
     * accumulates, which is what a setter should do and what the varargs form here does.
     */
    private static Channel channel(final int items) {
        Item[] built = new Item[items];
        for (int i = 0; i < items; i++) {
            built[i] = Item.builder().title(new PlainValue("item " + i)).build();
        }
        return Channel.builder()
                .title(new PlainValue("a title"))
                .link(new PlainValue("https://example.com/"))
                .description(new PlainValue("a description"))
                .items(built)
                .build();
    }
}
