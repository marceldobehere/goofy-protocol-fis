package com.masl.goofy_protocol_core.crypto.isolated.symm;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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

	@ParameterizedTest(name = "Raw enc/dec roundtrip (type={0})")
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

	@ParameterizedTest(name = "Encoded enc/dec roundtrip (type={0})")
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

	@ParameterizedTest(name = "String enc/dec roundtrip (type={0})")
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

	@ParameterizedTest(name = "Ciphertext differs across runs (type={0})")
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

	// Helper Method to get all Types and Sizes
	static Stream<Arguments> cryptoTypeAndSizes() {
		return Stream.of(SymmCryptoType.values())
				.flatMap(type -> Stream.of(
						0,
						1,
						1_000,
						10_000,
						100_000,
						1_000_000,
						10_000_000,
						100_000_000,
						300_000_000
				).map(size -> Arguments.of(type, size)));
	}

	@ParameterizedTest(name = "Raw enc/dec roundtrip with sizes (type={0}, size={1})")
	@MethodSource("cryptoTypeAndSizes")
	void testGlobalSymmCryptoRawEncDecSizes(SymmCryptoType type, int size) {
		// Create data
		byte[] data = new byte[size];
		ThreadLocalRandom.current().nextBytes(data);

		// Encrypt with Secret
		String randomSecret = randomSecretBase + type.toString() + size;
		byte[] enc = crypto.encryptRaw(data, randomSecret, type);
		assertThat(enc).isNotNull();

		// Decrypt with Secret
		byte[] dec = crypto.decryptRaw(enc, randomSecret);
		assertThat(dec).isNotNull();
		assertThat(dec).isNotEqualTo(enc);
		assertThat(dec).isEqualTo(data);
	}
}
