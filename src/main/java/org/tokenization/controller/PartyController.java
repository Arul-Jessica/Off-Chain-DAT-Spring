package com.tokenization.controller;

import com.tokenization.dto.CreatePartyRequest;
import com.tokenization.dto.PartyResponse;
import com.tokenization.entity.Party;
import com.tokenization.service.PartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parties")
public class PartyController {

    @Autowired
    private PartyService partyService;

    @PostMapping
    @Operation(summary = "Create a new party", description = "Creates a new party (user/issuer) and their associated ASSET and CASH wallets.")
    @ApiResponse(responseCode = "201", description = "Party created successfully")
    public ResponseEntity<PartyResponse> createParty(@RequestBody CreatePartyRequest request) {
        Party newParty = partyService.createPartyAndWallets(request);

        // Convert the saved Party entity to a DTO for the response
        PartyResponse response = new PartyResponse();
        response.setId(newParty.getId());
        response.setName(newParty.getName());
        response.setKycLevel(newParty.getKycLevel());
        response.setStatus(newParty.getStatus());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}