package pl.atins.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.atins.core.AnnualRaiseStrategy;
import pl.atins.core.EmployeeEvent;
import pl.atins.core.EmployeeEventPublisher;
import pl.atins.core.PromotionBonusStrategy;
import pl.atins.core.SalaryAdjustmentStrategy;
import pl.atins.domain.Employee;
import pl.atins.repository.EmployeeRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeEventPublisher eventPublisher;

    @Override
    public BigDecimal applyAdjustment(Employee employee, SalaryAdjustmentStrategy strategy) {
        var oldSalary = employee.getSalary();
        var newSalary = strategy.adjustSalary(employee);
        employee.setSalary(newSalary);
        employeeRepository.save(employee);

        var event = new EmployeeEvent(employee, EmployeeEvent.EventType.SALARY_ADJUSTED,
                "Salary adjusted via strategy", oldSalary, newSalary);
        eventPublisher.publishEvent(event);

        return newSalary;
    }

    @Override
    public SalaryAdjustmentStrategy createStrategy(String strategyType, BigDecimal amount) {
        return switch (strategyType.toLowerCase()) {
            case "annual_raise" -> new AnnualRaiseStrategy(amount);
            case "promotion_bonus" -> new PromotionBonusStrategy(amount);
            default -> throw new IllegalArgumentException("Unknown strategy type: " + strategyType);
        };
    }
}
