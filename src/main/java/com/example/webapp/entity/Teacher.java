package com.example.webapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Teacher extends User {

    @Column(unique = true)
    private String employeeId;

    private String designation;

    private String specialization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Course> courses = new ArrayList<>();

    @Builder
    public Teacher(Long id, String username, String password, String email, String name, 
                   Role role, boolean enabled, String employeeId, String designation, 
                   String specialization, Department department) {
        super(id, username, password, email, name, role, enabled);
        this.employeeId = employeeId;
        this.designation = designation;
        this.specialization = specialization;
        this.department = department;
    }
}
