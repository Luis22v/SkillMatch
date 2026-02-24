package com.skillmatch.backend.repository;

import com.skillmatch.backend.model.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    
    // Buscar certificaciones por usuario
    List<Certification> findByUserId(Long userId);
    
    // Buscar certificaciones ordenadas por fecha de emisión (más reciente primero)
    @Query("SELECT c FROM Certification c WHERE c.user.id = :userId ORDER BY c.issueDate DESC")
    List<Certification> findByUserIdOrderByIssueDateDesc(@Param("userId") Long userId);
    
    // Buscar certificaciones que no han expirado
    @Query("SELECT c FROM Certification c WHERE c.user.id = :userId AND (c.expirationDate IS NULL OR c.expirationDate > CURRENT_DATE)")
    List<Certification> findActiveByUserId(@Param("userId") Long userId);
}
