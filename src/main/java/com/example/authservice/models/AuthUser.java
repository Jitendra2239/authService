package com.example.authservice.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class AuthUser extends BaseModel{



    private Long userId; // from UserService
    private String username;

    private String email;

    private String password;
    private  String phone;
    @ElementCollection
    private List<String> roles;

    private boolean isActive;
}