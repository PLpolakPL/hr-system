package pl.atins.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuditLogObserver implements EmployeeEventObserver {

    @Override
    public void onEmployeeEvent(EmployeeEvent event) {
        log.info("AUDIT: {} - Employee: {} {} (ID: {}), Details: {}, Old Value: {}, New Value: {}",
                event.getEventType(),
                event.getEmployee().getFirstName(),
                event.getEmployee().getLastName(),
                event.getEmployee().getId(),
                event.getDetails(),
                event.getOldValue(),
                event.getNewValue());
    }

    @Override
    public boolean isInterestedIn(EmployeeEvent.EventType eventType) {
        return true;
    }
}