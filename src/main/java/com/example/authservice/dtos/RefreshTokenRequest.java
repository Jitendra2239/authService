package com.example.authservice.dtos;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}