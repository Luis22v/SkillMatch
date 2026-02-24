package com.skillmatch.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    
    private Long id;
    
    // Información completa del candidato
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String userHeadline;
    private String userLocation;
    private String userProfileImageUrl;
    
    // Información del trabajo
    private Long jobId;
    private String jobTitle;
    private String companyName;
    
    // Información de la aplicación
    private String status;
    private String resume;
    private String coverLetter;
    private String notes;
    private LocalDateTime appliedDate;
    private LocalDateTime reviewedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
