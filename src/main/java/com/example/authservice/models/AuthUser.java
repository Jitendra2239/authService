package com.example.authservice.models;

import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class AuthUser extends BaseModel{



    private Long userId; // from UserService

    private String email;

    private String password;

    private String role;

    private boolean isActive;
}