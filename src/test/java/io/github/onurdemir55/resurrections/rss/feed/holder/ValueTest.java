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
        Value plain = new SimpleValue("text");
        Value cdata = new CDATAValue("text");

        assertAll(
                () -> assertEquals("text", plain.value()),
                () -> assertEquals("text", cdata.value()));
    }

    @Test
    @DisplayName("values of the same form and content are equal")
    void equalityByContent() {
        assertAll(
                () -> assertEquals(new SimpleValue("a"), new SimpleValue("a")),
                () -> assertEquals(new SimpleValue("a").hashCode(), new SimpleValue("a").hashCode()),
                () -> assertEquals(new CDATAValue("a"), new CDATAValue("a")),
                () -> assertNotEquals(new SimpleValue("a"), new SimpleValue("b")));
    }

    @Test
    @DisplayName("the two forms are never equal, even with identical text")
    void formsAreDistinct() {
        assertNotEquals(new SimpleValue("a"), new CDATAValue("a"));
    }

    @Test
    @DisplayName("a Set de-duplicates repeated values")
    void setDeduplicates() {
        Set<Value> values = new LinkedHashSet<>();
        values.add(new SimpleValue("dup"));
        values.add(new SimpleValue("dup"));
        values.add(new CDATAValue("dup"));

        assertEquals(2, values.size(), () -> "plain and CDATA are distinct, duplicates collapse: " + values);
    }

    @Test
    @DisplayName("the hierarchy is closed to the two known forms")
    void hierarchyIsSealed() {
        assertAll(
                () -> assertTrue(Value.class.isSealed()),
                () -> assertEquals(
                        Set.of(SimpleValue.class, CDATAValue.class),
                        Set.of(Value.class.getPermittedSubclasses())));
    }
}
