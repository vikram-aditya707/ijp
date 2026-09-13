package com.example.jobservice;

import com.example.jobservice.entity.JobPosting;
import com.example.jobservice.repository.JobPostingRepository;
import com.example.jobservice.service.JobPostingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class JobPostingServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;

    @InjectMocks
    private JobPostingService jobPostingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================================================
    // 1. CREATE JOB TESTS
    // ==================================================

    @Test
    public void testCreateJob() {
        JobPosting job = new JobPosting(null, "JOB101", "Developer Role", "Java Developer", "Bangalore", "Java, Spring Boot", "2 years", 50000.0, 80000.0, "OPEN");
        when(jobPostingRepository.save(any(JobPosting.class))).thenReturn(job);

        JobPosting created = jobPostingService.createJob(job);

        assertNotNull(created);
        assertEquals("OPEN", created.getStatus());
        assertEquals("Java Developer", created.getDesignation());
        verify(jobPostingRepository, times(1)).save(job);
    }

    @Test
    public void shouldCreateJobWithDefaultOpenStatusWhenStatusNull() {
        JobPosting job = new JobPosting(null, "JOB102", "Angular Role", "Frontend Developer", "Pune", "Angular, TS", "3 years", 60000.0, 90000.0, null);
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobPosting created = jobPostingService.createJob(job);

        assertNotNull(created);
        assertEquals("OPEN", created.getStatus());
    }

    // ==================================================
    // 2. GET JOBS TESTS
    // ==================================================

    @Test
    public void shouldGetAllJobsSuccessfully() {
        JobPosting job1 = new JobPosting(1L, "JOB101", "Desc", "Java Dev", "Blr", "Java", "2 yrs", 50.0, 80.0, "OPEN");
        when(jobPostingRepository.findAll()).thenReturn(List.of(job1));

        List<JobPosting> jobs = jobPostingService.getAllJobs();

        assertEquals(1, jobs.size());
        assertEquals("JOB101", jobs.get(0).getJobId());
    }

    @Test
    public void shouldGetOpenJobsSuccessfully() {
        JobPosting job1 = new JobPosting(1L, "JOB101", "Desc", "Java Dev", "Blr", "Java", "2 yrs", 50.0, 80.0, "OPEN");
        when(jobPostingRepository.findByStatus("OPEN")).thenReturn(List.of(job1));

        List<JobPosting> openJobs = jobPostingService.getOpenJobs();

        assertEquals(1, openJobs.size());
        assertEquals("OPEN", openJobs.get(0).getStatus());
    }

    @Test
    public void shouldGetJobByIdSuccessfully() {
        JobPosting job = new JobPosting(1L, "JOB101", "Desc", "Java Dev", "Blr", "Java", "2 yrs", 50.0, 80.0, "OPEN");
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(job));

        Optional<JobPosting> result = jobPostingService.getJobById(1L);

        assertTrue(result.isPresent());
        assertEquals("Java Dev", result.get().getDesignation());
    }

    @Test
    public void shouldReturnEmptyWhenJobNotFoundById() {
        when(jobPostingRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<JobPosting> result = jobPostingService.getJobById(99L);

        assertTrue(result.isEmpty());
    }

    // ==================================================
    // 3. UPDATE & CLOSE JOB TESTS
    // ==================================================

    @Test
    public void shouldUpdateJobSuccessfully() {
        JobPosting existingJob = new JobPosting(1L, "JOB101", "Old Desc", "Java Dev", "Blr", "Java", "2 yrs", 50.0, 80.0, "OPEN");
        JobPosting updatedInfo = new JobPosting(null, "JOB101", "New Desc", "Senior Java Dev", "Hyderabad", "Java 17, Spring", "5 yrs", 80.0, 120.0, "OPEN");

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(existingJob));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(i -> i.getArgument(0));

        JobPosting result = jobPostingService.updateJob(1L, updatedInfo);

        assertNotNull(result);
        assertEquals("Senior Java Dev", result.getDesignation());
        assertEquals("Hyderabad", result.getLocation());
    }

    @Test
    public void shouldReturnNullWhenUpdatingNonExistentJob() {
        JobPosting updatedInfo = new JobPosting();
        when(jobPostingRepository.findById(99L)).thenReturn(Optional.empty());

        JobPosting result = jobPostingService.updateJob(99L, updatedInfo);

        assertNull(result);
    }

    @Test
    public void testCloseJob() {
        JobPosting job = new JobPosting(1L, "JOB101", "Developer Role", "Java Developer", "Bangalore", "Java, Spring Boot", "2 years", 50000.0, 80000.0, "OPEN");
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobPosting closed = jobPostingService.closeJob(1L);

        assertNotNull(closed);
        assertEquals("CLOSED", closed.getStatus());
        verify(jobPostingRepository, times(1)).findById(1L);
        verify(jobPostingRepository, times(1)).save(job);
    }

    @Test
    public void shouldReturnNullWhenClosingNonExistentJob() {
        when(jobPostingRepository.findById(99L)).thenReturn(Optional.empty());

        JobPosting result = jobPostingService.closeJob(99L);

        assertNull(result);
    }

    // ==================================================
    // 4. DELETE JOB TESTS
    // ==================================================

    @Test
    public void testDeleteJob_Success() {
        JobPosting job = new JobPosting(1L, "JOB101", "Developer Role", "Java Developer", "Bangalore", "Java, Spring Boot", "2 years", 50000.0, 80000.0, "OPEN");
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(job));

        jobPostingService.deleteJob(1L);

        verify(jobPostingRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteJob_NotFound() {
        when(jobPostingRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jobPostingService.deleteJob(99L));
        assertTrue(exception.getMessage().contains("not found"));
    }
}
