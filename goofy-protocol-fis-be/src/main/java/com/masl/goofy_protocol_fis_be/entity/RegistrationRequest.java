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
public class RegistrationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter @Setter
    private Long id;

    @Column(nullable = false, length = FieldSize.NORMAL_TEXT_LEN)
    @Getter @Setter
    private String messsage;

    @Column(nullable = false, length = FieldSize.SHORT_TEXT_LEN)
    @Getter @Setter
    private String generalContact;

    @Column(length = FieldSize.SHORT_TEXT_LEN)
    @Getter @Setter
    private String optEmail;

    @Column(nullable = false)
    @Getter @Setter
    private Instant createdAt;

    @Column
    @Getter @Setter
    private Instant resolvedAt; // Also acts as boolean
}
