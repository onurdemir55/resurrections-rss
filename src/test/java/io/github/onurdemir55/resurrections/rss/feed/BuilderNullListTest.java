package io.github.onurdemir55.resurrections.rss.feed;

import io.github.onurdemir55.resurrections.rss.feed.element.Category;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.io.RssOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The repeatable elements accept {@code null} to mean "not set".
 * <p>
 * This is the one place the library is deliberately lenient about {@code null}, and only for
 * lists of optional elements: passing {@code null} for a list is a way of saying the caller
 * has nothing to add, which is different from the {@code null} text this library rejects
 * outright. Worth pinning, because the alternative reading is that these setters would throw.
 */
class BuilderNullListTest {

    @Test
    @DisplayName("a null category list leaves the element out of a channel")
    void channelCategory() {
        Channel channel = channelBuilder().category((List<Category>) null).build();

        assertAll(
                () -> assertNull(channel.getCategory()),
                () -> assertFalse(RssOutput.outputString(feed(channel)).contains("<category")));
    }

    @Test
    @DisplayName("a null item list leaves the items out of a channel")
    void channelItems() {
        Channel channel = channelBuilder().items((List<Item>) null).build();

        assertAll(
                () -> assertNull(channel.getItems()),
                () -> assertFalse(RssOutput.outputString(feed(channel)).contains("<item")));
    }

    @Test
    @DisplayName("a null category list leaves the element out of an item")
    void itemCategory() {
        Item item = Item.builder()
                .title(new SimpleValue("an item"))
                .category((List<Category>) null)
                .build();

        Channel channel = channelBuilder().items(item).build();

        assertAll(
                () -> assertNull(item.getCategory()),
                () -> assertFalse(RssOutput.outputString(feed(channel)).contains("<category")),
                () -> assertTrue(RssOutput.outputString(feed(channel)).contains("<item>")));
    }

    private static Channel.Builder channelBuilder() {
        return Channel.builder()
                .title(new SimpleValue("a title"))
                .link(new SimpleValue("https://example.com/"))
                .description(new SimpleValue("a description"));
    }

    private static Rss feed(final Channel channel) {
        return Rss.builder().channel(channel).build();
    }
}
