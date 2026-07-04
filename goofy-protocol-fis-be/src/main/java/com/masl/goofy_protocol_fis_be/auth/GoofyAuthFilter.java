package com.masl.goofy_protocol_fis_be.auth;

import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.request.BasicRequestValidator;
import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;
import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequestValidator;
import com.masl.goofy_protocol_core.crypto.exceptions.PubSplitKeyNotFound;
import com.masl.goofy_protocol_fis_be.crypto.HandleHelper;
import com.masl.goofy_protocol_fis_be.entity.User;
import com.masl.goofy_protocol_fis_be.exception.client.InvalidSignatureException;
import com.masl.goofy_protocol_fis_be.exception.server.PublicKeyLookupFailed;
import com.masl.goofy_protocol_fis_be.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GoofyAuthFilter extends OncePerRequestFilter {
    private final SignedRequestValidator validator = new BasicRequestValidator();
    private final HandleCrypto handleCrypto;
    private final UserRepository userRepository;
    private final int maxRequestSizeBytes;
    private final boolean disableUniqueIdCheck;
    private final HandlerExceptionResolver resolver;

    public GoofyAuthFilter(HandleHelper handleHelper, UserRepository userRepository, Environment env,
                           @Value("${goofy.auth.max-cache-bytes}") int maxCacheBytes,
                           @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.handleCrypto = new HandleCrypto(handleHelper);
        this.userRepository = userRepository;
        this.disableUniqueIdCheck = env.acceptsProfiles(Profiles.of("test")); // Important for Perf Testing
        this.maxRequestSizeBytes = maxCacheBytes;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) {
        try {
            Map<String, String> headers = Collections.list(request.getHeaderNames())
                    .stream().collect(Collectors.toMap(h -> h, request::getHeader));

            // If the Request is not signed, we dont need to check it
            if (!SignedRequest.hasAllRequestHeaders(headers)) {
                SecurityContextHolder.getContext().setAuthentication(new GoofyAuth());
                filterChain.doFilter(request, response);
                return;
            }

            // Cache Request So body can be read without issues
            ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, maxRequestSizeBytes);
            byte[] body = wrapped.getInputStream().readAllBytes(); // should be an empty array if no body is provided

            // Parse Request
            SignedRequest req;
            try {
                req = SignedRequest.fromRequestHeaders(headers, request.getMethod(), request.getRequestURI(), body, handleCrypto);
            } catch (PubSplitKeyNotFound e) {
                throw new PublicKeyLookupFailed(e.handle);
            }

            // Check Validity
            SignedRequest.SignedRequestValidity valid = req.isValid(handleCrypto, validator);
            if (!valid.equals(SignedRequest.SignedRequestValidity.VALID))
                throw new InvalidSignatureException(valid);

            // Invalidate ID
            if (!disableUniqueIdCheck)
                validator.invalidateUniqueId(req.uniqueId());

            // Get User Data and Create Authentication
            User user = userRepository.findById(req.handle()).orElse(null);
            boolean isUser = user != null;
            boolean isAdmin = user != null && user.isAdmin();
            SecurityContextHolder.getContext().setAuthentication(new GoofyAuth(req, isUser, isAdmin));

            // Continue
            filterChain.doFilter(wrapped, response);
        } catch (Exception e) {
            resolver.resolveException(request, response, null, e);
        }

        SecurityContextHolder.clearContext();
    }
}
