package com.mams.service;

import com.mams.audit.AuditService;
import com.mams.entity.Transfer;
import com.mams.exception.BusinessException;
import com.mams.exception.ErrorCode;
import com.mams.repository.BaseRepository;
import com.mams.repository.TransferRepository;
import com.mams.security.AuthUser;
import com.mams.security.BaseAccessService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final BaseRepository baseRepository;
    private final InventoryService inventoryService;
    private final BaseAccessService baseAccessService;
    private final AuditService auditService;

    public TransferService(TransferRepository transferRepository, BaseRepository baseRepository,
                           InventoryService inventoryService, BaseAccessService baseAccessService,
                           AuditService auditService) {
        this.transferRepository = transferRepository;
        this.baseRepository = baseRepository;
        this.inventoryService = inventoryService;
        this.baseAccessService = baseAccessService;
        this.auditService = auditService;
    }

    @Transactional
    public Transfer create(Transfer req) {
        AuthUser user = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (req.getSourceBaseId().equals(req.getDestinationBaseId())) {
            throw new BusinessException(ErrorCode.INVALID_TRANSFER, "Source and destination must differ");
        }

        // Validate RBAC (Base scope)
        baseAccessService.assertCanAccess(req.getSourceBaseId(), user);

        // Lock source base to prevent race conditions during availability check
        baseRepository.findByIdForUpdate(req.getSourceBaseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Source base not found"));

        // Check stock
        long available = inventoryService.available(req.getSourceBaseId(), req.getEquipmentTypeId());
        if (req.getQuantity() > available) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY, "Insufficient available inventory at the source base.");
        }

        req.setStatus("COMPLETED");
        req.setCreatedBy(user.getId());
        req.setCreatedAt(LocalDateTime.now());
        
        Transfer saved = transferRepository.save(req);

        // Record Audit
        auditService.log("CREATE_TRANSFER", "TRANSFER", saved.getId(), saved.getSourceBaseId(), null, saved);

        return saved;
    }
}
