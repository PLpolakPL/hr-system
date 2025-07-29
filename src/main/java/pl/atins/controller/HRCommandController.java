package pl.atins.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.atins.core.AssignSupervisorCommand;
import pl.atins.core.EmployeeEventPublisher;
import pl.atins.core.HRCommandManager;
import pl.atins.core.PromoteEmployeeCommand;
import pl.atins.dto.EmployeeResponse;
import pl.atins.repository.EmployeeRepository;
import pl.atins.service.EmployeeService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/hr-commands")
@RequiredArgsConstructor
public class HRCommandController {

    private final HRCommandManager commandManager;
    private final EmployeeRepository employeeRepository;
    private final EmployeeEventPublisher eventPublisher;
    private final EmployeeService employeeService;

    @PostMapping("/{employeeId}/promote")
    public ResponseEntity<EmployeeResponse> promoteEmployee(
            @PathVariable Long employeeId,
            @RequestParam String newJobTitle,
            @RequestParam(required = false) BigDecimal salaryIncrease) {

        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

        var command = new PromoteEmployeeCommand(employee, newJobTitle, salaryIncrease,
                employeeRepository, eventPublisher);
        commandManager.executeCommand(command);

        var response = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{employeeId}/assign-supervisor/{supervisorId}")
    public ResponseEntity<EmployeeResponse> assignSupervisor(
            @PathVariable Long employeeId,
            @PathVariable Long supervisorId) {

        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));
        var supervisor = employeeRepository.findById(supervisorId)
                .orElseThrow(() -> new IllegalArgumentException("Supervisor not found with id: " + supervisorId));

        var command = new AssignSupervisorCommand(employee, supervisor, employeeRepository, eventPublisher);
        commandManager.executeCommand(command);

        var response = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/undo")
    public ResponseEntity<String> undoLastCommand() {
        if (!commandManager.canUndo()) {
            return ResponseEntity.badRequest().body("No commands to undo");
        }

        commandManager.undoLastCommand();
        return ResponseEntity.ok("Last command undone successfully");
    }

    @GetMapping("/can-undo")
    public ResponseEntity<Boolean> canUndo() {
        return ResponseEntity.ok(commandManager.canUndo());
    }

    @GetMapping("/history-size")
    public ResponseEntity<Integer> getHistorySize() {
        return ResponseEntity.ok(commandManager.getHistorySize());
    }

    @PostMapping("/clear-history")
    public ResponseEntity<String> clearHistory() {
        commandManager.clearHistory();
        return ResponseEntity.ok("Command history cleared");
    }
}