package com.example.candidateservice.service;

import com.example.candidateservice.client.JobServiceClient;
import com.example.candidateservice.dto.JobPostingDto;
import com.example.candidateservice.entity.Candidate;
import com.example.candidateservice.entity.Interview;
import com.example.candidateservice.entity.Notification;
import com.example.candidateservice.repository.CandidateRepository;
import com.example.candidateservice.repository.InterviewRepository;
import com.example.candidateservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobServiceClient jobServiceClient;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static final List<String> VALID_JOB_ROLES = Arrays.asList(
            "Software Engineer",
            "Software Developer",
            "Java Developer",
            "Java Backend Developer",
            "Java Full Stack Developer",
            "Python Developer",
            ".NET Developer",
            "C# Developer",
            "Angular Developer",
            "React Developer",
            "Frontend Developer",
            "Backend Developer",
            "Full Stack Developer",
            "QA Engineer",
            "Test Engineer",
            "Automation Test Engineer",
            "DevOps Engineer",
            "Cloud Engineer",
            "Data Analyst",
            "Data Engineer",
            "Data Scientist",
            "Database Administrator",
            "UI/UX Developer",
            "Business Analyst",
            "System Engineer",
            "Network Engineer",
            "Cyber Security Engineer",
            "Machine Learning Engineer",
            "AI Engineer",
            "Technical Support Engineer",
            "Project Engineer",
            "Employee (General)",
            "EMPLOYEE"
    );

    public void validatePasswordPolicy(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password is required!");
        }
        String pass = password.trim();
        if (pass.length() < 8) {
            throw new RuntimeException("Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number and one special character.");
        }
        boolean hasUpper = pass.matches(".*[A-Z].*");
        boolean hasLower = pass.matches(".*[a-z].*");
        boolean hasDigit = pass.matches(".*[0-9].*");
        boolean hasSpecial = pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            throw new RuntimeException("Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number and one special character.");
        }
    }

    public void validateDateOfBirth(String dob) {
        if (dob == null || dob.trim().isEmpty()) {
            throw new RuntimeException("Date of Birth is required!");
        }
        try {
            LocalDate birthDate = LocalDate.parse(dob.trim());
            LocalDate today = LocalDate.now();
            if (birthDate.isAfter(today)) {
                throw new RuntimeException("Age must be between 18 and 80 years.");
            }
            int age = Period.between(birthDate, today).getYears();
            if (age < 18 || age > 80) {
                throw new RuntimeException("Age must be between 18 and 80 years.");
            }
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Age must be between 18 and 80 years.");
        }
    }

    public Candidate registerEmployee(Candidate candidate) {
        if (candidate.getFirstName() == null || candidate.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("First Name is required!");
        }
        String firstName = candidate.getFirstName().trim();
        if (firstName.matches(".*\\d.*")) {
            throw new RuntimeException("First Name must contain letters only and cannot contain numbers.");
        }
        if ("admin".equalsIgnoreCase(firstName)) {
            throw new RuntimeException("Registration with this email is not allowed.");
        }

        String lastName = candidate.getLastName() != null ? candidate.getLastName().trim() : "";
        if (!lastName.isEmpty() && lastName.matches(".*\\d.*")) {
            throw new RuntimeException("Last Name must contain letters only and cannot contain numbers.");
        }

        if (candidate.getRole() == null || candidate.getRole().trim().isEmpty()) {
            throw new RuntimeException("Please select a valid job role.");
        }
        String role = candidate.getRole().trim();
        boolean isValidRole = VALID_JOB_ROLES.stream().anyMatch(r -> r.equalsIgnoreCase(role));
        if (!isValidRole) {
            throw new RuntimeException("Please select a valid job role.");
        }

        validateDateOfBirth(candidate.getDob());

        if (candidate.getEmployeeId() == null || candidate.getEmployeeId().trim().isEmpty()) {
            throw new RuntimeException("Employee ID is required!");
        }
        String empId = candidate.getEmployeeId().trim();

        if (candidate.getEmail() == null || candidate.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Company Email is required!");
        }
        String email = candidate.getEmail().trim();

        if (!email.toLowerCase().endsWith("@company.com")) {
            throw new RuntimeException("Only company email addresses ending with @company.com are allowed.");
        }
        if ("admin@company.com".equalsIgnoreCase(email)) {
            throw new RuntimeException("Registration with this email is not allowed.");
        }

        validatePasswordPolicy(candidate.getPassword());

        boolean empIdExists = !candidateRepository.findByEmployeeIdIgnoreCase(empId).isEmpty();
        boolean emailExists = !candidateRepository.findByEmailIgnoreCase(email).isEmpty();

        if (empIdExists && emailExists) {
            throw new RuntimeException("Employee ID and email already exist. Cannot register.");
        } else if (empIdExists) {
            throw new RuntimeException("Employee ID already exists. Cannot register.");
        } else if (emailExists) {
            throw new RuntimeException("This email is already registered. Cannot register.");
        }

        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);
        candidate.setRole(role);
        candidate.setDob(candidate.getDob().trim());
        candidate.setEmployeeId(empId);
        candidate.setEmail(email);
        candidate.setPassword(passwordEncoder.encode(candidate.getPassword().trim()));
        if (candidate.getJobId() == null) {
            candidate.setJobId(0L);
        }
        candidate.setStatus("APPLIED");

        return candidateRepository.save(candidate);
    }

    public Candidate applyForJob(Candidate candidate) {
        if (candidate.getEmail() == null || candidate.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required!");
        }

        String email = candidate.getEmail().trim();

        // 1. Company Email Validation (*@company.com)
        if (!email.toLowerCase().endsWith("@company.com")) {
            throw new RuntimeException("Only company email addresses ending with @company.com are allowed.");
        }

        // 2. Reject Admin Application Submission
        if ("admin@company.com".equalsIgnoreCase(email) || (candidate.getRole() != null && "ADMIN".equalsIgnoreCase(candidate.getRole().trim()))) {
            throw new RuntimeException("Administrators and HR Admins are not allowed to apply for job postings.");
        }

        if (candidate.getEmployeeId() == null || candidate.getEmployeeId().trim().isEmpty()) {
            throw new RuntimeException("Employee ID is required!");
        }

        if (candidate.getFirstName() != null && candidate.getFirstName().matches(".*\\d.*")) {
            throw new RuntimeException("First Name must contain letters only and cannot contain numbers.");
        }

        if (candidate.getLastName() != null && candidate.getLastName().matches(".*\\d.*")) {
            throw new RuntimeException("Last Name must contain letters only and cannot contain numbers.");
        }

        if (candidate.getDob() != null && !candidate.getDob().trim().isEmpty()) {
            validateDateOfBirth(candidate.getDob());
        }

        String empId = candidate.getEmployeeId().trim();
        Long jobId = candidate.getJobId();
        String password = candidate.getPassword();

        if (password != null) {
            password = password.trim();
        }

        // Check if an employee profile already exists in system by email OR by employee ID
        List<Candidate> existingByEmail = candidateRepository.findByEmailIgnoreCase(email);
        List<Candidate> existingByEmpId = candidateRepository.findByEmployeeId(empId);

        // Identity validation: Email and Employee ID must belong to the same employee!
        if (!existingByEmail.isEmpty() && !existingByEmpId.isEmpty()) {
            String emailForEmpId = existingByEmpId.get(0).getEmail();
            String empIdForEmail = existingByEmail.get(0).getEmployeeId();
            if (!email.equalsIgnoreCase(emailForEmpId) || !empId.equalsIgnoreCase(empIdForEmail)) {
                throw new RuntimeException("An employee with this email or employee ID already exists.");
            }
        } else if (!existingByEmail.isEmpty()) {
            String existingEmpId = existingByEmail.get(0).getEmployeeId();
            if (!empId.equalsIgnoreCase(existingEmpId)) {
                throw new RuntimeException("An employee with this email or employee ID already exists.");
            }
        } else if (!existingByEmpId.isEmpty()) {
            String existingEmail = existingByEmpId.get(0).getEmail();
            if (!email.equalsIgnoreCase(existingEmail)) {
                throw new RuntimeException("An employee with this email or employee ID already exists.");
            }
        }

        // Preserve candidate password & role if candidate profile already exists
        if (!existingByEmail.isEmpty() || !existingByEmpId.isEmpty()) {
            Candidate existingCand = !existingByEmail.isEmpty() ? existingByEmail.get(0) : existingByEmpId.get(0);
            if ((password == null || password.isEmpty()) && existingCand.getPassword() != null) {
                candidate.setPassword(existingCand.getPassword());
            } else if (password != null && !password.isEmpty()) {
                candidate.setPassword(password);
            }
            if ((candidate.getRole() == null || candidate.getRole().trim().isEmpty()) && existingCand.getRole() != null) {
                candidate.setRole(existingCand.getRole());
            }
        } else {
            // New candidate account requires password
            if (password == null || password.isEmpty()) {
                throw new RuntimeException("Password is required for new candidates!");
            }
            candidate.setPassword(password);
        }

        // 2. Standalone Candidate Creation vs Job Application
        if (jobId == null || jobId == 0L) {
            if (!existingByEmail.isEmpty() || !existingByEmpId.isEmpty()) {
                throw new RuntimeException("An employee with this email or employee ID already exists.");
            }
        } else {
            // 3. Job Application: Check duplicate application for THIS specific job
            Optional<Candidate> existingAppByEmail = candidateRepository.findByEmailIgnoreCaseAndJobId(email, jobId);
            if (existingAppByEmail.isPresent()) {
                throw new RuntimeException("You have already applied for this job.");
            }

            Optional<Candidate> existingAppByEmp = candidateRepository.findByEmployeeIdAndJobId(empId, jobId);
            if (existingAppByEmp.isPresent()) {
                throw new RuntimeException("You have already applied for this job.");
            }

            // Verify job existence and OPEN status via JobServiceClient
            JobPostingDto job = null;
            try {
                job = jobServiceClient.getJobById(jobId);
            } catch (Exception e) {
                throw new RuntimeException("Job with ID " + jobId + " does not exist!");
            }

            if (job == null) {
                throw new RuntimeException("Job with ID " + jobId + " does not exist!");
            }

            if (!"OPEN".equalsIgnoreCase(job.getStatus())) {
                throw new RuntimeException("Cannot apply: Job with ID " + jobId + " is CLOSED!");
            }
        }

        if (candidate.getStatus() == null || candidate.getStatus().trim().isEmpty()) {
            candidate.setStatus("APPLIED");
        }

        candidate.setEmail(email);
        candidate.setEmployeeId(empId);

        // 4. Save candidate application
        return candidateRepository.save(candidate);
    }

    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    public Optional<Candidate> getCandidateById(Long id) {
        return candidateRepository.findById(id);
    }

    public List<Candidate> getCandidatesByJobId(Long jobId) {
        return candidateRepository.findByJobId(jobId);
    }

    public List<Candidate> getCandidatesByEmail(String email) {
        if (email == null) return Collections.emptyList();
        return candidateRepository.findByEmailIgnoreCase(email.trim()).stream()
                .filter(c -> c.getJobId() != null && c.getJobId() > 0L)
                .collect(Collectors.toList());
    }

    public List<Candidate> getCandidatesByEmployeeId(String employeeId) {
        if (employeeId == null) return Collections.emptyList();
        return candidateRepository.findByEmployeeId(employeeId.trim()).stream()
                .filter(c -> c.getJobId() != null && c.getJobId() > 0L)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCandidate(Long id) {
        Optional<Candidate> optionalCandidate = candidateRepository.findById(id);
        if (optionalCandidate.isPresent()) {
            notificationRepository.deleteByCandidateId(id);
            interviewRepository.deleteByCandidateId(id);
            candidateRepository.deleteById(id);
        } else {
            throw new RuntimeException("Candidate record with ID " + id + " not found!");
        }
    }

    public Candidate updateCandidateStatus(Long candidateId, String status) {
        Optional<Candidate> optionalCandidate = candidateRepository.findById(candidateId);
        if (optionalCandidate.isPresent()) {
            Candidate candidate = optionalCandidate.get();
            candidate.setStatus(status);
            Candidate saved = candidateRepository.save(candidate);

            createStatusNotification(saved, status);
            return saved;
        }
        throw new RuntimeException("Candidate with ID " + candidateId + " not found!");
    }

    private void createStatusNotification(Candidate candidate, String status) {
        String jobTitle = "Job #" + candidate.getJobId();
        try {
            JobPostingDto job = jobServiceClient.getJobById(candidate.getJobId());
            if (job != null && job.getDesignation() != null) {
                jobTitle = job.getDesignation();
            }
        } catch (Exception e) {
            // fallback
        }

        String title = "";
        String message = "";
        String type = status;

        if ("SHORTLISTED".equalsIgnoreCase(status)) {
            title = "Profile Shortlisted";
            message = "Your profile has been shortlisted for " + jobTitle + ".";
        } else if ("SELECTED".equalsIgnoreCase(status)) {
            title = "Application Selected";
            message = "Congratulations! You have been selected for the position of " + jobTitle + ".";
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            title = "Application Update";
            message = "Thank you for your interest. Your application for " + jobTitle + " was not selected at this time.";
        } else {
            return;
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Notification notification = new Notification(null, candidate.getId(), title, message, type, false, now);
        notificationRepository.save(notification);
    }

    public Interview scheduleInterview(Interview interview) {
        if (interview.getCandidateId() == null) {
            throw new RuntimeException("Candidate ID must be provided!");
        }

        Optional<Candidate> optionalCandidate = candidateRepository.findById(interview.getCandidateId());
        if (optionalCandidate.isEmpty()) {
            throw new RuntimeException("Candidate with ID " + interview.getCandidateId() + " not found!");
        }

        Candidate candidate = optionalCandidate.get();
        if (interview.getJobId() == null) {
            interview.setJobId(candidate.getJobId());
        }

        String mode = interview.getInterviewMode();
        if (mode == null || mode.trim().isEmpty()) {
            mode = "OFFLINE";
            interview.setInterviewMode(mode);
        }

        if ("ONLINE".equalsIgnoreCase(mode)) {
            if (interview.getMeetingLink() == null || interview.getMeetingLink().trim().isEmpty()) {
                throw new RuntimeException("Meeting Link is required for ONLINE interviews!");
            }
            interview.setLocation(null);
        } else if ("OFFLINE".equalsIgnoreCase(mode)) {
            if (interview.getLocation() == null || interview.getLocation().trim().isEmpty()) {
                throw new RuntimeException("Meeting Room Name / Location is required for OFFLINE interviews!");
            }
            interview.setMeetingLink(null);
        }

        interview.setStatus("SCHEDULED");
        Interview savedInterview = interviewRepository.save(interview);

        candidate.setStatus("INTERVIEW_SCHEDULED");
        candidateRepository.save(candidate);

        String jobTitle = "Job #" + candidate.getJobId();
        try {
            JobPostingDto job = jobServiceClient.getJobById(candidate.getJobId());
            if (job != null && job.getDesignation() != null) {
                jobTitle = job.getDesignation();
            }
        } catch (Exception e) {
            // fallback
        }

        String title = "Interview Scheduled";
        StringBuilder sb = new StringBuilder();
        sb.append("Your interview for ").append(jobTitle)
          .append(" is scheduled on ").append(interview.getInterviewDate());
        if (interview.getInterviewTime() != null && !interview.getInterviewTime().trim().isEmpty()) {
            sb.append(" at ").append(interview.getInterviewTime());
        }

        if (interview.getInterviewer() != null && !interview.getInterviewer().trim().isEmpty()) {
            sb.append(". Interviewer: ").append(interview.getInterviewer());
        }

        if ("ONLINE".equalsIgnoreCase(mode)) {
            sb.append(". Mode: ONLINE. Meeting Link: ").append(interview.getMeetingLink()).append(".");
        } else {
            sb.append(". Mode: OFFLINE. Room Location: ").append(interview.getLocation()).append(".");
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Notification notification = new Notification(null, candidate.getId(), title, sb.toString(), "INTERVIEW_SCHEDULED", false, now);
        notificationRepository.save(notification);

        return savedInterview;
    }

    public Candidate loginEmployee(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Company Email is required!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Invalid email or password.");
        }

        String emailTrimmed = email.trim();
        String passTrimmed = password.trim();

        // 1. Reject admin accounts from candidate login
        if ("admin@company.com".equalsIgnoreCase(emailTrimmed)) {
            throw new RuntimeException("Admin accounts cannot log in through employee login.");
        }

        // 2. Employee login requires a valid company email address
        if (!emailTrimmed.contains("@") || !emailTrimmed.toLowerCase().endsWith("@company.com")) {
            throw new RuntimeException("Only company email addresses ending with @company.com are allowed.");
        }

        // 3. Query strictly by EMAIL ONLY
        List<Candidate> list = candidateRepository.findByEmailIgnoreCase(emailTrimmed);

        if (list.isEmpty()) {
            throw new RuntimeException("Invalid email or password.");
        }

        for (Candidate cand : list) {
            if ("ADMIN".equalsIgnoreCase(cand.getRole())) {
                throw new RuntimeException("Admin accounts cannot log in through employee login.");
            }
            if (cand.getPassword() == null || cand.getPassword().trim().isEmpty()) {
                cand.setPassword(passwordEncoder.encode(passTrimmed));
                candidateRepository.save(cand);
                return cand;
            } else if (passwordEncoder.matches(passTrimmed, cand.getPassword()) || cand.getPassword().trim().equals(passTrimmed)) {
                if (cand.getPassword().trim().equals(passTrimmed) && !cand.getPassword().startsWith("$2a$")) {
                    cand.setPassword(passwordEncoder.encode(passTrimmed));
                    candidateRepository.save(cand);
                }
                return cand;
            }
        }

        throw new RuntimeException("Invalid email or password.");
    }

    public List<Interview> getInterviewsByCandidateId(Long candidateId) {
        return interviewRepository.findByCandidateId(candidateId);
    }

    public List<Notification> getNotificationsForEmployee(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String query = identifier.trim();
        List<Candidate> candidates = candidateRepository.findByEmailIgnoreCase(query);
        if (candidates.isEmpty()) {
            candidates = candidateRepository.findByEmployeeId(query);
        }
        if (candidates.isEmpty()) {
            try {
                Long cId = Long.parseLong(query);
                Optional<Candidate> cOpt = candidateRepository.findById(cId);
                if (cOpt.isPresent()) {
                    candidates = candidateRepository.findByEmailIgnoreCase(cOpt.get().getEmail());
                    if (candidates.isEmpty()) {
                        candidates = candidateRepository.findByEmployeeId(cOpt.get().getEmployeeId());
                    }
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> candidateIds = candidates.stream().map(Candidate::getId).collect(Collectors.toList());
        return notificationRepository.findByCandidateIdInOrderByIdDesc(candidateIds);
    }

    public long getUnreadNotificationCountForEmployee(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return 0L;
        }
        String query = identifier.trim();
        List<Candidate> candidates = candidateRepository.findByEmailIgnoreCase(query);
        if (candidates.isEmpty()) {
            candidates = candidateRepository.findByEmployeeId(query);
        }
        if (candidates.isEmpty()) {
            try {
                Long cId = Long.parseLong(query);
                Optional<Candidate> cOpt = candidateRepository.findById(cId);
                if (cOpt.isPresent()) {
                    candidates = candidateRepository.findByEmailIgnoreCase(cOpt.get().getEmail());
                    if (candidates.isEmpty()) {
                        candidates = candidateRepository.findByEmployeeId(cOpt.get().getEmployeeId());
                    }
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (candidates.isEmpty()) {
            return 0L;
        }

        List<Long> candidateIds = candidates.stream().map(Candidate::getId).collect(Collectors.toList());
        return notificationRepository.countByCandidateIdInAndIsReadFalse(candidateIds);
    }

    public List<Notification> getNotificationsForCandidate(Long candidateId) {
        return getNotificationsForEmployee(candidateId.toString());
    }

    public long getUnreadNotificationCount(Long candidateId) {
        return getUnreadNotificationCountForEmployee(candidateId.toString());
    }

    public Notification markNotificationAsRead(Long notificationId) {
        Optional<Notification> optional = notificationRepository.findById(notificationId);
        if (optional.isPresent()) {
            Notification notification = optional.get();
            notification.setRead(true);
            return notificationRepository.save(notification);
        }
        throw new RuntimeException("Notification not found!");
    }
}
