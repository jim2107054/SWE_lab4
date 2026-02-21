package com.example.webapp.service;

import com.example.webapp.dto.DepartmentDTO;
import com.example.webapp.entity.Department;
import com.example.webapp.exception.DuplicateResourceException;
import com.example.webapp.exception.ResourceNotFoundException;
import com.example.webapp.repository.DepartmentRepository;
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
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;
    private DepartmentDTO departmentDTO;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Computer Science")
                .code("CS")
                .description("Department of Computer Science")
                .build();

        departmentDTO = DepartmentDTO.builder()
                .name("Computer Science")
                .code("CS")
                .description("Department of Computer Science")
                .build();
    }

    @Nested
    @DisplayName("getAllDepartments")
    class GetAllDepartments {

        @Test
        @DisplayName("should return all departments")
        void shouldReturnAllDepartments() {
            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department));

            List<DepartmentDTO> result = departmentService.getAllDepartments();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Computer Science");
        }

        @Test
        @DisplayName("should return empty list when no departments exist")
        void shouldReturnEmptyListWhenNoDepartmentsExist() {
            when(departmentRepository.findAll()).thenReturn(List.of());

            List<DepartmentDTO> result = departmentService.getAllDepartments();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getDepartmentById")
    class GetDepartmentById {

        @Test
        @DisplayName("should return department when found")
        void shouldReturnDepartmentWhenFound() {
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

            DepartmentDTO result = departmentService.getDepartmentById(1L);

            assertThat(result.getName()).isEqualTo("Computer Science");
            assertThat(result.getCode()).isEqualTo("CS");
        }

        @Test
        @DisplayName("should throw exception when department not found")
        void shouldThrowExceptionWhenDepartmentNotFound() {
            when(departmentRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.getDepartmentById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createDepartment")
    class CreateDepartment {

        @Test
        @DisplayName("should create department successfully")
        void shouldCreateDepartmentSuccessfully() {
            when(departmentRepository.existsByName(anyString())).thenReturn(false);
            when(departmentRepository.existsByCode(anyString())).thenReturn(false);
            when(departmentRepository.save(any(Department.class))).thenReturn(department);

            DepartmentDTO result = departmentService.createDepartment(departmentDTO);

            assertThat(result.getName()).isEqualTo("Computer Science");
            verify(departmentRepository, times(1)).save(any(Department.class));
        }

        @Test
        @DisplayName("should throw exception when department name exists")
        void shouldThrowExceptionWhenNameExists() {
            when(departmentRepository.existsByName("Computer Science")).thenReturn(true);

            assertThatThrownBy(() -> departmentService.createDepartment(departmentDTO))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("should throw exception when department code exists")
        void shouldThrowExceptionWhenCodeExists() {
            when(departmentRepository.existsByName(anyString())).thenReturn(false);
            when(departmentRepository.existsByCode("CS")).thenReturn(true);

            assertThatThrownBy(() -> departmentService.createDepartment(departmentDTO))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("updateDepartment")
    class UpdateDepartment {

        @Test
        @DisplayName("should update department successfully")
        void shouldUpdateDepartmentSuccessfully() {
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
            when(departmentRepository.save(any(Department.class))).thenReturn(department);

            DepartmentDTO result = departmentService.updateDepartment(1L, departmentDTO);

            assertThat(result).isNotNull();
            verify(departmentRepository, times(1)).save(any(Department.class));
        }

        @Test
        @DisplayName("should throw exception when department not found")
        void shouldThrowExceptionWhenDepartmentNotFound() {
            when(departmentRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.updateDepartment(999L, departmentDTO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteDepartment")
    class DeleteDepartment {

        @Test
        @DisplayName("should delete department successfully")
        void shouldDeleteDepartmentSuccessfully() {
            when(departmentRepository.existsById(1L)).thenReturn(true);
            doNothing().when(departmentRepository).deleteById(1L);

            departmentService.deleteDepartment(1L);

            verify(departmentRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("should throw exception when department not found")
        void shouldThrowExceptionWhenDepartmentNotFound() {
            when(departmentRepository.existsById(anyLong())).thenReturn(false);

            assertThatThrownBy(() -> departmentService.deleteDepartment(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
