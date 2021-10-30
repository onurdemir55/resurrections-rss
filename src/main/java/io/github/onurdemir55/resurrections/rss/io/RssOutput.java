package io.github.onurdemir55.resurrections.rss.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import io.github.onurdemir55.resurrections.rss.feed.Rss;

/**
 * Rss Generator class
 * Creates XML output String.
 */
public class RssOutput {

    public static String outputString(final Rss rss) throws JsonProcessingException {
        JacksonXmlModule xmlModule = new JacksonXmlModule();
        xmlModule.setDefaultUseWrapper(false);
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return xmlMapper.writeValueAsString(rss);
    }
}
