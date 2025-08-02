package pl.atins.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.atins.core.AssignSupervisorCommand;
import pl.atins.core.EmployeeEventPublisher;
import pl.atins.core.HRCommandManager;
import pl.atins.core.PromoteEmployeeCommand;
import pl.atins.domain.Employee;
import pl.atins.dto.EmployeeResponse;
import pl.atins.repository.EmployeeRepository;
import pl.atins.service.EmployeeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HRCommandController.class)
class HRCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HRCommandManager commandManager;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private EmployeeEventPublisher eventPublisher;

    @MockitoBean
    private EmployeeService employeeService;

    private Employee employee;
    private Employee supervisor;
    private EmployeeResponse employeeResponse;

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

        employeeResponse = new EmployeeResponse();
        employeeResponse.setId(1L);
        employeeResponse.setFirstName("John");
        employeeResponse.setLastName("Doe");
        employeeResponse.setJobTitle("Senior Developer");
    }

    @Test
    void shouldPromoteEmployee() throws Exception {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeService.getEmployeeById(1L)).thenReturn(employeeResponse);

        mockMvc.perform(post("/api/hr-commands/1/promote")
                .param("newJobTitle", "Senior Developer")
                .param("salaryIncrease", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.jobTitle").value("Senior Developer"));

        verify(commandManager).executeCommand(any(PromoteEmployeeCommand.class));
    }

    @Test
    void shouldPromoteEmployeeWithoutSalaryIncrease() throws Exception {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeService.getEmployeeById(1L)).thenReturn(employeeResponse);

        mockMvc.perform(post("/api/hr-commands/1/promote")
                .param("newJobTitle", "Senior Developer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(commandManager).executeCommand(any(PromoteEmployeeCommand.class));
    }

    @Test
    void shouldReturnBadRequestWhenEmployeeNotFoundForPromotion() throws Exception {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/hr-commands/999/promote")
                .param("newJobTitle", "Senior Developer"))
                .andExpect(status().isBadRequest());

        verify(commandManager, never()).executeCommand(any());
    }

    @Test
    void shouldAssignSupervisor() throws Exception {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(supervisor));
        when(employeeService.getEmployeeById(1L)).thenReturn(employeeResponse);

        mockMvc.perform(post("/api/hr-commands/1/assign-supervisor/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(commandManager).executeCommand(any(AssignSupervisorCommand.class));
    }

    @Test
    void shouldReturnBadRequestWhenEmployeeNotFoundForSupervisorAssignment() throws Exception {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/hr-commands/999/assign-supervisor/2"))
                .andExpect(status().isBadRequest());

        verify(commandManager, never()).executeCommand(any());
    }

    @Test
    void shouldReturnBadRequestWhenSupervisorNotFound() throws Exception {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/hr-commands/1/assign-supervisor/999"))
                .andExpect(status().isBadRequest());

        verify(commandManager, never()).executeCommand(any());
    }

    @Test
    void shouldUndoLastCommand() throws Exception {
        when(commandManager.canUndo()).thenReturn(true);

        mockMvc.perform(post("/api/hr-commands/undo"))
                .andExpect(status().isOk())
                .andExpect(content().string("Last command undone successfully"));

        verify(commandManager).undoLastCommand();
    }

    @Test
    void shouldReturnBadRequestWhenNoCommandsToUndo() throws Exception {
        when(commandManager.canUndo()).thenReturn(false);

        mockMvc.perform(post("/api/hr-commands/undo"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No commands to undo"));

        verify(commandManager, never()).undoLastCommand();
    }

    @Test
    void shouldReturnCanUndoStatus() throws Exception {
        when(commandManager.canUndo()).thenReturn(true);

        mockMvc.perform(get("/api/hr-commands/can-undo"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldReturnCannotUndoStatus() throws Exception {
        when(commandManager.canUndo()).thenReturn(false);

        mockMvc.perform(get("/api/hr-commands/can-undo"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldReturnHistorySize() throws Exception {
        when(commandManager.getHistorySize()).thenReturn(5);

        mockMvc.perform(get("/api/hr-commands/history-size"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void shouldClearHistory() throws Exception {
        mockMvc.perform(post("/api/hr-commands/clear-history"))
                .andExpect(status().isOk())
                .andExpect(content().string("Command history cleared"));

        verify(commandManager).clearHistory();
    }

    @Test
    void shouldHandleCommandExecutionException() throws Exception {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doThrow(new RuntimeException("Command execution failed")).when(commandManager).executeCommand(any());

        mockMvc.perform(post("/api/hr-commands/1/promote")
                .param("newJobTitle", "Senior Developer"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldHandleUndoException() throws Exception {
        when(commandManager.canUndo()).thenReturn(true);
        doThrow(new IllegalStateException("Cannot undo")).when(commandManager).undoLastCommand();

        mockMvc.perform(post("/api/hr-commands/undo"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldValidatePromotionParameters() throws Exception {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        mockMvc.perform(post("/api/hr-commands/1/promote"))
                .andExpect(status().isBadRequest());

        verify(commandManager, never()).executeCommand(any());
    }

    @Test
    void shouldHandleInvalidSalaryIncrease() throws Exception {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        mockMvc.perform(post("/api/hr-commands/1/promote")
                .param("newJobTitle", "Senior Developer")
                .param("salaryIncrease", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnZeroHistorySizeWhenEmpty() throws Exception {
        when(commandManager.getHistorySize()).thenReturn(0);

        mockMvc.perform(get("/api/hr-commands/history-size"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}