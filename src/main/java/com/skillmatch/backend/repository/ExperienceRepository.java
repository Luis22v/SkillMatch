package com.skillmatch.backend.repository;

import com.skillmatch.backend.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    
    // Buscar experiencias por usuario
    List<Experience> findByUserId(Long userId);
    
    // Buscar experiencias actuales de un usuario
    List<Experience> findByUserIdAndIsCurrentTrue(Long userId);
    
    // Buscar experiencias ordenadas por fecha (más reciente primero)
    @Query("SELECT e FROM Experience e WHERE e.user.id = :userId ORDER BY e.startDate DESC")
    List<Experience> findByUserIdOrderByStartDateDesc(@Param("userId") Long userId);
}
