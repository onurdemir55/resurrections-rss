package io.github.onurdemir55.resurrections.rss.io;

/**
 * Thrown by {@link RssOutput#outputString(Rss)} when a feed cannot be written as XML.
 * <p>
 * This is unchecked because {@code outputString} has no target that can fail: it builds a
 * string in memory, so there is no full disk or broken pipe to recover from, and a document
 * that still cannot be produced means this library assembled something the XML writer
 * rejected. That is a bug here rather than a condition a caller can handle, and making every
 * call site catch it would be noise in return for nothing.
 * <p>
 * The overloads that write to a stream, a writer or a file report failure as
 * {@link java.io.IOException} instead, because those targets can genuinely fail and the
 * caller does have something to do about it.
 */
public final class RssOutputException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the given message and cause.
     *
     * @param message what went wrong
     * @param cause the underlying failure
     */
    RssOutputException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
