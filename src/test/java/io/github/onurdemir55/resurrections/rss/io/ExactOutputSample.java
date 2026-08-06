package io.github.onurdemir55.resurrections.rss.io;

import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Item;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.element.AtomLink;
import io.github.onurdemir55.resurrections.rss.feed.element.Category;
import io.github.onurdemir55.resurrections.rss.feed.element.Cloud;
import io.github.onurdemir55.resurrections.rss.feed.element.Day;
import io.github.onurdemir55.resurrections.rss.feed.element.Enclosure;
import io.github.onurdemir55.resurrections.rss.feed.element.Guid;
import io.github.onurdemir55.resurrections.rss.feed.element.Image;
import io.github.onurdemir55.resurrections.rss.feed.element.SkipDays;
import io.github.onurdemir55.resurrections.rss.feed.element.SkipHours;
import io.github.onurdemir55.resurrections.rss.feed.element.Source;
import io.github.onurdemir55.resurrections.rss.feed.element.TextInput;
import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;

/**
 * One feed that uses every element the model can hold.
 * <p>
 * Shared deliberately. {@link ExactOutputTest} pins what this produces down to the last space,
 * and {@link WriterCoversModelTest} checks that nothing in the model is missing from it. Both
 * depend on this being exhaustive, so there is one place to extend when an element is added,
 * and forgetting to extend it makes a test say so.
 */
final class ExactOutputSample {

    private ExactOutputSample() {
        // test fixture
    }

    static Rss feed() {
        Item item = Item.builder()
                .title(new CDATAValue("An <b>item</b>"))
                .link(new SimpleValue("https://example.com/items/1"))
                .description(new CDATAValue("<p>Body with a terminator ]]> inside.</p>"))
                .author(new SimpleValue("author@example.com (An Author)"))
                .category(Category.of("Newspapers"),
                        Category.of("MSFT", "http://www.fool.com/cusips"))
                .comments(new SimpleValue("https://example.com/items/1/comments"))
                .enclosure(Enclosure.of("https://example.com/media/e.mp3", 12216320L, "audio/mpeg"))
                .guid(Guid.of("https://example.com/items/1", true))
                .pubDate(new SimpleValue("Wed, 05 Aug 2026 20:00:00 GMT"))
                .source(Source.of("Origin Feed", "https://origin.example.com/feed.xml"))
                .extension("content:encoded", new CDATAValue("<p>rich <b>html</b></p>"))
                .extension("dc:creator", new SimpleValue("Onur Demir"))
                .build();

        Channel channel = Channel.builder()
                .title(new CDATAValue("Every <b>element</b>"))
                .link(new SimpleValue("https://example.com/"))
                .description(new CDATAValue("Exercises every element."))
                .atomLink(AtomLink.self("https://example.com/feed.xml"))
                .language(new SimpleValue("en-us"))
                .copyright(new SimpleValue("Copyright 2026, Onur Demir"))
                .managingEditor(new SimpleValue("editor@example.com (An Editor)"))
                .webMaster(new SimpleValue("webmaster@example.com (A Webmaster)"))
                .pubDate(new SimpleValue("Wed, 05 Aug 2026 20:00:00 GMT"))
                .lastBuildDate(new SimpleValue("Wed, 05 Aug 2026 20:00:00 GMT"))
                .category(Category.of("Technology"), Category.of("1765", "Syndic8"))
                .generator(new SimpleValue("resurrections-rss"))
                .docs(new SimpleValue("https://www.rssboard.org/rss-specification"))
                .cloud(Cloud.of("rpc.example.com", 80, "/RPC2", "myCloud.rssPleaseNotify",
                        "xml-rpc"))
                .ttl(60)
                .image(Image.of("https://example.com/logo.png", "Every element",
                                "https://example.com/")
                        .withSize(144, 400)
                        .withDescription(new CDATAValue("The <i>logo</i>")))
                .rating(new SimpleValue("(PICS-1.1)"))
                .textInput(TextInput.of("Search", "Search the archive", "q",
                        "https://example.com/search"))
                .skipHours(SkipHours.of(0, 23))
                .skipDays(SkipDays.of(Day.SATURDAY, Day.SUNDAY))
                .extension("dc:language", new SimpleValue("en"))
                .items(item)
                .build();

        return Rss.builder()
                .namespace("content", "http://purl.org/rss/1.0/modules/content/")
                .namespace("dc", "http://purl.org/dc/elements/1.1/")
                .channel(channel)
                .build();
    }
}
