package com.mams.controller;

import com.mams.entity.Transfer;
import com.mams.service.TransferService;
import com.mams.repository.TransferRepository;
import com.mams.security.AuthUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;
    private final TransferRepository transferRepository;

    public TransferController(TransferService transferService, TransferRepository transferRepository) {
        this.transferService = transferService;
        this.transferRepository = transferRepository;
    }

    @PostMapping
    public Transfer create(@RequestBody Transfer req) {
        return transferService.create(req);
    }

    @GetMapping
    public List<Transfer> getAll() {
        List<Transfer> transfers = transferRepository.findAll();
        AuthUser user = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!"ADMIN".equals(user.getRoleName())) {
            transfers = transfers.stream()
                .filter(t -> user.getBaseId().equals(t.getSourceBaseId()) || user.getBaseId().equals(t.getDestinationBaseId()))
                .collect(Collectors.toList());
        }
        return transfers;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        transferRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public Transfer update(@PathVariable Long id, @RequestBody Transfer req) {
        Transfer existing = transferRepository.findById(id).orElseThrow();
        req.setId(id);
        
        // Retain original fields if needed
        
        
        Transfer saved = transferRepository.save(req);
        
        return saved;
    }
}
