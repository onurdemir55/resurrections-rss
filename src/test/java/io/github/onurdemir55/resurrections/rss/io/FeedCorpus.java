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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * A corpus of feeds covering the shapes this library can produce, each under a stable name.
 * <p>
 * This exists because of a mistake. The migration away from annotation-driven serialization
 * was checked with one hand-written feed, compared after {@code strip()}, and that hid a real
 * difference: the document had stopped ending with a newline. It was noticed by accident,
 * while measuring something else. The lesson was not "test more carefully" but "stop choosing
 * the cases by hand": what gets compared here is generated from the element list, so it does
 * not depend on anyone remembering that trailing whitespace is worth a look.
 * <p>
 * The corpus was first compared feed by feed, byte for byte, against the previous
 * implementation. 294 of 297 were identical; the three that differed did so in one line each,
 * all the same intended change. {@link FeedCorpusRegressionTest} keeps it that way.
 */
final class FeedCorpus {

    private FeedCorpus() {
        // test fixture
    }

    /** Text shapes worth writing into an element, including the ones nobody thinks of. */
    private static final String[] TEXTS = {
        "", " ", "  spaced  ", "plain",
        "a & b", "a < b", "a > b", "a \" b", "a ' b",
        "&amp; already escaped", "<b>markup</b>",
        "line1\nline2", "tab\there", "cr\rhere", "crlf\r\nhere",
        "trailing newline\n", "\nleading newline",
        "]]>", "]]>]]>", "a]]>b", "]]", ">", "]]]>",
        "<![CDATA[nested]]>",
        "ünïcödé ığşç", "日本語", "عربى", "emoji 📡 pair",
        "very " + "long ".repeat(200) + "text",
        "-- comment --", "<!-- comment -->", "<?pi?>",
        "\t\n\r", "0", "00", "null",
    };

    /**
     * Every feed in the corpus, keyed by a stable name and mapped to its rendered XML.
     *
     * @return name to XML, in a fixed order
     */
    static Map<String, String> feeds() {
        Map<String, String> feeds = new LinkedHashMap<>();
        Emitter emit = new Emitter(feeds);

        texts(emit);
        channelElements(emit);
        itemElements(emit);
        mixedValueForms(emit);
        repeatedElements(emit);
        itemCounts(emit);
        imageShapes(emit);
        guidShapes(emit);
        atomShapes(emit);
        skipShapes(emit);
        namespacesAndExtensions(emit);
        everything(emit);

        return feeds;
    }

    /** Numbers each feed so a name stays stable even if two cases would otherwise collide. */
    private static final class Emitter {

        private final Map<String, String> target;
        private int n;

        Emitter(final Map<String, String> target) {
            this.target = target;
        }

        void feed(final String name, final Rss rss) {
            target.put(String.format("%03d_%s", n++, name), RssOutput.outputString(rss));
        }

        void channel(final String name, final UnaryOperator<Channel.Builder> shape) {
            feed(name, Rss.builder().channel(shape.apply(base()).build()).build());
        }

        void item(final String name, final UnaryOperator<Item.Builder> shape) {
            feed(name, Rss.builder()
                    .channel(base().items(shape.apply(baseItem()).build()).build())
                    .build());
        }
    }

    private static Channel.Builder base() {
        return Channel.builder()
                .title(new SimpleValue("T"))
                .link(new SimpleValue("https://example.com/"))
                .description(new SimpleValue("D"));
    }

    private static Item.Builder baseItem() {
        return Item.builder().title(new SimpleValue("I"));
    }

    private static void texts(final Emitter emit) {
        for (int i = 0; i < TEXTS.length; i++) {
            final String text = TEXTS[i];
            emit.channel("text_plain_" + i, c -> c.description(new SimpleValue(text)));
            emit.channel("text_cdata_" + i, c -> c.description(new CDATAValue(text)));
            emit.channel("text_title_plain_" + i, c -> c.title(new SimpleValue(text)));
            emit.channel("text_title_cdata_" + i, c -> c.title(new CDATAValue(text)));
        }
    }

