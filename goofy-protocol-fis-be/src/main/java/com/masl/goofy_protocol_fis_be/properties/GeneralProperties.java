package com.masl.goofy_protocol_fis_be.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "goofy.general")
@Data
public class GeneralProperties {
    private String frontendUrl;
    private String url;
    private String name;
    private String description;
    private String contact;
    private String version;
}
