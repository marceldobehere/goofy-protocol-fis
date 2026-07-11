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
    // Also check for Identities and maybe have a special role like REGISTERED_IDENTITY or so to differentiate
    // Would also need to affect all identities of the user
    // Would be good to have an extra table that has every user and identity ever registered (just the handle) + information if they have moved FIS domains
    // Additionally support moving a singular identity handle -> shouldn't be too hard, just have to watch out in the request i guess
    // Additionally add a custom Fis Exception everywhere where its needed to indicate an Identity/Account was moved

    // Get Storage Details / Stats

    // Get Complete Account Export (What about Tables / Buckets)
    // Import FIS Data
    // Should maybe be a two step process like delete, because youd replace all your old data
    // For clients therell be two options of importing an export (backup or when moving FIS), either direct import using the same keypair for registration
    // - or decrypting and re-encrypting everything before importing it

    // Delete Account
    // Move Account (Would be the same as update external handle information?)
    // Would also need to affect all identities of the user
    // Should be a two step process with a token, ideally also enforce having done an export beforehand

    // Deactivate Handle (Highly specific, needs more thought put into it)
}
