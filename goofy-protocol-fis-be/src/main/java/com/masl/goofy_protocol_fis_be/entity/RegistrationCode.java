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
public class RegistrationCode {
    @Id
    @Getter @Setter
    private String code;

    @Getter @Setter
    private Boolean admin;

    @Getter @Setter
    @ManyToOne()
    @JoinColumn()
    private User createdBy;

    @Getter @Setter
    @ManyToOne()
    @JoinColumn()
    private User usedBy;

    @Column
    @Getter @Setter
    private Instant usedAt;

    @Column(nullable = false)
    @Getter @Setter
    private Instant createdAt;
}
