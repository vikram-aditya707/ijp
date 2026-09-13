package com.example.candidateservice.controller;

import com.example.candidateservice.entity.Candidate;
import com.example.candidateservice.entity.Interview;
import com.example.candidateservice.entity.Notification;
import com.example.candidateservice.service.CandidateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CandidateControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CandidateService candidateService;

    @InjectMocks
    private CandidateController candidateController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(candidateController).build();
    }

    // ==================================================
    // 1. LOGIN CONTROLLER TESTS
    // ==================================================

    @Test
    public void shouldReturn200OnEmployeeLoginSuccess() throws Exception {
        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("email", "aditya@company.com");
        loginReq.put("password", "Pass@123");

        Candidate candidate = new Candidate(1L, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Pass@123", 0L);
        when(candidateService.loginEmployee("aditya@company.com", "Pass@123")).thenReturn(candidate);

        mockMvc.perform(post("/api/candidates/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("aditya@company.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    @Test
    public void shouldReturn401OnEmployeeLoginFailure() throws Exception {
        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("email", "aditya@company.com");
        loginReq.put("password", "WrongPass");

        when(candidateService.loginEmployee("aditya@company.com", "WrongPass")).thenThrow(new RuntimeException("Invalid email or password."));

        mockMvc.perform(post("/api/candidates/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    public void shouldReturn401OnLoginMissingEmail() throws Exception {
        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("password", "Pass@123");

        mockMvc.perform(post("/api/candidates/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Company Email is required!"));
    }

    // ==================================================
    // 2. REGISTER CONTROLLER TESTS
    // ==================================================

    @Test
    public void shouldReturn201OnEmployeeRegisterSuccess() throws Exception {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Pass@123", 0L);
        Candidate savedCandidate = new Candidate(10L, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Pass@123", 0L);

        when(candidateService.registerEmployee(any(Candidate.class))).thenReturn(savedCandidate);

        mockMvc.perform(post("/api/candidates/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(candidate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("aditya@company.com"));
    }

    @Test
    public void shouldReturn400OnEmployeeRegisterFailure() throws Exception {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@gmail.com", "Pass@123", 0L);

        when(candidateService.registerEmployee(any(Candidate.class))).thenThrow(new RuntimeException("Only company email addresses ending with @company.com are allowed."));

        mockMvc.perform(post("/api/candidates/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(candidate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only company email addresses ending with @company.com are allowed."));
    }

    // ==================================================
    // 3. APPLY FOR JOB CONTROLLER TESTS
    // ==================================================

    @Test
    public void shouldReturn201OnApplyForJobSuccess() throws Exception {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Pass@123", 1L);
        Candidate savedCandidate = new Candidate(5L, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Pass@123", 1L);

        when(candidateService.applyForJob(any(Candidate.class))).thenReturn(savedCandidate);

        mockMvc.perform(post("/api/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(candidate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.jobId").value(1));
    }

    @Test
    public void shouldReturn400OnApplyForJobFailure() throws Exception {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Pass@123", 1L);

        when(candidateService.applyForJob(any(Candidate.class))).thenThrow(new RuntimeException("You have already applied for this job."));

        mockMvc.perform(post("/api/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(candidate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You have already applied for this job."));
    }

    // ==================================================
    // 4. RETRIEVAL & DELETION CONTROLLER TESTS
    // ==================================================

    @Test
    public void shouldReturn200OnGetAllCandidates() throws Exception {
        Candidate c1 = new Candidate(1L, "A", "B", "E1", "1990-01-01", "a@company.com", "Pass@123", 1L);
        when(candidateService.getAllCandidates()).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void shouldReturn200OnGetCandidateByIdSuccess() throws Exception {
        Candidate c1 = new Candidate(1L, "A", "B", "E1", "1990-01-01", "a@company.com", "Pass@123", 1L);
        when(candidateService.getCandidateById(1L)).thenReturn(Optional.of(c1));

        mockMvc.perform(get("/api/candidates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void shouldReturn404OnGetCandidateByIdNotFound() throws Exception {
        when(candidateService.getCandidateById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/candidates/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn200OnDeleteCandidateSuccess() throws Exception {
        doNothing().when(candidateService).deleteCandidate(1L);

        mockMvc.perform(delete("/api/candidates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee deleted successfully."));
    }

    @Test
    public void shouldReturn400OnDeleteCandidateFailure() throws Exception {
        doThrow(new RuntimeException("Candidate record with ID 99 not found!")).when(candidateService).deleteCandidate(99L);

        mockMvc.perform(delete("/api/candidates/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Candidate record with ID 99 not found!"));
    }

    // ==================================================
    // 5. STATUS UPDATE CONTROLLER TESTS
    // ==================================================

    @Test
    public void shouldReturn200OnUpdateCandidateStatusSuccess() throws Exception {
        Candidate updated = new Candidate(1L, "A", "B", "E1", "1990-01-01", "a@company.com", "Pass@123", 1L, "SHORTLISTED");
        Map<String, String> body = Map.of("status", "SHORTLISTED");

        when(candidateService.updateCandidateStatus(eq(1L), eq("SHORTLISTED"))).thenReturn(updated);

        mockMvc.perform(put("/api/candidates/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    @Test
    public void shouldReturn400OnUpdateCandidateStatusFailure() throws Exception {
        Map<String, String> body = Map.of("status", "SHORTLISTED");
        when(candidateService.updateCandidateStatus(eq(99L), eq("SHORTLISTED"))).thenThrow(new RuntimeException("Candidate with ID 99 not found!"));

        mockMvc.perform(put("/api/candidates/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Candidate with ID 99 not found!"));
    }

    // ==================================================
    // 6. INTERVIEW CONTROLLER TESTS
    // ==================================================

    @Test
    public void shouldReturn201OnScheduleInterviewSuccess() throws Exception {
        Interview interview = new Interview(null, 1L, 1L, 10L, "ONLINE", "2026-10-01", "10:00 AM", null, "https://meet.google.com/abc", "HR Lead", "SCHEDULED");
        Interview saved = new Interview(100L, 1L, 1L, 10L, "ONLINE", "2026-10-01", "10:00 AM", null, "https://meet.google.com/abc", "HR Lead", "SCHEDULED");

        when(candidateService.scheduleInterview(any(Interview.class))).thenReturn(saved);

        mockMvc.perform(post("/api/candidates/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(interview)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    public void shouldReturn400OnScheduleInterviewFailure() throws Exception {
        Interview interview = new Interview();
        when(candidateService.scheduleInterview(any(Interview.class))).thenThrow(new RuntimeException("Candidate ID must be provided!"));

        mockMvc.perform(post("/api/candidates/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(interview)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Candidate ID must be provided!"));
    }

    // ==================================================
    // 7. NOTIFICATION CONTROLLER TESTS
    // ==================================================

    @Test
    public void shouldReturn200OnMarkNotificationAsReadSuccess() throws Exception {
        Notification notification = new Notification(1L, 5L, "Title", "Message", "TYPE", true, "2026-09-01");
        when(candidateService.markNotificationAsRead(1L)).thenReturn(notification);

        mockMvc.perform(put("/api/candidates/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    public void shouldReturn404OnMarkNotificationAsReadNotFound() throws Exception {
        when(candidateService.markNotificationAsRead(99L)).thenThrow(new RuntimeException("Notification not found!"));

        mockMvc.perform(put("/api/candidates/notifications/99/read"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Notification not found!"));
    }
}
