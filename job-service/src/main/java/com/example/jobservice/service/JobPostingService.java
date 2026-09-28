package com.example.jobservice.service;

import com.example.jobservice.entity.JobPosting;
import com.example.jobservice.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobPostingService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    public synchronized String generateNextJobCode() {
        List<JobPosting> allJobs = jobPostingRepository.findAll();
        int maxCodeNum = 100;
        for (JobPosting job : allJobs) {
            if (job.getJobId() != null && job.getJobId().trim().toUpperCase().startsWith("JOB")) {
                String numPart = job.getJobId().trim().substring(3);
                try {
                    int num = Integer.parseInt(numPart);
                    if (num > maxCodeNum) {
                        maxCodeNum = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore non-numeric job code suffixes
                }
            }
        }
        return "JOB" + (maxCodeNum + 1);
    }

    public JobPosting createJob(JobPosting jobPosting) {
        if (jobPosting.getDesignation() == null || jobPosting.getDesignation().trim().isEmpty()) {
            throw new RuntimeException("Designation / Role is required.");
        }
        if (jobPosting.getDescription() == null || jobPosting.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Job description is required.");
        }
        if (jobPosting.getLocation() == null || jobPosting.getLocation().trim().isEmpty()) {
            throw new RuntimeException("Location is required.");
        }
        if (jobPosting.getExperience() == null || jobPosting.getExperience().trim().isEmpty()) {
            throw new RuntimeException("Required experience is required.");
        }
        if (jobPosting.getSkillSet() == null || jobPosting.getSkillSet().trim().isEmpty()) {
            throw new RuntimeException("Required skill set is required.");
        }
        if (jobPosting.getSalaryMin() == null || jobPosting.getSalaryMin() <= 0) {
            throw new RuntimeException("Minimum salary is required.");
        }
        if (jobPosting.getSalaryMax() == null || jobPosting.getSalaryMax() <= 0) {
            throw new RuntimeException("Maximum salary is required.");
        }

        // Auto-generate Job Code (backend source of truth)
        jobPosting.setJobId(generateNextJobCode());

        if (jobPosting.getStatus() == null || jobPosting.getStatus().trim().isEmpty()) {
            jobPosting.setStatus("OPEN");
        }
        return jobPostingRepository.save(jobPosting);
    }

    public List<JobPosting> getAllJobs() {
        return jobPostingRepository.findAll();
    }

    public List<JobPosting> getOpenJobs() {
        return jobPostingRepository.findByStatus("OPEN");
    }

    public Optional<JobPosting> getJobById(Long id) {
        return jobPostingRepository.findById(id);
    }

    public void deleteJob(Long id) {
        Optional<JobPosting> optionalJob = jobPostingRepository.findById(id);
        if (optionalJob.isPresent()) {
            jobPostingRepository.deleteById(id);
        } else {
            throw new RuntimeException("Job posting with ID " + id + " not found!");
        }
    }

    public JobPosting updateJob(Long id, JobPosting updatedJob) {
        Optional<JobPosting> optionalJob = jobPostingRepository.findById(id);
        if (optionalJob.isPresent()) {
            JobPosting existingJob = optionalJob.get();
            existingJob.setDescription(updatedJob.getDescription());
            existingJob.setDesignation(updatedJob.getDesignation());
            existingJob.setLocation(updatedJob.getLocation());
            existingJob.setSkillSet(updatedJob.getSkillSet());
            existingJob.setExperience(updatedJob.getExperience());
            existingJob.setSalaryMin(updatedJob.getSalaryMin());
            existingJob.setSalaryMax(updatedJob.getSalaryMax());
            if (updatedJob.getStatus() != null) {
                existingJob.setStatus(updatedJob.getStatus());
            }
            return jobPostingRepository.save(existingJob);
        }
        return null;
    }

    public JobPosting closeJob(Long id) {
        Optional<JobPosting> optionalJob = jobPostingRepository.findById(id);
        if (optionalJob.isPresent()) {
            JobPosting existingJob = optionalJob.get();
            existingJob.setStatus("CLOSED");
            return jobPostingRepository.save(existingJob);
        }
        return null;
    }
}
