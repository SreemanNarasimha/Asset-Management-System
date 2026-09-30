package com.mams.controller;

import com.mams.entity.Base;
import com.mams.repository.BaseRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bases")
@PreAuthorize("hasRole('ADMIN')")
public class BaseController {

    private final BaseRepository baseRepository;
    private final com.mams.audit.AuditService auditService;

    public BaseController(BaseRepository baseRepository, com.mams.audit.AuditService auditService) {
        this.baseRepository = baseRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public List<Base> getAll() {
        return baseRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional
    public Base create(@RequestBody Base base) {
        base.setStatus("ACTIVE");
        Base saved = baseRepository.save(base);
        auditService.log("CREATE_BASE", "BASE", saved.getId(), saved.getId(), null, saved);
        return saved;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        baseRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public Base update(@PathVariable Long id, @RequestBody Base req) {
        Base existing = baseRepository.findById(id).orElseThrow();
        req.setId(id);
        
        // Retain original fields if needed
        
        
        Base saved = baseRepository.save(req);
        auditService.log("UPDATE_BASE", "BASE", saved.getId(), saved.getId(), null, saved);
        return saved;
    }
}
