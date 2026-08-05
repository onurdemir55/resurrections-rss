package io.github.onurdemir55.resurrections.rss.feed.element;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import java.util.Objects;

/**
 * A {@code <category>} element, valid inside both a channel and an item.
 * <p>
 * The value identifies a location in a taxonomy, as a forward-slash separated string. The
 * optional {@code domain} attribute names the taxonomy, so the same value may legitimately
 * appear more than once under different domains.
 *
 * @param value the category, for example {@code MSFT} or {@code Top/News/Sports}
 * @param domain the taxonomy the value belongs to, or {@code null} to omit the attribute
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Category(@JacksonXmlText String value,
                       @JacksonXmlProperty(isAttribute = true, localName = "domain") String domain) {

    public Category {
        Objects.requireNonNull(value, "category value is required");
    }

    /**
     * A category with no taxonomy.
     *
     * @param value the category
     * @return the category
     */
    public static Category of(final String value) {
        return new Category(value, null);
    }

    /**
     * A category within a named taxonomy.
     *
     * @param value the category
     * @param domain the taxonomy, for example {@code http://www.fool.com/cusips}
     * @return the category
     */
    public static Category of(final String value, final String domain) {
        return new Category(value, domain);
    }
}
