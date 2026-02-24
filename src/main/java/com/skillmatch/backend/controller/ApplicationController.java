package com.skillmatch.backend.controller;

import com.skillmatch.backend.dto.ApplicationRequest;
import com.skillmatch.backend.dto.ApplicationResponse;
import com.skillmatch.backend.dto.MessageResponse;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ApplicationController {
    
    private final ApplicationService applicationService;
    
    // Crear nueva postulación (solo usuarios autenticados)
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createApplication(
        @Valid @RequestBody ApplicationRequest request,
        @AuthenticationPrincipal User currentUser
    ) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            ApplicationResponse response = applicationService.createApplication(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse(ex.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Error al crear postulación: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Error inesperado al crear postulación"));
        }
    }
    
    // Obtener postulación por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('EMPRESA') or hasRole('ADMIN')")
    public ResponseEntity<?> getApplicationById(@PathVariable Long id) {
        try {
            ApplicationResponse response = applicationService.getApplicationById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse("Postulación no encontrada: " + e.getMessage()));
        }
    }
    
    // Obtener postulaciones del usuario autenticado
    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyApplications(@AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            List<ApplicationResponse> applications = applicationService.getApplicationsByUser(userId);
            return ResponseEntity.ok(applications);
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse(ex.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Error al obtener postulaciones: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Error inesperado al obtener postulaciones"));
        }
    }
    
    // Obtener postulaciones por job (solo empresa/admin)
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('EMPRESA') or hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByJob(@PathVariable Long jobId) {
        List<ApplicationResponse> applications = applicationService.getApplicationsByJob(jobId);
        return ResponseEntity.ok(applications);
    }
    
    // Obtener postulaciones por compañía (solo empresa/admin)
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('EMPRESA') or hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByCompany(@PathVariable Long companyId) {
        List<ApplicationResponse> applications = applicationService.getApplicationsByCompany(companyId);
        return ResponseEntity.ok(applications);
    }
    
    // Obtener postulaciones por estado
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('EMPRESA') or hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByStatus(@PathVariable String status) {
        List<ApplicationResponse> applications = applicationService.getApplicationsByStatus(status);
        return ResponseEntity.ok(applications);
    }
    
    // Actualizar estado de postulación (solo empresa/admin)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPRESA') or hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(
        @PathVariable Long id,
        @RequestParam String status,
        @RequestParam(required = false) String notes
    ) {
        try {
            ApplicationResponse response = applicationService.updateStatus(id, status, notes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Error al actualizar estado: " + e.getMessage()));
        }
    }
    
    // Eliminar postulación
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteApplication(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser
    ) {
        try {
            Long requesterId = requireAuthenticatedUser(currentUser);
            boolean hasManagementPrivileges = hasRole(currentUser, "ADMIN") || hasRole(currentUser, "EMPRESA");
            applicationService.deleteApplication(id, requesterId, hasManagementPrivileges);
            return ResponseEntity.ok(new MessageResponse("Postulación eliminada exitosamente"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse("Error al eliminar postulación: " + e.getMessage()));
        }
    }
    
    // Contar postulaciones por job
    @GetMapping("/job/{jobId}/count")
    @PreAuthorize("hasRole('EMPRESA') or hasRole('ADMIN')")
    public ResponseEntity<Long> countApplicationsByJob(@PathVariable Long jobId) {
        Long count = applicationService.countApplicationsByJob(jobId);
        return ResponseEntity.ok(count);
    }
    
    // Verificar si el usuario ya se postuló a un job
    @GetMapping("/check")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> hasUserApplied(
        @RequestParam Long jobId,
        @AuthenticationPrincipal User currentUser
    ) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            boolean hasApplied = applicationService.hasUserApplied(userId, jobId);
            return ResponseEntity.ok(hasApplied);
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(false);
        }
    }

    private Long requireAuthenticatedUser(User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        return currentUser.getId();
    }

    private boolean hasRole(User currentUser, String roleName) {
        if (currentUser == null) {
            return false;
        }
        String expectedAuthority = "ROLE_" + roleName.toUpperCase();
        return currentUser.getAuthorities().stream()
            .anyMatch(authority -> expectedAuthority.equals(authority.getAuthority()));
    }
}
