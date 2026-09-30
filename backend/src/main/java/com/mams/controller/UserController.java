package com.mams.controller;

import com.mams.entity.User;
import com.mams.repository.UserRepository;
import com.mams.repository.RoleRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.mams.audit.AuditService auditService;

    public UserController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, com.mams.audit.AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional
    public User create(@RequestBody User req) {
        req.setPasswordHash(passwordEncoder.encode(req.getPasswordHash())); // Assume frontend sends plain text in passwordHash for ease
        req.setRole(roleRepository.findById(req.getRole().getId()).orElseThrow());
        req.setStatus("ACTIVE");
        req.setCreatedAt(LocalDateTime.now());
        req.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(req);
        auditService.log("CREATE_USER", "USER", saved.getId(), saved.getBaseId(), null, saved);
        return saved;
    }

    @GetMapping
    public java.util.List<User> getAll() {
        return userRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public User update(@PathVariable Long id, @RequestBody User req) {
        User existing = userRepository.findById(id).orElseThrow();
        req.setId(id);
        
        // Retain original fields if needed
        req.setPasswordHash(existing.getPasswordHash());
        if (req.getRole() != null && req.getRole().getId() != null) req.setRole(roleRepository.findById(req.getRole().getId()).orElseThrow());
        else req.setRole(existing.getRole());

        
        User saved = userRepository.save(req);
        auditService.log("UPDATE_USER", "USER", saved.getId(), saved.getBaseId(), null, saved);
        return saved;
    }
}
