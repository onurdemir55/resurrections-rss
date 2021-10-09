package com.resurrections.rss.feed.holder;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CDATA value class. When you want to create CDATA use this class object
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CDATAValue implements Value {

    @JacksonXmlText
    @JacksonXmlCData
    String value;
}
