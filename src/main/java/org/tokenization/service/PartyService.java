// File: src/main/java/com/tokenization/service/PartyService.java
package com.tokenization.service;

import com.tokenization.dto.CreatePartyRequest;
import com.tokenization.entity.Party;
import com.tokenization.entity.Wallet;
import com.tokenization.repository.PartyRepository;
import com.tokenization.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartyService {

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private WalletRepository walletRepository;

    /**
     * Creates a new Party and its default ASSET and CASH wallets.
     * The @Transactional annotation ensures that all database operations within this method
     * either all succeed together, or all fail together (rollback).
     *
     * @param request The DTO containing the new party's information.
     * @return The newly created and saved Party entity.
     */
    @Transactional
    public Party createPartyAndWallets(CreatePartyRequest request) {
        // 1. Create and save the Party entity.
        Party newParty = new Party();
        newParty.setName(request.getName());
        newParty.setKycLevel(request.getKycLevel());
        newParty.setStatus("ACTIVE"); // Set a default status.

        // The save method returns the persisted entity, now containing the auto-generated ID.
        Party savedParty = partyRepository.save(newParty);

        // 2. Create the ASSET wallet, linked to the new party's ID.
        Wallet assetWallet = new Wallet();
        assetWallet.setPartyId(savedParty.getId());
        assetWallet.setWalletType("ASSET");
        walletRepository.save(assetWallet);

        // 3. Create the CASH wallet, linked to the new party's ID.
        Wallet cashWallet = new Wallet();
        cashWallet.setPartyId(savedParty.getId());
        cashWallet.setWalletType("CASH");
        walletRepository.save(cashWallet);

        // Note: We don't have an audit log event for creating a party yet, but we could add it here.

        return savedParty;
    }
}