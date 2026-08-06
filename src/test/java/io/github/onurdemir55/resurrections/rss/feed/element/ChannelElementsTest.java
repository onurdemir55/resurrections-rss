package io.github.onurdemir55.resurrections.rss.feed.element;

import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.io.RssOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The channel elements, checked against the examples given in the RSS 2.0 specification.
 */
class ChannelElementsTest {

    @Nested
    @DisplayName("plain text elements")
    class PlainText {

        @Test
        @DisplayName("the optional text elements match the specification examples")
        void specificationExamples() {
            String xml = channelXml(base()
                    .copyright(new SimpleValue("Copyright 2002, Spartanburg Herald-Journal"))
                    .managingEditor(new SimpleValue("geo@herald.com (George Matesky)"))
                    .webMaster(new SimpleValue("betty@herald.com (Betty Guernsey)"))
                    .lastBuildDate(new SimpleValue("Sat, 07 Sep 2002 09:42:31 GMT"))
                    .generator(new SimpleValue("MightyInHouse Content System v2.3"))
                    .docs(new SimpleValue("https://www.rssboard.org/rss-specification"))
                    .ttl(60));

            assertAll(
                    () -> assertTrue(xml.contains(
                            "<copyright>Copyright 2002, Spartanburg Herald-Journal</copyright>"), () -> xml),
                    () -> assertTrue(xml.contains(
                            "<managingEditor>geo@herald.com (George Matesky)</managingEditor>"), () -> xml),
                    () -> assertTrue(xml.contains(
                            "<webMaster>betty@herald.com (Betty Guernsey)</webMaster>"), () -> xml),
                    () -> assertTrue(xml.contains(
                            "<lastBuildDate>Sat, 07 Sep 2002 09:42:31 GMT</lastBuildDate>"), () -> xml),
                    () -> assertTrue(xml.contains(
                            "<generator>MightyInHouse Content System v2.3</generator>"), () -> xml),
                    () -> assertTrue(xml.contains(
                            "<docs>https://www.rssboard.org/rss-specification</docs>"), () -> xml),
                    () -> assertTrue(xml.contains("<ttl>60</ttl>"), () -> xml));
        }

        @Test
        @DisplayName("a channel category can link the feed to a cataloguing system")
        void categoryWithDomain() {
            String xml = channelXml(base()
                    .categories(Category.of("Newspapers"), Category.of("1765", "Syndic8")));

            assertAll(
                    () -> assertTrue(xml.contains("<category>Newspapers</category>"), () -> xml),
                    () -> assertTrue(xml.contains("<category domain=\"Syndic8\">1765</category>"),
                            () -> xml));
        }
    }

    @Nested
    @DisplayName("image")
    class ImageElement {

        @Test
        @DisplayName("only the three required sub-elements are emitted when nothing else is set")
        void requiredOnly() {
            String xml = channelXml(base().image(
                    Image.of("http://example.com/logo.png", "Logo", "http://example.com/")));

            assertAll(
                    () -> assertTrue(xml.contains("<url>http://example.com/logo.png</url>"), () -> xml),
                    () -> assertTrue(xml.contains("<title>Logo</title>"), () -> xml),
                    () -> assertTrue(xml.contains("<link>http://example.com/</link>"), () -> xml),
                    () -> assertEquals(-1, xml.indexOf("<width>"), () -> xml),
                    () -> assertEquals(-1, xml.indexOf("<height>"), () -> xml));
        }

        @Test
        @DisplayName("dimensions and description are emitted when set")
        void allSubElements() {
            String xml = channelXml(base().image(
                    Image.of("http://example.com/logo.png", "Logo", "http://example.com/")
                            .withSize(144, 400)
                            .withDescription(new CDATAValue("Tooltip & <b>more</b>"))));

            assertAll(
                    () -> assertTrue(xml.contains("<width>144</width>"), () -> xml),
                    () -> assertTrue(xml.contains("<height>400</height>"), () -> xml),
                    () -> assertTrue(xml.contains(
                            "<description><![CDATA[Tooltip & <b>more</b>]]></description>"), () -> xml));
        }

