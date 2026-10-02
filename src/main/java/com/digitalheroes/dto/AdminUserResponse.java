package com.digitalheroes.dto;
import com.digitalheroes.entity.Role; public record AdminUserResponse(Long id,String email,String fullName,Role role,boolean active){}