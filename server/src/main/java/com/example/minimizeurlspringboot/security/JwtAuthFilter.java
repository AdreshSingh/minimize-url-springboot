package com.example.minimizeurlspringboot.security;

import com.example.minimizeurlspringboot.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Purpose: JWT authentication filter for extracting and validating tokens from requests

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws IOException, jakarta.servlet.ServletException {

        System.out.println("JwtAuthFilter: doFilterInternal called for URL: " + request.getRequestURI());

        // ✅ Allow CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.info("Token received: {}", token);

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                logger.info("Token valid. Username: {}", username);

                userRepository.findByUsername(username).ifPresentOrElse(user -> {
                    logger.info("User found in DB: {}", user.getUsername());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user, // principal object for UserDetails
                            null,
                            Collections.emptyList());

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                    logger.info("Authentication set in SecurityContext");
                }, () -> {
                    logger.warn("User with username {} not found in database", username);
                });
            } else {
                logger.warn("Invalid token");
            }
        } else {
            logger.info("No Bearer token found in Authorization header. Header: {}", authHeader);
        }

        filterChain.doFilter(request, response);
    }
}
