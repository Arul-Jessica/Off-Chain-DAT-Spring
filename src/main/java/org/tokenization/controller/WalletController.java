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
}