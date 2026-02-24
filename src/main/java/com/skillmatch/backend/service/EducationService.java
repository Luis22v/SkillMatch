package com.skillmatch.backend.service;

import com.skillmatch.backend.dto.EducationRequest;
import com.skillmatch.backend.model.Education;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.repository.EducationRepository;
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
public class EducationService {
    
    private final EducationRepository educationRepository;
    private final UserRepository userRepository;
    
    public List<Map<String, Object>> getUserEducations(Long userId) {
        List<Education> educations = educationRepository.findByUserIdOrderByStartDateDesc(userId);
        return educations.stream()
                .map(this::mapEducationToResponse)
                .collect(Collectors.toList());
    }
    @Transactional
    public Education addEducation(@NonNull Long userId, EducationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Education education = new Education();
        education.setUser(user);
        education.setSchool(request.getSchool());
        education.setDegree(request.getDegree());
        education.setFieldOfStudy(request.getFieldOfStudy());
        education.setStartDate(request.getStartDate());
        education.setEndDate(request.getEndDate());
        education.setIsCurrent(request.getIsCurrent() != null ? request.getIsCurrent() : false);
        education.setDescription(request.getDescription());
        
        return educationRepository.save(education);
    }
    
    @Transactional
    public Education updateEducation(Long userId, @NonNull Long educationId, EducationRequest request) {
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new IllegalArgumentException("Educación no encontrada"));
        
        if (!education.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Esta educación no pertenece a este usuario");
        }
        
        education.setSchool(request.getSchool());
        education.setDegree(request.getDegree());
        education.setFieldOfStudy(request.getFieldOfStudy());
        education.setStartDate(request.getStartDate());
        education.setEndDate(request.getEndDate());
        education.setIsCurrent(request.getIsCurrent() != null ? request.getIsCurrent() : false);
        education.setDescription(request.getDescription());
        
        return educationRepository.save(education);
    }
    
    @Transactional
    public void deleteEducation(Long userId, @NonNull Long educationId) {
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new IllegalArgumentException("Educación no encontrada"));
        
        if (!education.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Esta educación no pertenece a este usuario");
        }
        
        educationRepository.delete(education);
    }
    
    private Map<String, Object> mapEducationToResponse(Education education) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", education.getId());
        response.put("school", education.getSchool());
        response.put("degree", education.getDegree());
        response.put("fieldOfStudy", education.getFieldOfStudy());
        response.put("startDate", education.getStartDate());
        response.put("endDate", education.getEndDate());
        response.put("isCurrent", education.getIsCurrent());
        response.put("description", education.getDescription());
        response.put("createdAt", education.getCreatedAt());
        return response;
    }
}
