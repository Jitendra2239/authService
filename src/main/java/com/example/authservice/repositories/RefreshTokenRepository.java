package com.example.authservice.repositories;

import com.example.authservice.models.AuthUser;
import com.example.authservice.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(AuthUser user);
}