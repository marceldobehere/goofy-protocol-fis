package com.masl.goofy_protocol_fis_be.entity;

import com.masl.goofy_protocol_fis_be.config.CacheDuration;
import com.masl.goofy_protocol_fis_be.service.UserBucketService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class ServiceBucketEntry {
    @Id
    @Column(nullable = false, length = FieldSize.GENERIC_CODE_LEN)
    private String fileUuid;

    @Column(nullable = false, length = FieldSize.SHORT_TEXT_LEN)
    private String contentType;

    @Column(nullable = false, length = FieldSize.SHORT_TEXT_LEN)
    private String filename;

    @Column(nullable = false)
    private Long contentSize;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IdentityStorageEntry linkedIdentity;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ServiceEntry linkedServiceEntry;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'NORMAL'")
    private CacheDuration cacheDuration;

    // Useful information in case we need to find out who originally uploaded the file and when
    @Column
    private String createdBy;
    @Column(nullable = false)
    private Instant createdAt;

    // Useful information in case we need to find out who last changed the file content and when
    @Column
    private String lastUploadedBy;
    @Column
    private Instant lastUploadedAt;

    // Useful information in case we need to find out who last updated the entry and when
    @Column
    private String lastUpdatedBy;
    @Column
    private Instant lastUpdatedAt;


    // Bucket Entry Perm Stuff
    @ElementCollection
    private Set<String> extraReadPerms; // NOTE: These can only be less restrictive! By Default Empty (So Private, only accessible by the Identity), Will have an Entry for each Handle with Read Access or an Entry with "*" to make it public
    @ElementCollection
    private Set<String> extraWritePerms; // NOTE: These can only be less restrictive!  By Default Empty (So Private, only accessible by the Identity), Will have an Entry for each Handle with Write Access (No * for public write access!)

    // Helper Stuff

    private static final Logger log = LoggerFactory.getLogger(ServiceBucketEntry.class);

    @PreRemove
    public void deleteEntryHandler() throws IOException {
        log.info("Deleting Service Bucket Entry: {}", fileUuid);
        UserBucketService.getSingleton().deleteBucketEntry(linkedServiceEntry, fileUuid);
    }
}
