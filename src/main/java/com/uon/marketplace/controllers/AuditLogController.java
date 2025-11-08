package com.uon.marketplace.controllers;

import com.uon.marketplace.entities.AuditLog;
import com.uon.marketplace.services.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize; // Removed for development
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/audit-logs")
// @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')") // Removed for easier frontend access during development
public class AuditLogController {
    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    // Optionally add filtering by user, action, date, etc.
}
