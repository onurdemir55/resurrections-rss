package io.github.onurdemir55.resurrections.rss.feed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;

import java.util.List;

/**
 * The {@code <channel>} element of an RSS feed.
 * <p>
 * Instances are immutable and created through {@link #builder()}.
 */
@JsonPropertyOrder({"title", "link", "description", "language", "pubDate", "item"})
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

    @JacksonXmlProperty(localName = "pubDate")
    private final Value pubDate;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    private final List<Item> items;

    private Channel(final Builder builder) {
        this.title = builder.title;
        this.link = builder.link;
        this.description = builder.description;
        this.language = builder.language;
        this.pubDate = builder.pubDate;
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
        private Value pubDate;
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

        public Builder language(final Value language) {
            this.language = language;
            return this;
        }

        public Builder pubDate(final Value pubDate) {
            this.pubDate = pubDate;
            return this;
        }

        public Builder items(final List<Item> items) {
            this.items = items;
            return this;
        }

        public Channel build() {
            return new Channel(this);
        }
    }
}
