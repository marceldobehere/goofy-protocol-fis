package com.masl.goofy_protocol_fis_be.auth;

import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.request.BasicRequestValidator;
import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;
import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequestValidator;
import com.masl.goofy_protocol_core.crypto.exceptions.PubSplitKeyNotFound;
import com.masl.goofy_protocol_fis_be.crypto.HandleHelper;
import com.masl.goofy_protocol_fis_be.exception.PublicKeyNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GoofyAuthFilter extends OncePerRequestFilter {
    private final SignedRequestValidator validator = new BasicRequestValidator();
    private final HandleCrypto handleCrypto;

    public GoofyAuthFilter(HandleHelper handleHelper) {
        this.handleCrypto = new HandleCrypto(handleHelper);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws IOException, ServletException {
        Map<String, String> headers = Collections.list(request.getHeaderNames())
                .stream().collect(Collectors.toMap(h -> h, request::getHeader));

        // If the Request is not signed, we dont need to check it
        if (!SignedRequest.hasAllRequestHeaders(headers)) {
            SecurityContextHolder.getContext().setAuthentication(new GoofyAuth());
            filterChain.doFilter(request, response);
            return;
        }

        // Cache Request So body can be read without issues
        // TODO: Add configuration for max request sizes and use it in this cache limit too!
        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, 0);
        byte[] body = wrapped.getInputStream().readAllBytes(); // should be an empty array if no body is provided

        // Parse Request
        SignedRequest req;
        try {
            req = SignedRequest.fromRequestHeaders(headers, request.getMethod(), request.getRequestURI(), body, handleCrypto);
        } catch (PubSplitKeyNotFound e) {
            throw new PublicKeyNotFoundException();
        }

        // Check Validity
        if (!req.isValid(handleCrypto, validator))
            throw new PublicKeyNotFoundException();

        // Get User Data and Create Authentication
        boolean isUser = false; // TODO: fetch from DB
        boolean isAdmin = false; // TODO: fetch from DB
        SecurityContextHolder.getContext().setAuthentication(new GoofyAuth(req, isUser, isAdmin));

        // Continue
        filterChain.doFilter(wrapped, response);
    }
}
