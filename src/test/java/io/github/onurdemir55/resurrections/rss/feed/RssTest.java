package io.github.onurdemir55.resurrections.rss.feed;

import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import io.github.onurdemir55.resurrections.rss.io.RssOutput;
import io.github.onurdemir55.resurrections.rss.util.DateParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

class RssTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void builder() {
        Assertions.assertDoesNotThrow(() -> {
            List<Item> items = Arrays.asList(createSampleItem1(),
                                             createSampleItem2());

            Channel channel = Channel.builder()
                    .items(items)
                    .language(new SimpleValue("en"))
                    .pubDate(new SimpleValue(DateParser.format_RFC1123_RFC822(Instant.now())))
                    .title(new CDATAValue("sample title"))
                    .description(new CDATAValue("sample description"))
                    .link(new SimpleValue("https://www.google.com/"))
                    .build();

            Rss rss = Rss.builder()
                    .version("2.0")
                    .channel(channel)
                    .build();

            String xmlOutput = RssOutput.outputString(rss);
            System.out.println(xmlOutput);
            return xmlOutput;
        });
    }

    private Item createSampleItem1() {
        Set<Value> set = new HashSet<>(Arrays.asList(new SimpleValue("category-1"),
                                                     new SimpleValue("category-2"),
                                                     new SimpleValue("category-3"),
                                                     new SimpleValue("category-3"),
                                                     new SimpleValue("category-3")));
        return Item.builder()
                .title(new SimpleValue("sample title"))
                .category(set)
                .description(new CDATAValue("sample description"))
                .link(new CDATAValue("https://www.google.com/"))
                .pubDate(new SimpleValue(DateParser.format_RFC1123_RFC822(new Date())))
                .build();
    }

    private Item createSampleItem2() {
        Set<Value> set = new HashSet<>(Arrays.asList(new CDATAValue("category-1"),
                                                     new CDATAValue("category-2"),
                                                     new CDATAValue("category-2")));
        return Item.builder()
                .title(new CDATAValue("sample title"))
                .category(set)
                .description(new CDATAValue("sample description"))
                .link(new CDATAValue("https://www.google.com/"))
                .pubDate(new CDATAValue(DateParser.format_RFC1123_RFC822(new Date())))
                .build();
    }
}