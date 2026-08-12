package com.hnigrowth.config;

import com.hnigrowth.lead.LeadPriority;
import com.hnigrowth.lead.LeadSource;
import com.hnigrowth.lead.LeadService;
import com.hnigrowth.lead.LeadRepository;
import com.hnigrowth.lead.dto.LeadRequest;
import com.hnigrowth.user.Role;
import com.hnigrowth.user.User;
import com.hnigrowth.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds a default ADMIN, a few RMs (so advisor routing has candidates), and
 * sample leads (which flow through the real AI pipeline). Idempotent: it only
 * seeds when the corresponding tables are empty.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final LeadService leadService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:admin@hnigrowth.com}")
    private String adminEmail;

    @Value("${app.seed.admin-password:Admin@123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        seedUsers();
        seedLeads();
    }

    private void seedUsers() {
        if (userRepository.existsByEmail(adminEmail)) return;

        userRepository.save(User.builder()
                .fullName("Platform Admin").email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN).enabled(true).available(false).build());

        userRepository.saveAll(List.of(
                User.builder().fullName("Riya Sharma").email("riya.rm@hnigrowth.com")
                        .password(passwordEncoder.encode("Rm@12345")).role(Role.RM)
                        .region("Hyderabad").language("English")
                        .specialization("Technology").available(true).build(),
                User.builder().fullName("Arjun Mehta").email("arjun.rm@hnigrowth.com")
                        .password(passwordEncoder.encode("Rm@12345")).role(Role.RM)
                        .region("Mumbai").language("Hindi")
                        .specialization("Finance").available(true).build(),
                User.builder().fullName("Nina Verma").email("manager@hnigrowth.com")
                        .password(passwordEncoder.encode("Mgr@1234")).role(Role.MANAGER)
                        .available(false).build(),
                User.builder().fullName("Dev Compliance").email("compliance@hnigrowth.com")
                        .password(passwordEncoder.encode("Cmp@1234")).role(Role.COMPLIANCE)
                        .available(false).build()));

        log.info("[seed] Default users created. Admin login: {} / {}", adminEmail, adminPassword);
    }

    private void seedLeads() {
        if (leadRepository.count() > 0) return;

        List<LeadRequest> samples = List.of(
                new LeadRequest("Vikram Anand", "vikram@google.com", "+91-9000000001",
                        "Google", "Director of Engineering", "Technology", "Hyderabad",
                        18, "https://linkedin.com/in/vikram", LeadSource.REFERRAL,
                        LeadPriority.HIGH, "Referred by existing client", 40),
                new LeadRequest("Priya Nair", "priya@tatacapital.com", "+91-9000000002",
                        "Tata Capital", "VP Finance", "Finance", "Mumbai",
                        12, "https://linkedin.com/in/priya", LeadSource.LINKEDIN,
                        LeadPriority.MEDIUM, null, 10),
                new LeadRequest("Sameer Khan", null, "+91-9000000003",
                        "Local Retailer", "Store Manager", "Retail", "Indore",
                        4, null, LeadSource.COLD_OUTREACH, LeadPriority.LOW, null, 0),
                new LeadRequest("Ananya Rao", "ananya@sequoia.com", "+91-9000000004",
                        "Sequoia", "Partner", "Venture Capital", "Bengaluru",
                        22, "https://linkedin.com/in/ananya", LeadSource.PREMIUM,
                        LeadPriority.CRITICAL, "Attended flagship webinar", 60));

        samples.forEach(s -> leadService.create(s, adminEmail));
        log.info("[seed] {} sample leads created and AI-scored", samples.size());
    }
}
