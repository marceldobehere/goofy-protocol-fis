package com.masl.goofy_protocol_fis_be.unit.crypto;

import com.masl.goofy_protocol_core.crypto.connected.GenericHandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.request.BasicRequestValidator;
import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;
import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequestValidator;
import com.masl.goofy_protocol_core.crypto.exceptions.PubSplitKeyNotFound;
import com.masl.goofy_protocol_core.crypto.isolated.BaseCryptoTestBase;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCryptoType;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.GlobAsymmCrypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@Execution(ExecutionMode.CONCURRENT)
class SignedRequestTests extends BaseCryptoTestBase {
	private static final Logger log = LoggerFactory.getLogger(SignedRequestTests.class);

	// Using EC_C25519 & DEF_METHOD & DEF_PATH & DEF_BODY
	private static final Map<String, String> knownHeaders = Map.of(
		"X-Goofy-Public-Key", "PUB.EC_C25519.MCowBQYDK2VwAyEAUjDbklJIC-tS6DWPl50vl_jYh5pxx68rHCQ89gpEWWA=.MCowBQYDK2VuAyEAuaDP2Gxo36baT2Hl6M4vo3vG4wlku1Lud71pSHfO22U=.gFAbQl8p8Ln0igp0sl71UFJIjmeBCS5EP2De_2m7TSKdfOTd6IhlQAp6E0Ql87BZbR2e5iFp4qsvlpsfROxxDQ==",
		"X-Goofy-Signature", "2nGWawycJfKAkmJMHyj8nzEdKQ86RT3iNFinWAsbPVh-0LTBeC1ZZzE7FLpnhr4ANIhhvVWmQmAVboyO1feRDg==",
		"X-Goofy-Valid-Until", "1782942585680",
		"X-Goofy-Id", "-8979107366769658500"
	);

	private static final String DEF_DOMAIN = "rocc.systems";
	private static final String DEF_METHOD = "GET";
	private static final String DEF_PATH = "/api/test";
	private static final byte[] DEF_BODY = "abcde12345".repeat(100000).getBytes(StandardCharsets.UTF_8);

	private final GlobAsymmCrypto crypto = new GlobAsymmCrypto();
	private final SignedRequestValidator basicValidator = new BasicRequestValidator();

	private HandleCrypto handleCrypto;

	@BeforeEach
	void preTest() {
		handleCrypto = new HandleCrypto(new IsolatedHandleHelper());
	}

	void checkFields(SignedRequest original, SignedRequest check) {
		assertThat(original.pubSplitKey()).isEqualTo(check.pubSplitKey());
		assertThat(original.handle()).isEqualTo(check.handle());
		assertThat(original.signature()).isEqualTo(check.signature());
		assertThat(original.uniqueId()).isEqualTo(check.uniqueId());
		assertThat(original.validUntil().toEpochMilli()).isEqualTo(check.validUntil().toEpochMilli()); // Gets rounded
		assertThat(original.method()).isEqualTo(check.method());
		assertThat(original.pathHash()).isEqualTo(check.pathHash());
		assertThat(original.bodyHash()).isEqualTo(check.bodyHash());
	}

