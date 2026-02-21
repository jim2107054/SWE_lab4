package com.example.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherDTO {
    
    private Long id;
    private String username;
    private String email;
    private String name;
    private String employeeId;
    private String designation;
    private String specialization;
    private Long departmentId;
    private String departmentName;
    private int courseCount;
}
