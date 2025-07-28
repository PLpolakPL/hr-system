package pl.atins.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateEmployeeRequest {

    private String firstName;

    private String lastName;

    @Email(message = "Email should be valid")
    private String email;

    private String jobTitle;

    private LocalDate hireDate;

    @Positive(message = "Salary must be positive")
    private BigDecimal salary;

    private String phone;

    private String officeLocation;

    private Long supervisorId;

    private Set<Long> departmentIds;
}