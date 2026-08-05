package io.github.onurdemir55.resurrections.rss.feed.element;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The {@code <skipDays>} sub-element of a channel: days during which a reader need not poll
 * the feed.
 * <p>
 * There can be at most seven, and repeating one says nothing extra, so duplicates are
 * rejected rather than silently emitted twice.
 *
 * @param day the days to skip
 */
public record SkipDays(@JacksonXmlElementWrapper(useWrapping = false)
                       @JacksonXmlProperty(localName = "day") List<Day> day) {

    public SkipDays {
        Objects.requireNonNull(day, "skipDays needs at least one day");
        if (day.isEmpty()) {
            throw new IllegalArgumentException("skipDays needs at least one day");
        }
        Set<Day> seen = new LinkedHashSet<>();
        for (Day value : day) {
            Objects.requireNonNull(value, "a day cannot be null");
            if (!seen.add(value)) {
                throw new IllegalArgumentException(value.label() + " is listed twice");
            }
        }
        day = List.copyOf(day);
    }

    /**
     * @param days the days to skip
     * @return the element
     */
    public static SkipDays of(final Day... days) {
        return new SkipDays(Arrays.asList(days));
    }
}
