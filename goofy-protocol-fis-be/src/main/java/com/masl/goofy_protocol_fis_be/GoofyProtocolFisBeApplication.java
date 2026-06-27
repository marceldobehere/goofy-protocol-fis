package com.masl.goofy_protocol_fis_be;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SpringBootApplication
public class GoofyProtocolFisBeApplication {
	static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		Security.addProvider(new BouncyCastlePQCProvider());
		SpringApplication.run(GoofyProtocolFisBeApplication.class, args);
	}
}
