package com.example.authservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken  extends  BaseModel{

    private String token;

    private String deviceId;     // 🔥 important
    private String deviceName;   // optional (Chrome, Android, etc.)

    private boolean revoked;

    private LocalDateTime expiryDate;

    @ManyToOne
    private AuthUser user;
}