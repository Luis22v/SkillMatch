package com.skillmatch.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // Tipo: "empleo", "práctica", "freelance"
    @Column(length = 50, nullable = false)
    private String type;
    
    // Nivel de experiencia: "junior", "semi-senior", "senior", "sin-experiencia"
    @Column(name = "experience_level", length = 50)
    private String experienceLevel;
    
    @Column(name = "salary_min")
    private Double salaryMin;
    
    @Column(name = "salary_max")
    private Double salaryMax;
    
    @Column(length = 100)
    private String location;
    
    // Modalidad: "presencial", "remoto", "híbrido"
    @Column(length = 50)
    private String modality;
    
    // Duración para prácticas/freelance (ej: "3 meses", "6 meses", "por proyecto")
    @Column(length = 100)
    private String duration;
    
    // Requisitos (como lista JSON)
    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> requirements;
    
    // Responsabilidades
    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> responsibilities;
    
    // Habilidades requeridas
    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> skills;
    
    // Beneficios
    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> benefits;
    
    // Estado: "abierta", "cerrada", "pausada"
    @Column(length = 50, nullable = false)
    private String status = "abierta";
    
    @Column(name = "posted_date", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime postedDate;
    
    @Column(name = "expiration_date", columnDefinition = "DATETIME")
    private LocalDateTime expirationDate;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        postedDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// Converter para listas de strings (almacenar como JSON en base de datos)
@Converter
class StringListConverter implements AttributeConverter<List<String>, String> {
    
    private static final String DELIMITER = ";";
    
    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(DELIMITER, list);
    }
    
    @Override
    public List<String> convertToEntityAttribute(String joined) {
        if (joined == null || joined.isEmpty()) {
            return List.of();
        }
        return List.of(joined.split(DELIMITER));
    }
}
