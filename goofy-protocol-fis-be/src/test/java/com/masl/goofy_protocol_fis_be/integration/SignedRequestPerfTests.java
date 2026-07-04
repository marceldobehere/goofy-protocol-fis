package com.masl.goofy_protocol_fis_be.integration;

import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCrypto;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCryptoType;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.GlobAsymmCrypto;
import com.masl.goofy_protocol_fis_be.test_data.test_only.TestDataUser;
import com.masl.goofy_protocol_core.crypto.connected.IsolatedHandleHelper;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.masl.goofy_protocol_fis_be.integration.SignedRequestUtils.performSignedRequest;
import static com.masl.goofy_protocol_fis_be.integration.SignedRequestUtils.performUnsignedRequest;
import static java.lang.System.getProperty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("perf")
@ExtendWith(JUnitPerfInterceptor.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = IsolatedTestConfig.class)
@Disabled // Don't run this during mvn clean install/test
class SignedRequestPerfTests {
	private static final String TEST_API_GUEST = "/api/test/test-guest";
	private static final String TEST_API_OUTSIDER = "/api/test/test-outsider";
	private static final String TEST_API_USER = "/api/test/test-user";
	private static final String TEST_API_ADMIN = "/api/test/test-admin";

	@JUnitPerfTestActiveConfig
	private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
			.reportGenerator(new ConsoleReportGenerator())
			.reportGenerator(new HtmlReportGenerator(getProperty("user.dir") + "/build/reports/" + "perf-req.html"))
			.build();

	private final GlobAsymmCrypto crypto = new GlobAsymmCrypto();
	private HandleCrypto handleCrypto;

	private final MockMvc mvc;
	private final TestDataUser testDataUser;

	@Autowired
	public SignedRequestPerfTests(MockMvc mvc, TestDataUser testDataUser) {
		this.mvc = mvc;
		this.testDataUser = testDataUser;
	}

	@BeforeEach
	void preTest() {
		AssertionsForClassTypes.assertThat(testDataUser.testUser).isNotNull();
		AssertionsForClassTypes.assertThat(testDataUser.testAdmin).isNotNull();
		handleCrypto = new HandleCrypto(new IsolatedHandleHelper());
	}

	// This is a fairly good baseline tests for how many requests can be done in general (during the test env)
	@Test
	@JUnitPerfTest(threads = 4, durationMs = 15_000, rampUpPeriodMs = 2_000, warmUpMs = 5_000, maxExecutionsPerSecond = 30_000)
	void checkTestEndpointsGuest() throws Exception {
		// Test Guest Endpoint
		performUnsignedRequest(HttpMethod.GET, TEST_API_GUEST, mvc)
				.andExpect(status().isOk());
	}

	// Do note, that this tests also includes the time needed to create the signatures which is expensive, for a better result check below
	@ParameterizedTest(name = "Check Test Endpoints as Outsider (type={0})")
	@EnumSource(AsymmCryptoType.class)
	@JUnitPerfTest(threads = 4, durationMs = 15_000, rampUpPeriodMs = 2_000, warmUpMs = 5_000, maxExecutionsPerSecond = 30_000)
	void checkTestEndpointsOutsider(AsymmCryptoType type) throws Exception {
		var keypair = crypto.generateKeypair(type);

		// Test Outsider Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_OUTSIDER, keypair, mvc, handleCrypto)
				.andExpect(status().isOk());
	}

	// Do note, that this tests also includes the time needed to create the signatures which is expensive, for a better result check below
	@Test
	@JUnitPerfTest(threads = 4, durationMs = 15_000, rampUpPeriodMs = 2_000, warmUpMs = 5_000, maxExecutionsPerSecond = 30_000)
	void checkTestEndpointsUser() throws Exception {
		var keypair = testDataUser.testUser;

		// Test User Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_USER, keypair, mvc, handleCrypto)
				.andExpect(status().isOk());
	}

