package com.mams.repository;

import com.mams.entity.Base;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface BaseRepository extends JpaRepository<Base, Long> {
    Optional<Base> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Base b WHERE b.id = :id")
    Optional<Base> findByIdForUpdate(@Param("id") Long id);
}