    private static List<Map.Entry<String, Consumer<Channel.Builder>>> channelShapes() {
        List<Map.Entry<String, Consumer<Channel.Builder>>> all = new ArrayList<>();
        all.add(Map.entry("language",
                (Consumer<Channel.Builder>) c -> c.language(new SimpleValue("en"))));
        all.add(Map.entry("copyright", c -> c.copyright(new SimpleValue("(c) 2026"))));
        all.add(Map.entry("managingEditor",
                c -> c.managingEditor(new SimpleValue("e@example.com"))));
        all.add(Map.entry("webMaster", c -> c.webMaster(new SimpleValue("w@example.com"))));
        all.add(Map.entry("pubDate",
                c -> c.pubDate(new SimpleValue("Wed, 05 Aug 2026 20:00:00 GMT"))));
        all.add(Map.entry("lastBuildDate",
                c -> c.lastBuildDate(new SimpleValue("Wed, 05 Aug 2026 20:00:00 GMT"))));
        all.add(Map.entry("generator", c -> c.generator(new SimpleValue("gen"))));
        all.add(Map.entry("docs", c -> c.docs(new SimpleValue("https://example.com/docs"))));
        all.add(Map.entry("ttl", c -> c.ttl(60)));
        all.add(Map.entry("ttlZero", c -> c.ttl(0)));
        all.add(Map.entry("rating", c -> c.rating(new SimpleValue("(PICS-1.1)"))));
        all.add(Map.entry("category", c -> c.categories(Category.of("Tech"))));
        all.add(Map.entry("categoryDomain", c -> c.categories(Category.of("Tech", "Syndic8"))));
        all.add(Map.entry("categoryCdata", c -> c.categories(Category.cdata("<b>Tech</b>"))));
        all.add(Map.entry("cloud",
                c -> c.cloud(Cloud.of("rpc.example.com", 80, "/RPC2", "p", "xml-rpc"))));
        all.add(Map.entry("cloudPortZero",
                c -> c.cloud(Cloud.of("rpc.example.com", 0, "/", "p", "soap"))));
        all.add(Map.entry("textInput",
                c -> c.textInput(TextInput.of("t", "d", "n", "https://example.com/s"))));
        all.add(Map.entry("image", c -> c.image(
                Image.of("https://example.com/i.png", "t", "https://example.com/"))));
        all.add(Map.entry("skipHours", c -> c.skipHours(SkipHours.of(1))));
        all.add(Map.entry("skipDays", c -> c.skipDays(SkipDays.of(Day.MONDAY))));
        all.add(Map.entry("atomLink",
                c -> c.atomLink(AtomLink.self("https://example.com/f.xml"))));
        return all;
    }

