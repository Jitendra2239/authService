package com.example.authservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponse {
    private Long userId;
    private String name;

    private String email;

    private String phone;
    private String message;
    private RequestStatus requestStatus;
}
