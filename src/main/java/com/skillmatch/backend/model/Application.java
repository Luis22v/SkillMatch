package com.skillmatch.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "job_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    
    // Estado: "pendiente", "revisada", "aceptada", "rechazada"
    @Column(length = 50, nullable = false)
    private String status = "pendiente";
    
    @Column(columnDefinition = "TEXT")
    private String resume; // CV o enlace al CV
    
    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter; // Carta de presentación
    
    @Column(name = "applied_date", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime appliedDate;
    
    @Column(name = "reviewed_date", columnDefinition = "DATETIME")
    private LocalDateTime reviewedDate;
    
    @Column(columnDefinition = "TEXT")
    private String notes; // Notas de la empresa sobre el candidato
    
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        appliedDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
