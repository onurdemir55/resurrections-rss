package io.github.onurdemir55.resurrections.rss.feed;

import io.github.onurdemir55.resurrections.rss.feed.element.Category;
import io.github.onurdemir55.resurrections.rss.feed.element.Enclosure;
import io.github.onurdemir55.resurrections.rss.feed.element.Guid;
import io.github.onurdemir55.resurrections.rss.feed.element.Source;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import io.github.onurdemir55.resurrections.rss.util.Uris;
import io.github.onurdemir55.resurrections.rss.util.XmlNames;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An {@code <item>} element of a {@link Channel}.
 * <p>
 * Every element of an item is optional, but the specification requires at least one of
 * {@code title} or {@code description} to be present. Elements are emitted in the order the
 * specification lists them.
 * <p>
 * Instances are immutable and created through {@link #builder()}.
 */
public final class Item {

    private final Value title;

    private final Value link;

    private final Value description;

    private final Value author;

    private final List<Category> category;

    private final Value comments;

    private final Enclosure enclosure;

    private final Guid guid;

    private final Value pubDate;

    private final Source source;

    /** Elements from other namespaces, keyed by their prefixed name. */
    private final Map<String, Value> extensions;

    private Item(final Builder builder) {
        this.title = builder.title;
        this.link = builder.link;
        this.description = builder.description;
        this.author = builder.author;
        this.category = builder.category == null ? List.of() : builder.category;
        this.comments = builder.comments;
        this.enclosure = builder.enclosure;
        this.guid = builder.guid;
        this.pubDate = builder.pubDate;
        this.source = builder.source;
        // LinkedHashMap, not Map.copyOf: the output order must be the order they
        // were declared in, so the same feed serializes the same way every time.
        this.extensions = Collections.unmodifiableMap(new LinkedHashMap<>(builder.extensions));
    }

    /**
     * Extension elements, keyed by the prefixed name they were registered under and written
     * in the order they were added.
     *
     * @return the extension elements, never {@code null} and possibly empty
     */
    public Map<String, Value> getExtensions() {
        return extensions;
    }

    /**
     * @return the item title, or {@code null} when it carries a description instead
     */
    public Value getTitle() {
        return title;
    }

    /**
     * @return the item link, or {@code null}
     */
    public Value getLink() {
        return link;
    }

    /**
     * @return the item description, or {@code null} when it carries a title instead
     */
    public Value getDescription() {
        return description;
    }

    /**
     * @return the author's email address, or {@code null}
     */
    public Value getAuthor() {
        return author;
    }

    /**
     * @return the categories, never {@code null} and empty when none were set
     */
    public List<Category> getCategory() {
        return category;
    }

    /**
     * @return the comments page url, or {@code null}
     */
    public Value getComments() {
        return comments;
    }

    /**
     * @return the attached media object, or {@code null}
     */
    public Enclosure getEnclosure() {
        return enclosure;
    }

    /**
     * @return the unique identifier, or {@code null}
     */
    public Guid getGuid() {
        return guid;
    }

    /**
     * @return the publication date, or {@code null}
     */
    public Value getPubDate() {
        return pubDate;
    }

    /**
     * @return the originating channel, or {@code null}
     */
    public Source getSource() {
        return source;
    }

    /**
     * @return a new builder for {@code <item>}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link Item}.
     */
    public static final class Builder {

        private Value title;
        private Value link;
        private Value description;
        private Value author;
        private List<Category> category;
        private Value comments;
        private Enclosure enclosure;
        private Guid guid;
        private Value pubDate;
        private Source source;
        private final Map<String, Value> extensions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * @param title the item title
         */
        public Builder title(final Value title) {
            this.title = title;
            return this;
        }

        /**
         * @param link the item link
         */
        public Builder link(final Value link) {
            this.link = link;
            return this;
        }

        /**
         * @param description the item description
         */
        public Builder description(final Value description) {
            this.description = description;
            return this;
        }

        /**
         * Email address of the author of the item. For a weblog written by one person it
         * makes more sense to leave this out and use the channel's managing editor.
         *
         * @param author the author's email address
         */
        public Builder author(final Value author) {
            this.author = author;
            return this;
        }

        /**
         * Categories for this item, emitted as one {@code <category>} element each, in the
         * order given. Repeating a value under different domains is allowed.
         *
         * @param category the categories
         */
        public Builder category(final List<Category> category) {
            this.category = category == null ? null : List.copyOf(category);
            return this;
        }

        /**
         * Convenience form of {@link #category(List)}.
         *
         * @param category the categories
         */
        public Builder category(final Category... category) {
            this.category = List.of(category);
            return this;
        }

        /**
         * URL of a page for comments relating to the item.
         *
         * @param comments the comments page url
         */
        public Builder comments(final Value comments) {
            this.comments = comments;
            return this;
        }

        /**
         * A media object attached to the item.
         *
         * @param enclosure the enclosure
         */
        public Builder enclosure(final Enclosure enclosure) {
            this.enclosure = enclosure;
            return this;
        }

        /**
         * A string that uniquely identifies the item, which readers use to tell whether they
         * have seen it before. Providing one is recommended even when a link is present.
         *
         * @param guid the identifier
         */
        public Builder guid(final Guid guid) {
            this.guid = guid;
            return this;
        }

        /**
         * @param pubDate the publication date
         */
        public Builder pubDate(final Value pubDate) {
            this.pubDate = pubDate;
            return this;
        }

        /**
         * The channel this item came from, used to propagate credit when forwarding an item.
         *
         * @param source the originating channel
         */
        public Builder source(final Source source) {
            this.source = source;
            return this;
        }

        /**
         * Adds an element from another namespace, which is the only kind of element the
         * specification permits beyond the ones it describes.
         * <p>
         * The prefix has to be declared on the document with
         * {@link Rss.Builder#namespace(String, String)}.
         *
         * @param prefixedName the element name including its prefix, for example
         *     {@code content:encoded}
         * @param value the element text, plain or CDATA
         */
        public Builder extension(final String prefixedName, final Value value) {
            this.extensions.put(
                    XmlNames.requireElementName(
                            Objects.requireNonNull(prefixedName, "an extension needs a name"),
                            "an extension name"),
                    Objects.requireNonNull(value, "an extension needs a value"));
            return this;
        }

        /**
         * @return the item
         * @throws IllegalStateException if neither a title nor a description is set
         * @throws IllegalArgumentException if the link has no URI scheme
         */
        public Item build() {
            if (title == null && description == null) {
                throw new IllegalStateException(
                        "an item requires a title or a description; the specification allows "
                                + "every other element to be omitted, but not both of these");
            }
            if (link != null) {
                Uris.requireScheme("item link", link.value());
            }
            return new Item(this);
        }
    }
}
