package com.masl.goofy_protocol_fis_be.unit.crypto.asymm;

import com.masl.goofy_protocol_fis_be.crypto.asymm.GlobAsymmCrypto;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AsymmGlobTests {
	private static final Logger log = LoggerFactory.getLogger(AsymmGlobTests.class);

	@Test
	void testGlobalAsymmCryptoKeygen() {
		GlobAsymmCrypto crypto = new GlobAsymmCrypto();
		for (var type : crypto.getTypes()) {
			var keypair = crypto.generateKeypair(type);
			log.info("Generated keypair for type {}: {} & {}", type, keypair.pub().serialize(), keypair.priv().serialize());
			assertThat(keypair).isNotNull();
			assertThat(crypto.checkPublicSplitKey(keypair.pub().serialize())).isTrue();
		}
	}

	@Test
	void testGlobalAsymmCryptoEncDec() {
		GlobAsymmCrypto crypto = new GlobAsymmCrypto();
		for (var type : crypto.getTypes()) {
			var keypair = crypto.generateKeypair(type);
			assertThat(keypair).isNotNull();
			assertThat(crypto.checkPublicSplitKey(keypair.pub().serialize())).isTrue();

			String msg = "bruhus";
			byte[] enc = crypto.encrypt(msg.getBytes(StandardCharsets.UTF_8), keypair.pub().serialize());
			assertThat(enc).isNotNull();

			byte[] dec = crypto.decrypt(enc, keypair.priv().serialize());
			assertThat(dec).isNotNull();
			assertThat(dec).isNotEqualTo(enc);

			log.info("Decrypted message: {} = {}?", new String(dec, StandardCharsets.UTF_8), msg);
			assertThat(new String(dec, StandardCharsets.UTF_8)).isEqualTo(msg);
		}
	}

	@Test
	void testGlobalAsymmCryptoSignVerify() {
		GlobAsymmCrypto crypto = new GlobAsymmCrypto();
		for (var type : crypto.getTypes()) {
			var keypair = crypto.generateKeypair(type);
			assertThat(keypair).isNotNull();
			assertThat(crypto.checkPublicSplitKey(keypair.pub().serialize())).isTrue();

			String msg = "bruhus";
			byte[] data = msg.getBytes(StandardCharsets.UTF_8);

			byte[] sig = crypto.sign(data, keypair.priv().serialize());
			assertThat(sig).isNotNull();

			boolean valid = crypto.verify(data, sig, keypair.pub().serialize());
			assertThat(valid).isTrue();
		}
	}
}
