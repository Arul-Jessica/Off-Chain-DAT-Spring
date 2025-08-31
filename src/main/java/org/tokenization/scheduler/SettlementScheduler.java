package com.tokenization.scheduler;

import com.tokenization.entity.Trade;
import com.tokenization.repository.TradeRepository;
import com.tokenization.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class SettlementScheduler {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private SettlementService settlementService;

    // Run this scheduler every 15 seconds.
    @Scheduled(fixedDelay = 15000)
    public void runSettlementProcessor() {
        System.out.println("--- Running Settlement Scheduler ---");
        List<Trade> unsettledTrades = tradeRepository.findByStatus("UNSETTLED");

        if (unsettledTrades.isEmpty()) {
            System.out.println("No unsettled trades found. Skipping settlement run.");
            return;
        }

        System.out.printf("Found %d trades to settle.%n", unsettledTrades.size());
        for (Trade trade : unsettledTrades) {
            try {
                settlementService.settleTrade(trade);
                System.out.printf("Successfully settled trade ID: %d%n", trade.getId());
            } catch (Exception e) {
                System.err.printf("Failed to settle trade ID %d: %s%n", trade.getId(), e.getMessage());
                // In a real system, you might mark the trade as "FAILED" here.
            }
        }
    }
}