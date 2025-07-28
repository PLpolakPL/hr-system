package pl.atins.service;

import pl.atins.core.SalaryAdjustmentStrategy;
import pl.atins.domain.Employee;

import java.math.BigDecimal;

public interface SalaryService {

    BigDecimal applyAdjustment(Employee employee, SalaryAdjustmentStrategy strategy);

}
