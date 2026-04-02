package com.example.authservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RegisterRequest {

    private String name;
    private String email;
    private String password;
    private String phone;

    private List<String> roles;

    private List<AddressDto> addresses;
}