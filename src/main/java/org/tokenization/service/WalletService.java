package com.tokenization.service;

import com.tokenization.dto.WalletBalanceResponse;
import com.tokenization.entity.*;
import com.tokenization.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletService {

    @Autowired private PartyRepository partyRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private CashLedgerRepository cashLedgerRepository;
    @Autowired private PositionRepository positionRepository;
    @Autowired private AssetRepository assetRepository; // Needed to get asset symbols

    @Transactional(readOnly = true) // readOnly is a performance optimization for queries
    public WalletBalanceResponse getWalletBalances(Long partyId) {
        partyRepository.findById(partyId).orElseThrow(() -> new IllegalArgumentException("Party not found"));

        List<Wallet> wallets = walletRepository.findByPartyId(partyId);
        Wallet assetWallet = wallets.stream().filter(w -> "ASSET".equals(w.getWalletType())).findFirst().orElseThrow();
        Wallet cashWallet = wallets.stream().filter(w -> "CASH".equals(w.getWalletType())).findFirst().orElseThrow();

        // 1. Get Cash Balances
        List<WalletBalanceResponse.CashBalance> cashBalances = cashLedgerRepository.findByWalletId(cashWallet.getId())
                .stream()
                .map(ledger -> {
                    var cb = new WalletBalanceResponse.CashBalance();
                    cb.setWalletId(ledger.getWalletId());
                    cb.setCurrency(ledger.getCurrency());
                    cb.setAmount(ledger.getAmount());
                    return cb;
                }).collect(Collectors.toList());

        // 2. Get Token Balances
        List<WalletBalanceResponse.TokenBalance> tokenBalances = positionRepository.findByWalletId(assetWallet.getId())
                .stream()
                .map(position -> {
                    var tb = new WalletBalanceResponse.TokenBalance();
                    Asset asset = assetRepository.findById(position.getAssetId()).orElseThrow();
                    tb.setWalletId(position.getWalletId());
                    tb.setAssetId(position.getAssetId());
                    tb.setAssetSymbol(asset.getSymbol());
                    tb.setQuantity(position.getQuantity());
                    return tb;
                }).collect(Collectors.toList());

        WalletBalanceResponse response = new WalletBalanceResponse();
        response.setCashBalances(cashBalances);
        response.setTokenBalances(tokenBalances);
        return response;
    }
}