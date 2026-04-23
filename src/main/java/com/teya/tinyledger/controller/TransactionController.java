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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/transactions")
@Tag(name = "Transactions", description = "APIs for managing account transactions (deposits and withdrawals)")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(
            summary = "Create a transaction for an account",
            description = "Creates a deposit or withdrawal transaction for the specified account.",
            tags = {"Transactions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - amount must be positive"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Service Unavailable. Transaction has been queued for automatic retry.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example =
                            "{\"message\": \"Transaction not persisted, queued for retry\", " +
                            "\"statusCode\": 503}"))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<TransactionResponse> createTransaction(
            @Parameter(description = "The unique identifier of the account", required = true)
            @PathVariable String accountId,
            @Valid @RequestBody TransactionRequest transactionRequest) {
        var transaction = transactionService.addTransaction(accountId, transactionRequest);
        TransactionResponse response = new TransactionResponse(
                transaction.id(),
                accountId,
                transaction.amount(),
                transaction.transactionType(),
                transaction.createdAt()
        );
        return ResponseEntity.status(CREATED).body(response);
    }

    @GetMapping
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
            @Parameter(description = "The unique identifier of the account", required = true)
            @PathVariable String accountId) {
        TransactionHistoryResponse history = transactionService.getTransactionHistory(accountId);
        return ResponseEntity.ok(history);
    }
}
