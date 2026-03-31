package com.example.authservice.controllers;

import com.example.authservice.dtos.AuthResponse;
import com.example.authservice.dtos.LoginRequest;
import com.example.authservice.dtos.RefreshTokenRequest;
import com.example.authservice.dtos.RegisterRequest;
import com.example.authservice.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(
            @RequestHeader("Authorization") String header) {

        String token = header.substring(7);

        boolean isValid = authService.validate(token);

        if (isValid) {
            return ResponseEntity.ok("Token is valid");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token");
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String header) {

        String token = header.substring(7);

        authService.logout(token);

        return ResponseEntity.ok("Logged out successfully");
    }


    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenRequest> refreshToken(
            @RequestBody RefreshTokenRequest request) {

       RefreshTokenRequest response = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(response);
    }
}