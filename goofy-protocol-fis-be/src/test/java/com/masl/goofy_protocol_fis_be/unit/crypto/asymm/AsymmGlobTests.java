package com.masl.goofy_protocol_fis_be.unit.crypto.asymm;

import com.masl.goofy_protocol_fis_be.crypto.asymm.AsymmCryptoType;
import com.masl.goofy_protocol_fis_be.crypto.asymm.GlobAsymmCrypto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AsymmGlobTests {

	@Test
	void contextLoads() {
		GlobAsymmCrypto crypto = new GlobAsymmCrypto();

		var keypair = crypto.generateKeypair(AsymmCryptoType.RSA_2048);
		assertThat(keypair).isNotNull();


	}

}
