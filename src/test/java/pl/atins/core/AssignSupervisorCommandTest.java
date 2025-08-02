package pl.atins.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.atins.domain.Employee;
import pl.atins.repository.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignSupervisorCommandTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeEventPublisher eventPublisher;

    private Employee employee;
    private Employee supervisor;
    private AssignSupervisorCommand command;

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

        supervisor = new Employee();
        supervisor.setId(2L);
        supervisor.setFirstName("Jane");
        supervisor.setLastName("Smith");
        supervisor.setEmail("jane.smith@company.com");
        supervisor.setJobTitle("Team Lead");
        supervisor.setSalary(new BigDecimal("70000"));
        supervisor.setHireDate(LocalDate.now().minusYears(2));
    }

    @Test
    void shouldExecuteSupervisorAssignment() {
        command = new AssignSupervisorCommand(employee, supervisor, employeeRepository, eventPublisher);

        assertFalse(command.canUndo());

        command.execute();

        assertEquals(supervisor, employee.getSupervisor());
        assertNotNull(employee.getSupervisorSince());
        assertTrue(command.canUndo());
        verify(employeeRepository).save(employee);
        verify(eventPublisher).publishEvent(any(EmployeeEvent.class));
    }

    @Test
    void shouldUndoSupervisorAssignment() {
        var originalSupervisor = new Employee();
        originalSupervisor.setId(3L);
        originalSupervisor.setFirstName("Previous");
        originalSupervisor.setLastName("Boss");

        var originalSupervisorSince = LocalDate.now().minusMonths(6);
        employee.setSupervisor(originalSupervisor);
        employee.setSupervisorSince(originalSupervisorSince);

        command = new AssignSupervisorCommand(employee, supervisor, employeeRepository, eventPublisher);

        command.execute();
        assertEquals(supervisor, employee.getSupervisor());

        command.undo();

        assertEquals(originalSupervisor, employee.getSupervisor());
        assertEquals(originalSupervisorSince, employee.getSupervisorSince());
        assertFalse(command.canUndo());
        verify(employeeRepository, times(2)).save(employee);
        verify(eventPublisher, times(2)).publishEvent(any(EmployeeEvent.class));
    }

    @Test
    void shouldUndoSupervisorAssignmentWhenNoPreviousSupervisor() {
        assertNull(employee.getSupervisor());

        command = new AssignSupervisorCommand(employee, supervisor, employeeRepository, eventPublisher);

        command.execute();
        assertEquals(supervisor, employee.getSupervisor());

        command.undo();

        assertNull(employee.getSupervisor());
        assertNull(employee.getSupervisorSince());
        verify(employeeRepository, times(2)).save(employee);
    }

    @Test
    void shouldThrowExceptionWhenEmployeeIsTheirOwnSupervisor() {
        command = new AssignSupervisorCommand(employee, employee, employeeRepository, eventPublisher);

        assertThrows(IllegalArgumentException.class, () -> command.execute());
        verify(employeeRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenExecutingAlreadyExecutedCommand() {
        command = new AssignSupervisorCommand(employee, supervisor, employeeRepository, eventPublisher);

        command.execute();

        assertThrows(IllegalStateException.class, () -> command.execute());
    }

    @Test
    void shouldThrowExceptionWhenUndoingNonExecutedCommand() {
        command = new AssignSupervisorCommand(employee, supervisor, employeeRepository, eventPublisher);

        assertThrows(IllegalStateException.class, () -> command.undo());
    }

    @Test
    void shouldPublishSupervisorAssignedEventOnExecution() {
        command = new AssignSupervisorCommand(employee, supervisor, employeeRepository, eventPublisher);

        command.execute();

        ArgumentCaptor<EmployeeEvent> eventCaptor = ArgumentCaptor.forClass(EmployeeEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        var event = eventCaptor.getValue();
        assertEquals(EmployeeEvent.EventType.SUPERVISOR_ASSIGNED, event.getEventType());
        assertEquals(employee, event.getEmployee());
        assertEquals(supervisor, event.getNewValue());
        assertTrue(event.getDetails().contains("assigned via command"));
    }

    @Test
    void shouldPublishUndoEventOnUndo() {
        command = new AssignSupervisorCommand(employee, supervisor, employeeRepository, eventPublisher);

        command.execute();
        command.undo();

        ArgumentCaptor<EmployeeEvent> eventCaptor = ArgumentCaptor.forClass(EmployeeEvent.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        var events = eventCaptor.getAllValues();
        var undoEvent = events.get(1);
        assertEquals(EmployeeEvent.EventType.SUPERVISOR_ASSIGNED, undoEvent.getEventType());
        assertTrue(undoEvent.getDetails().contains("undone"));
    }

    @Test
    void shouldHaveCorrectDescription() {
        command = new AssignSupervisorCommand(employee, supervisor, employeeRepository, eventPublisher);

        var description = command.getDescription();

        assertTrue(description.contains("Jane"));
        assertTrue(description.contains("Smith"));
        assertTrue(description.contains("John"));
        assertTrue(description.contains("Doe"));
        assertTrue(description.contains("supervisor"));
    }

    @Test
    void shouldSetSupervisorSinceToCurrentDate() {
        var beforeExecution = LocalDate.now();
        command = new AssignSupervisorCommand(employee, supervisor, employeeRepository, eventPublisher);

        command.execute();

        var afterExecution = LocalDate.now();
        assertTrue(employee.getSupervisorSince().isEqual(beforeExecution) ||
                employee.getSupervisorSince().isEqual(afterExecution));
    }

    @Test
    void shouldPreservePreviousSupervisorInformation() {
        var previousSupervisor = new Employee();
        previousSupervisor.setId(3L);
        var previousDate = LocalDate.now().minusMonths(3);

        employee.setSupervisor(previousSupervisor);
        employee.setSupervisorSince(previousDate);

        command = new AssignSupervisorCommand(employee, supervisor, employeeRepository, eventPublisher);

        command.execute();
        command.undo();

        assertEquals(previousSupervisor, employee.getSupervisor());
        assertEquals(previousDate, employee.getSupervisorSince());
    }
}