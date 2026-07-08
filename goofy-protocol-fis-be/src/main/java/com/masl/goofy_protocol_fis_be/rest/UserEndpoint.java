package com.masl.goofy_protocol_fis_be.rest;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.config.ROLES;
import com.masl.goofy_protocol_fis_be.dto.response.MyUserInfoDto;
import com.masl.goofy_protocol_fis_be.entity.User;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.properties.GeneralProperties;
import com.masl.goofy_protocol_fis_be.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "Endpoints relating to User Info")
public class UserEndpoint {
    private final UserRepository userRepository;
    private final GeneralProperties generalProperties;

    public UserEndpoint(UserRepository userRepository, GeneralProperties generalProperties) {
        this.userRepository = userRepository;
        this.generalProperties = generalProperties;
    }

    // Get My User Info (Handle, Public Key, Auth Role, ...)
    @GetMapping("/info")
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Gets Information for the current User", description = "This Endpoint returns information about the current user, including their handle, public key, and authentication role.")
    public MyUserInfoDto myInfo(@AuthenticationPrincipal GoofyAuthUser auth) {
        User user = userRepository.findByHandle(auth.getHandle());
        return new MyUserInfoDto(user.getHandle(), generalProperties.getDomain(), user.getPubSplitKey(), user.isAdmin() ? ROLES.AuthRoleEnumDto.ADMIN : ROLES.AuthRoleEnumDto.REGISTERED_USER, user.isRestricted());
    }

    // Look Up User / Public Key Info based on Handle (Check if moved)
    // Set/Update external Handle Information (for example the domain of the user)

    // - For each identity the user has, they can set global public and private data.
    // - This data is basically just a JSON Object with keys and values.
    // - Public Data for example should include a `services` key which has an object with service names and the urls of the service instance the handle is used on.
    // - This can be useful if you use the same handle for several services and want others to find the instances.
    // Get Public Data for User (Include Handle/Domain if moved)
    // Set Public Data for User

    // Get my Private Data
    // Set my Private Data

    // Get Storage Details / Stats

    // Get Complete Account Export (What about Tables / Buckets)
    // Import FIS Data?

    // Delete Account
    // Move Account (Would be the same as update external handle information?)
    // Deactivate Handle (Highly specific, needs more thought put into it)
}
