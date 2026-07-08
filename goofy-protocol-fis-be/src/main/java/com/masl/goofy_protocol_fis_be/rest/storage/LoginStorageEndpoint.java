package com.masl.goofy_protocol_fis_be.rest.storage;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.entity.LoginStorageEntry;
import com.masl.goofy_protocol_fis_be.entity.User;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.exception.client.LoginEntryAlreadyExists;
import com.masl.goofy_protocol_fis_be.exception.client.LoginEntryInvalid;
import com.masl.goofy_protocol_fis_be.exception.client.LoginEntryNotFound;
import com.masl.goofy_protocol_fis_be.repository.LoginStorageEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

// TODO: Write Tests
@RestController
@RequestMapping("/api/login-storage")
@Tag(name = "Login Storage", description = "Endpoints relating to Logging in using a username/pasword and retrieving the encrypted Keypair.<br>Each registered user can have one entry with a chosen username (if available) and can store text data in it. <br>The entry can be looked up publicly with the username as the key. The usernames should always be hashed beforehand!<br>It's intended for users to symmetrically encrypt their keypair with a password and store that in this storage.")
public class LoginStorageEndpoint {
    private final LoginStorageEntryRepository entryRepository;
    private final UserRepository userRepository;

    public LoginStorageEndpoint(LoginStorageEntryRepository loginStorageEntryRepository, UserRepository userRepository) {
        this.entryRepository = loginStorageEntryRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{usernameHash}")
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Sets a Login Entry for a Username Hash", description = "If the Username is available, it deletes all previous Entries by the User/Handle and sets the Login Entry for the Username Hash.<br>The Data will be stored in Plaintext so the User needs to encrypt it!")
    public void setEntry(@PathVariable String usernameHash, @Valid @RequestBody String encKeypair, @AuthenticationPrincipal GoofyAuthUser auth) throws LoginEntryAlreadyExists, LoginEntryInvalid {
        LoginStorageEntry oldEntry = entryRepository.findByUsernameHash(usernameHash);
        if (oldEntry != null && !oldEntry.getCreatedBy().getHandle().equals(auth.getHandle()))
            throw new LoginEntryAlreadyExists(usernameHash);

        entryRepository.deleteAllByCreatedByHandle(auth.getHandle());

        try {
            User user = userRepository.findByHandle(auth.getHandle());

            LoginStorageEntry entry = new LoginStorageEntry();
            entry.setUsernameHash(usernameHash);
            entry.setEncKeypair(encKeypair);
            entry.setCreatedBy(user);
            entry.setCreatedAt(Instant.now());
            entryRepository.save(entry);
        } catch (Exception e) {
            throw new LoginEntryInvalid();
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER')")
    @FisEndpoint(summary = "Deletes all Login Entries for the current User/Handle")
    public void deleteEntry(@AuthenticationPrincipal GoofyAuthUser auth) {
        entryRepository.deleteAllByCreatedByHandle(auth.getHandle());
    }

    @GetMapping("/{usernameHash}")
    @FisEndpoint(summary = "Gets the Entry from a Username Hash")
    public String getEntry(@PathVariable String usernameHash) throws LoginEntryNotFound {
        LoginStorageEntry entry = entryRepository.findByUsernameHash(usernameHash);
        if (entry == null)
            throw new LoginEntryNotFound(usernameHash);
        return entry.getEncKeypair();
    }
}