	// Do note, that this tests also includes the time needed to create the signatures which is expensive, for a better result check below
	@Test
	@JUnitPerfTest(threads = 4, durationMs = 15_000, rampUpPeriodMs = 2_000, warmUpMs = 5_000, maxExecutionsPerSecond = 30_000)
	void checkTestEndpointsAdmin() throws Exception {
		var keypair = testDataUser.testAdmin;

		// Test Admin Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_ADMIN, keypair, mvc, handleCrypto)
				.andExpect(status().isOk());
	}

	@Test
	@JUnitPerfTest(threads = 8, durationMs = 15_000, rampUpPeriodMs = 2_000, warmUpMs = 5_000, maxExecutionsPerSecond = 30_000)
	void checkTestEndpointsNoRaceConditions() throws Exception {
		var keypair = testDataUser.testUser;

		// Test Guest Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_GUEST, keypair, mvc, handleCrypto)
				.andExpect(status().isOk());

		// Test Outsider Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_OUTSIDER, keypair, mvc, handleCrypto)
				.andExpect(status().isOk());

		// Test User Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_USER, keypair, mvc, handleCrypto)
				.andExpect(status().isOk());

		// Test Admin Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_ADMIN, keypair, mvc, handleCrypto)
				.andExpect(status().isForbidden());
	}

	// Helper Util
	ConcurrentHashMap<AsymmCryptoType, AsymmCrypto.AsymmFullKeyPair> cachedKeypairs = new ConcurrentHashMap<>();
	synchronized AsymmCrypto.AsymmFullKeyPair generateCachedKeypair(AsymmCryptoType type) {
		return cachedKeypairs.computeIfAbsent(type, _ -> {
			var keypair = crypto.generateKeypair(type);
			assertThat(keypair).isNotNull();
			return keypair;
		});
	}

	// Helper Util
	ConcurrentHashMap<String, SignedRequest> cachedReqs = new ConcurrentHashMap<>();
	synchronized SignedRequest generateCachedReq(AsymmCryptoType type, HttpMethod method, String path) {
		final String key = type.toString() + method.toString() + path;
		return cachedReqs.computeIfAbsent(key, _ -> {
			var keypair = generateCachedKeypair(type);

			// Create Signed Request
			return SignedRequest.fromParts(keypair, method.name(), path, handleCrypto);
		});
	}


	// This is better but not quite ideal, because we still need to generate the signatures, which is a bit expensive.
	@ParameterizedTest(name = "Check Test Endpoints as Outsider Cached Keypair (type={0})")
	@EnumSource(AsymmCryptoType.class)
	@JUnitPerfTest(threads = 4, durationMs = 15_000, rampUpPeriodMs = 2_000, warmUpMs = 5_000, maxExecutionsPerSecond = 30_000)
	void checkTestEndpointsOutsiderCached(AsymmCryptoType type) throws Exception {
		var keypair = generateCachedKeypair(type);

		// Test Outsider Endpoint
		performSignedRequest(HttpMethod.GET, TEST_API_OUTSIDER, keypair, mvc, handleCrypto)
				.andExpect(status().isOk());
	}

	// This is a better test to see how fast the server can process the requests, bc we dont need to generate keypairs or the signatures
	// For these tests to not fail, i have set up the main filter to ignore unique id checks if it is running in the "test" profile
	@ParameterizedTest(name = "Check Test Endpoints as Outsider Cached Request (type={0})")
	@EnumSource(AsymmCryptoType.class)
	@JUnitPerfTest(threads = 4, durationMs = 15_000, rampUpPeriodMs = 2_000, warmUpMs = 5_000, maxExecutionsPerSecond = 30_000)
	void checkTestEndpointsOutsiderCachedReq(AsymmCryptoType type) throws Exception {
		// Create Signed Request
		SignedRequest req = generateCachedReq(type, HttpMethod.GET, TEST_API_OUTSIDER);

		// Get Headers as MultiValueMap
		Map<String, String> headers = req.toHeadersWithPubKey();
		MultiValueMap<String, String> multiHeaders = new LinkedMultiValueMap<>();
		headers.forEach(multiHeaders::add);

		// Perform Request
		mvc.perform(MockMvcRequestBuilders.request(HttpMethod.GET, TEST_API_OUTSIDER)
				.headers(new HttpHeaders(multiHeaders)))
				.andExpect(status().isOk());
	}
}
