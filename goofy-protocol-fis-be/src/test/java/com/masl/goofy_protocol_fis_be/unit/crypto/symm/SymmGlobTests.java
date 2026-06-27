package com.masl.goofy_protocol_fis_be.unit.crypto.symm;

import com.masl.goofy_protocol_fis_be.crypto.symm.GlobSymmCrypto;
import com.masl.goofy_protocol_fis_be.crypto.symm.SymmCryptoType;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Security;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SymmGlobTests {
	private static final Logger log = LoggerFactory.getLogger(SymmGlobTests.class);
	private static final String randomSecretBase = "bla bla bla randdom secret";
	private static final String testMessageStr = "This is a very crazy amazing test message";
	private static final byte[] testMessageBytes = new byte[] {1, 2, 3, 10, 20, 30, 9, 10, 11, 0, 100, 127, -1, -100, -128, 123};
	private final GlobSymmCrypto crypto = new GlobSymmCrypto();

	@BeforeAll
	static void init() {
		Security.addProvider(new BouncyCastleProvider());
		Security.addProvider(new BouncyCastlePQCProvider());
	}

	@ParameterizedTest
	@EnumSource(SymmCryptoType.class)
	void testGlobalSymmCryptoRawEncDec(SymmCryptoType type) {
		// Encrypt with Secret
		String randomSecret = randomSecretBase + type.toString();
		byte[] enc = crypto.encryptRaw(testMessageBytes, randomSecret, type);
		assertThat(enc).isNotNull();

		// Decrypt with Secret
		byte[] dec = crypto.decryptRaw(enc, randomSecret);
		assertThat(dec).isNotNull();
		assertThat(dec).isNotEqualTo(enc);
		assertThat(dec).isEqualTo(testMessageBytes);
	}

	@ParameterizedTest
	@EnumSource(SymmCryptoType.class)
	void testGlobalSymmCryptoEncDec(SymmCryptoType type) {
		// Encrypt with Secret
		String randomSecret = randomSecretBase + type.toString();
		String enc = crypto.encrypt(testMessageBytes, randomSecret, type);
		assertThat(enc).isNotNull();

		// Decrypt with Secret
		byte[] dec = crypto.decrypt(enc, randomSecret);
		assertThat(dec).isNotNull();
		assertThat(dec).isEqualTo(testMessageBytes);
	}

	@ParameterizedTest
	@EnumSource(SymmCryptoType.class)
	void testGlobalSymmCryptoStrEncDec(SymmCryptoType type) {
		// Encrypt with Secret
		String randomSecret = randomSecretBase + type.toString();
		String enc = crypto.encryptStr(testMessageStr, randomSecret, type);
		assertThat(enc).isNotNull();

		// Decrypt with Secret
		String dec = crypto.decryptStr(enc, randomSecret);
		assertThat(dec).isNotNull();
		assertThat(dec).isNotEqualTo(enc);

		// Check
		log.info("Decrypted message: \"{}\" = \"{}\" ?", testMessageStr, dec);
		assertThat(dec).isEqualTo(testMessageStr);
	}

	@ParameterizedTest
	@EnumSource(SymmCryptoType.class)
	void testGlobalSymmCryptoEncNotIdentical(SymmCryptoType type) {
		// Encrypt with Secret
		String randomSecret = randomSecretBase + type.toString();
		String enc1 = crypto.encrypt(testMessageBytes, randomSecret, type);
		assertThat(enc1).isNotNull();

		// Encrypt again with Secret
		String enc2 = crypto.encrypt(testMessageBytes, randomSecret, type);
		assertThat(enc2).isNotNull();

		// Check
		assertThat(enc1).isNotEqualTo(enc2);
	}
}
