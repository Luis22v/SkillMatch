package com.skillmatch.backend.repository;

import com.skillmatch.backend.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    // Buscar jobs por compañía
    List<Job> findByCompanyId(Long companyId);
    
    // Buscar jobs activas y abiertas
    @Query("SELECT j FROM Job j WHERE j.active = true AND j.status = 'abierta'")
    List<Job> findActiveJobs();
    
    // Buscar por tipo (empleo, práctica, freelance)
    List<Job> findByTypeAndActiveTrue(String type);
    
    // Buscar por modalidad (presencial, remoto, híbrido)
    List<Job> findByModalityAndActiveTrue(String modality);
    
    // Buscar por nivel de experiencia
    List<Job> findByExperienceLevelAndActiveTrue(String experienceLevel);
    
    // Buscar por ubicación
    List<Job> findByLocationContainingIgnoreCaseAndActiveTrue(String location);
    
    // Búsqueda por palabra clave (título o descripción)
    @Query("SELECT j FROM Job j WHERE j.active = true AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Job> searchByKeyword(@Param("keyword") String keyword);
    
    // Búsqueda avanzada con múltiples filtros
    @Query("SELECT j FROM Job j WHERE j.active = true " +
           "AND (:type IS NULL OR j.type = :type) " +
           "AND (:modality IS NULL OR j.modality = :modality) " +
           "AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel) " +
           "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:minSalary IS NULL OR j.salaryMax >= :minSalary) " +
           "AND (:maxSalary IS NULL OR j.salaryMin <= :maxSalary) " +
           "AND j.status = 'abierta'")
    List<Job> findByFilters(
        @Param("type") String type,
        @Param("modality") String modality,
        @Param("experienceLevel") String experienceLevel,
        @Param("location") String location,
        @Param("minSalary") Double minSalary,
        @Param("maxSalary") Double maxSalary
    );
    
    // Jobs que expiran pronto (para cierre automático)
    @Query("SELECT j FROM Job j WHERE j.active = true AND j.status = 'abierta' " +
           "AND j.expirationDate IS NOT NULL AND j.expirationDate <= :date")
    List<Job> findExpiredJobs(@Param("date") LocalDateTime date);
    
    // Jobs más recientes
    @Query("SELECT j FROM Job j WHERE j.active = true AND j.status = 'abierta' " +
           "ORDER BY j.postedDate DESC")
    List<Job> findRecentJobs();
}
