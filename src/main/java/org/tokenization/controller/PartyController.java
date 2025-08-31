package com.tokenization.controller;

import com.tokenization.dto.CreatePartyRequest;
import com.tokenization.dto.PartyResponse;
import com.tokenization.entity.Party;
import com.tokenization.service.PartyService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parties")
public class PartyController {

    @Autowired
    private PartyService partyService;

    @PostMapping
    @Operation(summary = "Create a new party and their wallets")
    public ResponseEntity<PartyResponse> createParty(@RequestBody CreatePartyRequest request) {
        Party newParty = partyService.createPartyAndWallets(request);
        PartyResponse response = new PartyResponse();
        response.setId(newParty.getId());
        response.setName(newParty.getName());
        response.setKycLevel(newParty.getKycLevel());
        response.setStatus(newParty.getStatus());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}