// In AssetRepository.java
package com.tokenization.repository;

// ... imports
import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    // This query finds the unique asset IDs that have open orders.
    // This makes our matching engine efficient, as it won't check assets with no activity.
    @Query("SELECT DISTINCT o.assetId FROM Order o WHERE o.status = 'OPEN'")
    List<Long> findAssetIdsWithOpenOrders();
}