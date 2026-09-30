package com.mams.controller;

import com.mams.service.InventoryService;
import com.mams.security.AuthUser;
import com.mams.security.BaseAccessService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final InventoryService inventoryService;
    private final BaseAccessService baseAccessService;

    public DashboardController(InventoryService inventoryService, BaseAccessService baseAccessService) {
        this.inventoryService = inventoryService;
        this.baseAccessService = baseAccessService;
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) String date,
            @AuthenticationPrincipal AuthUser user) {
        
        Long resolvedBaseId = baseAccessService.resolveBaseId(baseId, user);
        
        return inventoryService.getDashboardMetrics(resolvedBaseId, equipmentTypeId, date);
    }

    @GetMapping("/inventory")
    public java.util.List<Map<String, Object>> getInventory(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) String date,
            @AuthenticationPrincipal AuthUser user) {
        
        Long resolvedBaseId = baseAccessService.resolveBaseId(baseId, user);
        
        return inventoryService.getInventoryGrid(resolvedBaseId, equipmentTypeId, date);
    }
}
