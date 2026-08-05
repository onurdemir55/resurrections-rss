package io.github.onurdemir55.resurrections.rss.feed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;

import java.util.Arrays;
import java.util.List;

/**
 * An {@code <item>} element of a {@link Channel}.
 * <p>
 * Instances are immutable and created through {@link #builder()}.
 */
@JsonPropertyOrder({"title", "link", "description", "category", "pubDate"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "item")
public final class Item {

    @JacksonXmlProperty(localName = "title")
    private final Value title;

    @JacksonXmlProperty(localName = "link")
    private final Value link;

    @JacksonXmlProperty(localName = "description")
    private final Value description;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "category")
    private final List<Value> category;

    @JacksonXmlProperty(localName = "pubDate")
    private final Value pubDate;

    private Item(final Builder builder) {
        this.title = builder.title;
        this.link = builder.link;
        this.description = builder.description;
        this.category = builder.category;
        this.pubDate = builder.pubDate;
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
        private List<Value> category;
        private Value pubDate;

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
         * Categories for this item, emitted as one {@code <category>} element each, in the
         * order given.
         * <p>
         * This takes a {@code List} rather than a {@code Set} on purpose. A set left the
         * output order undefined, and it silently dropped categories that share the same
         * text, which the specification explicitly allows across different taxonomies.
         *
         * @param category the categories, in the order they should appear
         */
        public Builder category(final List<Value> category) {
            this.category = category;
            return this;
        }

        /**
         * Convenience form of {@link #category(List)}.
         *
         * @param category the categories, in the order they should appear
         */
        public Builder category(final Value... category) {
            this.category = Arrays.asList(category);
            return this;
        }

        public Builder pubDate(final Value pubDate) {
            this.pubDate = pubDate;
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }
}
