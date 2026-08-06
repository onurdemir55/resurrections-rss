package io.github.onurdemir55.resurrections.rss.feed.element;

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
 * <p>
 * The checks look much like the ones in {@link SkipDays} and were once shared with them. They
 * were put back: the extracted version needed a function parameter to build one message, and it
 * hid the {@code List.copyOf} far enough away that static analysis could no longer prove the
 * list handed out is immutable. Ten lines of plain validation, read in the place they apply, are
 * worth more than one abstraction over two callers.
 *
 * @param hours the hours to skip
 */
public record SkipHours(List<Integer> hours) {

    /** The last hour of the day, and so the largest allowed value. */
    public static final int MAX_HOUR = 23;

    public SkipHours {
        Objects.requireNonNull(hours, "the hour list cannot be null");
        if (hours.isEmpty()) {
            throw new IllegalArgumentException("skipHours needs at least one hour");
        }
        Set<Integer> seen = new LinkedHashSet<>();
        for (Integer hour : hours) {
            Objects.requireNonNull(hour, "an hour cannot be null");
            if (hour < 0 || hour > MAX_HOUR) {
                throw new IllegalArgumentException(
                        "an hour must be between 0 and " + MAX_HOUR + ", was " + hour);
            }
            if (!seen.add(hour)) {
                throw new IllegalArgumentException("hour " + hour + " is listed twice");
            }
        }
        hours = List.copyOf(hours);
    }

    /**
     * @param hours the hours to skip, each between 0 and {@value #MAX_HOUR}
     * @return the element
     */
    public static SkipHours of(final int... hours) {
        return new SkipHours(IntStream.of(hours).boxed().toList());
    }
}
