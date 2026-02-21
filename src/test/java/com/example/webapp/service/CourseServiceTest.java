package com.example.webapp.service;

import com.example.webapp.dto.CourseDTO;
import com.example.webapp.entity.Course;
import com.example.webapp.entity.Department;
import com.example.webapp.entity.Teacher;
import com.example.webapp.exception.DuplicateResourceException;
import com.example.webapp.exception.ResourceNotFoundException;
import com.example.webapp.repository.CourseRepository;
import com.example.webapp.repository.DepartmentRepository;
import com.example.webapp.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private CourseService courseService;

    private Course course;
    private CourseDTO courseDTO;
    private Teacher teacher;
    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Computer Science")
                .code("CS")
                .description("CS Department")
                .build();

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setName("Dr. Smith");
        teacher.setDepartment(department);

        course = Course.builder()
                .id(1L)
                .title("Data Structures")
                .description("Introduction to Data Structures")
                .courseCode("CS101")
                .credits(3)
                .teacher(teacher)
                .department(department)
                .build();

        courseDTO = CourseDTO.builder()
                .title("Data Structures")
                .description("Introduction to Data Structures")
                .courseCode("CS101")
                .credits(3)
                .teacherId(1L)
                .departmentId(1L)
                .build();
    }

    @Nested
    @DisplayName("getAllCourses")
    class GetAllCourses {

        @Test
        @DisplayName("should return all courses")
        void shouldReturnAllCourses() {
            when(courseRepository.findAllWithDetails()).thenReturn(Arrays.asList(course));

            List<CourseDTO> result = courseService.getAllCourses();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Data Structures");
            assertThat(result.get(0).getCourseCode()).isEqualTo("CS101");
            verify(courseRepository, times(1)).findAllWithDetails();
        }

        @Test
        @DisplayName("should return empty list when no courses exist")
        void shouldReturnEmptyListWhenNoCoursesExist() {
            when(courseRepository.findAllWithDetails()).thenReturn(List.of());

            List<CourseDTO> result = courseService.getAllCourses();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCourseById")
    class GetCourseById {

        @Test
        @DisplayName("should return course when found")
        void shouldReturnCourseWhenFound() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            CourseDTO result = courseService.getCourseById(1L);

            assertThat(result.getTitle()).isEqualTo("Data Structures");
            assertThat(result.getCourseCode()).isEqualTo("CS101");
        }

        @Test
        @DisplayName("should throw exception when course not found")
        void shouldThrowExceptionWhenCourseNotFound() {
            when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.getCourseById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Course");
        }
    }

    @Nested
    @DisplayName("createCourse")
    class CreateCourse {

        @Test
        @DisplayName("should create course successfully")
        void shouldCreateCourseSuccessfully() {
            when(courseRepository.existsByCourseCode(anyString())).thenReturn(false);
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
            when(courseRepository.save(any(Course.class))).thenReturn(course);

            CourseDTO result = courseService.createCourse(courseDTO);

            assertThat(result.getTitle()).isEqualTo("Data Structures");
            verify(courseRepository, times(1)).save(any(Course.class));
        }

        @Test
        @DisplayName("should throw exception when course code already exists")
        void shouldThrowExceptionWhenCourseCodeExists() {
            when(courseRepository.existsByCourseCode("CS101")).thenReturn(true);

            assertThatThrownBy(() -> courseService.createCourse(courseDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("courseCode");
        }

        @Test
        @DisplayName("should throw exception when teacher not found")
        void shouldThrowExceptionWhenTeacherNotFound() {
            when(courseRepository.existsByCourseCode(anyString())).thenReturn(false);
            when(teacherRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.createCourse(courseDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Teacher");
        }
    }

    @Nested
    @DisplayName("updateCourse")
    class UpdateCourse {

        @Test
        @DisplayName("should update course successfully")
        void shouldUpdateCourseSuccessfully() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
            when(courseRepository.save(any(Course.class))).thenReturn(course);

            CourseDTO updatedDTO = CourseDTO.builder()
                    .title("Advanced Data Structures")
                    .description("Advanced topics")
                    .courseCode("CS101")
                    .credits(4)
                    .teacherId(1L)
                    .departmentId(1L)
                    .build();

            CourseDTO result = courseService.updateCourse(1L, updatedDTO);

            assertThat(result).isNotNull();
            verify(courseRepository, times(1)).save(any(Course.class));
        }

        @Test
        @DisplayName("should throw exception when course not found")
        void shouldThrowExceptionWhenCourseNotFound() {
            when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.updateCourse(999L, courseDTO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteCourse")
    class DeleteCourse {

        @Test
        @DisplayName("should delete course successfully")
        void shouldDeleteCourseSuccessfully() {
            when(courseRepository.existsById(1L)).thenReturn(true);
            doNothing().when(courseRepository).deleteById(1L);

            courseService.deleteCourse(1L);

            verify(courseRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("should throw exception when course not found")
        void shouldThrowExceptionWhenCourseNotFound() {
            when(courseRepository.existsById(anyLong())).thenReturn(false);

            assertThatThrownBy(() -> courseService.deleteCourse(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
