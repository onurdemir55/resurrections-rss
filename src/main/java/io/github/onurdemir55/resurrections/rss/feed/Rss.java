package io.github.onurdemir55.resurrections.rss.feed;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.github.onurdemir55.resurrections.rss.feed.element.AtomLink;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
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

    /** Namespace prefix to URI, written as {@code xmlns:prefix} attributes. */
    private final Map<String, String> namespaces;

    private Rss(final Builder builder) {
        this.version = builder.version;
        this.channel = builder.channel;
        // LinkedHashMap, not Map.copyOf: the output order must be the order they
        // were declared in, so the same feed serializes the same way every time.
        this.namespaces = Collections.unmodifiableMap(new LinkedHashMap<>(builder.namespaces));
    }

    /**
     * The namespace declarations, as attributes. An element carrying a prefix is not
     * well-formed unless its prefix is declared, so these belong on the root element.
     * Package-private: this is a serialization hook for Jackson, called once per write.
     * Use {@link #getNamespaces()} to inspect a built document.
     *
     * @return {@code xmlns:prefix} to namespace URI
     */
    @JsonAnyGetter
    @JacksonXmlProperty(isAttribute = true)
    Map<String, String> namespaceDeclarations() {
        Map<String, String> declarations = new LinkedHashMap<>();
        namespaces.forEach((prefix, uri) -> declarations.put("xmlns:" + prefix, uri));
        return declarations;
    }


    /**
     * @return the {@code version} attribute, always {@code "2.0"}
     */
    public String getVersion() {
        return version;
    }
    /**
     * @return the channel
     */
    public Channel getChannel() {
        return channel;
    }
    /**
     * The declared namespaces, prefix to URI, without the {@code xmlns:} that appears in the
     * serialized attribute name.
     *
     * @return the declared namespaces, empty if none were declared
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
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
        private final Map<String, String> namespaces = new LinkedHashMap<>();

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

        /**
         * Declares a namespace on the document, which the specification requires before any
         * element from outside RSS may appear.
         *
         * @param prefix the prefix used on element names, for example {@code content}
         * @param uri the namespace URI
         */
        public Builder namespace(final String prefix, final String uri) {
            this.namespaces.put(
                    Objects.requireNonNull(prefix, "a namespace needs a prefix"),
                    Objects.requireNonNull(uri, "a namespace needs a URI"));
            return this;
        }

        /**
         * @param channel the channel
         */
        public Builder channel(final Channel channel) {
            this.channel = channel;
            return this;
        }

        /**
         * @return the document
         * @throws IllegalStateException if the version is not {@code "2.0"} or no channel
         *     was set
         */
        public Rss build() {
            if (!VERSION_2_0.equals(version)) {
                throw new IllegalStateException(
                        "a document conforming to RSS 2.0 must declare version=\"" + VERSION_2_0
                                + "\", and this one declares: " + version);
            }
            if (channel == null) {
                throw new IllegalStateException("rss requires a channel");
            }
            if (channel.usesAtom()) {
                namespaces.putIfAbsent(AtomLink.PREFIX, AtomLink.NAMESPACE);
            }
            return new Rss(this);
        }
    }
}
