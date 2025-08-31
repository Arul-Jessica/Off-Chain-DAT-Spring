package com.tokenization.scheduler;

import com.tokenization.repository.AssetRepository;
import com.tokenization.service.TradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchingEngineScheduler {

    @Autowired
    private TradingService tradingService;

    @Autowired
    private AssetRepository assetRepository;

    // This annotation tells Spring to run this method on a schedule.
    // fixedDelay = 10000 means "run this method, wait 10 seconds after it finishes, then run it again."
    @Scheduled(fixedDelay = 10000)
    public void runMatchingEngine() {
        System.out.println("--- Running Matching Engine Scheduler ---");

        // 1. Find all assets that have open orders to be efficient.
        List<Long> assetIdsToMatch = assetRepository.findAssetIdsWithOpenOrders();

        if (assetIdsToMatch.isEmpty()) {
            System.out.println("No open orders found for any assets. Skipping matching run.");
            return;
        }

        // 2. Run the matching logic for each of those assets.
        for (Long assetId : assetIdsToMatch) {
            try {
                tradingService.runMatchingEngineForAsset(assetId);
            } catch (Exception e) {
                // In a real system, you would log this error much more robustly.
                System.err.printf("Error during matching for asset ID %d: %s%n", assetId, e.getMessage());
            }
        }
    }
}