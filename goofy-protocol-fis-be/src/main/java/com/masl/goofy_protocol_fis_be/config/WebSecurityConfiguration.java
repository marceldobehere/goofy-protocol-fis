package com.masl.goofy_protocol_fis_be.config;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfiguration {

    private final GoofyAuthFilter goofyAuthFilter;

    public WebSecurityConfiguration(GoofyAuthFilter goofyAuthFilter) {
        this.goofyAuthFilter = goofyAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/test/**").permitAll() // Testing Stuff // TODO: Remove or Only have for integration tests?
                .requestMatchers("/api/general/**").permitAll() // General Info of FIS Server
                .requestMatchers("/api/lookup/**").permitAll() // Lookup and set FIS Info
                .requestMatchers("/api/register/**").permitAll() // Registration
                .requestMatchers("/api/global-handle/**").permitAll() // Global Handle Info
                .requestMatchers("/api/ext-service-access/**").permitAll() // Redirect Links for Service Config/Access

                .requestMatchers("/api/check/**").permitAll() // Check/Verify Request Validity / Get User Info
                .requestMatchers("/api/account-details/**").hasRole(ROLES.REGISTERED_USER) // Account Details
                .requestMatchers("/api/key-storage/**").hasRole(ROLES.REGISTERED_USER) // Password/Keypair Storage
                .requestMatchers("/api/id-storage/**").hasRole(ROLES.REGISTERED_USER) // Identity Storage
                .requestMatchers("/api/service-entry-config/**").hasRole(ROLES.REGISTERED_USER) // Service Entry Configuration
                .requestMatchers("/api/service-data-config/**").hasRole(ROLES.REGISTERED_USER) // Service Data Access Configuration
                .requestMatchers("/api/gdpr/**").hasRole(ROLES.REGISTERED_USER) // GDPR Stuff (for Users)

                .requestMatchers("/api/service-table/**").hasRole(ROLES.OUTSIDE_ENTITY) // Service Table Access
                .requestMatchers("/api/service-bucket/**").hasRole(ROLES.OUTSIDE_ENTITY) // Service Bucket Access


                .requestMatchers("/api/admin/**").hasRole(ROLES.ADMIN)
                .requestMatchers("/api/**").hasRole(ROLES.REGISTERED_USER)
                .anyRequest().authenticated()
        );

        http.addFilterBefore(goofyAuthFilter, BasicAuthenticationFilter.class);
        return http.build();
    }
}

