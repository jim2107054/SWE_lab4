package com.example.webapp.integration;

import com.example.webapp.dto.*;
import com.example.webapp.entity.Role;
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
class CourseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String teacherToken;
    private static Long courseId;
    private static Long departmentId;

    @BeforeAll
    static void setUpTokens(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        // Register a teacher to get token
        RegisterRequest teacherRequest = RegisterRequest.builder()
                .username("course_test_teacher")
                .password("password123")
                .email("course_test_teacher@test.com")
                .name("Course Test Teacher")
                .role(Role.ROLE_TEACHER)
                .employeeId("EMP_COURSE_001")
                .designation("Associate Professor")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teacherRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        teacherToken = response.getToken();

        // Create a department for testing
        DepartmentDTO deptRequest = DepartmentDTO.builder()
                .name("Course Test Department")
                .code("CTD")
                .description("Department for course testing")
                .build();

        MvcResult deptResult = mockMvc.perform(post("/api/departments")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deptRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        DepartmentDTO deptResponse = objectMapper.readValue(
                deptResult.getResponse().getContentAsString(), DepartmentDTO.class);
        departmentId = deptResponse.getId();
    }

    @Test
    @Order(1)
    @DisplayName("Should create a new course")
    void shouldCreateCourse() throws Exception {
        CourseDTO request = CourseDTO.builder()
                .title("Advanced Programming")
                .description("Advanced programming concepts and techniques")
                .courseCode("AP201")
                .credits(4)
                .departmentId(departmentId)
                .build();

        MvcResult result = mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Advanced Programming"))
                .andExpect(jsonPath("$.courseCode").value("AP201"))
                .andExpect(jsonPath("$.credits").value(4))
                .andReturn();

        CourseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), CourseDTO.class);
        courseId = response.getId();
    }

    @Test
    @Order(2)
    @DisplayName("Should get all courses")
    void shouldGetAllCourses() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    @DisplayName("Should get course by ID")
    void shouldGetCourseById() throws Exception {
        mockMvc.perform(get("/api/courses/" + courseId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(courseId))
                .andExpect(jsonPath("$.title").value("Advanced Programming"));
    }

    @Test
    @Order(4)
    @DisplayName("Should get course by course code")
    void shouldGetCourseByCourseCode() throws Exception {
        mockMvc.perform(get("/api/courses/code/AP201")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseCode").value("AP201"));
    }

    @Test
    @Order(5)
    @DisplayName("Should update course")
    void shouldUpdateCourse() throws Exception {
        CourseDTO updateRequest = CourseDTO.builder()
                .title("Advanced Programming - Updated")
                .description("Updated description")
                .courseCode("AP201")
                .credits(5)
                .departmentId(departmentId)
                .build();

        mockMvc.perform(put("/api/courses/" + courseId)
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Advanced Programming - Updated"))
                .andExpect(jsonPath("$.credits").value(5));
    }

    @Test
    @Order(6)
    @DisplayName("Should fail to create course with duplicate code")
    void shouldFailToCreateCourseWithDuplicateCode() throws Exception {
        CourseDTO request = CourseDTO.builder()
                .title("Another Course")
                .description("Description")
                .courseCode("AP201") // Duplicate code
                .credits(3)
                .build();

        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(7)
    @DisplayName("Should return 404 for non-existent course")
    void shouldReturn404ForNonExistentCourse() throws Exception {
        mockMvc.perform(get("/api/courses/99999")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    @DisplayName("Should validate course creation request")
    void shouldValidateCourseCreationRequest() throws Exception {
        CourseDTO invalidRequest = CourseDTO.builder()
                .title("") // Empty title
                .courseCode("") // Empty code
                .credits(0) // Invalid credits
                .build();

        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @Order(9)
    @DisplayName("Should get courses by department")
    void shouldGetCoursesByDepartment() throws Exception {
        mockMvc.perform(get("/api/courses/department/" + departmentId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(10)
    @DisplayName("Should delete course")
    void shouldDeleteCourse() throws Exception {
        // First create a course to delete
        CourseDTO request = CourseDTO.builder()
                .title("Course to Delete")
                .description("This course will be deleted")
                .courseCode("DEL101")
                .credits(2)
                .build();

        MvcResult result = mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        CourseDTO createdCourse = objectMapper.readValue(
                result.getResponse().getContentAsString(), CourseDTO.class);

        // Now delete it
        mockMvc.perform(delete("/api/courses/" + createdCourse.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/api/courses/" + createdCourse.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isNotFound());
    }
}
