package com.example.authservice.repositories;


import com.example.authservice.models.AuthUser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {



    Optional<AuthUser> findByEmail(String email);
}
