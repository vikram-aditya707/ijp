package com.example.jobservice.controller;

import com.example.jobservice.entity.JobPosting;
import com.example.jobservice.service.JobPostingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class JobPostingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobPostingService jobPostingService;

    @InjectMocks
    private JobPostingController jobPostingController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(jobPostingController).build();
    }

    // ==================================================
    // 1. CREATE & GET JOBS CONTROLLER TESTS
    // ==================================================

    @Test
    public void shouldReturn201OnCreateJobSuccess() throws Exception {
        JobPosting job = new JobPosting(null, "JOB101", "Developer Role", "Java Developer", "Bangalore", "Java, Spring", "2 years", 50000.0, 80000.0, "OPEN");
        JobPosting createdJob = new JobPosting(1L, "JOB101", "Developer Role", "Java Developer", "Bangalore", "Java, Spring", "2 years", 50000.0, 80000.0, "OPEN");

        when(jobPostingService.createJob(any(JobPosting.class))).thenReturn(createdJob);

        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.jobId").value("JOB101"));
    }

    @Test
    public void shouldReturn200OnGetAllJobs() throws Exception {
        JobPosting job = new JobPosting(1L, "JOB101", "Developer Role", "Java Developer", "Bangalore", "Java", "2 years", 50000.0, 80000.0, "OPEN");
        when(jobPostingService.getAllJobs()).thenReturn(List.of(job));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void shouldReturn200OnGetOpenJobs() throws Exception {
        JobPosting job = new JobPosting(1L, "JOB101", "Developer Role", "Java Developer", "Bangalore", "Java", "2 years", 50000.0, 80000.0, "OPEN");
        when(jobPostingService.getOpenJobs()).thenReturn(List.of(job));

        mockMvc.perform(get("/api/jobs/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    public void shouldReturn200OnGetJobByIdSuccess() throws Exception {
        JobPosting job = new JobPosting(1L, "JOB101", "Developer Role", "Java Developer", "Bangalore", "Java", "2 years", 50000.0, 80000.0, "OPEN");
        when(jobPostingService.getJobById(1L)).thenReturn(Optional.of(job));

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.designation").value("Java Developer"));
    }

    @Test
    public void shouldReturn404OnGetJobByIdNotFound() throws Exception {
        when(jobPostingService.getJobById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/jobs/99"))
                .andExpect(status().isNotFound());
    }

    // ==================================================
    // 2. DELETE, UPDATE & CLOSE CONTROLLER TESTS
    // ==================================================

    @Test
    public void shouldReturn200OnDeleteJobSuccess() throws Exception {
        doNothing().when(jobPostingService).deleteJob(1L);

        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Job posting deleted successfully."));
    }

    @Test
    public void shouldReturn400OnDeleteJobFailure() throws Exception {
        doThrow(new RuntimeException("Job posting with ID 99 not found!")).when(jobPostingService).deleteJob(99L);

        mockMvc.perform(delete("/api/jobs/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Job posting with ID 99 not found!"));
    }

    @Test
    public void shouldReturn200OnUpdateJobSuccess() throws Exception {
        JobPosting updateData = new JobPosting(null, "JOB101", "Updated", "Java Lead", "Bangalore", "Java", "5 yrs", 90000.0, 130000.0, "OPEN");
        JobPosting updatedResult = new JobPosting(1L, "JOB101", "Updated", "Java Lead", "Bangalore", "Java", "5 yrs", 90000.0, 130000.0, "OPEN");

        when(jobPostingService.updateJob(eq(1L), any(JobPosting.class))).thenReturn(updatedResult);

        mockMvc.perform(put("/api/jobs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designation").value("Java Lead"));
    }

    @Test
    public void shouldReturn404OnUpdateJobNotFound() throws Exception {
        JobPosting updateData = new JobPosting();
        when(jobPostingService.updateJob(eq(99L), any(JobPosting.class))).thenReturn(null);

        mockMvc.perform(put("/api/jobs/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn200OnCloseJobSuccess() throws Exception {
        JobPosting closedJob = new JobPosting(1L, "JOB101", "Developer Role", "Java Developer", "Bangalore", "Java", "2 years", 50000.0, 80000.0, "CLOSED");
        when(jobPostingService.closeJob(1L)).thenReturn(closedJob);

        mockMvc.perform(put("/api/jobs/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    public void shouldReturn404OnCloseJobNotFound() throws Exception {
        when(jobPostingService.closeJob(99L)).thenReturn(null);

        mockMvc.perform(put("/api/jobs/99/close"))
                .andExpect(status().isNotFound());
    }
}
