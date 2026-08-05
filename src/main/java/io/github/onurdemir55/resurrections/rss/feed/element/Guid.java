package io.github.onurdemir55.resurrections.rss.feed.element;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import java.util.Objects;

/**
 * The {@code <guid>} sub-element of an item: a string that uniquely identifies it.
 * <p>
 * There are no rules for the syntax of a guid; readers must treat it as an opaque string.
 * The optional {@code isPermaLink} attribute says whether it may also be opened in a
 * browser. Its default is {@code true}, so leaving it unset is not the same as setting it
 * to {@code false}.
 *
 * @param value the identifier
 * @param isPermaLink whether the identifier is a URL pointing at the item, or {@code null}
 *     to omit the attribute and let readers assume the default of {@code true}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Guid(@JacksonXmlText String value,
                   @JacksonXmlProperty(isAttribute = true, localName = "isPermaLink")
                   Boolean isPermaLink) {

    public Guid {
        Objects.requireNonNull(value, "guid value is required");
    }

    /**
     * A guid with no {@code isPermaLink} attribute. Readers assume it is a permalink,
     * which is the specification's default.
     *
     * @param value the identifier
     * @return the guid
     */
    public static Guid of(final String value) {
        return new Guid(value, null);
    }

    /**
     * A guid that states explicitly whether it is a permalink.
     *
     * @param value the identifier
     * @param isPermaLink {@code false} when the value is not a URL, or not a URL pointing
     *     at this item
     * @return the guid
     */
    public static Guid of(final String value, final boolean isPermaLink) {
        return new Guid(value, isPermaLink);
    }
}
