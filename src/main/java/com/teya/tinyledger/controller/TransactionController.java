package com.teya.tinyledger.controller;

import com.teya.tinyledger.dto.TransactionHistoryResponse;
import com.teya.tinyledger.dto.TransactionRequest;
import com.teya.tinyledger.dto.TransactionResponse;
import com.teya.tinyledger.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transactions", description = "APIs for managing account transactions (deposits and withdrawals)")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/accounts/transactions/deposit")
    @Operation(
            summary = "Deposit money to an account",
            description = "Adds funds to the specified account. The amount must be greater than zero.",
            tags = {"Transactions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Deposit processed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - amount must be positive, account ID is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody TransactionRequest transactionRequest) {
        transactionService.addDeposit(transactionRequest);
        TransactionResponse response = new TransactionResponse(
                "Deposit processed successfully",
                HttpStatus.CREATED.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/accounts/transactions/withdrawal")
    @Operation(
            summary = "Withdraw money from an account",
            description = "Removes funds from the specified account. Withdrawal fails if insufficient balance exists.",
            tags = {"Transactions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Withdrawal processed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - amount must be positive, insufficient balance, or account not found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<TransactionResponse> withdrawal(@Valid @RequestBody TransactionRequest transactionRequest) {
        transactionService.addWithdrawal(transactionRequest);
        TransactionResponse response = new TransactionResponse(
                "Withdrawal processed successfully",
                HttpStatus.CREATED.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    @Operation(
            summary = "Get transaction history for an account",
            description = "Retrieves the complete transaction history for the specified account, sorted by timestamp with most recent transactions first.",
            tags = {"Transactions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction history retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionHistoryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(
            @Parameter(description = "The unique identifier (UUID) of the account", required = true)
            @PathVariable UUID accountId) {
        try {
            TransactionHistoryResponse history = transactionService.getTransactionHistory(accountId);
            return ResponseEntity.ok(history);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}


