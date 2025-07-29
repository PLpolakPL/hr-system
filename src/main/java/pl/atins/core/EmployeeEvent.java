package pl.atins.core;

import lombok.Getter;
import pl.atins.domain.Employee;

import java.time.LocalDateTime;

@Getter
public class EmployeeEvent {

    public enum EventType {
        HIRED, PROMOTED, SALARY_ADJUSTED, DEPARTMENT_CHANGED, SUPERVISOR_ASSIGNED, TERMINATED
    }

    private final Employee employee;
    private final EventType eventType;
    private final LocalDateTime timestamp;
    private final String details;
    private final Object oldValue;
    private final Object newValue;

    public EmployeeEvent(Employee employee, EventType eventType, String details, Object oldValue, Object newValue) {
        this.employee = employee;
        this.eventType = eventType;
        this.details = details;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("EmployeeEvent{employee=%s %s, eventType=%s, timestamp=%s, details='%s'}",
                employee.getFirstName(), employee.getLastName(), eventType, timestamp, details);
    }
}