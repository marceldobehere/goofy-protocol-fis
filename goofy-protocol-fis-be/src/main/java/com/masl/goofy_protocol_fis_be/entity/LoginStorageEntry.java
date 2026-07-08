package com.masl.goofy_protocol_fis_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class LoginStorageEntry {
    @Id
    @Column(nullable = false, length = FieldSize.SHA256_LEN)
    @Getter @Setter
    private String usernameHash;

    @Column(nullable = false, length = FieldSize.FULL_KEY_LEN)
    @Getter @Setter
    private String encKeypair;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @Getter @Setter
    private User createdBy;

    @Column(nullable = false)
    @Getter @Setter
    private Instant createdAt;
}
