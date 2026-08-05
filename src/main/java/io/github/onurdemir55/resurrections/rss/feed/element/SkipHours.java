package io.github.onurdemir55.resurrections.rss.feed.element;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * The {@code <skipHours>} sub-element of a channel: hours during which a reader need not
 * poll the feed.
 * <p>
 * Each hour is a number from 0 to 23 in GMT, where 0 is the hour beginning at midnight.
 * There can be at most 24 of them, and repeating one says nothing extra, so duplicates are
 * rejected rather than silently emitted twice.
 *
 * @param hour the hours to skip
 */
public record SkipHours(@JacksonXmlElementWrapper(useWrapping = false)
                        @JacksonXmlProperty(localName = "hour") List<Integer> hour) {

    /** The last hour of the day, and so the largest allowed value. */
    public static final int MAX_HOUR = 23;

    public SkipHours {
        Objects.requireNonNull(hour, "skipHours needs at least one hour");
        if (hour.isEmpty()) {
            throw new IllegalArgumentException("skipHours needs at least one hour");
        }
        Set<Integer> seen = new LinkedHashSet<>();
        for (Integer value : hour) {
            Objects.requireNonNull(value, "an hour cannot be null");
            if (value < 0 || value > MAX_HOUR) {
                throw new IllegalArgumentException(
                        "an hour must be between 0 and " + MAX_HOUR + ", was " + value);
            }
            if (!seen.add(value)) {
                throw new IllegalArgumentException("hour " + value + " is listed twice");
            }
        }
        hour = List.copyOf(hour);
    }

    /**
     * @param hours the hours to skip, each between 0 and {@value #MAX_HOUR}
     * @return the element
     */
    public static SkipHours of(final int... hours) {
        return new SkipHours(IntStream.of(hours).boxed().toList());
    }
}
