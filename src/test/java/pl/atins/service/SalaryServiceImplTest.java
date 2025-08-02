package pl.atins.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.atins.core.AnnualRaiseStrategy;
import pl.atins.core.EmployeeEvent;
import pl.atins.core.EmployeeEventPublisher;
import pl.atins.core.PromotionBonusStrategy;
import pl.atins.core.SalaryAdjustmentStrategy;
import pl.atins.domain.Employee;
import pl.atins.repository.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeEventPublisher eventPublisher;

    @Mock
    private SalaryAdjustmentStrategy mockStrategy;

    private SalaryServiceImpl salaryService;
    private Employee employee;

    @BeforeEach
    void setUp() {
        salaryService = new SalaryServiceImpl(employeeRepository, eventPublisher);

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
    void shouldApplyAdjustmentWithStrategy() {
        var newSalary = new BigDecimal("55000");
        when(mockStrategy.adjustSalary(employee)).thenReturn(newSalary);

        var result = salaryService.applyAdjustment(employee, mockStrategy);

        assertEquals(newSalary, result);
        assertEquals(newSalary, employee.getSalary());
        verify(employeeRepository).save(employee);
        verify(eventPublisher).publishEvent(any(EmployeeEvent.class));
    }

    @Test
    void shouldPublishSalaryAdjustedEvent() {
        var oldSalary = employee.getSalary();
        var newSalary = new BigDecimal("55000");
        when(mockStrategy.adjustSalary(employee)).thenReturn(newSalary);

        salaryService.applyAdjustment(employee, mockStrategy);

        ArgumentCaptor<EmployeeEvent> eventCaptor = ArgumentCaptor.forClass(EmployeeEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        var event = eventCaptor.getValue();
        assertEquals(EmployeeEvent.EventType.SALARY_ADJUSTED, event.getEventType());
        assertEquals(employee, event.getEmployee());
        assertEquals(oldSalary, event.getOldValue());
        assertEquals(newSalary, event.getNewValue());
        assertTrue(event.getDetails().contains("Salary adjusted via strategy"));
    }

    @Test
    void shouldCreateAnnualRaiseStrategy() {
        var strategy = salaryService.createStrategy("annual_raise", new BigDecimal("0.05"));

        assertNotNull(strategy);
        assertInstanceOf(AnnualRaiseStrategy.class, strategy);
    }

    @Test
    void shouldCreatePromotionBonusStrategy() {
        var strategy = salaryService.createStrategy("promotion_bonus", new BigDecimal("5000"));

        assertNotNull(strategy);
        assertInstanceOf(PromotionBonusStrategy.class, strategy);
    }

    @Test
    void shouldThrowExceptionForUnknownStrategyType() {
        assertThrows(IllegalArgumentException.class,
                () -> salaryService.createStrategy("unknown_strategy", new BigDecimal("1000")));
    }

    @Test
    void shouldCreateStrategyCaseInsensitive() {
        var strategy1 = salaryService.createStrategy("ANNUAL_RAISE", new BigDecimal("0.05"));
        var strategy2 = salaryService.createStrategy("Annual_Raise", new BigDecimal("0.05"));
        var strategy3 = salaryService.createStrategy("promotion_BONUS", new BigDecimal("5000"));

        assertInstanceOf(AnnualRaiseStrategy.class, strategy1);
        assertInstanceOf(AnnualRaiseStrategy.class, strategy2);
        assertInstanceOf(PromotionBonusStrategy.class, strategy3);
    }

    @Test
    void shouldHandleZeroSalaryAdjustment() {
        var currentSalary = employee.getSalary();
        when(mockStrategy.adjustSalary(employee)).thenReturn(currentSalary);

        var result = salaryService.applyAdjustment(employee, mockStrategy);

        assertEquals(currentSalary, result);
        verify(eventPublisher).publishEvent(any(EmployeeEvent.class));
    }

    @Test
    void shouldHandleNegativeSalaryAdjustment() {
        var newSalary = new BigDecimal("45000");
        when(mockStrategy.adjustSalary(employee)).thenReturn(newSalary);

        var result = salaryService.applyAdjustment(employee, mockStrategy);

        assertEquals(newSalary, result);
        assertEquals(newSalary, employee.getSalary());
        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldSaveEmployeeAfterSalaryAdjustment() {
        var newSalary = new BigDecimal("60000");
        when(mockStrategy.adjustSalary(employee)).thenReturn(newSalary);

        salaryService.applyAdjustment(employee, mockStrategy);

        verify(employeeRepository).save(employee);
        assertEquals(newSalary, employee.getSalary());
    }

    @Test
    void shouldCallStrategyAdjustSalaryMethod() {
        var newSalary = new BigDecimal("55000");
        when(mockStrategy.adjustSalary(employee)).thenReturn(newSalary);

        salaryService.applyAdjustment(employee, mockStrategy);

        verify(mockStrategy).adjustSalary(employee);
    }

    @Test
    void shouldReturnNewSalaryFromStrategy() {
        var expectedSalary = new BigDecimal("62500");
        when(mockStrategy.adjustSalary(employee)).thenReturn(expectedSalary);

        var actualSalary = salaryService.applyAdjustment(employee, mockStrategy);

        assertEquals(expectedSalary, actualSalary);
    }

    @Test
    void shouldIncludeCorrectEventDetails() {
        var newSalary = new BigDecimal("55000");
        when(mockStrategy.adjustSalary(employee)).thenReturn(newSalary);

        salaryService.applyAdjustment(employee, mockStrategy);

        ArgumentCaptor<EmployeeEvent> eventCaptor = ArgumentCaptor.forClass(EmployeeEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        var event = eventCaptor.getValue();
        assertEquals("Salary adjusted via strategy", event.getDetails());
    }
}