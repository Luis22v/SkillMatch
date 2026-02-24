package com.skillmatch.backend.service;

import com.skillmatch.backend.dto.CompanyRequest;
import com.skillmatch.backend.dto.CompanyResponse;
import com.skillmatch.backend.model.Company;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.repository.ApplicationRepository;
import com.skillmatch.backend.repository.CompanyRepository;
import com.skillmatch.backend.repository.JobRepository;
import com.skillmatch.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyService {
    
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    
    public CompanyResponse createCompany(CompanyRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("El email de la empresa es obligatorio");
        }
        // Verificar si el email ya existe
        if (companyRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe una empresa con este email");
        }
        
        User owner = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Debe existir un usuario registrado con este email"));

        Company company = new Company();
        company.setUser(owner);
        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setIndustry(request.getIndustry());
        company.setSize(request.getSize());
        company.setLocation(request.getLocation());
        company.setLogo(request.getLogo());
        company.setWebsite(request.getWebsite());
        company.setEmail(request.getEmail());
        company.setPhone(request.getPhone());
        company.setFoundedYear(request.getFoundedYear());
        company.setBenefits(request.getBenefits());
        
        Company savedCompany = companyRepository.save(company);
        return mapToResponse(savedCompany);
    }
    
    public CompanyResponse updateCompany(@NonNull Long id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        
        String requestedEmail = request.getEmail();
        if (requestedEmail == null || requestedEmail.isBlank()) {
            throw new RuntimeException("El email de la empresa es obligatorio");
        }
        // Verificar si el email ya existe (excepto para la misma empresa)
        companyRepository.findByEmail(requestedEmail).ifPresent(existingCompany -> {
            if (!existingCompany.getId().equals(id)) {
                throw new RuntimeException("Ya existe una empresa con este email");
            }
        });
        
        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setIndustry(request.getIndustry());
        company.setSize(request.getSize());
        company.setLocation(request.getLocation());
        company.setLogo(request.getLogo());
        company.setWebsite(request.getWebsite());
        if (!company.getEmail().equals(requestedEmail)) {
            User owner = company.getUser();
            if (userRepository.existsByEmail(requestedEmail)) {
                throw new RuntimeException("El email ya está en uso por otro usuario");
            }
            owner.setEmail(requestedEmail);
            userRepository.save(owner);
            company.setEmail(requestedEmail);
        }
        company.setPhone(request.getPhone());
        company.setFoundedYear(request.getFoundedYear());
        company.setBenefits(request.getBenefits());
        
        // Actualizar campos opcionales si se proporcionan
        if (request.getIsVerified() != null) {
            company.setIsVerified(request.getIsVerified());
        }
        if (request.getActive() != null) {
            company.setActive(request.getActive());
        }
        
        Company updatedCompany = companyRepository.save(company);
        return mapToResponse(updatedCompany);
    }
    
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(@NonNull Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        return mapToResponse(company);
    }
    
    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CompanyResponse> getActiveCompanies() {
        return companyRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CompanyResponse> searchCompanies(String keyword) {
        return companyRepository.searchCompanies(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CompanyResponse> getCompaniesByFilters(String industry, String location, String size) {
        return companyRepository.findByFilters(industry, location, size).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public void deleteCompany(@NonNull Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        company.setActive(false);
        companyRepository.save(company);
    }
    
    public void verifyCompany(@NonNull Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        company.setIsVerified(true);
        companyRepository.save(company);
    }
    
    public CompanyResponse updateCompanyDescription(@NonNull Long id, String description) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        company.setDescription(description);
        Company updatedCompany = companyRepository.save(company);
        return mapToResponse(updatedCompany);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getCompanyStatistics(@NonNull Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        
        // Obtener número real de trabajos activos
        long activeJobs = jobRepository.findByCompanyId(companyId).stream()
                .filter(job -> "ACTIVA".equals(job.getStatus()))
                .count();
        
        // Obtener número total de trabajos publicados
        long totalJobs = jobRepository.findByCompanyId(companyId).size();
        
        // Obtener número real de aplicaciones recibidas
        long totalApplications = applicationRepository.findByCompanyId(companyId).size();
        
        // Obtener aplicaciones pendientes de revisión
        long pendingApplications = applicationRepository.findByCompanyId(companyId).stream()
                .filter(app -> "PENDIENTE".equals(app.getStatus()))
                .count();
        
        // Calcular vistas del perfil basadas en la actividad de la empresa
        LocalDateTime createdAt = company.getCreatedAt();
        long daysSinceCreation = createdAt != null ? ChronoUnit.DAYS.between(createdAt, LocalDateTime.now()) : 0;
        long profileViews = 100 + (totalJobs * 15) + (totalApplications * 3) + (daysSinceCreation * 5);
        
        // Calcular tasa de respuesta (porcentaje de aplicaciones revisadas)
        double responseRate = 0;
        if (totalApplications > 0) {
            long reviewedApplications = totalApplications - pendingApplications;
            responseRate = (reviewedApplications * 100.0) / totalApplications;
        }
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("profileViews", profileViews);
        statistics.put("activeJobs", activeJobs);
        statistics.put("totalJobs", totalJobs);
        statistics.put("totalApplications", totalApplications);
        statistics.put("pendingApplications", pendingApplications);
        statistics.put("responseRate", Math.round(responseRate));
        
        return statistics;
    }
    
    private CompanyResponse mapToResponse(Company company) {
        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setDescription(company.getDescription());
        response.setIndustry(company.getIndustry());
        response.setSize(company.getSize());
        response.setLocation(company.getLocation());
        response.setLogo(company.getLogo());
        response.setWebsite(company.getWebsite());
        response.setEmail(company.getEmail());
        response.setPhone(company.getPhone());
        response.setUserId(company.getUser() != null ? company.getUser().getId() : null);
        response.setFoundedYear(company.getFoundedYear());
        response.setBenefits(company.getBenefits());
        response.setIsVerified(company.getIsVerified());
        response.setActive(company.getActive());
        response.setCreatedAt(company.getCreatedAt());
        response.setUpdatedAt(company.getUpdatedAt());
        return response;
    }
}
