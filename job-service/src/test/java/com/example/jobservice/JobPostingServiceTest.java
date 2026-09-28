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
    // PART 7 — JOB CREATION TESTS (20–32)
    // ==================================================

    @Test
    public void test20_shouldCreateValidJobSuccessfully() {
        JobPosting job = new JobPosting(null, null, "Developer Role", "Java Developer", "Bangalore", "Java, Spring Boot", "2 years", 50000.0, 80000.0, "OPEN");
        when(jobPostingRepository.findAll()).thenReturn(Collections.emptyList());
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(i -> i.getArgument(0));

        JobPosting created = jobPostingService.createJob(job);

        assertNotNull(created);
        assertEquals("JOB101", created.getJobId());
        assertEquals("OPEN", created.getStatus());
        assertEquals("Java Developer", created.getDesignation());
    }

    @Test
    public void test21_shouldFailWhenMissingDesignation() {
        JobPosting job = new JobPosting(null, null, "Desc", "", "Blr", "Java", "2 yrs", 50.0, 80.0, "OPEN");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jobPostingService.createJob(job));
        assertTrue(exception.getMessage().contains("Designation / Role is required"));
    }

    @Test
    public void test22_shouldFailWhenMissingDescription() {
        JobPosting job = new JobPosting(null, null, "", "Java Dev", "Blr", "Java", "2 yrs", 50.0, 80.0, "OPEN");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jobPostingService.createJob(job));
        assertTrue(exception.getMessage().contains("Job description is required"));
    }

    @Test
    public void test23_shouldFailWhenMissingLocation() {
        JobPosting job = new JobPosting(null, null, "Desc", "Java Dev", "", "Java", "2 yrs", 50.0, 80.0, "OPEN");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jobPostingService.createJob(job));
        assertTrue(exception.getMessage().contains("Location is required"));
    }

    @Test
    public void test24_shouldFailWhenMissingExperience() {
        JobPosting job = new JobPosting(null, null, "Desc", "Java Dev", "Blr", "Java", "", 50.0, 80.0, "OPEN");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jobPostingService.createJob(job));
        assertTrue(exception.getMessage().contains("Required experience is required"));
    }

    @Test
    public void test25_shouldFailWhenMissingSkillSet() {
        JobPosting job = new JobPosting(null, null, "Desc", "Java Dev", "Blr", "", "2 yrs", 50.0, 80.0, "OPEN");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jobPostingService.createJob(job));
        assertTrue(exception.getMessage().contains("Required skill set is required"));
    }

    @Test
    public void test26_shouldFailWhenMissingMinimumSalary() {
        JobPosting job = new JobPosting(null, null, "Desc", "Java Dev", "Blr", "Java", "2 yrs", 0.0, 80.0, "OPEN");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jobPostingService.createJob(job));
        assertTrue(exception.getMessage().contains("Minimum salary is required"));
    }

    @Test
    public void test27_shouldFailWhenMissingMaximumSalary() {
        JobPosting job = new JobPosting(null, null, "Desc", "Java Dev", "Blr", "Java", "2 yrs", 50.0, null, "OPEN");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jobPostingService.createJob(job));
        assertTrue(exception.getMessage().contains("Maximum salary is required"));
    }

    @Test
    public void test28_shouldOverrideUserSuppliedJobCodeWithBackendGeneratedCode() {
        JobPosting job = new JobPosting(null, "JOB999", "Desc", "Java Dev", "Blr", "Java", "2 yrs", 50.0, 80.0, "OPEN");
        when(jobPostingRepository.findAll()).thenReturn(Collections.emptyList());
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(i -> i.getArgument(0));

        JobPosting created = jobPostingService.createJob(job);
        assertEquals("JOB101", created.getJobId());
    }

    @Test
    public void test29_shouldGenerateFirstJobCodeAsJOB101WhenNoJobsExist() {
        when(jobPostingRepository.findAll()).thenReturn(Collections.emptyList());

        String nextCode = jobPostingService.generateNextJobCode();
        assertEquals("JOB101", nextCode);
    }

    @Test
    public void test30_shouldAutoIncrementSubsequentJobCode() {
        JobPosting job1 = new JobPosting(1L, "JOB101", "Desc", "Java Dev", "Blr", "Java", "2 yrs", 50.0, 80.0, "OPEN");
        JobPosting job2 = new JobPosting(2L, "JOB102", "Desc", "Angular Dev", "Blr", "Angular", "3 yrs", 60.0, 90.0, "OPEN");
        when(jobPostingRepository.findAll()).thenReturn(List.of(job1, job2));

        String nextCode = jobPostingService.generateNextJobCode();
        assertEquals("JOB103", nextCode);
    }

    @Test
    public void test31_shouldNotOverwriteExistingJobCodesOnUpdate() {
        JobPosting existingJob = new JobPosting(1L, "JOB101", "Old Desc", "Java Dev", "Blr", "Java", "2 yrs", 50.0, 80.0, "OPEN");
        JobPosting updatedInfo = new JobPosting(null, "JOB101", "New Desc", "Senior Java Dev", "Hyderabad", "Java 17, Spring", "5 yrs", 80.0, 120.0, "OPEN");

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(existingJob));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(i -> i.getArgument(0));

        JobPosting result = jobPostingService.updateJob(1L, updatedInfo);

        assertNotNull(result);
        assertEquals("JOB101", result.getJobId());
        assertEquals("Senior Java Dev", result.getDesignation());
    }

    @Test
    public void test32_shouldPreventDuplicateJobCodeGenerations() {
        JobPosting job1 = new JobPosting(1L, "JOB101", "Desc", "Java Dev", "Blr", "Java", "2 yrs", 50.0, 80.0, "OPEN");
        JobPosting job2 = new JobPosting(2L, "JOB103", "Desc", "Angular Dev", "Blr", "Angular", "3 yrs", 60.0, 90.0, "OPEN");
        when(jobPostingRepository.findAll()).thenReturn(List.of(job1, job2));

        String nextCode = jobPostingService.generateNextJobCode();
        assertEquals("JOB104", nextCode);
    }

    // ==================================================
    // ADDITIONAL GET, UPDATE, CLOSE, DELETE TESTS
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
    public void testCloseJob() {
        JobPosting job = new JobPosting(1L, "JOB101", "Developer Role", "Java Developer", "Bangalore", "Java, Spring Boot", "2 years", 50000.0, 80000.0, "OPEN");
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobPosting closed = jobPostingService.closeJob(1L);

        assertNotNull(closed);
        assertEquals("CLOSED", closed.getStatus());
    }

    @Test
    public void testDeleteJob_Success() {
        JobPosting job = new JobPosting(1L, "JOB101", "Developer Role", "Java Developer", "Bangalore", "Java, Spring Boot", "2 years", 50000.0, 80000.0, "OPEN");
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(job));

        jobPostingService.deleteJob(1L);

        verify(jobPostingRepository, times(1)).deleteById(1L);
    }
}
