package pl.atins.domain.core;

import pl.atins.domain.Employee;

import java.math.BigDecimal;

public class PromotionBonusStrategy implements SalaryAdjustmentStrategy {
    private final BigDecimal bonusAmount;

    public PromotionBonusStrategy(BigDecimal bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    @Override
    public BigDecimal adjustSalary(Employee employee) {
        return employee.getSalary().add(bonusAmount);
    }
}
