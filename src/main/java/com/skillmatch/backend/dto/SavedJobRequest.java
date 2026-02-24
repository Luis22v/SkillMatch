package com.skillmatch.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedJobRequest {
    @NotNull(message = "Job ID es requerido")
    private Long jobId;
    
    private String notes;
}
