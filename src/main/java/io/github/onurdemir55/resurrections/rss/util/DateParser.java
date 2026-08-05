package io.github.onurdemir55.resurrections.rss.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * Formats dates the way RSS 2.0 wants them.
 * <p>
 * The specification requires the RFC 822 date and time format, with a four digit year
 * preferred, and every example in it pads the day to two digits:
 * {@code Sat, 07 Sep 2002 00:00:01 GMT}.
 */
public final class DateParser {

    /**
     * {@code Locale.ENGLISH} is not optional. The day and month names are part of the wire
     * format, so with the default locale a Turkish or German system would emit
     * {@code Cum, 07 Eyl 2002} and produce a feed no reader can parse.
     */
    private static final DateTimeFormatter RFC_822 =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);

    private DateParser() {
        // utility class
    }

    /**
     * Formats an instant as an RSS 2.0 date, in GMT.
     *
     * @param instant the instant to format
     * @return for example {@code Sat, 07 Sep 2002 00:00:01 GMT}
     */
    public static String formatRfc822(final Instant instant) {
        return RFC_822.format(instant.atOffset(ZoneOffset.UTC));
    }

    /**
     * Formats a date as an RSS 2.0 date, in GMT.
     *
     * @param date the date to format
     * @return for example {@code Sat, 07 Sep 2002 00:00:01 GMT}
     */
    public static String formatRfc822(final Date date) {
        return formatRfc822(date.toInstant());
    }
}
