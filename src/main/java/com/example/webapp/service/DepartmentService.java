package com.example.webapp.service;

import com.example.webapp.dto.DepartmentDTO;
import com.example.webapp.entity.Department;
import com.example.webapp.exception.DuplicateResourceException;
import com.example.webapp.exception.ResourceNotFoundException;
import com.example.webapp.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return mapToDTO(department);
    }

    public DepartmentDTO getDepartmentByCode(String code) {
        Department department = departmentRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "code", code));
        return mapToDTO(department);
    }

    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) {
        if (departmentRepository.existsByName(departmentDTO.getName())) {
            throw new DuplicateResourceException("Department", "name", departmentDTO.getName());
        }
        if (departmentRepository.existsByCode(departmentDTO.getCode())) {
            throw new DuplicateResourceException("Department", "code", departmentDTO.getCode());
        }

        Department department = Department.builder()
                .name(departmentDTO.getName())
                .description(departmentDTO.getDescription())
                .code(departmentDTO.getCode())
                .build();

        Department savedDepartment = departmentRepository.save(department);
        return mapToDTO(savedDepartment);
    }

    @Transactional
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Check for duplicate name (excluding current department)
        if (!department.getName().equals(departmentDTO.getName()) 
                && departmentRepository.existsByName(departmentDTO.getName())) {
            throw new DuplicateResourceException("Department", "name", departmentDTO.getName());
        }
        
        // Check for duplicate code (excluding current department)
        if (!department.getCode().equals(departmentDTO.getCode()) 
                && departmentRepository.existsByCode(departmentDTO.getCode())) {
            throw new DuplicateResourceException("Department", "code", departmentDTO.getCode());
        }

        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department.setCode(departmentDTO.getCode());

        Department updatedDepartment = departmentRepository.save(department);
        return mapToDTO(updatedDepartment);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department", "id", id);
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentDTO mapToDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .code(department.getCode())
                .teacherCount(department.getTeachers() != null ? department.getTeachers().size() : 0)
                .courseCount(department.getCourses() != null ? department.getCourses().size() : 0)
                .build();
    }
}
