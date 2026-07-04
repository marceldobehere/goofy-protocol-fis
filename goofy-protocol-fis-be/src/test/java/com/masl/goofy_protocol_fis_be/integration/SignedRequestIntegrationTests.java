package com.masl.goofy_protocol_fis_be.integration;

import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCrypto;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCryptoType;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.GlobAsymmCrypto;
import com.masl.goofy_protocol_fis_be.exception.client.AllClientErrorCodes;
import com.masl.goofy_protocol_fis_be.exception.server.AllServerErrorCodes;
import com.masl.goofy_protocol_fis_be.test_data.test_only.TestDataUser;
import com.masl.goofy_protocol_fis_be.unit.crypto.IsolatedHandleHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "tests-signed-req"})
@ContextConfiguration(initializers = IsolatedTestConfig.class)
class SignedRequestIntegrationTests {
	private static final String TEST_API_GUEST = "/api/test/test-guest";
	private static final String TEST_API_OUTSIDER = "/api/test/test-outsider";
	private static final String TEST_API_USER = "/api/test/test-user";
	private static final String TEST_API_ADMIN = "/api/test/test-admin";


	private final GlobAsymmCrypto crypto = new GlobAsymmCrypto();
	private HandleCrypto handleCrypto;

	private final MockMvc mvc;
	private final TestDataUser testDataUser;

	@Autowired
	public SignedRequestIntegrationTests(MockMvc mvc, TestDataUser testDataUser) {
		this.mvc = mvc;
        this.testDataUser = testDataUser;
	}

	@BeforeEach
	void preTest() {
		assertThat(testDataUser.testUser).isNotNull();
		assertThat(testDataUser.testAdmin).isNotNull();
		handleCrypto = new HandleCrypto(new IsolatedHandleHelper());
	}

