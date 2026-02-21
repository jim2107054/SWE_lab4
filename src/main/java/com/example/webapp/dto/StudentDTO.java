package com.example.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO {
    
    private Long id;
    private String username;
    private String email;
    private String name;
    private String roll;
    private String program;
    private Integer semester;
    private List<String> enrolledCourses;
}

