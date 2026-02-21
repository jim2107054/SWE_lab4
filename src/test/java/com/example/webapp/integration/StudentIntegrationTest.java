package com.example.webapp.integration;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.entity.Role;
import com.example.webapp.entity.Student;
import com.example.webapp.repository.StudentRepository;
import com.example.webapp.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudentIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        // Clean up before each test
    }

    private Student createTestStudent(String username, String roll) {
        Student student = new Student();
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

    @Test
    void testGetStudentById() {
        // Arrange
        Student student = createTestStudent("inttest1", "INT-001");
        Student savedStudent = studentRepository.save(student);

        // Act
        StudentDTO found = studentService.getStudentById(savedStudent.getId());

        // Assert
        assertNotNull(found);
        assertEquals("INT-001", found.getRoll());
        assertEquals("Test Student inttest1", found.getName());
    }

    @Test
    void testGetStudentByRoll() {
        // Arrange
        Student student = createTestStudent("inttest2", "INT-002");
        studentRepository.save(student);

        // Act
        StudentDTO found = studentService.getStudentByRoll("INT-002");

        // Assert
        assertNotNull(found);
        assertEquals("INT-002", found.getRoll());
    }

    @Test
    void testGetAllStudents() {
        // Arrange
        Student s1 = createTestStudent("inttest3", "INT-003");
        Student s2 = createTestStudent("inttest4", "INT-004");
        studentRepository.save(s1);
        studentRepository.save(s2);

        // Act
        List<StudentDTO> allStudents = studentService.getAllStudents();

        // Assert
        assertFalse(allStudents.isEmpty());
        assertTrue(allStudents.size() >= 2);
    }
}
