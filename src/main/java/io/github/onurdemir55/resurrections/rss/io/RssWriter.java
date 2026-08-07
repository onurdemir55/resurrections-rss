package io.github.onurdemir55.resurrections.rss.io;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxOutputFactory;
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
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;
import java.util.Map;

/**
 * Writes a feed as XML, one element at a time.
 * <p>
 * The model classes carry no serialization annotations; this class decides what the document
 * looks like. That separation is deliberate. When the mapping lived in annotations, the XML
 * a change would produce was not something you could read off the code, and adding an
 * ordinary getter to the model once altered the output. Here the document is written in the
 * order it appears, and the one decision this library exists to make is a plain branch:
 * {@link #content(Value)} calls either {@code writeCData} or {@code writeCharacters}.
 * <p>
 * Instances are not thread safe and are not meant to be reused; {@link RssOutput} creates
 * one per document.
 */
final class RssWriter {

    /**
     * Shared, and safe to share: a StAX output factory is thread safe for creating writers
     * once configured.
     */
    private static final XMLOutputFactory FACTORY = createFactory();

    private static final String INDENT = "  ";

    /** Broken out of a CDATA section, because XML normalizes it away otherwise. */
    private static final char CARRIAGE_RETURN = '\r';

    /** {@code String.valueOf(CARRIAGE_RETURN)}, cached: {@code char} has no such cache of its own. */
    private static final String CARRIAGE_RETURN_STRING = String.valueOf(CARRIAGE_RETURN);

    private final XMLStreamWriter out;
    private int depth;

    private RssWriter(final XMLStreamWriter out) {
        this.out = out;
    }

    /**
     * Writes the feed to the given writer. The writer is flushed but not closed; the caller
     * keeps ownership of what they passed in.
     *
     * @param rss the feed to write
     * @param target where to write
     * @throws XMLStreamException if the document cannot be written
     */
    static void write(final Rss rss, final java.io.Writer target) throws XMLStreamException {
        XMLStreamWriter writer = FACTORY.createXMLStreamWriter(target);
        new RssWriter(writer).writeDocument(rss);
        writer.flush();
    }

    /**
     * Writes the feed to the given stream as UTF-8, the encoding the XML declaration
     * announces. The stream is flushed but not closed.
     *
     * @param rss the feed to write
     * @param target where to write
     * @throws XMLStreamException if the document cannot be written
     */
    static void write(final Rss rss, final java.io.OutputStream target) throws XMLStreamException {
        XMLStreamWriter writer = FACTORY.createXMLStreamWriter(target, "UTF-8");
        new RssWriter(writer).writeDocument(rss);
        writer.flush();
    }

    private void writeDocument(final Rss rss) throws XMLStreamException {
        out.writeStartDocument("UTF-8", "1.0");
        newline();

        out.writeStartElement("rss");
        out.writeAttribute("version", rss.getVersion());
        // A prefixed element is not well formed unless its prefix is declared, and the
        // declarations belong on the root element.
        for (Map.Entry<String, String> namespace : rss.getNamespaces().entrySet()) {
            out.writeAttribute("xmlns:" + namespace.getKey(), namespace.getValue());
        }

        depth++;
        channel(rss.getChannel());
        depth--;

        newline();
        out.writeEndElement();
        // A text file ends with a newline. This is also what the previous, annotation-driven
        // implementation emitted, so a feed written by either is the same file.
        newline();
        out.writeEndDocument();
    }

    /** Elements are written in the order the specification lists them, with the items last. */
    private void channel(final Channel channel) throws XMLStreamException {
        open("channel");

        value("title", channel.getTitle());
        value("link", channel.getLink());
        value("description", channel.getDescription());
        atomLink(channel.getAtomLink());
        value("language", channel.getLanguage());
        value("copyright", channel.getCopyright());
        value("managingEditor", channel.getManagingEditor());
        value("webMaster", channel.getWebMaster());
        value("pubDate", channel.getPubDate());
        value("lastBuildDate", channel.getLastBuildDate());
        categories(channel.getCategories());
        value("generator", channel.getGenerator());
        value("docs", channel.getDocs());
        cloud(channel.getCloud());
        number("ttl", channel.getTtl());
        image(channel.getImage());
        value("rating", channel.getRating());
        textInput(channel.getTextInput());
        skipHours(channel.getSkipHours());
        skipDays(channel.getSkipDays());
        extensions(channel.getExtensions());
        items(channel.getItems());

        close();
    }

