// File: src/main/java/com/tokenization/service/TokenizationService.java
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

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AuditService auditService; // Injecting our audit service.

    /**
     * Creates a new digital Asset, mints the total supply to the issuer's wallet,
     * and records the action in the audit log.
     *
     * @param request The DTO containing the new asset's information.
     * @return The newly created and saved Asset entity.
     */
    @Transactional
    public Asset tokenizeNewAsset(TokenizeAssetRequest request) {
        // 1. Find the issuer's ASSET wallet to credit the new tokens to.
        List<Wallet> wallets = walletRepository.findByPartyId(request.getIssuerPartyId());
        Wallet issuerAssetWallet = wallets.stream()
                .filter(w -> "ASSET".equals(w.getWalletType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ASSET wallet found for issuer party ID: " + request.getIssuerPartyId()));

        // 2. Create and save the new Asset entity.
        Asset newAsset = new Asset();
        newAsset.setName(request.getName());
        newAsset.setSymbol(request.getSymbol());
        newAsset.setTotalSupply(request.getTotalSupply());
        newAsset.setIssuerId(request.getIssuerPartyId());
        assetRepository.save(newAsset);

        // 3. Create the initial Position for the issuer (minting the total supply).
        Position issuerPosition = new Position();
        issuerPosition.setAssetId(newAsset.getId());
        issuerPosition.setWalletId(issuerAssetWallet.getId());
        issuerPosition.setQuantity(request.getTotalSupply());
        positionRepository.save(issuerPosition);

        // 4. Create the JSON payload for the audit log.
        String payload = String.format(
                "{\"assetId\": %d, \"symbol\": \"%s\", \"totalSupply\": %s, \"issuerId\": %d}",
                newAsset.getId(), newAsset.getSymbol(), newAsset.getTotalSupply().toPlainString(), newAsset.getIssuerId()
        );

        // 5. Call the AuditService to append the event to the log.
        auditService.appendEvent("ASSET_ISSUED", payload);

        return newAsset;
    }
}