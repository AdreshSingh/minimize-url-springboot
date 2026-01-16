package com.example.minimizeurlspringboot.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.minimizeurlspringboot.dto.ShortUrlRequest;
import com.example.minimizeurlspringboot.dto.ShortUrlResponse;
import com.example.minimizeurlspringboot.models.ShortUrl;
import com.example.minimizeurlspringboot.service.ShortUrlService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Purpose: REST API endpoints for creating and redirecting short URLs

@RestController
@RequestMapping("/url")
public class ShortUrlController {
    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @PostMapping("/short")
    public ShortUrlResponse createShortUrl(@RequestBody ShortUrlRequest request) {
        ShortUrl shortUrl = shortUrlService.createShortUrl(request.getOriginalUrl());

        return new ShortUrlResponse(shortUrl.getOriginalUrl(), "http://localhost:8080/" + shortUrl.getShortCode());
    }

    @GetMapping("/{shortCode}")
    public void redirect(
            @PathVariable String shortCode,
            HttpServletResponse response) throws IOException {

        ShortUrl shortUrl = shortUrlService.getOriginalUrl(shortCode);
        shortUrlService.incrementAccessCount(shortUrl);

        response.sendRedirect(shortUrl.getOriginalUrl());
    }

}
