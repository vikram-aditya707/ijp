package com.example.adminservice.controller;

import com.example.adminservice.entity.Designation;
import com.example.adminservice.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    // ==================================================
    // 1. ADMIN LOGIN CONTROLLER TESTS
    // ==================================================

    @Test
    public void shouldReturn200OnAdminLoginSuccess() throws Exception {
        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("email", "admin@company.com");
        loginReq.put("password", "admin123");

        when(adminService.validateLogin("admin@company.com", "admin123")).thenReturn(true);

        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    public void shouldReturn400WhenEmailOrPasswordMissing() throws Exception {
        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("email", "admin@company.com");

        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email and password are required."));
    }

    @Test
    public void shouldReturn400WhenEmailDomainInvalid() throws Exception {
        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("email", "admin@gmail.com");
        loginReq.put("password", "admin123");

        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only company email addresses ending with @company.com are allowed."));
    }

    @Test
    public void shouldReturn401WhenPasswordInvalid() throws Exception {
        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("email", "admin@company.com");
        loginReq.put("password", "WrongPass");

        when(adminService.validateLogin("admin@company.com", "WrongPass")).thenReturn(false);

        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    // ==================================================
    // 2. DESIGNATION CONTROLLER TESTS
    // ==================================================

    @Test
    public void shouldReturn201OnAddDesignationSuccess() throws Exception {
        Designation desig = new Designation(null, "React Developer", "ACTIVE");
        Designation created = new Designation(1L, "React Developer", "ACTIVE");

        when(adminService.addDesignation(any(Designation.class))).thenReturn(created);

        mockMvc.perform(post("/api/admin/designations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(desig)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("React Developer"));
    }

    @Test
    public void shouldReturn400OnAddDesignationFailure() throws Exception {
        Designation desig = new Designation(null, "React Developer", "ACTIVE");

        when(adminService.addDesignation(any(Designation.class))).thenThrow(new RuntimeException("Designation 'React Developer' already exists!"));

        mockMvc.perform(post("/api/admin/designations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(desig)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Designation 'React Developer' already exists!"));
    }

    @Test
    public void shouldReturn200OnGetAllDesignations() throws Exception {
        Designation d1 = new Designation(1L, "Java Developer", "ACTIVE");
        when(adminService.getAllDesignations()).thenReturn(List.of(d1));

        mockMvc.perform(get("/api/admin/designations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java Developer"));
    }

    @Test
    public void shouldReturn200OnGetActiveDesignations() throws Exception {
        Designation d1 = new Designation(1L, "Java Developer", "ACTIVE");
        when(adminService.getActiveDesignations()).thenReturn(List.of(d1));

        mockMvc.perform(get("/api/admin/designations/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    public void shouldReturn200OnUpdateDesignationSuccess() throws Exception {
        Designation updateData = new Designation(null, "Senior Java Dev", "ACTIVE");
        Designation updated = new Designation(1L, "Senior Java Dev", "ACTIVE");

        when(adminService.updateDesignation(eq(1L), any(Designation.class))).thenReturn(updated);

        mockMvc.perform(put("/api/admin/designations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Senior Java Dev"));
    }

    @Test
    public void shouldReturn400OnUpdateDesignationFailure() throws Exception {
        Designation updateData = new Designation();
        when(adminService.updateDesignation(eq(99L), any(Designation.class))).thenThrow(new RuntimeException("Designation with ID 99 not found!"));

        mockMvc.perform(put("/api/admin/designations/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Designation with ID 99 not found!"));
    }

    @Test
    public void shouldReturn200OnUpdateDesignationStatusSuccess() throws Exception {
        Designation updated = new Designation(1L, "Java Developer", "INACTIVE");
        Map<String, String> body = Map.of("status", "INACTIVE");

        when(adminService.updateDesignationStatus(eq(1L), eq("INACTIVE"))).thenReturn(updated);

        mockMvc.perform(put("/api/admin/designations/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    public void shouldReturn400OnUpdateDesignationStatusFailure() throws Exception {
        Map<String, String> body = Map.of("status", "INACTIVE");
        when(adminService.updateDesignationStatus(eq(99L), eq("INACTIVE"))).thenThrow(new RuntimeException("Designation with ID 99 not found!"));

        mockMvc.perform(put("/api/admin/designations/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Designation with ID 99 not found!"));
    }
}