	private ResultActions performUnsignedRequest(HttpMethod method, String path, byte[] body) {
		try {
			// Perform Request
			return mvc.perform(MockMvcRequestBuilders.request(method, path)
					.content(body));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ResultActions performSignedRequest(HttpMethod method, String path, byte[] body, AsymmCrypto.AsymmFullKeyPair keypair) {
		try {
			// Create Signed Request
			SignedRequest req = SignedRequest.fromParts(keypair, method.name(), path, body, handleCrypto);

			// Get Headers as MultiValueMap
			Map<String, String> headers = req.toHeadersWithPubKey();
			MultiValueMap<String, String> multiHeaders = new LinkedMultiValueMap<>();
			headers.forEach(multiHeaders::add);

			// Perform Request
			return mvc.perform(MockMvcRequestBuilders.request(method, path)
					.headers(new HttpHeaders(multiHeaders))
					.content(body));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void checkTestEndpointsGuest() throws Exception {
		// Test Guest Endpoint
		performUnsignedRequest(HttpMethod.GET, TEST_API_GUEST, null)
				.andExpect(status().isOk());

		// Test Outsider Endpoint
		performUnsignedRequest(HttpMethod.GET, TEST_API_OUTSIDER, null)
				.andExpect(status().isForbidden());

		// Test User Endpoint
		performUnsignedRequest(HttpMethod.GET, TEST_API_USER, null)
				.andExpect(status().isForbidden());

		// Test Admin Endpoint
		performUnsignedRequest(HttpMethod.GET, TEST_API_ADMIN, null)
				.andExpect(status().isForbidden());
	}

	@ParameterizedTest(name = "Check Test Endpoints as Outsider (type={0})")
	@EnumSource(AsymmCryptoType.class)
	void checkTestEndpointsOutsider(AsymmCryptoType type) throws Exception {
		var keypair = crypto.generateKeypair(type);

		// Test Guest Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_GUEST, null, keypair)
				.andExpect(status().isOk());

		// Test Outsider Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_OUTSIDER, null, keypair)
				.andExpect(status().isOk());

		// Test User Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_USER, null, keypair)
				.andExpect(status().isForbidden());

		// Test Admin Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_ADMIN, null, keypair)
				.andExpect(status().isForbidden());
	}

	@Test
	void checkTestEndpointsUser() throws Exception {
		var keypair = testDataUser.testUser;

		// Test Guest Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_GUEST, null, keypair)
				.andExpect(status().isOk());

		// Test Outsider Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_OUTSIDER, null, keypair)
				.andExpect(status().isOk());

		// Test User Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_USER, null, keypair)
				.andExpect(status().isOk());

		// Test Admin Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_ADMIN, null, keypair)
				.andExpect(status().isForbidden());
	}

	@Test
	void checkTestEndpointsAdmin() throws Exception {
		var keypair = testDataUser.testAdmin;

		// Test Guest Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_GUEST, null, keypair)
				.andExpect(status().isOk());

		// Test Outsider Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_OUTSIDER, null, keypair)
				.andExpect(status().isOk());

		// Test User Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_USER, null, keypair)
				.andExpect(status().isOk());

		// Test Admin Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_ADMIN, null, keypair)
				.andExpect(status().isOk());
	}

	@Test
	void unknownHandleLookupShouldFail() throws Exception {
		var keypair = crypto.generateKeypair();

		// Create Signed Request
		SignedRequest req = SignedRequest.fromParts(keypair, HttpMethod.GET.name(), TEST_API_OUTSIDER, null, handleCrypto);

		// Get Headers as MultiValueMap
		Map<String, String> headers = req.toHeadersWithHandle();
		MultiValueMap<String, String> multiHeaders = new LinkedMultiValueMap<>();
		headers.forEach(multiHeaders::add);

		// Test Outsider Endpoint
		var res = mvc.perform(MockMvcRequestBuilders.request(HttpMethod.GET, TEST_API_OUTSIDER)
				.headers(new HttpHeaders(multiHeaders)));

		// Check for correct result
		res.andExpect(status().is4xxClientError())
				.andExpect(jsonPath("$.errorCode").value(AllServerErrorCodes.PUBLIC_KEY_LOOKUP_FAILED));
	}

	@Test
	void invalidSignatureShouldFail() throws Exception {
		var keypair = crypto.generateKeypair();

		// Create Signed Request
		SignedRequest req = SignedRequest.fromParts(keypair, HttpMethod.GET.name(), TEST_API_OUTSIDER, null, handleCrypto);

		// Get Headers as MultiValueMap
		Map<String, String> headers = req.toHeadersWithPubKey();
		headers.put("X-Goofy-Signature", "BRUH");
		MultiValueMap<String, String> multiHeaders = new LinkedMultiValueMap<>();
		headers.forEach(multiHeaders::add);

		// Test Outsider Endpoint
		var res = mvc.perform(MockMvcRequestBuilders.request(HttpMethod.GET, TEST_API_OUTSIDER)
				.headers(new HttpHeaders(multiHeaders)));

		// Check for correct result
		res.andExpect(status().is4xxClientError())
				.andExpect(jsonPath("$.errorCode").value(AllClientErrorCodes.INVALID_SIGNATURE));
	}

	@Test
	void PartialSignedRequestShouldFail() throws Exception {
		var keypair = crypto.generateKeypair();

		// Create Signed Request
		SignedRequest req = SignedRequest.fromParts(keypair, HttpMethod.GET.name(), TEST_API_OUTSIDER, null, handleCrypto);

		// Get Headers as MultiValueMap
		Map<String, String> headers = req.toHeadersWithPubKey();
		headers.remove("X-Goofy-Valid-Until");
		MultiValueMap<String, String> multiHeaders = new LinkedMultiValueMap<>();
		headers.forEach(multiHeaders::add);

		// Test Outsider Endpoint
		var res = mvc.perform(MockMvcRequestBuilders.request(HttpMethod.GET, TEST_API_OUTSIDER)
				.headers(new HttpHeaders(multiHeaders)));

		// Check for correct result
		res.andExpect(status().is4xxClientError());
	}

	// TODO: Write Test using Body with e.g POST

	// TODO: Write Test to check Multipart Behaviour
}
