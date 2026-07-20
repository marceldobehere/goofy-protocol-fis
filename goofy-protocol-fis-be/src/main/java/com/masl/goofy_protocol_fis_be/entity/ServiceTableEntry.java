package com.masl.goofy_protocol_fis_be.entity;

import com.masl.goofy_protocol_fis_be.entity.listeners.ServiceTableEntryListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.Set;

@Entity
@EntityListeners(ServiceTableEntryListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class ServiceTableEntry {
    @Id
    @Column(nullable = false, length = FieldSize.GENERIC_CODE_LEN)
    private String tableUuid;

    @Column(nullable = false, length = FieldSize.SHORT_TEXT_LEN)
    private String tableName;

    // Useful for versioning the schema of the table and applying migrations client side
    @Column(nullable = false)
    private Integer schemaVersion;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IdentityStorageEntry linkedIdentity;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ServiceEntry linkedServiceEntry;

    // Useful information in case we need to find out who originally uploaded the file and when
    @Column
    private String createdBy;
    @Column(nullable = false)
    private Instant createdAt;

    // Useful information in case we need to find out who last updated the entry and when
    @Column
    private String lastUpdatedBy;
    @Column
    private Instant lastUpdatedAt;


    // Table Entry Perm Stuff
    @ElementCollection
    private Set<String> extraReadPerms; // NOTE: These can only be less restrictive! By Default Empty (So Private, only accessible by the Identity), Will have an Entry for each Handle with Read Access or an Entry with "*" to make it public
    @ElementCollection
    private Set<String> extraWritePerms; // NOTE: These can only be less restrictive!  By Default Empty (So Private, only accessible by the Identity), Will have an Entry for each Handle with Write Access (No * for public write access!)
}
