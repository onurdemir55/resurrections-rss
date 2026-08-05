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

    /** The only version a document conforming to this specification may declare. */
    public static final String VERSION_2_0 = "2.0";

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

        /** RSS 2.0 is the only version this library emits, so it is the default. */
        private String version = VERSION_2_0;
        private Channel channel;

        private Builder() {
        }

        /**
         * Overrides the {@code version} attribute. Defaults to {@code "2.0"}, which is what
         * the specification requires for a document that conforms to it.
         *
         * @param version the value of the {@code version} attribute
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
            if (!VERSION_2_0.equals(version)) {
                throw new IllegalStateException(
                        "a document conforming to RSS 2.0 must declare version=\"" + VERSION_2_0
                                + "\", and this one declares: " + version);
            }
            if (channel == null) {
                throw new IllegalStateException("rss requires a channel");
            }
            return new Rss(this);
        }
    }
}
