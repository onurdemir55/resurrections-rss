package io.github.onurdemir55.resurrections.rss.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Rss 2.0 accept RFC 1123 and RFC822 time format
 * This class formats by given Date and Instant
 */
public class DateParser {

    // pubDate supported format
    public static String format_RFC1123_RFC822(final Date date) {
        OffsetDateTime offsetDateTime = date.toInstant().atOffset(ZoneOffset.UTC);
        return offsetDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    public static String format_RFC1123_RFC822(final Instant instant) {
        OffsetDateTime offsetDateTime = instant.atOffset(ZoneOffset.UTC);
        return offsetDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}
