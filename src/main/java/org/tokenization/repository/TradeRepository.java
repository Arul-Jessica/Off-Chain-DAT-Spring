// In TradeRepository.java
package com.tokenization.repository;

// ... imports
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    // Find all trades that are waiting for settlement.
    List<Trade> findByStatus(String status);
}