package io.github.onurdemir55.resurrections.rss.feed.holder;

/**
 * Text content of an RSS element.
 * <p>
 * An RSS element's text is emitted either as plain text or wrapped in a CDATA section.
 * Those are the only two forms, so this type is sealed: it is deliberately not an
 * extension point. The writer chooses between the two by testing which of these a value is,
 * and it can only do that exhaustively while the set stays closed — a third implementation
 * would fall through to the plain branch and quietly lose its CDATA section.
 *
 * @see PlainValue plain text, with XML special characters escaped
 * @see CDATAValue text wrapped in a CDATA section
 */
public sealed interface Value permits PlainValue, CDATAValue {

    /**
     * The raw text content, without any XML escaping or CDATA wrapping applied.
     *
     * @return the text content, never {@code null} for values built through the constructors
     */
    String value();
}
