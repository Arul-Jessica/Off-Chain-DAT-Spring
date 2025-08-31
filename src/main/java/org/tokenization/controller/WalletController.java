package com.tokenization.controller;

import com.tokenization.dto.WalletBalanceResponse;
import com.tokenization.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/{partyId}/balances")
    @Operation(summary = "Get all cash and token balances for a party")
    public ResponseEntity<WalletBalanceResponse> getBalances(@PathVariable Long partyId) {
        WalletBalanceResponse balances = walletService.getWalletBalances(partyId);
        return ResponseEntity.ok(balances);
    }

    @PostMapping("/seed-cash")
    @Operation(summary = "Seed cash into a party's wallet (Admin function)",
            description = "Adds a specified amount of a currency to a party's cash wallet. This is an administrative action.")
// NOTE: When we add security, we will lock this endpoint down with @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericResponse> seedCash(@RequestBody SeedCashRequest request) {
        walletService.seedCash(request);
        String message = String.format("Successfully seeded %s %s to party %d", request.getAmount(), request.getCurrency(), request.getPartyId());
        return ResponseEntity.ok(new GenericResponse(message));
}