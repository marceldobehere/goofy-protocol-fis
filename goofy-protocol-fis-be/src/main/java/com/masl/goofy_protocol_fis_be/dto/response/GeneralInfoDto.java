package com.masl.goofy_protocol_fis_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralInfoDto {
    private String frontendUrl;
    private String url;
    private String name;
    private String description;
    private String version;
    private String pubKey;
    private String handle;
}
