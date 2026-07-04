package com.masl.goofy_protocol_core.crypto.connected;

import com.masl.goofy_protocol_core.crypto.isolated.BaseCryptoTestBase;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCrypto;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCryptoType;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.GlobAsymmCrypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class HandleTests extends BaseCryptoTestBase {
	private static final Logger log = LoggerFactory.getLogger(HandleTests.class);
	private static final String knownPubSplitKey = "PUB.EC_P256.MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAERCDEqWbiDmy3dM9G22qvRsZME_mYNP4Pjzr5l-RzOl_BYycCAwjjjfSiYcCanfPgJ2x6L5xqOpejjixBF6-47A==.X.X";
	private static final String knownPubSplitKeyHandle = "kaval_rigor_flats26014";

	private final GlobAsymmCrypto crypto = new GlobAsymmCrypto();

	private HandleCrypto handleCrypto;

	@BeforeEach
	void preTest() {
		handleCrypto = new HandleCrypto(new IsolatedHandleHelper());
	}

	@ParameterizedTest(name = "Keygen & handle derivation (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testHandleDerivation(AsymmCryptoType type) {
		var keypair = crypto.generateKeypair(type);
		if (keypair.priv().serialize().length() < 1024) {
			log.info("Generated pub  keypair for type {} ({}): {}", type, keypair.pub().serialize().length(), keypair.pub().serialize());
			log.info("Generated priv keypair for type {} ({}): {}", type, keypair.priv().serialize().length(), keypair.priv().serialize());
		} else {
			log.info("Generated pub  keypair for type {} ({}):", type, keypair.pub().serialize().length());
			log.info("Generated priv keypair for type {} ({})", type, keypair.priv().serialize().length());
		}
		assertThat(keypair).isNotNull();
		assertThat(crypto.checkPublicSplitKey(keypair.pub().serialize())).isTrue();

		String handle = handleCrypto.deriveHandle(keypair.pub().serialize());
		log.info(" > Derived handle for pub keypair: {}", handle);
		assertThat(handle).isNotNull();
		assertThat(handleCrypto.verifyKeyAndHandle(keypair.pub().serialize(), handle)).isTrue();
	}

	@ParameterizedTest(name = "Deterministic Keygen & handle derivation (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testDeterministicHandleDerivation(AsymmCryptoType type) {
		var keypair = crypto.generateKeypair(type);
		assertThat(keypair).isNotNull();
		String serialized = keypair.pub().serialize();

		// Use serialized pub split key
		String handle1 = handleCrypto.deriveHandle(serialized);
		assertThat(handle1).isNotNull();
		assertThat(handleCrypto.verifyKeyAndHandle(serialized, handle1)).isTrue();

		// parse serialized pub split key
		String handle2 = handleCrypto.deriveHandle(AsymmCrypto.AsymmPubKeyPair.parse(serialized));
		assertThat(handle1).isEqualTo(handle2);
	}

	@Test
	void testKnownHandleDerivation() {
		String handle = handleCrypto.deriveHandle(knownPubSplitKey);
		assertThat(handle).isNotNull();
		assertThat(handle).isEqualTo(knownPubSplitKeyHandle);
	}

	@ParameterizedTest(name = "Keygen & handle derivation + (cached) lookup (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testHandleDerivationLookup(AsymmCryptoType type) {
		var keypair = crypto.generateKeypair(type);
		assertThat(keypair).isNotNull();
		assertThat(crypto.checkPublicSplitKey(keypair.pub().serialize())).isTrue();

		String handle = handleCrypto.deriveHandle(keypair.pub().serialize());
		assertThat(handle).isNotNull();
		assertThat(handleCrypto.verifyKeyAndHandle(keypair.pub().serialize(), handle)).isTrue();

		String pubSplitKey = handleCrypto.getPublicSplitKeyFromHandle(handle);
		assertThat(pubSplitKey).isNotNull();
		assertThat(pubSplitKey).isEqualTo(keypair.pub().serialize());
	}

	@ParameterizedTest(name = "Keygen & unknown lookup (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testHandleUnknownLookup(AsymmCryptoType type) {
		var keypair = crypto.generateKeypair(type);
		assertThat(keypair).isNotNull();
		assertThat(crypto.checkPublicSplitKey(keypair.pub().serialize())).isTrue();

		// Derive it without interacting with the cache
		String handle = handleCrypto._internalDeriveHandle(keypair.pub().serialize());
		assertThat(handle).isNotNull();

		// Verify without cache
		assertThat(handleCrypto.verifyKeyAndHandle(keypair.pub().serialize(), handle, false)).isTrue();

		// Lookup Key (should not be cached -> should not be found)
		String pubSplitKey = handleCrypto.getPublicSplitKeyFromHandle(handle);
		assertThat(pubSplitKey).isNull();
	}
}
