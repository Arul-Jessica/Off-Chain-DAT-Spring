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
        // 1. Create and save the party
        Party newParty = new Party();
        newParty.setName(request.getName());
        newParty.setKycLevel(request.getKycLevel());
        newParty.setStatus("ACTIVE");
        Party savedParty = partyRepository.save(newParty);

        // 2. Create an ASSET wallet for the new party
        Wallet assetWallet = new Wallet();
        assetWallet.setPartyId(savedParty.getId());
        assetWallet.setWalletType("ASSET");
        walletRepository.save(assetWallet);

        // 3. Create a CASH wallet for the new party
        Wallet cashWallet = new Wallet();
        cashWallet.setPartyId(savedParty.getId());
        cashWallet.setWalletType("CASH");
        walletRepository.save(cashWallet);

        return savedParty;
    }
}