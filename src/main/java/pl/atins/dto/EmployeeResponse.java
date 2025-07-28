package pl.atins.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class EmployeeResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String jobTitle;
    private LocalDate hireDate;
    private BigDecimal salary;
    private String phone;
    private String officeLocation;
    private Long supervisorId;
    private String supervisorName;
    private LocalDate supervisorSince;
    private Set<DepartmentResponse> departments;
    private Integer subordinatesCount;
}