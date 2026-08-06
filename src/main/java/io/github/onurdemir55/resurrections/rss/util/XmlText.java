package io.github.onurdemir55.resurrections.rss.util;

/**
 * Checks that text can be represented in an XML document at all.
 * <p>
 * Escaping does not help here. {@code &} becomes {@code &amp;} and a CDATA section keeps
 * markup readable, but XML 1.0 simply has no spelling for a NUL byte or most other control
 * characters: there is no escape that produces one, so text containing them cannot be written
 * whatever form it is given.
 * <p>
 * This is checked when a value is created rather than when the feed is written, for the same
 * reason every other rule in this library is checked early, plus one specific to writing: the
 * document is streamed out, so a value rejected at write time would leave a half-written feed
 * on the caller's stream. Text that arrives from a database column or an HTTP response is
 * exactly where a stray control character comes from, so the failure belongs at the point the
 * value is built, where the stack trace still says which value it was.
 *
 * @see <a href="https://www.w3.org/TR/xml/#charsets">XML 1.0, legal characters</a>
 */
public final class XmlText {

    private XmlText() {
        // utility class
    }

    /**
     * Requires text that XML can represent.
     *
     * @param text the text to check
     * @return {@code text}
     * @throws IllegalArgumentException if it contains a character XML 1.0 cannot represent
     */
    public static String requireWritable(final String text) {
        return requireWritable(text, "text");
    }

    /**
     * Requires text that XML can represent, naming what was being checked.
     *
     * @param text the text to check
     * @param what the element or attribute the text belongs to, used in the error message
     * @return {@code text}
     * @throws IllegalArgumentException if it contains a character XML 1.0 cannot represent
     */
    public static String requireWritable(final String text, final String what) {
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            if (!isLegal(codePoint)) {
                throw new IllegalArgumentException(String.format(
                        "%s contains a character XML cannot represent, U+%04X at index %d; "
                                + "no escape or CDATA section can encode it",
                        what, codePoint, i));
            }
            i += Character.charCount(codePoint);
        }
        return text;
    }

    /**
     * Requires text that XML can represent, when the text is optional.
     *
     * @param text the text to check, or {@code null}
     * @param what the element or attribute the text belongs to, used in the error message
     * @return {@code text}
     * @throws IllegalArgumentException if it contains a character XML 1.0 cannot represent
     */
    public static String requireWritableOrNull(final String text, final String what) {
        return text == null ? null : requireWritable(text, what);
    }

    /**
     * The production XML 1.0 gives for {@code Char}, split in two so each half can be read
     * against the specification rather than against operator precedence.
     */
    private static boolean isLegal(final int codePoint) {
        return isAllowedControl(codePoint) || isAllowedGraphic(codePoint);
    }

    /** Of the control characters, XML permits exactly these three. */
    private static boolean isAllowedControl(final int codePoint) {
        return codePoint == 0x9 || codePoint == 0xA || codePoint == 0xD;
    }

    /**
     * Everything from the space upwards, minus the surrogate block, which only has meaning as
     * half of a pair, and minus the two non-characters that end the basic plane.
     * <p>
     * An unpaired surrogate reaches here as a code point inside that block, because
     * {@link String#codePointAt} combines only a well-formed pair, so it is rejected too.
     */
    private static boolean isAllowedGraphic(final int codePoint) {
        return inRange(codePoint, 0x20, 0xD7FF)
                || inRange(codePoint, 0xE000, 0xFFFD)
                || inRange(codePoint, 0x10000, Character.MAX_CODE_POINT);
    }

    private static boolean inRange(final int codePoint, final int lowest, final int highest) {
        return codePoint >= lowest && codePoint <= highest;
    }
}
