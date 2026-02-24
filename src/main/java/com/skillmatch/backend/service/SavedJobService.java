package com.skillmatch.backend.service;

import com.skillmatch.backend.dto.SavedJobRequest;
import com.skillmatch.backend.dto.SavedJobResponse;
import com.skillmatch.backend.model.Job;
import com.skillmatch.backend.model.SavedJob;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.repository.JobRepository;
import com.skillmatch.backend.repository.SavedJobRepository;
import com.skillmatch.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedJobService {
    
    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    @Transactional
    public SavedJobResponse saveJob(Long userId, SavedJobRequest request) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Job job = jobRepository.findById(Objects.requireNonNull(request.getJobId()))
                .orElseThrow(() -> new RuntimeException("Oferta de trabajo no encontrada"));
        
        // Verificar si ya está guardado
        if (savedJobRepository.existsByUserIdAndJobId(userId, request.getJobId())) {
            throw new RuntimeException("Esta oferta ya está guardada");
        }
        
        SavedJob savedJob = new SavedJob();
        savedJob.setUser(user);
        savedJob.setJob(job);
        savedJob.setNotes(request.getNotes());
        
        savedJob = savedJobRepository.save(savedJob);
        
        return mapToResponse(savedJob);
    }
    
    @Transactional
    public void unsaveJob(Long userId, Long jobId) {
        if (!savedJobRepository.existsByUserIdAndJobId(userId, jobId)) {
            throw new RuntimeException("Esta oferta no está guardada");
        }
        savedJobRepository.deleteByUserIdAndJobId(userId, jobId);
    }
    
    @Transactional(readOnly = true)
    public List<SavedJobResponse> getUserSavedJobs(Long userId) {
        List<SavedJob> savedJobs = savedJobRepository.findByUserIdOrderBySavedAtDesc(userId);
        return savedJobs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public boolean isJobSaved(Long userId, Long jobId) {
        return savedJobRepository.existsByUserIdAndJobId(userId, jobId);
    }
    
    @Transactional(readOnly = true)
    public List<Long> getSavedJobIds(Long userId) {
        return savedJobRepository.findJobIdsByUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public Long getSavedJobsCount(Long userId) {
        return savedJobRepository.countByUserId(userId);
    }
    
    @Transactional
    public SavedJobResponse updateNotes(Long userId, Long jobId, String notes) {
        SavedJob savedJob = savedJobRepository.findByUserIdAndJobId(userId, jobId)
                .orElseThrow(() -> new RuntimeException("Esta oferta no está guardada"));
        
        savedJob.setNotes(notes);
        savedJob = savedJobRepository.save(savedJob);
        
        return mapToResponse(savedJob);
    }
    
    private SavedJobResponse mapToResponse(SavedJob savedJob) {
        Job job = savedJob.getJob();
        SavedJobResponse response = new SavedJobResponse();
        response.setId(savedJob.getId());
        response.setUserId(savedJob.getUser().getId());
        response.setJobId(job.getId());
        response.setJobTitle(job.getTitle());
        response.setCompanyName(job.getCompany().getName());
        response.setJobType(job.getType());
        response.setLocation(job.getLocation());
        String salaryRange = null;
        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
            salaryRange = "$" + job.getSalaryMin() + " - $" + job.getSalaryMax();
        }
        response.setSalaryRange(salaryRange);
        response.setSavedAt(savedJob.getSavedAt());
        response.setNotes(savedJob.getNotes());
        return response;
    }
}
