package com.uon.marketplace.services;

import com.uon.marketplace.entities.AuditLog;
import com.uon.marketplace.repositories.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {
    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog logEvent(Long userId, String action, String targetType, String targetId, String details) {
        AuditLog log = new AuditLog(userId, action, targetType, targetId, details);
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    // Add filtering methods as needed
}
