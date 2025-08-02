package pl.atins.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.atins.dto.CreateEmployeeRequest;
import pl.atins.dto.EmployeeResponse;
import pl.atins.dto.SalaryAdjustmentRequest;
import pl.atins.dto.UpdateEmployeeRequest;
import pl.atins.service.EmployeeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeResponse employeeResponse;
    private CreateEmployeeRequest createRequest;

    @BeforeEach
    void setUp() {
        employeeResponse = new EmployeeResponse();
        employeeResponse.setId(1L);
        employeeResponse.setFirstName("John");
        employeeResponse.setLastName("Doe");
        employeeResponse.setEmail("john.doe@company.com");
        employeeResponse.setJobTitle("Developer");
        employeeResponse.setSalary(new BigDecimal("50000"));
        employeeResponse.setHireDate(LocalDate.now());

        createRequest = new CreateEmployeeRequest();
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setEmail("john.doe@company.com");
        createRequest.setJobTitle("Developer");
        createRequest.setHireDate(LocalDate.now());
        createRequest.setSalary(new BigDecimal("50000"));
    }

    @Test
    void shouldCreateEmployee() throws Exception {
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(employeeResponse);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@company.com"));
    }

    @Test
    void shouldReturnBadRequestForInvalidCreateRequest() throws Exception {
        createRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetEmployeeById() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employeeResponse);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void shouldGetEmployeeByEmail() throws Exception {
        when(employeeService.getEmployeeByEmail("john.doe@company.com")).thenReturn(employeeResponse);

        mockMvc.perform(get("/api/employees/email/john.doe@company.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@company.com"));
    }

    @Test
    void shouldGetAllEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(employeeResponse));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void shouldGetEmployeesByJobTitle() throws Exception {
        when(employeeService.getEmployeesByJobTitle("Developer")).thenReturn(List.of(employeeResponse));

        mockMvc.perform(get("/api/employees?jobTitle=Developer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobTitle").value("Developer"));
    }

    @Test
    void shouldGetEmployeesByDepartment() throws Exception {
        when(employeeService.getEmployeesByDepartment(1L)).thenReturn(List.of(employeeResponse));

        mockMvc.perform(get("/api/employees?departmentId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldGetEmployeesBySupervisor() throws Exception {
        when(employeeService.getEmployeesBySupervisor(2L)).thenReturn(List.of(employeeResponse));

        mockMvc.perform(get("/api/employees?supervisorId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldSearchEmployeesByName() throws Exception {
        when(employeeService.searchEmployeesByName("John")).thenReturn(List.of(employeeResponse));

        mockMvc.perform(get("/api/employees?name=John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void shouldUpdateEmployee() throws Exception {
        var updateRequest = new UpdateEmployeeRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setJobTitle("Senior Developer");

        when(employeeService.updateEmployee(eq(1L), any(UpdateEmployeeRequest.class))).thenReturn(employeeResponse);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAdjustSalary() throws Exception {
        var request = new SalaryAdjustmentRequest();
        request.setStrategyType("annual_raise");
        request.setAmount(new BigDecimal("5000"));

        when(employeeService.adjustSalary(eq(1L), any(SalaryAdjustmentRequest.class)))
                .thenReturn(new BigDecimal("55000"));

        mockMvc.perform(post("/api/employees/1/salary/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("55000"));
    }

    @Test
    void shouldPromoteEmployee() throws Exception {
        when(employeeService.promoteEmployee(eq(1L), eq("Senior Developer"), eq(new BigDecimal("10000"))))
                .thenReturn(employeeResponse);

        mockMvc.perform(post("/api/employees/1/promote")
                        .param("newJobTitle", "Senior Developer")
                        .param("salaryAdjustment", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldPromoteEmployeeWithoutSalaryAdjustment() throws Exception {
        when(employeeService.promoteEmployee(eq(1L), eq("Senior Developer"), isNull()))
                .thenReturn(employeeResponse);

        mockMvc.perform(post("/api/employees/1/promote")
                        .param("newJobTitle", "Senior Developer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldAssignSupervisor() throws Exception {
        when(employeeService.assignSupervisor(1L, 2L)).thenReturn(employeeResponse);

        mockMvc.perform(put("/api/employees/1/supervisor/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldAssignToDepartment() throws Exception {
        when(employeeService.assignToDepartment(1L, 1L)).thenReturn(employeeResponse);

        mockMvc.perform(put("/api/employees/1/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldRemoveFromDepartment() throws Exception {
        when(employeeService.removeFromDepartment(1L, 1L)).thenReturn(employeeResponse);

        mockMvc.perform(delete("/api/employees/1/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnBadRequestForMissingRequiredFields() throws Exception {
        createRequest.setFirstName(null);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        createRequest.setEmail("not-an-email");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForNegativeSalary() throws Exception {
        createRequest.setSalary(new BigDecimal("-1000"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleServiceExceptions() throws Exception {
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new IllegalArgumentException("Employee not found"));

        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isBadRequest());
    }
}