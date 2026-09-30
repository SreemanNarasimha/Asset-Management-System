package com.mams.controller;

import com.mams.entity.UserSession;
import com.mams.repository.UserSessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final UserSessionRepository userSessionRepository;
    private final JdbcTemplate jdbcTemplate;

    public SessionController(UserSessionRepository userSessionRepository, JdbcTemplate jdbcTemplate) {
        this.userSessionRepository = userSessionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserSession> listSessions(
            @RequestParam(required = false) Long userId,
            Pageable pageable) {
        if (userId != null) {
            return userSessionRepository.findByUserId(userId, pageable);
        }
        return userSessionRepository.findAll(pageable);
    }

    @GetMapping("/{id}/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> getSessionAuditLogs(@PathVariable Long id) {
        return jdbcTemplate.queryForList("SELECT * FROM audit_logs WHERE session_id = ? ORDER BY created_at ASC", id);
    }
}
