package com.masl.goofy_protocol_fis_be.service;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthUser;
import com.masl.goofy_protocol_fis_be.dto.request.RegistrationRequestDto;
import com.masl.goofy_protocol_fis_be.entity.*;
import com.masl.goofy_protocol_fis_be.exception.client.GenericNotFound;
import com.masl.goofy_protocol_fis_be.exception.client.HandleAlreadyRegistered;
import com.masl.goofy_protocol_fis_be.exception.client.InvalidRegisterCode;
import com.masl.goofy_protocol_fis_be.exception.client.RegistrationCodeAlreadyUsed;
import com.masl.goofy_protocol_fis_be.repository.*;
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
    private final IdentityStorageEntryRepository identityStorageEntryRepository;
    private final UserQuotasRepository userQuotasRepository;

    public RegistrationService(RegistrationCodeRepository registrationCodeRepository, RegistrationRequestRepository registrationRequestRepository, UserRepository userRepository, IdentityStorageEntryRepository identityStorageEntryRepository, UserQuotasRepository userQuotasRepository) {
        this.registrationCodeRepository = registrationCodeRepository;
        this.registrationRequestRepository = registrationRequestRepository;
        this.userRepository = userRepository;
        this.identityStorageEntryRepository = identityStorageEntryRepository;
        this.userQuotasRepository = userQuotasRepository;
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

    public void deleteRegistrationCode(String code) {
        log.info("Deleting registration code: {}", code);
        registrationCodeRepository.deleteByCode(code);
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

    private void useCode(String code, User user) throws RegistrationCodeAlreadyUsed {
        RegistrationCode regCode = registrationCodeRepository.findById(code).orElseThrow(() -> new IllegalArgumentException("Invalid registration code"));
        if (regCode.getUsedAt() != null)
            throw new RegistrationCodeAlreadyUsed(code);

        regCode.setUsedBy(user);
        regCode.setUsedAt(Instant.now());
        registrationCodeRepository.save(regCode);
    }

    // Synchronized should hopefully be enough to avoid race conditions, since the backend will only really use one instance.
    // (Future) TODO: Make Code Safe from Race Conditions when scaling
    synchronized public void attemptRegistration(String code, GoofyAuthUser auth) throws InvalidRegisterCode, HandleAlreadyRegistered, RegistrationCodeAlreadyUsed {
        RegistrationCode regCode = getValidCode(code);
        if (regCode == null)
            throw new InvalidRegisterCode(code);

        // Check if Handle is already registered
        if (userRepository.findById(auth.getHandle()).isPresent())
            throw new HandleAlreadyRegistered(auth.getHandle());

        if (identityStorageEntryRepository.findByHandle(auth.getHandle()) != null)
            throw new HandleAlreadyRegistered(auth.getHandle());

        // Create User
        User user = new User();
        user.setHandle(auth.getHandle());
        user.setPubSplitKey(auth.getSignedRequest().pubSplitKey());
        user.setAdmin(regCode.getAdmin());
        userRepository.save(user);

        // Create User Quotas
        UserQuotas quotas = new UserQuotas();
        quotas.setUser(user);
        userQuotasRepository.save(quotas);

        // Use Code
        useCode(code, user);
        log.info("User {} registered successfully with code {}", user.getHandle(), code);
    }

    public void submitRegistrationRequest(RegistrationRequestDto requestDto, String handle) {
        log.info("Received Registration Request: {}", requestDto);
        RegistrationRequest request = new RegistrationRequest();
        request.setMessage(requestDto.getMessage());
        request.setGeneralContact(requestDto.getContact());
        request.setOptEmail(requestDto.getOptEmail());
        request.setCreatedAt(Instant.now());
        request.setCreatedByHandle(handle);
        registrationRequestRepository.save(request);
    }

    public List<RegistrationRequest> getAllRequests() {
        return registrationRequestRepository.findAll();
    }

    public List<RegistrationRequest> getAllUnresolvedRequests() {
        return registrationRequestRepository.findAllByResolvedAtIsNull();
    }

    public RegistrationRequest getRequestById(Long id) throws GenericNotFound {
        return registrationRequestRepository.findById(id).orElseThrow(() -> new GenericNotFound(id));
    }

    public void deleteRegistrationRequest(Long id) {
        registrationRequestRepository.deleteById(id);
    }

    public void setResolvedStatus(Long id, boolean resolved) throws GenericNotFound {
        RegistrationRequest req = registrationRequestRepository.findById(id)
                .orElseThrow(() -> new GenericNotFound(id));

        req.setResolvedAt(resolved ? Instant.now() : null);
        registrationRequestRepository.save(req);
    }
}
