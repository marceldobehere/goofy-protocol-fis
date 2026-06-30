package com.masl.goofy_protocol_fis_be.auth;

import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;
import com.masl.goofy_protocol_core.crypto.exceptions.PubSplitKeyNotFound;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class GoofyAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws IOException, ServletException {
        Map<String, String> headers = Collections.list(request.getHeaderNames())
                .stream().collect(Collectors.toMap(h -> h, request::getHeader));

        // Cache Request So body can be read without issues
        // TODO: Add configuration for max request sizes and use it in this cache limit too!
        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, 0);
        byte[] body = wrapped.getInputStream().readAllBytes(); // should be an empty array if no body is provided

        // TODO: Bind HandleCrypto
        SignedRequest req;
        try {
            req = SignedRequest.fromRequestHeaders(headers, request.getMethod(), request.getRequestURI(), body, null);
        } catch (PubSplitKeyNotFound e) {
            // TODO: Document correct Error Code/Object and throw it
            throw new ServletException("TODO");
        }

        // TODO: Bind HandleCrypto and Basic Validator
        if (req.isValid(null, null)) {
            // TODO: implement correct auth
            SecurityContextHolder.getContext().setAuthentication(new GoofyAuth());

            // Continue
            filterChain.doFilter(wrapped, response);
        } else {
            // TODO: Document correct Error Code/Object and throw it
            throw new ServletException("TODO");
        }
    }
}
