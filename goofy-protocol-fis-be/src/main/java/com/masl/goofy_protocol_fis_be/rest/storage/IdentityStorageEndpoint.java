package com.masl.goofy_protocol_fis_be.rest.storage;

import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.GlobAsymmCrypto;
import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.crypto.HandleHelper;
import com.masl.goofy_protocol_fis_be.dto.both.IdentityStorageEntryDto;
import com.masl.goofy_protocol_fis_be.dto.response.MyIdentityEntryQuotasDto;
import com.masl.goofy_protocol_fis_be.entity.IdentityStorageEntry;
import com.masl.goofy_protocol_fis_be.entity.User;
import com.masl.goofy_protocol_fis_be.entity.UserQuotas;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.exception.client.*;
import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import com.masl.goofy_protocol_fis_be.repository.IdentityStorageEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.UserQuotasRepository;
import com.masl.goofy_protocol_fis_be.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

// TODO: Write Tests
@RestController
@RequestMapping("/api/identity-storage")
@Tag(name = "Identity Storage", description = "Endpoints relating to Identity Keypair Storage for Services. <br>Users can store their identity Keypairs encrypted here and use those for Service Access")
public class IdentityStorageEndpoint {
    private final BaseQuotaProperties baseQuotaProperties;
    private final IdentityStorageEntryRepository identityRepository;
    private final UserQuotasRepository userQuotasRepository;
    private final UserRepository userRepository;
    private final GlobAsymmCrypto asymmCrypto = new GlobAsymmCrypto();
    private final HandleCrypto handleCrypto;

    public IdentityStorageEndpoint(BaseQuotaProperties baseQuotaProperties, IdentityStorageEntryRepository identityRepository, UserQuotasRepository userQuotasRepository, UserRepository userRepository, HandleHelper handleHelper) {
        this.baseQuotaProperties = baseQuotaProperties;
        this.identityRepository = identityRepository;
        this.userQuotasRepository = userQuotasRepository;
        this.userRepository = userRepository;
        this.handleCrypto = new HandleCrypto(handleHelper);
    }

    @GetMapping("/quotas")
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Gets the Users Identity Entry related Quotas", description = "This Endpoint returns the maximum number of Identity Entries a user can have and how many they currently have stored.")
    public MyIdentityEntryQuotasDto getMyQuotas(@AuthenticationPrincipal GoofyAuthUser auth) {
        // Get Quotas
        UserQuotas quotas = userQuotasRepository.findByUserHandle(auth.getHandle());
        BaseQuotaProperties userQuotas = UserQuotas.getUserQuotas(quotas, baseQuotaProperties);
        int quota = userQuotas.getIdentity().getMaxEntries();

        // Set DTO
        long count = identityRepository.countAllByCreatedByHandle(auth.getHandle());
        return new MyIdentityEntryQuotasDto(quota, count);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Sets an Identity Entry for a Handle", description = "If the handle isn't used in any other entry it will be saved in the users identity storage. <br>The entry request needs to have the encKeypair Data signed with the public key of the identity to be added to make sure it belongs to the user. <br>If the handle has an entry by the user, it will simply get updated")
    public void setEntry(@Valid @RequestBody IdentityStorageEntryDto entryDto, @AuthenticationPrincipal GoofyAuthUser auth) throws InvalidPublicKey, InvalidSignedObject, NotMatchingPublicKey, IdentityEntryAlreadyExists, IdentityEntryInvalid, IdentityEntryQuotaExceeded {
        // Check Handle
        IdentityStorageEntryDto.checkValidity(entryDto, handleCrypto, asymmCrypto);

        // Check for old Entry that doesn't belong to the user
        IdentityStorageEntry oldEntry = identityRepository.findByHandle(entryDto.getHandle());
        if (oldEntry != null && !oldEntry.getCreatedBy().getHandle().equals(auth.getHandle()))
            throw new IdentityEntryAlreadyExists(entryDto.getHandle());

        // Delete old Entry if exists
        identityRepository.deleteByCreatedByHandle_AndHandle(auth.getHandle(), entryDto.getHandle());

        // Get Quotas
        UserQuotas quotas = userQuotasRepository.findByUserHandle(auth.getHandle());
        BaseQuotaProperties userQuotas = UserQuotas.getUserQuotas(quotas, baseQuotaProperties);
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
            identityRepository.save(entry);
        } catch (Exception e) {
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
                entry.getEncKeypairEntrySignature()
        )).toList();
    }

    // TODO: Make this a two step process / enforce export data first? To avoid data loss!
    @DeleteMapping("/{handle}")
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER')")
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
                entry.getEncKeypairEntrySignature()
        );
    }

    // TODO: Implement
    // - For each identity the user has, they can set global public and private data.
    // - This data is basically just a JSON Object with keys and values.
    // - Public Data for example should include a `services` key which has an object with service names and the urls of the service instance the handle is used on.
    // - This can be useful if you use the same handle for several services and want others to find the instances.
    // Get Public Data for User (Include Handle/Domain if moved)
    // Set Public Data for User

    // Get my Private Data
    // Set my Private Data
}
