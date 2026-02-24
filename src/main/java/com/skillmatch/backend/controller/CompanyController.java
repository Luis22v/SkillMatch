package com.skillmatch.backend.controller;

import com.skillmatch.backend.dto.CompanyRequest;
import com.skillmatch.backend.dto.CompanyResponse;
import com.skillmatch.backend.dto.MessageResponse;
import com.skillmatch.backend.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CompanyController {
    
    private final CompanyService companyService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCompany(@Valid @RequestBody CompanyRequest request) {
        try {
            CompanyResponse response = companyService.createCompany(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("ID de empresa inválido"));
            }
            CompanyResponse response = companyService.updateCompany(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getCompany(@PathVariable Long id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("ID de empresa inválido"));
            }
            CompanyResponse response = companyService.getCompanyById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAllCompanies(
            @RequestParam(required = false) String status) {
        if ("active".equals(status)) {
            return ResponseEntity.ok(companyService.getActiveCompanies());
        }
        return ResponseEntity.ok(companyService.getAllCompanies());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CompanyResponse>> searchCompanies(
            @RequestParam String keyword) {
        return ResponseEntity.ok(companyService.searchCompanies(keyword));
    }
    
    @GetMapping("/filter")
    public ResponseEntity<List<CompanyResponse>> filterCompanies(
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String size) {
        return ResponseEntity.ok(
                companyService.getCompaniesByFilters(industry, location, size));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("ID de empresa inválido"));
            }
            companyService.deleteCompany(id);
            return ResponseEntity.ok(new MessageResponse("Empresa eliminada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> verifyCompany(@PathVariable Long id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("ID de empresa inválido"));
            }
            companyService.verifyCompany(id);
            return ResponseEntity.ok(new MessageResponse("Empresa verificada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/description")
    public ResponseEntity<?> updateCompanyDescription(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> request) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("ID de empresa inválido"));
            }
            String description = request.get("description");
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("La descripción no puede estar vacía"));
            }
            CompanyResponse response = companyService.updateCompanyDescription(id, description);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCompanyStatistics(@PathVariable Long id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("ID de empresa inválido"));
            }
            java.util.Map<String, Object> statistics = companyService.getCompanyStatistics(id);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }
}
