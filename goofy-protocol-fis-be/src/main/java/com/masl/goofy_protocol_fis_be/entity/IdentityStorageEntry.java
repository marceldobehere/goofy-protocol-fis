package com.masl.goofy_protocol_fis_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class IdentityStorageEntry {
    @Id
    @Column(nullable = false, length = FieldSize.HANDLE_LEN)
    @Getter @Setter
    private String handle;

    @Column(nullable = false, length = FieldSize.PUB_KEY_LEN)
    @Getter @Setter
    private String pubSplitKey;

    @Column(nullable = false, length = FieldSize.FULL_KEY_LEN)
    @Getter @Setter
    private String encKeypairEntry;

    @Column(nullable = false, length = FieldSize.SIGNATURE_LEN)
    @Getter @Setter
    private String encKeypairEntrySignature;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Getter @Setter
    private User createdBy;

    @Column(nullable = false)
    @Getter @Setter
    private Instant createdAt;
}
