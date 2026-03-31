package com.example.authservice.services;

import com.example.authservice.dtos.*;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    String register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    String getProfile(String token);

    void changePassword(String token, ChangePasswordRequest request);

    void logout(String token);
    boolean validate(String token);
    public RefreshTokenRequest refreshToken(String email);
}