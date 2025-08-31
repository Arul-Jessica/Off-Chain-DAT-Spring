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

    // This ensures this method MUST run inside an existing transaction.
    @Transactional(propagation = Propagation.MANDATORY)
    public void appendEvent(String eventType, String payload) {
        String prevHash = eventLogRepository.findLastHash().orElse(GENESIS_BLOCK_HASH);

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