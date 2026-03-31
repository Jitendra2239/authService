package com.example.authservice.models;

import jakarta.persistence.Entity;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Token extends BaseModel  {



    private String email;


    private String accessToken;


    private String refreshToken;

    private boolean isRevoked;

    private Date expiryDate;
}