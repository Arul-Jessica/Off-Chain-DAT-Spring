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
}