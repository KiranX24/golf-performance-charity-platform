package com.digitalheroes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final CustomUserDetailsService uds;

    public JwtAuthenticationFilter(
            JwtService jwt,
            CustomUserDetailsService uds) {

        this.jwt = jwt;
        this.uds = uds;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain)
            throws ServletException, IOException {

        String authorizationHeader = req.getHeader("Authorization");

        System.out.println("========================================");
        System.out.println("JWT FILTER");
        System.out.println("Request: " + req.getMethod() + " " + req.getRequestURI());

        if (authorizationHeader == null) {
            System.out.println("Authorization header: MISSING");
            chain.doFilter(req, res);
            return;
        }

        System.out.println("Authorization header: PRESENT");

        if (!authorizationHeader.startsWith("Bearer ")) {
            System.out.println("Authorization header does not start with Bearer");
            chain.doFilter(req, res);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {

            boolean valid = jwt.valid(token);

            System.out.println("JWT valid: " + valid);

            if (!valid) {
                System.out.println("JWT validation FAILED");
                chain.doFilter(req, res);
                return;
            }

            String email = jwt.username(token);

            System.out.println("JWT username/email: " + email);

            UserDetails userDetails =
                    uds.loadUserByUsername(email);

            System.out.println("User loaded successfully: " + userDetails.getUsername());
            System.out.println("Authorities: " + userDetails.getAuthorities());
            System.out.println("Enabled: " + userDetails.isEnabled());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(req)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            System.out.println("Authentication SUCCESS");
            System.out.println(
                    "Authenticated user: "
                            + SecurityContextHolder
                                    .getContext()
                                    .getAuthentication()
                                    .getName()
            );

        } catch (Exception e) {

            System.out.println("JWT AUTHENTICATION FAILED");
            System.out.println("Exception: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());

            e.printStackTrace();
        }

        System.out.println("========================================");

        chain.doFilter(req, res);
    }
}