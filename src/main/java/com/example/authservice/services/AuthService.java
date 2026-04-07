package com.example.authservice.services;

import com.example.authservice.dtos.*;
import com.example.authservice.exceptions.UserNotFoundException;
import com.example.authservice.models.AuthUser;
import com.example.authservice.models.RefreshToken;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    RegisterResponse register(RegisterRequest request) throws UserNotFoundException;

    AuthResponse login(LoginRequest request);

    String getProfile(String token);

    void changePassword(String token, ChangePasswordRequest request);

    void logout(String token);

    boolean validate(String token);

    public AuthResponse refreshToken(String requestToken);

    public void forgotPassword(String email);

    public void resetPassword(ResetPasswordRequest request);
}