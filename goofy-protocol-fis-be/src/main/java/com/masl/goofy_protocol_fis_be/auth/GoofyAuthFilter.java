package com.masl.goofy_protocol_fis_be.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class GoofyAuthFilter extends OncePerRequestFilter {

    // 'X-Goofy-Signature': encodeURIComponent(signature),
    // 'X-Goofy-Id': id,
    // 'X-Goofy-Valid-Until': validUntil,
    // 'X-Goofy-Public-Key': encodeURIComponent(publicKey),
    // 'X-Goofy-RAW': !!sendRawBody,

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String sig = request.getHeader("X-Goofy-Signature");
        String id = request.getHeader("X-Goofy-Id");
        String validUntil = request.getHeader("X-Goofy-Valid-Until");
        String publicKey = request.getHeader("X-Goofy-Public-Key");
        String handle = request.getHeader("X-Goofy-Handle");
        String rawBody = request.getHeader("X-Goofy-RAW"); // true|false






        SecurityContextHolder.getContext().setAuthentication(new GoofyAuth());
    }
}
