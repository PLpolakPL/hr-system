package pl.atins.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.atins.dto.CreateDepartmentRequest;
import pl.atins.dto.DepartmentResponse;
import pl.atins.dto.UpdateDepartmentRequest;
import pl.atins.service.DepartmentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private DepartmentResponse departmentResponse;
    private CreateDepartmentRequest createRequest;

    @BeforeEach
    void setUp() {
        departmentResponse = new DepartmentResponse();
        departmentResponse.setId(1L);
        departmentResponse.setName("IT Department");
        departmentResponse.setDescription("Information Technology Department");
        departmentResponse.setLocation("Building A, Floor 3");

        createRequest = new CreateDepartmentRequest();
        createRequest.setName("HR Department");
        createRequest.setDescription("Human Resources Department");
        createRequest.setLocation("Building B, Floor 1");
    }

    @Test
    void shouldCreateDepartment() throws Exception {
        when(departmentService.createDepartment(any(CreateDepartmentRequest.class))).thenReturn(departmentResponse);

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT Department"))
                .andExpect(jsonPath("$.location").value("Building A, Floor 3"));
    }

    @Test
    void shouldReturnBadRequestForInvalidCreateRequest() throws Exception {
        createRequest.setName(null);

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetDepartmentById() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(departmentResponse);

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT Department"));
    }

    @Test
    void shouldGetDepartmentByName() throws Exception {
        when(departmentService.getDepartmentByName("IT Department")).thenReturn(departmentResponse);

        mockMvc.perform(get("/api/departments/name/IT Department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("IT Department"));
    }

    @Test
    void shouldGetDepartmentByHeadId() throws Exception {
        when(departmentService.getDepartmentByHeadId(1L)).thenReturn(departmentResponse);

        mockMvc.perform(get("/api/departments/head/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldGetAllDepartments() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(List.of(departmentResponse));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("IT Department"));
    }

    @Test
    void shouldUpdateDepartment() throws Exception {
        var updateRequest = new UpdateDepartmentRequest();
        updateRequest.setName("Updated IT Department");
        updateRequest.setDescription("Updated description");

        when(departmentService.updateDepartment(eq(1L), any(UpdateDepartmentRequest.class)))
                .thenReturn(departmentResponse);

        mockMvc.perform(put("/api/departments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldDeleteDepartment() throws Exception {
        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAssignHead() throws Exception {
        when(departmentService.assignHead(1L, 1L)).thenReturn(departmentResponse);

        mockMvc.perform(put("/api/departments/1/head/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldRemoveHead() throws Exception {
        when(departmentService.removeHead(1L)).thenReturn(departmentResponse);

        mockMvc.perform(delete("/api/departments/1/head"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnBadRequestForMissingRequiredFields() throws Exception {
        createRequest.setName("");

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForMissingLocation() throws Exception {
        createRequest.setLocation(null);

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleServiceExceptions() throws Exception {
        when(departmentService.getDepartmentById(999L))
                .thenThrow(new IllegalArgumentException("Department not found"));

        mockMvc.perform(get("/api/departments/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateDepartmentWithHead() throws Exception {
        createRequest.setHeadId(1L);
        when(departmentService.createDepartment(any(CreateDepartmentRequest.class))).thenReturn(departmentResponse);

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldUpdateDepartmentPartially() throws Exception {
        var updateRequest = new UpdateDepartmentRequest();
        updateRequest.setDescription("New description only");

        when(departmentService.updateDepartment(eq(1L), any(UpdateDepartmentRequest.class)))
                .thenReturn(departmentResponse);

        mockMvc.perform(put("/api/departments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}