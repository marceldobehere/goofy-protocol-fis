package com.masl.goofy_protocol_fis_be.rest.admin;

import com.masl.goofy_protocol_fis_be.dto.both.UserDto;
import com.masl.goofy_protocol_fis_be.dto.both.UserQuotasDto;
import com.masl.goofy_protocol_fis_be.entity.User;
import com.masl.goofy_protocol_fis_be.entity.UserQuotas;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.exception.client.HandleNotFound;
import com.masl.goofy_protocol_fis_be.properties.AdminQuotaProperties;
import com.masl.goofy_protocol_fis_be.properties.BaseQuotaProperties;
import com.masl.goofy_protocol_fis_be.repository.UserQuotasRepository;
import com.masl.goofy_protocol_fis_be.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: Write tests
@RestController
@RequestMapping("/fis-api/admin/user")
@Tag(name = "User (Admin)", description = "Admin Endpoints relating to User Management")
public class AdminUserEndpoint {
    private final UserRepository userRepository;
    private final UserQuotasRepository userQuotasRepository;
    private final BaseQuotaProperties baseQuotaProperties;
    private final AdminQuotaProperties adminQuotaProperties;

    public AdminUserEndpoint(UserRepository userRepository, UserQuotasRepository userQuotasRepository, BaseQuotaProperties baseQuotaProperties, AdminQuotaProperties adminQuotaProperties) {
        this.userRepository = userRepository;
        this.userQuotasRepository = userQuotasRepository;
        this.baseQuotaProperties = baseQuotaProperties;
        this.adminQuotaProperties = adminQuotaProperties;
    }

