package com.skillmatch.backend.controller;

import com.skillmatch.backend.dto.EducationRequest;
import com.skillmatch.backend.dto.MessageResponse;
import com.skillmatch.backend.model.Education;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.service.EducationService;
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
@RequestMapping("/api/users/{userId}/educations")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class EducationController {
    
    private final EducationService educationService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getUserEducations(@PathVariable(required = false) String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long targetUserId = (userId != null && !userId.equals("undefined")) ? Long.parseLong(userId) : currentUser.getId();
        
        List<Map<String, Object>> educations = educationService.getUserEducations(targetUserId);
        return ResponseEntity.ok(educations);
    }
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addEducation(
            @PathVariable(required = false) String userId,
            @Valid @RequestBody EducationRequest request) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long targetUserId = (userId != null && !userId.equals("undefined")) ? Long.parseLong(userId) : currentUser.getId();
        
        if (!currentUser.getId().equals(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("No tienes permiso para agregar educación a este perfil"));
        }
        
        try {
            Education education = educationService.addEducation(targetUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Educación agregada exitosamente",
                "education", educationService.getUserEducations(targetUserId)
                        .stream()
                        .filter(e -> e.get("id").equals(education.getId()))
                        .findFirst()
                        .orElse(null)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/{educationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateEducation(
            @PathVariable(required = false) String userId,
            @PathVariable Long educationId,
            @Valid @RequestBody EducationRequest request) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long targetUserId = (userId != null && !userId.equals("undefined")) ? Long.parseLong(userId) : currentUser.getId();
        
        if (!currentUser.getId().equals(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("No tienes permiso para actualizar esta educación"));
        }
        
        try {
            educationService.updateEducation(targetUserId, educationId, request);
            return ResponseEntity.ok(new MessageResponse("Educación actualizada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{educationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteEducation(
            @PathVariable(required = false) String userId,
            @PathVariable Long educationId) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long targetUserId = (userId != null && !userId.equals("undefined")) ? Long.parseLong(userId) : currentUser.getId();
        
        if (!currentUser.getId().equals(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("No tienes permiso para eliminar esta educación"));
        }
        
        try {
            educationService.deleteEducation(targetUserId, educationId);
            return ResponseEntity.ok(new MessageResponse("Educación eliminada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