    /** Each optional channel element alone, then in pairs, which is where ordering shows. */
    private static void channelElements(final Emitter emit) {
        var all = channelShapes();
        for (var shape : all) {
            emit.channel("chan_" + shape.getKey(), c -> {
                shape.getValue().accept(c);
                return c;
            });
        }
        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j += 5) {
                final var first = all.get(i);
                final var second = all.get(j);
                emit.channel("chanpair_" + first.getKey() + "_" + second.getKey(), c -> {
                    first.getValue().accept(c);
                    second.getValue().accept(c);
                    return c;
                });
            }
        }
    }

    private static List<Map.Entry<String, Consumer<Item.Builder>>> itemShapes() {
        List<Map.Entry<String, Consumer<Item.Builder>>> all = new ArrayList<>();
        all.add(Map.entry("link",
                (Consumer<Item.Builder>) i -> i.link(new SimpleValue("https://example.com/a"))));
        all.add(Map.entry("description", i -> i.description(new SimpleValue("d"))));
        all.add(Map.entry("descriptionCdata", i -> i.description(new CDATAValue("<p>d</p>"))));
        all.add(Map.entry("author", i -> i.author(new SimpleValue("a@example.com"))));
        all.add(Map.entry("comments", i -> i.comments(new SimpleValue("https://example.com/c"))));
        all.add(Map.entry("pubDate",
                i -> i.pubDate(new SimpleValue("Wed, 05 Aug 2026 20:00:00 GMT"))));
        all.add(Map.entry("category", i -> i.categories(Category.of("Tech"))));
        all.add(Map.entry("categoryDomain", i -> i.categories(Category.of("Tech", "Syndic8"))));
        all.add(Map.entry("enclosure", i -> i.enclosure(
                Enclosure.of("https://example.com/a.mp3", 1L, "audio/mpeg"))));
        all.add(Map.entry("enclosureZero", i -> i.enclosure(
                Enclosure.of("http://example.com/a.mp3", 0L, "x/y"))));
        all.add(Map.entry("guid", i -> i.guid(Guid.of("https://example.com/g"))));
        all.add(Map.entry("guidTrue", i -> i.guid(Guid.of("https://example.com/g", true))));
        all.add(Map.entry("guidFalse", i -> i.guid(Guid.of("id-1", false))));
        all.add(Map.entry("source",
                i -> i.source(Source.of("Origin", "https://example.com/f.xml"))));
        return all;
    }

    private static void itemElements(final Emitter emit) {
        var all = itemShapes();
        for (var shape : all) {
            emit.item("item_" + shape.getKey(), i -> {
                shape.getValue().accept(i);
                return i;
            });
        }
        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j += 4) {
                final var first = all.get(i);
                final var second = all.get(j);
                emit.item("itempair_" + first.getKey() + "_" + second.getKey(), it -> {
                    first.getValue().accept(it);
                    second.getValue().accept(it);
                    return it;
                });
            }
        }
        emit.feed("item_onlyDescription", Rss.builder().channel(base()
                .items(Item.builder().description(new SimpleValue("only d")).build())
                .build()).build());
        emit.feed("item_onlyDescriptionCdata", Rss.builder().channel(base()
                .items(Item.builder().description(new CDATAValue("<p>only d</p>")).build())
                .build()).build());
    }

    private static void mixedValueForms(final Emitter emit) {
        emit.feed("mixed_forms", Rss.builder().channel(Channel.builder()
                .title(new CDATAValue("<b>t</b>"))
                .link(new SimpleValue("https://example.com/"))
                .description(new SimpleValue("d & d"))
                .language(new CDATAValue("en"))
                .copyright(new SimpleValue("c"))
                .items(Item.builder()
                        .title(new SimpleValue("t"))
                        .description(new CDATAValue("<p>x</p>"))
                        .categories(Category.cdata("<i>c</i>"), Category.of("plain"))
                        .guid(Guid.cdata("<id>", false))
                        .source(Source.cdata("<origin>", "https://example.com/f.xml"))
                        .build())
                .build()).build());
    }

    private static void repeatedElements(final Emitter emit) {
        for (int k = 1; k <= 4; k++) {
            final int count = k;
            emit.channel("chan_categories_" + k, c -> {
                Category[] categories = new Category[count];
                for (int i = 0; i < count; i++) {
                    categories[i] = Category.of("c" + i);
                }
                return c.categories(categories);
            });
        }
        emit.channel("chan_categories_sameText", c -> c.categories(
                Category.of("dup", "d1"), Category.of("dup", "d2")));
        emit.item("item_categories_many", i -> i.categories(
                Category.of("a"), Category.of("b", "d"), Category.cdata("<c>")));
    }

    private static void itemCounts(final Emitter emit) {
        for (int k = 0; k <= 3; k++) {
            List<Item> items = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                items.add(Item.builder()
                        .title(new SimpleValue("i" + i))
                        .description(new CDATAValue("<p>b" + i + "</p>"))
                        .build());
            }
            final List<Item> fixed = items;
            emit.channel("items_" + k, c -> c.items(fixed));
        }
        emit.channel("items_emptyList", c -> c.items(List.of()));
    }

    private static void imageShapes(final Emitter emit) {
        emit.channel("image_minimal", c -> c.image(
                Image.of("https://example.com/i.png", "t", "https://example.com/")));
        emit.channel("image_size", c -> c.image(
                Image.of("https://example.com/i.png", "t", "https://example.com/")
                        .withSize(1, 1)));
        emit.channel("image_maxSize", c -> c.image(
                Image.of("https://example.com/i.png", "t", "https://example.com/")
                        .withSize(144, 400)));
        emit.channel("image_description", c -> c.image(
                Image.of("https://example.com/i.png", "t", "https://example.com/")
                        .withDescription(new CDATAValue("<i>d</i>"))));
        emit.channel("image_full", c -> c.image(
                Image.of("https://example.com/i.png", "t", "https://example.com/")
                        .withSize(88, 31).withDescription(new SimpleValue("d & d"))));
    }

    private static void guidShapes(final Emitter emit) {
        emit.item("guid_plain", i -> i.guid(Guid.of("g")));
        emit.item("guid_urlTrue", i -> i.guid(Guid.of("https://example.com/g", true)));
        emit.item("guid_false", i -> i.guid(Guid.of("g", false)));
        emit.item("guid_cdata", i -> i.guid(Guid.cdata("<g>")));
        emit.item("guid_cdataFalse", i -> i.guid(Guid.cdata("<g>", false)));
    }

    private static void atomShapes(final Emitter emit) {
        emit.channel("atom_self", c -> c.atomLink(AtomLink.self("https://example.com/f.xml")));
        emit.channel("atom_of", c -> c.atomLink(
                AtomLink.of("https://example.com/x", "alternate")));
        emit.feed("atom_explicitNamespace", Rss.builder()
                .namespace("atom", "http://www.w3.org/2005/Atom")
                .channel(base().atomLink(AtomLink.self("https://example.com/f.xml")).build())
                .build());
    }

    private static void skipShapes(final Emitter emit) {
        emit.channel("skip_hoursOne", c -> c.skipHours(SkipHours.of(0)));
        emit.channel("skip_hoursAll", c -> {
            int[] hours = new int[24];
            for (int i = 0; i < 24; i++) {
                hours[i] = i;
            }
            return c.skipHours(SkipHours.of(hours));
        });
        emit.channel("skip_hoursDescending", c -> c.skipHours(SkipHours.of(23, 5, 0)));
        emit.channel("skip_daysOne", c -> c.skipDays(SkipDays.of(Day.SUNDAY)));
        emit.channel("skip_daysAll", c -> c.skipDays(SkipDays.of(Day.values())));
    }

    private static void namespacesAndExtensions(final Emitter emit) {
        emit.feed("ns_itemExtension", Rss.builder()
                .namespace("dc", "http://purl.org/dc/elements/1.1/")
                .channel(base().items(baseItem()
                        .extension("dc:creator", new SimpleValue("O")).build()).build())
                .build());
        emit.feed("ns_itemExtensionCdata", Rss.builder()
                .namespace("content", "http://purl.org/rss/1.0/modules/content/")
                .channel(base().items(baseItem()
                        .extension("content:encoded", new CDATAValue("<p>h</p>")).build()).build())
                .build());
        emit.feed("ns_many", Rss.builder()
                .namespace("dc", "http://purl.org/dc/elements/1.1/")
                .namespace("content", "http://purl.org/rss/1.0/modules/content/")
                .namespace("itunes", "http://www.itunes.com/dtds/podcast-1.0.dtd")
                .channel(base().items(baseItem()
                        .extension("dc:creator", new SimpleValue("O"))
                        .extension("content:encoded", new CDATAValue("<p>h</p>"))
                        .extension("itunes:author", new SimpleValue("A"))
                        .build()).build())
                .build());
        emit.feed("ns_channelExtension", Rss.builder()
                .namespace("dc", "http://purl.org/dc/elements/1.1/")
                .channel(base().extension("dc:language", new SimpleValue("tr")).build())
                .build());
        emit.feed("ns_channelExtensionWithItems", Rss.builder()
                .namespace("dc", "http://purl.org/dc/elements/1.1/")
                .channel(base()
                        .extension("dc:language", new SimpleValue("tr"))
                        .items(baseItem().build())
                        .build())
                .build());
        emit.feed("ns_bothExtensions", Rss.builder()
                .namespace("dc", "http://purl.org/dc/elements/1.1/")
                .channel(base()
                        .extension("dc:language", new SimpleValue("tr"))
                        .items(baseItem().extension("dc:creator", new SimpleValue("O")).build())
                        .build())
                .build());
        emit.feed("ns_withAtom", Rss.builder()
                .namespace("dc", "http://purl.org/dc/elements/1.1/")
                .channel(base()
                        .atomLink(AtomLink.self("https://example.com/f.xml"))
                        .items(baseItem().extension("dc:creator", new SimpleValue("O")).build())
                        .build())
                .build());
    }

    private static void everything(final Emitter emit) {
        emit.feed("everything", ExactOutputSample.feed());
    }
}
