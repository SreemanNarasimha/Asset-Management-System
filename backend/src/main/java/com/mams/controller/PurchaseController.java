package com.mams.controller;

import com.mams.entity.Purchase;
import com.mams.service.PurchaseService;
import com.mams.repository.PurchaseRepository;
import com.mams.security.AuthUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseRepository purchaseRepository;

    public PurchaseController(PurchaseService purchaseService, PurchaseRepository purchaseRepository) {
        this.purchaseService = purchaseService;
        this.purchaseRepository = purchaseRepository;
    }

    @PostMapping
    public Purchase create(@RequestBody Purchase req) {
        return purchaseService.create(req);
    }

    @GetMapping
    public List<Purchase> getAll(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long equipmentTypeId) {
        List<Purchase> purchases = purchaseRepository.findAll();
        
        AuthUser user = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!"ADMIN".equals(user.getRoleName())) {
            purchases = purchases.stream()
                .filter(p -> user.getBaseId().equals(p.getBaseId()))
                .collect(Collectors.toList());
        }
        
        if (date != null && !date.trim().isEmpty()) {
            purchases = purchases.stream()
                .filter(p -> p.getPurchaseDate() != null && p.getPurchaseDate().toString().compareTo(date) <= 0)
                .collect(Collectors.toList());
        }
        
        if (equipmentTypeId != null) {
            purchases = purchases.stream()
                .filter(p -> equipmentTypeId.equals(p.getEquipmentTypeId()))
                .collect(Collectors.toList());
        }
        
        return purchases;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        purchaseRepository.deleteById(id);
    }
}

