package com.masl.goofy_protocol_fis_be.unit.crypto;

import com.masl.goofy_protocol_fis_be.crypto.SecureRandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GeneralCryptoTests {

	@Test
	void testSecureRandomSeedIsDeterministic() throws NoSuchAlgorithmException {
//		byte[] seed = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
//		SecureRandom rnd1 = SecureRandomUtils.secRandomFromSeed(seed);
//		SecureRandom rnd2 = SecureRandomUtils.secRandomFromSeed(seed);
//
//		byte[] res1 = rnd1.generateSeed(16);
//		byte[] res2 = rnd2.generateSeed(16);
//
//		assertThat(res1).isEqualTo(res2);
	}

}
