package com.example.candidateservice.repository;

import com.example.candidateservice.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    List<Candidate> findByEmail(String email);

    List<Candidate> findByEmailIgnoreCase(String email);

    List<Candidate> findByEmployeeId(String employeeId);

    List<Candidate> findByEmployeeIdIgnoreCase(String employeeId);

    Optional<Candidate> findByEmployeeIdIgnoreCaseAndEmailIgnoreCase(String employeeId, String email);

    Optional<Candidate> findByEmailAndJobId(String email, Long jobId);

    Optional<Candidate> findByEmailIgnoreCaseAndJobId(String email, Long jobId);

    Optional<Candidate> findByEmployeeIdAndJobId(String employeeId, Long jobId);

    List<Candidate> findByJobId(Long jobId);
}
