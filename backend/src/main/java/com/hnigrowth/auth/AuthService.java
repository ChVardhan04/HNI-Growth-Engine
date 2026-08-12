package com.hnigrowth.auth;

import com.hnigrowth.auth.dto.AuthResponse;
import com.hnigrowth.auth.dto.LoginRequest;
import com.hnigrowth.auth.dto.RegisterRequest;
import com.hnigrowth.audit.AuditAction;
import com.hnigrowth.audit.AuditService;
import com.hnigrowth.common.exception.BadRequestException;
import com.hnigrowth.security.jwt.JwtService;
import com.hnigrowth.user.User;
import com.hnigrowth.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final com.hnigrowth.security.CustomUserDetailsService userDetailsService;
    private final AuditService auditService;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BadRequestException("Email already registered: " + req.email());
        }
        User user = User.builder()
                .fullName(req.fullName())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .role(req.role())
                .region(req.region())
                .language(req.language())
                .specialization(req.specialization())
                .available(true)
                .enabled(true)
                .build();
        userRepository.save(user);
        auditService.log(AuditAction.CREATE, "User", user.getId(), req.email(),
                "Registered user with role " + req.role());
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadRequestException("User not found"));
        auditService.log(AuditAction.LOGIN, "User", user.getId(), req.email(), "User logged in");
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(details, Map.of(
                "role", user.getRole().name(),
                "name", user.getFullName()));
        return new AuthResponse(user.getId(), token, "Bearer", user.getFullName(), user.getEmail(), user.getRole().name());
    }
}
