package pl.atins.core;

import pl.atins.domain.Employee;

import java.math.BigDecimal;

public class AnnualRaiseStrategy implements SalaryAdjustmentStrategy {
    private final BigDecimal annualRate;

    public AnnualRaiseStrategy(BigDecimal annualRate) {
        this.annualRate = annualRate;
    }

    @Override
    public BigDecimal adjustSalary(Employee employee) {
        return employee.getSalary()
                .multiply(BigDecimal.ONE.add(annualRate));
    }
}