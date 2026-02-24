package com.skillmatch.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {
    
    @NotNull(message = "El ID de la compañía es obligatorio")
    private Long companyId;
    
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String title;
    
    @Size(max = 5000, message = "La descripción no puede exceder 5000 caracteres")
    private String description;
    
    @NotBlank(message = "El tipo es obligatorio")
    @Pattern(regexp = "empleo|práctica|freelance", message = "El tipo debe ser: empleo, práctica o freelance")
    private String type;
    
    @Pattern(regexp = "junior|semi-senior|senior|sin-experiencia", message = "El nivel debe ser: junior, semi-senior, senior o sin-experiencia")
    private String experienceLevel;
    
    @Min(value = 0, message = "El salario mínimo no puede ser negativo")
    private Double salaryMin;
    
    @Min(value = 0, message = "El salario máximo no puede ser negativo")
    private Double salaryMax;
    
    @Size(max = 100, message = "La ubicación no puede exceder 100 caracteres")
    private String location;
    
    @Pattern(regexp = "presencial|remoto|híbrido", message = "La modalidad debe ser: presencial, remoto o híbrido")
    private String modality;
    
    @Size(max = 100, message = "La duración no puede exceder 100 caracteres")
    private String duration;
    
    private List<String> requirements;
    
    private List<String> responsibilities;
    
    private List<String> skills;
    
    private List<String> benefits;
    
    private LocalDateTime expirationDate;
}
