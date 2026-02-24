package com.skillmatch.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {
    
    @NotNull(message = "El ID del job es obligatorio")
    private Long jobId;
    
    @Size(max = 1000, message = "El CV no puede exceder 1000 caracteres")
    private String resume;
    
    @Size(max = 2000, message = "La carta de presentación no puede exceder 2000 caracteres")
    private String coverLetter;
}
