package com.skillmatch.backend.config;

import com.skillmatch.backend.model.*;
import com.skillmatch.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@Order(2) // Se ejecuta después de CompanyUserSyncRunner
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    // Datos de ejemplo
    private final String[] firstNames = {
        "Carlos", "María", "Juan", "Ana", "Pedro", "Laura", "Luis", "Carmen", "Jorge", "Isabel",
        "Miguel", "Elena", "Fernando", "Sofía", "Ricardo", "Patricia", "Andrés", "Beatriz", "Diego", "Claudia",
        "Roberto", "Gabriela", "Manuel", "Valentina", "Alberto", "Natalia", "Javier", "Andrea", "Alejandro", "Mariana",
        "Francisco", "Carolina", "Sergio", "Daniela", "Raúl", "Victoria", "Pablo", "Camila", "Oscar", "Lucía",
        "Eduardo", "Mónica", "Arturo", "Paula", "Guillermo", "Sandra", "Héctor", "Diana", "Rodrigo", "Adriana"
    };

    private final String[] lastNames = {
        "García", "Rodríguez", "Martínez", "Hernández", "López", "González", "Pérez", "Sánchez", "Ramírez", "Torres",
        "Flores", "Rivera", "Gómez", "Díaz", "Cruz", "Morales", "Reyes", "Jiménez", "Ruiz", "Álvarez",
        "Castro", "Ortiz", "Romero", "Mendoza", "Silva", "Vargas", "Herrera", "Medina", "Gutiérrez", "Vega",
        "Ríos", "Ramos", "Castillo", "Domínguez", "Moreno", "Guerrero", "Palacios", "Salazar", "Cortés", "Aguilar"
    };

    private final String[] companyNames = {
        "TechSolutions", "InnovaCode", "DataDrive", "CloudFirst", "DevMasters", "PixelPerfect", "CodeCraft",
        "NextGen Systems", "Digital Dreams", "SmartApps", "WebWizards", "FutureCode", "ByteBuilders", "AppGenius",
        "CyberSoft", "TechVision", "CodeExperts", "InnoTech", "SoftwarePro", "DevHub",
        "Soluciones Digitales", "Tecnología Avanzada", "Sistemas Integrados", "Desarrollo Creativo", "Innovación TI",
        "Consultoría Tech", "Servicios Cloud", "Desarrollo Web", "Aplicaciones Móviles", "Ingeniería Software"
    };

    private final String[] industries = {
        "Tecnología", "Servicios Financieros", "E-commerce", "Educación", "Salud",
        "Retail", "Manufactura", "Logística", "Turismo", "Entretenimiento"
    };

    private final String[] jobTitles = {
        "Desarrollador Full Stack", "Desarrollador Frontend", "Desarrollador Backend", "Ingeniero DevOps",
        "Arquitecto de Software", "Analista de Datos", "Científico de Datos", "Diseñador UX/UI",
        "Product Manager", "Scrum Master", "QA Engineer", "Mobile Developer", "Cloud Engineer",
        "Security Engineer", "Database Administrator", "Business Analyst", "Project Manager",
        "Tech Lead", "Software Engineer", "Machine Learning Engineer"
    };

    private final String[] skills = {
        "Java", "Spring Boot", "JavaScript", "React", "Angular", "Vue.js", "Node.js", "Python",
        "Django", "Flask", "PHP", "Laravel", "C#", ".NET", "Ruby", "Rails", "Go", "Kotlin",
        "Swift", "Flutter", "React Native", "Docker", "Kubernetes", "AWS", "Azure", "GCP",
        "MySQL", "PostgreSQL", "MongoDB", "Redis", "Git", "CI/CD", "Agile", "Scrum",
        "REST API", "GraphQL", "Microservices", "TDD", "Design Patterns", "SQL"
    };

    private final String[] locations = {
        "Cartagena", "Bogotá", "Medellín", "Cali", "Barranquilla", "Bucaramanga",
        "Pereira", "Manizales", "Santa Marta", "Ibagué"
    };

    @Override
    @Transactional
    public void run(String... args) {
        try {
            // Verificar si ya hay datos - evitar duplicados
            long totalUsers = userRepository.count();
            if (totalUsers > 0) {
                log.info("✓ Base de datos ya tiene {} usuarios. Saltando seed.", totalUsers);
                return;
            }

            log.info("🌱 Iniciando seed de datos...");

            // 1. Crear roles
            createRoles();

            // 2. Crear skills
            createSkills();

            // 3. Crear usuarios regulares (100)
            log.info("Creando usuarios regulares...");
            List<User> users = createUsers(100);

            // 4. Crear empresas con sus usuarios (50)
            log.info("Creando empresas...");
            List<Company> companies = createCompanies(50);

            // 5. Crear trabajos/ofertas (100)
            log.info("Creando ofertas de trabajo...");
            List<Job> jobs = createJobs(companies, 100);

            // 6. Crear aplicaciones (100)
            log.info("Creando aplicaciones...");
            createApplications(users, jobs, 100);

            log.info("✅ Seed completado exitosamente!");
            log.info("📊 Datos creados: 150 usuarios, 50 empresas, 100 ofertas, 100 aplicaciones, 3 roles = 403 registros totales");

        } catch (Exception e) {
            log.error("❌ Error durante el seed: ", e);
        }
    }

    private void createRoles() {
        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("Usuario estándar");
        roleRepository.save(userRole);

        Role empresaRole = new Role();
        empresaRole.setName("EMPRESA");
        empresaRole.setDescription("Empresa");
        roleRepository.save(empresaRole);

        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrador");
        roleRepository.save(adminRole);

        log.info("✓ Roles creados");
    }

    private void createSkills() {
        // Skills se crearán asociados a usuarios, no como entidades independientes
        log.info("✓ Skills se crearán con los usuarios");
    }

    private List<User> createUsers(int count) {
        List<User> users = new ArrayList<>();
        Role userRole = roleRepository.findByName("USER").orElseThrow();
        int batchSize = 50;

        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setEmail("usuario" + i + "@skillmatch.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setFirstName(firstNames[random.nextInt(firstNames.length)]);
            user.setLastName(lastNames[random.nextInt(lastNames.length)] + " " + lastNames[random.nextInt(lastNames.length)]);
            user.setPhone("+57 3" + String.format("%09d", random.nextInt(1000000000)));
            user.setLocation(locations[random.nextInt(locations.length)]);
            user.setHeadline(jobTitles[random.nextInt(jobTitles.length)]);
            user.setBio("Profesional con experiencia en desarrollo de software y pasión por la tecnología.");
            user.setEnabled(true);
            user.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
            user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

            users.add(user);

            // Guardar en lotes
            if ((i + 1) % batchSize == 0 || i == count - 1) {
                userRepository.saveAll(users);
                userRepository.flush();
                log.info("   - Usuarios creados: {}/{}", i + 1, count);
                users.clear(); // Limpiar lista para siguiente lote
            }
        }

        // Retornar todos los usuarios guardados
        return userRepository.findAll();
    }

    private List<Company> createCompanies(int count) {
        List<Company> companies = new ArrayList<>();
        List<User> usersToSave = new ArrayList<>();
        Role empresaRole = roleRepository.findByName("EMPRESA").orElseThrow();
        int batchSize = 50;

        for (int i = 0; i < count; i++) {
            // Crear usuario para la empresa
            User user = new User();
            user.setEmail("empresa" + i + "@skillmatch.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setFirstName(companyNames[random.nextInt(companyNames.length)]);
            user.setLastName("Empresa");
            user.setPhone("+57 3" + String.format("%09d", random.nextInt(1000000000)));
            user.setEnabled(true);
            user.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
            user.setRoles(new HashSet<>(Collections.singletonList(empresaRole)));
            usersToSave.add(user);

            if ((i + 1) % batchSize == 0 || i == count - 1) {
                // Guardar usuarios en lote
                List<User> savedUsers = userRepository.saveAll(usersToSave);
                userRepository.flush();
                
                // Crear empresas para estos usuarios
                List<Company> companiesToSave = new ArrayList<>();
                for (User savedUser : savedUsers) {
                    Company company = new Company();
                    company.setUser(savedUser);
                    company.setName(savedUser.getFirstName());
                    company.setEmail(savedUser.getEmail());
                    company.setPhone(savedUser.getPhone());
                    company.setDescription("Empresa líder en " + industries[random.nextInt(industries.length)] + 
                        " con más de " + (random.nextInt(20) + 5) + " años de experiencia en el mercado.");
                    company.setIndustry(industries[random.nextInt(industries.length)]);
                    company.setSize(new String[]{"small", "medium", "large", "enterprise"}[random.nextInt(4)]);
                    company.setLocation(locations[random.nextInt(locations.length)]);
                    company.setWebsite("https://www." + savedUser.getFirstName().toLowerCase().replace(" ", "") + ".com");
                    company.setActive(true);
                    company.setIsVerified(random.nextBoolean());
                    company.setCreatedAt(savedUser.getCreatedAt());
                    companiesToSave.add(company);
                }
                
                // Guardar empresas en lote
                companies.addAll(companyRepository.saveAll(companiesToSave));
                companyRepository.flush();
                
                log.info("   - Empresas creadas: {}/{}", i + 1, count);
                usersToSave.clear();
            }
        }

        return companies;
    }

    private List<Job> createJobs(List<Company> companies, int count) {
        List<Job> jobs = new ArrayList<>();
        int batchSize = 50;

        for (int i = 0; i < count; i++) {
            Company company = companies.get(random.nextInt(companies.size()));

            Job job = new Job();
            job.setCompany(company);
            job.setTitle(jobTitles[random.nextInt(jobTitles.length)]);
            job.setDescription(generateJobDescription());
            job.setType(new String[]{"empleo", "práctica", "freelance"}[random.nextInt(3)]);
            job.setExperienceLevel(new String[]{"sin-experiencia", "junior", "semi-senior", "senior"}[random.nextInt(4)]);
            
            double salaryMin = 1000000 + (random.nextInt(4) * 500000);
            job.setSalaryMin(salaryMin);
            job.setSalaryMax(salaryMin + (random.nextInt(3) + 1) * 500000);
            
            job.setLocation(company.getLocation());
            job.setModality(new String[]{"presencial", "remoto", "híbrido"}[random.nextInt(3)]);
            job.setActive(random.nextInt(10) > 1); // 90% activos
            job.setPostedDate(LocalDateTime.now().minusDays(random.nextInt(90)));

            // Agregar skills aleatorias como lista de strings
            int skillCount = random.nextInt(5) + 3; // 3-7 skills
            List<String> jobSkills = new ArrayList<>();
            for (int j = 0; j < skillCount; j++) {
                jobSkills.add(skills[random.nextInt(skills.length)]);
            }
            job.setSkills(jobSkills);

            // Agregar requirements y responsibilities
            job.setRequirements(Arrays.asList(
                "Título universitario en Ingeniería de Sistemas o afín",
                "Experiencia mínima de " + (random.nextInt(3) + 1) + " años",
                "Excelentes habilidades de comunicación"
            ));

            job.setResponsibilities(Arrays.asList(
                "Desarrollar y mantener aplicaciones",
                "Colaborar con el equipo de desarrollo",
                "Participar en reuniones de planificación"
            ));

            job.setBenefits(Arrays.asList(
                "Salario competitivo",
                "Seguro de salud",
                "Días de vacaciones",
                "Capacitación continua"
            ));

            jobs.add(job);

            // Guardar en lotes
            if ((i + 1) % batchSize == 0 || i == count - 1) {
                jobRepository.saveAll(jobs);
                jobRepository.flush();
                log.info("   - Ofertas creadas: {}/{}", i + 1, count);
                jobs.clear();
            }
        }

        // Retornar todos los jobs guardados
        return jobRepository.findAll();
    }

    private void createApplications(List<User> users, List<Job> jobs, int count) {
        Set<String> uniqueApplications = new HashSet<>();
        List<Application> applications = new ArrayList<>();
        int batchSize = 50;

        for (int i = 0; i < count; i++) {
            User user = users.get(random.nextInt(users.size()));
            Job job = jobs.get(random.nextInt(jobs.size()));

            String key = user.getId() + "-" + job.getId();
            if (uniqueApplications.contains(key)) {
                i--; // Reintentar
                continue;
            }

            Application application = new Application();
            application.setUser(user);
            application.setJob(job);
            application.setStatus(new String[]{"pending", "reviewed", "accepted", "rejected"}[random.nextInt(4)]);
            application.setAppliedDate(LocalDateTime.now().minusDays(random.nextInt(60)));
            application.setCoverLetter("Estimado equipo de " + job.getCompany().getName() + 
                ", me interesa mucho la posición de " + job.getTitle() + 
                " y creo que mi experiencia se alinea perfectamente con los requisitos.");

            applications.add(application);
            uniqueApplications.add(key);

            // Guardar en lotes
            if ((i + 1) % batchSize == 0 || i == count - 1) {
                applicationRepository.saveAll(applications);
                applicationRepository.flush();
                log.info("   - Aplicaciones creadas: {}/{}", i + 1, count);
                applications.clear();
            }
        }
    }

    private String generateJobDescription() {
        return "Buscamos un profesional talentoso para unirse a nuestro equipo. " +
               "El candidato ideal tendrá experiencia en desarrollo de software, " +
               "trabajará en proyectos innovadores y colaborará con un equipo dinámico. " +
               "Ofrecemos un ambiente de trabajo flexible, oportunidades de crecimiento " +
               "y beneficios competitivos.";
    }
}
