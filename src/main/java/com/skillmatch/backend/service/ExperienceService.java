package com.skillmatch.backend.service;

import com.skillmatch.backend.dto.ExperienceRequest;
import com.skillmatch.backend.model.Experience;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.repository.ExperienceRepository;
import com.skillmatch.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExperienceService {
    
    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserExperiences(Long userId) {
        List<Experience> experiences = experienceRepository.findByUserIdOrderByStartDateDesc(userId);
        return experiences.stream()
                .map(this::mapExperienceToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public Experience addExperience(Long userId, ExperienceRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Experience experience = new Experience();
        experience.setUser(user);
        experience.setCompany(request.getCompany());
        experience.setPosition(request.getPosition());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setIsCurrent(request.getIsCurrent() != null ? request.getIsCurrent() : false);
        experience.setDescription(request.getDescription());
        experience.setLocation(request.getLocation());
        
        return experienceRepository.save(experience);
    }
    
    @Transactional
    public Experience updateExperience(Long userId, Long experienceId, ExperienceRequest request) {
        if (experienceId == null) {
            throw new IllegalArgumentException("El ID de experiencia no puede ser nulo");
        }
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new IllegalArgumentException("Experiencia no encontrada"));
        
        if (!experience.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Esta experiencia no pertenece a este usuario");
        }
        
        experience.setCompany(request.getCompany());
        experience.setPosition(request.getPosition());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setIsCurrent(request.getIsCurrent() != null ? request.getIsCurrent() : false);
        experience.setDescription(request.getDescription());
        experience.setLocation(request.getLocation());
        
        return experienceRepository.save(experience);
    }
    
    @Transactional
    public void deleteExperience(Long userId, Long experienceId) {
        if (experienceId == null) {
            throw new IllegalArgumentException("El ID de experiencia no puede ser nulo");
        }
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new IllegalArgumentException("Experiencia no encontrada"));
        
        if (!experience.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Esta experiencia no pertenece a este usuario");
        }
        
        experienceRepository.delete(experience);
    }
    
    private Map<String, Object> mapExperienceToResponse(Experience experience) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", experience.getId());
        response.put("company", experience.getCompany());
        response.put("position", experience.getPosition());
        response.put("startDate", experience.getStartDate());
        response.put("endDate", experience.getEndDate());
        response.put("isCurrent", experience.getIsCurrent());
        response.put("description", experience.getDescription());
        response.put("location", experience.getLocation());
        response.put("createdAt", experience.getCreatedAt());
        return response;
    }
}
