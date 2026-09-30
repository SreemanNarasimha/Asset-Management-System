package com.mams.service;

import com.mams.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SessionCleanupService {

    private final UserSessionRepository userSessionRepository;
    private final long jwtExpirationMs;

    public SessionCleanupService(UserSessionRepository userSessionRepository, @Value("${jwt.expiration}") long jwtExpirationMs) {
        this.userSessionRepository = userSessionRepository;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    @Scheduled(fixedRateString = "${jwt.expiration}")
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(jwtExpirationMs / 1000);
        userSessionRepository.markExpiredSessions(threshold);
    }
}
