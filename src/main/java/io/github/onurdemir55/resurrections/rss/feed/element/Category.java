package io.github.onurdemir55.resurrections.rss.feed.element;

import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.PlainValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import io.github.onurdemir55.resurrections.rss.util.XmlText;

import java.util.Objects;

/**
 * A {@code <category>} element, valid inside both a channel and an item.
 * <p>
 * The value identifies a location in a taxonomy, as a forward-slash separated string. The
 * optional {@code domain} attribute names the taxonomy, so the same value may legitimately
 * appear more than once under different domains.
 * <p>
 * The text is a {@link Value}, so it can be plain or CDATA like any other element.
 *
 * @param value the category, for example {@code MSFT} or {@code Top/News/Sports}
 * @param domain the taxonomy the value belongs to, or {@code null} to omit the attribute
 */
public record Category(Value value,
                       String domain) {

    public Category {
        Objects.requireNonNull(value, "category value is required");
        XmlText.requireWritableOrNull(domain, "category domain");
    }

    /**
     * A plain text category with no taxonomy.
     *
     * @param value the category
     * @return the category
     */
    public static Category of(final String value) {
        return new Category(new PlainValue(value), null);
    }

    /**
     * A plain text category within a named taxonomy.
     *
     * @param value the category
     * @param domain the taxonomy, for example {@code http://www.fool.com/cusips}
     * @return the category
     */
    public static Category of(final String value, final String domain) {
        return new Category(new PlainValue(value), domain);
    }

    /**
     * A category whose text is wrapped in a CDATA section.
     *
     * @param value the category
     * @return the category
     */
    public static Category cdata(final String value) {
        return new Category(new CDATAValue(value), null);
    }

    /**
     * A category within a named taxonomy, whose text is wrapped in a CDATA section.
     *
     * @param value the category
     * @param domain the taxonomy
     * @return the category
     */
    public static Category cdata(final String value, final String domain) {
        return new Category(new CDATAValue(value), domain);
    }
}
