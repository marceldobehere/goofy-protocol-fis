package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.dto.request.RegistrationRequestDto;
import com.masl.goofy_protocol_fis_be.entity.RegistrationCode;
import com.masl.goofy_protocol_fis_be.entity.RegistrationRequest;
import com.masl.goofy_protocol_fis_be.entity.User;
import com.masl.goofy_protocol_fis_be.exception.client.HandleAlreadyRegistered;
import com.masl.goofy_protocol_fis_be.exception.client.InvalidRegisterCode;
import com.masl.goofy_protocol_fis_be.repository.RegistrationCodeRepository;
import com.masl.goofy_protocol_fis_be.repository.RegistrationRequestRepository;
import com.masl.goofy_protocol_fis_be.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RegistrationService {
    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final RegistrationCodeRepository registrationCodeRepository;
    private final RegistrationRequestRepository registrationRequestRepository;
    private final UserRepository userRepository;

    public RegistrationService(RegistrationCodeRepository registrationCodeRepository, RegistrationRequestRepository registrationRequestRepository, UserRepository userRepository) {
        this.registrationCodeRepository = registrationCodeRepository;
        this.registrationRequestRepository = registrationRequestRepository;
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

    private void useCode(String code, User user) {
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

        // Check if Handle is already registered
        if (userRepository.findById(auth.getHandle()).isPresent())
            throw new HandleAlreadyRegistered(auth.getHandle());

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

    public void submitRegistrationRequest(RegistrationRequestDto requestDto) {
        log.info("Received Registration Request: {}", requestDto);
        RegistrationRequest request = new RegistrationRequest();
        request.setMesssage(requestDto.getMessage());
        request.setGeneralContact(requestDto.getContact());
        request.setOptEmail(requestDto.getOptEmail());
        request.setCreatedAt(Instant.now());
        registrationRequestRepository.save(request);
    }

    public List<RegistrationRequest> getAllRequests() {
        return registrationRequestRepository.findAll();
    }

    public List<RegistrationRequest> getAllUnresolvedRequests() {
        return registrationRequestRepository.findAllByResolvedAtIsNull();
    }
}
