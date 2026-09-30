package com.mams.controller;

import com.mams.security.AuthUser;
import com.mams.security.BaseAccessService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final JdbcTemplate jdbcTemplate;
    private final BaseAccessService baseAccessService;

    public AuditLogController(JdbcTemplate jdbcTemplate, BaseAccessService baseAccessService) {
        this.jdbcTemplate = jdbcTemplate;
        this.baseAccessService = baseAccessService;
    }

    @GetMapping
    public List<Map<String, Object>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long baseId,
            @AuthenticationPrincipal AuthUser user) {

        StringBuilder sql = new StringBuilder("""
            SELECT al.*, u.username 
            FROM audit_logs al 
            LEFT JOIN users u ON al.user_id = u.id 
            WHERE 1=1 
        """);
        List<Object> params = new ArrayList<>();

        if ("LOGISTICS_OFFICER".equals(user.getRoleName())) {
            sql.append(" AND al.user_id = ?");
            params.add(user.getId());
        } else if ("BASE_COMMANDER".equals(user.getRoleName())) {
            sql.append(" AND al.base_id = ?");
            params.add(user.getBaseId());
        }

        if (action != null && !action.isBlank()) {
            sql.append(" AND al.action = ?");
            params.add(action);
        }
        if (entityType != null && !entityType.isBlank()) {
            sql.append(" AND al.entity_type = ?");
            params.add(entityType);
        }
        if (baseId != null) {
            sql.append(" AND al.base_id = ?");
            params.add(baseId);
        }

        sql.append(" ORDER BY al.created_at DESC");

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }
}
