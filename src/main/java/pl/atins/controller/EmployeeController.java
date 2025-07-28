package pl.atins.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.atins.dto.CreateEmployeeRequest;
import pl.atins.dto.EmployeeResponse;
import pl.atins.dto.SalaryAdjustmentRequest;
import pl.atins.dto.UpdateEmployeeRequest;
import pl.atins.service.EmployeeService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        var response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        var response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<EmployeeResponse> getEmployeeByEmail(@PathVariable String email) {
        var response = employeeService.getEmployeeByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long supervisorId,
            @RequestParam(required = false) String name) {

        List<EmployeeResponse> employees;

        if (jobTitle != null) {
            employees = employeeService.getEmployeesByJobTitle(jobTitle);
        } else if (departmentId != null) {
            employees = employeeService.getEmployeesByDepartment(departmentId);
        } else if (supervisorId != null) {
            employees = employeeService.getEmployeesBySupervisor(supervisorId);
        } else if (name != null) {
            employees = employeeService.searchEmployeesByName(name);
        } else {
            employees = employeeService.getAllEmployees();
        }

        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        var response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/salary/adjust")
    public ResponseEntity<BigDecimal> adjustSalary(
            @PathVariable Long id,
            @Valid @RequestBody SalaryAdjustmentRequest request) {
        var newSalary = employeeService.adjustSalary(id, request);
        return ResponseEntity.ok(newSalary);
    }

    @PostMapping("/{id}/promote")
    public ResponseEntity<EmployeeResponse> promoteEmployee(
            @PathVariable Long id,
            @RequestParam String newJobTitle,
            @RequestParam(required = false) BigDecimal salaryAdjustment) {
        var response = employeeService.promoteEmployee(id, newJobTitle, salaryAdjustment);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/supervisor/{supervisorId}")
    public ResponseEntity<EmployeeResponse> assignSupervisor(
            @PathVariable Long id,
            @PathVariable Long supervisorId) {
        var response = employeeService.assignSupervisor(id, supervisorId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/departments/{departmentId}")
    public ResponseEntity<EmployeeResponse> assignToDepartment(
            @PathVariable Long id,
            @PathVariable Long departmentId) {
        var response = employeeService.assignToDepartment(id, departmentId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/departments/{departmentId}")
    public ResponseEntity<EmployeeResponse> removeFromDepartment(
            @PathVariable Long id,
            @PathVariable Long departmentId) {
        var response = employeeService.removeFromDepartment(id, departmentId);
        return ResponseEntity.ok(response);
    }
}