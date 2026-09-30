package com.mams.controller;

import com.mams.entity.EquipmentType;
import com.mams.repository.EquipmentTypeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/equipment-types")
@PreAuthorize("hasRole('ADMIN')")
public class EquipmentTypeController {

    private final EquipmentTypeRepository equipmentTypeRepository;
    private final com.mams.audit.AuditService auditService;

    public EquipmentTypeController(EquipmentTypeRepository equipmentTypeRepository, com.mams.audit.AuditService auditService) {
        this.equipmentTypeRepository = equipmentTypeRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public List<EquipmentType> getAll() {
        return equipmentTypeRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional
    public EquipmentType create(@RequestBody EquipmentType equipmentType) {
        equipmentType.setStatus("ACTIVE");
        EquipmentType saved = equipmentTypeRepository.save(equipmentType);
        auditService.log("CREATE_EQUIPMENT_TYPE", "EQUIPMENT_TYPE", saved.getId(), null, null, saved);
        return saved;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        equipmentTypeRepository.deleteById(id);
    }
}

