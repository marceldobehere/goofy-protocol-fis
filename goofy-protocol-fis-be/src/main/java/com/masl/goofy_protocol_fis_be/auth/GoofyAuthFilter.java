package com.masl.goofy_protocol_fis_be.auth;

import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class GoofyAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws IOException {
        Map<String, String> headers = Collections.list(request.getHeaderNames())
                .stream().collect(Collectors.toMap(h -> h, request::getHeader));

        // TODO: Check if this could cause any issues
        byte[] body = request.getInputStream().readAllBytes();

        // TODO: implement
        SignedRequest req = SignedRequest.fromRequestHeaders(headers, request.getMethod(), request.getRequestURI(), body, null);
        if (req == null) {
            // TODO: implement
        }

        // TODO: implement
        if (req.isValid(null, null)) {
            // TODO: implement
            SecurityContextHolder.getContext().setAuthentication(new GoofyAuth());
        } else {
            // TODO: implement
        }
    }
}
