package com.example.authservice.dtos;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
}