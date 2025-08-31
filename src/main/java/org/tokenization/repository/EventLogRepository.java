package com.tokenization.repository;

import com.tokenization.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    // This custom query finds the 'thisHash' of the most recently created event.
    @Query(value = "SELECT e.this_hash FROM AJ_EVENT_LOG e ORDER BY e.id DESC FETCH FIRST 1 ROW ONLY", nativeQuery = true)
    Optional<String> findLastHash();

    List<EventLog> findAllByOrderByIdAsc();
}