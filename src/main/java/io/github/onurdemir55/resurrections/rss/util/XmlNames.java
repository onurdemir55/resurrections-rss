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
     * A name with no colon in it. The rule is the {@code NameStartChar} and {@code NameChar}
     * productions XML gives, not a Unicode category test: the two are close enough to be
     * mistaken for each other and different enough to matter.
     */
    private static void requireNcName(final String part, final String whole, final String what) {
        if (part.isEmpty()) {
            throw new IllegalArgumentException(
                    what + " must not have an empty prefix or local name, but was \"" + whole + "\"");
        }
        if (!isNameStart(part.charAt(0))) {
            throw new IllegalArgumentException(String.format(
                    "%s must begin with a letter or underscore, but \"%s\" begins with U+%04X",
                    what, whole, (int) part.charAt(0)));
        }
        for (int i = 1; i < part.length(); i++) {
            if (!isNamePart(part.charAt(i))) {
                throw new IllegalArgumentException(String.format(
                        "%s may only contain letters, digits, hyphens, dots and underscores, "
                                + "but \"%s\" contains U+%04X",
                        what, whole, (int) part.charAt(i)));
            }
        }
    }

    /**
     * The {@code NameStartChar} production of XML, narrowed to ASCII.
     * <p>
     * Narrowed deliberately, and this is the interesting decision in the class. A generator is
     * only useful if what it emits is accepted on the other side, and the two are not the same
     * question as whether a name is legal. XML's fifth edition widened what may start a name;
     * the parser that ships with the JDK implements the fourth, and rejects part of what the
     * fifth added - {@code U+02B0}, {@code U+2113} and {@code U+FF10} were all measured being
     * written happily here and then refused by {@code DocumentBuilder}. Matching the current
     * edition exactly would mean knowingly producing feeds that a very widely deployed reader
     * will not parse.
     * <p>
     * So names are held to the subset every parser agrees on. Nothing is lost in practice: an
     * extension element name comes from a published module and every module in use spells its
     * names in ASCII - dc, content, itunes, media, slash, sy, georss, wfw, admin. A caller who
     * wants a name outside this gets a clear refusal rather than a feed that some readers drop.
     * <p>
     * This restriction is on names only. Element text may contain any character XML can
     * represent, which is the whole point of {@link XmlText}.
     *
     * @see <a href="https://www.w3.org/TR/xml/#NT-NameStartChar">XML 1.0, NameStartChar</a>
     */
    private static boolean isNameStart(final char c) {
        return c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_';
    }

    /**
     * The {@code NameChar} production, narrowed the same way: what a name may start with, plus
     * the hyphen, the dot and the ASCII digits.
     * <p>
     * Not every digit Unicode knows about. A fullwidth zero is a digit to
     * {@link Character#isLetterOrDigit}, which is what this used to ask, and is not a character
     * the JDK's parser will accept in a name.
     *
     * @see <a href="https://www.w3.org/TR/xml/#NT-NameChar">XML 1.0, NameChar</a>
     */
    private static boolean isNamePart(final char c) {
        return isNameStart(c)
                || c >= '0' && c <= '9'
                || c == '-'
                || c == '.';
    }
}
