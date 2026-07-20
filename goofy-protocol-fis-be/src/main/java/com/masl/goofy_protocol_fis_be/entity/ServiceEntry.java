package com.masl.goofy_protocol_fis_be.entity;

import com.masl.goofy_protocol_fis_be.entity.listeners.ServiceEntryListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.Set;

@Entity
@EntityListeners(ServiceEntryListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class ServiceEntry {
    @Id
    @Column(nullable = false, length = FieldSize.GENERIC_CODE_LEN)
    private String uuid; // Should be randomly generated unless its coming from an import

    // NOTE: Not encrypted, because it's not really important
    @Column(nullable = false, length = FieldSize.TITLE_LEN)
    @ColumnDefault("")
    private String name;

    @Column(length = FieldSize.NORMAL_TEXT_LEN)
    private String usedService;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdBy;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IdentityStorageEntry linkedIdentity;

    @Column(nullable = false)
    private Instant createdAt;

    // Bucket Perm Stuff
    @ElementCollection
    private Set<String> extraReadPerms; // By Default Empty (So Private, only accessible by the Identity), Will have an Entry for each Handle with Read Access or an Entry with "*" to make it public
    @ElementCollection
    private Set<String> extraWritePerms; // By Default Empty (So Private, only accessible by the Identity), Will have an Entry for each Handle with Write Access (No * for public write access!)

    @OneToMany(mappedBy="linkedServiceEntry", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private Set<ServiceBucketEntry> serviceBucketEntries;

    @OneToMany(mappedBy="linkedServiceEntry", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private Set<ServiceTableEntry> serviceTableEntries;
}