    private void items(final List<Item> items) throws XMLStreamException {
        for (Item item : items) {
            open("item");

            value("title", item.getTitle());
            value("link", item.getLink());
            value("description", item.getDescription());
            value("author", item.getAuthor());
            categories(item.getCategories());
            value("comments", item.getComments());
            enclosure(item.getEnclosure());
            guid(item.getGuid());
            value("pubDate", item.getPubDate());
            source(item.getSource());
            extensions(item.getExtensions());

            close();
        }
    }

    private void categories(final List<Category> categories) throws XMLStreamException {
        for (Category category : categories) {
            indent();
            out.writeStartElement("category");
            attributeIfPresent("domain", category.domain());
            content(category.value());
            out.writeEndElement();
        }
    }

    private void guid(final Guid guid) throws XMLStreamException {
        if (guid == null) {
            return;
        }
        indent();
        out.writeStartElement("guid");
        // Omitted rather than written as true: the specification's default is true, so
        // leaving it out and setting it to true are not the same statement.
        if (guid.isPermaLink() != null) {
            out.writeAttribute("isPermaLink", String.valueOf(guid.isPermaLink()));
        }
        content(guid.value());
        out.writeEndElement();
    }

    private void source(final Source source) throws XMLStreamException {
        if (source == null) {
            return;
        }
        indent();
        out.writeStartElement("source");
        out.writeAttribute("url", source.url());
        content(source.value());
        out.writeEndElement();
    }

    private void enclosure(final Enclosure enclosure) throws XMLStreamException {
        if (enclosure == null) {
            return;
        }
        indent();
        out.writeEmptyElement("enclosure");
        out.writeAttribute("url", enclosure.url());
        out.writeAttribute("length", String.valueOf(enclosure.length()));
        out.writeAttribute("type", enclosure.type());
    }

    private void atomLink(final AtomLink link) throws XMLStreamException {
        if (link == null) {
            return;
        }
        indent();
        out.writeEmptyElement("atom:link");
        out.writeAttribute("href", link.href());
        out.writeAttribute("rel", link.rel());
        attributeIfPresent("type", link.type());
    }

    private void cloud(final Cloud cloud) throws XMLStreamException {
        if (cloud == null) {
            return;
        }
        indent();
        out.writeEmptyElement("cloud");
        out.writeAttribute("domain", cloud.domain());
        out.writeAttribute("port", String.valueOf(cloud.port()));
        out.writeAttribute("path", cloud.path());
        out.writeAttribute("registerProcedure", cloud.registerProcedure());
        out.writeAttribute("protocol", cloud.protocol());
    }

    private void image(final Image image) throws XMLStreamException {
        if (image == null) {
            return;
        }
        open("image");
        value("url", image.url());
        value("title", image.title());
        value("link", image.link());
        number("width", image.width());
        number("height", image.height());
        value("description", image.description());
        close();
    }

    private void textInput(final TextInput textInput) throws XMLStreamException {
        if (textInput == null) {
            return;
        }
        open("textInput");
        value("title", textInput.title());
        value("description", textInput.description());
        value("name", textInput.name());
        value("link", textInput.link());
        close();
    }

    private void skipHours(final SkipHours skipHours) throws XMLStreamException {
        if (skipHours == null) {
            return;
        }
        open("skipHours");
        for (Integer hour : skipHours.hours()) {
            number("hour", hour);
        }
        close();
    }

    private void skipDays(final SkipDays skipDays) throws XMLStreamException {
        if (skipDays == null) {
            return;
        }
        open("skipDays");
        for (Day day : skipDays.days()) {
            text("day", day.label());
        }
        close();
    }

