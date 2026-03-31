package com.example.authservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth ->
                    auth
                            .requestMatchers("/api/auth/login", "/api/auth/register").permitAll())

                .csrf(csrf -> csrf.disable());
        return http.build();
    }
  //  .requestMatchers("/api/admin/**").hasRole("ADMIN")
   //                             .requestMatchers("/api/admin/**").hasRole("ADMIN").anyRequest().authenticated()
}
