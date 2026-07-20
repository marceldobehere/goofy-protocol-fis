package com.masl.goofy_protocol_fis_be.rest.service;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.dto.both.ServiceEntryDto;
import com.masl.goofy_protocol_fis_be.dto.response.MyServiceEntryQuotasDto;
import com.masl.goofy_protocol_fis_be.entity.IdentityStorageEntry;
import com.masl.goofy_protocol_fis_be.entity.ServiceEntry;
import com.masl.goofy_protocol_fis_be.entity.UserQuotas;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.exception.client.ServiceEntryInvalid;
import com.masl.goofy_protocol_fis_be.exception.client.ServiceEntryNotFound;
import com.masl.goofy_protocol_fis_be.exception.client.ServiceEntryQuotaExceeded;
import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import com.masl.goofy_protocol_fis_be.repository.IdentityStorageEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.ServiceEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.UserQuotasRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// TODO: Write Tests
@RestController
@RequestMapping("/api/service-entry")
@Tag(name = "Service Entry", description = "Endpoints related to configuring Service Entries. <br>For Each Identity Users may create \"Service Entries\" which the User can then use to store data for the service. <br>Additionally the User can allow a Service to read/write some tables or view/access the bucket.<br>Important Note: These Endpoints need to be signed/access using the Identity Keypair, not the User")
public class ServiceEntryEndpoint {
    private final IdentityStorageEntryRepository identityRepository;
    private final ServiceEntryRepository serviceEntryRepository;
    private final UserQuotasRepository userQuotasRepository;
    private final BaseQuotaProperties baseQuotaProperties;

    public ServiceEntryEndpoint(IdentityStorageEntryRepository identityRepository, ServiceEntryRepository serviceEntryRepository, UserQuotasRepository userQuotasRepository, BaseQuotaProperties baseQuotaProperties) {
        this.identityRepository = identityRepository;
        this.serviceEntryRepository = serviceEntryRepository;
        this.userQuotasRepository = userQuotasRepository;
        this.baseQuotaProperties = baseQuotaProperties;
    }

    @GetMapping("/quotas")
    @PreAuthorize("hasRole('ROLE_REGISTERED_IDENTITY') and not hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Gets the Identity Service related Quotas")
    public MyServiceEntryQuotasDto getMyQuotas(@AuthenticationPrincipal GoofyAuthUser auth) {
        // Get Identity
        IdentityStorageEntry identity = identityRepository.findByHandle(auth.getHandle());

        // Get Quotas
        UserQuotas quotas = userQuotasRepository.findByUserHandle(identity.getCreatedBy().getHandle());
        BaseQuotaProperties userQuotas = UserQuotas.getUserQuotas(quotas, baseQuotaProperties);
        int quota = userQuotas.getIdentity().getMaxServiceEntries();

        // Set DTO
        long count = serviceEntryRepository.countAllByLinkedIdentity_Handle(auth.getHandle());
        return new MyServiceEntryQuotasDto(quota, count);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_REGISTERED_IDENTITY') and not hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Sets a Service Entry for a Identity", description = "The UUID Field should not be set.")
    public String createEntry(@Valid @RequestBody ServiceEntryDto entryDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryQuotaExceeded, ServiceEntryInvalid {
        // Get Identity
        IdentityStorageEntry identity = identityRepository.findByHandle(auth.getHandle());

        // Get Quotas
        UserQuotas quotas = userQuotasRepository.findByUserHandle(identity.getCreatedBy().getHandle());
        BaseQuotaProperties userQuotas = UserQuotas.getUserQuotas(quotas, baseQuotaProperties);
        int quota = userQuotas.getIdentity().getMaxServiceEntries();

        // Check count against quota
        long count = serviceEntryRepository.countAllByLinkedIdentity_Handle(auth.getHandle());
        if (count >= quota)
            throw new ServiceEntryQuotaExceeded(quota);

        // Save Entry
        try {
            ServiceEntry entry = new ServiceEntry();
            entry.setUuid(UUID.randomUUID().toString());
            entry.setName(entryDto.getName());
            entry.setUsedService(entryDto.getUsedService());
            entry.setLinkedIdentity(identity);
            entry.setCreatedBy(identity.getCreatedBy());
            entry.setCreatedAt(Instant.now());
            serviceEntryRepository.save(entry);
            return entry.getUuid();
        } catch (Exception e) {
            throw new ServiceEntryInvalid();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_REGISTERED_IDENTITY') and not hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Gets all Service Entries from the Identity")
    public List<ServiceEntryDto> getMyEntries(@AuthenticationPrincipal GoofyAuthUser auth) {
        List<ServiceEntry> entries = serviceEntryRepository.findAllByLinkedIdentity_Handle(auth.getHandle());
        return entries.stream().map(entry -> new ServiceEntryDto(
                entry.getName(),
                entry.getUsedService(),
                entry.getUuid()
        )).toList();
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ROLE_REGISTERED_IDENTITY') and not hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Updates a Service Entry for a Identity", description = "The UUID Field should be set. Currently you can only update the name and the used service.")
    public void updateEntry(@PathVariable String uuid, @Valid @RequestBody ServiceEntryDto entryDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceEntryInvalid {
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(uuid, auth.getHandle());
        if (entry == null)
            throw new ServiceEntryNotFound(uuid);

        try {
            entry.setName(entryDto.getName());
            entry.setUsedService(entryDto.getUsedService());
            serviceEntryRepository.save(entry);
        } catch (Exception e) {
            throw new ServiceEntryInvalid();
        }
    }

    // TODO: Don't make it a two step process but enforce that the data has been exported atleast 72h before attempting to delete the entry (IF IT IS NOT EMPTY -> then it doesnt matter)
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ROLE_REGISTERED_IDENTITY') and not hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Deletes the Service Entry of the Identity if it exists")
    public void deleteEntry(@PathVariable String uuid, @AuthenticationPrincipal GoofyAuthUser auth) {
        serviceEntryRepository.deleteByUuid_AndLinkedIdentity_Handle(uuid, auth.getHandle());
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('ROLE_REGISTERED_IDENTITY') and not hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Gets a Service Entry for a Identity & UUID")
    public ServiceEntryDto updateEntry(@PathVariable String uuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound {
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(uuid, auth.getHandle());
        if (entry == null)
            throw new ServiceEntryNotFound(uuid);

        return new ServiceEntryDto(
                entry.getName(),
                entry.getUsedService(),
                entry.getUuid()
        );
    }

    // TODO: Add Export
}
