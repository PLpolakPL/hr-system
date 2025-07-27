package pl.atins.domain.core;

import pl.atins.domain.Employee;

import java.math.BigDecimal;

public interface SalaryAdjustmentStrategy {
    BigDecimal adjustSalary(Employee employee);
}
