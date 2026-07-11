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
@Getter @Setter
public class LoginStorageEntry {
    @Id
    @Column(nullable = false, length = FieldSize.SHA256_LEN)
    private String usernameHash;

    @Column(nullable = false, length = FieldSize.FULL_KEY_LEN)
    private String encKeypair;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdBy;

    @Column(nullable = false)
    private Instant createdAt;
}
