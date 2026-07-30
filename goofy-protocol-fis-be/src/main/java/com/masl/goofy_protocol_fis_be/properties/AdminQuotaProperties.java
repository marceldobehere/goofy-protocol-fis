package com.masl.goofy_protocol_fis_be.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "goofy.quota.admin")
public class AdminQuotaProperties extends BaseQuotaProperties {

}
