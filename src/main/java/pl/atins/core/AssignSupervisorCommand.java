package pl.atins.core;

import pl.atins.domain.Employee;
import pl.atins.repository.EmployeeRepository;

import java.time.LocalDate;

public class AssignSupervisorCommand implements HRCommand {

    private final Employee employee;
    private final Employee newSupervisor;
    private final EmployeeRepository employeeRepository;
    private final EmployeeEventPublisher eventPublisher;

    private Employee previousSupervisor;
    private LocalDate previousSupervisorSince;
    private boolean executed = false;

    public AssignSupervisorCommand(Employee employee, Employee newSupervisor,
            EmployeeRepository employeeRepository, EmployeeEventPublisher eventPublisher) {
        this.employee = employee;
        this.newSupervisor = newSupervisor;
        this.employeeRepository = employeeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command already executed");
        }

        if (employee.getId().equals(newSupervisor.getId())) {
            throw new IllegalArgumentException("Employee cannot be their own supervisor");
        }

        previousSupervisor = employee.getSupervisor();
        previousSupervisorSince = employee.getSupervisorSince();

        employee.addSupervisor(newSupervisor);
        employee.setSupervisorSince(LocalDate.now());

        employeeRepository.save(employee);

        var event = new EmployeeEvent(employee, EmployeeEvent.EventType.SUPERVISOR_ASSIGNED,
                "Supervisor assigned via command", previousSupervisor, newSupervisor);
        eventPublisher.publishEvent(event);

        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException("Cannot undo command that wasn't executed");
        }

        if (previousSupervisor != null) {
            employee.addSupervisor(previousSupervisor);
            employee.setSupervisorSince(previousSupervisorSince);
        } else {
            employee.setSupervisor(null);
            employee.setSupervisorSince(null);
        }

        employeeRepository.save(employee);

        var event = new EmployeeEvent(employee, EmployeeEvent.EventType.SUPERVISOR_ASSIGNED,
                "Supervisor assignment undone", newSupervisor, previousSupervisor);
        eventPublisher.publishEvent(event);

        executed = false;
    }

    @Override
    public String getDescription() {
        return String.format("Assign %s %s as supervisor of %s %s",
                newSupervisor.getFirstName(), newSupervisor.getLastName(),
                employee.getFirstName(), employee.getLastName());
    }

    @Override
    public boolean canUndo() {
        return executed;
    }
}