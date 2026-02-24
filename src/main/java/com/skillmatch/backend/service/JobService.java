package com.skillmatch.backend.service;

import com.skillmatch.backend.dto.JobRequest;
import com.skillmatch.backend.dto.JobResponse;
import com.skillmatch.backend.model.Company;
import com.skillmatch.backend.model.Job;
import com.skillmatch.backend.repository.CompanyRepository;
import com.skillmatch.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {
    
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    
    @Transactional
    public JobResponse createJob(JobRequest request) {
        // Validar que la compañía exista
        Long companyId = request.getCompanyId();
        if (companyId == null) {
            throw new RuntimeException("El ID de la compañía no puede ser nulo");
        }
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Compañía no encontrada con ID: " + companyId));
        
        // Validar rango salarial
        if (request.getSalaryMin() != null && request.getSalaryMax() != null) {
            if (request.getSalaryMin() > request.getSalaryMax()) {
                throw new RuntimeException("El salario mínimo no puede ser mayor al salario máximo");
            }
        }
        
        Job job = new Job();
        job.setCompany(company);
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setType(request.getType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setLocation(request.getLocation());
        job.setModality(request.getModality());
        job.setDuration(request.getDuration());
        job.setRequirements(request.getRequirements());
        job.setResponsibilities(request.getResponsibilities());
        job.setSkills(request.getSkills());
        job.setBenefits(request.getBenefits());
        job.setExpirationDate(request.getExpirationDate());
        job.setStatus("abierta");
        job.setActive(true);
        
        Job savedJob = jobRepository.save(job);
        return mapToResponse(savedJob);
    }
    
    @Transactional
    public JobResponse updateJob(Long id, JobRequest request) {
        if (id == null) {
            throw new RuntimeException("El ID del job no puede ser nulo");
        }
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Job no encontrado con ID: " + id));
        
        // Validar rango salarial
        if (request.getSalaryMin() != null && request.getSalaryMax() != null) {
            if (request.getSalaryMin() > request.getSalaryMax()) {
                throw new RuntimeException("El salario mínimo no puede ser mayor al salario máximo");
            }
        }
        
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setType(request.getType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setLocation(request.getLocation());
        job.setModality(request.getModality());
        job.setDuration(request.getDuration());
        job.setRequirements(request.getRequirements());
        job.setResponsibilities(request.getResponsibilities());
        job.setSkills(request.getSkills());
        job.setBenefits(request.getBenefits());
        job.setExpirationDate(request.getExpirationDate());
        
        Job updatedJob = jobRepository.save(job);
        return mapToResponse(updatedJob);
    }
    
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {
        if (id == null) {
            throw new RuntimeException("El ID del job no puede ser nulo");
        }
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Job no encontrado con ID: " + id));
        return mapToResponse(job);
    }
    
    @Transactional(readOnly = true)
    public List<JobResponse> getAllActiveJobs() {
        return jobRepository.findActiveJobs().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<JobResponse> getJobsByCompany(Long companyId) {
        return jobRepository.findByCompanyId(companyId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<JobResponse> searchJobs(String keyword) {
        return jobRepository.searchByKeyword(keyword).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<JobResponse> getJobsByFilters(
        String type,
        String modality,
        String experienceLevel,
        String location,
        Double minSalary,
        Double maxSalary
    ) {
        return jobRepository.findByFilters(type, modality, experienceLevel, location, minSalary, maxSalary)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<JobResponse> getRecentJobs() {
        return jobRepository.findRecentJobs().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteJob(Long id) {
        if (id == null) {
            throw new RuntimeException("El ID del job no puede ser nulo");
        }
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Job no encontrado con ID: " + id));
        
        // Soft delete
        job.setActive(false);
        job.setStatus("cerrada");
        jobRepository.save(job);
    }
    
    @Transactional
    public JobResponse changeStatus(Long id, String status) {
        if (id == null) {
            throw new RuntimeException("El ID del job no puede ser nulo");
        }
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Job no encontrado con ID: " + id));
        
        if (!List.of("abierta", "cerrada", "pausada").contains(status)) {
            throw new RuntimeException("Estado inválido: " + status);
        }
        
        job.setStatus(status);
        // Mantener el flag de actividad sincronizado con el estado principal
        job.setActive(!"cerrada".equalsIgnoreCase(status));
        Job updatedJob = jobRepository.save(job);
        return mapToResponse(updatedJob);
    }
    
    // Cierre automático de jobs expiradas (puede llamarse desde un scheduler)
    @Transactional
    public void closeExpiredJobs() {
        List<Job> expiredJobs = jobRepository.findExpiredJobs(LocalDateTime.now());
        expiredJobs.forEach(job -> {
            job.setStatus("cerrada");
            job.setActive(false);
        });
        if (!expiredJobs.isEmpty()) {
            jobRepository.saveAll(expiredJobs);
        }
    }
    
    private JobResponse mapToResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setCompanyId(job.getCompany().getId());
        response.setCompanyName(job.getCompany().getName());
        response.setCompanyLogo(job.getCompany().getLogo());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setType(job.getType());
        response.setExperienceLevel(job.getExperienceLevel());
        response.setSalaryMin(job.getSalaryMin());
        response.setSalaryMax(job.getSalaryMax());
        response.setLocation(job.getLocation());
        response.setModality(job.getModality());
        response.setDuration(job.getDuration());
        response.setRequirements(job.getRequirements());
        response.setResponsibilities(job.getResponsibilities());
        response.setSkills(job.getSkills());
        response.setBenefits(job.getBenefits());
        response.setStatus(job.getStatus());
        response.setPostedDate(job.getPostedDate());
        response.setExpirationDate(job.getExpirationDate());
        response.setActive(job.getActive());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }
}
