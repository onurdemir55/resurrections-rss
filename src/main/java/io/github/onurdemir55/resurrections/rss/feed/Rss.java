package io.github.onurdemir55.resurrections.rss.feed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Root {@code <rss>} element.
 * <p>
 * Instances are immutable and created through {@link #builder()}.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "rss")
public final class Rss {

    @JacksonXmlProperty(isAttribute = true, localName = "version")
    private final String version;

    @JacksonXmlProperty(localName = "channel")
    private final Channel channel;

    private Rss(final Builder builder) {
        this.version = builder.version;
        this.channel = builder.channel;
    }

    /**
     * @return a new builder for {@code <rss>}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link Rss}.
     */
    public static final class Builder {

        private String version;
        private Channel channel;

        private Builder() {
        }

        /**
         * @param version the {@code version} attribute; must be {@code "2.0"} for RSS 2.0
         */
        public Builder version(final String version) {
            this.version = version;
            return this;
        }

        public Builder channel(final Channel channel) {
            this.channel = channel;
            return this;
        }

        public Rss build() {
            return new Rss(this);
        }
    }
}
