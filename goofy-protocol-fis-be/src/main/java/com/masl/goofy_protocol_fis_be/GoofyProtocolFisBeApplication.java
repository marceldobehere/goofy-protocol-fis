package com.masl.goofy_protocol_fis_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;


@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class })
public class GoofyProtocolFisBeApplication {
	static void main(String[] args) {
		SpringApplication.run(GoofyProtocolFisBeApplication.class, args);
	}
}
