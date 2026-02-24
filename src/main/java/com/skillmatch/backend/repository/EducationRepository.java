package com.skillmatch.backend.repository;

import com.skillmatch.backend.model.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    
    // Buscar educación por usuario
    List<Education> findByUserId(Long userId);
    
    // Buscar educación actual de un usuario
    List<Education> findByUserIdAndIsCurrentTrue(Long userId);
    
    // Buscar educación ordenada por fecha (más reciente primero)
    @Query("SELECT e FROM Education e WHERE e.user.id = :userId ORDER BY e.startDate DESC")
    List<Education> findByUserIdOrderByStartDateDesc(@Param("userId") Long userId);
}
