package com.skillmatch.backend.service;

import com.skillmatch.backend.dto.UpdatePasswordRequest;
import com.skillmatch.backend.dto.UpdateProfileRequest;
import com.skillmatch.backend.exception.ResourceNotFoundException;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.repository.ApplicationRepository;
import com.skillmatch.backend.repository.CompanyRepository;
import com.skillmatch.backend.repository.SkillRepository;
import com.skillmatch.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationRepository applicationRepository;
    private final SkillRepository skillRepository;
    
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }
    
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }
    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("El email ya está en uso");
            }
            user.setEmail(request.getEmail());
            companyRepository.findByUserId(userId).ifPresent(company -> {
                company.setEmail(request.getEmail());
                companyRepository.save(company);
            });
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getHeadline() != null) {
            user.setHeadline(request.getHeadline());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        return Objects.requireNonNull(userRepository.save(user));
    }
    @Transactional
    public User updateProfileImage(Long userId, String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL de la imagen de perfil es obligatoria");
        }
        User user = getUserById(userId);
        user.setProfileImageUrl(imageUrl);
        return Objects.requireNonNull(userRepository.save(user));
    }
    
    @Transactional
    public User updateCoverImage(Long userId, String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL de la imagen de portada es obligatoria");
        }
        User user = getUserById(userId);
        user.setCoverImageUrl(imageUrl);
        return Objects.requireNonNull(userRepository.save(user));
    }
    
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = getUserById(userId);
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        Objects.requireNonNull(userRepository.save(user));
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getUserProfile(Long userId) {
        User user = getUserById(userId);
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("phone", user.getPhone());
        profile.put("headline", user.getHeadline());
        profile.put("location", user.getLocation());
        profile.put("bio", user.getBio());
        profile.put("profileImageUrl", user.getProfileImageUrl());
        profile.put("coverImageUrl", user.getCoverImageUrl());
        profile.put("createdAt", user.getCreatedAt());
        
        return profile;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(Long userId) {
        User user = getUserById(userId);
        
        // Obtener número real de aplicaciones
        long applicationsCount = applicationRepository.findByUserId(userId).size();
        
        // Obtener número real de skills
        long skillsCount = skillRepository.findByUserId(userId).size();
        
        // Calcular vistas del perfil basadas en la actividad del usuario
        // Simulación realista: base + aplicaciones * 5 + días desde creación
        LocalDateTime createdAt = user.getCreatedAt();
        long daysSinceCreation = createdAt != null ? ChronoUnit.DAYS.between(createdAt, LocalDateTime.now()) : 0;
        long profileViews = 50 + (applicationsCount * 5) + (daysSinceCreation * 2);
        
        // Calcular tasa de coincidencia basada en completitud del perfil
        int completionScore = 0;
        if (user.getFirstName() != null && !user.getFirstName().isEmpty()) completionScore += 15;
        if (user.getLastName() != null && !user.getLastName().isEmpty()) completionScore += 15;
        if (user.getPhone() != null && !user.getPhone().isEmpty()) completionScore += 10;
        if (user.getHeadline() != null && !user.getHeadline().isEmpty()) completionScore += 15;
        if (user.getBio() != null && !user.getBio().isEmpty()) completionScore += 10;
        if (skillsCount > 0) completionScore += 20;
        if (applicationsCount > 0) completionScore += 15;
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("profileViews", profileViews);
        statistics.put("applicationsCount", applicationsCount);
        statistics.put("matchRate", completionScore);
        statistics.put("skillsCount", skillsCount);
        statistics.put("profileCompleteness", completionScore);
        
        return statistics;
    }
}
