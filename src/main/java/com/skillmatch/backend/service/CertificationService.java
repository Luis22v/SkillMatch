package com.skillmatch.backend.service;

import com.skillmatch.backend.dto.CertificationRequest;
import com.skillmatch.backend.model.Certification;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.repository.CertificationRepository;
import com.skillmatch.backend.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificationService {
    
    private final CertificationRepository certificationRepository;
    private final UserRepository userRepository;
    
    public List<Map<String, Object>> getUserCertifications(Long userId) {
        List<Certification> certifications = certificationRepository.findByUserIdOrderByIssueDateDesc(userId);
        return certifications.stream()
                .map(this::mapCertificationToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public Certification addCertification(@NonNull Long userId, CertificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Certification certification = new Certification();
        certification.setUser(user);
        certification.setName(request.getName());
        certification.setIssuer(request.getIssuer());
        certification.setIssueDate(request.getIssueDate());
        certification.setExpirationDate(request.getExpirationDate());
        certification.setCredentialId(request.getCredentialId());
        certification.setCredentialUrl(request.getCredentialUrl());
        certification.setDescription(request.getDescription());
        
        return certificationRepository.save(certification);
    }
    
    @Transactional
    public Certification updateCertification(Long userId, @NonNull Long certificationId, CertificationRequest request) {
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new IllegalArgumentException("Certificación no encontrada"));
        
        if (!certification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Esta certificación no pertenece a este usuario");
        }
        
        certification.setName(request.getName());
        certification.setIssuer(request.getIssuer());
        certification.setIssueDate(request.getIssueDate());
        certification.setExpirationDate(request.getExpirationDate());
        certification.setCredentialId(request.getCredentialId());
        certification.setCredentialUrl(request.getCredentialUrl());
        certification.setDescription(request.getDescription());
        
        return certificationRepository.save(certification);
    }
    
    @Transactional
    public void deleteCertification(Long userId, @NonNull Long certificationId) {
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new IllegalArgumentException("Certificación no encontrada"));
        
        if (!certification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Esta certificación no pertenece a este usuario");
        }
        
        certificationRepository.delete(certification);
    }
    
    private Map<String, Object> mapCertificationToResponse(Certification certification) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", certification.getId());
        response.put("name", certification.getName());
        response.put("issuer", certification.getIssuer());
        response.put("issueDate", certification.getIssueDate());
        response.put("expirationDate", certification.getExpirationDate());
        response.put("credentialId", certification.getCredentialId());
        response.put("credentialUrl", certification.getCredentialUrl());
        response.put("description", certification.getDescription());
        response.put("createdAt", certification.getCreatedAt());
        return response;
    }
}
