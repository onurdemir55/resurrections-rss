package io.github.onurdemir55.resurrections.rss.io;

import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Item;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.element.Source;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What {@link RssOutput} does when writing fails.
 * <p>
 * There is less here than there once was, and that is the result rather than an omission. A
 * feed that cannot be turned into XML is now rejected while it is being built, so the only
 * failure the writer can still report is the target's: a full disk, a closed socket. These
 * tests pin that, and check the exception mapping directly for the case the writer is not
 * expected to produce at all.
 */
class RssOutputFailureTest {

    @Nested
    @DisplayName("A target that fails")
    class TargetFailures {

        @Test
        @DisplayName("a stream failure reaches the caller with its own message intact")
        void stream() {
            IOException thrown = assertThrows(IOException.class,
                    () -> RssOutput.output(feed(), new FailingStream()));

            assertEquals("disk full", thrown.getMessage());
        }

        @Test
        @DisplayName("a writer failure reaches the caller with its own message intact")
        void writer() {
            IOException thrown = assertThrows(IOException.class,
                    () -> RssOutput.output(feed(), new FailingWriter()));

            assertEquals("disk full", thrown.getMessage());
        }

        @Test
        @DisplayName("and a file that cannot be created is an IOException, not a crash")
        void unwritableFile(@TempDir final Path directory) {
            // A path whose parent does not exist. Built from the temporary directory rather
            // than hard-coded, so the test says the same thing on every platform.
            Path missing = directory.resolve("absent").resolve("feed.xml");

            assertThrows(IOException.class, () -> RssOutput.output(feed(), missing));
        }
    }

    @Nested
    @DisplayName("The common case needs no exception handling")
    class HappyPath {

        @Test
        @DisplayName("outputString declares no checked exception")
        void outputStringIsUnchecked() {
            String xml = RssOutput.outputString(feed());

            assertTrue(xml.contains("<rss version=\"2.0\">"), () -> xml);
        }
    }

    /**
     * A character XML cannot represent used to travel through the model and fail while the
     * document was being written, which on a stream meant half a feed had already been sent.
     * It is now refused at the point it enters the model.
     */
    @Nested
    @DisplayName("Content that cannot be written never reaches the writer")
    class RejectedEarly {

        @Test
        @DisplayName("in element text")
        void text() {
            assertThrows(IllegalArgumentException.class, () -> new SimpleValue("a\u0000b"));
        }

        @Test
        @DisplayName("in an attribute value, checked as of this release")
        void attribute() {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> Source.of("Origin", "https://example.com/\u0000"));

            assertTrue(thrown.getMessage().contains("source url"), thrown::getMessage);
        }
    }

    /**
     * Reached directly rather than through a feed, because no feed produces it any more. The
     * mapping still has to be right: an exception carrying a real I/O failure must hand that
     * failure over untouched, and one carrying nothing must not be mistaken for I/O.
     */
    @Nested
    @DisplayName("Mapping what the XML writer reports")
    class ExceptionMapping {

        @Test
        @DisplayName("a nested I/O failure is handed over as itself, losing nothing")
        void nestedIoIsUnwrapped() {
            IOException cause = new IOException("disk full");

            IOException mapped = RssOutput.asIoException(new XMLStreamException(cause));

            assertSame(cause, mapped);
        }

        @Test
        @DisplayName("anything else becomes an IOException that keeps the original as its cause")
        void otherFailuresAreWrapped() {
            XMLStreamException cause = new XMLStreamException("not an I/O problem");

            IOException mapped = RssOutput.asIoException(cause);

            assertSame(cause, mapped.getCause());
            assertTrue(mapped.getMessage().contains("could not be written"), mapped::getMessage);
        }

        @Test
        @DisplayName("and outputString's form is unchecked, since it has no target to blame")
        void stringFormIsUnchecked() {
            XMLStreamException cause = new XMLStreamException("not an I/O problem");

            RssOutputException mapped = RssOutput.failed(cause);

            assertSame(cause, mapped.getCause());
            assertTrue(mapped.getMessage().contains("could not be written"), mapped::getMessage);
        }
    }

    private static Rss feed() {
        return Rss.builder()
                .channel(Channel.builder()
                        .title(new SimpleValue("a title"))
                        .link(new SimpleValue("https://example.com/"))
                        .description(new SimpleValue("a description"))
                        .items(Item.builder().title(new SimpleValue("an item")).build())
                        .build())
                .build();
    }

    /** Fails on the first byte, the way a full disk would. */
    private static final class FailingStream extends OutputStream {

        @Override
        public void write(final int b) throws IOException {
            throw new IOException("disk full");
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            throw new IOException("disk full");
        }
    }

    /** Fails on the first character, the way a full disk would. */
    private static final class FailingWriter extends Writer {

        @Override
        public void write(final char[] buffer, final int off, final int len) throws IOException {
            throw new IOException("disk full");
        }

        @Override
        public void flush() throws IOException {
            throw new IOException("disk full");
        }

        @Override
        public void close() throws IOException {
            throw new IOException("disk full");
        }
    }
}
