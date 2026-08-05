package io.github.onurdemir55.resurrections.rss.util;

import java.util.Locale;

/**
 * The URI checks the RSS 2.0 specification requires.
 * <p>
 * The specification restricts the first non-whitespace characters of the data in
 * {@code <link>} and {@code <url>} elements: they must begin with an IANA-registered URI
 * scheme such as {@code http://}, {@code https://}, {@code mailto:}, {@code ftp://} or
 * {@code news://}.
 * <p>
 * This deliberately checks that a scheme is present rather than checking it against the
 * IANA register. That register holds hundreds of schemes and keeps growing, so embedding a
 * copy would mean maintaining it and, worse, rejecting a legitimate but unusual scheme.
 * A missing scheme is the mistake worth catching.
 */
public final class Uris {

    private Uris() {
        // utility class
    }

    /**
     * Checks that a value carries a URI scheme.
     *
     * @param element the element or attribute being checked, used in the error message
     * @param value the value to check
     * @return the value, unchanged
     * @throws IllegalArgumentException if the value has no scheme
     */
    public static String requireScheme(final String element, final String value) {
        if (scheme(value) == null) {
            throw new IllegalArgumentException(element
                    + " must begin with a URI scheme, such as https://, and was: " + value);
        }
        return value;
    }

    /**
     * Checks that a value is an HTTP URL. The specification says so explicitly for an
     * enclosure; {@code https} is accepted alongside {@code http}.
     *
     * @param element the element or attribute being checked, used in the error message
     * @param value the value to check
     * @return the value, unchanged
     * @throws IllegalArgumentException if the value is not an http or https URL
     */
    public static String requireHttpScheme(final String element, final String value) {
        String scheme = scheme(value);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException(element
                    + " must be an http or https url, and was: " + value);
        }
        return value;
    }

    /**
     * @param value a candidate URI
     * @return the scheme in lower case, or {@code null} when there is none
     */
    private static String scheme(final String value) {
        if (value == null) {
            return null;
        }
        int colon = value.indexOf(':');
        if (colon < 1) {
            return null;
        }
        // A scheme is a letter followed by letters, digits, '+', '-' or '.'
        if (!Character.isLetter(value.charAt(0))) {
            return null;
        }
        for (int i = 1; i < colon; i++) {
            char c = value.charAt(i);
            boolean allowed = Character.isLetterOrDigit(c) || c == '+' || c == '-' || c == '.';
            if (!allowed) {
                return null;
            }
        }
        return value.substring(0, colon).toLowerCase(Locale.ROOT);
    }
}
