package pl.atins.core;

public interface EmployeeEventObserver {

    void onEmployeeEvent(EmployeeEvent event);

    boolean isInterestedIn(EmployeeEvent.EventType eventType);
}