package io.github.onurdemir55.resurrections.rss.feed.element;

import com.fasterxml.jackson.annotation.JsonValue;

import java.time.DayOfWeek;
import java.util.Objects;

/**
 * A day name as {@code <skipDays>} spells it.
 * <p>
 * This exists rather than using {@link DayOfWeek} directly because the day name is part of
 * the wire format and the specification spells it {@code Monday}, not {@code MONDAY}.
 */
public enum Day {

    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    private final String label;

    Day(final String label) {
        this.label = label;
    }

    /**
     * @return the day name as it appears in a feed, for example {@code Monday}
     */
    @JsonValue
    public String label() {
        return label;
    }

    /**
     * @param dayOfWeek the day
     * @return the matching value
     * @throws NullPointerException if {@code dayOfWeek} is {@code null}
     */
    public static Day from(final DayOfWeek dayOfWeek) {
        Objects.requireNonNull(dayOfWeek, "dayOfWeek");
        return values()[dayOfWeek.getValue() - 1];
    }
}
