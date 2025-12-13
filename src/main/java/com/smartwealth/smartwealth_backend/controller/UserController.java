package com.smartwealth.smartwealth_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/all")
    public List<String> getAllUsers() {
        // Placeholder implementation
        return new ArrayList<>(Collections.singleton("User"));
    }
}
