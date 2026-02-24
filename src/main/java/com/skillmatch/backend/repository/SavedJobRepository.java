package com.skillmatch.backend.repository;

import com.skillmatch.backend.model.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    
    List<SavedJob> findByUserIdOrderBySavedAtDesc(Long userId);
    
    Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);
    
    boolean existsByUserIdAndJobId(Long userId, Long jobId);
    
    @Query("SELECT sj.job.id FROM SavedJob sj WHERE sj.user.id = ?1")
    List<Long> findJobIdsByUserId(Long userId);
    
    void deleteByUserIdAndJobId(Long userId, Long jobId);
    
    long countByUserId(Long userId);
}
