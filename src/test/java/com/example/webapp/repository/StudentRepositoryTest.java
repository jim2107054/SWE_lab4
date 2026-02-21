package com.example.webapp.repository;

import com.example.webapp.entity.Role;
import com.example.webapp.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    private Student createTestStudent(String username, String roll) {
        Student student = new Student();
        student.setUsername(username);
        student.setPassword("password123");
        student.setEmail(username + "@test.com");
        student.setName("Test Student");
        student.setRole(Role.ROLE_STUDENT);
        student.setEnabled(true);
        student.setRoll(roll);
        student.setProgram("BSc Computer Science");
        student.setSemester(4);
        return student;
    }

    @Test
    void testFindById() {
        Student student = createTestStudent("teststudent1", "S001");
        Student savedStudent = studentRepository.save(student);

        Optional<Student> found = studentRepository.findById(savedStudent.getId());
        
        assertTrue(found.isPresent());
        assertEquals(savedStudent.getId(), found.get().getId());
        assertEquals("S001", found.get().getRoll());
    }

    @Test
    void testFindByRoll() {
        Student student = createTestStudent("teststudent2", "S002");
        studentRepository.save(student);

        Optional<Student> found = studentRepository.findByRoll("S002");
        
        assertTrue(found.isPresent());
        assertEquals("S002", found.get().getRoll());
    }

    @Test
    void testFindByUsername() {
        Student student = createTestStudent("uniquestudent", "S003");
        studentRepository.save(student);

        Optional<Student> found = studentRepository.findByUsername("uniquestudent");
        
        assertTrue(found.isPresent());
        assertEquals("uniquestudent", found.get().getUsername());
    }

    @Test
    void testExistsByRoll() {
        Student student = createTestStudent("teststudent4", "S004");
        studentRepository.save(student);

        assertTrue(studentRepository.existsByRoll("S004"));
        assertFalse(studentRepository.existsByRoll("NONEXISTENT"));
    }

    @Test
    void testFindAll() {
        Student student1 = createTestStudent("findallstudent1", "FA001");
        Student student2 = createTestStudent("findallstudent2", "FA002");
        
        studentRepository.save(student1);
        studentRepository.save(student2);

        List<Student> students = studentRepository.findAll();
        
        assertFalse(students.isEmpty());
        assertTrue(students.size() >= 2);
    }
}
