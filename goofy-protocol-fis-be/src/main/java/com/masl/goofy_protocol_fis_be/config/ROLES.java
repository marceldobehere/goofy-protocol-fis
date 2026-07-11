package com.masl.goofy_protocol_fis_be.config;

public class ROLES {
    // Outsider which has a valid signature (Can be a User or Service)
    public static final String OUTSIDE_ENTITY = "OUTSIDE_ENTITY";

    // Registered Identity
    public static final String REGISTERED_IDENTITY = "REGISTERED_IDENTITY";

    // Registered User
    public static final String REGISTERED_USER = "REGISTERED_USER";

    // Administrator
    public static final String ADMIN = "ADMIN";

    public enum AuthRoleEnumDto {
        OUTSIDE_ENTITY,
        REGISTERED_IDENTITY,
        REGISTERED_USER,
        ADMIN
    }
}
