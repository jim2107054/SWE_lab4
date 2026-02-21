package com.example.webapp.service;

import com.example.webapp.dto.AuthResponse;
import com.example.webapp.dto.LoginRequest;
import com.example.webapp.dto.RegisterRequest;
import com.example.webapp.entity.*;
import com.example.webapp.exception.BadRequestException;
import com.example.webapp.exception.DuplicateResourceException;
import com.example.webapp.exception.ResourceNotFoundException;
import com.example.webapp.repository.*;
import com.example.webapp.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate that username and email are unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user;
        
        if (request.getRole() == Role.ROLE_STUDENT) {
            user = createStudent(request);
        } else if (request.getRole() == Role.ROLE_TEACHER) {
            user = createTeacher(request);
        } else {
            throw new BadRequestException("Invalid role specified");
        }

        // Generate token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .message("Registration successful")
                .build();
    }

    private Student createStudent(RegisterRequest request) {
        if (request.getRoll() != null && studentRepository.existsByRoll(request.getRoll())) {
            throw new DuplicateResourceException("Student", "roll", request.getRoll());
        }

        Student student = new Student();
        student.setUsername(request.getUsername());
        student.setPassword(passwordEncoder.encode(request.getPassword()));
        student.setEmail(request.getEmail());
        student.setName(request.getName());
        student.setRole(Role.ROLE_STUDENT);
        student.setEnabled(true);
        student.setRoll(request.getRoll());
        student.setProgram(request.getProgram());
        student.setSemester(request.getSemester());

        return studentRepository.save(student);
    }

    private Teacher createTeacher(RegisterRequest request) {
        if (request.getEmployeeId() != null && teacherRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Teacher", "employeeId", request.getEmployeeId());
        }

        Teacher teacher = new Teacher();
        teacher.setUsername(request.getUsername());
        teacher.setPassword(passwordEncoder.encode(request.getPassword()));
        teacher.setEmail(request.getEmail());
        teacher.setName(request.getName());
        teacher.setRole(Role.ROLE_TEACHER);
        teacher.setEnabled(true);
        teacher.setEmployeeId(request.getEmployeeId());
        teacher.setDesignation(request.getDesignation());
        teacher.setSpecialization(request.getSpecialization());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            teacher.setDepartment(department);
        }

        return teacherRepository.save(teacher);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .message("Login successful")
                .build();
    }
}
