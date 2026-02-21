package com.example.webapp.controller;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/roll/{roll}")
    public ResponseEntity<StudentDTO> getStudentByRoll(@PathVariable String roll) {
        return ResponseEntity.ok(studentService.getStudentByRoll(roll));
    }
}


