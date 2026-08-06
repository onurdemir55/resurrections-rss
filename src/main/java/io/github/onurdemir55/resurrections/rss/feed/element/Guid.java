package io.github.onurdemir55.resurrections.rss.feed.element;

import io.github.onurdemir55.resurrections.rss.feed.holder.CDATAValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;

import java.util.Objects;

/**
 * The {@code <guid>} sub-element of an item: a string that uniquely identifies it.
 * <p>
 * There are no rules for the syntax of a guid; readers must treat it as an opaque string.
 * The optional {@code isPermaLink} attribute says whether it may also be opened in a
 * browser. Its default is {@code true}, so leaving it unset is not the same as setting it
 * to {@code false}.
 *
 * @param value the identifier, plain or CDATA
 * @param isPermaLink whether the identifier is a URL pointing at the item, or {@code null}
 *     to omit the attribute and let readers assume the default of {@code true}
 */
public record Guid(Value value,
                   Boolean isPermaLink) {

    public Guid {
        Objects.requireNonNull(value, "guid value is required");
    }

    /**
     * A plain text guid with no {@code isPermaLink} attribute. Readers assume it is a
     * permalink, which is the specification's default.
     *
     * @param value the identifier
     * @return the guid
     */
    public static Guid of(final String value) {
        return new Guid(new SimpleValue(value), null);
    }

    /**
     * A plain text guid that states explicitly whether it is a permalink.
     *
     * @param value the identifier
     * @param isPermaLink {@code false} when the value is not a URL, or not a URL pointing
     *     at this item
     * @return the guid
     */
    public static Guid of(final String value, final boolean isPermaLink) {
        return new Guid(new SimpleValue(value), isPermaLink);
    }

    /**
     * A guid whose text is wrapped in a CDATA section.
     *
     * @param value the identifier
     * @return the guid
     */
    public static Guid cdata(final String value) {
        return new Guid(new CDATAValue(value), null);
    }

    /**
     * A guid wrapped in a CDATA section, stating explicitly whether it is a permalink.
     *
     * @param value the identifier
     * @param isPermaLink whether the value is a URL pointing at this item
     * @return the guid
     */
    public static Guid cdata(final String value, final boolean isPermaLink) {
        return new Guid(new CDATAValue(value), isPermaLink);
    }
}
