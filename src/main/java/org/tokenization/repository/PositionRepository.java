// In PositionRepository.java
package com.tokenization.repository;

// ... imports
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    // Add this new method
    Optional<Position> findByWalletIdAndAssetId(Long walletId, Long assetId);
    // In PositionRepository.java
    List<Position> findByWalletId(Long walletId);
}