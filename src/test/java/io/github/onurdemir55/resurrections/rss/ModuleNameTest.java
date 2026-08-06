package io.github.onurdemir55.resurrections.rss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The published jar declares a module name.
 * <p>
 * Without one, the name is derived from the file the jar happens to be called, so renaming the
 * artifact would rename the module and break anyone who put it on the module path. The
 * derivation was checked and gave {@code resurrections.rss}, which is neither stable nor the
 * package root. Declaring it in the manifest is what a library published for others to depend
 * on is expected to do, and this test is here because the manifest is easy to lose in a build
 * file and nothing else would notice.
 */
class ModuleNameTest {

    private static final String EXPECTED = "io.github.onurdemir55.resurrections.rss";

    @Test
    @DisplayName("the jar's manifest names the module after the package root")
    void manifestDeclaresModuleName() throws IOException {
        Path jar = builtJar();

        try (JarFile file = new JarFile(jar.toFile())) {
            Manifest manifest = file.getManifest();
            assertTrue(manifest != null, () -> "no manifest in " + jar);
            assertEquals(EXPECTED,
                    manifest.getMainAttributes().getValue("Automatic-Module-Name"),
                    () -> "Automatic-Module-Name is missing or wrong in " + jar);
        }
    }

    @Test
    @DisplayName("and it matches the package the public API lives in")
    void moduleNameMatchesThePackage() {
        assertTrue(EXPECTED.equals(ModuleNameTest.class.getPackageName()),
                () -> "the module name should be the package root, but the package is "
                        + ModuleNameTest.class.getPackageName());
    }

    /**
     * Runs against whatever the build produced rather than a fixed name, so a version bump
     * does not break it. Skipping when there is no jar would make the test lie, so a missing
     * jar is a failure with an explanation instead.
     */
    private static Path builtJar() throws IOException {
        Path libs = Path.of("build", "libs");
        if (!Files.isDirectory(libs)) {
            throw new AssertionError("no build/libs directory; run the jar task before this test");
        }
        try (var entries = Files.list(libs)) {
            List<Path> jars = entries
                    .filter(path -> fileName(path).endsWith(".jar"))
                    .filter(path -> !fileName(path).contains("-sources"))
                    .filter(path -> !fileName(path).contains("-javadoc"))
                    .toList();
            if (jars.size() != 1) {
                throw new AssertionError("expected exactly one main jar in " + libs + ", found " + jars);
            }
            return jars.get(0);
        }
    }

    /** {@link Path#getFileName()} is null for a root, which nothing here can be, but a null
     * check costs less than an explanation. */
    private static String fileName(final Path path) {
        Path name = path.getFileName();
        return name == null ? "" : name.toString();
    }
}
