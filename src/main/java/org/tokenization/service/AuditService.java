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
}