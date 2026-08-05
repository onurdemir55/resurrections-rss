package io.github.onurdemir55.resurrections.rss.feed.holder;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * Element content wrapped in a CDATA section, so markup stays readable in the feed
 * instead of being entity-encoded.
 *
 * @param value the text content, emitted inside {@code <![CDATA[ ... ]]>}
 */
public record CDATAValue(@JacksonXmlText @JacksonXmlCData String value) implements Value {
}
