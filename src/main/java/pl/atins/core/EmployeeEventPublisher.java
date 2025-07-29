package pl.atins.core;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class EmployeeEventPublisher {

    private final List<EmployeeEventObserver> observers = new ArrayList<>();

    public void addObserver(EmployeeEventObserver observer) {
        observers.add(observer);
    }

    public void publishEvent(EmployeeEvent event) {
        for (EmployeeEventObserver observer : observers) {
            if (observer.isInterestedIn(event.getEventType())) {
                CompletableFuture.runAsync(() -> observer.onEmployeeEvent(event));
            }
        }
    }
}