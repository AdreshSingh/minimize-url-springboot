package com.example.minimizeurlspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShortUrlResponse {
    private String originalUrl;
    private String shortUrl;
}
