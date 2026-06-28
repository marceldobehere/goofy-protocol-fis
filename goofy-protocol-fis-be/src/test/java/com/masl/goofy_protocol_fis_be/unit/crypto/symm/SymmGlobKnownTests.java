package com.masl.goofy_protocol_fis_be.unit.crypto.symm;

import com.masl.goofy_protocol_fis_be.crypto.symm.GlobSymmCrypto;
import com.masl.goofy_protocol_fis_be.crypto.symm.SymmCryptoType;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Security;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SymmGlobKnownTests {
	private static final Set<Map.Entry<SymmCryptoType, SymmGlobKnownValues.KnownValueSet>> knownValueEntries = SymmGlobKnownValues.knownValues.entrySet();
	private final GlobSymmCrypto crypto = new GlobSymmCrypto();

	@BeforeAll
    static void init() {
		Security.addProvider(new BouncyCastleProvider());
		Security.addProvider(new BouncyCastlePQCProvider());
	}

	@ParameterizedTest(name = "Global Symmetric Decryption using known values ({0})")
	@FieldSource("knownValueEntries")
	void testGlobalSymmCryptoDecryptKnownValues(Map.Entry<SymmCryptoType, SymmGlobKnownValues.KnownValueSet> value) {
		SymmGlobKnownValues.KnownValueSet set = value.getValue();

		// Run Decryption
		String strDec = crypto.decryptStr(set.strEnc(), set.secret());
		byte[] rawDec = crypto.decryptRaw(set.rawEnc(), set.secret());
		byte[] defDec = crypto.decrypt(set.defEnc(), set.secret());

		// Check Decryption
		assertThat(strDec).isEqualTo(set.strOg());
		assertThat(rawDec).isEqualTo(set.rawDefOg());
		assertThat(defDec).isEqualTo(set.rawDefOg());
	}

	@ParameterizedTest(name = "Global Symmetric Encryption using known values ({0})")
	@FieldSource("knownValueEntries")
	void testGlobalSymmCryptoEncryptKnownValues(Map.Entry<SymmCryptoType, SymmGlobKnownValues.KnownValueSet> value) {
		SymmGlobKnownValues.KnownValueSet set = value.getValue();

		// Run Encryption
		String strEnc = crypto.encryptStr(set.strOg(), set.secret());
		byte[] rawEnc = crypto.encryptRaw(set.rawDefOg(), set.secret());
		String defEnc = crypto.encrypt(set.rawDefOg(), set.secret());

		// Check Encryption
		assertThat(strEnc).isNotNull();
		assertThat(rawEnc).isNotNull();
		assertThat(defEnc).isNotNull();

		// Run Decryption
		String strDec = crypto.decryptStr(strEnc, set.secret());
		byte[] rawDec = crypto.decryptRaw(rawEnc, set.secret());
		byte[] defDec = crypto.decrypt(defEnc, set.secret());

		// Check Decryption
		assertThat(strDec).isEqualTo(set.strOg());
		assertThat(rawDec).isEqualTo(set.rawDefOg());
		assertThat(defDec).isEqualTo(set.rawDefOg());
	}
}
