package com.example.webapp.service;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.entity.Student;
import com.example.webapp.exception.ResourceNotFoundException;
import com.example.webapp.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAllWithCourses().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return mapToDTO(student);
    }

    public StudentDTO getStudentByRoll(String roll) {
        Student student = studentRepository.findByRoll(roll)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "roll", roll));
        return mapToDTO(student);
    }

    private StudentDTO mapToDTO(Student student) {
        return StudentDTO.builder()
                .id(student.getId())
                .username(student.getUsername())
                .email(student.getEmail())
                .name(student.getName())
                .roll(student.getRoll())
                .program(student.getProgram())
                .semester(student.getSemester())
                .enrolledCourses(student.getEnrolledCourses() != null 
                        ? student.getEnrolledCourses().stream()
                                .map(course -> course.getTitle())
                                .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}

