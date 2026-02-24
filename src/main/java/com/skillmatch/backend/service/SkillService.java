package com.skillmatch.backend.service;

import com.skillmatch.backend.dto.SkillRequest;
import com.skillmatch.backend.model.Skill;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.repository.SkillRepository;
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
public class SkillService {
    
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    
    // Obtener todas las skills de un usuario
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserSkills(Long userId) {
        List<Skill> skills = skillRepository.findByUserId(userId);
        return skills.stream()
                .map(this::mapSkillToResponse)
                .collect(Collectors.toList());
    }
    
    // Agregar una skill
    @Transactional
    public Skill addSkill(Long userId, SkillRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Validar que no exista una skill con el mismo nombre para este usuario
        List<Skill> existingSkills = skillRepository.findByUserId(userId);
        boolean skillExists = existingSkills.stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(request.getName()));
        
        if (skillExists) {
            throw new IllegalArgumentException("Esta habilidad ya existe en tu perfil");
        }
        
        Skill skill = new Skill();
        skill.setUser(user);
        skill.setName(request.getName());
        skill.setLevel(request.getLevel());
        
        return skillRepository.save(skill);
    }
    
    // Actualizar una skill
    @Transactional
    public Skill updateSkill(Long userId, Long skillId, SkillRequest request) {
        if (skillId == null) {
            throw new IllegalArgumentException("El ID de habilidad no puede ser nulo");
        }
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Habilidad no encontrada"));
        
        // Verificar que la skill pertenece al usuario
        if (!skill.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Esta habilidad no pertenece a este usuario");
        }
        
        // Validar que no exista otra skill con el mismo nombre (excepto la actual)
        List<Skill> existingSkills = skillRepository.findByUserId(userId);
        boolean duplicateExists = existingSkills.stream()
                .anyMatch(s -> !s.getId().equals(skillId) && 
                              s.getName().equalsIgnoreCase(request.getName()));
        
        if (duplicateExists) {
            throw new IllegalArgumentException("Ya existe otra habilidad con este nombre");
        }
        
        skill.setName(request.getName());
        skill.setLevel(request.getLevel());
        
        return skillRepository.save(skill);
    }
    
    // Eliminar una skill
    @Transactional
    public void deleteSkill(Long userId, Long skillId) {
        if (skillId == null) {
            throw new IllegalArgumentException("El ID de habilidad no puede ser nulo");
        }
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Habilidad no encontrada"));
        
        // Verificar que la skill pertenece al usuario
        if (!skill.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Esta habilidad no pertenece a este usuario");
        }
        
        skillRepository.delete(skill);
    }
    
    // Mapear Skill a respuesta
    private Map<String, Object> mapSkillToResponse(Skill skill) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", skill.getId());
        response.put("name", skill.getName());
        response.put("level", skill.getLevel());
        if (skill.getYearsOfExperience() != null) {
            response.put("yearsOfExperience", skill.getYearsOfExperience());
        }
        if (skill.getDescription() != null) {
            response.put("description", skill.getDescription());
        }
        response.put("createdAt", skill.getCreatedAt());
        return response;
    }
}
