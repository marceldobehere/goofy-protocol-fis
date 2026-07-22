package com.masl.goofy_protocol_fis_be.integration.service;

import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.IsolatedHandleHelper;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCrypto;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.GlobAsymmCrypto;
import com.masl.goofy_protocol_core.crypto.isolated.symm.GlobSymmCrypto;
import com.masl.goofy_protocol_fis_be.IsolatedTestConfig;
import com.masl.goofy_protocol_fis_be.dto.both.IdentityStorageEntryDto;
import com.masl.goofy_protocol_fis_be.dto.both.ServiceEntryDto;
import com.masl.goofy_protocol_fis_be.dto.both.ServiceTableEntryDto;
import com.masl.goofy_protocol_fis_be.dto.both.TableColumnDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.masl.goofy_protocol_fis_be.integration.signed_request.SignedRequestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "tests-service-table-entry"})
@ContextConfiguration(initializers = IsolatedTestConfig.class)
class ServiceTableEntryTests {
	private static final String BASE = "/api/service-table";
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
	public ServiceTableEntryTests(MockMvc mvc, IdentityStorageEntryRepository identityRepository, ServiceEntryRepository serviceEntryRepository, BaseQuotaProperties baseQuotaProperties, ObjectMapper objectMapper, TestDataUser testDataUser) {
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
	void testCreateTable() throws Exception {
		AsymmCrypto.AsymmFullKeyPair identity = asymmCrypto.generateKeypair();
		String identityHandle = handleCrypto.deriveHandle(identity.pub().serialize());
		String serviceUuid  = prepareServiceEntry(identity, testDataUser.testUser);

		// Prepare
		ServiceTableEntryDto entry = new ServiceTableEntryDto();
		entry.setTableName("table_1_" + identityHandle);
		entry.setSchemaVersion(1);
		entry.setHandlesWithReadPerms(new String[]{});
		entry.setHandlesWithWritePerms(new String[]{});
		List<TableColumnDto> cols = new ArrayList<>();
		cols.add(new TableColumnDto(
				"id",
				TableColumnDto.Type.INT,
				0,
				Set.of(TableColumnDto.Constraint.PRIMARY_KEY, TableColumnDto.Constraint.NOT_NULL),
				null));
		cols.add(new TableColumnDto(
				"stringus",
				TableColumnDto.Type.VAR_STRING_N,
				100,
				Set.of(),
				null));
		cols.add(new TableColumnDto(
				"dingus_123",
				TableColumnDto.Type.VAR_STRING_N,
				200,
				Set.of(TableColumnDto.Constraint.NOT_NULL),
				"Hello \"you\", this is a 'test' /* comment ;-- inject"));
		entry.setColumns(cols.toArray(new TableColumnDto[0]));
		System.out.printf("Creating table entry: %s\n", objectMapper.writeValueAsString(entry));

		// Create
		String postResultStr = performSignedRequestStr(HttpMethod.POST, BASE + "/" + identityHandle + "/" + serviceUuid + "/entry", objectMapper.writeValueAsString(entry), identity, mvc, handleCrypto)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		ServiceTableEntryDto postResultDto = objectMapper.readValue(postResultStr, ServiceTableEntryDto.class);
		assertThat(postResultDto).isNotNull();
		String tableUuid = postResultDto.getTableUuid();
		System.out.println("Created table entry with UUID: " + tableUuid);

		// Check Lookup
		String resultStr = performSignedRequest(HttpMethod.GET, BASE + "/" + identityHandle + "/" + serviceUuid + "/entry/" + tableUuid, identity, mvc, handleCrypto)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		ServiceTableEntryDto resultDto = objectMapper.readValue(resultStr, ServiceTableEntryDto.class);
		assertThat(resultDto).isNotNull();
		System.out.printf("Retrieved table entry: %s\n", objectMapper.writeValueAsString(resultDto));

		// Check
		assertThat(resultDto.getTableName()).isEqualTo(entry.getTableName());
		assertThat(resultDto.getSchemaVersion()).isEqualTo(entry.getSchemaVersion());
		assertThat(resultDto.getHandlesWithReadPerms()).isEqualTo(entry.getHandlesWithReadPerms());
		assertThat(resultDto.getHandlesWithWritePerms()).isEqualTo(entry.getHandlesWithWritePerms());
		assertThat(resultDto.getColumns().length).isEqualTo(entry.getColumns().length);

		for (TableColumnDto ogCol : entry.getColumns()) {
			TableColumnDto rCol = null;
			for (TableColumnDto c : resultDto.getColumns())
				if (c.getColName().equals(ogCol.getColName())) {
					rCol = c;
					break;
				}
			assertThat(rCol).isNotNull();
			assertThat(ogCol.getType()).isEqualTo(rCol.getType());
			if (ogCol.getTypeSize() != 0)
				assertThat(ogCol.getTypeSize()).isEqualTo(rCol.getTypeSize());
			assertThat(ogCol.getConstraints().size()).isLessThanOrEqualTo(rCol.getConstraints().size()); // might add unique constraint to just primary key constraint
			assertThat(ogCol.getDefaultValue()).isEqualTo(rCol.getDefaultValue());
		}
	}

	@Test
	void testCreateTableAllColumns() throws Exception {
		AsymmCrypto.AsymmFullKeyPair identity = asymmCrypto.generateKeypair();
		String identityHandle = handleCrypto.deriveHandle(identity.pub().serialize());
		String serviceUuid  = prepareServiceEntry(identity, testDataUser.testUser);

		// Prepare
		ServiceTableEntryDto entry = new ServiceTableEntryDto();
		entry.setTableName("table_1_" + identityHandle);
		entry.setSchemaVersion(1);
		entry.setHandlesWithReadPerms(new String[]{});
		entry.setHandlesWithWritePerms(new String[]{});
		List<TableColumnDto> cols = new ArrayList<>();

		cols.add(new TableColumnDto(
				"id",
				TableColumnDto.Type.INT,
				0,
				Set.of(TableColumnDto.Constraint.PRIMARY_KEY, TableColumnDto.Constraint.NOT_NULL),
				null));
		cols.add(new TableColumnDto(
				"stringus",
				TableColumnDto.Type.FIXED_STRING_N,
				100,
				Set.of(),
				null));
		cols.add(new TableColumnDto(
				"dingus_123",
				TableColumnDto.Type.VAR_STRING_N,
				200,
				Set.of(TableColumnDto.Constraint.UNIQUE),
				null));



		cols.add(new TableColumnDto(
				"string1",
				TableColumnDto.Type.FIXED_STRING_N,
				10,
				Set.of(),
				"1234567890"));
		cols.add(new TableColumnDto(
				"string2",
				TableColumnDto.Type.VAR_STRING_N,
				10,
				Set.of(),
				"1234567890"));
		cols.add(new TableColumnDto(
				"bool1",
				TableColumnDto.Type.BOOLEAN,
				0,
				Set.of(),
				true));
		cols.add(new TableColumnDto(
				"tinyint",
				TableColumnDto.Type.TINYINT,
				0,
				Set.of(),
				-128)); // signed
		cols.add(new TableColumnDto(
				"smallint",
				TableColumnDto.Type.SMALLINT,
				0,
				Set.of(),
				32767)); // signed
		cols.add(new TableColumnDto(
				"int",
				TableColumnDto.Type.INT,
				0,
				Set.of(),
				2_000_000_000));
		cols.add(new TableColumnDto(
				"bigint",
				TableColumnDto.Type.BIGINT,
				0,
				Set.of(),
				200_000_000_000L));
		cols.add(new TableColumnDto(
				"float",
				TableColumnDto.Type.FLOAT,
				0,
				Set.of(),
				123.456));
		cols.add(new TableColumnDto(
				"double",
				TableColumnDto.Type.DOUBLE,
				0,
				Set.of(),
				123.4567890));
		cols.add(new TableColumnDto(
				"date1",
				TableColumnDto.Type.DATE,
				0,
				Set.of(),
				"2026-07-21"));
		cols.add(new TableColumnDto(
				"time1",
				TableColumnDto.Type.TIME,
				0,
				Set.of(),
				"23:59:12"));

		entry.setColumns(cols.toArray(new TableColumnDto[0]));
		System.out.printf("Creating table entry: %s\n", objectMapper.writeValueAsString(entry));

		// Create
		String postResultStr = performSignedRequestStr(HttpMethod.POST, BASE + "/" + identityHandle + "/" + serviceUuid + "/entry", objectMapper.writeValueAsString(entry), identity, mvc, handleCrypto)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		ServiceTableEntryDto resultDto = objectMapper.readValue(postResultStr, ServiceTableEntryDto.class);
		assertThat(resultDto).isNotNull();
		String tableUuid = resultDto.getTableUuid();
		System.out.println("Created table entry with UUID: " + tableUuid);
		System.out.printf("Created table entry: %s\n", objectMapper.writeValueAsString(resultDto));

		// Check
		assertThat(resultDto.getTableName()).isEqualTo(entry.getTableName());
		assertThat(resultDto.getSchemaVersion()).isEqualTo(entry.getSchemaVersion());
		assertThat(resultDto.getHandlesWithReadPerms()).isEqualTo(entry.getHandlesWithReadPerms());
		assertThat(resultDto.getHandlesWithWritePerms()).isEqualTo(entry.getHandlesWithWritePerms());
		assertThat(resultDto.getColumns().length).isEqualTo(entry.getColumns().length);

		// Check Columns
		for (TableColumnDto ogCol : entry.getColumns()) {
			TableColumnDto rCol = null;
			for (TableColumnDto c : resultDto.getColumns())
				if (c.getColName().equals(ogCol.getColName())) {
					rCol = c;
					break;
				}
			assertThat(rCol).isNotNull();
			assertThat(ogCol.getType()).isEqualTo(rCol.getType());
			if (ogCol.getTypeSize() != 0)
				assertThat(ogCol.getTypeSize()).isEqualTo(rCol.getTypeSize());
			assertThat(ogCol.getConstraints().size()).isLessThanOrEqualTo(rCol.getConstraints().size()); // might add unique constraint to just primary key constraint
			assertThat(ogCol.getDefaultValue()).isEqualTo(rCol.getDefaultValue());
		}
	}

	// TODO: More Tests

	// TODO: Check multi col primary key behaviour and document!
}
