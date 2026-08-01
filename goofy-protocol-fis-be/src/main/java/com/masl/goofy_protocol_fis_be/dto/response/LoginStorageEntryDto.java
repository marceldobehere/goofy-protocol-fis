package com.masl.goofy_protocol_fis_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginStorageEntryDto {
    private String usernameHash;
    private String encKeypair;
    private String createdByHandle;
    private Instant createdAt;
}
