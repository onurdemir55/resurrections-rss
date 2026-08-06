package io.github.onurdemir55.resurrections.rss.feed.holder;

import io.github.onurdemir55.resurrections.rss.util.XmlText;

import java.util.Objects;

/**
 * Plain text element content. XML special characters are escaped on the way out,
 * so {@code &} becomes {@code &amp;}.
 * <p>
 * Use {@link CDATAValue} instead when the content is markup that should stay readable.
 *
 * @param value the text content, which may be empty but not {@code null}. To leave an
 *     element out of the feed, do not set it at all. It must not contain a character
 *     XML cannot represent, such as a NUL byte.
 */
public record PlainValue(String value) implements Value {

    public PlainValue {
        Objects.requireNonNull(value, "value is required; leave the element unset to omit it");
        XmlText.requireWritable(value);
    }
}
