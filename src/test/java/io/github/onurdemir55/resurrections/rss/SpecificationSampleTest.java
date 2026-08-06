package io.github.onurdemir55.resurrections.rss;

import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Item;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.element.AtomLink;
import io.github.onurdemir55.resurrections.rss.feed.element.Enclosure;
import io.github.onurdemir55.resurrections.rss.feed.element.Guid;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.io.RssOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Rebuilds the sample feed the specification publishes at
 * <a href="https://www.rssboard.org/files/sample-rss-2.xml">sample-rss-2.xml</a>.
 * <p>
 * The comparison is on content rather than bytes. RSS places no ordering constraint on the
 * children of a channel, and the published sample happens to order them differently from
 * this library, so a byte comparison would assert something the specification does not
 * require. What matters is that every element and value in the sample can be expressed, and
 * that the awkward parts of it survive: an item with no title, and an enclosure.
 */
class SpecificationSampleTest {

    private static final String FEED_URL = "https://www.rssboard.org/files/sample-rss-2.xml";

    @Test
    @DisplayName("every element and value of the published sample can be expressed")
    void reproducesTheSample() {
        String xml = RssOutput.outputString(sample());

        assertAll(
                () -> assertTrue(xml.contains("<title>NASA Space Station News</title>"), () -> xml),
                () -> assertTrue(xml.contains("<link>http://www.nasa.gov/</link>"), () -> xml),
                () -> assertTrue(xml.contains("<language>en-us</language>"), () -> xml),
                () -> assertTrue(xml.contains("<pubDate>Tue, 10 Jun 2003 04:00:00 GMT</pubDate>"),
                        () -> xml),
                () -> assertTrue(xml.contains("<lastBuildDate>Fri, 21 Jul 2023 09:04 EDT</lastBuildDate>"),
                        () -> xml),
                () -> assertTrue(xml.contains("<docs>https://www.rssboard.org/rss-specification</docs>"),
                        () -> xml),
                () -> assertTrue(xml.contains("<generator>Blosxom 2.1.2</generator>"), () -> xml),
                () -> assertTrue(xml.contains(
                        "<managingEditor>neil.armstrong@example.com (Neil Armstrong)</managingEditor>"),
                        () -> xml),
                () -> assertTrue(xml.contains(
                        "<webMaster>sally.ride@example.com (Sally Ride)</webMaster>"), () -> xml),
                () -> assertTrue(xml.contains("<atom:link href=\"" + FEED_URL
                        + "\" rel=\"self\" type=\"application/rss+xml\"/>"), () -> xml),
                () -> assertTrue(xml.contains("xmlns:atom=\"" + AtomLink.NAMESPACE + "\""), () -> xml));
    }

    @Test
    @DisplayName("the sample has five items, one of which has no title")
    void itemsMatch() {
        String xml = RssOutput.outputString(sample());

        assertAll(
                () -> assertEquals(5, countOccurrences(xml, "<item>"), () -> xml),
                () -> assertEquals(4, countOccurrences(xml, "<title>")
                        - countOccurrences(xml, "<title>NASA Space Station News</title>"),
                        () -> "four items have a title, the second one does not:\n" + xml),
                () -> assertEquals(5, countOccurrences(xml, "<guid>"), () -> xml),
                () -> assertEquals(2, countOccurrences(xml, "<enclosure "), () -> xml));
    }

    @Test
    @DisplayName("the titleless item is accepted, since it has a description")
    void titlelessItemIsAllowed() {
        String xml = RssOutput.outputString(sample());

        int second = xml.indexOf("<item>", xml.indexOf("<item>") + 1);
        String secondItem = xml.substring(second, xml.indexOf("</item>", second));

        assertAll(
                () -> assertFalse(secondItem.contains("<title>"), () -> secondItem),
                () -> assertTrue(secondItem.contains("NASA has selected KBR Wyle Services"),
                        () -> secondItem));
    }

    private static Rss sample() {
        Channel channel = Channel.builder()
                .title(new SimpleValue("NASA Space Station News"))
                .link(new SimpleValue("http://www.nasa.gov/"))
                .description(new SimpleValue("A RSS news feed containing the latest NASA press "
                        + "releases on the International Space Station."))
                .language(new SimpleValue("en-us"))
                .pubDate(new SimpleValue("Tue, 10 Jun 2003 04:00:00 GMT"))
                .lastBuildDate(new SimpleValue("Fri, 21 Jul 2023 09:04 EDT"))
                .docs(new SimpleValue("https://www.rssboard.org/rss-specification"))
                .generator(new SimpleValue("Blosxom 2.1.2"))
                .managingEditor(new SimpleValue("neil.armstrong@example.com (Neil Armstrong)"))
                .webMaster(new SimpleValue("sally.ride@example.com (Sally Ride)"))
                .atomLink(AtomLink.self(FEED_URL))
                .items(items())
                .build();

        return Rss.builder().channel(channel).build();
    }

