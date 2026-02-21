package com.example.webapp.repository;

import com.example.webapp.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRoll(String roll);
    Optional<Student> findByUsername(String username);
    
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.enrolledCourses")
    List<Student> findAllWithCourses();
    
    boolean existsByRoll(String roll);
}

