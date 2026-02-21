package com.example.webapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Student-Teacher Management System");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("description", "A Spring Boot REST API with JWT authentication and role-based access control");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("Register", "POST /api/auth/register");
        endpoints.put("Login", "POST /api/auth/login");
        endpoints.put("Students", "GET /api/students (requires auth)");
        endpoints.put("Teachers", "GET /api/teachers (requires auth)");
        endpoints.put("Courses", "GET /api/courses (requires auth)");
        endpoints.put("Departments", "GET /api/departments (requires auth)");
        response.put("endpoints", endpoints);
        
        Map<String, String> roles = new HashMap<>();
        roles.put("TEACHER", "Can create, update, delete courses and departments");
        roles.put("STUDENT", "Can view all resources (read-only access)");
        response.put("roles", roles);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/info")
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
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("message", "Application is running");
        return ResponseEntity.ok(health);
    }
}
