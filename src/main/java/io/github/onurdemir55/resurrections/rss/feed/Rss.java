package io.github.onurdemir55.resurrections.rss.feed;

import io.github.onurdemir55.resurrections.rss.feed.element.AtomLink;
import io.github.onurdemir55.resurrections.rss.util.XmlNames;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Root {@code <rss>} element.
 * <p>
 * Instances are immutable and created through {@link #builder()}.
 */
public final class Rss {

    /** The only version a document conforming to this specification may declare. */
    public static final String VERSION_2_0 = "2.0";

    private final String version;

    private final Channel channel;

    /** Namespace prefix to URI, written as {@code xmlns:prefix} attributes. */
    private final Map<String, String> namespaces;

    private Rss(final Builder builder, final Map<String, String> declared) {
        this.version = builder.version;
        this.channel = builder.channel;
        // LinkedHashMap, not Map.copyOf: the output order must be the order they
        // were declared in, so the same feed serializes the same way every time.
        this.namespaces = Collections.unmodifiableMap(new LinkedHashMap<>(declared));
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
         * <p>
         * The prefix becomes part of an {@code xmlns:} attribute name. An attribute value is
         * escaped on the way out, but a name is not, so the prefix is checked here rather
         * than allowed to break the document later.
         *
         * @param prefix the prefix used on element names, for example {@code content}
         * @param uri the namespace URI
         * @return this builder
         * @throws NullPointerException if either argument is {@code null}
         * @throws IllegalArgumentException if the prefix cannot be an XML name, or is one
         *     XML reserves
         */
        public Builder namespace(final String prefix, final String uri) {
            this.namespaces.put(
                    XmlNames.requireNamespacePrefix(
                            Objects.requireNonNull(prefix, "a namespace needs a prefix")),
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
         * @throws IllegalStateException if the version is not {@code "2.0"}, no channel was
         *     set, or an extension element uses a namespace prefix that was never declared
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

            // The declarations this document needs, which is what was asked for plus the Atom
            // namespace when the channel uses an Atom element. Computed into a copy rather
            // than added to the builder's own map: building must not change the builder, or
            // reusing one for a second feed would carry a declaration over to a document that
            // has no use for it.
            Map<String, String> declared = new LinkedHashMap<>(namespaces);
            if (channel.usesAtom()) {
                declared.putIfAbsent(AtomLink.PREFIX, AtomLink.NAMESPACE);
            }
            requireDeclaredPrefixes(declared);

            return new Rss(this, declared);
        }

        /**
         * An element carrying a prefix is not well-formed XML unless that prefix is declared,
         * and the declaration can only go on this element, which is why the check belongs
         * here rather than on the channel or the item.
         * <p>
         * Without it the mistake is silent: the feed is produced, looks right, and is rejected
         * by whatever finally reads it. The specification's own requirement is the same one —
         * an element it does not describe is allowed only if it is in a namespace.
         *
         * @param declared the prefixes this document will declare
         */
        private void requireDeclaredPrefixes(final Map<String, String> declared) {
            Map<String, String> used = new LinkedHashMap<>();
            channel.getExtensions().keySet()
                    .forEach(name -> used.put(name, "the channel"));
            if (channel.getItems() != null) {
                channel.getItems().forEach(item -> item.getExtensions().keySet()
                        .forEach(name -> used.putIfAbsent(name, "an item")));
            }

            for (Map.Entry<String, String> entry : used.entrySet()) {
                String name = entry.getKey();
                int colon = name.indexOf(':');
                if (colon < 0) {
                    throw new IllegalStateException(
                            "the extension <" + name + "> on " + entry.getValue()
                                    + " needs a namespace prefix; the specification allows an "
                                    + "element it does not describe only inside a namespace");
                }
                String prefix = name.substring(0, colon);
                if (!declared.containsKey(prefix)) {
                    throw new IllegalStateException(
                            "the extension <" + name + "> on " + entry.getValue() + " uses the "
                                    + "prefix \"" + prefix + "\", which was never declared; call "
                                    + "namespace(\"" + prefix + "\", uri) on this builder");
                }
            }
        }
    }
}
