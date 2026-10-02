package com.digitalheroes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Minimal, dependency-free endpoint used to verify the application has started
 * and is reachable. Does not touch the database — see the "readiness" check via
 * Flyway/DB verification steps in the setup guide for confirming the DB
 * connection itself.
 */
@RestController
public class HealthController {

	@GetMapping("/api/health")
	public Map<String, Object> health() {
		return Map.of("status", "UP", "service", "digital-heroes-backend", "timestamp", Instant.now().toString());
	}
}
