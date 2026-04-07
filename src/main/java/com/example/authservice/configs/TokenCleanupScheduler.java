package com.example.authservice.configs;

import com.example.authservice.repositories.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final PasswordResetTokenRepository repository;

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {

        log.info("Running token cleanup job...");

        int deleted = repository.deleteExpiredTokens(LocalDateTime.now());

        log.info("Deleted {} expired password reset tokens", deleted);
    }
}