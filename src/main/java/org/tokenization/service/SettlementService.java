package com.tokenization.service;

import com.tokenization.entity.*;
import com.tokenization.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class SettlementService {

    @Autowired private TradeRepository tradeRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PositionRepository positionRepository;
    @Autowired private CashLedgerRepository cashLedgerRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private AuditService auditService;

    // This is the core of the DvP (Delivery versus Payment) process.
    // It's marked as @Transactional, so if ANY part of this fails, the entire
    // operation is rolled back, guaranteeing no party loses funds or assets.
    @Transactional
    public void settleTrade(Trade trade) {
        // 1. Get Buyer and Seller from the original orders
        Order buyOrder = orderRepository.findById(trade.getBuyOrderId()).orElseThrow();
        Order sellOrder = orderRepository.findById(trade.getSellOrderId()).orElseThrow();

        Wallet buyerAssetWallet = walletRepository.findById(buyOrder.getWalletId()).orElseThrow();
        Wallet sellerAssetWallet = walletRepository.findById(sellOrder.getWalletId()).orElseThrow();

        // For simplicity, we assume the CASH wallet is owned by the same party.
        Wallet buyerCashWallet = findCashWalletByPartyId(buyerAssetWallet.getPartyId());
        Wallet sellerCashWallet = findCashWalletByPartyId(sellerAssetWallet.getPartyId());

        BigDecimal totalCost = trade.getQuantity().multiply(trade.getPrice());

        // 2. Debit Seller's Tokens
        Position sellerPosition = positionRepository.findByWalletIdAndAssetId(sellerAssetWallet.getId(), trade.getAssetId()).orElseThrow();
        if (sellerPosition.getQuantity().compareTo(trade.getQuantity()) < 0) {
            throw new IllegalStateException("Settlement failed: Seller has insufficient tokens. Trade ID: " + trade.getId());
        }
        sellerPosition.setQuantity(sellerPosition.getQuantity().subtract(trade.getQuantity()));
        positionRepository.save(sellerPosition);

        // 3. Debit Buyer's Cash (assuming USD for now)
        CashLedger buyerCash = cashLedgerRepository.findByWalletIdAndCurrency(buyerCashWallet.getId(), "USD").orElseThrow();
        if (buyerCash.getAmount().compareTo(totalCost) < 0) {
            throw new IllegalStateException("Settlement failed: Buyer has insufficient cash. Trade ID: " + trade.getId());
        }
        buyerCash.setAmount(buyerCash.getAmount().subtract(totalCost));
        cashLedgerRepository.save(buyerCash);

        // 4. Credit Buyer's Tokens
        Position buyerPosition = positionRepository.findByWalletIdAndAssetId(buyerAssetWallet.getId(), trade.getAssetId())
                .orElseGet(() -> createNewPosition(buyerAssetWallet.getId(), trade.getAssetId()));
        buyerPosition.setQuantity(buyerPosition.getQuantity().add(trade.getQuantity()));
        positionRepository.save(buyerPosition);

        // 5. Credit Seller's Cash
        CashLedger sellerCash = cashLedgerRepository.findByWalletIdAndCurrency(sellerCashWallet.getId(), "USD")
                .orElseGet(() -> createNewCashLedger(sellerCashWallet.getId(), "USD"));
        sellerCash.setAmount(sellerCash.getAmount().add(totalCost));
        cashLedgerRepository.save(sellerCash);

        // 6. Mark Trade as SETTLED
        trade.setStatus("SETTLED");
        tradeRepository.save(trade);

        // 7. Log the settlement event
        String payload = String.format("{\"tradeId\": %d, \"status\": \"SETTLED\"}", trade.getId());
        auditService.appendEvent("TRADE_SETTLED", payload);
    }

    // Helper methods
    private Wallet findCashWalletByPartyId(Long partyId) {
        return walletRepository.findByPartyId(partyId).stream()
                .filter(w -> "CASH".equals(w.getWalletType())).findFirst().orElseThrow();
    }
    private Position createNewPosition(Long walletId, Long assetId) {
        Position p = new Position();
        p.setWalletId(walletId);
        p.setAssetId(assetId);
        p.setQuantity(BigDecimal.ZERO);
        return p;
    }
    private CashLedger createNewCashLedger(Long walletId, String currency) {
        CashLedger cl = new CashLedger();
        cl.setWalletId(walletId);
        cl.setCurrency(currency);
        cl.setAmount(BigDecimal.ZERO);
        return cl;
    }
}