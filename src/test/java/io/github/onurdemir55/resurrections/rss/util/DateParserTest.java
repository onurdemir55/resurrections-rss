package io.github.onurdemir55.resurrections.rss.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RSS 2.0 requires dates to follow RFC 822, with a four digit year preferred.
 */
class DateParserTest {

    /** 2002-09-07T00:00:01Z, the example used by the RSS specification. */
    private static final Instant SPEC_EXAMPLE = Instant.parse("2002-09-07T00:00:01Z");

    @Test
    @DisplayName("formats an Instant as RFC 1123 in GMT")
    void formatsInstant() {
        String formatted = DateParser.format_RFC1123_RFC822(SPEC_EXAMPLE);

        assertAll(
                () -> assertTrue(formatted.startsWith("Sat, "), () -> formatted),
                () -> assertTrue(formatted.contains("Sep 2002"), () -> formatted),
                () -> assertTrue(formatted.endsWith("GMT"), () -> formatted),
                () -> assertTrue(formatted.contains("00:00:01"), () -> formatted));
    }

    @Test
    @DisplayName("the Date and Instant overloads agree")
    void overloadsAgree() {
        String fromInstant = DateParser.format_RFC1123_RFC822(SPEC_EXAMPLE);
        String fromDate = DateParser.format_RFC1123_RFC822(Date.from(SPEC_EXAMPLE));

        assertEquals(fromInstant, fromDate);
    }

    @Test
    @DisplayName("the result is always GMT, regardless of the default time zone")
    void alwaysGmt() {
        String formatted = DateParser.format_RFC1123_RFC822(Instant.parse("2021-10-09T20:38:50Z"));

        assertEquals("Sat, 9 Oct 2021 20:38:50 GMT", formatted);
    }

    @Test
    @DisplayName("the year is four digits, as the specification prefers")
    void fourDigitYear() {
        String formatted = DateParser.format_RFC1123_RFC822(SPEC_EXAMPLE);

        assertTrue(formatted.contains("2002"), () -> formatted);
    }

    /**
     * Characterization test. RFC 822 allows a one or two digit day, so this output is
     * valid, but every example in the RSS specification pads to two digits
     * ({@code Sat, 07 Sep 2002}) and that is the common convention in the wild.
     * Aligning with it is tracked as a later correctness change; this test records
     * what the library does today so the change is visible when it happens.
     */
    @Test
    @DisplayName("CURRENT BEHAVIOUR: single digit days are not zero padded")
    void singleDigitDayIsNotPadded() {
        String formatted = DateParser.format_RFC1123_RFC822(SPEC_EXAMPLE);

        assertEquals("Sat, 7 Sep 2002 00:00:01 GMT", formatted);
    }

    @Test
    @DisplayName("two digit days are unaffected")
    void twoDigitDay() {
        String formatted = DateParser.format_RFC1123_RFC822(Instant.parse("2002-09-17T12:30:00Z"));

        assertEquals("Tue, 17 Sep 2002 12:30:00 GMT", formatted);
    }
}
