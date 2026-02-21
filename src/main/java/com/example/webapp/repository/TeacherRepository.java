package com.example.webapp.repository;

import com.example.webapp.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByEmployeeId(String employeeId);
    Optional<Teacher> findByUsername(String username);
    
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.department")
    List<Teacher> findAllWithDepartment();
    
    List<Teacher> findByDepartmentId(Long departmentId);
    
    boolean existsByEmployeeId(String employeeId);
}
