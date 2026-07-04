package com.masl.goofy_protocol_fis_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationCode {
    @Id
    @Column(nullable = false, length = FieldSize.GENERIC_CODE_LEN)
    @Getter @Setter
    private String code;

    @Column(nullable = false)
    @Getter @Setter
    @ColumnDefault("false")
    private Boolean admin;

    @Getter @Setter
    @ManyToOne
    @JoinColumn
    private User createdBy;

    @Column(nullable = false)
    @Getter @Setter
    private Instant createdAt;

    @Getter @Setter
    @ManyToOne
    @JoinColumn
    private User usedBy;

    @Column
    @Getter @Setter
    private Instant usedAt;
}
