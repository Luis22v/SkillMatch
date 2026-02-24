package com.skillmatch.backend.controller;

import com.skillmatch.backend.dto.CertificationRequest;
import com.skillmatch.backend.dto.MessageResponse;
import com.skillmatch.backend.model.Certification;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.service.CertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/certifications")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CertificationController {
    
    private final CertificationService certificationService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getUserCertifications(@PathVariable(required = false) String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long targetUserId = (userId != null && !userId.equals("undefined")) ? Long.parseLong(userId) : currentUser.getId();
        
        List<Map<String, Object>> certifications = certificationService.getUserCertifications(targetUserId);
        return ResponseEntity.ok(certifications);
    }
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addCertification(
            @PathVariable(required = false) String userId,
            @Valid @RequestBody CertificationRequest request) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long targetUserId = (userId != null && !userId.equals("undefined")) ? Long.parseLong(userId) : currentUser.getId();
        
        if (!currentUser.getId().equals(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("No tienes permiso para agregar certificación a este perfil"));
        }
        
        try {
            Certification certification = certificationService.addCertification(targetUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Certificación agregada exitosamente",
                "certification", certificationService.getUserCertifications(targetUserId)
                        .stream()
                        .filter(c -> c.get("id").equals(certification.getId()))
                        .findFirst()
                        .orElse(null)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/{certificationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCertification(
            @PathVariable(required = false) String userId,
            @PathVariable Long certificationId,
            @Valid @RequestBody CertificationRequest request) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long targetUserId = (userId != null && !userId.equals("undefined")) ? Long.parseLong(userId) : currentUser.getId();
        
        if (!currentUser.getId().equals(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("No tienes permiso para actualizar esta certificación"));
        }
        
        try {
            certificationService.updateCertification(targetUserId, certificationId, request);
            return ResponseEntity.ok(new MessageResponse("Certificación actualizada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{certificationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteCertification(
            @PathVariable(required = false) String userId,
            @PathVariable Long certificationId) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long targetUserId = (userId != null && !userId.equals("undefined")) ? Long.parseLong(userId) : currentUser.getId();
        
        if (!currentUser.getId().equals(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("No tienes permiso para eliminar esta certificación"));
        }
        
        try {
            certificationService.deleteCertification(targetUserId, certificationId);
            return ResponseEntity.ok(new MessageResponse("Certificación eliminada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
