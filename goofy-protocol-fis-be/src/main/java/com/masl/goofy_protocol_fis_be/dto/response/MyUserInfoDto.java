package com.masl.goofy_protocol_fis_be.dto.response;

import com.masl.goofy_protocol_fis_be.config.ROLES;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyUserInfoDto {
    private String handle;
    private String handleDomain;
    private String pubKey;
    private ROLES.AuthRoleEnumDto authRole;
    private boolean isRestricted;
}
