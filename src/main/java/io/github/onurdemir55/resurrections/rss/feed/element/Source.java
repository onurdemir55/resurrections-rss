package io.github.onurdemir55.resurrections.rss.feed.element;

import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import io.github.onurdemir55.resurrections.rss.util.Uris;

import java.util.Objects;

/**
 * The {@code <source>} sub-element of an item: the channel the item came from.
 * <p>
 * Its purpose is to propagate credit when an item is forwarded, so the value is the source
 * channel's title and the required {@code url} attribute points at that channel's feed.
 *
 * @param value the title of the originating channel, plain or CDATA
 * @param url the url of the originating feed, required by the specification
 */
public record Source(Value value,
                     String url) {

    public Source {
        Objects.requireNonNull(value, "source value is required");
        Objects.requireNonNull(url, "source url is required");
        Uris.requireScheme("source url", url);
    }

    /**
     * @param value the plain text title of the originating channel
     * @param url the url of the originating feed
     * @return the source
     */
    public static Source of(final String value, final String url) {
        return new Source(new SimpleValue(value), url);
    }

    /**
     * @param value the title of the originating channel, wrapped in a CDATA section
     * @param url the url of the originating feed
     * @return the source
     */
    public static Source cdata(final String value, final String url) {
        return new Source(new CDATAValue(value), url);
    }
}
