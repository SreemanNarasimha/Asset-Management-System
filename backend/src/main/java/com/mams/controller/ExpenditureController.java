package com.mams.controller;

import com.mams.entity.Expenditure;
import com.mams.service.ExpenditureService;
import com.mams.repository.ExpenditureRepository;
import com.mams.security.AuthUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenditures")
public class ExpenditureController {

    private final ExpenditureService expenditureService;
    private final ExpenditureRepository expenditureRepository;

    public ExpenditureController(ExpenditureService expenditureService, ExpenditureRepository expenditureRepository) {
        this.expenditureService = expenditureService;
        this.expenditureRepository = expenditureRepository;
    }

    @PostMapping
    public Expenditure create(@RequestBody Expenditure req) {
        return expenditureService.create(req);
    }

    @GetMapping
    public List<Expenditure> getAll() {
        List<Expenditure> expenditures = expenditureRepository.findAll();
        AuthUser user = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!"ADMIN".equals(user.getRoleName())) {
            expenditures = expenditures.stream()
                .filter(e -> user.getBaseId().equals(e.getBaseId()))
                .collect(Collectors.toList());
        }
        return expenditures;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        expenditureRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public Expenditure update(@PathVariable Long id, @RequestBody Expenditure req) {
        Expenditure existing = expenditureRepository.findById(id).orElseThrow();
        req.setId(id);
        
        // Retain original fields if needed
        
        
        Expenditure saved = expenditureRepository.save(req);
        
        return saved;
    }
}
