package com.masl.goofy_protocol_fis_be.integration.service;

import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.IsolatedHandleHelper;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCrypto;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.GlobAsymmCrypto;
import com.masl.goofy_protocol_core.crypto.isolated.symm.GlobSymmCrypto;
import com.masl.goofy_protocol_fis_be.IsolatedTestConfig;
import com.masl.goofy_protocol_fis_be.dto.both.*;
import com.masl.goofy_protocol_fis_be.dto.request.query.*;
import com.masl.goofy_protocol_fis_be.dto.response.ServiceBucketQuotasDto;
import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import com.masl.goofy_protocol_fis_be.repository.IdentityStorageEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.ServiceEntryRepository;
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
import tools.jackson.databind.ObjectMapper;

import java.util.*;

import static com.masl.goofy_protocol_fis_be.integration.signed_request.SignedRequestUtils.performSignedRequest;
import static com.masl.goofy_protocol_fis_be.integration.signed_request.SignedRequestUtils.performSignedRequestStr;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "tests-service-bucket-entry"})
@ContextConfiguration(initializers = IsolatedTestConfig.class)
class ServiceBucketEntryTests {
	private static final String BASE = "/api/service-bucket";
	private static final String SERVICE_BASE = "/api/service-entry";
	private static final String IDENTITY_BASE = "/api/identity-storage";

	private final HandleCrypto handleCrypto = new HandleCrypto(new IsolatedHandleHelper());
	private final GlobAsymmCrypto asymmCrypto = new GlobAsymmCrypto();
	private final GlobSymmCrypto symmCrypto = new GlobSymmCrypto();

	private final MockMvc mvc;
	private final IdentityStorageEntryRepository identityRepository;
	private final ServiceEntryRepository serviceEntryRepository;
	private final BaseQuotaProperties baseQuotaProperties;
	private final ObjectMapper objectMapper;
	private final TestDataUser testDataUser;

	private static final String encryptionPassword = "test password";

	@Autowired
	public ServiceBucketEntryTests(MockMvc mvc, IdentityStorageEntryRepository identityRepository, ServiceEntryRepository serviceEntryRepository, BaseQuotaProperties baseQuotaProperties, ObjectMapper objectMapper, TestDataUser testDataUser) {
		this.mvc = mvc;
        this.identityRepository = identityRepository;
        this.serviceEntryRepository = serviceEntryRepository;
        this.baseQuotaProperties = baseQuotaProperties;
        this.objectMapper = objectMapper;
        this.testDataUser = testDataUser;
    }

	@BeforeEach
	@Transactional
	void prepServiceEntry() {
		// keep deterministic for each test
		serviceEntryRepository.deleteAll();
		identityRepository.deleteAll();
	}

	private IdentityStorageEntryDto createStorageDto(AsymmCrypto.AsymmFullKeyPair identity) {
		String identityHandle = handleCrypto.deriveHandle(identity.pub().serialize());

		IdentityStorageEntryDto dto = new IdentityStorageEntryDto();
		dto.setName("name_" + identityHandle);
		dto.setHandle(identityHandle);
		dto.setPubSplitKey(identity.pub().serialize());

		String encData = symmCrypto.encryptStr(identity.priv().serialize(), encryptionPassword);
		dto.setEncKeypairEntry(encData);
		dto.setEncKeypairEntrySignature(asymmCrypto.signStr(encData, identity.priv().serialize()));
		return dto;
	}

	private void createIdentityForAuthRole(AsymmCrypto.AsymmFullKeyPair identity, AsymmCrypto.AsymmFullKeyPair user) throws Exception {
		IdentityStorageEntryDto entry = createStorageDto(identity);
		performSignedRequestStr(HttpMethod.POST, IDENTITY_BASE, objectMapper.writeValueAsString(entry), user, mvc, handleCrypto)
				.andExpect(status().isOk());
	}

	private ServiceEntryDto createServiceEntryDto(String name, String usedService) {
		ServiceEntryDto dto = new ServiceEntryDto();
		dto.setName(name);
		dto.setUsedService(usedService);
		dto.setUuid(null);
		return dto;
	}

