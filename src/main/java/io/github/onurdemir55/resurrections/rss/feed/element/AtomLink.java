package io.github.onurdemir55.resurrections.rss.feed.element;

import io.github.onurdemir55.resurrections.rss.util.Uris;
import io.github.onurdemir55.resurrections.rss.util.XmlText;

import java.util.Objects;

/**
 * An {@code <atom:link>} element, the one extension the
 * <a href="https://www.rssboard.org/rss-profile">RSS Best Practices Profile</a> asks every
 * feed to carry: {@code rel="self"}, pointing at the feed's own address.
 * <p>
 * It is an Atom element rather than an RSS one, so it lives in the Atom namespace. Setting
 * it on a channel makes the feed declare {@code xmlns:atom} automatically, because a
 * prefixed element without its declaration is not well-formed XML and forgetting the
 * declaration should not be possible.
 *
 * @param href the address being linked to
 * @param rel the relationship, {@code self} for the feed's own address
 * @param type the media type of the target, or {@code null} to omit it
 */
public record AtomLink(String href,
                       String rel,
                       String type) {

    /** The prefix this library uses for the Atom namespace. */
    public static final String PREFIX = "atom";

    /** The Atom namespace URI. */
    public static final String NAMESPACE = "http://www.w3.org/2005/Atom";

    /** The media type of an RSS feed. */
    public static final String RSS_MEDIA_TYPE = "application/rss+xml";

    public AtomLink {
        Objects.requireNonNull(href, "atom:link href is required");
        Objects.requireNonNull(rel, "atom:link rel is required");
        Uris.requireScheme("atom:link href", href);
        XmlText.requireWritable(rel, "atom:link rel");
        XmlText.requireWritableOrNull(type, "atom:link type");
    }

    /**
     * The link the Best Practices Profile recommends: the address at which this feed itself
     * can be found, which lets a reader that received the feed some other way find its
     * canonical location.
     *
     * @param href the feed's own address
     * @return the link
     */
    public static AtomLink self(final String href) {
        return new AtomLink(href, "self", RSS_MEDIA_TYPE);
    }

    /**
     * @param href the address being linked to
     * @param rel the relationship
     * @return the link, with no media type
     */
    public static AtomLink of(final String href, final String rel) {
        return new AtomLink(href, rel, null);
    }
}
