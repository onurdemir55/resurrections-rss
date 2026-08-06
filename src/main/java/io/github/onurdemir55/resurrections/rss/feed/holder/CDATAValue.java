package io.github.onurdemir55.resurrections.rss.feed.holder;

import io.github.onurdemir55.resurrections.rss.util.XmlText;

import java.util.Objects;

/**
 * Element content wrapped in a CDATA section, so markup stays readable in the feed
 * instead of being entity-encoded.
 * <p>
 * Content containing {@code ]]>} is split across two CDATA sections, which a parser reads
 * back as the original text.
 *
 * @param value the text content, emitted inside {@code <![CDATA[ ... ]]>}. May be empty
 *     but not {@code null}; to leave an element out of the feed, do not set it at all.
 *     It must not contain a character XML cannot represent, such as a NUL byte.
 */
public record CDATAValue(String value) implements Value {

    public CDATAValue {
        Objects.requireNonNull(value, "value is required; leave the element unset to omit it");
        XmlText.requireWritable(value);
    }
}
