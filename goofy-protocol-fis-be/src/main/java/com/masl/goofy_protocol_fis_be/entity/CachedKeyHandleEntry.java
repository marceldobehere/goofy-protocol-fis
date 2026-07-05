package com.masl.goofy_protocol_fis_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CachedKeyHandleEntry {
    @Id
    @Column(nullable = false, length = FieldSize.PUB_KEY_LEN)
    @Getter @Setter
    private String pubSplitKey;

    @Column(nullable = false, length = FieldSize.HANDLE_LEN)
    @Getter @Setter
    private String handle;

    @Column(nullable = false)
    @Getter @Setter
    private Instant createdAt;
}
