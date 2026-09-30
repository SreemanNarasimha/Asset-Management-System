package com.mams.service;

import com.mams.audit.AuditService;
import com.mams.entity.Assignment;
import com.mams.exception.BusinessException;
import com.mams.exception.ErrorCode;
import com.mams.repository.AssignmentRepository;
import com.mams.repository.BaseRepository;
import com.mams.repository.PersonnelRepository;
import com.mams.security.AuthUser;
import com.mams.security.BaseAccessService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final BaseRepository baseRepository;
    private final PersonnelRepository personnelRepository;
    private final InventoryService inventoryService;
    private final BaseAccessService baseAccessService;
    private final AuditService auditService;

    public AssignmentService(AssignmentRepository assignmentRepository, BaseRepository baseRepository,
                             PersonnelRepository personnelRepository, InventoryService inventoryService,
                             BaseAccessService baseAccessService, AuditService auditService) {
        this.assignmentRepository = assignmentRepository;
        this.baseRepository = baseRepository;
        this.personnelRepository = personnelRepository;
        this.inventoryService = inventoryService;
        this.baseAccessService = baseAccessService;
        this.auditService = auditService;
    }

    @Transactional
    public Assignment create(Assignment req) {
        AuthUser user = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        baseAccessService.assertCanAccess(req.getBaseId(), user);

        baseRepository.findByIdForUpdate(req.getBaseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Base not found"));

        var personnel = personnelRepository.findById(req.getPersonnelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Personnel not found"));

        if (!personnel.getBaseId().equals(req.getBaseId())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Personnel must belong to the same base");
        }

        long available = inventoryService.available(req.getBaseId(), req.getEquipmentTypeId());
        if (req.getQuantity() > available) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_INVENTORY, "Insufficient available inventory");
        }

        req.setStatus("ACTIVE");
        req.setCreatedBy(user.getId());
        req.setCreatedAt(LocalDateTime.now());
        req.setUpdatedAt(LocalDateTime.now());

        Assignment saved = assignmentRepository.save(req);
        auditService.log("CREATE_ASSIGNMENT", "ASSIGNMENT", saved.getId(), saved.getBaseId(), null, saved);
        return saved;
    }

    @Transactional
    public Assignment returnAssignment(Long assignmentId) {
        AuthUser user = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Assignment not found"));

        baseAccessService.assertCanAccess(assignment.getBaseId(), user);

        if (!"ACTIVE".equals(assignment.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Assignment is already returned");
        }

        Assignment oldState = new Assignment();
        oldState.setStatus(assignment.getStatus());

        assignment.setStatus("RETURNED");
        assignment.setReturnedAt(LocalDateTime.now());
        assignment.setReturnedBy(user.getId());
        assignment.setUpdatedAt(LocalDateTime.now());

        Assignment saved = assignmentRepository.save(assignment);
        auditService.log("RETURN_ASSIGNMENT", "ASSIGNMENT", saved.getId(), saved.getBaseId(), oldState, saved);
        return saved;
    }
}
