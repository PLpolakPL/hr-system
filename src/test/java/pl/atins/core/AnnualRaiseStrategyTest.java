package pl.atins.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.atins.domain.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnualRaiseStrategyTest {

    private Employee employee;
    private AnnualRaiseStrategy strategy;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@company.com");
        employee.setJobTitle("Developer");
        employee.setSalary(new BigDecimal("50000"));
        employee.setHireDate(LocalDate.now());
    }

    @Test
    void shouldCalculateAnnualRaiseCorrectly() {
        var raiseRate = new BigDecimal("0.05"); // 5%
        strategy = new AnnualRaiseStrategy(raiseRate);

        var newSalary = strategy.adjustSalary(employee);

        assertEquals(new BigDecimal("52500.00"), newSalary.setScale(2));
    }

    @Test
    void shouldCalculateZeroRaise() {
        var raiseRate = BigDecimal.ZERO;
        strategy = new AnnualRaiseStrategy(raiseRate);

        var newSalary = strategy.adjustSalary(employee);

        assertEquals(employee.getSalary(), newSalary);
    }

    @Test
    void shouldCalculateSmallRaise() {
        var raiseRate = new BigDecimal("0.01"); // 1%
        strategy = new AnnualRaiseStrategy(raiseRate);

        var newSalary = strategy.adjustSalary(employee);

        assertEquals(new BigDecimal("50500.00"), newSalary.setScale(2));
    }

    @Test
    void shouldCalculateLargeRaise() {
        var raiseRate = new BigDecimal("0.20"); // 20%
        strategy = new AnnualRaiseStrategy(raiseRate);

        var newSalary = strategy.adjustSalary(employee);

        assertEquals(new BigDecimal("60000.00"), newSalary.setScale(2));
    }

    @Test
    void shouldCalculateDecimalRaise() {
        var raiseRate = new BigDecimal("0.025"); // 2.5%
        strategy = new AnnualRaiseStrategy(raiseRate);

        var newSalary = strategy.adjustSalary(employee);

        assertEquals(new BigDecimal("51250.00"), newSalary.setScale(2));
    }

    @Test
    void shouldWorkWithDifferentSalaryAmounts() {
        employee.setSalary(new BigDecimal("100000"));
        var raiseRate = new BigDecimal("0.10"); // 10%
        strategy = new AnnualRaiseStrategy(raiseRate);

        var newSalary = strategy.adjustSalary(employee);

        assertEquals(new BigDecimal("110000.00"), newSalary.setScale(2));
    }

    @Test
    void shouldWorkWithVerySmallSalary() {
        employee.setSalary(new BigDecimal("1000"));
        var raiseRate = new BigDecimal("0.05"); // 5%
        strategy = new AnnualRaiseStrategy(raiseRate);

        var newSalary = strategy.adjustSalary(employee);

        assertEquals(new BigDecimal("1050.00"), newSalary.setScale(2));
    }

    @Test
    void shouldCalculateWithHighPrecision() {
        employee.setSalary(new BigDecimal("55555.55"));
        var raiseRate = new BigDecimal("0.0333"); // 3.33%
        strategy = new AnnualRaiseStrategy(raiseRate);

        var newSalary = strategy.adjustSalary(employee);

        // 55555.55 * 1.0333 = 57405.549815
        assertEquals(new BigDecimal("57405.549815"), newSalary);
    }

    @Test
    void shouldNotModifyOriginalEmployeeSalary() {
        var originalSalary = employee.getSalary();
        var raiseRate = new BigDecimal("0.05");
        strategy = new AnnualRaiseStrategy(raiseRate);

        strategy.adjustSalary(employee);

        assertEquals(originalSalary, employee.getSalary());
    }

    @Test
    void shouldHandleNullEmployee() {
        var raiseRate = new BigDecimal("0.05");
        strategy = new AnnualRaiseStrategy(raiseRate);

        assertThrows(NullPointerException.class, () -> strategy.adjustSalary(null));
    }

    @Test
    void shouldHandleEmployeeWithNullSalary() {
        employee.setSalary(null);
        var raiseRate = new BigDecimal("0.05");
        strategy = new AnnualRaiseStrategy(raiseRate);

        assertThrows(NullPointerException.class, () -> strategy.adjustSalary(employee));
    }

    @Test
    void shouldCreateStrategyWithDifferentRates() {
        var strategy1 = new AnnualRaiseStrategy(new BigDecimal("0.03"));
        var strategy2 = new AnnualRaiseStrategy(new BigDecimal("0.07"));

        var salary1 = strategy1.adjustSalary(employee);
        var salary2 = strategy2.adjustSalary(employee);

        assertTrue(salary2.compareTo(salary1) > 0);
        assertEquals(new BigDecimal("51500.00"), salary1.setScale(2));
        assertEquals(new BigDecimal("53500.00"), salary2.setScale(2));
    }
}