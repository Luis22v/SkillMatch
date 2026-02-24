package com.skillmatch.backend.repository;

import com.skillmatch.backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    Optional<Company> findByEmail(String email);

    Optional<Company> findByUserId(Long userId);
    
    List<Company> findByActiveTrue();
    
    List<Company> findByIndustry(String industry);
    
    List<Company> findByLocation(String location);
    
    List<Company> findByIsVerifiedTrue();
    
    @Query("SELECT c FROM Company c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.industry) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Company> searchCompanies(@Param("keyword") String keyword);
    
    @Query("SELECT c FROM Company c WHERE c.active = true AND " +
           "(:industry IS NULL OR c.industry = :industry) AND " +
           "(:location IS NULL OR c.location = :location) AND " +
           "(:size IS NULL OR c.size = :size)")
    List<Company> findByFilters(
        @Param("industry") String industry,
        @Param("location") String location,
        @Param("size") String size
    );
}
