package io.github.onurdemir55.resurrections.rss.feed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import lombok.Builder;

import java.util.Set;

/**
 * item element bean of Rss.
 */
@Builder
@JsonPropertyOrder({"title", "link", "description", "category", "pubDate"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "item")
public class Item {

    @JacksonXmlProperty(localName = "title")
    Value title;

    @JacksonXmlProperty(localName = "link")
    Value link;

    @JacksonXmlProperty(localName = "description")
    Value description;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "category")
    private Set<Value> category;

    @JacksonXmlProperty(localName = "pubDate")
    Value pubDate;

}
