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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAllWithDetails().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return mapToDTO(course);
    }

    public CourseDTO getCourseByCourseCode(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "courseCode", courseCode));
        return mapToDTO(course);
    }

    @Transactional
    public CourseDTO createCourse(CourseDTO courseDTO) {
        if (courseRepository.existsByCourseCode(courseDTO.getCourseCode())) {
            throw new DuplicateResourceException("Course", "courseCode", courseDTO.getCourseCode());
        }

        Course course = new Course();
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setCourseCode(courseDTO.getCourseCode());
        course.setCredits(courseDTO.getCredits());

        if (courseDTO.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(courseDTO.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", courseDTO.getTeacherId()));
            course.setTeacher(teacher);
        }

        if (courseDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(courseDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", courseDTO.getDepartmentId()));
            course.setDepartment(department);
        }

        Course savedCourse = courseRepository.save(course);
        return mapToDTO(savedCourse);
    }

    @Transactional
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Check for duplicate course code (excluding current course)
        if (!course.getCourseCode().equals(courseDTO.getCourseCode()) 
                && courseRepository.existsByCourseCode(courseDTO.getCourseCode())) {
            throw new DuplicateResourceException("Course", "courseCode", courseDTO.getCourseCode());
        }

        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setCourseCode(courseDTO.getCourseCode());
        course.setCredits(courseDTO.getCredits());

        if (courseDTO.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(courseDTO.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", courseDTO.getTeacherId()));
            course.setTeacher(teacher);
        } else {
            course.setTeacher(null);
        }

        if (courseDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(courseDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", courseDTO.getDepartmentId()));
            course.setDepartment(department);
        } else {
            course.setDepartment(null);
        }

        Course updatedCourse = courseRepository.save(course);
        return mapToDTO(updatedCourse);
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course", "id", id);
        }
        courseRepository.deleteById(id);
    }

    public List<CourseDTO> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<CourseDTO> getCoursesByDepartment(Long departmentId) {
        return courseRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CourseDTO mapToDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .courseCode(course.getCourseCode())
                .credits(course.getCredits())
                .teacherId(course.getTeacher() != null ? course.getTeacher().getId() : null)
                .teacherName(course.getTeacher() != null ? course.getTeacher().getName() : null)
                .departmentId(course.getDepartment() != null ? course.getDepartment().getId() : null)
                .departmentName(course.getDepartment() != null ? course.getDepartment().getName() : null)
                .build();
    }
}
