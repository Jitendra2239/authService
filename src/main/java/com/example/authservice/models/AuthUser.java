package com.example.authservice.models;

import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class AuthUser extends BaseModel{



    private Long userId; // from UserService
    private String username;

    private String email;

    private String password;
    private  String phone;
    private String role;

    private boolean isActive;
}