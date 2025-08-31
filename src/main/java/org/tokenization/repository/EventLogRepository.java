package org.tokenization.repository;

import org.tokenization.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    // JPQL query to find the hash of the most recent event
    @Query("SELECT e.thisHash FROM EventLog e ORDER BY e.id DESC LIMIT 1")
    Optional<String> findLastHash();
}