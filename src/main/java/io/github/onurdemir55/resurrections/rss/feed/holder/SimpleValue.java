package io.github.onurdemir55.resurrections.rss.feed.holder;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple plain text value class. String can be encoded or you can choose CDATAValue.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SimpleValue implements Value {

    @JacksonXmlText
    String value;
}
