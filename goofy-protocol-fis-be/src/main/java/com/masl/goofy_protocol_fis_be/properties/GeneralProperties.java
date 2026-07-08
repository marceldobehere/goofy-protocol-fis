package com.masl.goofy_protocol_fis_be.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "goofy.general")
@Data
public class GeneralProperties {
    private String frontendUrl;
    private String url;
    private String name;
    private String description;
    private String contact;
    private String version;

    // https://fis.rocc.systems -> fis.rocc.systems
    // http://localhost:8080 -> localhost:8080
    // https://fis.rocc.systems/abc -> fis.rocc.systems
    public String getDomain() {
        URI uri = URI.create(url);
        String host = uri.getHost();

        // Return with/without Port
        int port = uri.getPort();
        return (port == -1) ? host : host + ":" + port;
    }
}
