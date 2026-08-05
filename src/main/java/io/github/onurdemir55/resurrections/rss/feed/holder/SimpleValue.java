package io.github.onurdemir55.resurrections.rss.feed.holder;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import java.util.Objects;

/**
 * Plain text element content. XML special characters are escaped by the serializer,
 * so {@code &} becomes {@code &amp;}.
 * <p>
 * Use {@link CDATAValue} instead when the content is markup that should stay readable.
 *
 * @param value the text content, which may be empty but not {@code null}. To leave an
 *     element out of the feed, do not set it at all.
 */
public record SimpleValue(@JacksonXmlText String value) implements Value {

    public SimpleValue {
        Objects.requireNonNull(value, "value is required; leave the element unset to omit it");
    }
}
