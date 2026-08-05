package io.github.onurdemir55.resurrections.rss.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RSS 2.0 requires the RFC 822 date format, with a four digit year preferred.
 * <p>
 * The whole class runs with a Turkish default locale. Day and month names are part of the
 * wire format, so formatting must not follow the platform locale: a feed reading
 * {@code Cum, 07 Eyl 2002} is unparseable. Setting the locale for the class rather than a
 * single test means every assertion here also guards that.
 */
class DateParserTest {

    /** 2002-09-07T00:00:01Z, the example used by the RSS specification. */
    private static final Instant SPEC_EXAMPLE = Instant.parse("2002-09-07T00:00:01Z");

    private static final String SPEC_EXAMPLE_FORMATTED = "Sat, 07 Sep 2002 00:00:01 GMT";

    private static Locale originalLocale;

    @BeforeAll
    static void useNonEnglishLocale() {
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
    }

    @AfterAll
    static void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    @Test
    @DisplayName("matches the example in the specification exactly")
    void matchesSpecificationExample() {
        assertEquals(SPEC_EXAMPLE_FORMATTED, DateParser.formatRfc822(SPEC_EXAMPLE));
    }

    @Test
    @DisplayName("a single digit day is padded to two digits")
    void singleDigitDayIsPadded() {
        String formatted = DateParser.formatRfc822(Instant.parse("2021-10-09T20:38:50Z"));

        assertEquals("Sat, 09 Oct 2021 20:38:50 GMT", formatted);
    }

    @Test
    @DisplayName("a two digit day is unchanged")
    void twoDigitDay() {
        String formatted = DateParser.formatRfc822(Instant.parse("2002-09-17T12:30:00Z"));

        assertEquals("Tue, 17 Sep 2002 12:30:00 GMT", formatted);
    }

    @Test
    @DisplayName("day and month names stay English under a Turkish locale")
    void namesAreEnglishRegardlessOfLocale() {
        String formatted = DateParser.formatRfc822(SPEC_EXAMPLE);

        assertAll(
                () -> assertEquals("tr", Locale.getDefault().getLanguage(),
                        "the test should be running under a Turkish locale"),
                () -> assertEquals(SPEC_EXAMPLE_FORMATTED, formatted));
    }

    @Test
    @DisplayName("the Date and Instant overloads agree")
    void overloadsAgree() {
        assertEquals(
                DateParser.formatRfc822(SPEC_EXAMPLE),
                DateParser.formatRfc822(Date.from(SPEC_EXAMPLE)));
    }

    @Test
    @DisplayName("the result is always GMT, whatever the default time zone")
    void alwaysGmt() {
        String formatted = DateParser.formatRfc822(Instant.parse("2021-10-09T20:38:50Z"));

        assertEquals("Sat, 09 Oct 2021 20:38:50 GMT", formatted);
    }

    @Test
    @DisplayName("the deprecated names still work and delegate to the new ones")
    @SuppressWarnings({"deprecation", "removal"})
    void deprecatedAliasesDelegate() {
        assertAll(
                () -> assertEquals(SPEC_EXAMPLE_FORMATTED,
                        DateParser.format_RFC1123_RFC822(SPEC_EXAMPLE)),
                () -> assertEquals(SPEC_EXAMPLE_FORMATTED,
                        DateParser.format_RFC1123_RFC822(Date.from(SPEC_EXAMPLE))));
    }
}
