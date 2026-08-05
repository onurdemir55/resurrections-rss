package io.github.onurdemir55.resurrections.rss.feed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.github.onurdemir55.resurrections.rss.feed.element.Category;
import io.github.onurdemir55.resurrections.rss.feed.element.Cloud;
import io.github.onurdemir55.resurrections.rss.feed.element.Image;
import io.github.onurdemir55.resurrections.rss.feed.element.SkipDays;
import io.github.onurdemir55.resurrections.rss.feed.element.SkipHours;
import io.github.onurdemir55.resurrections.rss.feed.element.TextInput;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import io.github.onurdemir55.resurrections.rss.util.Uris;

import java.util.Arrays;
import java.util.List;

/**
 * The {@code <channel>} element of an RSS feed.
 * <p>
 * {@code title}, {@code link} and {@code description} are required by the specification;
 * everything else is optional. Elements are emitted in the order the specification lists
 * them, with the items last.
 * <p>
 * Instances are immutable and created through {@link #builder()}.
 */
@JsonPropertyOrder({"title", "link", "description", "language", "copyright", "managingEditor",
                    "webMaster", "pubDate", "lastBuildDate", "category", "generator", "docs",
                    "cloud", "ttl", "image", "rating", "textInput", "skipHours", "skipDays",
                    "item"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "channel")
public final class Channel {

    @JacksonXmlProperty(localName = "title")
    private final Value title;

    @JacksonXmlProperty(localName = "link")
    private final Value link;

    @JacksonXmlProperty(localName = "description")
    private final Value description;

    @JacksonXmlProperty(localName = "language")
    private final Value language;

    @JacksonXmlProperty(localName = "copyright")
    private final Value copyright;

    @JacksonXmlProperty(localName = "managingEditor")
    private final Value managingEditor;

    @JacksonXmlProperty(localName = "webMaster")
    private final Value webMaster;

    @JacksonXmlProperty(localName = "pubDate")
    private final Value pubDate;

    @JacksonXmlProperty(localName = "lastBuildDate")
    private final Value lastBuildDate;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "category")
    private final List<Category> category;

    @JacksonXmlProperty(localName = "generator")
    private final Value generator;

    @JacksonXmlProperty(localName = "docs")
    private final Value docs;

    @JacksonXmlProperty(localName = "cloud")
    private final Cloud cloud;

    @JacksonXmlProperty(localName = "ttl")
    private final Integer ttl;

    @JacksonXmlProperty(localName = "image")
    private final Image image;

    @JacksonXmlProperty(localName = "rating")
    private final Value rating;

    @JacksonXmlProperty(localName = "textInput")
    private final TextInput textInput;

    @JacksonXmlProperty(localName = "skipHours")
    private final SkipHours skipHours;

    @JacksonXmlProperty(localName = "skipDays")
    private final SkipDays skipDays;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    private final List<Item> items;

    private Channel(final Builder builder) {
        this.title = builder.title;
        this.link = builder.link;
        this.description = builder.description;
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

        private Builder() {
        }

        public Builder title(final Value title) {
            this.title = title;
            return this;
        }

        public Builder link(final Value link) {
            this.link = link;
            return this;
        }

        public Builder description(final Value description) {
            this.description = description;
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
            this.category = category;
            return this;
        }

        /**
         * Convenience form of {@link #category(List)}.
         *
         * @param category the categories
         */
        public Builder category(final Category... category) {
            this.category = Arrays.asList(category);
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

        public Builder items(final List<Item> items) {
            this.items = items;
            return this;
        }

        /**
         * Convenience form of {@link #items(List)}.
         *
         * @param items the items
         */
        public Builder items(final Item... items) {
            this.items = Arrays.asList(items);
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
