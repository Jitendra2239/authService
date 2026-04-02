package com.example.authservice.services;

import com.example.authservice.dtos.*;
import com.example.authservice.exceptions.UserNotFoundException;
import com.example.authservice.models.AuthUser;
import com.example.authservice.models.Token;
import com.example.authservice.repositories.AuthUserRepository;
import com.example.authservice.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthUserRepository authUserRepository;
    private  final TokenRepository tokenRepository ;
    @Value("${user.service.url}")
    private String userServiceUrl;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        RegisterResponse registerResponse = new RegisterResponse();

        try {

            AuthUser authUser = new AuthUser();
            authUser.setEmail(request.getEmail());
            authUser.setPassword(passwordEncoder.encode(request.getPassword()));

            authUser = authUserRepository.save(authUser);


            UserResponseDto userResponseDto = restTemplate.postForObject(
                    userServiceUrl + "/register",
                    request,
                    UserResponseDto.class
            );

            if (userResponseDto == null) {
                throw new RuntimeException("UserService returned null");
            }


            authUser.setUserId(userResponseDto.getId());
            authUser.setUsername(userResponseDto.getName());
            authUser.setPhone(userResponseDto.getPhone());




            AuthUser savedUser = authUserRepository.save(authUser);

            // 🔹 Step 4: Success Response
            registerResponse.setEmail(savedUser.getEmail());
            registerResponse.setPhone(savedUser.getPhone());
            registerResponse.setUserId(savedUser.getId());
            registerResponse.setName(savedUser.getUsername());
            registerResponse.setRequestStatus(RequestStatus.SUCCESS);
            registerResponse.setMessage("User Registered Successfully");

        } catch (Exception ex) {

            // 🔥 Rollback logic
            try {
                // delete auth user if exists
                authUserRepository.findByEmail(request.getEmail())
                        .ifPresent(user -> authUserRepository.delete(user));
            } catch (Exception e) {
                System.out.println("Rollback failed: " + e.getMessage());
            }

            registerResponse.setRequestStatus(RequestStatus.FAILURE);
            registerResponse.setMessage("Registration failed: " + ex.getMessage());
        }

        return registerResponse;
    }



    @Override
    public AuthResponse login(LoginRequest request) {

        AuthUser user = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = UUID.randomUUID().toString();

        Token token = new Token();
        token.setEmail(user.getEmail());
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setRevoked(false);
        tokenRepository.save(token);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public String getProfile(String token) {

        String email = jwtService.extractEmail(token);

        return "Logged in user: " + email;
    }


    @Override
    public void changePassword(String token, ChangePasswordRequest request) {

        String email = jwtService.extractEmail(token);

        UserDto user = restTemplate.getForObject(
                userServiceUrl + "/api/users/email/" + email,
                UserDto.class
        );

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }

        // Update password in UserService
        request.setNewPassword(passwordEncoder.encode(request.getNewPassword()));

        restTemplate.put(
                userServiceUrl + "/api/users/password/" + email,
                request
        );
    }
    @Override
    public RefreshTokenRequest refreshToken(String refreshToken) {

        Token token = tokenRepository.findAll().stream()
                .filter(t -> refreshToken.equals(t.getRefreshToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.isRevoked()) {
            throw new RuntimeException("Token revoked");
        }

        String newAccessToken = jwtService.generateToken(token.getEmail());

        token.setAccessToken(newAccessToken);
        tokenRepository.save(token);

        return  new RefreshTokenRequest();
    }



    @Override
    public void logout(String token) {

        Token dbToken = tokenRepository.findByAccessToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        dbToken.setRevoked(true);

        tokenRepository.save(dbToken);
    }

    @Override
    public boolean validate(String token) {
        Token dbToken = tokenRepository.findByAccessToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        if (dbToken.isRevoked()) {
            return false;
        }

        return jwtService.isValid(token, dbToken.getEmail());
    }


}