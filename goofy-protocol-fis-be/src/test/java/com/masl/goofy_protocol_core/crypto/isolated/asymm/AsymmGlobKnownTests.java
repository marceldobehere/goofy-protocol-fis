package com.masl.goofy_protocol_core.crypto.isolated.asymm;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.security.Security;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class AsymmGlobKnownTests {
	private static final Set<Map.Entry<AsymmCryptoType, AsymmGlobKnownValues.KnownValueSet>> knownValueEntries = AsymmGlobKnownValues.knownValues.entrySet();
	private final GlobAsymmCrypto crypto = new GlobAsymmCrypto();

	@BeforeAll
    static void init() {
		Security.addProvider(new BouncyCastleProvider());
		Security.addProvider(new BouncyCastlePQCProvider());
	}

	@ParameterizedTest(name = "Global Asymmetric Decryption using known values ({0})")
	@FieldSource("knownValueEntries")
	void testGlobalAsymmCryptoDecryptKnownValues(Map.Entry<AsymmCryptoType, AsymmGlobKnownValues.KnownValueSet> value) {
		// Check Public Keypair
		AsymmGlobKnownValues.KnownValueSet set = value.getValue();
		crypto.checkPublicSplitKey(set.keypair().pub().serialize());

		// Run Decryption
		String strDec = crypto.decryptStr(set.strEnc(), set.keypair().priv().serialize());
		byte[] rawDec = crypto.decryptRaw(set.rawEnc(), set.keypair().priv().serialize());
		byte[] defDec = crypto.decrypt(set.defEnc(), set.keypair().priv().serialize());

		// Check Decryption
		assertThat(strDec).isEqualTo(set.strOg());
		assertThat(rawDec).isEqualTo(set.rawDefOg());
		assertThat(defDec).isEqualTo(set.rawDefOg());
	}

	@ParameterizedTest(name = "Global Asymmetric Verification using known values ({0})")
	@FieldSource("knownValueEntries")
	void testGlobalAsymmCryptoVerifyKnownValues(Map.Entry<AsymmCryptoType, AsymmGlobKnownValues.KnownValueSet> value) {
		// Check Public Keypair
		AsymmGlobKnownValues.KnownValueSet set = value.getValue();
		crypto.checkPublicSplitKey(set.keypair().pub().serialize());

		// Run Verification
		boolean strVer = crypto.verifyStr(set.strOg(), set.strSig(), set.keypair().pub().serialize());
		boolean rawVer = crypto.verifyRaw(set.rawDefOg(), set.rawSig(), set.keypair().pub().serialize());
		boolean defVer = crypto.verify(set.rawDefOg(), set.defSig(), set.keypair().pub().serialize());

		// Check Verification
		assertThat(strVer).isTrue();
		assertThat(rawVer).isTrue();
		assertThat(defVer).isTrue();
	}

	@ParameterizedTest(name = "Global Asymmetric Encryption using known values ({0})")
	@FieldSource("knownValueEntries")
	void testGlobalAsymmCryptoEncryptKnownValues(Map.Entry<AsymmCryptoType, AsymmGlobKnownValues.KnownValueSet> value) {
		// Check Public Keypair
		AsymmGlobKnownValues.KnownValueSet set = value.getValue();
		crypto.checkPublicSplitKey(set.keypair().pub().serialize());

		// Run Encryption
		String strEnc = crypto.encryptStr(set.strOg(), set.keypair().pub().serialize());
		byte[] rawEnc = crypto.encryptRaw(set.rawDefOg(), set.keypair().pub().serialize());
		String defEnc = crypto.encrypt(set.rawDefOg(), set.keypair().pub().serialize());

		// Check Encryption
		assertThat(strEnc).isNotNull();
		assertThat(rawEnc).isNotNull();
		assertThat(defEnc).isNotNull();

		// Run Decryption
		String strDec = crypto.decryptStr(strEnc, set.keypair().priv().serialize());
		byte[] rawDec = crypto.decryptRaw(rawEnc, set.keypair().priv().serialize());
		byte[] defDec = crypto.decrypt(defEnc, set.keypair().priv().serialize());

		// Check Decryption
		assertThat(strDec).isEqualTo(set.strOg());
		assertThat(rawDec).isEqualTo(set.rawDefOg());
		assertThat(defDec).isEqualTo(set.rawDefOg());
	}

	@ParameterizedTest(name = "Global Asymmetric Sign using known values ({0})")
	@FieldSource("knownValueEntries")
	void testGlobalAsymmCryptoSignKnownValues(Map.Entry<AsymmCryptoType, AsymmGlobKnownValues.KnownValueSet> value) {
		// Check Public Keypair
		AsymmGlobKnownValues.KnownValueSet set = value.getValue();
		crypto.checkPublicSplitKey(set.keypair().pub().serialize());

		// Run Verification
		String strSig = crypto.signStr(set.strOg(), set.keypair().priv().serialize());
		byte[] rawSig = crypto.signRaw(set.rawDefOg(), set.keypair().priv().serialize());
		String defSig = crypto.sign(set.rawDefOg(), set.keypair().priv().serialize());

		// Check Verification
		assertThat(strSig).isNotNull();
		assertThat(rawSig).isNotNull();
		assertThat(defSig).isNotNull();

		// Run Verification
		boolean strVer = crypto.verifyStr(set.strOg(), strSig, set.keypair().pub().serialize());
		boolean rawVer = crypto.verifyRaw(set.rawDefOg(), rawSig, set.keypair().pub().serialize());
		boolean defVer = crypto.verify(set.rawDefOg(), defSig, set.keypair().pub().serialize());

		// Check Verification
		assertThat(strVer).isTrue();
		assertThat(rawVer).isTrue();
		assertThat(defVer).isTrue();
	}
}
