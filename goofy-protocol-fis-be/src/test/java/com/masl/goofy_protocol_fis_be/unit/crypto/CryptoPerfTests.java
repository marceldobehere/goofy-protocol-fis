package com.masl.goofy_protocol_fis_be.unit.crypto;

import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.masl.goofy_protocol_fis_be.crypto.HandleCrypto;
import com.masl.goofy_protocol_fis_be.crypto.SecretUtils;
import com.masl.goofy_protocol_fis_be.crypto.asymm.AsymmCrypto;
import com.masl.goofy_protocol_fis_be.crypto.asymm.AsymmCryptoType;
import com.masl.goofy_protocol_fis_be.crypto.asymm.GlobAsymmCrypto;
import com.masl.goofy_protocol_fis_be.crypto.symm.GlobSymmCrypto;
import com.masl.goofy_protocol_fis_be.crypto.symm.SymmCryptoType;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.Security;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.getProperty;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(JUnitPerfInterceptor.class)
@SpringBootTest
@Disabled // Don't run this during mvn clean install/test
class CryptoPerfTests {
	private static final String randomSecretBase = "bla bla bla randdom secret";
	private final GlobSymmCrypto symmCrypto = new GlobSymmCrypto();
	private final GlobAsymmCrypto asymmCrypto = new GlobAsymmCrypto();
	private final HandleCrypto handleCrypto = new HandleCrypto(false);

	@JUnitPerfTestActiveConfig
	private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
			.reportGenerator(new ConsoleReportGenerator())
			.reportGenerator(new HtmlReportGenerator(getProperty("user.dir") + "/build/reports/" + "perf-crypto.html"))
			.build();

    CryptoPerfTests() throws IOException {
    }

    @BeforeAll
	static void init() {
		Security.addProvider(new BouncyCastleProvider());
		Security.addProvider(new BouncyCastlePQCProvider());
	}


	@ParameterizedTest(name = "PERF: Raw enc/dec roundtrip with size 1_000_000 (type={0})")
	@EnumSource(SymmCryptoType.class)
	@JUnitPerfTest(durationMs = 15_000, rampUpPeriodMs = 2_000, warmUpMs = 3_000, maxExecutionsPerSecond = 15_000)
	void testGlobalSymmCryptoRawEncDecPerf(SymmCryptoType type) {
		// Create data
		final int size = 1_000_000;
		byte[] data = new byte[size];
		ThreadLocalRandom.current().nextBytes(data);

		// Encrypt with Secret
		String randomSecret = randomSecretBase + type.toString() + size;
		byte[] enc = symmCrypto.encryptRaw(data, randomSecret, type);
		assertThat(enc).isNotNull();

		// Decrypt with Secret
		byte[] dec = symmCrypto.decryptRaw(enc, randomSecret);
		assertThat(dec).isNotNull();
		assertThat(dec).isNotEqualTo(enc);
		assertThat(dec).isEqualTo(data);
	}


	// Helper Util
	ConcurrentHashMap<AsymmCryptoType, AsymmCrypto.AsymmFullKeyPair> cachedKeypairs = new ConcurrentHashMap<>();
	synchronized AsymmCrypto.AsymmFullKeyPair generateCachedKeypair(AsymmCryptoType type) {
		return cachedKeypairs.computeIfAbsent(type, _ -> {
			var keypair = asymmCrypto.generateKeypair(type);
			assertThat(keypair).isNotNull();
			return keypair;
		});
	}

	// Helper Util
	ConcurrentHashMap<AsymmCryptoType, byte[]> cachedEncData = new ConcurrentHashMap<>();
	synchronized byte[] generateCachedEncData(AsymmCryptoType type) {
		return cachedEncData.computeIfAbsent(type, _ -> {
			// Get Keypair & Random Data
			var keypair = generateCachedKeypair(type);
			var data = getCachedRandomData();

			// Encrypt with Public Enc Key
			return asymmCrypto.encryptRaw(data, keypair.pub().serialize());
		});
	}

	// Helper Util
	ConcurrentHashMap<AsymmCryptoType, byte[]> cachedSigData = new ConcurrentHashMap<>();
	synchronized byte[] generateCachedSigData(AsymmCryptoType type) {
		return cachedSigData.computeIfAbsent(type, _ -> {
			// Get Keypair & Random Data
			var keypair = generateCachedKeypair(type);
			var data = getCachedRandomData();

			// Sign with Private
			return asymmCrypto.signRaw(data, keypair.priv().serialize());
		});
	}

	// Helper Util
	byte[] cachedRandomData = null;
	synchronized byte[] getCachedRandomData() {
		if (cachedRandomData == null) {
			final int size = 1_000_000;
			byte[] data = new byte[size];
			ThreadLocalRandom.current().nextBytes(data);
			cachedRandomData = data;
		}
		return cachedRandomData;
	}

	@ParameterizedTest(name = "PERF: Raw enc with size 1MB (type={0})")
	@EnumSource(AsymmCryptoType.class)
	@JUnitPerfTest(durationMs = 10_000, rampUpPeriodMs = 2_000, warmUpMs = 3_000, maxExecutionsPerSecond = 15_000)
	void testGlobalAsymmCryptoRawEncPerfBig(AsymmCryptoType type) {
		// Get Keypair & Random Data
		var keypair = generateCachedKeypair(type);
		var data = getCachedRandomData();

		// Encrypt with Public Enc Key
		byte[] enc = asymmCrypto.encryptRaw(data, keypair.pub().serialize());
		assertThat(enc).isNotNull();
	}

