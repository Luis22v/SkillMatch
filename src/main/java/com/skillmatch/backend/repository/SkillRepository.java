package com.skillmatch.backend.repository;

import com.skillmatch.backend.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    
    // Buscar habilidades por usuario
    List<Skill> findByUserId(Long userId);
    
    // Buscar habilidades por nivel
    List<Skill> findByUserIdAndLevel(Long userId, String level);
    
    // Buscar habilidades ordenadas por años de experiencia
    @Query("SELECT s FROM Skill s WHERE s.user.id = :userId ORDER BY s.yearsOfExperience DESC")
    List<Skill> findByUserIdOrderByExperienceDesc(@Param("userId") Long userId);
}
