package io.github.onurdemir55.resurrections.rss.feed.element;

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
 * @param days the days to skip
 * @see SkipHours for why these checks are not shared with the hours
 */
public record SkipDays(List<Day> days) {

    public SkipDays {
        Objects.requireNonNull(days, "the day list cannot be null");
        if (days.isEmpty()) {
            throw new IllegalArgumentException("skipDays needs at least one day");
        }
        Set<Day> seen = new LinkedHashSet<>();
        for (Day day : days) {
            Objects.requireNonNull(day, "a day cannot be null");
            if (!seen.add(day)) {
                throw new IllegalArgumentException("day " + day.label() + " is listed twice");
            }
        }
        days = List.copyOf(days);
    }

    /**
     * @param days the days to skip
     * @return the element
     */
    public static SkipDays of(final Day... days) {
        return new SkipDays(Arrays.stream(days).toList());
    }
}
