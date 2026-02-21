package com.example.webapp.integration;

import com.example.webapp.dto.*;
import com.example.webapp.entity.Role;
import com.example.webapp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    private static String teacherToken;
    private static String studentToken;
    private static Long departmentId;

    @BeforeEach
    void setUp() {
        // Clean up before each test
    }

    @AfterAll
    static void cleanUp() {
        // Reset static variables
        teacherToken = null;
        studentToken = null;
        departmentId = null;
    }

    @Test
    @Order(1)
    @DisplayName("Should register a teacher successfully")
    void shouldRegisterTeacher() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("teacher_integration")
                .password("password123")
                .email("teacher_integration@test.com")
                .name("Integration Teacher")
                .role(Role.ROLE_TEACHER)
                .employeeId("EMP_INT_001")
                .designation("Professor")
                .specialization("Software Engineering")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("teacher_integration"))
                .andExpect(jsonPath("$.role").value("ROLE_TEACHER"))
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        teacherToken = response.getToken();
    }

    @Test
    @Order(2)
    @DisplayName("Should register a student successfully")
    void shouldRegisterStudent() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("student_integration")
                .password("password123")
                .email("student_integration@test.com")
                .name("Integration Student")
                .role(Role.ROLE_STUDENT)
                .roll("STU_INT_001")
                .program("BSc Computer Science")
                .semester(4)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("student_integration"))
                .andExpect(jsonPath("$.role").value("ROLE_STUDENT"))
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        studentToken = response.getToken();
    }

    @Test
    @Order(3)
    @DisplayName("Should fail to register with duplicate username")
    void shouldFailToRegisterWithDuplicateUsername() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("teacher_integration")
                .password("password123")
                .email("different@test.com")
                .name("Another Teacher")
                .role(Role.ROLE_TEACHER)
                .employeeId("EMP_DIFF")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(4)
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("teacher_integration")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @Order(5)
    @DisplayName("Should fail login with wrong password")
    void shouldFailLoginWithWrongPassword() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("teacher_integration")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    @DisplayName("Teacher should create department successfully")
    void teacherShouldCreateDepartment() throws Exception {
        DepartmentDTO request = DepartmentDTO.builder()
                .name("Integration Test Department")
                .code("ITD")
                .description("Department for integration testing")
                .build();

        MvcResult result = mockMvc.perform(post("/api/departments")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test Department"))
                .andReturn();

        DepartmentDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), DepartmentDTO.class);
        departmentId = response.getId();
    }

    @Test
    @Order(7)
    @DisplayName("Student should NOT be able to create department")
    void studentShouldNotCreateDepartment() throws Exception {
        DepartmentDTO request = DepartmentDTO.builder()
                .name("Student Department")
                .code("SD")
                .description("This should fail")
                .build();

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    @DisplayName("Teacher should create course successfully")
    void teacherShouldCreateCourse() throws Exception {
        CourseDTO request = CourseDTO.builder()
                .title("Integration Testing Course")
                .description("A course about integration testing")
                .courseCode("ITC101")
                .credits(3)
                .departmentId(departmentId)
                .build();

        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Testing Course"))
                .andExpect(jsonPath("$.courseCode").value("ITC101"));
    }

    @Test
    @Order(9)
    @DisplayName("Student should NOT be able to create course")
    void studentShouldNotCreateCourse() throws Exception {
        CourseDTO request = CourseDTO.builder()
                .title("Student Course")
                .description("This should fail")
                .courseCode("SC101")
                .credits(3)
                .build();

        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(10)
    @DisplayName("Both teacher and student should view courses")
    void bothShouldViewCourses() throws Exception {
        // Teacher can view
        mockMvc.perform(get("/api/courses")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // Student can view
        mockMvc.perform(get("/api/courses")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    @DisplayName("Both teacher and student should view departments")
    void bothShouldViewDepartments() throws Exception {
        // Teacher can view
        mockMvc.perform(get("/api/departments")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // Student can view
        mockMvc.perform(get("/api/departments")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(12)
    @DisplayName("Unauthenticated user should NOT access protected resources")
    void unauthenticatedShouldNotAccessProtectedResources() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(13)
    @DisplayName("Student should NOT be able to delete course")
    void studentShouldNotDeleteCourse() throws Exception {
        mockMvc.perform(delete("/api/courses/1")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(14)
    @DisplayName("Invalid token should be rejected")
    void invalidTokenShouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .header("Authorization", "Bearer invalid_token_here"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(15)
    @DisplayName("Should validate registration request")
    void shouldValidateRegistrationRequest() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("ab") // Too short
                .password("123") // Too short
                .email("invalid-email") // Invalid format
                .name("")
                .role(Role.ROLE_STUDENT)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }
}
