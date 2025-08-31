package org.tokenization.service;

import org.tokenization.entity.EventLog;
import org.tokenization.repository.EventLogRepository;
import org.tokenization.util.HashUtil; // We will create this utility class next
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

    // This annotation ensures that this method runs within an existing transaction.
    // It doesn't start a new one, which is exactly what we need.
    @Transactional(propagation = Propagation.MANDATORY)
    public void appendEvent(String eventType, String payload) {
        // Find the last event to get its hash
        String prevHash = eventLogRepository.findLastHash()
                .orElse(GENESIS_BLOCK_HASH);

        long timestamp = Instant.now().toEpochMilli();
        String dataToHash = prevHash + eventType + payload + timestamp;
        String thisHash = HashUtil.calculateSHA256(dataToHash);

        EventLog eventLog = new EventLog();
        eventLog.setEventType(eventType);
        eventLog.setPayloadJson(payload);
        eventLog.setPrevHash(prevHash);
        eventLog.setThisHash(thisHash);

        eventLogRepository.save(eventLog);
    }
}