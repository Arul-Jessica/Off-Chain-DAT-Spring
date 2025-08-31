package com.tokenization.controller;

import com.tokenization.dto.TokenizeAssetRequest;
import com.tokenization.entity.Asset;
import com.tokenization.service.TokenizationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    @Autowired
    private TokenizationService tokenizationService;

    @PostMapping("/tokenize")
    @Operation(summary = "Tokenize a new asset")
    public ResponseEntity<Asset> tokenizeAsset(@RequestBody TokenizeAssetRequest request) {
        Asset newAsset = tokenizationService.tokenizeNewAsset(request);
        return ResponseEntity.ok(newAsset);
    }
}