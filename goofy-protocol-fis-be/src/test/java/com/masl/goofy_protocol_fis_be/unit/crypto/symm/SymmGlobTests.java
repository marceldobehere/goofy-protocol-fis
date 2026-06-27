package com.masl.goofy_protocol_fis_be.unit.crypto.symm;

import com.masl.goofy_protocol_fis_be.crypto.symm.GlobSymmCrypto;
import com.masl.goofy_protocol_fis_be.crypto.symm.SymmCryptoType;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.security.Security;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SymmGlobTests {
	private static final Logger log = LoggerFactory.getLogger(SymmGlobTests.class);

	@BeforeAll
	static void init() {
		Security.addProvider(new BouncyCastleProvider());
		Security.addProvider(new BouncyCastlePQCProvider());
	}

	@Test
	void testGlobalAsymmCryptoEncDec() {
		GlobSymmCrypto crypto = new GlobSymmCrypto();
		for (var type : crypto.getTypes()) {
			String randomSecret = "bla bla bla " + type.toString();

			String msg = "This is a very crazy amazing test of a message that is going to get encrypted and also decrypted, hopefully returning to the same original content after the dencryption and encryption of the original message.";
			String enc = crypto.encrypt(msg.getBytes(StandardCharsets.UTF_8), randomSecret, type);
			assertThat(enc).isNotNull();

			byte[] dec = crypto.decrypt(enc, randomSecret);
			assertThat(dec).isNotNull();
			assertThat(dec).isNotEqualTo(enc);

			log.info("Decrypted message: {} = {}?", new String(dec, StandardCharsets.UTF_8), msg);
			assertThat(new String(dec, StandardCharsets.UTF_8)).isEqualTo(msg);
		}
	}

	@Test
	void testGlobalAsymmCryptoRawEncDec() {
		GlobSymmCrypto crypto = new GlobSymmCrypto();
		for (var type : crypto.getTypes()) {
			String randomSecret = "bla bla bla " + type.toString();

			String msg = "This is a very crazy amazing test of a message that is going to get encrypted and also decrypted, hopefully returning to the same original content after the dencryption and encryption of the original message.";
			byte[] enc = crypto.encryptRaw(msg.getBytes(StandardCharsets.UTF_8), randomSecret, type);
			assertThat(enc).isNotNull();

			byte[] dec = crypto.decryptRaw(enc, randomSecret);
			assertThat(dec).isNotNull();
			assertThat(dec).isNotEqualTo(enc);

			log.info("Decrypted message: {} = {}?", new String(dec, StandardCharsets.UTF_8), msg);
			assertThat(new String(dec, StandardCharsets.UTF_8)).isEqualTo(msg);
		}
	}

	@Test
	void testGlobalAsymmCryptoEncNotIdentical() {
		GlobSymmCrypto crypto = new GlobSymmCrypto();
		for (var type : crypto.getTypes()) {
			String randomSecret = "bla bla bla " + type.toString();

			String msg = "This is a very crazy amazing test of a message that is going to get encrypted and also decrypted, hopefully returning to the same original content after the dencryption and encryption of the original message.";
			String enc1 = crypto.encrypt(msg.getBytes(StandardCharsets.UTF_8), randomSecret, type);
			assertThat(enc1).isNotNull();

			String enc2 = crypto.encrypt(msg.getBytes(StandardCharsets.UTF_8), randomSecret, type);
			assertThat(enc2).isNotNull();

			assertThat(enc1).isNotEqualTo(enc2);
		}
	}

}
