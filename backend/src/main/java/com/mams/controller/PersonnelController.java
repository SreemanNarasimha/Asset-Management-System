package com.mams.controller;

import com.mams.entity.Personnel;
import com.mams.repository.PersonnelRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/personnel")
public class PersonnelController {

    private final PersonnelRepository personnelRepository;
    private final com.mams.audit.AuditService auditService;

    public PersonnelController(PersonnelRepository personnelRepository, com.mams.audit.AuditService auditService) {
        this.personnelRepository = personnelRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public List<Personnel> getAll() {
        return personnelRepository.findAll();
    }

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public Personnel create(@RequestBody Personnel personnel) {
        personnel.setStatus("ACTIVE");
        Personnel saved = personnelRepository.save(personnel);
        auditService.log("CREATE_PERSONNEL", "PERSONNEL", saved.getId(), saved.getBaseId(), null, saved);
        return saved;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        personnelRepository.deleteById(id);
    }
}

