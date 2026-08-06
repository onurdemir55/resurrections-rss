package io.github.onurdemir55.resurrections.rss.feed.holder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract of the {@link Value} hierarchy.
 * <p>
 * Both forms are records, so equality is by content. Feed models rely on that: a
 * {@code Set} of categories de-duplicates repeated values.
 */
class ValueTest {

    @Test
    @DisplayName("both forms expose their text through the Value contract")
    void valueContract() {
        Value plain = new PlainValue("text");
        Value cdata = new CDATAValue("text");

        assertAll(
                () -> assertEquals("text", plain.value()),
                () -> assertEquals("text", cdata.value()));
    }

    @Test
    @DisplayName("values of the same form and content are equal")
    void equalityByContent() {
        assertAll(
                () -> assertEquals(new PlainValue("a"), new PlainValue("a")),
                () -> assertEquals(new PlainValue("a").hashCode(), new PlainValue("a").hashCode()),
                () -> assertEquals(new CDATAValue("a"), new CDATAValue("a")),
                () -> assertNotEquals(new PlainValue("a"), new PlainValue("b")));
    }

    @Test
    @DisplayName("the two forms are never equal, even with identical text")
    void formsAreDistinct() {
        assertNotEquals(new PlainValue("a"), new CDATAValue("a"));
    }

    @Test
    @DisplayName("a Set de-duplicates repeated values")
    void setDeduplicates() {
        Set<Value> values = new LinkedHashSet<>();
        values.add(new PlainValue("dup"));
        values.add(new PlainValue("dup"));
        values.add(new CDATAValue("dup"));

        assertEquals(2, values.size(), () -> "plain and CDATA are distinct, duplicates collapse: " + values);
    }

    /**
     * Adding a third form here would break the writer silently, which is why this is pinned.
     * {@code RssWriter.content} chooses a CDATA section for a {@link CDATAValue} and plain
     * text for anything else, so a new form would take the plain branch and lose its CDATA
     * without any error — the one failure this library must never have. Java 17 cannot make
     * that branch exhaustive at compile time, so it is caught here instead: if this test
     * fails, update {@code RssWriter.content} before doing anything else.
     */
    @Test
    @DisplayName("the hierarchy is closed to the two known forms")
    void hierarchyIsSealed() {
        assertAll(
                () -> assertTrue(Value.class.isSealed()),
                () -> assertEquals(
                        Set.of(PlainValue.class, CDATAValue.class),
                        Set.of(Value.class.getPermittedSubclasses()),
                        "a new Value form must be handled in RssWriter.content, or its text "
                                + "will be written as plain text and its CDATA section lost"));
    }
}
