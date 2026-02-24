package com.skillmatch.backend.controller;

import com.skillmatch.backend.dto.MessageResponse;
import com.skillmatch.backend.dto.UpdatePasswordRequest;
import com.skillmatch.backend.dto.UpdateProfileRequest;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Usuario no autenticado"));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("phone", user.getPhone());
        response.put("headline", user.getHeadline());
        response.put("location", user.getLocation());
        response.put("bio", user.getBio());
        response.put("profileImageUrl", user.getProfileImageUrl());
        response.put("coverImageUrl", user.getCoverImageUrl());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Usuario no autenticado"));
        }
        Map<String, Object> profile = userService.getUserProfile(id);
        return ResponseEntity.ok(profile);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Usuario no autenticado"));
        }
        // Solo el propio usuario puede actualizar su perfil
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).body(new MessageResponse("No tienes permiso para actualizar este perfil"));
        }
        
        User updatedUser = userService.updateProfile(id, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Perfil actualizado exitosamente");
        response.put("user", userService.getUserProfile(updatedUser.getId()));
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/profile-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfileImage(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Usuario no autenticado"));
        }
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).body(new MessageResponse("No tienes permiso para actualizar esta imagen"));
        }
        
        String imageUrl = request.get("imageUrl");
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("La URL de la imagen es requerida"));
        }
        
        User updatedUser = userService.updateProfileImage(id, imageUrl);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Foto de perfil actualizada exitosamente");
        response.put("profileImageUrl", updatedUser.getProfileImageUrl());
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/cover-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCoverImage(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Usuario no autenticado"));
        }
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).body(new MessageResponse("No tienes permiso para actualizar esta imagen"));
        }
        
        String imageUrl = request.get("imageUrl");
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("La URL de la imagen es requerida"));
        }
        
        User updatedUser = userService.updateCoverImage(id, imageUrl);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Portada actualizada exitosamente");
        response.put("coverImageUrl", updatedUser.getCoverImageUrl());
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePasswordRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Usuario no autenticado"));
        }
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).body(new MessageResponse("No tienes permiso para actualizar esta contraseña"));
        }
        
        try {
            userService.updatePassword(id, request);
            return ResponseEntity.ok(new MessageResponse("Contraseña actualizada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserStatistics(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Usuario no autenticado"));
        }
        try {
            Map<String, Object> statistics = userService.getUserStatistics(id);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }
    
    @GetMapping("/test")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> testUserAccess() {
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "Acceso de usuario autorizado");
        }});
    }
}
