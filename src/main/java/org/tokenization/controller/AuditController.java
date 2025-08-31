package com.tokenization.controller;

import com.tokenization.dto.GenericResponse;
import com.tokenization.entity.EventLog;
import com.tokenization.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/events")
    @Operation(summary = "Get the full event log (Admin function)",
            description = "Retrieves the entire history of all actions recorded in the tamper-evident log.")
    // NOTE: When security is added, this will be locked down to ADMINS only.
    public ResponseEntity<List<EventLog>> getFullEventLog() {
        List<EventLog> events = auditService.getFullEventLog();
        return ResponseEntity.ok(events);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify the integrity of the ledger (Admin function)",
            description = "Scans the entire event log and verifies the hash chain to ensure no tampering has occurred.")
    // NOTE: When security is added, this will be locked down to ADMINS only.
    public ResponseEntity<GenericResponse> verifyLedger() {
        try {
            String verificationResult = auditService.verifyLedgerIntegrity();
            return ResponseEntity.ok(new GenericResponse(verificationResult));
        } catch (IllegalStateException e) {
            // If the service throws our specific exception, return a 422 Unprocessable Entity status code.
            return ResponseEntity.status(422).body(new GenericResponse(e.getMessage()));
        }
    }
}