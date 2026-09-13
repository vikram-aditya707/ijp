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
    // 1. REGISTER EMPLOYEE TESTS
    // ==================================================

    @Test
    public void shouldRegisterEmployeeSuccessfully() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Pass@123", 0L);
        when(candidateRepository.findByEmailIgnoreCase("aditya@company.com")).thenReturn(Collections.emptyList());
        when(candidateRepository.findByEmployeeId("EMP707")).thenReturn(Collections.emptyList());
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Candidate registered = candidateService.registerEmployee(candidate);

        assertNotNull(registered);
        assertEquals("aditya@company.com", registered.getEmail());
        assertEquals("APPLIED", registered.getStatus());
        verify(candidateRepository, times(1)).save(candidate);
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithMissingFirstName() {
        Candidate candidate = new Candidate(null, "", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Pass@123", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("First Name is required"));
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithMissingLastName() {
        Candidate candidate = new Candidate(null, "Aditya", " ", "EMP707", "1995-08-12", "aditya@company.com", "Pass@123", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Last Name is required"));
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithMissingDob() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "", "aditya@company.com", "Pass@123", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Date of Birth is required"));
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithMissingEmployeeId() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", null, "1995-08-12", "aditya@company.com", "Pass@123", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Employee ID is required"));
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithMissingEmail() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "", "Pass@123", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Company Email is required"));
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithMissingPassword() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", " ", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Password is required"));
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithInvalidEmailDomain() {
        Candidate candidate = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@gmail.com", "Pass@123", 0L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(candidate));
        assertTrue(exception.getMessage().contains("Only company email addresses ending with @company.com are allowed"));
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringDuplicateEmail() {
        Candidate existing = new Candidate(1L, "Existing", "User", "EMP100", "1990-01-01", "aditya@company.com", "Pass@123", 0L);
        Candidate newCand = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Pass@123", 0L);

        when(candidateRepository.findByEmailIgnoreCase("aditya@company.com")).thenReturn(List.of(existing));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(newCand));
        assertTrue(exception.getMessage().contains("An employee with this email already exists"));
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringDuplicateEmployeeId() {
        Candidate existing = new Candidate(1L, "Existing", "User", "EMP707", "1990-01-01", "other@company.com", "Pass@123", 0L);
        Candidate newCand = new Candidate(null, "Aditya", "Singh", "EMP707", "1995-08-12", "aditya@company.com", "Pass@123", 0L);

        when(candidateRepository.findByEmailIgnoreCase("aditya@company.com")).thenReturn(Collections.emptyList());
        when(candidateRepository.findByEmployeeId("EMP707")).thenReturn(List.of(existing));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.registerEmployee(newCand));
        assertTrue(exception.getMessage().contains("An employee with this employee ID already exists"));
    }

    // ==================================================
    // 2. APPLY FOR JOB TESTS
    // ==================================================

    @Test
    public void testApplyForJob_Success() {
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", "1995-05-15", "john.doe@company.com", "Test@123", 1L);
        JobPostingDto openJob = new JobPostingDto(1L, "JOB101", "Java Developer", "OPEN");

        when(candidateRepository.findByEmailIgnoreCaseAndJobId("john.doe@company.com", 1L)).thenReturn(Optional.empty());
        when(candidateRepository.findByEmployeeIdAndJobId("EMP101", 1L)).thenReturn(Optional.empty());
        when(jobServiceClient.getJobById(1L)).thenReturn(openJob);
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);

        Candidate saved = candidateService.applyForJob(candidate);

        assertNotNull(saved);
        assertEquals("John", saved.getFirstName());
        verify(candidateRepository, times(1)).save(candidate);
    }

    @Test
    public void testApplyForJob_DuplicateApplicationForSameJob() {
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", "1995-05-15", "john.doe@company.com", "Test@123", 1L);
        when(candidateRepository.findByEmailIgnoreCase("john.doe@company.com")).thenReturn(List.of(candidate));
        when(candidateRepository.findByEmployeeId("EMP101")).thenReturn(List.of(candidate));
        when(candidateRepository.findByEmailIgnoreCaseAndJobId("john.doe@company.com", 1L)).thenReturn(Optional.of(candidate));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(candidate));
        assertTrue(exception.getMessage().contains("already applied"));
    }

    @Test
    public void testApplyForJob_DuplicateEmailDifferentEmpId() {
        Candidate existing = new Candidate(1L, "John", "Doe", "EMP101", "1995-05-15", "john.doe@company.com", "Test@123", 1L);
        Candidate duplicateEmailCandidate = new Candidate(null, "Fake", "User", "EMP999", "1995-05-15", "john.doe@company.com", "Test@123", 1L);

        when(candidateRepository.findByEmailIgnoreCase("john.doe@company.com")).thenReturn(List.of(existing));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(duplicateEmailCandidate));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testApplyForJob_InvalidEmailDomain() {
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", "1995-05-15", "john.doe@gmail.com", "Test@123", 1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(candidate));
        assertTrue(exception.getMessage().contains("Only company email addresses ending with @company.com are allowed"));
    }

    @Test
    public void shouldThrowExceptionWhenApplyingWithoutEmail() {
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", "1995-05-15", null, "Test@123", 1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(candidate));
        assertTrue(exception.getMessage().contains("Email is required"));
    }

    @Test
    public void shouldThrowExceptionWhenApplyingWithoutEmployeeId() {
        Candidate candidate = new Candidate(null, "John", "Doe", "", "1995-05-15", "john.doe@company.com", "Test@123", 1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(candidate));
        assertTrue(exception.getMessage().contains("Employee ID is required"));
    }

    @Test
    public void shouldThrowExceptionWhenApplyingToNonExistentJob() {
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", "1995-05-15", "john.doe@company.com", "Test@123", 999L);
        when(candidateRepository.findByEmailIgnoreCaseAndJobId("john.doe@company.com", 999L)).thenReturn(Optional.empty());
        when(candidateRepository.findByEmployeeIdAndJobId("EMP101", 999L)).thenReturn(Optional.empty());
        when(jobServiceClient.getJobById(999L)).thenThrow(new RuntimeException("Not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(candidate));
        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    public void shouldThrowExceptionWhenApplyingToClosedJob() {
        Candidate candidate = new Candidate(null, "John", "Doe", "EMP101", "1995-05-15", "john.doe@company.com", "Test@123", 1L);
        JobPostingDto closedJob = new JobPostingDto(1L, "JOB101", "Java Developer", "CLOSED");

        when(candidateRepository.findByEmailIgnoreCaseAndJobId("john.doe@company.com", 1L)).thenReturn(Optional.empty());
        when(candidateRepository.findByEmployeeIdAndJobId("EMP101", 1L)).thenReturn(Optional.empty());
        when(jobServiceClient.getJobById(1L)).thenReturn(closedJob);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.applyForJob(candidate));
        assertTrue(exception.getMessage().contains("is CLOSED"));
    }

    // ==================================================
    // 3. EMPLOYEE LOGIN TESTS
    // ==================================================

    @Test
    public void testLoginEmployee_Success() {
        Candidate candidate = new Candidate(1L, "Naman", "Dheer", "E901", "1995-01-01", "naman@company.com", "Test@123", 1L);
        when(candidateRepository.findByEmailIgnoreCase("naman@company.com")).thenReturn(List.of(candidate));

        Candidate loggedIn = candidateService.loginEmployee("naman@company.com", "Test@123");

        assertNotNull(loggedIn);
        assertEquals("naman@company.com", loggedIn.getEmail());
    }

    @Test
    public void testLoginEmployee_WrongPassword() {
        Candidate candidate = new Candidate(1L, "Naman", "Dheer", "E901", "1995-01-01", "naman@company.com", "Test@123", 1L);
        when(candidateRepository.findByEmailIgnoreCase("naman@company.com")).thenReturn(List.of(candidate));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.loginEmployee("naman@company.com", "Wrong123"));
        assertTrue(exception.getMessage().contains("Invalid email or password"));
    }

    @Test
    public void testLoginEmployee_InvalidDomain() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.loginEmployee("naman@gmail.com", "Test@123"));
        assertTrue(exception.getMessage().contains("Only company email addresses ending with @company.com are allowed"));
    }

    @Test
    public void shouldThrowExceptionWhenLoginEmailMissing() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.loginEmployee("", "Test@123"));
        assertTrue(exception.getMessage().contains("Company Email is required"));
    }

    @Test
    public void shouldThrowExceptionWhenLoginPasswordMissing() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.loginEmployee("naman@company.com", ""));
        assertTrue(exception.getMessage().contains("Invalid email or password"));
    }

    @Test
    public void shouldThrowExceptionWhenLoginEmployeeNotFound() {
        when(candidateRepository.findByEmailIgnoreCase("unknown@company.com")).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.loginEmployee("unknown@company.com", "Test@123"));
        assertTrue(exception.getMessage().contains("Invalid email or password"));
    }

    // ==================================================
    // 4. CANDIDATE RETRIEVAL & DELETION TESTS
    // ==================================================

    @Test
    public void shouldGetAllCandidatesSuccessfully() {
        Candidate c1 = new Candidate(1L, "A", "B", "E1", "1990-01-01", "a@company.com", "Pass@123", 1L);
        when(candidateRepository.findAll()).thenReturn(List.of(c1));

        List<Candidate> result = candidateService.getAllCandidates();

        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getFirstName());
    }

    @Test
    public void shouldGetCandidateByIdSuccessfully() {
        Candidate c1 = new Candidate(1L, "A", "B", "E1", "1990-01-01", "a@company.com", "Pass@123", 1L);
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(c1));

        Optional<Candidate> result = candidateService.getCandidateById(1L);

        assertTrue(result.isPresent());
        assertEquals("A", result.get().getFirstName());
    }

    @Test
    public void shouldReturnEmptyWhenCandidateNotFoundById() {
        when(candidateRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Candidate> result = candidateService.getCandidateById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testDeleteCandidate_Success() {
        Candidate candidate = new Candidate(10L, "Jane", "Doe", "EMP202", "1996-06-16", "jane.doe@company.com", "Test@123", 1L);
        when(candidateRepository.findById(10L)).thenReturn(Optional.of(candidate));

        candidateService.deleteCandidate(10L);

        verify(notificationRepository, times(1)).deleteByCandidateId(10L);
        verify(interviewRepository, times(1)).deleteByCandidateId(10L);
        verify(candidateRepository, times(1)).deleteById(10L);
    }

    @Test
    public void shouldThrowExceptionWhenDeletingNonExistentCandidate() {
        when(candidateRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.deleteCandidate(99L));
        assertTrue(exception.getMessage().contains("not found"));
    }

    // ==================================================
    // 5. STATUS UPDATE & NOTIFICATION TESTS
    // ==================================================

    @Test
    public void shouldUpdateCandidateStatusToShortlistedSuccessfully() {
        Candidate candidate = new Candidate(1L, "A", "B", "E1", "1990-01-01", "a@company.com", "Pass@123", 10L, "APPLIED");
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(i -> i.getArgument(0));

        Candidate updated = candidateService.updateCandidateStatus(1L, "SHORTLISTED");

        assertEquals("SHORTLISTED", updated.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void shouldThrowExceptionWhenUpdatingStatusOfNonExistentCandidate() {
        when(candidateRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.updateCandidateStatus(99L, "SHORTLISTED"));
        assertTrue(exception.getMessage().contains("not found"));
    }

    // ==================================================
    // 6. INTERVIEW SCHEDULING TESTS
    // ==================================================

    @Test
    public void shouldScheduleOnlineInterviewSuccessfully() {
        Candidate candidate = new Candidate(1L, "A", "B", "E1", "1990-01-01", "a@company.com", "Pass@123", 10L);
        Interview interview = new Interview(null, 1L, 1L, 10L, "ONLINE", "2026-10-01", "10:00 AM", null, "https://meet.google.com/abc", "HR Lead", "SCHEDULED");

        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(i -> i.getArgument(0));

        Interview saved = candidateService.scheduleInterview(interview);

        assertNotNull(saved);
        assertEquals("ONLINE", saved.getInterviewMode());
        assertEquals("INTERVIEW_SCHEDULED", candidate.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void shouldScheduleOfflineInterviewSuccessfully() {
        Candidate candidate = new Candidate(1L, "A", "B", "E1", "1990-01-01", "a@company.com", "Pass@123", 10L);
        Interview interview = new Interview(null, 1L, 1L, 10L, "OFFLINE", "2026-10-01", "10:00 AM", "Room 402", null, "HR Lead", "SCHEDULED");

        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(i -> i.getArgument(0));

        Interview saved = candidateService.scheduleInterview(interview);

        assertNotNull(saved);
        assertEquals("OFFLINE", saved.getInterviewMode());
        assertEquals("Room 402", saved.getLocation());
    }

    @Test
    public void shouldThrowExceptionWhenSchedulingInterviewWithoutCandidateId() {
        Interview interview = new Interview();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.scheduleInterview(interview));
        assertTrue(exception.getMessage().contains("Candidate ID must be provided"));
    }

    @Test
    public void shouldThrowExceptionWhenSchedulingOnlineInterviewWithoutMeetingLink() {
        Candidate candidate = new Candidate(1L, "A", "B", "E1", "1990-01-01", "a@company.com", "Pass@123", 10L);
        Interview interview = new Interview(null, 1L, 1L, 10L, "ONLINE", "2026-10-01", "10:00 AM", null, "", "HR Lead", "SCHEDULED");

        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.scheduleInterview(interview));
        assertTrue(exception.getMessage().contains("Meeting Link is required"));
    }

    @Test
    public void shouldThrowExceptionWhenSchedulingOfflineInterviewWithoutLocation() {
        Candidate candidate = new Candidate(1L, "A", "B", "E1", "1990-01-01", "a@company.com", "Pass@123", 10L);
        Interview interview = new Interview(null, 1L, 1L, 10L, "OFFLINE", "2026-10-01", "10:00 AM", "", null, "HR Lead", "SCHEDULED");

        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.scheduleInterview(interview));
        assertTrue(exception.getMessage().contains("Meeting Room Name / Location is required"));
    }

    // ==================================================
    // 7. NOTIFICATION MANAGEMENT TESTS
    // ==================================================

    @Test
    public void shouldMarkNotificationAsReadSuccessfully() {
        Notification notification = new Notification(1L, 5L, "Title", "Message", "TYPE", false, "2026-09-01");
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        Notification readNotif = candidateService.markNotificationAsRead(1L);

        assertTrue(readNotif.isRead());
    }

    @Test
    public void shouldThrowExceptionWhenMarkingNonExistentNotificationAsRead() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> candidateService.markNotificationAsRead(99L));
        assertTrue(exception.getMessage().contains("not found"));
    }
}
