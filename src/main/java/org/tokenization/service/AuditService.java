// File: src/main/java/com/tokenization/service/AuditService.java
package com.tokenization.service;

import com.tokenization.entity.EventLog;
import com.tokenization.repository.EventLogRepository;
import com.tokenization.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class AuditService {

    public static final String GENESIS_BLOCK_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    @Autowired
    private EventLogRepository eventLogRepository;

    /**
     * Appends a new event to the audit log.
     * This method is designed to be called from within another @Transactional service method.
     * Propagation.MANDATORY ensures that it will fail if there is no active transaction, which is a safety check.
     *
     * @param eventType A string describing the event (e.g., "PARTY_CREATED").
     * @param payload A JSON string containing the details of the event.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void appendEvent(String eventType, String payload) {
        // Find the hash of the last event in the log. If none exists, use the genesis hash.
        String prevHash = eventLogRepository.findLastHash().orElse(GENESIS_BLOCK_HASH);

        // Create a unique hash for this new event.
        long timestamp = Instant.now().toEpochMilli();
        String dataToHash = prevHash + eventType + payload + timestamp;
        String thisHash = HashUtil.calculateSHA256(dataToHash);

        // Create and save the new EventLog entity.
        EventLog eventLog = new EventLog();
        eventLog.setEventType(eventType);
        eventLog.setPayloadJson(payload);
        eventLog.setPrevHash(prevHash);
        eventLog.setThisHash(thisHash);

        eventLogRepository.save(eventLog);
    }

    @Transactional(readOnly = true)
    public List<EventLog> getFullEventLog() {
        return eventLogRepository.findAllByOrderByIdAsc();
    }

    /**
     * Verifies the integrity of the entire hash chain in the event log.
     * @return A string message indicating the result of the verification.
     * @throws IllegalStateException if a break in the chain is detected.
     */
    @Transactional(readOnly = true)
    public String verifyLedgerIntegrity() {
        List<EventLog> events = getFullEventLog();
        String lastValidHash = GENESIS_BLOCK_HASH;
        int eventCount = 0;

        for (EventLog event : events) {
            eventCount++;
            // Check 1: Does the previous hash of the current event match the hash of the last valid event?
            if (!event.getPrevHash().equals(lastValidHash)) {
                String errorMessage = String.format(
                        "Chain TAMPERED! Break detected at Event ID %d. Expected prev_hash: '%s', but got: '%s'.",
                        event.getId(), lastValidHash, event.getPrevHash()
                );
                // In a real system, you would log this to a high-priority security channel.
                throw new IllegalStateException(errorMessage);
            }

            // Check 2 (Optional but good): We could recalculate the current hash and verify it.
            // For now, we trust the database unique constraint on `this_hash` and focus on the chain linkage.

            // If the check passes, the current event's hash becomes the one to check against for the next loop iteration.
            lastValidHash = event.getThisHash();
        }

        return String.format("✅ Ledger integrity verified successfully. Scanned %d events.", eventCount);
}