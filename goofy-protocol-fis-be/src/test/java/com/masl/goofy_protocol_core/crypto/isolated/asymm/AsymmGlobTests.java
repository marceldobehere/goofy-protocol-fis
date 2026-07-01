package com.masl.goofy_protocol_core.crypto.isolated.asymm;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
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

@Execution(ExecutionMode.CONCURRENT)
class AsymmGlobTests {
	private static final Logger log = LoggerFactory.getLogger(AsymmGlobTests.class);
	private static final String testMessageStr = "This is a very crazy amazing test message";
	private static final byte[] testMessageBytes = new byte[] {1, 2, 3, 10, 20, 30, 9, 10, 11, 0, 100, 127, -1, -100, -128, 123};
	private final GlobAsymmCrypto crypto = new GlobAsymmCrypto();

	@BeforeAll
    static void init() {
		Security.addProvider(new BouncyCastleProvider());
		Security.addProvider(new BouncyCastlePQCProvider());
	}

	@ParameterizedTest(name = "Keygen & public split check (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testGlobalAsymmCryptoKeygen(AsymmCryptoType type) {
		var keypair = crypto.generateKeypair(type);
		log.info("Generated pub keypair for type {} ({}): {}", type, keypair.pub().serialize().length(), keypair.pub().serialize());
		log.info(" > Generated priv keypair for type {} ({}): {}", type, keypair.priv().serialize().length(), keypair.priv().serialize());
		assertThat(keypair).isNotNull();
		assertThat(crypto.checkPublicSplitKey(keypair.pub().serialize())).isTrue();
	}

	@ParameterizedTest(name = "Raw enc/dec roundtrip (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testGlobalAsymmCryptoRawEncDec(AsymmCryptoType type) {
		// Create Keypair
		var keypair = crypto.generateKeypair(type);
		assertThat(keypair).isNotNull();

		// Encrypt with Public Enc Key
		byte[] enc = crypto.encryptRaw(testMessageBytes, keypair.pub().serialize());
		assertThat(enc).isNotNull();

		// Decrypt with Private Enc Key
		byte[] dec = crypto.decryptRaw(enc, keypair.priv().serialize());
		assertThat(dec).isNotNull();
		assertThat(dec).isNotEqualTo(enc);
		assertThat(dec).isEqualTo(testMessageBytes);
	}

	@ParameterizedTest(name = "Encoded enc/dec roundtrip (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testGlobalAsymmCryptoEncDec(AsymmCryptoType type) {
		// Create Keypair
		var keypair = crypto.generateKeypair(type);
		assertThat(keypair).isNotNull();

		// Encrypt with Public Enc Key
		String enc = crypto.encrypt(testMessageBytes, keypair.pub().serialize());
		assertThat(enc).isNotNull();

		// Decrypt with Private Enc Key
		byte[] dec = crypto.decrypt(enc, keypair.priv().serialize());
		assertThat(dec).isNotNull();
		assertThat(dec).isEqualTo(testMessageBytes);
	}

	@ParameterizedTest(name = "String enc/dec roundtrip (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testGlobalAsymmCryptoStrEncDec(AsymmCryptoType type) {
		// Create Keypair
		var keypair = crypto.generateKeypair(type);
		assertThat(keypair).isNotNull();

		// Encrypt with Public Enc Key
		String enc = crypto.encryptStr(testMessageStr, keypair.pub().serialize());
		assertThat(enc).isNotNull();

		// Decrypt with Private Enc Key
		String dec = crypto.decryptStr(enc, keypair.priv().serialize());
		assertThat(dec).isNotNull();
		assertThat(dec).isNotEqualTo(enc);

		// Check
		log.info("Decrypted message: \"{}\" = \"{}\" ?", testMessageStr, dec);
		assertThat(dec).isEqualTo(testMessageStr);
	}

	@ParameterizedTest(name = "Raw sign/verify (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testGlobalAsymmCryptoRawSignVerify(AsymmCryptoType type) {
		var keypair = crypto.generateKeypair(type);
		assertThat(keypair).isNotNull();

		byte[] sig = crypto.signRaw(testMessageBytes, keypair.priv().serialize());
		assertThat(sig).isNotNull();

		boolean valid = crypto.verifyRaw(testMessageBytes, sig, keypair.pub().serialize());
		assertThat(valid).isTrue();
	}

	@ParameterizedTest(name = "Encoded sign/verify (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testGlobalAsymmCryptoSignVerify(AsymmCryptoType type) {
		var keypair = crypto.generateKeypair(type);
		assertThat(keypair).isNotNull();

		String sig = crypto.sign(testMessageBytes, keypair.priv().serialize());
		assertThat(sig).isNotNull();

		boolean valid = crypto.verify(testMessageBytes, sig, keypair.pub().serialize());
		assertThat(valid).isTrue();
	}

	@ParameterizedTest(name = "String sign/verify (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testGlobalAsymmCryptoStrSignVerify(AsymmCryptoType type) {
		var keypair = crypto.generateKeypair(type);
		assertThat(keypair).isNotNull();

		String sig = crypto.signStr(testMessageStr, keypair.priv().serialize());
		assertThat(sig).isNotNull();

		boolean valid = crypto.verifyStr(testMessageStr, sig, keypair.pub().serialize());
		assertThat(valid).isTrue();
	}

	// Helper Method to get all Types and Sizes
	static Stream<Arguments> cryptoTypeAndSizes() {
		return Stream.of(AsymmCryptoType.values())
				.flatMap(type -> Stream.of(
						0,
						1,
						10,
						100,
						200,
						1_000,
						10_000,
						100_000,
						1_000_000,
						10_000_000,
						100_000_000
				).map(size -> Arguments.of(type, size)));
	}

	@ParameterizedTest(name = "Raw enc/dec roundtrip with sizes (type={0}, size={1})")
	@MethodSource("cryptoTypeAndSizes")
	void testGlobalAsymmCryptoRawEncDecSizes(AsymmCryptoType type, int size) {
		// Create data
		byte[] data = new byte[size];
		ThreadLocalRandom.current().nextBytes(data);

		// Create Keypair
		var keypair = crypto.generateKeypair(type);
		assertThat(keypair).isNotNull();

		// Encrypt with Public Enc Key
		byte[] enc = crypto.encryptRaw(data, keypair.pub().serialize());
		assertThat(enc).isNotNull();

		// Decrypt with Private Enc Key
		byte[] dec = crypto.decryptRaw(enc, keypair.priv().serialize());
		assertThat(dec).isNotNull();
		assertThat(dec).isNotEqualTo(enc);
		assertThat(dec).isEqualTo(data);
	}

	@ParameterizedTest(name = "Raw sign/verify with sizes (type={0}, size={1})")
	@MethodSource("cryptoTypeAndSizes")
	void testGlobalAsymmCryptoRawSignVerifySizes(AsymmCryptoType type, int size) {
		// Create Keypair
		var keypair = crypto.generateKeypair(type);
		assertThat(keypair).isNotNull();

		// Create data
		byte[] data = new byte[size];
		ThreadLocalRandom.current().nextBytes(data);

		// Sign with Private
		byte[] sig = crypto.signRaw(data, keypair.priv().serialize());
		assertThat(sig).isNotNull();

		// Verify with Public
		boolean valid = crypto.verifyRaw(data, sig, keypair.pub().serialize());
		assertThat(valid).isTrue();
	}
}
