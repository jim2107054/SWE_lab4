package com.example.webapp.controller;

import com.example.webapp.dto.TeacherDTO;
import com.example.webapp.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public ResponseEntity<List<TeacherDTO>> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherDTO> getTeacherById(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getTeacherById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<TeacherDTO> getTeacherByEmployeeId(@PathVariable String employeeId) {
        return ResponseEntity.ok(teacherService.getTeacherByEmployeeId(employeeId));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<TeacherDTO>> getTeachersByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(teacherService.getTeachersByDepartment(departmentId));
    }
}
