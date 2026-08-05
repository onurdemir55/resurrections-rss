package io.github.onurdemir55.resurrections.rss.feed.holder;

/**
 * Text content of an RSS element.
 * <p>
 * An RSS element's text is emitted either as plain text or wrapped in a CDATA section.
 * Those are the only two forms, so this type is sealed: it is deliberately not an
 * extension point. A hand-rolled implementation without the correct Jackson annotations
 * would silently produce malformed XML, so the set of permitted forms is closed.
 *
 * @see SimpleValue plain text, with XML special characters escaped
 * @see CDATAValue text wrapped in a CDATA section
 */
public sealed interface Value permits SimpleValue, CDATAValue {

    /**
     * The raw text content, without any XML escaping or CDATA wrapping applied.
     *
     * @return the text content, never {@code null} for values built through the constructors
     */
    String value();
}
