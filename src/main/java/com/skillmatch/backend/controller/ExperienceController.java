package com.skillmatch.backend.controller;

import com.skillmatch.backend.dto.ExperienceRequest;
import com.skillmatch.backend.dto.MessageResponse;
import com.skillmatch.backend.model.Experience;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.service.ExperienceService;
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
@RequestMapping("/api/users/{userId}/experiences")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class ExperienceController {
    
    private final ExperienceService experienceService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getUserExperiences(@PathVariable(required = false) String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long targetUserId = (userId != null && !userId.equals("undefined")) ? Long.parseLong(userId) : currentUser.getId();
        
        List<Map<String, Object>> experiences = experienceService.getUserExperiences(targetUserId);
        return ResponseEntity.ok(experiences);
    }
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addExperience(
            @PathVariable(required = false) String userId,
            @Valid @RequestBody ExperienceRequest request) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        // Si userId es null o "undefined", usar el ID del usuario autenticado
        Long targetUserId = (userId != null && !userId.equals("undefined")) ? Long.parseLong(userId) : currentUser.getId();
        
        if (!currentUser.getId().equals(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("No tienes permiso para agregar experiencia a este perfil"));
        }
        
        try {
            Experience experience = experienceService.addExperience(targetUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Experiencia agregada exitosamente",
                "experience", experienceService.getUserExperiences(targetUserId)
                        .stream()
                        .filter(e -> e.get("id").equals(experience.getId()))
                        .findFirst()
                        .orElse(null)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/{experienceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateExperience(
            @PathVariable(required = false) String userId,
            @PathVariable Long experienceId,
            @Valid @RequestBody ExperienceRequest request) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long targetUserId = (userId != null && !userId.equals("undefined")) ? Long.parseLong(userId) : currentUser.getId();
        
        if (!currentUser.getId().equals(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("No tienes permiso para actualizar esta experiencia"));
        }
        
        try {
            experienceService.updateExperience(targetUserId, experienceId, request);
            return ResponseEntity.ok(new MessageResponse("Experiencia actualizada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{experienceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteExperience(
            @PathVariable(required = false) String userId,
            @PathVariable Long experienceId) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long targetUserId = (userId != null && !userId.equals("undefined")) ? Long.parseLong(userId) : currentUser.getId();
        
        if (!currentUser.getId().equals(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("No tienes permiso para eliminar esta experiencia"));
        }
        
        try {
            experienceService.deleteExperience(targetUserId, experienceId);
            return ResponseEntity.ok(new MessageResponse("Experiencia eliminada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
