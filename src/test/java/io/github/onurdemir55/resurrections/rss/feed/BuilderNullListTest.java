package io.github.onurdemir55.resurrections.rss.feed;

import io.github.onurdemir55.resurrections.rss.feed.element.Category;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.io.RssOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The repeatable elements accept {@code null} to mean "not set", and never hand one back.
 * <p>
 * Passing {@code null} for a list says the caller has nothing to add, which is different from
 * the {@code null} text this library refuses outright. What comes back out, though, is an empty
 * list rather than a {@code null}, so a caller inspecting a feed does not have to guard every
 * read. Worth pinning at both ends: that the setter tolerates it, and that the getter does not
 * pass it on.
 */
class BuilderNullListTest {

    @Test
    @DisplayName("a null category list leaves the element out of a channel")
    void channelCategory() {
        Channel channel = channelBuilder().categories((List<Category>) null).build();

        assertAll(
                () -> assertTrue(channel.getCategories().isEmpty()),
                () -> assertFalse(RssOutput.outputString(feed(channel)).contains("<category")));
    }

    @Test
    @DisplayName("a null item list leaves the items out of a channel")
    void channelItems() {
        Channel channel = channelBuilder().items((List<Item>) null).build();

        assertAll(
                () -> assertTrue(channel.getItems().isEmpty()),
                () -> assertFalse(RssOutput.outputString(feed(channel)).contains("<item")));
    }

    @Test
    @DisplayName("a null category list leaves the element out of an item")
    void itemCategory() {
        Item item = Item.builder()
                .title(new SimpleValue("an item"))
                .categories((List<Category>) null)
                .build();

        Channel channel = channelBuilder().items(item).build();

        assertAll(
                () -> assertTrue(item.getCategories().isEmpty()),
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
