package com.digitalheroes.dto;
public record AuthResponse(String accessToken, String tokenType, UserResponse user){}