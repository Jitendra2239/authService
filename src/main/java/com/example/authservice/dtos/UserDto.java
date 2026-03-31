package com.example.authservice.dtos;

import lombok.Data;

@Data
public class UserDto {
    private String email;
    private String password;
}