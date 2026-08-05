package io.github.onurdemir55.resurrections.rss.feed.element;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Objects;

/**
 * The {@code <cloud>} sub-element of a channel: a web service that readers can register
 * with to be notified when the channel changes, rather than polling it.
 * <p>
 * The element carries no text, only attributes, and all five are required.
 *
 * @param domain the host of the service
 * @param port the port of the service
 * @param path the path of the service, for example {@code /RPC2}
 * @param registerProcedure the procedure to call to register
 * @param protocol the protocol, one of {@code xml-rpc}, {@code soap} or {@code http-post}
 */
public record Cloud(@JacksonXmlProperty(isAttribute = true, localName = "domain") String domain,
                    @JacksonXmlProperty(isAttribute = true, localName = "port") int port,
                    @JacksonXmlProperty(isAttribute = true, localName = "path") String path,
                    @JacksonXmlProperty(isAttribute = true, localName = "registerProcedure")
                    String registerProcedure,
                    @JacksonXmlProperty(isAttribute = true, localName = "protocol")
                    String protocol) {

    public Cloud {
        Objects.requireNonNull(domain, "cloud domain is required");
        Objects.requireNonNull(path, "cloud path is required");
        Objects.requireNonNull(registerProcedure, "cloud registerProcedure is required");
        Objects.requireNonNull(protocol, "cloud protocol is required");
    }

    /**
     * @param domain the host of the service
     * @param port the port of the service
     * @param path the path of the service
     * @param registerProcedure the procedure to call to register
     * @param protocol the protocol
     * @return the cloud
     */
    public static Cloud of(final String domain, final int port, final String path,
                           final String registerProcedure, final String protocol) {
        return new Cloud(domain, port, path, registerProcedure, protocol);
    }
}
