/**
 * Creates RSS 2.0 feeds, with first-class support for CDATA sections.
 * <p>
 * Four packages are exported and one is not. {@code util} holds the checks the model runs when
 * it is built - what may be an element name, what text XML can represent, what counts as a URI
 * scheme - and those are decisions this library makes about its own output, not a service it
 * offers. Leaving them out of the descriptor keeps them off the permanent API surface, so they
 * can be corrected later without breaking anyone. They have already been corrected twice.
 *
 * @see io.github.onurdemir55.resurrections.rss.io.RssOutput
 */
module io.github.onurdemir55.resurrections.rss {

    // The StAX API, which ships with the JDK.
    requires java.xml;

    // The StAX implementation. Required rather than optional because the JDK's own
    // implementation writes a CDATA section containing "]]>" as a document no parser accepts,
    // which was measured, and this library exists to get CDATA right.
    requires com.ctc.wstx;

    exports io.github.onurdemir55.resurrections.rss.feed;
    exports io.github.onurdemir55.resurrections.rss.feed.element;
    exports io.github.onurdemir55.resurrections.rss.feed.holder;
    exports io.github.onurdemir55.resurrections.rss.io;
}
