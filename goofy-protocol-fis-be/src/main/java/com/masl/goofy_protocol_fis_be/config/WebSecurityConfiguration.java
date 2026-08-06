package com.masl.goofy_protocol_fis_be.config;

import com.masl.goofy_protocol_fis_be.auth.GoofyAuthFilter;
import jakarta.servlet.DispatcherType;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.Security;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfiguration {

    private final GoofyAuthFilter goofyAuthFilter;
    private final Environment env;

    public WebSecurityConfiguration(GoofyAuthFilter goofyAuthFilter, Environment env) {
        this.goofyAuthFilter = goofyAuthFilter;
        this.env = env;

        // Set up Bouncy Castle globally
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new BouncyCastlePQCProvider());
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(false);
        config.setMaxAge(360000L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        boolean testOrDev = env.acceptsProfiles(Profiles.of("test", "dev"));
        boolean testOnly = env.acceptsProfiles(Profiles.of("test"));

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> {
                // Only for integration tests
                if (testOnly) {
                    auth
                        .requestMatchers("/fis-api/test/**").permitAll();
                }

                // Only for dev or integration tests
                if (testOrDev) {
                    auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll();

                    // H2-Console
                    http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
                }

                // Streaming Responses
                auth.dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll();

                // Default values
                auth
                    .requestMatchers("/").permitAll() // Redirect to Frontend
                    .requestMatchers("/short/**").permitAll() // Short Frontend Redirects
                    .requestMatchers("/fis-api/general/**").permitAll() // General Info of FIS Server
                    .requestMatchers("/fis-api/register/**").permitAll() // Registration of Users
                    .requestMatchers("/fis-api/user/**").permitAll() // User Info, Lookup, Export, etc.
                    .requestMatchers("/fis-api/redirect/**").permitAll() // Redirect Links for Service Login/Config/Access

                    .requestMatchers("/fis-api/login-storage/**").permitAll() // Password/Keypair Storage
                    .requestMatchers("/fis-api/identity-storage/**").hasRole(ROLES.OUTSIDE_ENTITY) // Identity Keypair Storage for Services.

                    .requestMatchers("/fis-api/service-entry/**").hasRole(ROLES.REGISTERED_IDENTITY) // Service Entry Configuration

                    // TODO: Potentially change if I decide that access requests for public content do not need to be signed
                    .requestMatchers("/fis-api/service-bucket/**").hasRole(ROLES.OUTSIDE_ENTITY) // Service Bucket Access
                    .requestMatchers("/fis-api/service-table/**").hasRole(ROLES.OUTSIDE_ENTITY) // Service Table Access

                    .requestMatchers("/fis-api/admin/**").hasRole(ROLES.ADMIN)
                    .anyRequest().hasRole(ROLES.ADMIN);
            }
        );

        http.addFilterBefore(goofyAuthFilter, BasicAuthenticationFilter.class);
        return http.build();
    }
}

