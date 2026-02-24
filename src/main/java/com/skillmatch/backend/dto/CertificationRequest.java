package com.skillmatch.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificationRequest {
    
    @NotBlank(message = "El nombre de la certificación es obligatorio")
    private String name;
    
    private String issuer;
    
    private LocalDate issueDate;
    
    private LocalDate expirationDate;
    
    private String credentialId;
    
    private String credentialUrl;
    
    private String description;
}
