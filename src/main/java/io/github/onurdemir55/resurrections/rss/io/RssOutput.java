package io.github.onurdemir55.resurrections.rss.io;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import io.github.onurdemir55.resurrections.rss.feed.Rss;

import javax.xml.stream.XMLOutputFactory;

/**
 * Renders an {@link Rss} feed as XML.
 */
public final class RssOutput {

    /**
     * Configured once and shared. {@link XmlMapper} is thread safe after configuration,
     * and building one per call was pure overhead.
     */
    private static final XmlMapper MAPPER = createMapper();

    private RssOutput() {
        // utility class
    }

    /**
     * Renders the feed as an indented XML document, including the XML declaration.
     *
     * @param rss the feed to render
     * @return the XML document
     * @throws JsonProcessingException if the feed cannot be serialized
     */
    public static String outputString(final Rss rss) throws JsonProcessingException {
        return MAPPER.writeValueAsString(rss);
    }

    private static XmlMapper createMapper() {
        XmlFactory xmlFactory = XmlFactory.builder()
                .xmlOutputFactory(cdataSafeOutputFactory())
                .build();

        XmlMapper mapper = new XmlMapper(xmlFactory);
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    /**
     * Text containing {@code ]]>} would close a CDATA section early, so it has to be
     * split across two sections. Woodstox refuses to write such content unless it is
     * allowed to fix it, and refusing means an ordinary piece of HTML makes the whole
     * feed fail to serialize. With both properties set it emits
     * {@code <![CDATA[a ]]]]><![CDATA[> b]]>} instead, which parsers read back as the
     * original text.
     */
    private static XMLOutputFactory cdataSafeOutputFactory() {
        WstxOutputFactory factory = new WstxOutputFactory();
        factory.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_CONTENT, true);
        factory.setProperty(WstxOutputProperties.P_OUTPUT_FIX_CONTENT, true);
        return factory;
    }
}
