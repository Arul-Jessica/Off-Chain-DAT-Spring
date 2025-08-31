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

    // The @Transactional annotation replaces all our manual connection.commit/rollback logic!
    @Transactional
    public Party createPartyAndWallets(CreatePartyRequest request) {
        Party newParty = new Party();
        newParty.setName(request.getName());
        newParty.setKycLevel(request.getKycLevel());
        newParty.setStatus("ACTIVE"); // Set a default status

        // Spring Data JPA saves the entity and populates the generated ID for us.
        Party savedParty = partyRepository.save(newParty);

        // Create the associated wallets using the new party's ID
        Wallet assetWallet = new Wallet();
        assetWallet.setPartyId(savedParty.getId());
        assetWallet.setWalletType("ASSET");
        walletRepository.save(assetWallet);

        Wallet cashWallet = new Wallet();
        cashWallet.setPartyId(savedParty.getId());
        cashWallet.setWalletType("CASH");
        walletRepository.save(cashWallet);

        return savedParty;
    }
}