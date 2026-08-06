package io.github.onurdemir55.resurrections.rss.util;

import java.util.Objects;
import java.util.Set;

/**
 * Checks that a name may be used as an XML element name.
 * <p>
 * Extension element names come from the caller and go straight into the document's structure,
 * where nothing can escape them: a name is markup, not text. An unchecked name therefore does
 * not produce an escaped oddity but a broken document, and a name built from someone else's
 * input could put arbitrary markup into the feed. So names are checked when the feed is built,
 * in the same place and the same way as every other rule this library enforces.
 */
public final class XmlNames {

    private XmlNames() {
        // utility class
    }

    /**
     * Prefixes XML reserves for itself. Declaring either produces an attribute no parser will
     * accept — {@code xmlns:xmlns} is illegal outright, and {@code xmlns:xml} is legal only
     * when bound to one fixed URI — and an RSS extension has no reason to want them.
     */
    private static final Set<String> RESERVED_PREFIXES = Set.of("xml", "xmlns");

    /**
     * Requires a prefix usable in an {@code xmlns:} declaration.
     *
     * @param prefix the prefix to check
     * @return {@code prefix}
     * @throws NullPointerException if {@code prefix} is {@code null}
     * @throws IllegalArgumentException if it cannot be an XML name, or XML reserves it
     */
    public static String requireNamespacePrefix(final String prefix) {
        requireElementName(prefix, "a namespace prefix");
        if (prefix.indexOf(':') >= 0) {
            throw new IllegalArgumentException(
                    "a namespace prefix must not itself contain a colon, but was \""
                            + prefix + "\"");
        }
        if (RESERVED_PREFIXES.contains(prefix)) {
            throw new IllegalArgumentException(
                    "\"" + prefix + "\" is reserved by XML and cannot be declared as a "
                            + "namespace prefix");
        }
        return prefix;
    }

    /**
     * Requires a name usable as an element name, optionally carrying a namespace prefix.
     *
     * @param name the name to check, such as {@code "content:encoded"} or {@code "title"}
     * @param what what the name is for, used in the exception message
     * @return {@code name}
     * @throws NullPointerException if {@code name} is {@code null}
     * @throws IllegalArgumentException if it cannot be an XML element name
     */
    public static String requireElementName(final String name, final String what) {
        Objects.requireNonNull(name, what + " must not be null");

        int colon = name.indexOf(':');
        if (colon < 0) {
            requireNcName(name, name, what);
            return name;
        }
        if (name.indexOf(':', colon + 1) >= 0) {
            throw new IllegalArgumentException(
                    what + " may carry at most one namespace prefix, but was \"" + name + "\"");
        }
        requireNcName(name.substring(0, colon), name, what);
        requireNcName(name.substring(colon + 1), name, what);
        return name;
    }

    /**
     * A name with no colon in it. The rule is the one XML uses: a letter or underscore to
     * begin with, then letters, digits, hyphens, dots and underscores.
     */
    private static void requireNcName(final String part, final String whole, final String what) {
        if (part.isEmpty()) {
            throw new IllegalArgumentException(
                    what + " must not have an empty prefix or local name, but was \"" + whole + "\"");
        }
        if (!isNameStart(part.charAt(0))) {
            throw new IllegalArgumentException(
                    what + " must begin with a letter or underscore, but was \"" + whole + "\"");
        }
        for (int i = 1; i < part.length(); i++) {
            if (!isNamePart(part.charAt(i))) {
                throw new IllegalArgumentException(
                        what + " may only contain letters, digits, hyphens, dots and underscores, "
                                + "but was \"" + whole + "\"");
            }
        }
    }

    private static boolean isNameStart(final char c) {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isNamePart(final char c) {
        return Character.isLetterOrDigit(c) || c == '-' || c == '.' || c == '_';
    }
}
