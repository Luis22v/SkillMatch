package com.skillmatch.backend.config;

import com.skillmatch.backend.model.Company;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.repository.CompanyRepository;
import com.skillmatch.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repara registros antiguos donde la compañía no estaba enlazada
 * correctamente con el usuario dueño. Se ejecuta al iniciar la aplicación.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyUserSyncRunner implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            List<Company> companies = companyRepository.findAll();
        int updated = 0;

        for (Company company : companies) {
            if (hasValidOwner(company)) {
                continue;
            }

            String email = company.getEmail();
            if (email == null || email.isBlank()) {
                log.warn("Company {} has no email to match a user; skipping user linkage", company.getId());
                continue;
            }

            userRepository.findByEmail(email).ifPresentOrElse(user -> {
                company.setUser(user);
                companyRepository.save(company);
                log.debug("Linked company {} to user {} via email {}", company.getId(), user.getId(), email);
            }, () -> log.warn("No user found for company {} using email {}", company.getId(), email));

            User owner = company.getUser();
            if (owner != null && owner.getId() != null && owner.getId() > 0) {
                updated++;
            }
        }

        if (updated > 0) {
            log.info("Linked {} company record(s) with their owner user accounts", updated);
        }
        } catch (Exception e) {
            log.debug("Skipping company sync - tables may not exist yet: {}", e.getMessage());
        }
    }

    private boolean hasValidOwner(Company company) {
        User owner = null;
        try {
            owner = company.getUser();
        } catch (EntityNotFoundException ex) {
            // the relationship points to a missing user, treat as not linked
            return false;
        }

        if (owner == null) {
            return false;
        }

        Long ownerId = owner.getId();
        return ownerId != null && ownerId > 0;
    }
}
