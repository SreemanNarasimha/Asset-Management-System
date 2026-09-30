package com.mams.service;

import com.mams.audit.AuditService;
import com.mams.entity.Expenditure;
import com.mams.exception.BusinessException;
import com.mams.exception.ErrorCode;
import com.mams.repository.BaseRepository;
import com.mams.repository.ExpenditureRepository;
import com.mams.security.AuthUser;
import com.mams.security.BaseAccessService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ExpenditureService {

    private final ExpenditureRepository expenditureRepository;
    private final BaseRepository baseRepository;
    private final InventoryService inventoryService;
    private final BaseAccessService baseAccessService;
    private final AuditService auditService;

    public ExpenditureService(ExpenditureRepository expenditureRepository, BaseRepository baseRepository,
                              InventoryService inventoryService, BaseAccessService baseAccessService, AuditService auditService) {
        this.expenditureRepository = expenditureRepository;
        this.baseRepository = baseRepository;
        this.inventoryService = inventoryService;
        this.baseAccessService = baseAccessService;
        this.auditService = auditService;
    }

    @Transactional
    public Expenditure create(Expenditure req) {
        AuthUser user = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        baseAccessService.assertCanAccess(req.getBaseId(), user);

        baseRepository.findByIdForUpdate(req.getBaseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Base not found"));

        long available = inventoryService.available(req.getBaseId(), req.getEquipmentTypeId());
        if (req.getQuantity() > available) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY, "Insufficient available inventory");
        }

        req.setCreatedBy(user.getId());
        req.setCreatedAt(LocalDateTime.now());
        
        Expenditure saved = expenditureRepository.save(req);
        auditService.log("CREATE_EXPENDITURE", "EXPENDITURE", saved.getId(), saved.getBaseId(), null, saved);
        return saved;
    }
}