        @Test
        @DisplayName("the specification's maximum dimensions are enforced")
        void dimensionBounds() {
            Image image = Image.of("http://example.com/logo.png", "Logo", "http://example.com/");

            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> image.withSize(Image.MAX_WIDTH + 1, 31)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> image.withSize(88, Image.MAX_HEIGHT + 1)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> image.withSize(-1, 31)));
        }

        @Test
        @DisplayName("url, title and link are required")
        void requiredParts() {
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> new Image(null, new SimpleValue("t"), new SimpleValue("l"),
                                    null, null, null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> new Image(new SimpleValue("u"), null, new SimpleValue("l"),
                                    null, null, null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> new Image(new SimpleValue("u"), new SimpleValue("t"), null,
                                    null, null, null)));
        }
    }

    @Nested
    @DisplayName("cloud")
    class CloudElement {

        @Test
        @DisplayName("matches the specification example, as an empty element")
        void specificationExample() {
            String xml = channelXml(base().cloud(
                    Cloud.of("rpc.sys.com", 80, "/RPC2", "myCloud.rssPleaseNotify", "xml-rpc")));

            assertTrue(xml.contains("<cloud domain=\"rpc.sys.com\" port=\"80\" path=\"/RPC2\" "
                    + "registerProcedure=\"myCloud.rssPleaseNotify\" protocol=\"xml-rpc\"/>"), () -> xml);
        }

        @Test
        @DisplayName("the string attributes are required")
        void requiredAttributes() {
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> Cloud.of(null, 80, "/RPC2", "p", "xml-rpc")),
                    () -> assertThrows(NullPointerException.class,
                            () -> Cloud.of("rpc.sys.com", 80, null, "p", "xml-rpc")),
                    () -> assertThrows(NullPointerException.class,
                            () -> Cloud.of("rpc.sys.com", 80, "/RPC2", null, "xml-rpc")),
                    () -> assertThrows(NullPointerException.class,
                            () -> Cloud.of("rpc.sys.com", 80, "/RPC2", "p", null)));
        }
    }

    @Nested
    @DisplayName("textInput")
    class TextInputElement {

        @Test
        @DisplayName("all four sub-elements are emitted, in specification order")
        void allSubElements() {
            String xml = channelXml(base().textInput(
                    TextInput.of("Submit", "Search the archive", "q", "http://example.com/search")));

            assertAll(
                    () -> assertTrue(xml.contains("<textInput>"), () -> xml),
                    () -> assertTrue(xml.contains("<name>q</name>"), () -> xml),
                    () -> assertTrue(xml.indexOf("<title>Submit</title>")
                            < xml.indexOf("<description>Search the archive</description>"), () -> xml),
                    () -> assertTrue(xml.indexOf("<name>q</name>")
                            < xml.indexOf("<link>http://example.com/search</link>"), () -> xml));
        }

        @Test
        @DisplayName("all four sub-elements are required")
        void everythingIsRequired() {
            SimpleValue set = new SimpleValue("x");

            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> new TextInput(null, set, set, set)),
                    () -> assertThrows(NullPointerException.class,
                            () -> new TextInput(set, null, set, set)),
                    () -> assertThrows(NullPointerException.class,
                            () -> new TextInput(set, set, null, set)),
                    () -> assertThrows(NullPointerException.class,
                            () -> new TextInput(set, set, set, null)));
        }
    }

    @Nested
    @DisplayName("skipHours and skipDays")
    class SkipElements {

        @Test
        @DisplayName("hours are emitted as repeated child elements")
        void hours() {
            String xml = channelXml(base().skipHours(SkipHours.of(0, 1, 23)));

            assertTrue(xml.contains("<skipHours>")
                    && xml.contains("<hour>0</hour>")
                    && xml.contains("<hour>23</hour>"), () -> xml);
        }

        @Test
        @DisplayName("days use the spelling from the specification, not the enum name")
        void days() {
            String xml = channelXml(base().skipDays(SkipDays.of(Day.SATURDAY, Day.SUNDAY)));

            assertAll(
                    () -> assertTrue(xml.contains("<day>Saturday</day>"), () -> xml),
                    () -> assertTrue(xml.contains("<day>Sunday</day>"), () -> xml),
                    () -> assertEquals(-1, xml.indexOf("SATURDAY"), () -> xml));
        }

        @Test
        @DisplayName("an hour outside 0..23 is rejected")
        void hourRange() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> SkipHours.of(-1)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> SkipHours.of(SkipHours.MAX_HOUR + 1)));
        }

        @Test
        @DisplayName("repeating an hour or a day is rejected, since it says nothing extra")
        void duplicatesRejected() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> SkipHours.of(3, 3)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> SkipDays.of(Day.MONDAY, Day.MONDAY)));
        }

        @Test
        @DisplayName("an empty list is rejected")
        void emptyRejected() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> SkipHours.of()),
                    () -> assertThrows(IllegalArgumentException.class, () -> SkipDays.of()));
        }

        @Test
        @DisplayName("Day maps from java.time.DayOfWeek")
        void fromDayOfWeek() {
            assertAll(
                    () -> assertEquals(Day.MONDAY, Day.from(java.time.DayOfWeek.MONDAY)),
                    () -> assertEquals(Day.SUNDAY, Day.from(java.time.DayOfWeek.SUNDAY)));
        }

        @Test
        @DisplayName("Day.from rejects a null DayOfWeek")
        void fromRejectsNull() {
            assertThrows(NullPointerException.class, () -> Day.from(null));
        }
    }

    @Nested
    @DisplayName("ttl")
    class Ttl {

        @Test
        @DisplayName("a negative lifetime is refused, as other out-of-range numbers are")
        void negativeIsRefused() {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> Channel.builder().ttl(-1));

            assertTrue(thrown.getMessage().contains("cannot be negative"), thrown::getMessage);
        }

        @Test
        @DisplayName("zero is allowed, since it means do not cache")
        void zeroIsAllowed() {
            assertTrue(channelXml(base().ttl(0)).contains("<ttl>0</ttl>"));
        }

        @Test
        @DisplayName("and null omits the element")
        void nullOmits() {
            assertFalse(channelXml(base().ttl(null)).contains("<ttl"));
        }
    }

    @Test
    @DisplayName("all channel elements are emitted in the order the specification lists them")
    void specificationElementOrder() {
        String xml = channelXml(base()
                .language(new SimpleValue("en-us"))
                .copyright(new SimpleValue("Copyright"))
                .managingEditor(new SimpleValue("editor@example.com"))
                .webMaster(new SimpleValue("master@example.com"))
                .pubDate(new SimpleValue("Sat, 07 Sep 2002 00:00:01 GMT"))
                .lastBuildDate(new SimpleValue("Sat, 07 Sep 2002 09:42:31 GMT"))
                .categories(Category.of("Newspapers"))
                .generator(new SimpleValue("resurrections-rss"))
                .docs(new SimpleValue("https://www.rssboard.org/rss-specification"))
                .cloud(Cloud.of("rpc.sys.com", 80, "/RPC2", "p", "xml-rpc"))
                .ttl(60)
                .image(Image.of("http://example.com/logo.png", "Logo", "http://example.com/"))
                .rating(new SimpleValue("(PICS-1.1)"))
                .textInput(TextInput.of("Submit", "Search", "q", "http://example.com/search"))
                .skipHours(SkipHours.of(0))
                .skipDays(SkipDays.of(Day.SUNDAY)));

        int previous = -1;
        for (String element : new String[]{"<title>", "<link>", "<description>", "<language>",
                                          "<copyright>", "<managingEditor>", "<webMaster>",
                                          "<pubDate>", "<lastBuildDate>", "<category>",
                                          "<generator>", "<docs>", "<cloud ", "<ttl>", "<image>",
                                          "<rating>", "<textInput>", "<skipHours>", "<skipDays>"}) {
            int index = xml.indexOf(element);
            assertTrue(index > previous,
                    () -> element + " is missing or out of specification order in:\n" + xml);
            previous = index;
        }
    }

    @Test
    @DisplayName("the List and varargs builder forms behave identically")
    void builderOverloadsAgree() {
        io.github.onurdemir55.resurrections.rss.feed.Item item =
                io.github.onurdemir55.resurrections.rss.feed.Item.builder()
                        .title(new SimpleValue("an item"))
                        .build();

        String fromVarargs = channelXml(base()
                .categories(Category.of("a"), Category.of("b"))
                .items(item));
        String fromList = channelXml(base()
                .categories(java.util.List.of(Category.of("a"), Category.of("b")))
                .items(java.util.List.of(item)));

        assertEquals(fromVarargs, fromList);
    }

    private static Channel.Builder base() {
        return Channel.builder()
                .title(new SimpleValue("GoUpstate.com News Headlines"))
                .link(new SimpleValue("http://www.goupstate.com/"))
                .description(new SimpleValue("The latest news from GoUpstate.com."));
    }

    private static String channelXml(final Channel.Builder channel) {
        return RssOutput.outputString(Rss.builder().channel(channel.build()).build());
    }
}
