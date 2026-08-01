package com.masl.goofy_protocol_fis_be.rest.storage;

import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.GlobAsymmCrypto;
import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.crypto.FisHandleCrypto;
import com.masl.goofy_protocol_fis_be.dto.both.IdentityStorageEntryDto;
import com.masl.goofy_protocol_fis_be.dto.response.MyIdentityEntryQuotasDto;
import com.masl.goofy_protocol_fis_be.entity.IdentityStorageEntry;
import com.masl.goofy_protocol_fis_be.entity.User;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.exception.client.*;
import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import com.masl.goofy_protocol_fis_be.repository.IdentityStorageEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.UserRepository;
import com.masl.goofy_protocol_fis_be.service.QuotaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/fis-api/identity-storage")
@Tag(name = "Identity Storage", description = "Endpoints relating to Identity Keypair Storage for Services. <br>Users can store their identity Keypairs encrypted here and use those for Service Access")
public class IdentityStorageEndpoint {
    private final IdentityStorageEntryRepository identityRepository;
    private final QuotaService quotaService;
    private final UserRepository userRepository;
    private final HandleCrypto handleCrypto;

    private final GlobAsymmCrypto asymmCrypto = new GlobAsymmCrypto();
    private final ObjectMapper mapper = new ObjectMapper();

    public IdentityStorageEndpoint(IdentityStorageEntryRepository identityRepository, QuotaService quotaService, UserRepository userRepository, FisHandleCrypto handleCrypto) {
        this.identityRepository = identityRepository;
        this.quotaService = quotaService;
        this.userRepository = userRepository;
        this.handleCrypto = handleCrypto;
    }

