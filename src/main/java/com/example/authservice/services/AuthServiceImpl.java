package com.example.authservice.services;

import com.example.authservice.dtos.*;
import com.example.authservice.exceptions.UserNotFoundException;
import com.example.authservice.models.AuthUser;
import com.example.authservice.models.PasswordResetToken;
import com.example.authservice.models.RefreshToken;
import com.example.authservice.models.Token;
import com.example.authservice.repositories.AuthUserRepository;
import com.example.authservice.repositories.PasswordResetTokenRepository;
import com.example.authservice.repositories.RefreshTokenRepository;
import com.example.authservice.repositories.TokenRepository;
import com.jitendra.event.PasswordResetEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
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
    private  final PasswordResetTokenRepository passwordResetTokenRepository;
    private  final RefreshTokenRepository refreshTokenRepository;
    private final KafkaTemplate<String, PasswordResetEvent> kafkaTemplate;
    @Value("${user.service.url}")
    private String userServiceUrl;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        RegisterResponse registerResponse = new RegisterResponse();

        try {

            AuthUser authUser = new AuthUser();
            authUser.setEmail(request.getEmail());
            authUser.setPassword(passwordEncoder.encode(request.getPassword()));
            authUser.setUsername(request.getName());
            authUser.setPhone(request.getPhone());
            authUser.setRoles(request.getRoles());
            authUser.setActive(true);

            authUser = authUserRepository.save(authUser);


            try {

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<RegisterRequest> entity = new HttpEntity<>(request, headers);

                ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                        "http://localhost:8083/api/v1/users/register",
                        HttpMethod.POST,
                        entity,
                        UserResponseDto.class
                );
                  System.out.println(response.getBody());
                UserResponseDto userResponseDto = response.getBody();
                System.out.println("userResponseDto->"+userResponseDto);
                if (userResponseDto == null) {
                    throw new RuntimeException("UserService returned null");
                }


                authUser.setUserId(userResponseDto.getId());
                authUser.setUsername(userResponseDto.getName());
                authUser.setPhone(userResponseDto.getPhone());
            } catch (HttpClientErrorException e) {
                throw new RuntimeException("User service error: " + e.getResponseBodyAsString());
            }





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


        String accessToken = jwtService.generateToken(user);


        String refreshTokenValue = UUID.randomUUID().toString();

        // 5️⃣ Save Refresh Token in DB
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshToken.setRevoked(false);


        refreshToken.setDeviceId(request.getDeviceId());   // pass from frontend
        refreshToken.setDeviceName(request.getDeviceName());

        refreshTokenRepository.save(refreshToken);


        Token token = new Token();
        token.setEmail(user.getEmail());
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshTokenValue);
        token.setRevoked(false);

        tokenRepository.save(token);


        return new AuthResponse(accessToken, refreshTokenValue);
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
    public AuthResponse refreshToken(String requestToken) {


        RefreshToken refreshToken = validateRefreshToken(requestToken);


        AuthUser user = refreshToken.getUser();


        String newAccessToken = generateAccessToken(user);


        String newRefreshToken = rotateRefreshToken(refreshToken);


        return new AuthResponse(newAccessToken, newRefreshToken);
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
    @Override
    public void forgotPassword(String email) {

        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete old tokens (important)
        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15)) // 15 min
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send Email
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        PasswordResetEvent event = new PasswordResetEvent();
        event.setEmail(email);
        event.setResetLink(resetLink);
        kafkaTemplate.send("password-reset-topic", event);
    }
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Token already used");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        AuthUser user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authUserRepository.save(user);

        // mark token as used instead of delete (better for auditing)
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
    private RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        return refreshToken;
    }
    private String rotateRefreshToken(RefreshToken refreshToken) {

        String newToken = UUID.randomUUID().toString();

        refreshToken.setToken(newToken);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        refreshTokenRepository.save(refreshToken);

        return newToken;
    }
    private String generateAccessToken(AuthUser user) {
        return jwtService.generateToken(user); // ⚠️ FIX: not generateRefreshToken
    }

}