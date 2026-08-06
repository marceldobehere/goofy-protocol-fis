package com.masl.goofy_protocol_fis_be.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityPublicDataDto {
    @NotNull
    private Map<String, Object> services;
}
