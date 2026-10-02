package com.digitalheroes.dto;
import com.digitalheroes.entity.Role;
public record UserResponse(Long id,String email,String fullName,Role role,boolean active){}