    // Get All Users
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @FisEndpoint(summary = "Gets All Users", description = "This Endpoint returns a list of all registered users in the system")
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDto(
                        user.getHandle(),
                        user.getPubSplitKey(),
                        user.isAdmin(),
                        user.isRestricted()
                )).toList();
    }

    // Update User (Change Admin & Restriction Bools)
    @PutMapping("/{handle}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @FisEndpoint(summary = "Updates a User's Admin and Restriction Status", description = "This Endpoint allows an admin to update a user's admin and restriction status. <br> The request body should contain the new admin and restriction status for the user.")
    public void updateUser(@PathVariable String handle, @RequestBody UserDto userDto) throws HandleNotFound {
        User user = userRepository.findByHandle(handle);
        if (user == null)
            throw new HandleNotFound(handle);

        user.setAdmin(userDto.getIsAdmin());
        user.setRestricted(userDto.getIsRestricted());
        userRepository.save(user);
    }

    // Delete User
    @DeleteMapping("/{handle}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @FisEndpoint(summary = "Deletes a User", description = "This Endpoint allows an admin to delete a user from the system. <br> This will remove all associated data and identities for the user. <br> This is a hard delete, do NOT expect to be able to recover the user without a backup afterwards!")
    public void deleteUser(@PathVariable String handle) throws HandleNotFound {
        User user = userRepository.findByHandle(handle);
        if (user == null)
            throw new HandleNotFound(handle);

        userRepository.deleteByHandle(handle);
    }

    // Get Base User Quotas
    @GetMapping("/quotas/base/user")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @FisEndpoint(summary = "Gets Base User Quotas", description = "This Endpoint allows an admin to retrieve the base quotas for users in the system.")
    public UserQuotasDto getBaseUserQuotas() {
        return fromProperties(baseQuotaProperties);
    }

    // Get Base Admin Quotas
    @GetMapping("/quotas/base/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @FisEndpoint(summary = "Gets Base Admin Quotas", description = "This Endpoint allows an admin to retrieve the base quotas for admins in the system.")
    public UserQuotasDto getBaseAdminQuotas() {
        return fromProperties(adminQuotaProperties);
    }

    // Get User Quotas
    @GetMapping("/{handle}/quotas")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @FisEndpoint(summary = "Gets User Quotas", description = "This Endpoint allows an admin to retrieve the EXPLICTILY DEFINED quotas for a specific user in the system. Everything unset will be NULL")
    public UserQuotasDto getUserQuotas(@PathVariable String handle) throws HandleNotFound {
        User user = userRepository.findByHandle(handle);
        if (user == null)
            throw new HandleNotFound(handle);

        UserQuotas userQuotas = userQuotasRepository.findByUserHandle(handle);
        if (userQuotas == null)
            return new UserQuotasDto();

        BaseQuotaProperties userQuotaProperties = userQuotas.convert();
        return fromProperties(userQuotaProperties);
    }

    // Add/Write User Quotas
    @PostMapping("/{handle}/quotas")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @FisEndpoint(summary = "Sets User Quotas", description = "This Endpoint allows an admin to set the quotas for a specific user in the system.")
    public UserQuotasDto setUserQuotas(@PathVariable String handle, @RequestBody UserQuotasDto quotasDto) throws HandleNotFound {
        User user = userRepository.findByHandle(handle);
        if (user == null)
            throw new HandleNotFound(handle);

        // Get / Create Quotas
        UserQuotas userQuotas = userQuotasRepository.findByUserHandle(handle);
        if (userQuotas == null) {
            userQuotas = new UserQuotas();
            userQuotas.setHandle(handle);
            userQuotas.setUser(user);
        }

        // Set Data
        userQuotas.setGeneralMaxNameSize(quotasDto.getGeneralMaxNameSize());
        userQuotas.setIdentityMaxEntries(quotasDto.getIdentityMaxEntries());
        userQuotas.setIdentityMaxServiceEntries(quotasDto.getIdentityMaxServiceEntries());
        userQuotas.setTableMaxDbSize(quotasDto.getMaxDbSize());
        userQuotas.setTableMaxFieldSize(quotasDto.getMaxFieldSize());
        userQuotas.setTableMaxTables(quotasDto.getMaxTableCount());
        userQuotas.setTableMaxCols(quotasDto.getMaxColumnCount());
        userQuotas.setTableMaxRows(quotasDto.getMaxRowCount());
        userQuotas.setTableMaxPermissionCount(quotasDto.getMaxTableUniquePermissionCount());
        userQuotas.setTableMaxLockDurationSeconds(quotasDto.getMaxLockDurationSeconds());
        userQuotas.setTableQueryMaxQueryLength(quotasDto.getMaxQueryLength());
        userQuotas.setTableQueryMaxConditionCount(quotasDto.getMaxConditionCount());
        userQuotas.setTableQueryMaxResultCount(quotasDto.getMaxResultCount());
        userQuotas.setBucketMaxBucketSize(quotasDto.getMaxBucketSize());
        userQuotas.setBucketMaxItemSize(quotasDto.getMaxBucketItemSize());
        userQuotas.setBucketMaxItemCount(quotasDto.getMaxBucketItemCount());
        userQuotas.setBucketMaxPermissionCount(quotasDto.getMaxBucketUniquePermissionCount());

        userQuotasRepository.save(userQuotas);
        return fromProperties(userQuotas.convert());
    }

    // Reset User Quotas
    @DeleteMapping("/{handle}/quotas")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @FisEndpoint(summary = "Resets User Quotas", description = "This Endpoint allows an admin to reset a user's quotas to the base quotas defined in the system to the default.")
    public void resetUserQuotas(@PathVariable String handle) throws HandleNotFound {
        User user = userRepository.findByHandle(handle);
        if (user == null)
            throw new HandleNotFound(handle);

        userQuotasRepository.deleteByUserHandle(handle);
    }


    // Helper Method
    private UserQuotasDto fromProperties(BaseQuotaProperties properties) {
        return new UserQuotasDto(
            properties.getGeneral().getMaxNameSize(),
            properties.getIdentity().getMaxEntries(),
            properties.getIdentity().getMaxServiceEntries(),
            properties.getTable().getMaxDbSize(),
            properties.getTable().getMaxFieldSize(),
            properties.getTable().getMaxTables(),
            properties.getTable().getMaxCols(),
            properties.getTable().getMaxRows(),
            properties.getTable().getMaxPermissionCount(),
            properties.getTable().getMaxLockDurationSeconds(),
            properties.getTableQuery().getMaxQueryLength(),
            properties.getTableQuery().getMaxConditionCount(),
            properties.getTableQuery().getMaxResultCount(),
            properties.getBucket().getMaxBucketSize(),
            properties.getBucket().getMaxItemSize(),
            properties.getBucket().getMaxItemCount(),
            properties.getBucket().getMaxPermissionCount()
        );
    }
}
