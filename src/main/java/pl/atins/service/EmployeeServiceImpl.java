package pl.atins.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.atins.core.EmployeeEvent;
import pl.atins.core.EmployeeEventPublisher;
import pl.atins.domain.Department;
import pl.atins.domain.Employee;
import pl.atins.dto.CreateEmployeeRequest;
import pl.atins.dto.DepartmentResponse;
import pl.atins.dto.EmployeeResponse;
import pl.atins.dto.SalaryAdjustmentRequest;
import pl.atins.dto.UpdateEmployeeRequest;
import pl.atins.repository.DepartmentRepository;
import pl.atins.repository.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final SalaryService salaryService;
    private final EmployeeEventPublisher eventPublisher;

    @Override
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        validateEmailUniqueness(request.getEmail(), null);

        var employee = new Employee();
        fillEmployeeDetails(request, employee);

        if (request.getSupervisorId() != null) {
            var supervisor = findEmployeeById(request.getSupervisorId());
            employee.addSupervisor(supervisor);
        }

        employee.setDepartments(new HashSet<>());
        assignDepartments(employee, request.getDepartmentIds());

        var response = saveAndMapToResponse(employee);

        var event = new EmployeeEvent(employee, EmployeeEvent.EventType.HIRED,
                "New employee hired", null, employee);
        eventPublisher.publishEvent(event);

        return response;
    }

    private static void fillEmployeeDetails(CreateEmployeeRequest request, Employee employee) {
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setJobTitle(request.getJobTitle());
        employee.setHireDate(request.getHireDate());
        employee.setSalary(request.getSalary());
        employee.setPhone(request.getPhone());
        employee.setOfficeLocation(request.getOfficeLocation());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        var employee = findEmployeeById(id);
        return mapToResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByEmail(String email) {
        var employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with email: " + email));
        return mapToResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return mapListToResponse(employeeRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByJobTitle(String jobTitle) {
        return mapListToResponse(employeeRepository.findByJobTitle(jobTitle));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByDepartment(Long departmentId) {
        return mapListToResponse(employeeRepository.findByDepartmentId(departmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesBySupervisor(Long supervisorId) {
        return mapListToResponse(employeeRepository.findBySupervisorId(supervisorId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> searchEmployeesByName(String name) {
        return mapListToResponse(employeeRepository.findByNameContaining(name));
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        var employee = findEmployeeById(id);

        updateEmployeeFields(employee, request);

        if (request.getSupervisorId() != null) {
            var supervisor = findEmployeeById(request.getSupervisorId());
            employee.addSupervisor(supervisor);
        }

        if (request.getDepartmentIds() != null) {
            employee.getDepartments().clear();
            assignDepartments(employee, request.getDepartmentIds());
        }

        return saveAndMapToResponse(employee);
    }

    @Override
    public void deleteEmployee(Long id) {
        var employee = findEmployeeById(id);
        validateEmployeeDeletion(employee);

        var event = new EmployeeEvent(employee, EmployeeEvent.EventType.TERMINATED,
                "Employee terminated", employee, null);
        eventPublisher.publishEvent(event);

        employeeRepository.delete(employee);
    }

    @Override
    public BigDecimal adjustSalary(Long employeeId, SalaryAdjustmentRequest request) {
        var employee = findEmployeeById(employeeId);
        var strategy = salaryService.createStrategy(request.getStrategyType(),
                request.getAmount());

        if (request.getJobTitle() != null) {
            employee.setJobTitle(request.getJobTitle());
        }

        return salaryService.applyAdjustment(employee, strategy);
    }

    @Override
    public EmployeeResponse promoteEmployee(Long employeeId, String newJobTitle, BigDecimal salaryAdjustment) {
        var employee = findEmployeeById(employeeId);
        var oldJobTitle = employee.getJobTitle();
        employee.setJobTitle(newJobTitle);

        if (salaryAdjustment != null && salaryAdjustment.compareTo(BigDecimal.ZERO) > 0) {
            var strategy = salaryService.createStrategy("promotion_bonus", salaryAdjustment);
            salaryService.applyAdjustment(employee, strategy);
        }

        var response = saveAndMapToResponse(employee);

        var event = new EmployeeEvent(employee, EmployeeEvent.EventType.PROMOTED,
                "Employee promoted", oldJobTitle, newJobTitle);
        eventPublisher.publishEvent(event);

        return response;
    }

    @Override
    public EmployeeResponse assignSupervisor(Long employeeId, Long supervisorId) {
        var employee = findEmployeeById(employeeId);
        var supervisor = findEmployeeById(supervisorId);
        var oldSupervisor = employee.getSupervisor();

        validateSupervisorAssignment(employee, supervisor);

        employee.addSupervisor(supervisor);
        employee.setSupervisorSince(LocalDate.now());

        var response = saveAndMapToResponse(employee);

        var event = new EmployeeEvent(employee, EmployeeEvent.EventType.SUPERVISOR_ASSIGNED,
                "Supervisor assigned", oldSupervisor, supervisor);
        eventPublisher.publishEvent(event);

        return response;
    }

    @Override
    public EmployeeResponse assignToDepartment(Long employeeId, Long departmentId) {
        var employee = findEmployeeById(employeeId);
        var department = findDepartmentById(departmentId);

        employee.addDepartment(department);

        var response = saveAndMapToResponse(employee);

        var event = new EmployeeEvent(employee, EmployeeEvent.EventType.DEPARTMENT_CHANGED,
                "Employee assigned to department", null, department.getName());
        eventPublisher.publishEvent(event);

        return response;
    }

    @Override
    public EmployeeResponse removeFromDepartment(Long employeeId, Long departmentId) {
        var employee = findEmployeeById(employeeId);
        var department = findDepartmentById(departmentId);

        employee.getDepartments().remove(department);
        department.getEmployees().remove(employee);

        return saveAndMapToResponse(employee);
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        var response = new EmployeeResponse();
        response.setId(employee.getId());
        fillEmployeeDetail(employee, response);
        response.setSupervisorSince(employee.getSupervisorSince());

        if (employee.getSupervisor() != null) {
            response.setSupervisorId(employee.getSupervisor().getId());
            response.setSupervisorName(getFullName(employee.getSupervisor()));
        }

        if (employee.getSubordinates() != null) {
            response.setSubordinatesCount(employee.getSubordinates().size());
        } else {
            response.setSubordinatesCount(0);
        }

        if (employee.getDepartments() != null) {
            Set<DepartmentResponse> departmentResponses = employee.getDepartments().stream()
                    .map(this::mapDepartmentToResponse)
                    .collect(Collectors.toSet());
            response.setDepartments(departmentResponses);
        }

        return response;
    }

    private static void fillEmployeeDetail(Employee employee, EmployeeResponse response) {
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setJobTitle(employee.getJobTitle());
        response.setHireDate(employee.getHireDate());
        response.setSalary(employee.getSalary());
        response.setPhone(employee.getPhone());
        response.setOfficeLocation(employee.getOfficeLocation());
    }

    private DepartmentResponse mapDepartmentToResponse(Department department) {
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

    private Employee findEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
    }

    private Department findDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
    }

    private void validateEmailUniqueness(String email, Long excludeEmployeeId) {
        var existingEmployee = employeeRepository.findByEmail(email);
        if (existingEmployee.isPresent() &&
                (excludeEmployeeId == null || !existingEmployee.get().getId().equals(excludeEmployeeId))) {
            throw new IllegalArgumentException("Employee with email " + email + " already exists");
        }
    }

    private void assignDepartments(Employee employee, Set<Long> departmentIds) {
        if (departmentIds != null && !departmentIds.isEmpty()) {
            for (Long departmentId : departmentIds) {
                var department = findDepartmentById(departmentId);
                employee.addDepartment(department);
            }
        }
    }

    private EmployeeResponse saveAndMapToResponse(Employee employee) {
        var savedEmployee = employeeRepository.save(employee);
        return mapToResponse(savedEmployee);
    }

    private List<EmployeeResponse> mapListToResponse(Collection<Employee> employees) {
        return employees.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private String getFullName(Employee employee) {
        return employee.getFirstName() + " " + employee.getLastName();
    }

    private void validateSupervisorAssignment(Employee employee, Employee supervisor) {
        if (employee.getId().equals(supervisor.getId())) {
            throw new IllegalArgumentException("Employee cannot be their own supervisor");
        }
    }

    private void validateEmployeeDeletion(Employee employee) {
        if (employee.getSubordinates() != null && !employee.getSubordinates().isEmpty()) {
            throw new IllegalStateException("Cannot delete employee with subordinates. Reassign subordinates first.");
        }
    }

    private void updateEmployeeFields(Employee employee, UpdateEmployeeRequest request) {
        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            validateEmailUniqueness(request.getEmail(), employee.getId());
            employee.setEmail(request.getEmail());
        }
        if (request.getJobTitle() != null) {
            employee.setJobTitle(request.getJobTitle());
        }
        if (request.getHireDate() != null) {
            employee.setHireDate(request.getHireDate());
        }
        if (request.getSalary() != null) {
            employee.setSalary(request.getSalary());
        }
        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }
        if (request.getOfficeLocation() != null) {
            employee.setOfficeLocation(request.getOfficeLocation());
        }
    }
}