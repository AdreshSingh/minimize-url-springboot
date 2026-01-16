package com.example.minimizeurlspringboot.service;

import org.springframework.stereotype.Service;

import com.example.minimizeurlspringboot.models.User;
import com.example.minimizeurlspringboot.repository.UserRepository;

@Service
public class UserService {
    UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
