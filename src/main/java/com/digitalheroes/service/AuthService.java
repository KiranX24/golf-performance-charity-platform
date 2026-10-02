package com.digitalheroes.service;

import com.digitalheroes.dto.*;
import com.digitalheroes.entity.*;
import com.digitalheroes.repository.UserRepository;
import com.digitalheroes.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager auth;
    private final JwtService jwt;

    public AuthService(
            UserRepository users,
            PasswordEncoder encoder,
            AuthenticationManager auth,
            JwtService jwt) {

        this.users = users;
        this.encoder = encoder;
        this.auth = auth;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse register(RegisterRequest r) {

        if (users.findByEmailIgnoreCase(r.email()).isPresent()) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        User u = new User();

        u.setEmail(r.email().toLowerCase());
        u.setFullName(r.fullName());
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRole(Role.ROLE_USER);

        users.save(u);

        return new AuthResponse(
                jwt.generate(u),
                "Bearer",
                toDto(u)
        );
    }

    public AuthResponse login(LoginRequest r) {

        auth.authenticate(
                new UsernamePasswordAuthenticationToken(
                        r.email(),
                        r.password()
                )
        );

        User u = users.findByEmailIgnoreCase(r.email())
                .orElseThrow();

        return new AuthResponse(
                jwt.generate(u),
                "Bearer",
                toDto(u)
        );
    }

    public User current(String email) {

        return users.findByEmailIgnoreCase(email)
                .orElseThrow();
    }

    public UserResponse toDto(User u) {

        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getRole(),
                u.isActive()
        );
    }
}