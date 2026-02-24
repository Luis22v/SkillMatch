package com.skillmatch.backend.service;

import com.skillmatch.backend.dto.AuthResponse;
import com.skillmatch.backend.dto.LoginRequest;
import com.skillmatch.backend.dto.RegisterRequest;
import com.skillmatch.backend.model.Company;
import com.skillmatch.backend.model.Role;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.repository.CompanyRepository;
import com.skillmatch.backend.repository.RoleRepository;
import com.skillmatch.backend.repository.UserRepository;
import com.skillmatch.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        String primaryRole = resolvePrimaryRole(user);
        Long companyId = null;
        if ("EMPRESA".equalsIgnoreCase(primaryRole)) {
            companyId = companyRepository.findByUserId(user.getId())
                .map(Company::getId)
                .orElse(null);
        }

        return new AuthResponse(jwt, user.getId(), companyId, user.getEmail(),
            user.getFirstName(), user.getLastName(), primaryRole);
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhone(registerRequest.getPhone());
        user.setEnabled(true);
        
        // Determinar el rol según el tipo de usuario
        String roleName = "EMPRESA".equalsIgnoreCase(registerRequest.getUserType()) ? "EMPRESA" : "USER";
        
        // Asignar rol
        Role userRole = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    newRole.setDescription(roleName.equals("EMPRESA") ? "Empresa" : "Usuario estándar");
                    return roleRepository.save(newRole);
                });
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        
        // Si es una empresa, crear automáticamente el registro de Company
        Long companyId = null;
        if ("EMPRESA".equals(roleName)) {
            Company company = new Company();
            company.setUser(savedUser);
            company.setName(registerRequest.getFirstName());
            company.setEmail(registerRequest.getEmail());
            company.setPhone(registerRequest.getPhone());
            company.setLocation("No especificada");
            company.setDescription("Bienvenido a " + registerRequest.getFirstName() + ". Edita esta sección para agregar información sobre tu empresa, misión, visión y valores.");
            company.setActive(true);
            company.setIsVerified(false);
            Company savedCompany = companyRepository.save(company);
            companyId = savedCompany.getId();
        }
        
        // Generar token
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                registerRequest.getEmail(),
                registerRequest.getPassword()
            )
        );
        
        String jwt = tokenProvider.generateToken(authentication);
        
        // Retornar el companyId en lugar del userId para empresas
        return new AuthResponse(jwt, savedUser.getId(), companyId, savedUser.getEmail(),
                savedUser.getFirstName(), savedUser.getLastName(), roleName);
    }

    private String resolvePrimaryRole(User user) {
        Set<Role> roles = Optional.ofNullable(user.getRoles()).orElse(Set.of());
        if (roles.stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()))) {
            return "ADMIN";
        }
        if (roles.stream().anyMatch(role -> "EMPRESA".equalsIgnoreCase(role.getName()))) {
            return "EMPRESA";
        }
        return roles.stream()
                .findFirst()
                .map(role -> role.getName().toUpperCase())
                .orElse("USER");
    }
}
