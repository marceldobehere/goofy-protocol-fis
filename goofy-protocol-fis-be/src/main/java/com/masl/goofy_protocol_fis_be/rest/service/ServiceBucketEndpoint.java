package com.masl.goofy_protocol_fis_be.rest.service;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.config.CacheDuration;
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
import java.util.*;

// TODO: Write Tests
@RestController
@RequestMapping("/api/service-bucket")
@Tag(name = "Service Bucket Access", description = "Endpoints related to accessing Buckets and their contents. <br>Important Note: These Endpoints need to be signed/access using the Identity Keypair, not the User.")
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


    // TODO: Do Admins realistically need access to these endpoints too?

    // Get Full Bucket Permissions (Read Access, Write Access, ...)
    @GetMapping("/{serviceUuid}/perms")
    @PreAuthorize("hasRole('ROLE_REGISTERED_IDENTITY') and not hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Gets the Bucket Permissions", description = "Gets the full Bucket Permission Lists, for now a List of Read and Write Access Permissions. <br>Consists of a List of Strings (handles of the identities with Access) <br>Additionally, readAccess can have an entry with just a \"*\" which makes the Bucket publicly visible by default.")
    public ServiceBucketPermissionDto getBucketPermissions(@PathVariable String serviceUuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound {
        ServiceEntry entry = findServiceEntry(auth.getHandle(), serviceUuid);
        // No extra Permission Checks needed

        return new ServiceBucketPermissionDto(
                entry.getExtraReadPerms().toArray(new String[0]),
                entry.getExtraWritePerms().toArray(new String[0])
        );
    }

    // Set Full Bucket Permissions (Read Access, Write Access, ...)
    @PutMapping("/{serviceUuid}/perms")
    @PreAuthorize("hasRole('ROLE_REGISTERED_IDENTITY') and not hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Sets the Bucket Permissions", description = "Sets the full Bucket Permission Lists, for now a List of Read and Write Access Permissions. <br>Consists of a List of Strings (handles of the identities with Access) <br>Additionally, readAccess can have an entry with just a \"*\" which makes the Bucket publicly visible by default.")
    public void setBucketPermissions(@PathVariable String serviceUuid, @Valid @RequestBody ServiceBucketPermissionDto permDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceBucketPermsInvalid {
        ServiceEntry entry = findServiceEntry(auth.getHandle(), serviceUuid);
        BaseQuotaProperties userQuotas = getServiceEntryQuotas(entry);
        // No extra Permission Checks needed

        // Check the Entry Counts against the Quotas
        if (permDto.getHandlesWithReadPerms().length > userQuotas.getBucket().getMaxPermissionCount() ||
                permDto.getHandlesWithWritePerms().length > userQuotas.getBucket().getMaxPermissionCount())
            throw new ServiceBucketPermsInvalid();

        // Update Permissions
        entry.setExtraReadPerms(new HashSet<>(Arrays.asList(permDto.getHandlesWithReadPerms())));
        entry.setExtraWritePerms(new HashSet<>(Arrays.asList(permDto.getHandlesWithWritePerms())));
        serviceEntryRepository.save(entry);
    }


    // --- OUTSIDE ENTITIES ---


    // Get Bucket Quota and Stats (Count & Size)
    @GetMapping("/{idHandle}/{serviceUuid}/quotas")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets the Bucket Quotas and Stats", description = "Gets the Bucket Quotas and Stats (Count & Size).")
    public ServiceBucketQuotasDto getBucketQuotas(@PathVariable String idHandle, @PathVariable String serviceUuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound {
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        checkServiceEntryReadPermissions(entry, auth);
        BaseQuotaProperties userQuotas = getServiceEntryQuotas(entry);

        // Get Stats
        List<ServiceBucketEntry> bucketEntries = bucketEntryRepository.findAllByLinkedIdentity_Handle_AndLinkedServiceEntry_Uuid(idHandle, serviceUuid);
        int currentItemCount = bucketEntries.size();
        long currentBucketSize = getCurrentBucketSize(bucketEntries);

        return new ServiceBucketQuotasDto(
                userQuotas.getBucket().getMaxBucketSize(),
                userQuotas.getBucket().getMaxItemSize(),
                userQuotas.getBucket().getMaxItemCount(),
                userQuotas.getBucket().getMaxPermissionCount(),
                currentBucketSize,
                currentItemCount
        );
    }

    public ServiceBucketEntryDto uploadBucketEntry(String idHandle, String serviceUuid, String uuid, String filename, CacheDuration cacheDuration, GoofyAuthUser auth, byte[] body, String contentType) throws ServiceEntryNotFound, ServiceBucketFileError, ServiceBucketQuotaExceeded {
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        checkServiceEntryWritePermissions(entry, auth);
        BaseQuotaProperties userQuotas = getServiceEntryQuotas(entry);

        // Check Max Item Size against Quota
        if (body.length > userQuotas.getBucket().getMaxItemSize())
            throw new ServiceBucketQuotaExceeded("maxItemSize");

        // Get Bucket Entry / Create if it doesn't exist
        ServiceBucketEntry bucketEntry = bucketEntryRepository.findByFileUuid_AndLinkedIdentity_Handle(uuid, idHandle);
        if (bucketEntry == null) {
            bucketEntry = new ServiceBucketEntry();
            bucketEntry.setFileUuid(uuid);
            bucketEntry.setLinkedIdentity(entry.getLinkedIdentity());
            bucketEntry.setLinkedServiceEntry(entry);
            bucketEntry.setCreatedBy(auth.getHandle());
            bucketEntry.setCreatedAt(Instant.now());
            bucketEntry.setFilename(filename);
            bucketEntry.setCacheDuration(cacheDuration != null ? cacheDuration : CacheDuration.NORMAL);

            // Private by default
            bucketEntry.setExtraReadPerms(new HashSet<>());
            bucketEntry.setExtraWritePerms(new HashSet<>());

            // Check Max Item Count against Quota
            long count = bucketEntryRepository.countAllByLinkedIdentity_Handle_AndLinkedServiceEntry_Uuid(idHandle, serviceUuid);
            if (count >= userQuotas.getBucket().getMaxItemCount())
                throw new ServiceBucketQuotaExceeded("maxItemCount");

            // Check Max Bucket Size against Quota
            List<ServiceBucketEntry> bucketEntries = bucketEntryRepository.findAllByLinkedIdentity_Handle_AndLinkedServiceEntry_Uuid(idHandle, serviceUuid);
            long currentBucketSize = getCurrentBucketSize(bucketEntries);
            if (currentBucketSize + body.length > userQuotas.getBucket().getMaxBucketSize())
                throw new ServiceBucketQuotaExceeded("maxBucketSize");
        } else {
            // Check Max Bucket Size against Quota
            List<ServiceBucketEntry> bucketEntries = bucketEntryRepository.findAllByLinkedIdentity_Handle_AndLinkedServiceEntry_Uuid(idHandle, serviceUuid);
            long currentBucketSize = getCurrentBucketSize(bucketEntries);
            if (currentBucketSize - bucketEntry.getContentSize() + body.length > userQuotas.getBucket().getMaxBucketSize())
                throw new ServiceBucketQuotaExceeded("maxBucketSize");

            // Potentially overwrite filename
            if (filename != null && !filename.isEmpty())
                bucketEntry.setFilename(filename);
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
        bucketEntry.setLastUploadedBy(auth.getHandle());
        bucketEntry.setLastUploadedAt(Instant.now());
        bucketEntryRepository.save(bucketEntry);

        return fromServiceBucketEntry(bucketEntry);
    }

    // Upload Bucket Entry (Default, no UUID) (will be private by default)
    @PostMapping("/{idHandle}/{serviceUuid}/upload")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Uploads a Bucket Entry", description = "Upload Bucket Entry (Default, no UUID) (will be private by default). <br>The data should be raw bytes in the POST Body + A `Content-Type` Header + A `X-Filename` Header + An (optional) `X-Cache-Duration` Header (With the String Value of the Enum). <br>Returns the ServiceBucketEntryDto.")
    public ServiceBucketEntryDto uploadRandomBucketEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @RequestBody byte[] body, @RequestHeader(name = "Content-Type") String contentType, @RequestHeader(name = "X-Filename") String filename, @RequestHeader(name = "X-Cache-Duration", required = false) CacheDuration cacheDuration, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceBucketFileError, ServiceBucketQuotaExceeded {
        return uploadBucketEntry(idHandle, serviceUuid, UUID.randomUUID().toString(), filename, cacheDuration, auth, body, contentType);
    }

    // Upload/Update Bucket Entry (UUID Set) (will be private by default)
    @PostMapping("/{idHandle}/{serviceUuid}/upload/{fileUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Uploads/Updates a Bucket Entry with a specific UUID", description = "Upload Bucket Entry using a UUID (will be private by default). <br>The data should be raw bytes in the POST Body + A `Content-Type` Header + An (optional) `X-Filename` Header. <br>Returns the ServiceBucketEntryDto.")
    public ServiceBucketEntryDto uploadUuidBucketEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String fileUuid, @RequestBody byte[] body, @RequestHeader(name = "Content-Type") String contentType, @RequestHeader(name = "X-Filename", required = false) String filename, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceBucketFileError, ServiceBucketQuotaExceeded {
        return uploadBucketEntry(idHandle, serviceUuid, fileUuid, filename, null, auth, body, contentType);
    }

    // Get Bucket Entry Config (Content-Type, Read Access, Write Access, Size, Timestamp, ...)
    @GetMapping("/{idHandle}/{serviceUuid}/entry/{fileUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets a Bucket Entry", description = "Get the Bucket Entry for a specific File UUID")
    public ServiceBucketEntryDto getBucketEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String fileUuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceBucketNotFound {
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceBucketEntry bucketEntry = findServiceBucketEntry(idHandle, fileUuid);
        checkServiceBucketEntryReadPermissions(entry, bucketEntry, auth);

        return fromServiceBucketEntry(bucketEntry);
    }

    // Set Bucket Entry Config (Content-Type, Read Access, Write Access, ...)
    @PutMapping("/{idHandle}/{serviceUuid}/entry/{fileUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Sets a Bucket Entry", description = "Set the Bucket Entry for a specific File UUID. <br>To be more specific the `Content-Type` and Permissions")
    public void setBucketEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String fileUuid, @Valid @RequestBody ServiceBucketEntryDto entryDto, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceBucketNotFound {
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceBucketEntry bucketEntry = findServiceBucketEntry(idHandle, fileUuid);
        checkServiceBucketEntryWritePermissions(entry, bucketEntry, auth);

        // Update Bucket Entry
        bucketEntry.setContentType(entryDto.getContentType());
        bucketEntry.setFilename(entryDto.getFilename());
        bucketEntry.setCacheDuration(entryDto.getCacheDuration());
        bucketEntry.setExtraReadPerms(new HashSet<>(Arrays.asList(entryDto.getHandlesWithReadPerms())));
        bucketEntry.setLastUpdatedAt(Instant.now());
        bucketEntry.setLastUpdatedBy(auth.getHandle());

        // Update Bucket Entry Perms, (only for the linked Identity)
        if (entry.getLinkedIdentity().getHandle().equals(auth.getHandle()))
            bucketEntry.setExtraWritePerms(new HashSet<>(Arrays.asList(entryDto.getHandlesWithWritePerms())));

        // Save
        bucketEntryRepository.save(bucketEntry);
    }

    // Delete Bucket Entry
    @DeleteMapping("/{idHandle}/{serviceUuid}/entry/{fileUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Deletes a Bucket Entry", description = "Deletes a Bucket Entry based on a specific File UUID")
    public void deleteBucketEntry(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String fileUuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceBucketNotFound {
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceBucketEntry bucketEntry = findServiceBucketEntry(idHandle, fileUuid);
        checkServiceBucketEntryWritePermissions(entry, bucketEntry, auth);

        // Delete Bucket Entry
        bucketEntryRepository.deleteByFileUuid_AndLinkedIdentity_Handle(fileUuid, idHandle);
    }

    // Get All Bucket Entries
    @GetMapping("/{idHandle}/{serviceUuid}/entry")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets all Bucket Entries", description = "Get all Bucket Entries")
    public List<ServiceBucketEntryDto> getAllBucketEntries(@PathVariable String idHandle, @PathVariable String serviceUuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound {
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        checkServiceEntryReadPermissions(entry, auth);

        return entry.getServiceBucketEntries().stream().map(this::fromServiceBucketEntry).toList();
    }

    // Get Bucket Entry Data
    @GetMapping("/{idHandle}/{serviceUuid}/content/{fileUuid}")
    @PreAuthorize("hasRole('ROLE_OUTSIDE_ENTITY')")
    @FisEndpoint(summary = "Gets the Bucket Entry Content", description = "Get the Raw Content of a Bucket Entry with a specific File UUID")
    public ResponseEntity<byte[]> getBucketEntryContent(@PathVariable String idHandle, @PathVariable String serviceUuid, @PathVariable String fileUuid, @AuthenticationPrincipal GoofyAuthUser auth) throws ServiceEntryNotFound, ServiceBucketNotFound, ServiceBucketFileError {
        ServiceEntry entry = findServiceEntry(idHandle, serviceUuid);
        ServiceBucketEntry bucketEntry = findServiceBucketEntry(idHandle, fileUuid);
        checkServiceBucketEntryReadPermissions(entry, bucketEntry, auth);

        try {
            // TODO: Look into if i should add Content-Disposition Header with Filename
            return ResponseEntity.ok()
                    .header("Content-Type", bucketEntry.getContentType())
                    .cacheControl(bucketEntry.getCacheDuration().getCacheControl())
                    .body(userBucketService.getBucketEntry(entry, fileUuid));
        } catch (IOException e) {
            throw new ServiceBucketFileError();
        }
    }


    // --- Helper Methods ---


    private ServiceBucketEntryDto fromServiceBucketEntry(ServiceBucketEntry entry) {
        return new ServiceBucketEntryDto(
                entry.getFileUuid(),
                entry.getContentType(),
                entry.getFilename(),
                entry.getCacheDuration(),
                entry.getContentSize(),
                entry.getCreatedAt(),
                entry.getExtraReadPerms().toArray(new String[0]),
                entry.getExtraWritePerms().toArray(new String[0])
        );
    }

    private ServiceEntry findServiceEntry(String idHandle, String serviceUuid) throws ServiceEntryNotFound {
        ServiceEntry entry = serviceEntryRepository.findByUuid_AndLinkedIdentity_Handle(serviceUuid, idHandle);
        if (entry == null)
            throw new ServiceEntryNotFound(serviceUuid);
        return entry;
    }

    private ServiceBucketEntry findServiceBucketEntry(String idHandle, String fileUuid) throws ServiceBucketNotFound {
        ServiceBucketEntry bucketEntry = bucketEntryRepository.findByFileUuid_AndLinkedIdentity_Handle(fileUuid, idHandle);
        if (bucketEntry == null)
            throw new ServiceBucketNotFound(fileUuid);
        return bucketEntry;
    }

    private long getCurrentBucketSize(List<ServiceBucketEntry> bucketEntries) {
        return bucketEntries.stream().reduce(0L, (acc, currBucketEntry) -> acc + currBucketEntry.getContentSize(), Long::sum);
    }

    private BaseQuotaProperties getServiceEntryQuotas(ServiceEntry entry) {
        UserQuotas quotas = userQuotasRepository.findByUserHandle(entry.getCreatedBy().getHandle());
        return UserQuotas.getUserQuotas(quotas, baseQuotaProperties);
    }

    private void checkServiceEntryReadPermissions(ServiceEntry entry, GoofyAuthUser auth) throws ServiceEntryNotFound {
        if (!entry.getExtraReadPerms().contains("*") && !entry.getExtraReadPerms().contains(auth.getHandle()) &&
                !entry.getLinkedIdentity().getHandle().equals(auth.getHandle()) && !auth.getAdmin())
            throw new ServiceEntryNotFound(entry.getUuid());
    }

    private void checkServiceEntryWritePermissions(ServiceEntry entry, GoofyAuthUser auth) throws ServiceEntryNotFound {
        if (!entry.getExtraWritePerms().contains(auth.getHandle()) && !entry.getLinkedIdentity().getHandle().equals(auth.getHandle()) && !auth.getAdmin())
            throw new ServiceEntryNotFound(entry.getUuid());
    }

    private void checkServiceBucketEntryReadPermissions(ServiceEntry entry, ServiceBucketEntry bucketEntry, GoofyAuthUser auth) throws ServiceEntryNotFound {
        if (!entry.getExtraReadPerms().contains("*") && !entry.getExtraReadPerms().contains(auth.getHandle()) && !entry.getLinkedIdentity().getHandle().equals(auth.getHandle()))
            if (!bucketEntry.getExtraReadPerms().contains("*") && !bucketEntry.getExtraReadPerms().contains(auth.getHandle()) && !auth.getAdmin())
                throw new ServiceEntryNotFound(entry.getUuid());
    }

    private void checkServiceBucketEntryWritePermissions(ServiceEntry entry, ServiceBucketEntry bucketEntry, GoofyAuthUser auth) throws ServiceEntryNotFound {
        if (!entry.getExtraWritePerms().contains(auth.getHandle()) && !entry.getLinkedIdentity().getHandle().equals(auth.getHandle()))
            if (!bucketEntry.getExtraWritePerms().contains(auth.getHandle()) && !auth.getAdmin())
                throw new ServiceEntryNotFound(entry.getUuid());
    }
}
