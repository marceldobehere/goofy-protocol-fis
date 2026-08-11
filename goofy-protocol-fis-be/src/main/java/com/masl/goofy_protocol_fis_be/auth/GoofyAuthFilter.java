package com.masl.goofy_protocol_fis_be.auth;

import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.request.BasicRequestValidator;
import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;
import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequestValidator;
import com.masl.goofy_protocol_core.crypto.exceptions.PubSplitKeyNotFound;
import com.masl.goofy_protocol_fis_be.crypto.FisHandleCrypto;
import com.masl.goofy_protocol_fis_be.entity.IdentityStorageEntry;
import com.masl.goofy_protocol_fis_be.entity.User;
import com.masl.goofy_protocol_fis_be.exception.client.ContentTooLarge;
import com.masl.goofy_protocol_fis_be.exception.client.InvalidSignature;
import com.masl.goofy_protocol_fis_be.exception.server.PublicKeyLookupFailed;
import com.masl.goofy_protocol_fis_be.repository.IdentityStorageEntryRepository;
import com.masl.goofy_protocol_fis_be.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GoofyAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(GoofyAuthFilter.class);

    private final SignedRequestValidator validator = new BasicRequestValidator();
    private final HandleCrypto handleCrypto;
    private final UserRepository userRepository;
    private final IdentityStorageEntryRepository identityRepository;
    private final int maxRequestSizeBytes;
    private final boolean disableUniqueIdCheck;
    private final HandlerExceptionResolver resolver;

    public GoofyAuthFilter(FisHandleCrypto handleCrypto, UserRepository userRepository, IdentityStorageEntryRepository identityRepository, Environment env,
                           @Value("${goofy.auth.max-request-bytes}") int maxRequestBytes,
                           @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.handleCrypto = handleCrypto;
        this.userRepository = userRepository;
        this.identityRepository = identityRepository;
        this.disableUniqueIdCheck = env.acceptsProfiles(Profiles.of("test")); // Important for Perf Testing
        this.maxRequestSizeBytes = maxRequestBytes;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)  {
        try {
            log.info("Incoming Request: {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
            Map<String, String> headers = Collections.list(request.getHeaderNames())
                    .stream().collect(Collectors.toMap(h -> h, request::getHeader));
            log.info("Request Headers: {}", headers);

            // If the Request is not signed, we don't need to check it
            if (!SignedRequest.hasAllRequestHeaders(headers)) {
                log.info("Request is not signed, skipping authentication for: {} {}", request.getMethod(), request.getRequestURI());
                SecurityContextHolder.getContext().setAuthentication(new GoofyAuth());
                filterChain.doFilter(request, response);
                return;
            }

            // Cache Request So body can be read without issues
            log.info("Request is signed, reading & caching body for: {} {}", request.getMethod(), request.getRequestURI());
            ContentCachingRequestWrapper _wrapped = new ContentCachingRequestWrapper(request, maxRequestSizeBytes);
            byte[] body = getBody(_wrapped);

            // Parse Request
            SignedRequest req;
            try {
                req = SignedRequest.fromRequestHeaders(headers, request.getMethod(), request.getRequestURI(), body, handleCrypto);
            } catch (PubSplitKeyNotFound e) {
                throw new PublicKeyLookupFailed(e.handle);
            }
            log.info("Parsed SignedRequest: handle={}, uniqueId={}", req.handle(), req.uniqueId());

            // Check Validity
            SignedRequest.SignedRequestValidity valid = req.isValid(handleCrypto, validator);
            if (!valid.equals(SignedRequest.SignedRequestValidity.VALID))
                throw new InvalidSignature(valid);
            log.info("Request is valid, proceeding with authentication for: {} {}", request.getMethod(), request.getRequestURI());

            // Invalidate ID
            if (!disableUniqueIdCheck)
                validator.invalidateUniqueId(req.uniqueId());

            // Get User Data and Create Authentication
            User user = userRepository.findByHandle(req.handle());
            boolean isIdentity = user != null; // Could have the Role be exclusive, but I'd rather explicitly check against it in the respective Endpoints to avoid misunderstandings
            boolean isUser = user != null;
            boolean isAdmin = user != null && user.isAdmin();
            boolean isRestricted = user != null && !user.isAdmin() && user.isRestricted();
            log.info("User Data: isIdentity={}, isUser={}, isAdmin={}, isRestricted={}", isIdentity, isUser, isAdmin, isRestricted);

            // Check if it's a Registered Identity
            if (!isUser) {
                IdentityStorageEntry identity = identityRepository.findByHandle(req.handle());
                isIdentity = identity != null;
            }

            SecurityContextHolder.getContext().setAuthentication(new GoofyAuth(req, isIdentity, isUser, isAdmin, isRestricted));

            // Fix Body for Filter
            RequestBodyContentWrapper wrapped = new RequestBodyContentWrapper(_wrapped, maxRequestSizeBytes);
            wrapped.prepareInputStream();

            // Continue
            log.info("Authentication successful, proceeding with filter chain for: {} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(wrapped, response);
        } catch (Exception e) {
            resolver.resolveException(request, response, null, e);
        }

        SecurityContextHolder.clearContext();
    }

    private byte @NonNull [] getBody(ContentCachingRequestWrapper _wrapped) throws IOException, ContentTooLarge {
        byte[] body;
        try (var in = _wrapped.getInputStream()) {
            body = in.readNBytes(maxRequestSizeBytes + 1);

            // Read the remaining data without storing it
            // This is NEEDED FOR THE RESPONSE / ERROR HANDLING TO WORK PROPERLY (don't ask me why)
            byte[] buffer = new byte[8192];

            //noinspection StatementWithEmptyBody
            while (in.read(buffer) != -1);
        }

        if (body.length > maxRequestSizeBytes)
            throw new ContentTooLarge();
        return body;
    }
}
