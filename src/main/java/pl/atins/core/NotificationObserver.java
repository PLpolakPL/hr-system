package pl.atins.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
public class NotificationObserver implements EmployeeEventObserver {

    private static final Set<EmployeeEvent.EventType> NOTIFICATION_EVENTS = Set.of(
            EmployeeEvent.EventType.HIRED,
            EmployeeEvent.EventType.PROMOTED,
            EmployeeEvent.EventType.TERMINATED);

    @Override
    public void onEmployeeEvent(EmployeeEvent event) {
        log.info("NOTIFICATION: {} event for employee {} {} - {}",
                event.getEventType(),
                event.getEmployee().getFirstName(),
                event.getEmployee().getLastName(),
                event.getDetails());

        sendNotification(event);
    }

    @Override
    public boolean isInterestedIn(EmployeeEvent.EventType eventType) {
        return NOTIFICATION_EVENTS.contains(eventType);
    }

    private void sendNotification(EmployeeEvent event) {
        log.info("Sending notification email/SMS for event: {}", event.getEventType());
    }
}