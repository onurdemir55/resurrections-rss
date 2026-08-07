package io.github.onurdemir55.resurrections.rss.feed.element;

import io.github.onurdemir55.resurrections.rss.util.XmlText;

import java.util.Objects;

/**
 * The {@code <cloud>} sub-element of a channel: a web service that readers can register
 * with to be notified when the channel changes, rather than polling it.
 * <p>
 * The element carries no text, only attributes, and all five are required.
 *
 * @param domain the host of the service
 * @param port the port of the service, between 0 and {@value #MAX_PORT}
 * @param path the path of the service, for example {@code /RPC2}
 * @param registerProcedure the procedure to call to register
 * @param protocol the protocol, one of {@code xml-rpc}, {@code soap} or {@code http-post}
 */
public record Cloud(String domain,
                    int port,
                    String path,
                    String registerProcedure,
                    String protocol) {

    /** The largest valid TCP port number. */
    public static final int MAX_PORT = 65535;

    public Cloud {
        Objects.requireNonNull(domain, "cloud domain is required");
        Objects.requireNonNull(path, "cloud path is required");
        Objects.requireNonNull(registerProcedure, "cloud registerProcedure is required");
        Objects.requireNonNull(protocol, "cloud protocol is required");
        XmlText.requireWritable(domain, "cloud domain");
        XmlText.requireWritable(path, "cloud path");
        XmlText.requireWritable(registerProcedure, "cloud registerProcedure");
        XmlText.requireWritable(protocol, "cloud protocol");
        if (port < 0 || port > MAX_PORT) {
            throw new IllegalArgumentException(
                    "cloud port must be between 0 and " + MAX_PORT + ", was " + port);
        }
    }

    /**
     * @param domain the host of the service
     * @param port the port of the service, between 0 and {@value #MAX_PORT}
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