	String prepareServiceEntry(AsymmCrypto.AsymmFullKeyPair identity, AsymmCrypto.AsymmFullKeyPair user) throws Exception {
		createIdentityForAuthRole(identity, user);
		String identityHandle = handleCrypto.deriveHandle(identity.pub().serialize());

		ServiceEntryDto entry = createServiceEntryDto("se_" + identityHandle, "testService");
		return performSignedRequestStr(HttpMethod.POST, SERVICE_BASE, objectMapper.writeValueAsString(entry), identity, mvc, handleCrypto)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	void testGetBucketPermissions() throws Exception {
		AsymmCrypto.AsymmFullKeyPair identity = asymmCrypto.generateKeypair();
		String serviceUuid  = prepareServiceEntry(identity, testDataUser.testUser);

		// Check Permissions
		String resultStr = performSignedRequest(HttpMethod.GET, BASE + "/" + serviceUuid + "/perms", identity, mvc, handleCrypto)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		ServiceBucketPermissionDto permsDto = objectMapper.readValue(resultStr, ServiceBucketPermissionDto.class);
		System.out.printf("Retrieved Bucket perms: %s\n", resultStr);
		assertThat(permsDto).isNotNull();
		assertThat(permsDto.getHandlesWithReadPerms()).isEmpty();
		assertThat(permsDto.getHandlesWithWritePerms()).isEmpty();
	}

	@Test
	void testSetBucketPermissions() throws Exception {
		AsymmCrypto.AsymmFullKeyPair identity = asymmCrypto.generateKeypair();
		String serviceUuid  = prepareServiceEntry(identity, testDataUser.testUser);

		ServiceBucketPermissionDto newPermsDto = new ServiceBucketPermissionDto();
		newPermsDto.setHandlesWithReadPerms(new String[]{"handle1", "handle2"});
		newPermsDto.setHandlesWithWritePerms(new String[]{"handle3", "handle4"});

		// Set Permissions
		performSignedRequestStr(HttpMethod.PUT, BASE + "/" + serviceUuid + "/perms", objectMapper.writeValueAsString(newPermsDto), identity, mvc, handleCrypto)
				.andExpect(status().isOk());

		// Check Permissions
		String resultStr = performSignedRequest(HttpMethod.GET, BASE + "/" + serviceUuid + "/perms", identity, mvc, handleCrypto)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		ServiceBucketPermissionDto permsDto = objectMapper.readValue(resultStr, ServiceBucketPermissionDto.class);
		System.out.printf("Retrieved Bucket perms: %s\n", resultStr);
		assertThat(permsDto).isNotNull();
		assertThat(permsDto.getHandlesWithReadPerms()).containsAll(Arrays.asList(newPermsDto.getHandlesWithReadPerms()));
		assertThat(permsDto.getHandlesWithWritePerms()).containsAll(Arrays.asList(newPermsDto.getHandlesWithWritePerms()));
	}

	@Test
	void testGetBucketQuotas() throws Exception {
		AsymmCrypto.AsymmFullKeyPair identity = asymmCrypto.generateKeypair();
		String identityHandle = handleCrypto.deriveHandle(identity.pub().serialize());
		String serviceUuid  = prepareServiceEntry(identity, testDataUser.testUser);

		// Get Quotas
		String resultStr = performSignedRequest(HttpMethod.GET, BASE + "/" + identityHandle + "/" + serviceUuid + "/quotas", identity, mvc, handleCrypto)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		ServiceBucketQuotasDto quotasDto = objectMapper.readValue(resultStr, ServiceBucketQuotasDto.class);
		System.out.printf("Retrieved Bucket quotas: %s\n", resultStr);
		assertThat(quotasDto).isNotNull();
		assertThat(quotasDto.getMaxBucketSize()).isEqualTo(baseQuotaProperties.getBucket().getMaxBucketSize());
		assertThat(quotasDto.getMaxItemSize()).isEqualTo(baseQuotaProperties.getBucket().getMaxItemSize());
		assertThat(quotasDto.getMaxItemCount()).isEqualTo(baseQuotaProperties.getBucket().getMaxItemCount());
		assertThat(quotasDto.getMaxUniquePermissionCount()).isEqualTo(baseQuotaProperties.getBucket().getMaxPermissionCount());

		assertThat(quotasDto.getCurrentBucketSize()).isEqualTo(0L);
		assertThat(quotasDto.getCurrentItemCount()).isEqualTo(0);
	}

	// TODO: Implement remaining tests

	@Test
	void testUploadRandomBucketEntry() throws Exception {
		// @PostMapping("/{idHandle}/{serviceUuid}/upload")
		// Required Input:
		// - PathVariable: idHandle (String)
		// - PathVariable: serviceUuid (String)
		// - RequestBody: byte[] body
		// - Header: Content-Type (String)
		// - Header: X-Filename (String)
		// - Optional Header: X-Cache-Duration (CacheDuration)
		// - Auth principal: GoofyAuthUser auth
		// PreAuthorize:
		// - hasRole('ROLE_OUTSIDE_ENTITY')
	}

	@Test
	void testUploadUuidBucketEntry() throws Exception {
		// @PostMapping("/{idHandle}/{serviceUuid}/upload/{fileUuid}")
		// Required Input:
		// - PathVariable: idHandle (String)
		// - PathVariable: serviceUuid (String)
		// - PathVariable: fileUuid (String)
		// - RequestBody: byte[] body
		// - Header: Content-Type (String)
		// - Optional Header: X-Filename (String)  (note: parameter marked required = false)
		// - Auth principal: GoofyAuthUser auth
		// PreAuthorize:
		// - hasRole('ROLE_OUTSIDE_ENTITY')
	}

	@Test
	void testGetBucketEntry() throws Exception {
		// @GetMapping("/{idHandle}/{serviceUuid}/entry/{fileUuid}")
		// Required Input:
		// - PathVariable: idHandle (String)
		// - PathVariable: serviceUuid (String)
		// - PathVariable: fileUuid (String)
		// - Auth principal: GoofyAuthUser auth
		// PreAuthorize:
		// - hasRole('ROLE_OUTSIDE_ENTITY')
	}

	@Test
	void testSetBucketEntry() throws Exception {
		// @PutMapping("/{idHandle}/{serviceUuid}/entry/{fileUuid}")
		// Required Input:
		// - PathVariable: idHandle (String)
		// - PathVariable: serviceUuid (String)
		// - PathVariable: fileUuid (String)
		// - RequestBody: ServiceBucketEntryDto entryDto
		// - Auth principal: GoofyAuthUser auth
		// PreAuthorize:
		// - hasRole('ROLE_OUTSIDE_ENTITY')
	}

	@Test
	void testDeleteBucketEntry() throws Exception {
		// @DeleteMapping("/{idHandle}/{serviceUuid}/entry/{fileUuid}")
		// Required Input:
		// - PathVariable: idHandle (String)
		// - PathVariable: serviceUuid (String)
		// - PathVariable: fileUuid (String)
		// - Auth principal: GoofyAuthUser auth
		// PreAuthorize:
		// - hasRole('ROLE_OUTSIDE_ENTITY')
	}

	@Test
	void testGetAllBucketEntries() throws Exception {
		// @GetMapping("/{idHandle}/{serviceUuid}/entry")
		// Required Input:
		// - PathVariable: idHandle (String)
		// - PathVariable: serviceUuid (String)
		// - Auth principal: GoofyAuthUser auth
		// PreAuthorize:
		// - hasRole('ROLE_OUTSIDE_ENTITY')
	}

	@Test
	void testGetBucketEntryContent() throws Exception {
		// @GetMapping("/{idHandle}/{serviceUuid}/content/{fileUuid}")
		// Required Input:
		// - PathVariable: idHandle (String)
		// - PathVariable: serviceUuid (String)
		// - PathVariable: fileUuid (String)
		// - Auth principal: GoofyAuthUser auth
		// - Returns: ResponseEntity<byte[]>
		// PreAuthorize:
		// - hasRole('ROLE_OUTSIDE_ENTITY')
	}

	// TODO: Access / Permission Tests

	// TODO: Test Restricted User cannot insert, delete, or update entries
}
