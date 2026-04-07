package com.example.authservice.repositories;


import com.example.authservice.models.AuthUser;
import com.example.authservice.models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(AuthUser user);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :time")
    int deleteExpiredTokens(@Param("time") LocalDateTime time);
}