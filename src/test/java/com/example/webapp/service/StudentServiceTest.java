package com.example.webapp.service;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.entity.Role;
import com.example.webapp.entity.Student;
import com.example.webapp.exception.ResourceNotFoundException;
import com.example.webapp.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Unit Tests")
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student createTestStudent(Long id, String username, String roll) {
        Student student = new Student();
        student.setId(id);
        student.setUsername(username);
        student.setPassword("password123");
        student.setEmail(username + "@test.com");
        student.setName("Test Student " + username);
        student.setRole(Role.ROLE_STUDENT);
        student.setEnabled(true);
        student.setRoll(roll);
        student.setProgram("BSc Computer Science");
        student.setSemester(4);
        return student;
    }

    @Nested
    @DisplayName("getAllStudents")
    class GetAllStudentsTests {
        @Test
        @DisplayName("should return all students as DTOs")
        void shouldReturnAllStudents() {
            Student s1 = createTestStudent(1L, "student1", "STU-001");
            Student s2 = createTestStudent(2L, "student2", "STU-002");

            when(studentRepository.findAll()).thenReturn(List.of(s1, s2));

            List<StudentDTO> students = studentService.getAllStudents();

            assertEquals(2, students.size());
            assertEquals("STU-001", students.get(0).getRoll());
            assertEquals("STU-002", students.get(1).getRoll());
            verify(studentRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getStudentById")
    class GetStudentByIdTests {
        @Test
        @DisplayName("should return student when found")
        void shouldReturnStudentWhenFound() {
            Student student = createTestStudent(1L, "student1", "STU-001");
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            StudentDTO result = studentService.getStudentById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("STU-001", result.getRoll());
        }

        @Test
        @DisplayName("should throw exception when not found")
        void shouldThrowExceptionWhenNotFound() {
            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                studentService.getStudentById(99L);
            });
        }
    }

    @Nested
    @DisplayName("getStudentByRoll")
    class GetStudentByRollTests {
        @Test
        @DisplayName("should return student when found by roll")
        void shouldReturnStudentByRoll() {
            Student student = createTestStudent(1L, "student1", "STU-001");
            when(studentRepository.findByRoll("STU-001")).thenReturn(Optional.of(student));

            StudentDTO result = studentService.getStudentByRoll("STU-001");

            assertNotNull(result);
            assertEquals("STU-001", result.getRoll());
        }

        @Test
        @DisplayName("should throw exception when roll not found")
        void shouldThrowExceptionWhenRollNotFound() {
            when(studentRepository.findByRoll("INVALID")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                studentService.getStudentByRoll("INVALID");
            });
        }
    }
}

