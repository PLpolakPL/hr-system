package pl.atins.service;

import pl.atins.dto.CreateDepartmentRequest;
import pl.atins.dto.DepartmentResponse;
import pl.atins.dto.UpdateDepartmentRequest;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    DepartmentResponse getDepartmentById(Long id);

    DepartmentResponse getDepartmentByName(String name);

    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse getDepartmentByHeadId(Long headId);

    List<DepartmentResponse> searchDepartmentsByName(String name);

    List<DepartmentResponse> searchDepartmentsByLocation(String location);

    List<DepartmentResponse> getEmptyDepartments();

    List<DepartmentResponse> getDepartmentsWithMinEmployees(int minSize);

    DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request);

    void deleteDepartment(Long id);

    DepartmentResponse assignHead(Long departmentId, Long employeeId);

    DepartmentResponse removeHead(Long departmentId);
}