    /** Elements from other namespaces, written with the prefixed name they were given. */
    private void extensions(final Map<String, Value> extensions) throws XMLStreamException {
        for (Map.Entry<String, Value> extension : extensions.entrySet()) {
            value(extension.getKey(), extension.getValue());
        }
    }

    private void value(final String name, final Value value) throws XMLStreamException {
        if (value == null) {
            return;
        }
        indent();
        out.writeStartElement(name);
        content(value);
        out.writeEndElement();
    }

    /**
     * The whole reason this library exists, and here it is a branch on the value's type
     * rather than an annotation on a field.
     * <p>
     * Text containing {@code ]]>} would close a CDATA section early. The output factory is
     * configured to split it across two sections instead of refusing, so ordinary HTML does
     * not make the whole feed fail.
     */
    private void content(final Value value) throws XMLStreamException {
        if (value instanceof CDATAValue cdata) {
            cdata(cdata.value());
        } else {
            out.writeCharacters(value.value());
        }
    }

    /**
     * Writes text as a CDATA section, carrying a carriage return out of it and back in.
     * <p>
     * XML normalizes line endings on the way in: a parser turns {@code \r\n} and a lone
     * {@code \r} into {@code \n} before anything else sees them. Escaped text survives that,
     * because the writer spells a carriage return {@code &#xd;} and a character reference is
     * resolved after normalization. Inside a CDATA section nothing can be escaped, so
     * {@code a\r\nb} came back out as {@code a\nb} - silently, and for the commonest input
     * there is, HTML written on Windows.
     * <p>
     * So the section is broken around each carriage return, which is then written as ordinary
     * escaped text between two sections. The same trick the CDATA terminator needs, for the
     * same reason: what the caller passed in is what a reader gets back. Text with no carriage
     * return takes the plain path and is written exactly as before.
     */
    private void cdata(final String text) throws XMLStreamException {
        if (text.indexOf(CARRIAGE_RETURN) < 0) {
            out.writeCData(text);
            return;
        }
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == CARRIAGE_RETURN) {
                if (i > start) {
                    out.writeCData(text.substring(start, i));
                }
                out.writeCharacters(CARRIAGE_RETURN_STRING);
                start = i + 1;
            }
        }
        if (start < text.length()) {
            out.writeCData(text.substring(start));
        }
    }

    private void text(final String name, final String value) throws XMLStreamException {
        if (value == null) {
            return;
        }
        indent();
        out.writeStartElement(name);
        out.writeCharacters(value);
        out.writeEndElement();
    }

    private void number(final String name, final Integer value) throws XMLStreamException {
        text(name, value == null ? null : String.valueOf(value));
    }

    private void attributeIfPresent(final String name, final String value)
            throws XMLStreamException {
        if (value != null) {
            out.writeAttribute(name, value);
        }
    }

    private void open(final String name) throws XMLStreamException {
        indent();
        out.writeStartElement(name);
        depth++;
    }

    private void close() throws XMLStreamException {
        depth--;
        indent();
        out.writeEndElement();
    }

    /**
     * StAX writes no whitespace of its own, so indentation is this class's job. Jackson used
     * to supply it through INDENT_OUTPUT; the cost of doing it here is this method, and the
     * benefit is that the layout is no longer at the mercy of a library default.
     */
    private void indent() throws XMLStreamException {
        newline();
        out.writeCharacters(INDENT.repeat(depth));
    }

    private void newline() throws XMLStreamException {
        out.writeCharacters("\n");
    }

    /**
     * Text containing {@code ]]>} cannot sit inside one CDATA section. Woodstox refuses to
     * write it unless allowed to fix the content, and refusing would mean an ordinary piece
     * of HTML makes the whole feed fail. With both properties set it emits
     * {@code <![CDATA[a ]]]]><![CDATA[> b]]>}, which a parser reads back as the original text.
     */
    private static XMLOutputFactory createFactory() {
        WstxOutputFactory factory = new WstxOutputFactory();
        factory.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_CONTENT, true);
        factory.setProperty(WstxOutputProperties.P_OUTPUT_FIX_CONTENT, true);
        return factory;
    }
}
