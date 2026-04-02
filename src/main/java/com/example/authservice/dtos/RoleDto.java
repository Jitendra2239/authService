package com.example.authservice.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleDto {

    private Long id;

    @NotBlank(message = "Role name is required")
    private String roleName;
}