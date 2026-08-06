package io.github.onurdemir55.resurrections.rss.feed;

import io.github.onurdemir55.resurrections.rss.feed.element.AtomLink;
import io.github.onurdemir55.resurrections.rss.feed.element.Category;
import io.github.onurdemir55.resurrections.rss.feed.element.Cloud;
import io.github.onurdemir55.resurrections.rss.feed.element.Image;
import io.github.onurdemir55.resurrections.rss.feed.element.SkipDays;
import io.github.onurdemir55.resurrections.rss.feed.element.SkipHours;
import io.github.onurdemir55.resurrections.rss.feed.element.TextInput;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import io.github.onurdemir55.resurrections.rss.util.Uris;
import io.github.onurdemir55.resurrections.rss.util.XmlNames;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The {@code <channel>} element of an RSS feed.
 * <p>
 * {@code title}, {@code link} and {@code description} are required by the specification;
 * everything else is optional. Elements are emitted in the order the specification lists
 * them, with the items last.
 * <p>
 * Instances are immutable and created through {@link #builder()}.
 */
public final class Channel {

    private final Value title;

    private final Value link;

    private final Value description;

    private final AtomLink atomLink;

    private final Value language;

    private final Value copyright;

    private final Value managingEditor;

    private final Value webMaster;

    private final Value pubDate;

    private final Value lastBuildDate;

    private final List<Category> category;

    private final Value generator;

    private final Value docs;

    private final Cloud cloud;

    private final Integer ttl;

    private final Image image;

    private final Value rating;

    private final TextInput textInput;

    private final SkipHours skipHours;

    private final SkipDays skipDays;

    private final List<Item> items;

    /**
     * Elements from other namespaces, keyed by their prefixed name. The specification allows
     * elements it does not describe only when they are defined in a namespace.
     */
    private final Map<String, Value> extensions;

    private Channel(final Builder builder) {
        this.title = builder.title;
        this.link = builder.link;
        this.description = builder.description;
        this.atomLink = builder.atomLink;
        this.language = builder.language;
        this.copyright = builder.copyright;
        this.managingEditor = builder.managingEditor;
        this.webMaster = builder.webMaster;
        this.pubDate = builder.pubDate;
        this.lastBuildDate = builder.lastBuildDate;
        this.category = builder.category;
        this.generator = builder.generator;
        this.docs = builder.docs;
        this.cloud = builder.cloud;
        this.ttl = builder.ttl;
        this.image = builder.image;
        this.rating = builder.rating;
        this.textInput = builder.textInput;
        this.skipHours = builder.skipHours;
        this.skipDays = builder.skipDays;
        this.items = builder.items;
        // LinkedHashMap, not Map.copyOf: the output order must be the order they
        // were declared in, so the same feed serializes the same way every time.
        this.extensions = Collections.unmodifiableMap(new LinkedHashMap<>(builder.extensions));
    }

    /**
     * Extension elements, keyed by the prefixed name they were registered under and written
     * in the order they were added.
     * <p>
     * These used to be hidden because they existed for a serialization framework to find.
     * Nothing reads them by reflection any more, so they are a normal part of the model like
     * every other element.
     *
     * @return the extension elements, never {@code null} and possibly empty
     */
    public Map<String, Value> getExtensions() {
        return extensions;
    }

    /**
     * @return whether this channel carries an Atom element, and so needs the Atom namespace
     *     declared on the document. Called by {@link Rss.Builder#build()}, which declares
     *     {@code xmlns:atom} automatically when this is {@code true} and no explicit
     *     declaration for that prefix was already made.
     */
    boolean usesAtom() {
        return atomLink != null;
    }

    /**
     * @return the channel title, never {@code null}
     */
    public Value getTitle() {
        return title;
    }

    /**
     * @return the channel link, never {@code null}
     */
    public Value getLink() {
        return link;
    }

    /**
     * @return the channel description, never {@code null}
     */
    public Value getDescription() {
        return description;
    }

    /**
     * @return the Atom self link, or {@code null}
     */
    public AtomLink getAtomLink() {
        return atomLink;
    }

    /**
     * @return the language code, or {@code null}
     */
    public Value getLanguage() {
        return language;
    }

    /**
     * @return the copyright notice, or {@code null}
     */
    public Value getCopyright() {
        return copyright;
    }

    /**
     * @return the editor's email address, or {@code null}
     */
    public Value getManagingEditor() {
        return managingEditor;
    }

    /**
     * @return the webmaster's email address, or {@code null}
     */
    public Value getWebMaster() {
        return webMaster;
    }

    /**
     * @return the publication date, or {@code null}
     */
    public Value getPubDate() {
        return pubDate;
    }

    /**
     * @return the last modification date, or {@code null}
     */
    public Value getLastBuildDate() {
        return lastBuildDate;
    }

    /**
     * @return the categories, or {@code null}
     */
    public List<Category> getCategory() {
        return category;
    }

    /**
     * @return the generating program, or {@code null}
     */
    public Value getGenerator() {
        return generator;
    }

    /**
     * @return the documentation url, or {@code null}
     */
    public Value getDocs() {
        return docs;
    }

    /**
     * @return the update notification service, or {@code null}
     */
    public Cloud getCloud() {
        return cloud;
    }

    /**
     * @return the cache lifetime in minutes, or {@code null}
     */
    public Integer getTtl() {
        return ttl;
    }

    /**
     * @return the channel image, or {@code null}
     */
    public Image getImage() {
        return image;
    }

    /**
     * @return the PICS rating, or {@code null}
     */
    public Value getRating() {
        return rating;
    }

    /**
     * @return the input box, or {@code null}
     */
    public TextInput getTextInput() {
        return textInput;
    }

    /**
     * @return the hours readers may skip, or {@code null}
     */
    public SkipHours getSkipHours() {
        return skipHours;
    }

    /**
     * @return the days readers may skip, or {@code null}
     */
    public SkipDays getSkipDays() {
        return skipDays;
    }

    /**
     * @return the items, or {@code null}
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * @return a new builder for {@code <channel>}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link Channel}.
     */
    public static final class Builder {

        private Value title;
        private Value link;
        private Value description;
        private AtomLink atomLink;
        private Value language;
        private Value copyright;
        private Value managingEditor;
        private Value webMaster;
        private Value pubDate;
        private Value lastBuildDate;
        private List<Category> category;
        private Value generator;
        private Value docs;
        private Cloud cloud;
        private Integer ttl;
        private Image image;
        private Value rating;
        private TextInput textInput;
        private SkipHours skipHours;
        private SkipDays skipDays;
        private List<Item> items;
        private final Map<String, Value> extensions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * @param title the channel title
         */
        public Builder title(final Value title) {
            this.title = title;
            return this;
        }

        /**
         * @param link the channel link
         */
        public Builder link(final Value link) {
            this.link = link;
            return this;
        }

        /**
         * @param description the channel description
         */
        public Builder description(final Value description) {
            this.description = description;
            return this;
        }

        /**
         * The {@code atom:link} the Best Practices Profile recommends, pointing at the feed's
         * own address. Setting it makes the document declare {@code xmlns:atom} on its own.
         *
         * @param atomLink the link
         */
        public Builder atomLink(final AtomLink atomLink) {
            this.atomLink = atomLink;
            return this;
        }

        /**
         * Adds an element from another namespace, which is the only kind of element the
         * specification permits beyond the ones it describes.
         * <p>
         * The prefix has to be declared on the document with
         * {@link Rss.Builder#namespace(String, String)}, otherwise the result is not
         * well-formed XML.
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
         * The language the channel is written in, as a language code such as {@code en-us}.
         *
         * @param language the language code
         */
        public Builder language(final Value language) {
            this.language = language;
            return this;
        }

        /**
         * Copyright notice for the content in the channel.
         *
         * @param copyright the notice
         */
        public Builder copyright(final Value copyright) {
            this.copyright = copyright;
            return this;
        }

        /**
         * Email address of the person responsible for editorial content.
         *
         * @param managingEditor the editor's email address
         */
        public Builder managingEditor(final Value managingEditor) {
            this.managingEditor = managingEditor;
            return this;
        }

        /**
         * Email address of the person responsible for technical issues with the channel.
         *
         * @param webMaster the webmaster's email address
         */
        public Builder webMaster(final Value webMaster) {
            this.webMaster = webMaster;
            return this;
        }

        /**
         * @param pubDate the publication date for the content in the channel
         */
        public Builder pubDate(final Value pubDate) {
            this.pubDate = pubDate;
            return this;
        }

        /**
         * The last time the content of the channel changed.
         *
         * @param lastBuildDate an RFC 822 date
         */
        public Builder lastBuildDate(final Value lastBuildDate) {
            this.lastBuildDate = lastBuildDate;
            return this;
        }

        /**
         * Categories the channel belongs to.
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
         * The program that generated the channel.
         *
         * @param generator the program name and version
         */
        public Builder generator(final Value generator) {
            this.generator = generator;
            return this;
        }

        /**
         * A url pointing at documentation for the format, so that someone who stumbles across
         * the file can find out what it is.
         *
         * @param docs the documentation url
         */
        public Builder docs(final Value docs) {
            this.docs = docs;
            return this;
        }

        /**
         * A service readers can register with to be notified of updates, instead of polling.
         *
         * @param cloud the service
         */
        public Builder cloud(final Cloud cloud) {
            this.cloud = cloud;
            return this;
        }

        /**
         * How many minutes a reader may cache the channel before refreshing it.
         *
         * @param ttl the cache lifetime in minutes
         */
        public Builder ttl(final Integer ttl) {
            this.ttl = ttl;
            return this;
        }

        /**
         * An image that can be displayed with the channel.
         *
         * @param image the image
         */
        public Builder image(final Image image) {
            this.image = image;
            return this;
        }

        /**
         * The PICS rating for the channel.
         *
         * @param rating the rating
         */
        public Builder rating(final Value rating) {
            this.rating = rating;
            return this;
        }

        /**
         * An input box a reader may display with the channel.
         *
         * @param textInput the input box
         */
        public Builder textInput(final TextInput textInput) {
            this.textInput = textInput;
            return this;
        }

        /**
         * Hours during which readers need not poll the feed.
         *
         * @param skipHours the hours
         */
        public Builder skipHours(final SkipHours skipHours) {
            this.skipHours = skipHours;
            return this;
        }

        /**
         * Days during which readers need not poll the feed.
         *
         * @param skipDays the days
         */
        public Builder skipDays(final SkipDays skipDays) {
            this.skipDays = skipDays;
            return this;
        }

        /**
         * @param items the items in this channel
         */
        public Builder items(final List<Item> items) {
            this.items = items == null ? null : List.copyOf(items);
            return this;
        }

        /**
         * Convenience form of {@link #items(List)}.
         *
         * @param items the items
         */
        public Builder items(final Item... items) {
            this.items = List.of(items);
            return this;
        }

        /**
         * @return the channel
         * @throws IllegalStateException if a required element is missing
         * @throws IllegalArgumentException if the link has no URI scheme
         */
        public Channel build() {
            requirePresent("title", title);
            requirePresent("link", link);
            requirePresent("description", description);
            Uris.requireScheme("channel link", link.value());
            return new Channel(this);
        }

        private static void requirePresent(final String element, final Value value) {
            if (value == null) {
                throw new IllegalStateException(
                        "channel requires " + element + "; the specification lists title, link "
                                + "and description as required elements");
            }
        }
    }
}
