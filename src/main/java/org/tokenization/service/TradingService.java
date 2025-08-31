package com.tokenization.service;

import com.tokenization.dto.PlaceOrderRequest;
import com.tokenization.dto.TransferRequest;
import com.tokenization.entity.Order;
import com.tokenization.entity.Position;
import com.tokenization.entity.Wallet;
import com.tokenization.repository.OrderRepository;
import com.tokenization.repository.PositionRepository;
import com.tokenization.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TradingService {

    @Autowired private WalletRepository walletRepository;
    @Autowired private PositionRepository positionRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private AuditService auditService;

    @Transactional
    public void transferTokens(TransferRequest request) {
        // 1. Find the ASSET wallets for both sender and receiver
        Wallet fromWallet = findAssetWalletByPartyId(request.getFromPartyId());
        Wallet toWallet = findAssetWalletByPartyId(request.getToPartyId());

        // 2. Debit the sender's position
        Position fromPosition = positionRepository.findByWalletIdAndAssetId(fromWallet.getId(), request.getAssetId())
                .orElseThrow(() -> new IllegalArgumentException("Sender does not own asset " + request.getAssetId()));

        if (fromPosition.getQuantity().compareTo(request.getQuantity()) < 0) {
            throw new IllegalStateException("Insufficient funds for transfer.");
        }
        fromPosition.setQuantity(fromPosition.getQuantity().subtract(request.getQuantity()));
        positionRepository.save(fromPosition);

        // 3. Credit the receiver's position (or create a new one)
        Position toPosition = positionRepository.findByWalletIdAndAssetId(toWallet.getId(), request.getAssetId())
                .orElseGet(() -> createNewPosition(toWallet.getId(), request.getAssetId()));
        toPosition.setQuantity(toPosition.getQuantity().add(request.getQuantity()));
        positionRepository.save(toPosition);

        // 4. Record the audit event
        String payload = String.format(
                "{\"fromPartyId\": %d, \"toPartyId\": %d, \"assetId\": %d, \"quantity\": %s}",
                request.getFromPartyId(), request.getToPartyId(), request.getAssetId(), request.getQuantity().toPlainString()
        );
        auditService.appendEvent("TOKEN_TRANSFERRED", payload);
    }

    @Transactional
    public Order placeOrder(PlaceOrderRequest request) {
        // 1. Find the party's ASSET wallet
        Wallet assetWallet = findAssetWalletByPartyId(request.getPartyId());

        // 2. Create and save the new order
        Order newOrder = new Order();
        newOrder.setWalletId(assetWallet.getId());
        newOrder.setAssetId(request.getAssetId());
        newOrder.setOrderType(request.getOrderType().toUpperCase());
        newOrder.setQuantity(request.getQuantity());
        newOrder.setPrice(request.getPrice());
        newOrder.setStatus("OPEN");

        return orderRepository.save(newOrder);
    }

    // Helper method to find a user's asset wallet
    private Wallet findAssetWalletByPartyId(Long partyId) {
        return walletRepository.findByPartyId(partyId).stream()
                .filter(w -> "ASSET".equals(w.getWalletType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ASSET wallet found for party ID: " + partyId));
    }

    // Helper method to create a new, empty position
    private Position createNewPosition(Long walletId, Long assetId) {
        Position newPosition = new Position();
        newPosition.setWalletId(walletId);
        newPosition.setAssetId(assetId);
        newPosition.setQuantity(BigDecimal.ZERO);
        return newPosition;
    }

    // In TradingService.java (add this new method)
    @Autowired private TradeRepository tradeRepository;
    @Transactional
    public void runMatchingEngineForAsset(Long assetId) {
        // 1. Fetch all open buy and sell orders for the asset, correctly sorted.
        List<Order> buyOrders = orderRepository.findOpenBuyOrders(assetId, "BUY");
        List<Order> sellOrders = orderRepository.findOpenSellOrders(assetId, "SELL");

        int matchesFound = 0;

        // 2. Loop through each buy order and try to match it against available sell orders.
        for (Order buyOrder : buyOrders) {
            for (Order sellOrder : sellOrders) {
                // Skip orders that we have already filled in this run.
                if (!"OPEN".equals(buyOrder.getStatus()) || !"OPEN".equals(sellOrder.getStatus())) {
                    continue;
                }

                // Match condition: The buyer's bid is greater than or equal to the seller's ask.
                if (buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0) {
                    // 3. A match is found!

                    // Determine the quantity of the trade (the smaller of the two orders).
                    BigDecimal tradeQuantity = buyOrder.getQuantity().min(sellOrder.getQuantity());

                    // The trade price is the price of the older order (the sell order, which was listed first).
                    BigDecimal tradePrice = sellOrder.getPrice();

                    // 4. Create and save the UNSETTLED trade.
                    Trade newTrade = new Trade();
                    newTrade.setBuyOrderId(buyOrder.getId());
                    newTrade.setSellOrderId(sellOrder.getId());
                    newTrade.setAssetId(assetId);
                    newTrade.setQuantity(tradeQuantity);
                    newTrade.setPrice(tradePrice);
                    newTrade.setStatus("UNSETTLED");
                    tradeRepository.save(newTrade); // We need to inject TradeRepository

                    // 5. Update the orders' statuses.
                    // For simplicity, we'll mark them as FILLED. A real system would handle partial fills.
                    buyOrder.setStatus("FILLED");
                    sellOrder.setStatus("FILLED");
                    orderRepository.save(buyOrder);
                    orderRepository.save(sellOrder);

                    // 6. Record the audit event for the match.
                    String payload = String.format(
                            "{\"tradeId\": %d, \"buyOrderId\": %d, \"sellOrderId\": %d, \"assetId\": %d, \"quantity\": %s, \"price\": %s}",
                            newTrade.getId(), buyOrder.getId(), sellOrder.getId(), assetId, tradeQuantity.toPlainString(), tradePrice.toPlainString()
                    );
                    auditService.appendEvent("ORDERS_MATCHED", payload);

                    matchesFound++;

                    // Since this sell order has been filled, break the inner loop to move to the next buy order.
                    break;
                }
            }
        }

        if (matchesFound > 0) {
            System.out.printf("Matching Engine: Found and processed %d new trades for asset ID %d.%n", matchesFound, assetId);
        }
    }
}