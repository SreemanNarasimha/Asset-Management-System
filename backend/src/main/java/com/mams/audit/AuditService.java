package com.mams.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.mams.security.AuthUser;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AuditService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    // Runs in the same transaction as the calling business method
    @Transactional(propagation = Propagation.MANDATORY)
    public void log(String action, String entityType, Long entityId, Long baseId, Object oldValue, Object newValue) {
        AuthUser user = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        String oldValStr = null;
        String newValStr = null;
        
        try {
            if (oldValue != null) oldValStr = objectMapper.writeValueAsString(oldValue);
            if (newValue != null) newValStr = objectMapper.writeValueAsString(newValue);
        } catch (Exception ignored) {}

        String sql = "INSERT INTO audit_logs (user_id, role, action, entity_type, entity_id, base_id, old_value, new_value, result, created_at, session_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'SUCCESS', ?, ?)";
        
        jdbcTemplate.update(sql, user.getId(), user.getRoleName(), action, entityType, entityId, baseId, oldValStr, newValStr, LocalDateTime.now(), user.getSessionId());
    }
}
