package pl.atins.core;

import pl.atins.domain.Employee;
import pl.atins.repository.EmployeeRepository;

import java.math.BigDecimal;

public class PromoteEmployeeCommand implements HRCommand {

    private final Employee employee;
    private final String newJobTitle;
    private final BigDecimal salaryIncrease;
    private final EmployeeRepository employeeRepository;
    private final EmployeeEventPublisher eventPublisher;

    private String previousJobTitle;
    private BigDecimal previousSalary;
    private boolean executed = false;

    public PromoteEmployeeCommand(Employee employee, String newJobTitle, BigDecimal salaryIncrease,
            EmployeeRepository employeeRepository, EmployeeEventPublisher eventPublisher) {
        this.employee = employee;
        this.newJobTitle = newJobTitle;
        this.salaryIncrease = salaryIncrease;
        this.employeeRepository = employeeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command already executed");
        }

        previousJobTitle = employee.getJobTitle();
        previousSalary = employee.getSalary();

        employee.setJobTitle(newJobTitle);
        if (salaryIncrease != null && salaryIncrease.compareTo(BigDecimal.ZERO) > 0) {
            employee.setSalary(employee.getSalary().add(salaryIncrease));
        }

        employeeRepository.save(employee);

        var event = new EmployeeEvent(employee, EmployeeEvent.EventType.PROMOTED,
                "Employee promoted via command", previousJobTitle, newJobTitle);
        eventPublisher.publishEvent(event);

        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException("Cannot undo command that wasn't executed");
        }

        employee.setJobTitle(previousJobTitle);
        employee.setSalary(previousSalary);

        employeeRepository.save(employee);

        var event = new EmployeeEvent(employee, EmployeeEvent.EventType.PROMOTED,
                "Employee promotion undone", newJobTitle, previousJobTitle);
        eventPublisher.publishEvent(event);

        executed = false;
    }

    @Override
    public String getDescription() {
        return String.format("Promote %s %s from %s to %s",
                employee.getFirstName(), employee.getLastName(), previousJobTitle, newJobTitle);
    }

    @Override
    public boolean canUndo() {
        return executed;
    }
}