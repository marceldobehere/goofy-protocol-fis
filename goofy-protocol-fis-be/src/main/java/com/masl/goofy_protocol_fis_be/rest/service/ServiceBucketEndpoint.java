package com.masl.goofy_protocol_fis_be.rest.service;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.dto.both.ServiceBucketEntryDto;
import com.masl.goofy_protocol_fis_be.dto.both.ServiceBucketPermissionDto;
import com.masl.goofy_protocol_fis_be.dto.response.ServiceBucketQuotasDto;
import com.masl.goofy_protocol_fis_be.entity.ServiceBucketEntry;
import com.masl.goofy_protocol_fis_be.entity.ServiceEntry;
import com.masl.goofy_protocol_fis_be.entity.UserQuotas;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.exception.client.*;
import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import com.masl.goofy_protocol_fis_be.repository.ServiceBucketEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.ServiceEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.UserQuotasRepository;
import com.masl.goofy_protocol_fis_be.service.UserBucketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

// TODO: Write Tests
@RestController
@RequestMapping("/api/service-bucket")
@Tag(name = "Service Bucket Access", description = "Endpoints related to accessing Buckets and their contents. <br>Important Note: These Endpoints need to be signed/access using the Identity Keypair, not the User. <br>ServiceEntry Format: `[id_handle]+[service_uuid]`")
public class ServiceBucketEndpoint {
    private final ServiceBucketEntryRepository bucketEntryRepository;
    private final ServiceEntryRepository serviceEntryRepository;
    private final UserQuotasRepository userQuotasRepository;
    private final BaseQuotaProperties baseQuotaProperties;
    private final UserBucketService userBucketService;

    public ServiceBucketEndpoint(ServiceBucketEntryRepository bucketEntryRepository, ServiceEntryRepository serviceEntryRepository, UserQuotasRepository userQuotasRepository, BaseQuotaProperties baseQuotaProperties, UserBucketService userBucketService) {
        this.bucketEntryRepository = bucketEntryRepository;
        this.serviceEntryRepository = serviceEntryRepository;
        this.userQuotasRepository = userQuotasRepository;
        this.baseQuotaProperties = baseQuotaProperties;
        this.userBucketService = userBucketService;
    }

    // TODO: Add access log table with the last x access entries, for example 10000, just like handle and serviec-uuid/file-uuid

    // --- IDENTITY ONLY ---

