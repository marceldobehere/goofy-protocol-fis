package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.entity.RegistrationCode;
import com.masl.goofy_protocol_fis_be.entity.User;
import com.masl.goofy_protocol_fis_be.exception.client.InvalidRegisterCode;
import com.masl.goofy_protocol_fis_be.repository.RegistrationCodeRepository;
import com.masl.goofy_protocol_fis_be.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// TODO: Test
@Service
public class RegistrationService {
    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final RegistrationCodeRepository registrationCodeRepository;
    private final UserRepository userRepository;

    public RegistrationService(RegistrationCodeRepository registrationCodeRepository, UserRepository userRepository) {
        this.registrationCodeRepository = registrationCodeRepository;
        this.userRepository = userRepository;
    }

    public RegistrationCode createNewRegistrationCode(boolean isAdmin) {
        return createNewRegistrationCode(null, isAdmin);
    }

    public RegistrationCode createNewRegistrationCode(User createdBy, boolean isAdmin) {
        RegistrationCode code = new RegistrationCode();
        code.setCode(UUID.randomUUID().toString());
        code.setAdmin(isAdmin);
        code.setCreatedBy(createdBy);
        code.setCreatedAt(Instant.now());
        log.info("Created new registration code: {} (Admin: {})", code.getCode(), isAdmin);
        return registrationCodeRepository.save(code);
    }

    public boolean anyCodesExist() {
        return registrationCodeRepository.count() > 0;
    }

    public boolean anyUsedCodesExist() {
        return !registrationCodeRepository.findAllByUsedAtIsNotNull().isEmpty();
    }

    public List<RegistrationCode> getAllUsedCodes() {
        return registrationCodeRepository.findAllByUsedAtIsNotNull();
    }

    public List<RegistrationCode> getAllUnusedCodes() {
        return registrationCodeRepository.findAllByUsedAtIsNull();
    }

    public boolean isCodeValid(String code) {
        return registrationCodeRepository.findByCodeAndUsedAtIsNull(code) != null;
    }

    public RegistrationCode getValidCode(String code) {
        return registrationCodeRepository.findByCodeAndUsedAtIsNull(code);
    }

    public void useCode(String code, User user) {
        RegistrationCode regCode = registrationCodeRepository.findById(code).orElseThrow(() -> new IllegalArgumentException("Invalid registration code"));
        if (regCode.getUsedAt() != null)
            throw new IllegalArgumentException("Registration code already used");

        regCode.setUsedBy(user);
        regCode.setUsedAt(Instant.now());
        registrationCodeRepository.save(regCode);
    }

    // Synchronized should hopefully be enough to avoid race conditions, since the backend will only really use one instance.
    // (Future) TODO: Make Code Safe from Race Conditions when scaling
    synchronized public void attemptRegistration(String code, GoofyAuthUser auth) {
        RegistrationCode regCode = getValidCode(code);
        if (regCode == null)
            throw new InvalidRegisterCode(code);

        // Create User
        User user = new User();
        user.setHandle(auth.getHandle());
        user.setPubSplitKey(auth.getSignedRequest().pubSplitKey());
        user.setAdmin(regCode.getAdmin());
        userRepository.save(user);

        // Use Code
        useCode(code, user);
        log.info("User {} registered successfully with code {}", user.getHandle(), code);
    }

    public void requestRegistrationCode(String requestMessage) {
        log.info("Registration code requested with message: {}", requestMessage);
        // TODO: Implement
    }
}
