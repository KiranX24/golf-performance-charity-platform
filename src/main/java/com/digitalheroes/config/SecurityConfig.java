package com.digitalheroes.config;

import com.digitalheroes.security.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwt;

    @Value("${app.cors.allowed-origin}")
    String origin;

    // Manual constructor because Lombok is removed
    public SecurityConfig(JwtAuthenticationFilter jwt) {
        this.jwt = jwt;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(c -> c.disable())

            .cors(c -> c.configurationSource(cors()))

            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(a -> a

                // Public endpoints
                .requestMatchers(
                    "/api/auth/**",
                    "/api/plans",
                    "/api/charities/**",
                    "/api/health",
                    "/api/stripe/webhook",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                )
                .permitAll()

                // Admin endpoints
                .requestMatchers("/api/admin/**")
                .hasRole("ADMIN")

                // Everything else requires authentication
                .anyRequest()
                .authenticated()
            )

            .addFilterBefore(
                jwt,
                UsernamePasswordAuthenticationFilter.class
            )

            .exceptionHandling(e -> e

                .authenticationEntryPoint((r, s, x) -> {
                    s.setStatus(401);
                    s.setContentType("application/json");

                    s.getWriter().write(
                        "{\"success\":false,\"message\":\"Authentication required\"}"
                    );
                })

                .accessDeniedHandler((r, s, x) -> {
                    s.setStatus(403);
                    s.setContentType("application/json");

                    s.getWriter().write(
                        "{\"success\":false,\"message\":\"Access denied\"}"
                    );
                })
            );

        return http.build();
    }

    @Bean
    CorsConfigurationSource cors() {

        CorsConfiguration c = new CorsConfiguration();

        c.setAllowedOrigins(List.of(origin));
        c.setAllowedMethods(List.of("*"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource s =
            new UrlBasedCorsConfigurationSource();

        s.registerCorsConfiguration("/**", c);

        return s;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration c) throws Exception {

        return c.getAuthenticationManager();
    }
}