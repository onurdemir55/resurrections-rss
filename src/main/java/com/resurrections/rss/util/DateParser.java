package com.resurrections.rss.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
