package io.github.onurdemir55.resurrections.rss.feed.element;

import io.github.onurdemir55.resurrections.rss.util.Uris;
import io.github.onurdemir55.resurrections.rss.util.XmlText;

import java.util.Objects;

/**
 * The {@code <enclosure>} sub-element of an item: a media object attached to it, which is
 * how podcasts are distributed over RSS.
 * <p>
 * The element carries no text, only attributes, and the specification makes all three
 * required.
 *
 * @param url where the enclosure is located; the specification requires an http url
 * @param length its size in bytes
 * @param type its MIME type, for example {@code audio/mpeg}
 */
public record Enclosure(String url,
                        long length,
                        String type) {

    public Enclosure {
        Objects.requireNonNull(url, "enclosure url is required");
        Objects.requireNonNull(type, "enclosure type is required");
        Uris.requireHttpScheme("enclosure url", url);
        XmlText.requireWritable(type, "enclosure type");
        if (length < 0) {
            throw new IllegalArgumentException("enclosure length cannot be negative, was " + length);
        }
    }

    /**
     * @param url where the enclosure is located
     * @param length its size in bytes
     * @param type its MIME type
     * @return the enclosure
     */
    public static Enclosure of(final String url, final long length, final String type) {
        return new Enclosure(url, length, type);
    }
}
