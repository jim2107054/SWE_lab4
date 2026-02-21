package com.example.webapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student extends User {

    @Column(unique = true)
    private String roll;

    private String program;

    private Integer semester;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "student_course_enrollment",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> enrolledCourses = new HashSet<>();

    @Builder
    public Student(Long id, String username, String password, String email, String name,
                   Role role, boolean enabled, String roll, String program, Integer semester) {
        super(id, username, password, email, name, role, enabled);
        this.roll = roll;
        this.program = program;
        this.semester = semester;
    }
}
