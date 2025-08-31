// In CashLedgerRepository.java
package com.tokenization.repository;

// ... imports
import java.util.Optional;
import java.util.List;

@Repository
public interface CashLedgerRepository extends JpaRepository<CashLedger, Long> {
    // Find a specific cash balance for a wallet and currency.
    Optional<CashLedger> findByWalletIdAndCurrency(Long walletId, String currency);

    // Find all cash balances for a given wallet.
    List<CashLedger> findByWalletId(Long walletId);
}