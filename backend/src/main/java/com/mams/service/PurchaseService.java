package com.mams.service;

import com.mams.audit.AuditService;
import com.mams.entity.Purchase;
import com.mams.repository.PurchaseRepository;
import com.mams.security.AuthUser;
import com.mams.security.BaseAccessService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final BaseAccessService baseAccessService;
    private final AuditService auditService;

    public PurchaseService(PurchaseRepository purchaseRepository, BaseAccessService baseAccessService, AuditService auditService) {
        this.purchaseRepository = purchaseRepository;
        this.baseAccessService = baseAccessService;
        this.auditService = auditService;
    }

    @Transactional
    public Purchase create(Purchase req) {
        AuthUser user = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        baseAccessService.assertCanAccess(req.getBaseId(), user);

        req.setCreatedBy(user.getId());
        req.setCreatedAt(LocalDateTime.now());
        
        Purchase saved = purchaseRepository.save(req);
        auditService.log("CREATE_PURCHASE", "PURCHASE", saved.getId(), saved.getBaseId(), null, saved);
        return saved;
    }
}
