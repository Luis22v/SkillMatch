package com.skillmatch.backend.controller;

import com.skillmatch.backend.dto.MessageResponse;
import com.skillmatch.backend.dto.SavedJobRequest;
import com.skillmatch.backend.dto.SavedJobResponse;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.service.SavedJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SavedJobController {
    
    private final SavedJobService savedJobService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> saveJob(
            @Valid @RequestBody SavedJobRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            SavedJobResponse response = savedJobService.saveJob(userId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/job/{jobId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> unsaveJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            savedJobService.unsaveJob(userId, jobId);
            return ResponseEntity.ok(new MessageResponse("Oferta eliminada de guardados exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/my-saved-jobs")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMySavedJobs(@AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            List<SavedJobResponse> savedJobs = savedJobService.getUserSavedJobs(userId);
            return ResponseEntity.ok(savedJobs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/check/{jobId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> checkIfSaved(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            boolean isSaved = savedJobService.isJobSaved(userId, jobId);
            return ResponseEntity.ok(new SavedStatus(isSaved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/job-ids")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getSavedJobIds(@AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            List<Long> jobIds = savedJobService.getSavedJobIds(userId);
            return ResponseEntity.ok(jobIds);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/count")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getSavedJobsCount(@AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            Long count = savedJobService.getSavedJobsCount(userId);
            return ResponseEntity.ok(new CountResponse(count));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PatchMapping("/job/{jobId}/notes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateNotes(
            @PathVariable Long jobId,
            @RequestBody NotesRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            SavedJobResponse response = savedJobService.updateNotes(userId, jobId, request.notes());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    private Long requireAuthenticatedUser(User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        return currentUser.getId();
    }
    
    private record SavedStatus(boolean isSaved) {}
    private record CountResponse(Long count) {}
    private record NotesRequest(String notes) {}
}
