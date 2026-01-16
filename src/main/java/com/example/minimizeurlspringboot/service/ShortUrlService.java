package com.example.minimizeurlspringboot.service;


import java.util.UUID;

// used with the mimicked user
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.minimizeurlspringboot.models.ShortUrl;
import com.example.minimizeurlspringboot.models.User;
import com.example.minimizeurlspringboot.repository.ShortUrlRepository;

// Purpose: Business logic for creating, retrieving, and managing short URLs

@Service
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;

    public ShortUrlService(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    // Core logic: generate + save short URL
    public ShortUrl createShortUrl(String originalUrl) {
        String shortCode = generateShortCode();

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setShortCode(shortCode);
        shortUrl.setAccessCount(0L);

        // // Mimicking a user for now as a token-based auth is not implemented
        // User user = new User();
        // user.setId(1L);
        // user.setUsername("dev");
        // user.setEmail("dev@gmail.com");
        // user.setPassword("1234");
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        // user.setCreatedAt(LocalDateTime.parse("2026-01-16 15:57:05.597345", formatter));

        // shortUrl.setUser(user); // No user association for now

        // Now after implementing JwtAuthenticationFilter, we can set the user properly in the controller
        User user = (User) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

        shortUrl.setUser(user);

        return shortUrlRepository.save(shortUrl);
    }

    public ShortUrl getOriginalUrl(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found"));
    }

    public void incrementAccessCount(ShortUrl shortUrl) {
        shortUrl.setAccessCount(shortUrl.getAccessCount() + 1);
        shortUrlRepository.save(shortUrl);
    }

    // Simple + safe generator
    private String generateShortCode() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }
}
