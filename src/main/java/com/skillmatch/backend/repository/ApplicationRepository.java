package com.skillmatch.backend.repository;

import com.skillmatch.backend.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    // Buscar postulaciones por usuario
    List<Application> findByUserId(Long userId);
    
    // Buscar postulaciones por job
    List<Application> findByJobId(Long jobId);
    
    // Buscar postulaciones por estado
    List<Application> findByStatus(String status);
    
    // Buscar postulaciones de un usuario con un estado específico
    List<Application> findByUserIdAndStatus(Long userId, String status);
    
    // Buscar postulaciones de un job con un estado específico
    List<Application> findByJobIdAndStatus(Long jobId, String status);
    
    // Verificar si un usuario ya se postuló a un job (evitar duplicados)
    boolean existsByUserIdAndJobId(Long userId, Long jobId);
    
    // Obtener una postulación específica de un usuario a un job
    Optional<Application> findByUserIdAndJobId(Long userId, Long jobId);
    
    // Obtener postulaciones de jobs de una compañía específica
    @Query("SELECT a FROM Application a WHERE a.job.company.id = :companyId")
    List<Application> findByCompanyId(@Param("companyId") Long companyId);
    
    // Contar postulaciones por job
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.id = :jobId")
    Long countByJobId(@Param("jobId") Long jobId);
    
    // Obtener postulaciones recientes de un usuario
    @Query("SELECT a FROM Application a WHERE a.user.id = :userId ORDER BY a.appliedDate DESC")
    List<Application> findRecentByUserId(@Param("userId") Long userId);
}
