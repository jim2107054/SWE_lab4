package com.example.webapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @GetMapping("/api/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Student-Teacher Management API");
        info.put("version", "1.0.0");
        info.put("documentation", "Use the endpoints below with JWT authentication");
        
        Map<String, Object> auth = new HashMap<>();
        auth.put("type", "JWT Bearer Token");
        auth.put("header", "Authorization: Bearer <token>");
        auth.put("register", "POST /api/auth/register");
        auth.put("login", "POST /api/auth/login");
        info.put("authentication", auth);
        
        return ResponseEntity.ok(info);
    }

    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("message", "Application is running");
        return ResponseEntity.ok(health);
    }
}
