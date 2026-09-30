package com.mams.repository;

import com.mams.entity.Personnel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PersonnelRepository extends JpaRepository<Personnel, Long> {
    Optional<Personnel> findByServiceNumber(String serviceNumber);
}
