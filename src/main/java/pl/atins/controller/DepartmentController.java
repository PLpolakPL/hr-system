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
import pl.atins.dto.CreateDepartmentRequest;
import pl.atins.dto.DepartmentResponse;
import pl.atins.dto.UpdateDepartmentRequest;
import pl.atins.service.DepartmentService;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        var response = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        var response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<DepartmentResponse> getDepartmentByName(@PathVariable String name) {
        var response = departmentService.getDepartmentByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/head/{headId}")
    public ResponseEntity<DepartmentResponse> getDepartmentByHeadId(@PathVariable Long headId) {
        var response = departmentService.getDepartmentByHeadId(headId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean empty,
            @RequestParam(required = false) Integer minEmployees) {

        List<DepartmentResponse> departments;

        if (name != null) {
            departments = departmentService.searchDepartmentsByName(name);
        } else if (location != null) {
            departments = departmentService.searchDepartmentsByLocation(location);
        } else if (empty != null && empty) {
            departments = departmentService.getEmptyDepartments();
        } else if (minEmployees != null) {
            departments = departmentService.getDepartmentsWithMinEmployees(minEmployees);
        } else {
            departments = departmentService.getAllDepartments();
        }

        return ResponseEntity.ok(departments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        var response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/head/{employeeId}")
    public ResponseEntity<DepartmentResponse> assignHead(
            @PathVariable Long id,
            @PathVariable Long employeeId) {
        var response = departmentService.assignHead(id, employeeId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/head")
    public ResponseEntity<DepartmentResponse> removeHead(@PathVariable Long id) {
        var response = departmentService.removeHead(id);
        return ResponseEntity.ok(response);
    }
}