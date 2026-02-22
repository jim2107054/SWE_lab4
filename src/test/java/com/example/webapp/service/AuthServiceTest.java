package com.example.webapp.service;

import com.example.webapp.dto.AuthResponse;
import com.example.webapp.dto.LoginRequest;
import com.example.webapp.dto.RegisterRequest;
import com.example.webapp.entity.Role;
import com.example.webapp.entity.Student;
import com.example.webapp.entity.Teacher;
import com.example.webapp.exception.BadRequestException;
import com.example.webapp.exception.DuplicateResourceException;
import com.example.webapp.repository.DepartmentRepository;
import com.example.webapp.repository.StudentRepository;
import com.example.webapp.repository.TeacherRepository;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest studentRegisterRequest;
    private RegisterRequest teacherRegisterRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        studentRegisterRequest = RegisterRequest.builder()
                .username("student1")
                .password("password123")
                .email("student1@test.com")
                .name("Test Student")
                .role(Role.ROLE_STUDENT)
                .roll("STU001")
                .program("BSc Computer Science")
                .semester(3)
                .build();

        teacherRegisterRequest = RegisterRequest.builder()
                .username("teacher1")
                .password("password123")
                .email("teacher1@test.com")
                .name("Test Teacher")
                .role(Role.ROLE_TEACHER)
                .employeeId("EMP001")
                .designation("Professor")
                .specialization("Machine Learning")
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("should register student successfully")
        void shouldRegisterStudentSuccessfully() {
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(studentRepository.existsByRoll(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            
            Student savedStudent = new Student();
            savedStudent.setId(1L);
            savedStudent.setUsername("student1");
            savedStudent.setEmail("student1@test.com");
            savedStudent.setName("Test Student");
            savedStudent.setRole(Role.ROLE_STUDENT);
            
            when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);
            
            Authentication auth = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
            when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");

            AuthResponse response = authService.register(studentRegisterRequest);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getRole()).isEqualTo(Role.ROLE_STUDENT);
            verify(studentRepository, times(1)).save(any(Student.class));
        }

        @Test
        @DisplayName("should register teacher successfully")
        void shouldRegisterTeacherSuccessfully() {
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(teacherRepository.existsByEmployeeId(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            
            Teacher savedTeacher = new Teacher();
            savedTeacher.setId(1L);
            savedTeacher.setUsername("teacher1");
            savedTeacher.setEmail("teacher1@test.com");
            savedTeacher.setName("Test Teacher");
            savedTeacher.setRole(Role.ROLE_TEACHER);
            
            when(teacherRepository.save(any(Teacher.class))).thenReturn(savedTeacher);
            
            Authentication auth = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
            when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");

            AuthResponse response = authService.register(teacherRegisterRequest);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getRole()).isEqualTo(Role.ROLE_TEACHER);
            verify(teacherRepository, times(1)).save(any(Teacher.class));
        }

        @Test
        @DisplayName("should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameExists() {
            when(userRepository.existsByUsername("student1")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(studentRegisterRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("username");
        }

        @Test
        @DisplayName("should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail("student1@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(studentRegisterRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("should throw exception when role is invalid")
        void shouldThrowExceptionWhenRoleIsInvalid() {
            RegisterRequest invalidRequest = RegisterRequest.builder()
                    .username("test")
                    .password("password")
                    .email("test@test.com")
                    .name("Test")
                    .role(null)
                    .build();

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);

            assertThatThrownBy(() -> authService.register(invalidRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid role");
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should login successfully")
        void shouldLoginSuccessfully() {
            Student user = new Student();
            user.setId(1L);
            user.setUsername("testuser");
            user.setEmail("test@test.com");
            user.setName("Test User");
            user.setRole(Role.ROLE_STUDENT);

            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(user);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
            when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");

            AuthResponse response = authService.login(loginRequest);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getUsername()).isEqualTo("testuser");
        }
    }
}
