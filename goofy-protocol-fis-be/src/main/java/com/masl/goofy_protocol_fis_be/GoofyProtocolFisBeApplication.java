package com.masl.goofy_protocol_fis_be;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@OpenAPIDefinition
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class })
@ConfigurationPropertiesScan
public class GoofyProtocolFisBeApplication {
	static void main(String[] args) {
		SpringApplication.run(GoofyProtocolFisBeApplication.class, args);
	}
}
