package pl.atins.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.atins.domain.Department;
import pl.atins.domain.Employee;
import pl.atins.dto.CreateDepartmentRequest;
import pl.atins.dto.DepartmentResponse;
import pl.atins.dto.UpdateDepartmentRequest;
import pl.atins.repository.DepartmentRepository;
import pl.atins.repository.EmployeeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        validateDepartmentNameUniqueness(request.getName(), null);

        var department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setLocation(request.getLocation());

        if (request.getHeadId() != null) {
            var head = findEmployeeById(request.getHeadId());
            validateHeadAssignment(head);
            department.setHead(head);
        }

        return saveAndMapToResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        var department = findDepartmentById(id);
        return mapToResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentByName(String name) {
        var department = departmentRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with name: " + name));
        return mapToResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentByHeadId(Long headId) {
        var department = departmentRepository.findByHeadId(headId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with head id: " + headId));
        return mapToResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> searchDepartmentsByName(String name) {
        return departmentRepository.findByNameContaining(name).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> searchDepartmentsByLocation(String location) {
        return departmentRepository.findByLocationContaining(location).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getEmptyDepartments() {
        return departmentRepository.findEmptyDepartments().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartmentsWithMinEmployees(int minSize) {
        return departmentRepository.findDepartmentsWithMinEmployees(minSize).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        var department = findDepartmentById(id);

        if (request.getName() != null) {
            validateDepartmentNameUniqueness(request.getName(), id);
            department.setName(request.getName());
        }

        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }

        if (request.getLocation() != null) {
            department.setLocation(request.getLocation());
        }

        if (request.getHeadId() != null) {
            var head = findEmployeeById(request.getHeadId());
            validateHeadAssignment(head);
            department.setHead(head);
        }

        return saveAndMapToResponse(department);
    }

    @Override
    public void deleteDepartment(Long id) {
        var department = findDepartmentById(id);
        validateDepartmentDeletion(department);
        departmentRepository.delete(department);
    }

    @Override
    public DepartmentResponse assignHead(Long departmentId, Long employeeId) {
        var department = findDepartmentById(departmentId);
        var employee = findEmployeeById(employeeId);

        validateHeadAssignment(employee);
        department.setHead(employee);

        return saveAndMapToResponse(department);
    }

    @Override
    public DepartmentResponse removeHead(Long departmentId) {
        var department = findDepartmentById(departmentId);
        department.setHead(null);
        return saveAndMapToResponse(department);
    }

    private DepartmentResponse mapToResponse(Department department) {
        var response = new DepartmentResponse();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setDescription(department.getDescription());
        response.setLocation(department.getLocation());

        if (department.getHead() != null) {
            response.setHeadId(department.getHead().getId());
            response.setHeadName(getFullName(department.getHead()));
        }

        return response;
    }

    private Department findDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
    }

    private Employee findEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
    }

    private void validateDepartmentNameUniqueness(String name, Long excludeDepartmentId) {
        var existingDepartment = departmentRepository.findByName(name);
        if (existingDepartment.isPresent() &&
                (excludeDepartmentId == null || !existingDepartment.get().getId().equals(excludeDepartmentId))) {
            throw new IllegalArgumentException("Department with name '" + name + "' already exists");
        }
    }

    private void validateHeadAssignment(Employee employee) {
        var existingDepartment = departmentRepository.findByHeadId(employee.getId());
        if (existingDepartment.isPresent()) {
            throw new IllegalArgumentException(
                    "Employee is already head of department: " + existingDepartment.get().getName());
        }
    }

    private void validateDepartmentDeletion(Department department) {
        if (department.getEmployees() != null && !department.getEmployees().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete department with employees. Remove employees from department first.");
        }
    }

    private DepartmentResponse saveAndMapToResponse(Department department) {
        var savedDepartment = departmentRepository.save(department);
        return mapToResponse(savedDepartment);
    }

    private String getFullName(Employee employee) {
        return employee.getFirstName() + " " + employee.getLastName();
    }
}