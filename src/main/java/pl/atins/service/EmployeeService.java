package pl.atins.service;

import pl.atins.dto.CreateEmployeeRequest;
import pl.atins.dto.EmployeeResponse;
import pl.atins.dto.SalaryAdjustmentRequest;
import pl.atins.dto.UpdateEmployeeRequest;

import java.math.BigDecimal;
import java.util.List;

public interface EmployeeService {

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    EmployeeResponse getEmployeeById(Long id);

    EmployeeResponse getEmployeeByEmail(String email);

    List<EmployeeResponse> getAllEmployees();

    List<EmployeeResponse> getEmployeesByJobTitle(String jobTitle);

    List<EmployeeResponse> getEmployeesByDepartment(Long departmentId);

    List<EmployeeResponse> getEmployeesBySupervisor(Long supervisorId);

    List<EmployeeResponse> searchEmployeesByName(String name);

    EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    void deleteEmployee(Long id);

    BigDecimal adjustSalary(Long employeeId, SalaryAdjustmentRequest request);

    EmployeeResponse promoteEmployee(Long employeeId, String newJobTitle, BigDecimal salaryAdjustment);

    EmployeeResponse assignSupervisor(Long employeeId, Long supervisorId);

    EmployeeResponse assignToDepartment(Long employeeId, Long departmentId);

    EmployeeResponse removeFromDepartment(Long employeeId, Long departmentId);
}