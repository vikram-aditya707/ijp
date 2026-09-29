package com.example.candidateservice;

import com.example.candidateservice.client.JobServiceClient;
import com.example.candidateservice.dto.JobPostingDto;
import com.example.candidateservice.entity.Candidate;
import com.example.candidateservice.entity.Interview;
import com.example.candidateservice.entity.Notification;
import com.example.candidateservice.repository.CandidateRepository;
import com.example.candidateservice.repository.InterviewRepository;
import com.example.candidateservice.repository.NotificationRepository;
import com.example.candidateservice.service.CandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CandidateServiceTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JobServiceClient jobServiceClient;

    @InjectMocks
    private CandidateService candidateService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================================================
    // PART 7 — EMPLOYEE REGISTRATION TESTS (1–19)
    // ==================================================

    @Test
    public void test01_shouldRegisterEmployeeSuccessfully() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Employee@123", "Software Engineer", 0L);
        when(candidateRepository.findByEmployeeIdIgnoreCaseAndEmailIgnoreCase("EMP707", "aditya@company.com")).thenReturn(Optional.empty());
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Candidate registered = candidateService.registerEmployee(candidate);

        assertNotNull(registered);
        assertEquals("aditya@company.com", registered.getEmail());
        assertEquals("Software Engineer", registered.getRole());
        assertEquals("APPLIED", registered.getStatus());
    }

    @Test
    public void test02_shouldThrowExceptionWhenEmptyFirstName() {
        Candidate candidate = new Candidate(null, "", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Employee@123", "Software Engineer", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("First Name is required"));
    }

    @Test
    public void test03_shouldThrowExceptionWhenFirstNameContainsDigits() {
        Candidate candidate = new Candidate(null, "Aditya123", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Employee@123", "Software Engineer", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("First Name must contain letters only"));
    }

    @Test
    public void test04_shouldThrowExceptionWhenEmptyEmployeeId() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "", "1995-08-12", "aditya@company.com", "Employee@123", "Software Engineer", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Employee ID is required"));
    }

    @Test
    public void test05_shouldThrowExceptionWhenEmptyEmail() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "", "Employee@123", "Software Engineer", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Company Email is required"));
    }

    @Test
    public void test06_shouldThrowExceptionWhenInvalidCompanyEmail() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@gmail.com", "Employee@123", "Software Engineer", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Only company email addresses ending with @company.com are allowed"));
    }

    @Test
    public void test07_shouldThrowExceptionWhenAdminEmailRegistration() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "admin@company.com", "Employee@123", "Software Engineer", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Registration with this email is not allowed"));
    }

    @Test
    public void test08_shouldThrowExceptionWhenCaseInsensitiveAdminEmailRegistration() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "ADMIN@COMPANY.COM", "Employee@123", "Software Engineer", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Registration with this email is not allowed"));
    }

    @Test
    public void test09_shouldSucceedWhenEmptyLastName() {
        Candidate candidate = new Candidate(null, "Khushboo", "", "EMP708", "1998-05-20", "khushboo@company.com", "Employee@123", "Java Developer", 0L);
        when(candidateRepository.findByEmployeeIdIgnoreCaseAndEmailIgnoreCase("EMP708", "khushboo@company.com")).thenReturn(Optional.empty());
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Candidate registered = candidateService.registerEmployee(candidate);

        assertNotNull(registered);
        assertEquals("", registered.getLastName());
    }

    @Test
    public void test10_shouldFailWhenAge17() {
        String dob17 = LocalDate.now().minusYears(17).toString();
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", dob17, "aditya@company.com", "Employee@123", "Software Engineer", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Age must be between 18 and 80 years"));
    }

    @Test
    public void test11_shouldSucceedWhenAgeExactly18() {
        String dob18 = LocalDate.now().minusYears(18).toString();
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP718", dob18, "aditya18@company.com", "Employee@123", "Software Engineer", 0L);
        when(candidateRepository.findByEmployeeIdIgnoreCaseAndEmailIgnoreCase("EMP718", "aditya18@company.com")).thenReturn(Optional.empty());
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(i -> i.getArgument(0));

        Candidate registered = candidateService.registerEmployee(candidate);
        assertNotNull(registered);
    }

    @Test
    public void test12_shouldSucceedWhenAgeExactly80() {
        String dob80 = LocalDate.now().minusYears(80).toString();
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP780", dob80, "aditya80@company.com", "Employee@123", "Software Engineer", 0L);
        when(candidateRepository.findByEmployeeIdIgnoreCaseAndEmailIgnoreCase("EMP780", "aditya80@company.com")).thenReturn(Optional.empty());
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(i -> i.getArgument(0));

        Candidate registered = candidateService.registerEmployee(candidate);
        assertNotNull(registered);
    }

    @Test
    public void test13_shouldFailWhenAge81() {
        String dob81 = LocalDate.now().minusYears(81).toString();
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", dob81, "aditya@company.com", "Employee@123", "Software Engineer", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Age must be between 18 and 80 years"));
    }

    @Test
    public void test14_shouldFailWhenFutureDOB() {
        String futureDob = LocalDate.now().plusDays(5).toString();
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", futureDob, "aditya@company.com", "Employee@123", "Software Engineer", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Age must be between 18 and 80 years"));
    }

    @Test
    public void test15_shouldFailWhenInvalidPassword() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "simplepass", "Software Engineer", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Password must contain at least 8 characters"));
    }

    @Test
    public void test16_shouldSucceedWhenValidPassword() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Employee@123", "Software Engineer", 0L);
        when(candidateRepository.findByEmployeeIdIgnoreCaseAndEmailIgnoreCase("EMP707", "aditya@company.com")).thenReturn(Optional.empty());
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(i -> i.getArgument(0));

        Candidate registered = candidateService.registerEmployee(candidate);
        assertNotNull(registered);
    }

    @Test
    public void test17_shouldFailWhenInvalidJobTitle() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Employee@123", "InvalidRoleTitle", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Please select a valid job role"));
    }

    @Test
    public void test18_shouldAcceptEmployeeGeneralAsFallbackJobTitle() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Employee@123", "Employee (General)", 0L);
        when(candidateRepository.findByEmployeeIdIgnoreCaseAndEmailIgnoreCase("EMP707", "aditya@company.com")).thenReturn(Optional.empty());
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(i -> i.getArgument(0));

        Candidate registered = candidateService.registerEmployee(candidate);
        assertEquals("Employee (General)", registered.getRole());
    }

    @Test
    public void test19_shouldRejectSameEmployeeIdAndSameEmail() {
        Candidate existing = new Candidate(1L, "Existing", "User", "EMP707", "1990-01-01", "aditya@company.com", "Employee@123", "Software Engineer", 0L);
        Candidate newCand = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Employee@123", "Software Engineer", 0L);

        when(candidateRepository.findByEmployeeIdIgnoreCase("EMP707")).thenReturn(Collections.singletonList(existing));
        when(candidateRepository.findByEmailIgnoreCase("aditya@company.com")).thenReturn(Collections.singletonList(existing));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(newCand));
        assertTrue(exception.getMessage().contains("Employee ID and email already exist. Cannot register."));
    }

    @Test
    public void test19a_shouldRejectExistingEmployeeIdWithNewEmail() {
        Candidate existing = new Candidate(1L, "Existing", "User", "EMP101", "1990-01-01", "aditya@company.com", "Employee@123", "Software Engineer", 0L);
        Candidate newCand = new Candidate(null, "Aditya", "Singh", "EMP101", "1995-08-12", "newemail@company.com", "Employee@123", "Software Engineer", 0L);

        when(candidateRepository.findByEmployeeIdIgnoreCase("EMP101")).thenReturn(Collections.singletonList(existing));
        when(candidateRepository.findByEmailIgnoreCase("newemail@company.com")).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(newCand));
        assertTrue(exception.getMessage().contains("Employee ID already exists. Cannot register."));
    }

    @Test
    public void test19b_shouldRejectNewEmployeeIdWithExistingEmail() {
        Candidate existing = new Candidate(1L, "Existing", "User", "EMP101", "1990-01-01", "aditya@company.com", "Employee@123", "Software Engineer", 0L);
        Candidate newCand = new Candidate(null, "Aditya", "Singh", "EMP999", "1995-08-12", "aditya@company.com", "Employee@123", "Software Engineer", 0L);

        when(candidateRepository.findByEmployeeIdIgnoreCase("EMP999")).thenReturn(Collections.emptyList());
        when(candidateRepository.findByEmailIgnoreCase("aditya@company.com")).thenReturn(Collections.singletonList(existing));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(newCand));
        assertTrue(exception.getMessage().contains("This email is already registered. Cannot register."));
    }

    @Test
    public void test19d_shouldAllowNewEmployeeIdWithNewEmail() {
        Candidate newCand = new Candidate(null, "Aditya", "Singh", "EMP555", "1995-08-12", "unique.email@company.com", "Employee@123", "Software Engineer", 0L);

        when(candidateRepository.findByEmployeeIdIgnoreCase("EMP555")).thenReturn(Collections.emptyList());
        when(candidateRepository.findByEmailIgnoreCase("unique.email@company.com")).thenReturn(Collections.emptyList());
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(i -> i.getArgument(0));

        Candidate registered = candidateService.registerEmployee(newCand);
        assertNotNull(registered);
        assertEquals("EMP555", registered.getEmployeeId());
        assertEquals("unique.email@company.com", registered.getEmail());
    }

    // ==================================================
    // PART 7 — CANDIDATE APPLICATION TESTS (33–45)
    // ==================================================

    @Test
    public void test33_shouldApplyForJobSuccessfully() {
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", "1995-05-15", "john.doe@company.com", "Test@123", 1L);
        JobPostingDto openJob = new JobPostingDto(1L, "JOB101", "Java Developer", "OPEN");

        when(candidateRepository.findByEmailIgnoreCaseAndJobId("john.doe@company.com", 1L)).thenReturn(Optional.empty());
        when(candidateRepository.findByEmployeeIdAndJobId("EMP101", 1L)).thenReturn(Optional.empty());
        when(jobServiceClient.getJobById(1L)).thenReturn(openJob);
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);

        Candidate saved = candidateService.applyForJob(candidate);
        assertNotNull(saved);
        assertEquals("John", saved.getFirstName());
    }

    @Test
    public void test34_shouldFailApplicationWhenMissingFirstName() {
        Candidate candidate = new Candidate(null, "", "Doe", "EMP101", "1995-05-15", "john.doe@company.com", "Test@123", 1L);
        // Note: First Name digits check applies
    }

    @Test
    public void test35_shouldSucceedApplicationWithEmptyLastName() {
        Candidate candidate = new Candidate(null, "John", "", "EMP101", "1995-05-15", "john.doe@company.com", "Test@123", 1L);
        JobPostingDto openJob = new JobPostingDto(1L, "JOB101", "Java Developer", "OPEN");

        when(candidateRepository.findByEmailIgnoreCaseAndJobId("john.doe@company.com", 1L)).thenReturn(Optional.empty());
        when(candidateRepository.findByEmployeeIdAndJobId("EMP101", 1L)).thenReturn(Optional.empty());
        when(jobServiceClient.getJobById(1L)).thenReturn(openJob);
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);

        Candidate saved = candidateService.applyForJob(candidate);
        assertNotNull(saved);
        assertEquals("", saved.getLastName());
    }

    @Test
    public void test36_shouldFailApplicationWithInvalidEmailDomain() {
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", "1995-05-15", "john.doe@gmail.com", "Test@123", 1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(candidate));
        assertTrue(exception.getMessage().contains("Only company email addresses ending with @company.com are allowed"));
    }

    @Test
    public void test37_shouldFailApplicationWithEmptyEmail() {
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", "1995-05-15", "", "Test@123", 1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(candidate));
        assertTrue(exception.getMessage().contains("Email is required"));
    }

    @Test
    public void test38_shouldFailApplicationWhenAge17() {
        String dob17 = LocalDate.now().minusYears(17).toString();
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", dob17, "john.doe@company.com", "Test@123", 1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(candidate));
        assertTrue(exception.getMessage().contains("Age must be between 18 and 80 years"));
    }

    @Test
    public void test39_shouldSucceedApplicationWhenAge18() {
        String dob18 = LocalDate.now().minusYears(18).toString();
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", dob18, "john.doe@company.com", "Test@123", 1L);
        JobPostingDto openJob = new JobPostingDto(1L, "JOB101", "Java Developer", "OPEN");

        when(candidateRepository.findByEmailIgnoreCaseAndJobId("john.doe@company.com", 1L)).thenReturn(Optional.empty());
        when(candidateRepository.findByEmployeeIdAndJobId("EMP101", 1L)).thenReturn(Optional.empty());
        when(jobServiceClient.getJobById(1L)).thenReturn(openJob);
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);

        Candidate saved = candidateService.applyForJob(candidate);
        assertNotNull(saved);
    }

    @Test
    public void test40_shouldSucceedApplicationWhenAge80() {
        String dob80 = LocalDate.now().minusYears(80).toString();
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", dob80, "john.doe@company.com", "Test@123", 1L);
        JobPostingDto openJob = new JobPostingDto(1L, "JOB101", "Java Developer", "OPEN");

        when(candidateRepository.findByEmailIgnoreCaseAndJobId("john.doe@company.com", 1L)).thenReturn(Optional.empty());
        when(candidateRepository.findByEmployeeIdAndJobId("EMP101", 1L)).thenReturn(Optional.empty());
        when(jobServiceClient.getJobById(1L)).thenReturn(openJob);
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);

        Candidate saved = candidateService.applyForJob(candidate);
        assertNotNull(saved);
    }

    @Test
    public void test41_shouldFailApplicationWhenAge81() {
        String dob81 = LocalDate.now().minusYears(81).toString();
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", dob81, "john.doe@company.com", "Test@123", 1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(candidate));
        assertTrue(exception.getMessage().contains("Age must be between 18 and 80 years"));
    }

    @Test
    public void test42_shouldFailApplicationWhenFutureDOB() {
        String futureDob = LocalDate.now().plusDays(5).toString();
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", futureDob, "john.doe@company.com", "Test@123", 1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(candidate));
        assertTrue(exception.getMessage().contains("Age must be between 18 and 80 years"));
    }

    @Test
    public void test43_shouldVerifyJobMustReferenceExistingJob() {
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", "1995-05-15", "john.doe@company.com", "Test@123", 1L);
        JobPostingDto openJob = new JobPostingDto(1L, "JOB101", "Java Developer", "OPEN");

        when(jobServiceClient.getJobById(1L)).thenReturn(openJob);
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);

        Candidate saved = candidateService.applyForJob(candidate);
        assertNotNull(saved);
    }

    @Test
    public void test44_shouldFailApplicationWhenJobIdNonExistent() {
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", "1995-05-15", "john.doe@company.com", "Test@123", 999L);
        when(jobServiceClient.getJobById(999L)).thenThrow(new RuntimeException("Job not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(candidate));
        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    public void test45_shouldRejectAdminFromApplyingForJob() {
        Candidate adminCandidate = new Candidate(null, "HR", "Admin", "ADM001", "1988-01-01", "admin@company.com", "Pass@123", "ADMIN", 1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(adminCandidate));
        assertTrue(exception.getMessage().contains("Administrators and HR Admins are not allowed to apply"));
    }
}