    @GetMapping("/quotas")
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Gets the Users Identity Entry related Quotas", description = "This Endpoint returns the maximum number of Identity Entries a user can have and how many they currently have stored.")
    public MyIdentityEntryQuotasDto getMyQuotas(@AuthenticationPrincipal GoofyAuthUser auth) {
        // Get Quotas
        BaseQuotaProperties userQuotas = quotaService.getUserQuotas(auth.getHandle());
        int quota = userQuotas.getIdentity().getMaxEntries();

        // Set DTO
        long count = identityRepository.countAllByCreatedByHandle(auth.getHandle());
        return new MyIdentityEntryQuotasDto(quota, count);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER') and not hasRole('ROLE_RESTRICTED')")
    @FisEndpoint(summary = "Sets an Identity Entry for a Handle", description = "If the handle isn't used in any other entry it will be saved in the users identity storage. <br>The entry request needs to have the encKeypair Data signed with the public key of the identity to be added to make sure it belongs to the user. <br>If the handle has an entry by the user, it will simply get updated. <br>Creates a default Public JSON Entry of `{\"services\": {}}`, if not defined")
    public void setEntry(@Valid @RequestBody IdentityStorageEntryDto entryDto, @AuthenticationPrincipal GoofyAuthUser auth) throws InvalidPublicKey, InvalidSignedObject, NotMatchingPublicKey, IdentityEntryAlreadyExists, IdentityEntryInvalid, IdentityEntryQuotaExceeded, InvalidJson {
        // Check Handle
        IdentityStorageEntryDto.checkValidity(entryDto, handleCrypto, asymmCrypto);

        // Check for old Entry that doesn't belong to the user
        IdentityStorageEntry oldEntry = identityRepository.findByHandle(entryDto.getHandle());
        if (oldEntry != null && !oldEntry.getCreatedBy().getHandle().equals(auth.getHandle()))
            throw new IdentityEntryAlreadyExists(entryDto.getHandle());

        // Delete old Entry if exists
        identityRepository.deleteByCreatedByHandle_AndHandle(auth.getHandle(), entryDto.getHandle());

        // Get Quotas
        BaseQuotaProperties userQuotas = quotaService.getUserQuotas(auth.getHandle());
        int quota = userQuotas.getIdentity().getMaxEntries();

        // Check count against quota
        long count = identityRepository.countAllByCreatedByHandle(auth.getHandle());
        if (count >= quota)
            throw new IdentityEntryQuotaExceeded(quota);

        try {
            // Get User
            User user = userRepository.findByHandle(auth.getHandle());

            IdentityStorageEntry entry = new IdentityStorageEntry();
            entry.setHandle(entryDto.getHandle());
            entry.setName(entryDto.getName());
            entry.setPubSplitKey(entryDto.getPubSplitKey());
            entry.setEncKeypairEntry(entryDto.getEncKeypairEntry());
            entry.setEncKeypairEntrySignature(entryDto.getEncKeypairEntrySignature());
            entry.setCreatedBy(user);
            entry.setCreatedAt(Instant.now());

            if (entryDto.getPublicJsonData() == null)
                entry.setPublicDataJson("{\"services\": {}}"); // Default to JSON Object with Services Key
            else {
                if (!isValid(entryDto.getPublicJsonData()))
                    throw new InvalidJson(entryDto.getPublicJsonData());
                entry.setPublicDataJson(entryDto.getPublicJsonData());
            }

            identityRepository.save(entry);
        } catch (InvalidJson e) {
            throw e;
        }
        catch (Exception e) {
            throw new IdentityEntryInvalid();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Gets all Identity Entries from the User")
    public List<IdentityStorageEntryDto> getMyEntries(@AuthenticationPrincipal GoofyAuthUser auth) {
        List<IdentityStorageEntry> entries = identityRepository.findAllByCreatedByHandle(auth.getHandle());
        return entries.stream().map(entry -> new IdentityStorageEntryDto(
                entry.getHandle(),
                entry.getName(),
                entry.getPubSplitKey(),
                entry.getEncKeypairEntry(),
                entry.getEncKeypairEntrySignature(),
                entry.getPublicDataJson()
        )).toList();
    }

    // TODO: Don't make it a two step process but enforce that the data has been exported at least 72h before attempting to delete the entry (IF IT IS NOT EMPTY -> then it doesnt matter)
    @DeleteMapping("/{handle}")
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER') and not hasRole('ROLE_RESTRICTED')")
    @FisEndpoint(summary = "Deletes the Identity Entry of the Users Storage if it exists")
    public void deleteEntry(@PathVariable String handle, @AuthenticationPrincipal GoofyAuthUser auth) {
        identityRepository.deleteByCreatedByHandle_AndHandle(auth.getHandle(), handle);
    }

    @GetMapping("/{handle}")
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Gets the Identity Entry from a Handle if it exists")
    public IdentityStorageEntryDto getEntry(@PathVariable String handle, @AuthenticationPrincipal GoofyAuthUser auth) throws IdentityEntryNotFound {
        IdentityStorageEntry entry = identityRepository.findByCreatedByHandle_AndHandle(auth.getHandle(), handle);
        if (entry == null)
            throw new IdentityEntryNotFound(handle);

        return new IdentityStorageEntryDto(
                entry.getHandle(),
                entry.getName(),
                entry.getPubSplitKey(),
                entry.getEncKeypairEntry(),
                entry.getEncKeypairEntrySignature(),
                entry.getPublicDataJson()
        );
    }

    // TODO: Have the Public Entry for the Identity support including paths for the actual entries (e.g: I have a public Goofy Media 2 Account on this identity and this is the service entry UUID + Table UUID / Name so that others can access stuff in a federated way!)
    // Get Public Data
    @GetMapping("/public/{handle}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets the Public Data of an Identity Entry from a Handle if it exists", description = "This Endpoint returns the Public Data of an Identity Entry from a Handle if it exists. <br>The Public Data is a JSON Object that can contain any information the user wants to share publicly. <br>For example, it can contain a `services` key which has an object with service names and the urls of the service instance the handle is used on.")
    public ResponseEntity<String> getPublicData(@PathVariable String handle) throws IdentityEntryNotFound {
        IdentityStorageEntry entry = identityRepository.findByHandle(handle);
        if (entry == null)
            throw new IdentityEntryNotFound(handle);

        return ResponseEntity
                .ok()
                .header("Content-Type", "application/json")
                .body(entry.getPublicDataJson());
    }

    // TODO: Potentially allow Identities themselves to edit the data too, not just the registered user.
    // Might be a better flow if you don't need to go back to the FIS to manage the linked service list for example
    // Set Public Data
    @PutMapping("/public/{handle}")
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER') and not hasRole('ROLE_RESTRICTED')")
    @FisEndpoint(summary = "Sets the Public Data of an Identity Entry from a Handle if it exists", description = "This Endpoint sets the Public Data of an Identity Entry from a Handle if it exists. <br>The Public Data is a JSON Object that can contain any information the user wants to share publicly. <br>For example, it can contain a `services` key which has an object with service names and the urls of the service instance the handle is used on.")
    public void setPublicData(@PathVariable String handle, String publicJson, @AuthenticationPrincipal GoofyAuthUser auth) throws IdentityEntryNotFound, InvalidJson {
        IdentityStorageEntry entry = identityRepository.findByCreatedByHandle_AndHandle(auth.getHandle(), handle);
        if (entry == null)
            throw new IdentityEntryNotFound(handle);

        if (!isValid(publicJson))
            throw new InvalidJson(publicJson);

        entry.setPublicDataJson(publicJson);
        identityRepository.save(entry);
    }

    // TODO: Add Export and Import?

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValid(String json) {
        try {
            mapper.readTree(json);
        } catch (JacksonException e) {
            return false;
        }
        return true;
    }
}
