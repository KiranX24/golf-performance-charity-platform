package com.digitalheroes.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.digitalheroes.entity.User;

@Service
public class JwtService {
	private final SecretKey key;
	private final long expiration;

	public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-ms}") long expiration) {
		if (secret.length() < 32)
			throw new IllegalArgumentException("JWT_SECRET must contain at least 32 characters");
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expiration = expiration;
	}

	public String generate(User u) {
		Date now = new Date();
		return Jwts.builder().subject(u.getEmail()).claim("role", u.getRole().name()).issuedAt(now)
				.expiration(new Date(now.getTime() + expiration)).signWith(key).compact();
	}

	public String username(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public boolean valid(String token) {
		try {
			Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
