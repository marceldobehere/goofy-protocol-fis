package com.masl.goofy_protocol_fis_be.entity;

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
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class IdentityStorageEntry {
    @Id
    @Column(nullable = false, length = FieldSize.HANDLE_LEN)
    private String handle;

    // NOTE: Not encrypted, because it's not really important
    @Column(nullable = false, length = FieldSize.TITLE_LEN)
    @ColumnDefault("")
    private String name;

    @Column(nullable = false, length = FieldSize.PUB_KEY_LEN)
    private String pubSplitKey;

    @Column(nullable = false, length = FieldSize.FULL_KEY_LEN)
    private String encKeypairEntry;

    @Column(nullable = false, length = FieldSize.SIGNATURE_LEN)
    private String encKeypairEntrySignature;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdBy;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy="linkedIdentity", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private Set<ServiceEntry> serviceEntries;
}