	@ParameterizedTest(name = "Create & Verify SignedRequest (type={0}) (Public Key, No Body)")
	@EnumSource(AsymmCryptoType.class)
	void testCreateAndVerifyRequestPubKeyNoBody(AsymmCryptoType type) throws PubSplitKeyNotFound {
		var keypair = crypto.generateKeypair(type);

		// Sign
		SignedRequest req = SignedRequest.fromParts(keypair, DEF_METHOD, DEF_PATH, handleCrypto);
		// Verify
		assertThat(req.isValid(handleCrypto, basicValidator)).isEqualTo(SignedRequest.SignedRequestValidity.VALID);

		// Turn into Headers
		Map<String, String> headers = req.toHeadersWithPubKey();

		// Check Headers
		assertThat(SignedRequest.hasAllRequestHeaders(headers)).isTrue();
		int headerSize = headers.entrySet().stream().reduce(0, (acc, e) -> acc + e.getKey().length() + e.getValue().length(), Integer::sum);
		log.info(" > SignedRequest Headers Size for {} & Pubkey: {} bytes", type, headerSize);

		// Reconstruct from Headers
		SignedRequest reconstructed = SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, handleCrypto);
		// Check fields
		checkFields(req, reconstructed);
		// Verify
		assertThat(reconstructed.isValid(handleCrypto, basicValidator)).isEqualTo(SignedRequest.SignedRequestValidity.VALID);
	}

	@ParameterizedTest(name = "Create & Verify SignedRequest (type={0}) (Public Key, With Body)")
	@EnumSource(AsymmCryptoType.class)
	void testCreateAndVerifyRequestPubKeyBody(AsymmCryptoType type) throws PubSplitKeyNotFound {
		var keypair = crypto.generateKeypair(type);

		// Sign
		SignedRequest req = SignedRequest.fromParts(keypair, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);
		// Verify
		assertThat(req.isValid(handleCrypto, basicValidator)).isEqualTo(SignedRequest.SignedRequestValidity.VALID);

		// Turn into Headers
		Map<String, String> headers = req.toHeadersWithPubKey();

		// Check Headers
		assertThat(SignedRequest.hasAllRequestHeaders(headers)).isTrue();
		int headerSize = headers.entrySet().stream().reduce(0, (acc, e) -> acc + e.getKey().length() + e.getValue().length(), Integer::sum);
		if (headerSize < 1024)
			log.info(" > SignedRequest Headers for {}: {}", type, headers);

		// Reconstruct from Headers
		SignedRequest reconstructed = SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);
		// Check fields
		checkFields(req, reconstructed);
		// Verify
		assertThat(reconstructed.isValid(handleCrypto, basicValidator)).isEqualTo(SignedRequest.SignedRequestValidity.VALID);
	}

	@ParameterizedTest(name = "Create & Verify SignedRequest (type={0}) (Handle)")
	@EnumSource(AsymmCryptoType.class)
	void testCreateAndVerifyRequestHandle(AsymmCryptoType type) throws PubSplitKeyNotFound {
		var keypair = crypto.generateKeypair(type);

		// Sign
		SignedRequest req = SignedRequest.fromParts(keypair, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);
		// Verify
		assertThat(req.isValid(handleCrypto, basicValidator)).isEqualTo(SignedRequest.SignedRequestValidity.VALID);

		// Turn into Headers
		Map<String, String> headers = req.toHeadersWithHandle();

		// Check Headers
		assertThat(SignedRequest.hasAllRequestHeaders(headers)).isTrue();
		int headerSize = headers.entrySet().stream().reduce(0, (acc, e) -> acc + e.getKey().length() + e.getValue().length(), Integer::sum);
		log.info(" > SignedRequest Headers Size for {} & Handle: {} bytes", type, headerSize);

		// Reconstruct from Headers (Using Fresh HandleCrypto -> Should Fail)
		HandleCrypto freshHandleCrypto = new HandleCrypto(new IsolatedHandleHelper());
		assertThrows(PubSplitKeyNotFound.class, () -> SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, DEF_BODY, freshHandleCrypto));

		// Reconstruct from Headers (Using Cached HandleCrypto)
		SignedRequest reconstructed = SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);
		// Check fields
		checkFields(req, reconstructed);
		// Verify
		assertThat(reconstructed.isValid(handleCrypto, basicValidator)).isEqualTo(SignedRequest.SignedRequestValidity.VALID);
	}


	@ParameterizedTest(name = "Check Domain Stripping of Handle (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testDomainStrippingRequestHandle(AsymmCryptoType type) throws PubSplitKeyNotFound {
		var keypair = crypto.generateKeypair(type);

		// Sign
		SignedRequest req = SignedRequest.fromParts(keypair, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

		// Turn into Headers
		Map<String, String> headers = req.toHeadersWithHandle(DEF_DOMAIN);

		// Check Headers
		assertThat(SignedRequest.hasAllRequestHeaders(headers)).isTrue();
		assertThat(headers.get("X-Goofy-Handle")).contains(DEF_DOMAIN);

		// Reconstruct from Headers (Using Fresh HandleCrypto -> Should Fail)
		HandleCrypto freshHandleCrypto = new HandleCrypto(new IsolatedHandleHelper());
		assertThrows(PubSplitKeyNotFound.class, () -> SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, DEF_BODY, freshHandleCrypto));

		// Reconstruct from Headers (Using Cached HandleCrypto, also strips domain part)
		SignedRequest reconstructed = SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);
		// Check fields
		checkFields(req, reconstructed);
		// Verify
		assertThat(reconstructed.isValid(handleCrypto, basicValidator)).isEqualTo(SignedRequest.SignedRequestValidity.VALID);
	}


	@ParameterizedTest(name = "Check Mocked Public Key Lookup from Handle (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void testRequestPubKeyLookup(AsymmCryptoType type) throws PubSplitKeyNotFound {
		var keypair = crypto.generateKeypair(type);
		String actualHandle = handleCrypto.deriveHandle(keypair.pub().serialize());

		// Create fresh & mocked HandleCrypto
		GenericHandleCrypto freshHandleCrypto = Mockito.mock(GenericHandleCrypto.class);
		Mockito.when(freshHandleCrypto.getPublicSplitKeyFromHandle(actualHandle + "@" + DEF_DOMAIN)).thenReturn(keypair.pub().serialize());

		// Sign
		SignedRequest req = SignedRequest.fromParts(keypair, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

		// Turn into Headers
		Map<String, String> headers = req.toHeadersWithHandle(DEF_DOMAIN);

		// Check Headers
		assertThat(SignedRequest.hasAllRequestHeaders(headers)).isTrue();
		assertThat(headers.get("X-Goofy-Handle")).contains(DEF_DOMAIN);

		// Reconstruct from Headers (Using Fresh HandleCrypto -> Should not Fail)
		SignedRequest reconstructed = SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, DEF_BODY, freshHandleCrypto);
		// Check fields
		checkFields(req, reconstructed);
		// Verify
		assertThat(reconstructed.isValid(handleCrypto, basicValidator)).isEqualTo(SignedRequest.SignedRequestValidity.VALID);
	}

	@Test
	void testKnownSignedRequestShouldHaveInvalidTime() throws PubSplitKeyNotFound {
		// Reconstruct from Headers (Using Fresh HandleCrypto -> Should not Fail)
		SignedRequest reconstructed = SignedRequest.fromRequestHeaders(knownHeaders, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

		// Check Headers
		assertThat(SignedRequest.hasAllRequestHeaders(knownHeaders)).isTrue();
		int headerSize = knownHeaders.entrySet().stream().reduce(0, (acc, e) -> acc + e.getKey().length() + e.getValue().length(), Integer::sum);

		// Verify
		assertThat(reconstructed.isValid(handleCrypto, basicValidator)).isEqualTo(SignedRequest.SignedRequestValidity.INVALID_TIME);
	}

	@Test
	void testKnownSignedRequestMockedTimeShouldBeValid() throws PubSplitKeyNotFound {
		// Create spy BasicRequestValidator with one mocked response
		BasicRequestValidator validator = Mockito.spy(BasicRequestValidator.class);
		doReturn(true).when(validator).isValidUntilValid(Mockito.any());

		// Reconstruct from Headers (Using Fresh HandleCrypto -> Should not Fail)
		SignedRequest reconstructed = SignedRequest.fromRequestHeaders(knownHeaders, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

		// Check Headers
		assertThat(SignedRequest.hasAllRequestHeaders(knownHeaders)).isTrue();
		int headerSize = knownHeaders.entrySet().stream().reduce(0, (acc, e) -> acc + e.getKey().length() + e.getValue().length(), Integer::sum);

		// Verify
		assertThat(reconstructed.isValid(handleCrypto, validator)).isEqualTo(SignedRequest.SignedRequestValidity.VALID);
	}
}
