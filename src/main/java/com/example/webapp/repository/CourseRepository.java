package com.example.webapp.repository;

import com.example.webapp.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.teacher LEFT JOIN FETCH c.department")
    List<Course> findAllWithDetails();
    
    List<Course> findByTeacherId(Long teacherId);
    
    List<Course> findByDepartmentId(Long departmentId);
    
    boolean existsByCourseCode(String courseCode);
}
