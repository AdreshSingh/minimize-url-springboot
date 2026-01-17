package com.example.minimizeurlspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String message;

    @com.fasterxml.jackson.annotation.JsonProperty("access_token")
    private String token;

    private String tokenType;
}