    // Get Full Bucket Permissions (Read Access, Write Access, ...)
    @GetMapping("/{serviceEntry}/perms")
    @PreAuthorize("hasRole('ROLE_REGISTERED_IDENTITY') and not hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Gets the Bucket Permissions", description = "Gets the full Bucket Permission Lists, for now a List of Read and Write Access Permissions. <br>Consists of a List of Strings (handles of the identities with Access) <br>Additionally, readAccess can have an entry with just a \"*\" which makes the Bucket publicly visible by default.")
    public ServiceBucketPermissionDto getBucketPermissions(@PathVariable String serviceEntry, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryPathInvalid, ServiceEntryNotFound {
        ServiceEntryPath path = ServiceEntryPath.fromStr(serviceEntry);

        // Get Service Entry Identity
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(path.serviceUuid(), auth.getHandle());
        if (entry == null)
            throw new ServiceEntryNotFound(path.serviceUuid());

        return new ServiceBucketPermissionDto(
                entry.getExtraReadPerms().toArray(new String[0]),
                entry.getExtraWritePerms().toArray(new String[0])
        );
    }

    // Set Full Bucket Permissions (Read Access, Write Access, ...)
    @PutMapping("/{serviceEntry}/perms")
    @PreAuthorize("hasRole('ROLE_REGISTERED_IDENTITY') and not hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Sets the Bucket Permissions", description = "Sets the full Bucket Permission Lists, for now a List of Read and Write Access Permissions. <br>Consists of a List of Strings (handles of the identities with Access) <br>Additionally, readAccess can have an entry with just a \"*\" which makes the Bucket publicly visible by default.")
    public void setBucketPermissions(@PathVariable String serviceEntry, @Valid @RequestBody ServiceBucketPermissionDto permDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryPathInvalid, ServiceEntryNotFound, ServiceBucketPermsInvalid {
        ServiceEntryPath path = ServiceEntryPath.fromStr(serviceEntry);

        // Get Service Entry Identity
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(path.serviceUuid(), auth.getHandle());
        if (entry == null)
            throw new ServiceEntryNotFound(path.serviceUuid());

        // Get Quotas
        UserQuotas quotas = userQuotasRepository.findByUserHandle(entry.getCreatedBy().getHandle());
        BaseQuotaProperties userQuotas = UserQuotas.getUserQuotas(quotas, baseQuotaProperties);

        // Check the Entry Counts against the Quotas
        if (permDto.getHandlesWithReadPerms().length > userQuotas.getBucket().getMaxPermissionCount() ||
                permDto.getHandlesWithWritePerms().length > userQuotas.getBucket().getMaxPermissionCount())
            throw new ServiceBucketPermsInvalid();

        // Update Permissions
        entry.setExtraReadPerms(Set.of(permDto.getHandlesWithReadPerms()));
        entry.setExtraWritePerms(Set.of(permDto.getHandlesWithWritePerms()));
        serviceEntryRepository.save(entry);
    }



    // --- OUTSIDE ENTITIES ---

    public record ServiceEntryPath(String idHandle, String serviceUuid) {
        static ServiceEntryPath fromStr(String str) throws ServiceEntryPathInvalid {
            if (str == null || !str.contains("+"))
                throw new ServiceEntryPathInvalid();

            String[] split = str.split(Pattern.quote("+"));
            if (split.length != 2 || split[0].isEmpty() || split[1].isEmpty())
                throw new ServiceEntryPathInvalid();

            return new ServiceEntryPath(split[0], split[1]);
        }

        String toStr() {
            return idHandle + "+" + serviceUuid;
        }
    }

    // Get Bucket Quota and Stats (Count & Size)
    @GetMapping("/{serviceEntry}/quotas")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets the Bucket Quotas and Stats", description = "Gets the Bucket Quotas and Stats (Count & Size).")
    public ServiceBucketQuotasDto getBucketQuotas(@PathVariable String serviceEntry, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryPathInvalid, ServiceEntryNotFound {
        ServiceEntryPath path = ServiceEntryPath.fromStr(serviceEntry);

        // Get Service Entry Identity
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(path.serviceUuid(), path.idHandle());
        if (entry == null)
            throw new ServiceEntryNotFound(path.serviceUuid());

        // Check Permissions
        if (!entry.getExtraReadPerms().contains("*") && !entry.getExtraReadPerms().contains(auth.getHandle()) && !entry.getLinkedIdentity().getHandle().equals(auth.getHandle()))
            throw new ServiceEntryNotFound(path.serviceUuid());

        // Get Quotas
        UserQuotas quotas = userQuotasRepository.findByUserHandle(entry.getCreatedBy().getHandle());
        BaseQuotaProperties userQuotas = UserQuotas.getUserQuotas(quotas, baseQuotaProperties);

        // Get Stats
        List<ServiceBucketEntry> bucketEntries = bucketEntryRepository.findAllByLinkedIdentity_Handle(path.idHandle());
        int currentItemCount = bucketEntries.size();
        long currentBucketSize = bucketEntries.stream().reduce(0L, (acc, bucketEntry) -> acc + bucketEntry.getContentSize(), Long::sum);

        return new ServiceBucketQuotasDto(
                userQuotas.getBucket().getMaxBucketSize(),
                userQuotas.getBucket().getMaxBucketSize(),
                userQuotas.getBucket().getMaxItemCount(),
                userQuotas.getBucket().getMaxPermissionCount(),
                currentBucketSize,
                currentItemCount
        );
    }

    private ServiceBucketEntryDto fromServiceBucketEntry(ServiceBucketEntry entry) {
        return new ServiceBucketEntryDto(
                entry.getFileUuid(),
                entry.getContentType(),
                entry.getContentSize(),
                entry.getCreatedAt(),
                entry.getExtraReadPerms().toArray(new String[0]),
                entry.getExtraWritePerms().toArray(new String[0])
        );
    }

    public ServiceBucketEntryDto uploadBucketEntry(String serviceEntry, String uuid, String accessHandle, byte[] body, String contentType) throws ServiceEntryPathInvalid, ServiceEntryNotFound, ServiceBucketFileError {
        ServiceEntryPath path = ServiceEntryPath.fromStr(serviceEntry);

        // Get Service Entry Identity
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(path.serviceUuid(), path.idHandle());
        if (entry == null)
            throw new ServiceEntryNotFound(path.serviceUuid());

        // Check Permissions
        if (!entry.getExtraWritePerms().contains(accessHandle) && !entry.getLinkedIdentity().getHandle().equals(accessHandle))
            throw new ServiceEntryNotFound(path.serviceUuid());

        // Get Bucket Entry / Create if it doesn't exist
        ServiceBucketEntry bucketEntry = bucketEntryRepository.findByFileUuid_AndLinkedIdentity_Handle(uuid, path.idHandle());
        if (bucketEntry == null) {
            bucketEntry = new ServiceBucketEntry();
            bucketEntry.setFileUuid(uuid);
            bucketEntry.setLinkedIdentity(entry.getLinkedIdentity());
            bucketEntry.setLinkedServiceEntry(entry);
            bucketEntry.setCreatedAt(Instant.now());

            // Private by default
            bucketEntry.setExtraReadPerms(Set.of());
            bucketEntry.setExtraWritePerms(Set.of());
        }

        // Upload File
        try {
            userBucketService.uploadBucketEntry(entry, uuid, body);
        } catch (IOException e) {
            throw new ServiceBucketFileError();
        }

        // Save Bucket Entry Data
        bucketEntry.setContentSize((long)body.length);
        bucketEntry.setContentType(contentType);
        bucketEntryRepository.save(bucketEntry);

        return fromServiceBucketEntry(bucketEntry);
    }

    // Upload Bucket Entry (Default, no UUID) (will be private by default)
    @PostMapping("/{serviceEntry}/upload")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Uploads a Bucket Entry", description = "Upload Bucket Entry (Default, no UUID) (will be private by default). <br>The data should be raw bytes in the POST Body + A Content-Type Header. <br>Returns the ServiceBucketEntryDto.")
    public ServiceBucketEntryDto uploadRandomBucketEntry(@PathVariable String serviceEntry, @RequestBody byte[] body, @RequestHeader(name = "Content-Type") String contentType, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryPathInvalid, ServiceEntryNotFound, ServiceBucketFileError {
        return uploadBucketEntry(serviceEntry, UUID.randomUUID().toString(), auth.getHandle(), body, contentType);
    }
    // Upload/Update Bucket Entry (UUID Set) (will be private by default)
    @PostMapping("/{serviceEntry}/upload/{fileUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Uploads/Updates a Bucket Entry with a specific UUID", description = "Upload Bucket Entry using a UUID (will be private by default). <br>The data should be raw bytes in the POST Body + A Content-Type Header. <br>Returns the ServiceBucketEntryDto.")
    public ServiceBucketEntryDto uploadUuidBucketEntry(@PathVariable String serviceEntry, @PathVariable String fileUuid, @RequestBody byte[] body, @RequestHeader(name = "Content-Type") String contentType, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryPathInvalid, ServiceEntryNotFound, ServiceBucketFileError {
        return uploadBucketEntry(serviceEntry, fileUuid, auth.getHandle(), body, contentType);
    }

    // Get Bucket Entry Config (Content-Type, Read Access, Write Access, Size, Timestamp, ...)
    @GetMapping("/{serviceEntry}/entry/{fileUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets a Bucket Entry", description = "Get the Bucket Entry for a specific File UUID")
    public ServiceBucketEntryDto getBucketEntry(@PathVariable String serviceEntry, @PathVariable String fileUuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryPathInvalid, ServiceEntryNotFound, ServiceBucketNotFound {
        ServiceEntryPath path = ServiceEntryPath.fromStr(serviceEntry);

        // Get Service Entry Identity
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(path.serviceUuid(), path.idHandle());
        if (entry == null)
            throw new ServiceEntryNotFound(path.serviceUuid());

        // Get Bucket Entry / Create if it doesn't exist
        ServiceBucketEntry bucketEntry = bucketEntryRepository.findByFileUuid_AndLinkedIdentity_Handle(fileUuid, path.idHandle());
        if (bucketEntry == null)
            throw new ServiceBucketNotFound(fileUuid);

        // Check Permissions
        if (!entry.getExtraReadPerms().contains("*") && !entry.getExtraReadPerms().contains(auth.getHandle()) && !entry.getLinkedIdentity().getHandle().equals(auth.getHandle()))
            if (!bucketEntry.getExtraReadPerms().contains("*") && !bucketEntry.getExtraReadPerms().contains(auth.getHandle()))
                throw new ServiceEntryNotFound(path.serviceUuid());

        return fromServiceBucketEntry(bucketEntry);
    }

    // Set Bucket Entry Config (Content-Type, Read Access, Write Access, ...)
    @PutMapping("/{serviceEntry}/entry/{fileUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Sets a Bucket Entry", description = "Set the Bucket Entry for a specific File UUID. <br>To be more specific the Content-Type and Permissions")
    public void setBucketEntry(@PathVariable String serviceEntry, @PathVariable String fileUuid, @Valid @RequestBody ServiceBucketEntryDto entryDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryPathInvalid, ServiceEntryNotFound, ServiceBucketNotFound {
        ServiceEntryPath path = ServiceEntryPath.fromStr(serviceEntry);

        // Get Service Entry Identity
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(path.serviceUuid(), path.idHandle());
        if (entry == null)
            throw new ServiceEntryNotFound(path.serviceUuid());

        // Get Bucket Entry / Create if it doesn't exist
        ServiceBucketEntry bucketEntry = bucketEntryRepository.findByFileUuid_AndLinkedIdentity_Handle(fileUuid, path.idHandle());
        if (bucketEntry == null)
            throw new ServiceBucketNotFound(fileUuid);

        // Check Permissions
        if (!entry.getExtraWritePerms().contains(auth.getHandle()) && !entry.getLinkedIdentity().getHandle().equals(auth.getHandle()))
            if (!bucketEntry.getExtraWritePerms().contains(auth.getHandle()))
                throw new ServiceEntryNotFound(path.serviceUuid());

        // Update Bucket Entry
        bucketEntry.setContentType(entryDto.getContentType());
        bucketEntry.setExtraReadPerms(Set.of(entryDto.getHandlesWithReadPerms()));

        // Update Bucket Entry Perms, (only for the linked Identity)
        if (entry.getLinkedIdentity().getHandle().equals(auth.getHandle())) {
            bucketEntry.setExtraWritePerms(Set.of(entryDto.getHandlesWithWritePerms()));
        }

        // Save
        bucketEntryRepository.save(bucketEntry);
    }

    // Delete Bucket Entry
    @DeleteMapping("/{serviceEntry}/entry/{fileUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Deletes a Bucket Entry", description = "Deletes a Bucket Entry based on a specific File UUID")
    public void deleteBucketEntry(@PathVariable String serviceEntry, @PathVariable String fileUuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryPathInvalid, ServiceEntryNotFound, ServiceBucketNotFound {
        ServiceEntryPath path = ServiceEntryPath.fromStr(serviceEntry);

        // Get Service Entry Identity
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(path.serviceUuid(), path.idHandle());
        if (entry == null)
            throw new ServiceEntryNotFound(path.serviceUuid());

        // Get Bucket Entry / Create if it doesn't exist
        ServiceBucketEntry bucketEntry = bucketEntryRepository.findByFileUuid_AndLinkedIdentity_Handle(fileUuid, path.idHandle());
        if (bucketEntry == null)
            throw new ServiceBucketNotFound(fileUuid);

        // Check Permissions
        if (!entry.getExtraWritePerms().contains(auth.getHandle()) && !entry.getLinkedIdentity().getHandle().equals(auth.getHandle()))
            if (!bucketEntry.getExtraWritePerms().contains(auth.getHandle()))
                throw new ServiceEntryNotFound(path.serviceUuid());

        // Delete Bucket Entry
        bucketEntryRepository.deleteByFileUuid_AndLinkedIdentity_Handle(fileUuid, path.idHandle());
    }

    // Get All Bucket Entries
    @GetMapping("/{serviceEntry}/entry")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets all Bucket Entries", description = "Get all Bucket Entries")
    public List<ServiceBucketEntryDto> getAllBucketEntries(@PathVariable String serviceEntry, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryPathInvalid, ServiceEntryNotFound {
        ServiceEntryPath path = ServiceEntryPath.fromStr(serviceEntry);

        // Get Service Entry Identity
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(path.serviceUuid(), path.idHandle());
        if (entry == null)
            throw new ServiceEntryNotFound(path.serviceUuid());

        // Check Permissions
        if (!entry.getExtraReadPerms().contains("*") && !entry.getExtraReadPerms().contains(auth.getHandle()) && !entry.getLinkedIdentity().getHandle().equals(auth.getHandle()))
            throw new ServiceEntryNotFound(path.serviceUuid());

        return entry.getServiceBucketEntries().stream().map(this::fromServiceBucketEntry).toList();
    }

    // Get Bucket Entry Data
    @GetMapping("/{serviceEntry}/content/{fileUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets the Bucket Entry Content", description = "Get the Raw Conent of a Bucket Entry with a specific File UUID")
    public ResponseEntity getBucketEntryContent(@PathVariable String serviceEntry, @PathVariable String fileUuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryPathInvalid, ServiceEntryNotFound, ServiceBucketNotFound, ServiceBucketFileError {
        ServiceEntryPath path = ServiceEntryPath.fromStr(serviceEntry);

        // Get Service Entry Identity
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(path.serviceUuid(), path.idHandle());
        if (entry == null)
            throw new ServiceEntryNotFound(path.serviceUuid());

        // Get Bucket Entry / Create if it doesn't exist
        ServiceBucketEntry bucketEntry = bucketEntryRepository.findByFileUuid_AndLinkedIdentity_Handle(fileUuid, path.idHandle());
        if (bucketEntry == null)
            throw new ServiceBucketNotFound(fileUuid);

        // Check Permissions
        if (!entry.getExtraReadPerms().contains("*") && !entry.getExtraReadPerms().contains(auth.getHandle()) && !entry.getLinkedIdentity().getHandle().equals(auth.getHandle()))
            if (!bucketEntry.getExtraReadPerms().contains("*") && !bucketEntry.getExtraReadPerms().contains(auth.getHandle()))
                throw new ServiceEntryNotFound(path.serviceUuid());

        try {
            return ResponseEntity.ok()
                    .header("Content-Type", bucketEntry.getContentType())
                    .body(userBucketService.getBucketEntry(entry, fileUuid));
        } catch (IOException e) {
            throw new ServiceBucketFileError();
        }
    }
}
