package io.github.onurdemir55.resurrections.rss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the module descriptor says, checked against what it is meant to say.
 * <p>
 * There are two claims here worth holding onto. The module is named after the package root,
 * because without a descriptor the name would be derived from whatever the jar file happens to
 * be called - which resolved to {@code resurrections.rss}, neither the package root nor stable
 * if the artifact were ever renamed. And {@code util} is not exported: it holds the checks the
 * model runs on itself, which have already been corrected twice, and keeping them off the
 * exported surface is what makes correcting them again possible.
 * <p>
 * Read from the compiled descriptor rather than from the source, so that what ships is what is
 * asserted. An earlier version of this test read the jar and broke the moment two jars of
 * different versions sat in the same directory; the descriptor has no such ambiguity.
 */
class ModuleNameTest {

    private static final String MODULE = "io.github.onurdemir55.resurrections.rss";

    private static final Set<String> EXPECTED_EXPORTS = Set.of(
            MODULE + ".feed",
            MODULE + ".feed.element",
            MODULE + ".feed.holder",
            MODULE + ".io");

    @Test
    @DisplayName("the module is named after the package root")
    void moduleIsNamedAfterThePackageRoot() throws IOException {
        assertEquals(MODULE, descriptor().name());
        assertEquals(MODULE, ModuleNameTest.class.getPackageName(),
                "the module name should be the package the public API lives in");
    }

    @Test
    @DisplayName("only the packages callers need are exported")
    void exportsOnlyThePublicPackages() throws IOException {
        Set<String> exported = descriptor().exports().stream()
                .map(ModuleDescriptor.Exports::source)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        assertEquals(EXPECTED_EXPORTS, exported);
    }

    @Test
    @DisplayName("util stays internal, so its checks can keep being corrected")
    void utilIsNotExported() throws IOException {
        Set<String> exported = descriptor().exports().stream()
                .map(ModuleDescriptor.Exports::source)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        assertFalse(exported.contains(MODULE + ".util"),
                () -> "util must not be exported, but the exports are " + exported);
    }

    @Test
    @DisplayName("the StAX implementation is required, not optional")
    void requiresTheStaxImplementation() throws IOException {
        Set<String> required = descriptor().requires().stream()
                .map(ModuleDescriptor.Requires::name)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        assertTrue(required.contains("com.ctc.wstx"),
                () -> "the JDK's own StAX writes a CDATA section containing ]]> as a document "
                        + "no parser accepts, so Woodstox is not optional; requires: " + required);
        assertTrue(required.contains("java.xml"), () -> "requires: " + required);
    }

    private static ModuleDescriptor descriptor() throws IOException {
        Path compiled = Path.of("build", "classes", "java", "main", "module-info.class");
        if (!Files.isRegularFile(compiled)) {
            throw new AssertionError("no compiled module descriptor at " + compiled);
        }
        try (InputStream in = Files.newInputStream(compiled)) {
            return ModuleDescriptor.read(in);
        }
    }
}
