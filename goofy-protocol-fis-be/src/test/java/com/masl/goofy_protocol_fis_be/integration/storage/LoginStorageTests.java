package com.masl.goofy_protocol_fis_be.integration.storage;

import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.IsolatedHandleHelper;
import com.masl.goofy_protocol_fis_be.IsolatedTestConfig;
import com.masl.goofy_protocol_fis_be.repository.LoginStorageEntryRepository;
import com.masl.goofy_protocol_fis_be.test_data.test_only.TestDataUser;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static com.masl.goofy_protocol_fis_be.integration.signed_request.SignedRequestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "tests-shared"})
@ContextConfiguration(initializers = IsolatedTestConfig.class)
class LoginStorageTests {
	private static final String BASE = "/api/login-storage";

	private static final String userHash1 = "user-hash-1";
	private static final String userHash2 = "user-hash-2";
	private static final String userHashInvalid = "invalid-user-hash";
	private static final String testKeypairData1 = "TEST KEYPAIR 1 YES";
	private static final String testKeypairData2 = "TEST KEYPAIR 2 YES";

	private final HandleCrypto handleCrypto = new HandleCrypto(new IsolatedHandleHelper());

	private final MockMvc mvc;
	private final LoginStorageEntryRepository entryRepository;
	private final TestDataUser testDataUser;

	@Autowired
	public LoginStorageTests(MockMvc mvc, LoginStorageEntryRepository entryRepository, TestDataUser testDataUser) {
		this.mvc = mvc;
        this.entryRepository = entryRepository;
        this.testDataUser = testDataUser;
    }

	@BeforeEach
	@Transactional
	void prepLoginStorage() {
		entryRepository.deleteAll();
	}

	@Test
	void testGetInvalidEntry() throws Exception {
		performUnsignedRequest(HttpMethod.GET, BASE + "/" + userHashInvalid, mvc)
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testInsertEntry() throws Exception {
		performSignedRequestStr(HttpMethod.POST, BASE + "/" + userHash1, testKeypairData1, testDataUser.testUser, mvc, handleCrypto)
				.andExpect(status().isOk());

		performUnsignedRequest(HttpMethod.GET, BASE + "/" + userHash1, mvc)
				.andExpect(status().isOk())
				.andExpect(result -> {
					String responseBody = result.getResponse().getContentAsString();
					assertThat(responseBody).isEqualTo(testKeypairData1);
				});
	}

	@Test
	void testInsertUpdateEntry() throws Exception {
		// First insert by testUser
		performSignedRequestStr(HttpMethod.POST, BASE + "/" + userHash1, testKeypairData1, testDataUser.testUser, mvc, handleCrypto)
				.andExpect(status().isOk());

		// Second insert with SAME usernameHash by same user should succeed and replace
		performSignedRequestStr(HttpMethod.POST, BASE + "/" + userHash1, testKeypairData2, testDataUser.testUser, mvc, handleCrypto)
				.andExpect(status().isOk());

		performUnsignedRequest(HttpMethod.GET, BASE + "/" + userHash1, mvc)
				.andExpect(status().isOk())
				.andExpect(result -> {
					String responseBody = result.getResponse().getContentAsString();
					assertThat(responseBody).isEqualTo(testKeypairData2);
				});
	}

	@Test
	void testInsertSameUserHashDifferentUsers() throws Exception {
		// Insert entry for usernameHash1 by testUser
		performSignedRequestStr(HttpMethod.POST, BASE + "/" + userHash1, testKeypairData1, testDataUser.testUser, mvc, handleCrypto)
				.andExpect(status().isOk());

		// Try to insert same usernameHash1 by different user
		// Should fail with LoginEntryAlreadyExists (mapped to 4xx)
		performSignedRequestStr(HttpMethod.POST, BASE + "/" + userHash1, testKeypairData2, testDataUser.testUserTwo, mvc, handleCrypto)
				.andExpect(status().is4xxClientError());

		// Ensure stored value didn't change
		performUnsignedRequest(HttpMethod.GET, BASE + "/" + userHash1, mvc)
				.andExpect(status().isOk())
				.andExpect(result -> {
					String responseBody = result.getResponse().getContentAsString();
					assertThat(responseBody).isEqualTo(testKeypairData1);
				});
	}

	@Test
	void testDeleteEntry() throws Exception {
		// Insert two entries for the same user (note: endpoint deletes by createdBy handle)
		performSignedRequestStr(HttpMethod.POST, BASE + "/" + userHash1, testKeypairData1, testDataUser.testUser, mvc, handleCrypto)
				.andExpect(status().isOk());

		performSignedRequestStr(HttpMethod.POST, BASE + "/" + userHash2, testKeypairData2, testDataUser.testUser, mvc, handleCrypto)
				.andExpect(status().isOk());

		// Delete all entries for that user
		performSignedRequest(HttpMethod.DELETE, BASE, testDataUser.testUser, mvc, handleCrypto)
				.andExpect(status().isOk());

		// Both should now be missing
		performUnsignedRequest(HttpMethod.GET, BASE + "/" + userHash1, mvc)
				.andExpect(status().is4xxClientError());

		performUnsignedRequest(HttpMethod.GET, BASE + "/" + userHash2, mvc)
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testInsertAuthCheck() throws Exception {
		performUnsignedRequestStr(HttpMethod.POST, BASE + "/" + userHash1, testKeypairData1, mvc)
				.andExpect(status().is4xxClientError());
	}
}
