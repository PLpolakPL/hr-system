package pl.atins.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.atins.core.AuditLogObserver;
import pl.atins.core.EmployeeEventPublisher;
import pl.atins.core.NotificationObserver;

@Configuration
@RequiredArgsConstructor
public class ObserverConfiguration {

    private final EmployeeEventPublisher publisher;
    private final AuditLogObserver auditLogObserver;
    private final NotificationObserver notificationObserver;

    @Bean
    public ApplicationRunner registerObservers() {
        return args -> {
            publisher.addObserver(auditLogObserver);
            publisher.addObserver(notificationObserver);
        };
    }
}