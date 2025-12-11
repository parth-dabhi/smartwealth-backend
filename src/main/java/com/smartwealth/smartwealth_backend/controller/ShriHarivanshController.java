package com.smartwealth.smartwealth_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/radha")
public class ShriHarivanshController {

    @GetMapping
    public List<String> radha() {
        return new ArrayList<>(List.of("Radha Radha - Shri Harivansh!"));
    }
}
