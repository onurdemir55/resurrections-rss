package io.github.onurdemir55.resurrections.rss.feed.holder;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * Plain text element content. XML special characters are escaped by the serializer,
 * so {@code &} becomes {@code &amp;}.
 * <p>
 * Use {@link CDATAValue} instead when the content is markup that should stay readable.
 *
 * @param value the text content
 */
public record SimpleValue(@JacksonXmlText String value) implements Value {
}
