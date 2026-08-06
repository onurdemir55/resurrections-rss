package io.github.onurdemir55.resurrections.rss.io;

import io.github.onurdemir55.resurrections.rss.feed.Rss;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Renders an {@link Rss} feed as XML.
 * <p>
 * The document is produced by {@link RssWriter}, which writes it element by element. The feed
 * model itself carries no knowledge of XML, so what comes out of here is decided in one place
 * that can be read from top to bottom.
 */
public final class RssOutput {

    private RssOutput() {
        // utility class
    }

    /**
     * Renders the feed as an indented XML document, including the XML declaration.
     *
     * @param rss the feed to render
     * @return the XML document
     * @throws NullPointerException if {@code rss} is {@code null}
     * @throws RssOutputException if the feed cannot be written, which would be a bug here
     */
    public static String outputString(final Rss rss) {
        Objects.requireNonNull(rss, "rss");

        StringWriter target = new StringWriter();
        try {
            RssWriter.write(rss, target);
        } catch (XMLStreamException e) {
            // No known input reaches this. A StringWriter does not fail for I/O reasons, and
            // everything that once made the writer refuse a document is now refused earlier:
            // element text and attribute values when they are created, element names and
            // namespace prefixes when the feed is built. It stays because XMLStreamException
            // is checked and swallowing it would be worse than an unreachable branch, and
            // because "no known input" is not the same as "no input". The conversion itself is
            // tested through RssOutput.failed, which is why that method is package-private.
            throw failed(e);
        }
        return target.toString();
    }

    /**
     * Writes the feed to a stream as UTF-8, which is the encoding the XML declaration
     * announces. The stream is flushed but not closed: the caller keeps ownership of what
     * they passed in.
     *
     * @param rss the feed to render
     * @param out where to write
     * @throws NullPointerException if {@code rss} or {@code out} is {@code null}
     * @throws IOException if the feed cannot be written, whether because the stream failed
     *     or because the document could not be produced
     */
    public static void output(final Rss rss, final OutputStream out) throws IOException {
        Objects.requireNonNull(rss, "rss");
        Objects.requireNonNull(out, "out");

        try {
            RssWriter.write(rss, out);
        } catch (XMLStreamException e) {
            throw asIoException(e);
        }
    }

    /**
     * Writes the feed to a character stream. The writer is flushed but not closed.
     * <p>
     * The XML declaration announces UTF-8, so the writer should encode as UTF-8;
     * otherwise the document will misdeclare its own encoding. Prefer
     * {@link #output(Rss, OutputStream)}, which cannot get this wrong.
     *
     * @param rss the feed to render
     * @param out where to write
     * @throws NullPointerException if {@code rss} or {@code out} is {@code null}
     * @throws IOException if the feed cannot be written, whether because the writer failed
     *     or because the document could not be produced
     */
    public static void output(final Rss rss, final Writer out) throws IOException {
        Objects.requireNonNull(rss, "rss");
        Objects.requireNonNull(out, "out");

        try {
            RssWriter.write(rss, out);
        } catch (XMLStreamException e) {
            throw asIoException(e);
        }
    }

    /**
     * Writes the feed to a file as UTF-8.
     *
     * @param rss the feed to render
     * @param target the file to write, created or truncated
     * @throws NullPointerException if {@code rss} or {@code target} is {@code null}
     * @throws IOException if the file cannot be created, or the feed cannot be written
     */
    public static void output(final Rss rss, final Path target) throws IOException {
        Objects.requireNonNull(rss, "rss");
        Objects.requireNonNull(target, "target");

        try (OutputStream out = Files.newOutputStream(target)) {
            output(rss, out);
        }
    }

    /**
     * Unwraps what a StAX writer reports so the caller sees the most specific
     * {@link IOException} available: a stream that failed with "disk full" should say so
     * rather than arrive as something generic.
     * <p>
     * No attempt is made to work out whether the failure was the target's fault or this
     * library's, because it cannot be done. Woodstox reports a full disk and text it cannot
     * encode the same way, as a {@code WstxIOException} wrapping an {@code IOException}, so
     * any test of the type would be guesswork dressed up as a diagnosis. Writing to a target
     * that can fail therefore reports failure as {@code IOException} whatever the cause, and
     * the causes worth preventing are prevented earlier instead: text and attribute values are
     * checked when they are created, and element names and namespace prefixes when the feed is
     * built.
     * <p>
     * Package-private so that this mapping can be tested on its own. With the model validated
     * up front there is no longer a feed that reaches the writer and fails to be written, which
     * is the point, but it also means the fallback below cannot be reached through the public
     * API and would otherwise go unverified.
     */
    static IOException asIoException(final XMLStreamException e) {
        Throwable nested = e.getNestedException();
        if (nested instanceof IOException io) {
            return io;
        }
        return new IOException("the feed could not be written as XML", e);
    }

    static RssOutputException failed(final XMLStreamException e) {
        return new RssOutputException("the feed could not be written as XML", e);
    }
}
