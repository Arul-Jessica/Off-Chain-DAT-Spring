package com.tokenization.service;

import com.tokenization.dto.TokenizeAssetRequest;
import com.tokenization.entity.Asset;
import com.tokenization.entity.Position;
import com.tokenization.entity.Wallet;
import com.tokenization.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TokenizationService {

    @Autowired private AssetRepository assetRepository;
    @Autowired private PositionRepository positionRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private AuditService auditService;

    @Transactional
    public Asset tokenizeNewAsset(TokenizeAssetRequest request) {
        // Find the issuer's asset wallet
        List<Wallet> wallets = walletRepository.findByPartyId(request.getIssuerPartyId());
        Wallet issuerAssetWallet = wallets.stream()
                .filter(w -> "ASSET".equals(w.getWalletType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ASSET wallet found for issuer party: " + request.getIssuerPartyId()));

        // 1. Create the Asset
        Asset newAsset = new Asset();
        newAsset.setName(request.getName());
        newAsset.setSymbol(request.getSymbol());
        newAsset.setTotalSupply(request.getTotalSupply());
        newAsset.setIssuerId(request.getIssuerPartyId());
        assetRepository.save(newAsset);

        // 2. Create the initial Position for the issuer (minting)
        Position issuerPosition = new Position();
        issuerPosition.setAssetId(newAsset.getId());
        issuerPosition.setWalletId(issuerAssetWallet.getId());
        issuerPosition.setQuantity(request.getTotalSupply());
        positionRepository.save(issuerPosition);

        // 3. Create the Audit Log Event
        String payload = String.format(
                "{\"assetId\": %d, \"symbol\": \"%s\", \"totalSupply\": %s, \"issuerId\": %d}",
                newAsset.getId(), newAsset.getSymbol(), newAsset.getTotalSupply().toPlainString(), newAsset.getIssuerId()
        );
        auditService.appendEvent("ASSET_ISSUED", payload);

        return newAsset;
    }
}