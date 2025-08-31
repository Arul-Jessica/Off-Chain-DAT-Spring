package com.tokenization.controller;

import com.tokenization.dto.GenericResponse;
import com.tokenization.dto.PlaceOrderRequest;
import com.tokenization.dto.TransferRequest;
import com.tokenization.entity.Order;
import com.tokenization.service.TradingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trading")
public class TradingController {

    @Autowired
    private TradingService tradingService;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer tokens directly between parties")
    public ResponseEntity<GenericResponse> transferTokens(@RequestBody TransferRequest request) {
        tradingService.transferTokens(request);
        return ResponseEntity.ok(new GenericResponse("Transfer successful."));
    }

    @PostMapping("/orders")
    @Operation(summary = "Place a new BUY or SELL order")
    public ResponseEntity<Order> placeOrder(@RequestBody PlaceOrderRequest request) {
        Order newOrder = tradingService.placeOrder(request);
        return ResponseEntity.ok(newOrder);
    }
}