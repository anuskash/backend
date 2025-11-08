package com.uon.marketplace.repositories;

import com.uon.marketplace.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Add custom query methods if needed
}
