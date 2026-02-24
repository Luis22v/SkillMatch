package com.skillmatch.backend.controller;

import com.skillmatch.backend.dto.JobRequest;
import com.skillmatch.backend.dto.JobResponse;
import com.skillmatch.backend.dto.MessageResponse;
import com.skillmatch.backend.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class JobController {
    
    private final JobService jobService;
    
    // Crear nueva oferta de trabajo (solo empresas/admin)
    @PostMapping
    @PreAuthorize("hasRole('EMPRESA') or hasRole('ADMIN')")
    public ResponseEntity<?> createJob(@Valid @RequestBody JobRequest request) {
        try {
            JobResponse response = jobService.createJob(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Error al crear job: " + e.getMessage()));
        }
    }
    
    // Actualizar oferta de trabajo
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPRESA') or hasRole('ADMIN')")
    public ResponseEntity<?> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        try {
            JobResponse response = jobService.updateJob(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Error al actualizar job: " + e.getMessage()));
        }
    }
    
    // Obtener job por ID (público)
    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable Long id) {
        try {
            JobResponse response = jobService.getJobById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse("Job no encontrado: " + e.getMessage()));
        }
    }
    
    // Listar todas las ofertas activas (público)
    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllActiveJobs() {
        List<JobResponse> jobs = jobService.getAllActiveJobs();
        return ResponseEntity.ok(jobs);
    }
    
    // Obtener ofertas más recientes (público)
    @GetMapping("/recent")
    public ResponseEntity<List<JobResponse>> getRecentJobs() {
        List<JobResponse> jobs = jobService.getRecentJobs();
        return ResponseEntity.ok(jobs);
    }
    
    // Buscar ofertas por palabra clave (público)
    @GetMapping("/search")
    public ResponseEntity<List<JobResponse>> searchJobs(@RequestParam String keyword) {
        List<JobResponse> jobs = jobService.searchJobs(keyword);
        return ResponseEntity.ok(jobs);
    }
    
    // Filtrar ofertas con múltiples criterios (público)
    @GetMapping("/filter")
    public ResponseEntity<List<JobResponse>> filterJobs(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String modality,
        @RequestParam(required = false) String experienceLevel,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) Double minSalary,
        @RequestParam(required = false) Double maxSalary
    ) {
        List<JobResponse> jobs = jobService.getJobsByFilters(
            type, modality, experienceLevel, location, minSalary, maxSalary
        );
        return ResponseEntity.ok(jobs);
    }
    
    // Obtener ofertas de una compañía específica (público)
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<JobResponse>> getJobsByCompany(@PathVariable Long companyId) {
        List<JobResponse> jobs = jobService.getJobsByCompany(companyId);
        return ResponseEntity.ok(jobs);
    }
    
    // Cambiar estado de una oferta (solo empresa/admin)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPRESA') or hasRole('ADMIN')")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            JobResponse response = jobService.changeStatus(id, status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Error al cambiar estado: " + e.getMessage()));
        }
    }
    
    // Eliminar oferta (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPRESA') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        try {
            jobService.deleteJob(id);
            return ResponseEntity.ok(new MessageResponse("Job eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse("Error al eliminar job: " + e.getMessage()));
        }
    }
}
