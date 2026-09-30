package com.mams.repository;

import com.mams.entity.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    Page<UserSession> findAll(Pageable pageable);
    
    Page<UserSession> findByUserId(Long userId, Pageable pageable);
    
    @Modifying
    @Query("UPDATE UserSession u SET u.status = 'EXPIRED' WHERE u.status = 'ACTIVE' AND u.loginTime < :threshold")
    int markExpiredSessions(LocalDateTime threshold);
}
