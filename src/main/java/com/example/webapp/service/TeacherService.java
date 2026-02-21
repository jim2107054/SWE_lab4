package com.example.webapp.service;

import com.example.webapp.dto.TeacherDTO;
import com.example.webapp.entity.Teacher;
import com.example.webapp.exception.ResourceNotFoundException;
import com.example.webapp.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAllWithDepartment().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TeacherDTO getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        return mapToDTO(teacher);
    }

    public TeacherDTO getTeacherByEmployeeId(String employeeId) {
        Teacher teacher = teacherRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "employeeId", employeeId));
        return mapToDTO(teacher);
    }

    public List<TeacherDTO> getTeachersByDepartment(Long departmentId) {
        return teacherRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TeacherDTO mapToDTO(Teacher teacher) {
        return TeacherDTO.builder()
                .id(teacher.getId())
                .username(teacher.getUsername())
                .email(teacher.getEmail())
                .name(teacher.getName())
                .employeeId(teacher.getEmployeeId())
                .designation(teacher.getDesignation())
                .specialization(teacher.getSpecialization())
                .departmentId(teacher.getDepartment() != null ? teacher.getDepartment().getId() : null)
                .departmentName(teacher.getDepartment() != null ? teacher.getDepartment().getName() : null)
                .courseCount(teacher.getCourses() != null ? teacher.getCourses().size() : 0)
                .build();
    }
}
