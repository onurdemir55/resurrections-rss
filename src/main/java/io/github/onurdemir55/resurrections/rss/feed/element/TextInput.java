package io.github.onurdemir55.resurrections.rss.feed.element;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import io.github.onurdemir55.resurrections.rss.util.Uris;

import java.util.Objects;

/**
 * The {@code <textInput>} sub-element of a channel: an input box a reader may display with
 * the channel, typically for search or feedback.
 * <p>
 * All four sub-elements are required. The specification itself calls the purpose of this
 * element "something of a mystery" and notes that most readers ignore it.
 *
 * @param title the label of the submit button
 * @param description explains the input box
 * @param name the name of the text field
 * @param link the url of the script that processes the input
 */
@JsonPropertyOrder({"title", "description", "name", "link"})
public record TextInput(@JacksonXmlProperty(localName = "title") Value title,
                        @JacksonXmlProperty(localName = "description") Value description,
                        @JacksonXmlProperty(localName = "name") Value name,
                        @JacksonXmlProperty(localName = "link") Value link) {

    public TextInput {
        Objects.requireNonNull(title, "textInput title is required");
        Objects.requireNonNull(description, "textInput description is required");
        Objects.requireNonNull(name, "textInput name is required");
        Objects.requireNonNull(link, "textInput link is required");
        Uris.requireScheme("textInput link", link.value());
    }

    /**
     * A text input with all four sub-elements as plain text. Unlike {@link Category},
     * {@link Guid} or {@link Source}, there is no {@code cdata(...)} counterpart: this
     * element has several text sub-elements, and a single factory cannot say which of them
     * should be CDATA and which should not. To mix forms, construct the record directly.
     *
     * @param title the label of the submit button
     * @param description explains the input box
     * @param name the name of the text field
     * @param link the url of the script that processes the input
     * @return the text input
     */
    public static TextInput of(final String title, final String description,
                               final String name, final String link) {
        return new TextInput(new SimpleValue(title), new SimpleValue(description),
                new SimpleValue(name), new SimpleValue(link));
    }
}
