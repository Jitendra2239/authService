package com.example.authservice.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class UserResponseDto {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private String status;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ✅ Relations
    private Set<RoleDto> roles;

    private Set<AddressDto> addresses;
}



