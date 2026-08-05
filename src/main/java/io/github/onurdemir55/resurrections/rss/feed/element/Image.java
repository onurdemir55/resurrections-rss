package io.github.onurdemir55.resurrections.rss.feed.element;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.github.onurdemir55.resurrections.rss.feed.holder.SimpleValue;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import io.github.onurdemir55.resurrections.rss.util.Uris;

import java.util.Objects;

/**
 * The {@code <image>} sub-element of a channel: a GIF, JPEG or PNG that can be displayed
 * with the channel.
 * <p>
 * {@code url}, {@code title} and {@code link} are required. In practice the title and link
 * should match the channel's own. The optional {@code width} and {@code height} are in
 * pixels; when left unset a reader applies the specification's defaults of 88 and 31, so
 * omitting them says more than repeating the default would.
 *
 * @param url the url of the image
 * @param title describes the image, used as the {@code alt} text when rendered
 * @param link the url the image links to when rendered
 * @param width the width in pixels, at most {@value #MAX_WIDTH}, or {@code null}
 * @param height the height in pixels, at most {@value #MAX_HEIGHT}, or {@code null}
 * @param description text used as the tooltip of the rendered link, or {@code null}
 */
@JsonPropertyOrder({"url", "title", "link", "width", "height", "description"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Image(@JacksonXmlProperty(localName = "url") Value url,
                    @JacksonXmlProperty(localName = "title") Value title,
                    @JacksonXmlProperty(localName = "link") Value link,
                    @JacksonXmlProperty(localName = "width") Integer width,
                    @JacksonXmlProperty(localName = "height") Integer height,
                    @JacksonXmlProperty(localName = "description") Value description) {

    /** The largest width the specification allows. */
    public static final int MAX_WIDTH = 144;

    /** The largest height the specification allows. */
    public static final int MAX_HEIGHT = 400;

    public Image {
        Objects.requireNonNull(url, "image url is required");
        Objects.requireNonNull(title, "image title is required");
        Objects.requireNonNull(link, "image link is required");
        Uris.requireScheme("image url", url.value());
        Uris.requireScheme("image link", link.value());
        checkBound("width", width, MAX_WIDTH);
        checkBound("height", height, MAX_HEIGHT);
    }

    /**
     * An image with only the three required sub-elements.
     *
     * @param url the url of the image
     * @param title describes the image
     * @param link the url the image links to
     * @return the image
     */
    public static Image of(final String url, final String title, final String link) {
        return new Image(new SimpleValue(url), new SimpleValue(title), new SimpleValue(link),
                null, null, null);
    }

    /**
     * A copy of this image with the given dimensions.
     *
     * @param newWidth the width in pixels, at most {@value #MAX_WIDTH}
     * @param newHeight the height in pixels, at most {@value #MAX_HEIGHT}
     * @return a new image
     */
    public Image withSize(final int newWidth, final int newHeight) {
        return new Image(url, title, link, newWidth, newHeight, description);
    }

    /**
     * A copy of this image with the given description.
     *
     * @param newDescription the tooltip text
     * @return a new image
     */
    public Image withDescription(final Value newDescription) {
        return new Image(url, title, link, width, height, newDescription);
    }

    private static void checkBound(final String name, final Integer actual, final int max) {
        if (actual != null && (actual < 0 || actual > max)) {
            throw new IllegalArgumentException(
                    "image " + name + " must be between 0 and " + max + ", was " + actual);
        }
    }
}
