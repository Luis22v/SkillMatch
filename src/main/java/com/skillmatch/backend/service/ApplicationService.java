package com.skillmatch.backend.service;

import com.skillmatch.backend.dto.ApplicationRequest;
import com.skillmatch.backend.dto.ApplicationResponse;
import com.skillmatch.backend.model.Application;
import com.skillmatch.backend.model.Job;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.repository.ApplicationRepository;
import com.skillmatch.backend.repository.JobRepository;
import com.skillmatch.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    
    @Transactional
    public ApplicationResponse createApplication(Long userId, ApplicationRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo");
        }
        // Validar que el usuario exista
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        
        // Validar que el job exista y esté activo
        Long jobId = request.getJobId();
        if (jobId == null) {
            throw new IllegalArgumentException("El ID del job no puede ser nulo");
        }
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job no encontrado con ID: " + jobId));
        
        String jobStatus = job.getStatus();
        if (!Boolean.TRUE.equals(job.getActive()) || jobStatus == null || !"abierta".equalsIgnoreCase(jobStatus)) {
            throw new RuntimeException("El job no está disponible para postulaciones");
        }
        
        // Verificar que el usuario no se haya postulado previamente
        if (applicationRepository.existsByUserIdAndJobId(userId, request.getJobId())) {
            throw new RuntimeException("Ya te has postulado a esta oferta");
        }
        
        Application application = new Application();
        application.setUser(user);
        application.setJob(job);
        application.setResume(request.getResume());
        application.setCoverLetter(request.getCoverLetter());
        application.setStatus("pendiente");
        
        Application savedApplication = applicationRepository.save(application);
        return mapToResponse(savedApplication);
    }
    
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la postulación no puede ser nulo");
        }
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + id));
        return mapToResponse(application);
    }
    
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByUser(Long userId) {
        return applicationRepository.findByUserId(userId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByJob(Long jobId) {
        return applicationRepository.findByJobId(jobId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByCompany(Long companyId) {
        return applicationRepository.findByCompanyId(companyId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByStatus(String status) {
        return applicationRepository.findByStatus(status).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ApplicationResponse updateStatus(Long id, String status, String notes) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la postulación no puede ser nulo");
        }
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + id));
        
        if (!List.of("pendiente", "revisada", "aceptada", "rechazada").contains(status)) {
            throw new RuntimeException("Estado inválido: " + status);
        }
        
        application.setStatus(status);
        if (notes != null) {
            application.setNotes(notes);
        }
        
        if (!status.equals("pendiente") && application.getReviewedDate() == null) {
            application.setReviewedDate(LocalDateTime.now());
        }
        
        Application updatedApplication = applicationRepository.save(application);
        return mapToResponse(updatedApplication);
    }
    
    @Transactional
    public void deleteApplication(Long id, Long requesterId, boolean hasManagementPrivileges) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la postulación no puede ser nulo");
        }
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + id));

        boolean isOwner = application.getUser().getId().equals(requesterId);
        if (!hasManagementPrivileges && !isOwner) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta postulación");
        }
        
        applicationRepository.delete(application);
    }
    
    @Transactional(readOnly = true)
    public Long countApplicationsByJob(Long jobId) {
        Long total = applicationRepository.countByJobId(jobId);
        logger.debug("Total de postulaciones para job {}: {}", jobId, total);
        return total;
    }
    
    @Transactional(readOnly = true)
    public boolean hasUserApplied(Long userId, Long jobId) {
        boolean exists = applicationRepository.existsByUserIdAndJobId(userId, jobId);
        logger.debug("Usuario {} ya aplicado a job {}: {}", userId, jobId, exists);
        return exists;
    }
    
    private ApplicationResponse mapToResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        User user = application.getUser();
        Job job = application.getJob();
        
        response.setId(application.getId());
        response.setUserId(user.getId());
        
        // Información completa del candidato
        response.setUserName(user.getFirstName() + " " + user.getLastName());
        response.setUserEmail(user.getEmail());
        response.setUserPhone(user.getPhone());
        response.setUserHeadline(user.getHeadline());
        response.setUserLocation(user.getLocation());
        response.setUserProfileImageUrl(user.getProfileImageUrl());
        
        // Información del trabajo y empresa
        response.setJobId(job.getId());
        response.setJobTitle(job.getTitle());
        response.setCompanyName(job.getCompany().getName());
        
        // Información de la aplicación
        response.setStatus(application.getStatus());
        response.setResume(application.getResume());
        response.setCoverLetter(application.getCoverLetter());
        response.setNotes(application.getNotes());
        response.setAppliedDate(application.getAppliedDate());
        response.setReviewedDate(application.getReviewedDate());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        
        return response;
    }
}