	@ParameterizedTest(name = "PERF: Raw dec with size 1M (type={0})")
	@EnumSource(AsymmCryptoType.class)
	@JUnitPerfTest(durationMs = 10_000, rampUpPeriodMs = 2_000, warmUpMs = 3_000, maxExecutionsPerSecond = 15_000)
	void testGlobalAsymmCryptoRawDecPerfBig(AsymmCryptoType type) {
		// Get Keypair & Random Data
		var keypair = generateCachedKeypair(type);
		var data = getCachedRandomData();

		// Get Encrypted Data
		byte[] enc = generateCachedEncData(type);
		assertThat(enc).isNotNull();

		// Decrypt with Private Enc Key
		byte[] dec = asymmCrypto.decryptRaw(enc, keypair.priv().serialize());
		assertThat(dec).isNotNull();
		assertThat(dec).isNotEqualTo(enc);
		assertThat(dec).isEqualTo(data);
	}

	@ParameterizedTest(name = "PERF: Raw sign with size 1MB (type={0})")
	@EnumSource(AsymmCryptoType.class)
	@JUnitPerfTest(durationMs = 10_000, rampUpPeriodMs = 2_000, warmUpMs = 3_000, maxExecutionsPerSecond = 15_000)
	void testGlobalAsymmCryptoRawSignPerf(AsymmCryptoType type) {
		// Get Keypair & Random Data
		var keypair = generateCachedKeypair(type);
		var data = getCachedRandomData();

		// Sign with Private
		byte[] sig = asymmCrypto.signRaw(data, keypair.priv().serialize());
		assertThat(sig).isNotNull();
	}

	@ParameterizedTest(name = "PERF: Raw verify with size 1MB (type={0})")
	@EnumSource(AsymmCryptoType.class)
	@JUnitPerfTest(durationMs = 10_000, rampUpPeriodMs = 2_000, warmUpMs = 3_000, maxExecutionsPerSecond = 15_000)
	void testGlobalAsymmCryptoRawVerifyPerf(AsymmCryptoType type) {
		// Get Keypair & Random Data
		var keypair = generateCachedKeypair(type);
		var data = getCachedRandomData();

		// Get Signature
		byte[] sig = generateCachedSigData(type);
		assertThat(sig).isNotNull();

		// Verify with Public
		boolean valid = asymmCrypto.verifyRaw(data, sig, keypair.pub().serialize());
		assertThat(valid).isTrue();
	}


	@ParameterizedTest(name = "PERF: Keygen with size 1MB (type={0})")
	@EnumSource(AsymmCryptoType.class)
	@JUnitPerfTest(durationMs = 10_000, rampUpPeriodMs = 2_000, warmUpMs = 3_000, maxExecutionsPerSecond = 15_000)
	void testGlobalAsymmCryptoKeygenPerf(AsymmCryptoType type) {
		// Get Keypair
		var keypair = asymmCrypto.generateKeypair(type);
		assertThat(keypair).isNotNull();
	}

	@ParameterizedTest(name = "PERF: Raw enc/dec/sign/verify with size 1MB (type={0})")
	@EnumSource(AsymmCryptoType.class)
	@JUnitPerfTest(durationMs = 15_000, rampUpPeriodMs = 2_000, warmUpMs = 3_000, maxExecutionsPerSecond = 15_000)
	void testGlobalAsymmCryptoRawFullPerf(AsymmCryptoType type) {
		// Get Keypair & Random Data
		var keypair = generateCachedKeypair(type);

		// Create Data
		final int size = 1_000_000;
		byte[] data = new byte[size];
		ThreadLocalRandom.current().nextBytes(data);

		// Encrypt with Public Enc Key
		byte[] enc = asymmCrypto.encryptRaw(data, keypair.pub().serialize());
		assertThat(enc).isNotNull();

		// Decrypt with Private Enc Key
		byte[] dec = asymmCrypto.decryptRaw(enc, keypair.priv().serialize());
		assertThat(dec).isNotNull();
		assertThat(dec).isNotEqualTo(enc);
		assertThat(dec).isEqualTo(data);

		// Sign with Private
		byte[] sig = asymmCrypto.signRaw(data, keypair.priv().serialize());
		assertThat(sig).isNotNull();

		// Verify with Public
		boolean valid = asymmCrypto.verifyRaw(data, sig, keypair.pub().serialize());
		assertThat(valid).isTrue();
	}

	@ParameterizedTest(name = "PERF: Symmetric Key from Secret with size={0} and iterations={1}")
	@CsvSource({
			"128,1000",
			"128,100000",
			"128,500000",
			"128,1000000",
			"256,1000",
			"256,50000",
			"256,100000",
			"256,400000",
			"256,800000",
			"256,1000000",
	})
	@JUnitPerfTest(durationMs = 10_000, rampUpPeriodMs = 2_000, warmUpMs = 3_000, maxExecutionsPerSecond = 15_000)
	void testSecretSymmKey(int size, int iterations) {
		SecretUtils.symmSecretFromSecret(randomSecretBase, SecretUtils.DEFAULT_DETERMINISTIC_SALT, size, iterations);
	}

	@ParameterizedTest(name = "PERF: Cacheless Key Derivation (type={0})")
	@EnumSource(AsymmCryptoType.class)
	@JUnitPerfTest(durationMs = 10_000, rampUpPeriodMs = 2_000, warmUpMs = 3_000, maxExecutionsPerSecond = 15_000)
	void testCachelessKeyDerivation(AsymmCryptoType type) {
		// Get Keypair & Random Data
		var keypair = generateCachedKeypair(type);

		String handle = handleCrypto._internalDeriveHandle(keypair.pub().serialize());
		assertThat(handle).isNotNull();
	}
}
