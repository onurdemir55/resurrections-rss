package com.resurrections.rss.feed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.resurrections.rss.feed.holder.Value;
import lombok.Builder;

import java.util.List;


@Builder
@JsonPropertyOrder({"title", "link", "description", "language", "pubDate", "item"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "channel")
public class Channel {

    @JacksonXmlProperty(localName = "title")
    Value title;

    @JacksonXmlProperty(localName = "link")
    Value link;

    @JacksonXmlProperty(localName = "description")
    Value description;

    @JacksonXmlProperty(localName = "language")
    Value language;

    @JacksonXmlProperty(localName = "pubDate")
    Value pubDate;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    private List<Item> items;

}