    private static List<Item> items() {
        String louisiana = "http://www.nasa.gov/press-release/louisiana-students-to-hear-from-"
                + "nasa-astronauts-aboard-space-station";
        String contract = "http://www.nasa.gov/press-release/nasa-awards-integrated-mission-"
                + "operations-contract-iii";
        String suits = "http://www.nasa.gov/press-release/nasa-expands-options-for-spacewalking-"
                + "moonwalking-suits-services";
        String dragon = "http://www.nasa.gov/press-release/nasa-to-provide-coverage-as-dragon-"
                + "departs-station-with-science";
        String laundry = "http://liftoff.msfc.nasa.gov/news/2003/news-laundry.asp";

        return List.of(
                Item.builder()
                        .title(new SimpleValue("Louisiana Students to Hear from NASA Astronauts "
                                + "Aboard Space Station"))
                        .link(new SimpleValue(louisiana))
                        .description(new SimpleValue("As part of the state's first Earth-to-space "
                                + "call, students from Louisiana will have an opportunity soon to "
                                + "hear from NASA astronauts aboard the International Space Station."))
                        .pubDate(new SimpleValue("Fri, 21 Jul 2023 09:04 EDT"))
                        .guid(Guid.of(louisiana))
                        .build(),
                // No title, only a description. The specification allows either one alone.
                Item.builder()
                        .description(new SimpleValue("NASA has selected KBR Wyle Services, LLC, of "
                                + "Fulton, Maryland, to provide mission and flight crew operations "
                                + "support for the International Space Station and future human "
                                + "space exploration."))
                        .link(new SimpleValue(contract))
                        .pubDate(new SimpleValue("Thu, 20 Jul 2023 15:05 EDT"))
                        .guid(Guid.of(contract))
                        .build(),
                Item.builder()
                        .title(new SimpleValue("NASA Expands Options for Spacewalking, "
                                + "Moonwalking Suits"))
                        .link(new SimpleValue(suits))
                        .description(new SimpleValue("NASA has awarded Axiom Space and Collins "
                                + "Aerospace task orders under existing contracts to advance "
                                + "spacewalking capabilities in low Earth orbit, as well as "
                                + "moonwalking services for Artemis missions."))
                        .enclosure(Enclosure.of("http://www.nasa.gov/sites/default/files/styles/"
                                + "1x1_cardfeed/public/thumbnails/image/iss068e027836orig.jpg"
                                + "?itok=ucNUaaGx", 1032272L, "image/jpeg"))
                        .pubDate(new SimpleValue("Mon, 10 Jul 2023 14:14 EDT"))
                        .guid(Guid.of(suits))
                        .build(),
                Item.builder()
                        .title(new SimpleValue("NASA to Provide Coverage as Dragon Departs Station"))
                        .link(new SimpleValue(dragon))
                        .description(new SimpleValue("NASA is set to receive scientific research "
                                + "samples and hardware as a SpaceX Dragon cargo resupply "
                                + "spacecraft departs the International Space Station on "
                                + "Thursday, June 29."))
                        .pubDate(new SimpleValue("Tue, 20 May 2003 08:56:02 GMT"))
                        .guid(Guid.of(dragon))
                        .build(),
                Item.builder()
                        .title(new SimpleValue("NASA Plans Coverage of Roscosmos Spacewalk Outside "
                                + "Space Station"))
                        .link(new SimpleValue(laundry))
                        .description(new SimpleValue("Compared to earlier spacecraft, the "
                                + "International Space Station has many luxuries, but laundry "
                                + "facilities are not one of them.  Instead, astronauts have "
                                + "other options."))
                        .enclosure(Enclosure.of("http://www.nasa.gov/sites/default/files/styles/"
                                + "1x1_cardfeed/public/thumbnails/image/spacex_dragon_june_29.jpg"
                                + "?itok=nIYlBLme", 269866L, "image/jpeg"))
                        .pubDate(new SimpleValue("Mon, 26 Jun 2023 12:45 EDT"))
                        .guid(Guid.of("http://liftoff.msfc.nasa.gov/2003/05/20.html#item570"))
                        .build());
    }

    private static int countOccurrences(final String haystack, final String needle) {
        int count = 0;
        int index = haystack.indexOf(needle);
        while (index >= 0) {
            count++;
            index = haystack.indexOf(needle, index + needle.length());
        }
        return count;
    }
}
